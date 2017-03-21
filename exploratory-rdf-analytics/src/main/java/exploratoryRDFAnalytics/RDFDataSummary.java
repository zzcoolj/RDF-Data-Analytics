package exploratoryRDFAnalytics;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author Zheng ZHANG
 */

public class RDFDataSummary {
	
	PostgresDatabaseHandler_Plus pgdbh;
	StatementGenerator sg;
	Visualization_D3 vd3;
	int numProperties;
	int fetchSize;
	private ArrayList<String> distinctProperties;
	private ArrayList<String> types;
	
	public RDFDataSummary(PostgresDatabaseHandler_Plus pgdbh,String tableName, int numProperties, int fetchSize) {
		super();
		this.pgdbh = pgdbh;
		this.numProperties = numProperties;
		this.fetchSize = fetchSize;
		this.sg = new StatementGenerator(tableName, numProperties);
		this.vd3 = new Visualization_D3();
		pgdbh.setFetchSize(fetchSize);
	}

	@SuppressWarnings("unused")
	public void getFrequentItemSetAndCount() throws SQLException, IOException, URISyntaxException, NoTypePropertyException {		
		// Step1: Select distinct properties
		selectDistinctProperties();	
		// Step2: Generate pivot table of the original table by executing SQL: tableName.crosstab()
		generatePivotTable();
		// Step3: Group by the pivot table and generate csv files for visualization
		groupByPivotTableAndGenerateCsvFiles();
		if(GlobalSettings.ANALYSIS_BY_TYPE) {
			// Step4: Generate part of the pivot table of different types and re-use the function of Step3
			pivotTableAnalysisByType();
		} else {
			vd3.launchBrowserToShowSequencesSunburstOfAllSubjects();
		}
		// Step5: Execute SQL drop table crosstab table and properties table.
		dropAllTempTables();
	}

	
	
	
	private HashMap<String, Integer> getTypeFrequencyInformation() throws IOException, NoTypePropertyException, SQLException, URISyntaxException {
		HashMap<String, Integer> typeFrequency = new HashMap<String, Integer>();
		String sqlQuerySelectDistinctType = sg.getStatementSelectDistinctType(distinctProperties);
		types = pgdbh.getSelectDistinctTypeStatementResults(sqlQuerySelectDistinctType);
		for (String s : types) {
			String sqlQueryCountTypeFrequency = sg.getStatementCountTypeFrequency(s);
			int frequencyOfType = pgdbh.getCountTypeFrequencyStatementResults(sqlQueryCountTypeFrequency);
			typeFrequency.put(s, frequencyOfType);
		}
		vd3.writeTypeFrequencyFile(typeFrequency);
		vd3.launchBrowserToShowPieChart();
		return typeFrequency;
	}
	
	private void selectDistinctProperties() throws SQLException {
		long tStart = System.currentTimeMillis();
		String sqlQuerySelectDistinctProperty = sg.getStatementSelectDistinctProperty();
		distinctProperties = pgdbh.getSelectDistinctPropertyStatementResults(sqlQuerySelectDistinctProperty);
		// numProperties equals 0 => Using all distinct properties as new
		// columns in the pivot table.
		if (numProperties == 0) {
			numProperties = distinctProperties.size();
			sg.setNumProperties(numProperties);
		}
		if (GlobalSettings.SQL_CROSSTAB_QUERY_STRATEGY == 2) {
			pgdbh.executeQuery(sg.getStatementCreatePropertiesTable());
			pgdbh.executeQuery(sg.getStatementInsertValuesIntoPropertiesTable(distinctProperties));
		}
		RDFDataSummary.timerEnd(tStart, "Select distinct properties");
	}
	
	private void generatePivotTable() throws SQLException {
		long tStart = System.currentTimeMillis();
		String sqlQueryCrosstab = sg.getStatementCrosstab();
		pgdbh.executeQuery(sqlQueryCrosstab);
		RDFDataSummary.timerEnd(tStart, "Crosstab");
	}
	
	private void groupByPivotTableAndGenerateCsvFiles() throws SQLException, IOException, URISyntaxException {
		long tStart = System.currentTimeMillis();
		// propertyOrder is not used when SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY is 1
		HashMap<String, Integer> propertyOrder = new HashMap<String, Integer>();
		HashMap<String, Integer> propertyCombinationCount = new HashMap<String, Integer>();
		if (GlobalSettings.SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY == 1) {
			String sqlQueryCountFrequenceGroupbyOfCrosstab = sg.getStatementCountFrequenceGroupbyOfCrosstab();
			propertyCombinationCount = pgdbh.getCountFrequenceGroupbyOfCrosstabStatementResults(sqlQueryCountFrequenceGroupbyOfCrosstab, distinctProperties, numProperties, GlobalSettings.GROUP_BY_PROPERTIES_RESULT_FILE_NAME);
		} else if (GlobalSettings.SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY == 2) {
			String sqlQueryCountFrequenceGroupbyCubeOfCrosstab = sg.getStatementCountFrequenceGroupbyCubeOfCrosstab();
			propertyCombinationCount = pgdbh.getCountFrequenceGroupbyCubeOfCrosstabStatementResults(sqlQueryCountFrequenceGroupbyCubeOfCrosstab, distinctProperties, propertyOrder, numProperties, GlobalSettings.ORDERED_PROPERTIES_FILE_NAME, GlobalSettings.GROUP_BY_PROPERTIES_RESULT_FILE_NAME);
		} else {
			String sqlQueryCountFrequenceGroupbyOfCrosstab = sg.getStatementCountFrequenceGroupbyOfCrosstab();
			propertyCombinationCount = pgdbh.getCountFrequenceGroupbyOfCrosstabStatementResultsWithPropertyOrder(sqlQueryCountFrequenceGroupbyOfCrosstab, distinctProperties, propertyOrder, numProperties, GlobalSettings.ORDERED_PROPERTIES_FILE_NAME, GlobalSettings.PROPERTY_FREQUENCE_FILE_NAME, GlobalSettings.GROUP_BY_PROPERTIES_RESULT_FILE_NAME);
		}
		RDFDataSummary.timerEnd(tStart, "Group by");

		// Generate csv files for visualization
		vd3.resultTranslatorForSequencesSunburst(propertyCombinationCount, propertyOrder, GlobalSettings.D3_SEQUENCES_SUNBURST_FILE_NAME, pgdbh.getNumPropertiesFrequencyNotZero(), true, sg.getTypePropertyPosition());
		vd3.resultTranslatorForBarChart(propertyCombinationCount, GlobalSettings.D3_BAR_CHART_FILE_NAME);
		vd3.resultTranslatorForBarChart_forPropertyCombinationCount(propertyCombinationCount, distinctProperties, 10, GlobalSettings.D3_BAR_CHART_FOR_PROPERTY_COMBINATION_COUNT_FILE_NAME);
	}
	
	// TODO: Only works when GlobalSettings.SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY is 3 now
	// Re-use the function of step3: similar to groupByPivotTableAndGenerateCsvFiles()
	private void groupByPivotTableOfSpecificTypeAndGenerateCsvFiles(String type) throws SQLException, IOException, URISyntaxException {
		String shorterType = StatementGenerator.getShorterTypeNameTemp(type);
		// Timer
		long tStart = System.currentTimeMillis();
		// propertyOrder is not used when SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY is 1
		HashMap<String, Integer> propertyOrder = new HashMap<String, Integer>();
		HashMap<String, Integer> propertyCombinationCount = new HashMap<String, Integer>();
		if (GlobalSettings.SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY == 3) {
			String sqlQueryCountFrequenceGroupbyOfCrosstab = sg.getStatementCountFrequenceGroupbyOfCrosstab(sg.getCrosstabedTypeTableName(type));
			propertyCombinationCount = pgdbh.getCountFrequenceGroupbyOfCrosstabStatementResultsWithPropertyOrder(
					sqlQueryCountFrequenceGroupbyOfCrosstab, distinctProperties, propertyOrder, numProperties, 
					GlobalSettings.ORDERED_PROPERTIES_FILE_NAME + "_type_" + shorterType, 
					GlobalSettings.PROPERTY_FREQUENCE_FILE_NAME + "_type_" + shorterType, 
					GlobalSettings.GROUP_BY_PROPERTIES_RESULT_FILE_NAME + "_type_" + shorterType);
		}
		RDFDataSummary.timerEnd(tStart, "Group by");

		// Generate csv files for visualization
		vd3.resultTranslatorForSequencesSunburst(propertyCombinationCount, propertyOrder, GlobalSettings.D3_SEQUENCES_SUNBURST_FILE_NAME + "_type_" + shorterType, pgdbh.getNumPropertiesFrequencyNotZero(), false, sg.getTypePropertyPosition());
		vd3.resultTranslatorForBarChart(propertyCombinationCount, GlobalSettings.D3_BAR_CHART_FILE_NAME + "_type_" + shorterType);
		vd3.resultTranslatorForBarChart_forPropertyCombinationCount(propertyCombinationCount, distinctProperties, 10, GlobalSettings.D3_BAR_CHART_FOR_PROPERTY_COMBINATION_COUNT_FILE_NAME + "_type_" + shorterType);
	}
	
	private void pivotTableAnalysisByType() throws IOException, NoTypePropertyException, SQLException, URISyntaxException {
		HashMap<String, Integer> typeFrequency = getTypeFrequencyInformation();
		System.out.println();
		for (Entry<String, Integer> entry : typeFrequency.entrySet()) {
			String type = entry.getKey();
			Integer frequency = entry.getValue();
//			// Show typeFrequency
//			System.out.println(type + "-->" + frequency);
			// Create table by type
			String sqlQueryCreateTableBySpecifyType = sg.getStatementCreateTableBySpecifyType(type);
			pgdbh.executeQuery(sqlQueryCreateTableBySpecifyType);
			// Re-use the function of step3: similar to groupByPivotTableAndGenerateCsvFiles()
			groupByPivotTableOfSpecificTypeAndGenerateCsvFiles(type);
		}
	}
	
	private void dropAllCrosstabedTypeTable() throws SQLException {
		for(String s: types) {
			String crosstabedTypeTableName = sg.getCrosstabedTypeTableName(s);
			String sqlQueryDropTable = sg.getStatementDropTable(crosstabedTypeTableName);
			pgdbh.executeQuery(sqlQueryDropTable);
		}
	}
	
	private void dropAllTempTables() throws SQLException {
//		pgdbh.executeQuery(sg.getStatementDropCrosstabTable());
		if(GlobalSettings.SQL_CROSSTAB_QUERY_STRATEGY == 2) {
			pgdbh.executeQuery(sg.getStatementDropPropertiesTable());
		}
		if(GlobalSettings.ANALYSIS_BY_TYPE) {
			dropAllCrosstabedTypeTable();
		}
	}
	
	private static void timerEnd(long tStart, String operations) {
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = tDelta / 1000.0;
		System.out.println("[" + operations + " takes " + elapsedSeconds + " seconds]");
	}
	
	public static void main(String[] args) throws Exception {
		GlobalSettings.configPostgresServer();
//		PostgresDataSource_Plus pgds = new PostgresDataSource_Plus(
//				GlobalSettings.SERVER_NAME,
//				Integer.valueOf(GlobalSettings.SERVER_PORT),
//				GlobalSettings.SERVER_DATABASE_NAME,
//				GlobalSettings.SERVER_USER, 
//				GlobalSettings.SERVER_PASSWORD);
		PostgresDataSource_Plus pgds = new PostgresDataSource_Plus();
		PostgresDatabaseHandler_Plus pgdbh = new PostgresDatabaseHandler_Plus(pgds.getConnection());
//		PostgresDatabaseHandler_Plus pgdbh = new PostgresDatabaseHandler_Plus();

//		RDFDataSummary pcc = new RDFDataSummary(pgdbh, GlobalSettings.TEST_TABLE_NAME, 0, 0);
		RDFDataSummary pcc = new RDFDataSummary(pgdbh, GlobalSettings.DBLP_COMPLETE_TABLE_NAME, 0, 0);
//		RDFDataSummary pcc = new RDFDataSummary(pgdbh, args[0], 0, 0);
//		RDFDataSummary pcc = new RDFDataSummary(pgdbh, "model", 0, 0);
		pcc.getFrequentItemSetAndCount();
	}
}
