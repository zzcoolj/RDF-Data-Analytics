package exploratoryRDFAnalytics;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Global settings, variable names...
 * @author Zheng ZHANG
 */

public class GlobalSettings {
	// Postgres server 
	public static String SERVER_CLASS_NAME = "org.postgresql.Driver";
	public static String SERVER_URL = "jdbc:postgresql://localhost:5432/mydb";
	public static String SERVER_USER = "zzcoolj";
	public static String SERVER_PASSWORD = "";
	public static String SERVER_NAME = "localhost";
	public static String SERVER_PORT = "5432";
	public static String SERVER_DATABASE_NAME = "mydb";
	
	// Query setting
	/*
	 * crosstab(text source_sql, text category_sql) 
	 * 
	 * Strategy 1: category_sql is
	 * like 'SELECT DISTINCT property FROM testTable ORDER BY 1 LIMIT 3', same
	 * as select distinct property query. 
	 * 
	 * Strategy 2: After getting result of
	 * select distinct property query, store the result into properties table.
	 * Then the category_sql should be 'SELECT * FROM properties'
	 */
	public static final int SQL_CROSSTAB_QUERY_STRATEGY = 1;
	/*
	 * Strategy 1: SELECT DISTINCT property FROM testTable ORDER BY 1.
	 * e.g. (s1,p2,o3) and (s1,p2,o6), p2 has been counted 2 times
	 * 
	 * Strategy 2: Get the distinct property order by analyzing the group by cube result of the pivot table. 
	 * e.g. (s1,p2,o3) and (s1,p2,o6), p2 has been counted only 1 time
	 * drawbacks of strategy 2: Group by cube takes too much time to execute; Cube can only take at most 12 elements.
	 * 
	 * Strategy 3: Get the distinct property order by analyzing the result of group by of crosstab table(result of strategy 1).
	 * e.g. (s1,p2,o3) and (s1,p2,o6), p2 has been counted only 1 time
	 */
	public static final int SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY = 3;
	
	public static final boolean ANALYSIS_BY_TYPE = true;
	
	
	/*
	 * PATHMODE 1: Execute in eclipse
	 * PATHMODE 2: Execute by jar
	 */
	public final static int PATHMODE = 1;
	
	
	
	// DB result
	public static String GROUP_BY_PROPERTIES_RESULT_FILE_FOLDER_PATH = "data/";
	public static final String GROUP_BY_PROPERTIES_RESULT_FILE_NAME = "group_by_properties_result";
	
	// DB table name
	public static final String TEST_TABLE_NAME = "testTable";
	public static final String LUBM_1MTRIPLES_TABLE_NAME = "lubm_1Mtriples";
	public static final String DBLP_FORTH_TABLE_NAME = "dblp_forth";
	public static final String DBLP_COMPLETE_TABLE_NAME = "dblp_complete";
	
	public static final String PROPERTIES_TABLE_NAME = "properties";
	
	// Table column names
	public static final String SUBJECT_COLUMN_NAME = "subject";
	public static final String PROPERTY_COLUMN_NAME = "property";
	public static final String OBJECT_COLUMN_NAME = "object";
	public static final String COUNT_COLUMN_NAME = "count";
	
	// Table column types
	public static final String SUBJECT_COLUMN_TYPE = "text";
	public static final String PROPERTY_COLUMN_TYPE = "text";
	
	// D3.js
	public static String ORDERED_PROPERTIES_FILE_FOLDER_PATH = "../d3/sequences_sunburst/data/";
	public static final String ORDERED_PROPERTIES_FILE_NAME = "ordered_properties";

	public static String PROPERTY_FREQUENCE_FILE_FOLDER_PATH = "../d3/sequences_sunburst/data/";
	public static final String PROPERTY_FREQUENCE_FILE_NAME = "property_frequency";
	
	public static String D3_SEQUENCES_SUNBURST_FILE_FOLDER_PATH = "../d3/sequences_sunburst/data/";
	public static final String D3_SEQUENCES_SUNBURST_FILE_NAME = "d3_sequences_sunburst";
	public static  String D3_SEQUENCES_SUNBURST_URL = "file:///Users/zzcoolj/rdfanalytics/trunk/Codes/d3/sequences_sunburst/index.html";
	
	public static String D3_BAR_CHART_FILE_FOLDER_PATH = "../d3/sequences_sunburst/data/";
	public static final String D3_BAR_CHART_FILE_NAME = "bar-data";
	
	public static String D3_BAR_CHART_FOR_PROPERTY_COMBINATION_COUNT_FILE_FOLDER_PATH = "../d3/sequences_sunburst/data/";
	public static final String D3_BAR_CHART_FOR_PROPERTY_COMBINATION_COUNT_FILE_NAME = "bar_property_combination_count";
	
	public static String D3_PIE_CHART_FILE_PATH = "../d3/bar_chart/pie-data.csv";
	public static String D3_PIE_CHART_URL = "file:///Users/zzcoolj/rdfanalytics/trunk/Codes/d3/bar_chart/index.html";
	
	
	
	
	public static void configPostgresServer() {
		if (PATHMODE == 2) {
			Properties prop = new Properties();
			InputStream input = null;
			try {
				String filename = getJavaFolder() + "config.properties";
				input = new FileInputStream(filename);
				System.err.println(input.toString());
				if (input == null) {
					System.err.println("Sorry, unable to find " + filename);
					System.exit(0);
				}
				// load a properties file from class path, inside static method
				prop.load(input);
				// get the property value
				SERVER_CLASS_NAME = prop.getProperty("SERVER_CLASS_NAME");
				SERVER_URL = prop.getProperty("SERVER_URL"); 
				SERVER_USER = prop.getProperty("SERVER_USER"); 
				SERVER_PASSWORD = prop.getProperty("SERVER_PASSWORD"); 
				SERVER_NAME = prop.getProperty("SERVER_NAME"); 
				SERVER_PORT = prop.getProperty("SERVER_PORT"); 
				SERVER_DATABASE_NAME = prop.getProperty("SERVER_DATABASE_NAME"); 
				
				String javaFolderPath = getJavaFolder();
				GROUP_BY_PROPERTIES_RESULT_FILE_FOLDER_PATH = javaFolderPath + "data/";
				ORDERED_PROPERTIES_FILE_FOLDER_PATH = javaFolderPath + "../d3/sequences_sunburst/data/";
				PROPERTY_FREQUENCE_FILE_FOLDER_PATH = javaFolderPath + "../d3/sequences_sunburst/data/";
				D3_SEQUENCES_SUNBURST_FILE_FOLDER_PATH = javaFolderPath + "../d3/sequences_sunburst/data/";
				D3_SEQUENCES_SUNBURST_URL = "file://" + javaFolderPath + "../d3/sequences_sunburst/index.html";
				D3_BAR_CHART_FILE_FOLDER_PATH = javaFolderPath + "../d3/sequences_sunburst/data/";
				D3_BAR_CHART_FOR_PROPERTY_COMBINATION_COUNT_FILE_FOLDER_PATH = javaFolderPath + "../d3/sequences_sunburst/data/";
				D3_PIE_CHART_FILE_PATH = javaFolderPath + "../d3/bar_chart/pie-data.csv";
				D3_PIE_CHART_URL = "file://" + javaFolderPath + "../d3/bar_chart/index.html";
				
				
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
	/*
	 *  INPUT: 	/Users/zzcoolj/Desktop/level1/level2/test.jar
	 *  OUTPUT:	/Users/zzcoolj/Desktop/level1/level2/
	 */
	private static String getJavaFolder() {
		String fullPath = GlobalSettings.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile(".*/");
		matcher = pattern.matcher(fullPath);
		while (matcher.find()) {
			return matcher.group().toString();
		}
		return null;
	}
}
