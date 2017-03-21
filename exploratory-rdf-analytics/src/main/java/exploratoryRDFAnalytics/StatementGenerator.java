package exploratoryRDFAnalytics;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generate query statements for exploratory RDF analytics.
 * @author Zheng ZHANG
 */

public class StatementGenerator {
	
	private int typePropertyPosition;
	private String tableName;
	int numProperties;
	
	public StatementGenerator(String tableName, int numProperties) {
		super();
		this.tableName = tableName;
		this.numProperties = numProperties;
	}
	
	public void setNumProperties(int numProperties) {
		this.numProperties = numProperties;
	}

	public int getTypePropertyPosition() {
		return typePropertyPosition;
	}

	/* An example of the output
	CREATE TABLE crosstabed_testTable AS
	SELECT * FROM crosstab(
	  'select subject, property, object from testTable order by 1',
	  'SELECT DISTINCT property FROM testTable ORDER BY 1 LIMIT 3'
	)
	AS testTable(subject text, property_1 text, property_2 text, property_3 text);
	*/
	@SuppressWarnings("unused")
	public String getStatementCrosstab() {
		String crosstabedTableName = "crosstabed_" + tableName;
		String statementPropertyPart = "";
		String category_sql = "";
		if(GlobalSettings.SQL_CROSSTAB_QUERY_STRATEGY == 2) {
			category_sql = String.format("  'SELECT * FROM %s'\n", GlobalSettings.PROPERTIES_TABLE_NAME);
		} else {
			category_sql = String.format("  'SELECT DISTINCT %s FROM %s ORDER BY 1 LIMIT %s'\n", GlobalSettings.PROPERTY_COLUMN_NAME, tableName, numProperties);
		}
		for(int i=0; i<numProperties; i++) {
			statementPropertyPart += ", property_" + (i+1) + " " + GlobalSettings.PROPERTY_COLUMN_TYPE;
		}
		return String.format("CREATE TABLE %s AS\n"
				+ "SELECT * FROM crosstab(\n"
				+ "  'select %s, %s, %s from %s order by 1',\n"
				+ "%s"
				+ ")\n"
				+ "AS %s(subject %s%s);"
				, crosstabedTableName
				, GlobalSettings.SUBJECT_COLUMN_NAME, GlobalSettings.PROPERTY_COLUMN_NAME, GlobalSettings.OBJECT_COLUMN_NAME, tableName
				, category_sql
				, tableName, GlobalSettings.SUBJECT_COLUMN_TYPE, statementPropertyPart);
	}
	
	// Used by SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY 2
	/*An example of the output
	 SELECT has_property_1, has_property_2, has_property_3, count FROM 
	 (
	 	select property_1 is not NULL AS has_property_1, 
	 		property_2 is not NULL AS has_property_2, 
	 		property_3 is not NULL AS has_property_3, 
//	 		grouping(property_1 is not NULL,property_2 is not NULL,property_3 is not NULL),
	 		count(*) AS count
	 	from crosstabed_testtable 
	 	group by cube (1,2,3)	 	
	 ) AS a
//	 where (has_property_1 = 't' or has_property_1 is NULL)
//	 and (has_property_2 = 't' or has_property_2 is NULL)
//	 and (has_property_3 = 't' or has_property_3 is NULL);
	 ORDER BY 4 DESC
	 */
	public String getStatementCountFrequenceGroupbyCubeOfCrosstab() {
		String statementPart1 = "";
		String statementPart2 = "";
		String statementPart3 = "";
		String statementPart4 = "";
		String statementPart5 = "";
		
		for(int i=0; i<numProperties; i++) {
			statementPart1 += "has_property_" + (i+1) + ", ";
			statementPart3 += "property_" + (i+1) +" is not NULL,";
			statementPart4 += (i+1) + ",";
			if(i == 0) {
				statementPart2 += "property_" + (i+1) +" is not NULL AS " + "has_property_" + (i+1) + ",\n";
				statementPart5 += "\nwhere (has_property_" + (i+1) + " = 't' or has_property_" + (i+1) + " is NULL)";
			} else {
				statementPart2 += "		property_" + (i+1) +" is not NULL AS " + "has_property_" + (i+1) + ",\n";
				statementPart5 += "\nand (has_property_" + (i+1) + " = 't' or has_property_" + (i+1) + " is NULL)";
			}
		}
		// Remove the last character(",") in statementPart3 and statementPart4
		statementPart3 = statementPart3.substring(0, statementPart3.length()-1);
		statementPart4 = statementPart4.substring(0, statementPart4.length()-1);
		
		return String.format("SELECT %s%s FROM\n"
				+ "(\n"
				+ "	select %s"
//				+ "		grouping(%s),\n"
				+ "		count(*) AS %s\n"
				+ "	from crosstabed_%s\n"
				+ "	group by cube (%s)\n"
				+ ") AS a"
//				+ "%s\n"
				+ "	ORDER BY %s DESC;"
				, statementPart1
				, GlobalSettings.COUNT_COLUMN_NAME
				, statementPart2
//				, statementPart3
				, GlobalSettings.COUNT_COLUMN_NAME
				, tableName
				, statementPart4
//				, statementPart5
				, numProperties+1);
	}
	
	// Used by SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY 1
	/*An example of the output
	 SELECT has_property_1, has_property_2, has_property_3, count FROM 
	 (
	 	select property_1 is not NULL AS has_property_1, 
	 		property_2 is not NULL AS has_property_2, 
	 		property_3 is not NULL AS has_property_3, 
	 		grouping(property_1 is not NULL,property_2 is not NULL,property_3 is not NULL),
	 		count(*) AS count
	 	from crosstabed_testtable 
	 	group by (1,2,3)
	 ) AS a
//	 where (has_property_1 is not NULL)
//	 and (has_property_2 is not NULL)
//	 and (has_property_3 is not NULL);
	 ORDER BY 4 DESC
	 */
	public String getStatementCountFrequenceGroupbyOfCrosstab() {
		String statementPart1 = "";
		String statementPart2 = "";
		String statementPart4 = "";
		String statementPart5 = "";
		
		for(int i=0; i<numProperties; i++) {
			statementPart1 += "has_property_" + (i+1) + ", ";
			statementPart4 += (i+1) + ",";
			if(i == 0) {
				statementPart2 += "property_" + (i+1) +" is not NULL AS " + "has_property_" + (i+1) + ",\n";
				statementPart5 += "\nwhere (has_property_" + (i+1) + " is not NULL)";
			} else {
				statementPart2 += "		property_" + (i+1) +" is not NULL AS " + "has_property_" + (i+1) + ",\n";
				statementPart5 += "\nand (has_property_" + (i+1) + " is not NULL)";
			}
		}
		// Remove the last character(",") in statementPart3 and statementPart4
		statementPart4 = statementPart4.substring(0, statementPart4.length()-1);
		
		return String.format("SELECT %s%s FROM\n"
				+ "(\n"
				+ "	select %s"
				+ "		count(*) AS %s\n"
				+ "	from crosstabed_%s\n"
				+ "	group by (%s)\n"
				+ ") AS a"
//				+ "%s\n"
				+ " ORDER BY %s DESC;"
				, statementPart1
				, GlobalSettings.COUNT_COLUMN_NAME
				, statementPart2
				, GlobalSettings.COUNT_COLUMN_NAME
				, tableName
				, statementPart4
//				, statementPart5
				, numProperties+1);
	}
	
	// Expected output: "drop table crosstabed_tableName"
	public String getStatementDropCrosstabTable() {	
		return new String("drop table crosstabed_" + tableName);
	}
	
	/*
	 * An example of the output 
	 * SELECT DISTINCT property FROM testTable ORDER BY 1
	 */
	public String getStatementSelectDistinctProperty() {
		return new String("SELECT DISTINCT "
				+ GlobalSettings.PROPERTY_COLUMN_NAME + " FROM " + tableName
				+ " ORDER BY 1");
	}
	
	/*
	 * An example of the output 
	 * SELECT COUNT(*) FROM (SELECT DISTINCT column_name FROM table_name) AS temp
	 */
	public String getStatementCountDistinctProperties() {
		return new String("SELECT COUNT(*) FROM (SELECT DISTINCT "
				+ GlobalSettings.PROPERTY_COLUMN_NAME + " FROM " + tableName
				+ ") AS temp");
	}
	
	/*
	 * An example of the output 
	 * CREATE TABLE properties(property text)
	 */
	public String getStatementCreatePropertiesTable() {
		return new String("CREATE TABLE "+ GlobalSettings.PROPERTIES_TABLE_NAME +"("+ GlobalSettings.PROPERTY_COLUMN_NAME +" "+ GlobalSettings.PROPERTY_COLUMN_TYPE +")");
	}
	
	/*
	 * An example of the output 
	 * INSERT INTO properties VALUES
	 * ('p1'),
	 * ('p2');
	 */
	public String getStatementInsertValuesIntoPropertiesTable(ArrayList<String> distinctProperties) {
		String propertyValues = "";
		for(int i=0; i<numProperties; i++) {
			propertyValues += "\n('" + distinctProperties.get(i) + "'),";
		}
		propertyValues = propertyValues.substring(0, propertyValues.length()-1);
		return String.format("INSERT INTO %s VALUES%s;", GlobalSettings.PROPERTIES_TABLE_NAME, propertyValues);
	}
	
	/*
	 * An example of the output 
	 * DROP TABLE properties
	 */
	public String getStatementDropPropertiesTable() {
		return new String("DROP TABLE " + GlobalSettings.PROPERTIES_TABLE_NAME);
	}

	/*
	 * An example of the output 
	 * select distinct property_26 as type from crosstabed_dblp_forth
	 */
	public String getStatementSelectDistinctType(ArrayList<String> distinctProperties) throws NoTypePropertyException {
		typePropertyPosition = this.getTypeColumnNameInPivotTable(distinctProperties);
		if(typePropertyPosition == -1) {
			throw new NoTypePropertyException();
		}
		String crosstabedTableName = "crosstabed_" + tableName;
		return String.format("select distinct property_%s as type from %s", String.valueOf(typePropertyPosition), crosstabedTableName);
	}
	
	/*
	 * An example of the output 
	 * select count(*) from crosstabed_dblp_forth where property_26='http://sw.deri.org/~aharth/2004/07/dblp/dblp.owl#Book'
	 */
	public String getStatementCountTypeFrequency(String typeName) {
		String crosstabedTableName = "crosstabed_" + tableName;
		if(typeName != null) {
			return String.format("select count(*) from %s where property_%s='%s'", crosstabedTableName, this.typePropertyPosition, typeName);
		} else {
			return String.format("select count(*) from %s where property_%s is null", crosstabedTableName, this.typePropertyPosition);
		}
	}
	
	/*
	 * An example of the output 
	 * CREATE TABLE crosstabed_dblp_forth_typeBook AS
	 * SELECT * FROM crosstabed_dblp_forth WHERE property_26='http://sw.deri.org/~aharth/2004/07/dblp/dblp.owl#Book';
	 * or
	 * CREATE TABLE crosstabed_dblp_forth_typeNull AS
	 * SELECT * FROM crosstabed_dblp_forth WHERE property_26 IS NULL;
	 */
	public String getStatementCreateTableBySpecifyType(String typeName) {
		String crosstabedTableName = "crosstabed_" + tableName;
		if(typeName != null) {
			String createdTableName = crosstabedTableName + "_type_" + StatementGenerator.getShorterTypeName(typeName);
			return String.format("CREATE TABLE %s AS\n"
					+ "select * from %s where property_%s='%s'", createdTableName, crosstabedTableName, this.typePropertyPosition, typeName);
		} else {
			String createdTableName = crosstabedTableName + "_type_null";
			return String.format("CREATE TABLE %s AS\n"
					+ "select * from %s where property_%s is null", createdTableName, crosstabedTableName, this.typePropertyPosition);
		}
	}
	
	/*
	 * An example of the output 
	 * DROP TABLE properties
	 */
	public String getStatementDropTable(String tableName) {
		return new String("DROP TABLE " + tableName);
	}
	
	/* 
	 * INPUT: 	http://sw.deri.org/~aharth/2004/07/dblp/dblp.owl#Article
	 * OUTPUT:	crosstabed_dblp_forth_type_article
	 */
	public String getCrosstabedTypeTableName(String typeName) {
		String crosstabedTableName = "crosstabed_" + tableName;
		String createdTableName;
		if(typeName != null) {
			createdTableName = crosstabedTableName + "_type_" + StatementGenerator.getShorterTypeName(typeName);	
		} else {
			createdTableName = crosstabedTableName + "_type_null";
		}
		return createdTableName;
	}
	
	// Used by SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY 1
		/*An example of the output
		 SELECT has_property_1, has_property_2, has_property_3, count FROM 
		 (
		 	select property_1 is not NULL AS has_property_1, 
		 		property_2 is not NULL AS has_property_2, 
		 		property_3 is not NULL AS has_property_3, 
		 		grouping(property_1 is not NULL,property_2 is not NULL,property_3 is not NULL),
		 		count(*) AS count
		 	from crosstabed_testtable 
		 	group by (1,2,3)
		 ) AS a
//		 where (has_property_1 is not NULL)
//		 and (has_property_2 is not NULL)
//		 and (has_property_3 is not NULL);
		 ORDER BY 4 DESC
		 */
		public String getStatementCountFrequenceGroupbyOfCrosstab(String crosstabedTableName) {
			String statementPart1 = "";
			String statementPart2 = "";
			String statementPart4 = "";
			String statementPart5 = "";
			
			for(int i=0; i<numProperties; i++) {
				statementPart1 += "has_property_" + (i+1) + ", ";
				statementPart4 += (i+1) + ",";
				if(i == 0) {
					statementPart2 += "property_" + (i+1) +" is not NULL AS " + "has_property_" + (i+1) + ",\n";
					statementPart5 += "\nwhere (has_property_" + (i+1) + " is not NULL)";
				} else {
					statementPart2 += "		property_" + (i+1) +" is not NULL AS " + "has_property_" + (i+1) + ",\n";
					statementPart5 += "\nand (has_property_" + (i+1) + " is not NULL)";
				}
			}
			// Remove the last character(",") in statementPart3 and statementPart4
			statementPart4 = statementPart4.substring(0, statementPart4.length()-1);
			
			return String.format("SELECT %s%s FROM\n"
					+ "(\n"
					+ "	select %s"
					+ "		count(*) AS %s\n"
					+ "	from %s\n"
					+ "	group by (%s)\n"
					+ ") AS a"
//					+ "%s\n"
					+ " ORDER BY %s DESC;"
					, statementPart1
					, GlobalSettings.COUNT_COLUMN_NAME
					, statementPart2
					, GlobalSettings.COUNT_COLUMN_NAME
					, crosstabedTableName
					, statementPart4
//					, statementPart5
					, numProperties+1);
		}
	
	// e.g. distinctProperties[25]:	http://www.w3.org/1999/02/22-rdf-syntax-ns#type
	// OUTPUT: 26 (property_26)
	private int getTypeColumnNameInPivotTable(ArrayList<String> distinctProperties) {
		/* This pattern works for two types of RDF data structure:
		 * 1. Data without opening and closing tags: http://www.w3.org/1999/02/22-rdf-syntax-ns#type
		 * 2. Data with opening and closing tags: <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
		 */	
		String pattern = "([A-Za-z]+)>?$";
		for(int i=0; i<distinctProperties.size(); i++) {
			String line = distinctProperties.get(i);
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(line);
			if (m.find()) {
				if(m.group(1).equals("type")) {
					return i+1;
				}
			}
		}
		// "-1" means there is no type property
		return -1;
	}
	
	public static String getShorterTypeName(String typeName) {
		String result = null;
		String pattern = "([A-Za-z0-9]+)>?$";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(typeName);
		if (m.find()) {
			result = m.group(1);
		}
		return result;
	}
	
	public static String getShorterTypeNameTemp(String typeName) {
		String result = null;
		if (typeName != null) {
			String pattern = "([A-Za-z0-9]+)>?$";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(typeName);
			if (m.find()) {
				result = m.group(1);
			}
		}
		return result;
	}
	
	
}
