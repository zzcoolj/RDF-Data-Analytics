package exploratoryRDFAnalytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

//import fr.inria.oak.commons.db.DatabaseUtils;
//import fr.inria.oak.commons.db.PostgresDatabaseHandler;

/**
 * PostgresDatabaseHandler (commons-db) function extension.
 * @author Zheng ZHANG
 */
//public class PostgresDatabaseHandler_Plus extends PostgresDatabaseHandler{
public class PostgresDatabaseHandler_Plus{

	private int fetchSize;
	private int numPropertiesFrequencyNotZero;
	Connection conn;
	
	public PostgresDatabaseHandler_Plus(Connection conn) {
//		super(conn);
		this.conn = conn;
	}

	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	/**
	 * Gets the results count for the given SQL query against the RDBMS.
	 * @param query SQL query to execute
	 * @return SQL query results HashMap<property (String), count(*) (Integer)>
	 * @throws SQLException 
	 */
	public HashMap<String, Integer> getGroupByOnePropertyStatementResults(final String sqlQuery) throws SQLException {
		HashMap<String, Integer> result = new HashMap<String, Integer>(); 
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// now
			stmt = conn.createStatement();
			stmt.setFetchSize(fetchSize);
//			stmt = createStatement(fetchSize);
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				String property = rs.getString(GlobalSettings.PROPERTY_COLUMN_NAME);
				int count = rs.getInt(GlobalSettings.COUNT_COLUMN_NAME);
				result.put(property, count);
			}
		} finally {
//			DatabaseUtils.tryClose(rs);
			rs.close();
//			DatabaseUtils.tryClose(stmt);
			stmt.close();
		}
		return result;
	}
	
	public void executeQuery(String sqlQuery) throws SQLException {
		Statement stmt = null;
		try {
			// now 2
			stmt = conn.createStatement();
			stmt.setFetchSize(fetchSize);
//			stmt = createStatement(fetchSize);
			stmt.executeQuery(sqlQuery);
		} catch (SQLException e) {
			// Error Code: 02000	Condition Name: no_data => This error is normal(and expected) for this function.
			if(!e.getSQLState().equals("02000")) {
				System.err.println(e);
			}
		}
		finally {
//			DatabaseUtils.tryClose(stmt);
			stmt.close();
		}
	}
	
	public HashMap<String, Integer> getCountFrequenceGroupbyOfCrosstabStatementResults(final String sqlQuery, ArrayList<String> distinctProperties, int numProperties, String groupByPropertiesResultFileName) throws SQLException, IOException {
		HashMap<String, Integer> result = new HashMap<String, Integer>(); 
		Statement stmt = null;
		ResultSet rs = null;
		
		// Write the properties in group_by_properties_result.txt file
		String filePath = GlobalSettings.GROUP_BY_PROPERTIES_RESULT_FILE_FOLDER_PATH + groupByPropertiesResultFileName + ".txt";
		// Open the file
		File file = new File(filePath);
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		System.out
				.println("################# Generate " + groupByPropertiesResultFileName + ".txt file #################");
		out.write(numProperties + "\n");
		int counter = 1;
		for (String s : distinctProperties) {
			//System.out.println("p" + (counter++) + ":	" + s);
			out.write("p" + (counter++) + ":	" + s + "\n");
		}
		
		// Write distinct property names and "count" in subject_property_state.txt file
		for (int i = 0; i < numProperties; i++) {
			out.write("p" + (i + 1) + "	");
		}
		out.write("count" + "\n");
		out.write("-------------------------------------------------------" + "\n");
		
		try {
			// now
			stmt = conn.createStatement();
			stmt.setFetchSize(fetchSize);
//			stmt = createStatement(fetchSize);
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				String propertiesString = "";
				for(int i=0; i<numProperties; i++) {
					String property = rs.getString(i+1);
					propertiesString += property + "	";
				}
				int count = rs.getInt(GlobalSettings.COUNT_COLUMN_NAME);
				//System.out.println(propertiesString + " " +count);
				out.write(propertiesString + " " +count + "\n");
				result.put(propertiesString, count);
			}
		} finally {
//			DatabaseUtils.tryClose(rs);
			rs.close();
//			DatabaseUtils.tryClose(stmt);
			stmt.close();
		}
		// Close the file
		out.flush();
		out.close();
		return result;
	}
	
	public HashMap<String, Integer> getCountFrequenceGroupbyOfCrosstabStatementResultsWithPropertyOrder(
			final String sqlQuery,
			ArrayList<String> distinctProperties, 
			HashMap<String, Integer> propertyOrder, 
			int numProperties, 
			String orderedPropertiesFileName, 
			String propertyFrequencyFileName,
			String groupByPropertiesResultFileName) 
			throws SQLException, IOException {
		HashMap<String, Integer> result = new HashMap<String, Integer>(); 
		Statement stmt = null;
		ResultSet rs = null;
		
		// Write the properties in group_by_properties_result.txt file
		String filePath = GlobalSettings.GROUP_BY_PROPERTIES_RESULT_FILE_FOLDER_PATH + groupByPropertiesResultFileName + ".txt";
		// Open the file
		File file = new File(filePath);
		
//		// TODO delete after
//		System.out.println("global: " + GlobalSettings.GROUP_BY_PROPERTIES_RESULT_FILE_FOLDER_PATH);
//		System.exit(0);
		
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		System.out.println("################# Generate " + groupByPropertiesResultFileName + ".txt file #################");
		out.write(numProperties + "\n");
		int counter = 1;
		for (String s : distinctProperties) {
			//System.out.println("p" + (counter++) + ":	" + s);
			out.write("p" + (counter++) + ":	" + s + "\n");
		}
		
		// Write distinct property names and "count" in subject_property_state.txt file
		for (int i = 0; i < numProperties; i++) {
			out.write("p" + (i + 1) + "	");
		}
		out.write("count" + "\n");
		out.write("-------------------------------------------------------" + "\n");
		
		HashMap<String, Integer> propertyFrequenceCount = new HashMap<String, Integer>();	
		try {
			// now
			stmt = conn.createStatement();
			stmt.setFetchSize(fetchSize);
//			stmt = createStatement(fetchSize);
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				String propertiesString = "";
				int count = rs.getInt(GlobalSettings.COUNT_COLUMN_NAME);
				for(int i=0; i<numProperties; i++) {
					String property = rs.getString(i+1);
					propertiesString += property + "	";
					// Update propertyFrequenceCount
					if (property.equals("t")) {
						if (propertyFrequenceCount.containsKey("p" + (i + 1))) {
							propertyFrequenceCount.put("p" + (i + 1), propertyFrequenceCount.get("p" + (i + 1)) + count);
						} else {
							propertyFrequenceCount.put("p" + (i + 1), count);
						}
					}
				}
				//System.out.println(propertiesString + " " +count);
				//System.out.println("propertyFrequenceCount: " + propertyFrequenceCount);
				out.write(propertiesString + " " +count + "\n");
				result.put(propertiesString, count);
			}
		} finally {
//			DatabaseUtils.tryClose(rs);
			rs.close();
//			DatabaseUtils.tryClose(stmt);
			stmt.close();
		}
		// Close the file
		out.flush();
		out.close();
		
		/* 
		 * When crosstabed table is the whole table (e.g. crosstabed_dblp_forth), propertyFrequenceCount.size() == numProperties.
		 * 
		 * When crosstabed table is part of the whole table (e.g. crosstabed_dblp_forth_type_article), propertyFrequenceCount.size() <= numProperties, 
		 * cause numProperties is calculated by the whole table and in the part table some properties' frequency is 0 (these properties have not been added into propertyFrequenceCount).
		 * 
		 * => Add these 0 frequency properties into propertyFrequenceCount.
		 */
		if(propertyFrequenceCount.size() < numProperties) {
			numPropertiesFrequencyNotZero = propertyFrequenceCount.size();
			for(int i=1; i<= numProperties; i++) {
				if (!propertyFrequenceCount.containsKey("p" + i)) {
					propertyFrequenceCount.put("p" + i, 0);
					//System.out.println("propertyFrequenceCount doesn't have property " + "p" + i);
				}
			}
		} else {
			numPropertiesFrequencyNotZero = numProperties;
		}
		
		/*
		 * Update HashMap<String, Integer> propertyOrder
		 * Before update: propertyOrder = {}; propertyFrequenceCount: {p1=4, p2=2, p3=3}.
		 * After update: e.g. {p1=0, p2=2, p3=1} (calculated by propertyFrequenceCount)
		 * "p1=0" means that the first property in crosstab table("p1") is the most frequent distinct property("0") 
		 * "p3=1" means that the third property in crosstab table("p3") is the second frequent distinct property("1") 
		 */
		LinkedHashMap<String, Integer> sortedPropertyFrequenceCount = PostgresDatabaseHandler_Plus.sortHashMapByValues(propertyFrequenceCount);
		//System.out.println("Sorted propertyFrequenceCount: " + sortedPropertyFrequenceCount);
		Set<String> keys = sortedPropertyFrequenceCount.keySet();
		//System.out.println("Sorted propertyFrequenceCount keySet: " + keys);
		int order = keys.size()-1;
		for(String s: keys) {
			propertyOrder.put(s, order--);
		}
		
		PostgresDatabaseHandler_Plus.writePropertyFrequenceTxtFile(propertyFrequenceCount, propertyFrequencyFileName);
		PostgresDatabaseHandler_Plus.writeOrderedPropertiesTxtFile(distinctProperties, propertyOrder, numProperties, orderedPropertiesFileName);
		
		return result;
	}
	
	public int getNumPropertiesFrequencyNotZero() {
		return numPropertiesFrequencyNotZero;
	}

	public HashMap<String, Integer> getCountFrequenceGroupbyCubeOfCrosstabStatementResults(final String sqlQuery, ArrayList<String> distinctProperties, HashMap<String, Integer> propertyOrder, int numProperties, String orderedPropertiesFileName, String groupByPropertiesResultFileName) throws SQLException, IOException {
		HashMap<String, Integer> result = new HashMap<String, Integer>(); 
		Statement stmt = null;
		ResultSet rs = null;
		String filePath = GlobalSettings.GROUP_BY_PROPERTIES_RESULT_FILE_FOLDER_PATH + groupByPropertiesResultFileName + ".txt";
		// Write the properties in group_by_properties_result.txt file
		// Open the file
		File file = new File(filePath);
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		System.out.println("################# Generate " + groupByPropertiesResultFileName + ".txt file #################");
		out.write(numProperties + "\n");
		int counter = 1;
		for (String s : distinctProperties) {
			//System.out.println("p" + (counter++) + ":	" + s);
			out.write("p" + (counter++) + ":	" + s + "\n");
		}
		
		// Write distinct property names and "count" in subject_property_state.txt file
		for (int i = 0; i < numProperties; i++) {
			out.write("p" + (i + 1) + "	");
		}
		out.write("count" + "\n");
		out.write("-------------------------------------------------------" + "\n");
		
		try {
			// now
			stmt = conn.createStatement();
			stmt.setFetchSize(fetchSize);
//			stmt = createStatement(fetchSize);
			rs = stmt.executeQuery(sqlQuery);
			int propertyOrderCounter = 0;
			while (rs.next()) {
				String propertiesString = "";
				/* Need only two kinds of lines:
				 * 1. No "null": 					e.g. "t f f t 5" 			=> nullCounter==0;
				 * 2. One "t" & others all null:	e.g. "null t null null 5" 	=> nullCounter==numProperties-1 && !contains("f").
				 */
				int nullCounter = 0;
				for(int i=0; i<numProperties; i++) {
					String property = rs.getString(i+1);
					propertiesString += property + "	";
					if(property == null) {
						nullCounter++;
					}
				}
				if(nullCounter==0) {
					int count = rs.getInt(GlobalSettings.COUNT_COLUMN_NAME);
					//System.out.println(propertiesString + " " +count);
					out.write(propertiesString + " " +count + "\n");
					result.put(propertiesString, count);
				} else if(nullCounter==(numProperties-1) && !propertiesString.contains("f")) {
					//int count = rs.getInt(GlobalSettings.COUNT_COLUMN_NAME);
					int tPosition = propertiesString.indexOf("t");
					//System.out.println(propertiesString + "	" + count + "--->" + tPosition);
					int propertyPosition = tPosition/5 + 1;
					propertyOrder.put("p"+propertyPosition, propertyOrderCounter++);
				}
			}
		} finally {
//			DatabaseUtils.tryClose(rs);
			rs.close();
//			DatabaseUtils.tryClose(stmt);
			stmt.close();
		}
		// Close the file
		out.flush();
		out.close();
		
		PostgresDatabaseHandler_Plus.writeOrderedPropertiesTxtFile(distinctProperties, propertyOrder, numProperties, orderedPropertiesFileName);
		
		return result;
	}
	
	public ArrayList<String> getSelectDistinctPropertyStatementResults(final String sqlQuery) throws SQLException {  
		ArrayList<String> result = new ArrayList<String>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// now 1
			stmt = conn.createStatement();
			stmt.setFetchSize(fetchSize);
//			stmt = createStatement(fetchSize);
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				result.add(rs.getString(GlobalSettings.PROPERTY_COLUMN_NAME));
			}
		} finally {
//			DatabaseUtils.tryClose(rs);
			rs.close();
//			DatabaseUtils.tryClose(stmt);
			stmt.close();
		}
		return result;
	}
	
	public int getCountDistinctProperties(String sqlQuery) throws SQLException {
		int count = -1;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// now
			stmt = conn.createStatement();
			stmt.setFetchSize(fetchSize);
//			stmt = createStatement(fetchSize);
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				count = rs.getInt(GlobalSettings.COUNT_COLUMN_NAME);
			}
		} finally {
//			DatabaseUtils.tryClose(rs);
			rs.close();
//			DatabaseUtils.tryClose(stmt);
			stmt.close();
		}
		return count;
	}
	
	public ArrayList<String>  getSelectDistinctTypeStatementResults(String sqlQuery) throws SQLException {
		ArrayList<String> result = new ArrayList<String>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// now
			stmt = conn.createStatement();
		    stmt.setFetchSize(fetchSize);
//			stmt = createStatement(fetchSize);
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				String type = rs.getString("type");
				result.add(type);
			}
		} finally {
//			DatabaseUtils.tryClose(rs);
			rs.close();
//			DatabaseUtils.tryClose(stmt);
			stmt.close();
		}
		return result;
	}
	
	public int getCountTypeFrequencyStatementResults(String sqlQuery) throws SQLException {
		int result = 0;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// now
			stmt = conn.createStatement();
			stmt.setFetchSize(fetchSize);
//			stmt = createStatement(fetchSize);
			rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				result = rs.getInt("count");
			}
		} finally {
//			DatabaseUtils.tryClose(rs);
			rs.close();
//			DatabaseUtils.tryClose(stmt);
			stmt.close();
		}
		return result;
	}
	
	// TODO public tools
	public static LinkedHashMap<String, Integer> sortHashMapByValues(HashMap<String, Integer> passedMap) {
		List<String> mapKeys = new ArrayList<>(passedMap.keySet());
		List<Integer> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

		Iterator<Integer> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			int val = valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				String key = keyIt.next();
				int comp1 = passedMap.get(key);
				int comp2 = val;

				if (comp1 == comp2) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}
	
	private static void writeOrderedPropertiesTxtFile(ArrayList<String> distinctProperties, HashMap<String, Integer> propertyOrder, int numProperties, String fileName) throws IOException {
		// Write the ordered properties in ordered_properties.txt file for d3.js
		String filePath = GlobalSettings.ORDERED_PROPERTIES_FILE_FOLDER_PATH + fileName + ".csv";
		// Open the file
		File file2 = new File(filePath);
		file2.createNewFile();
		BufferedWriter out2 = new BufferedWriter(new FileWriter(file2));
		System.out.println("################# Generate " + fileName + ".csv file #################");
		out2.write(numProperties + "\n");
		/*
		 * Input: 
		 * e.g. HashMap<String, Integer> propertyOrder: {p1=0, p2=2, p3=1} 
		 * e.g. ArrayList<String> distinctProperties: {"propertyNameA", "propertyNameB", "propertyNameC"} 
		 * Output (in file ordered_properties.txt): 
		 * e.g. p1: propertyNameA p3: propertyNameC p2: propertyNameB
		 */
		String[] orderedProperties = new String[numProperties];
		for (Entry<String, Integer> entry : propertyOrder.entrySet()) {
			String propertyReferenceName = entry.getKey();
			Integer propertyOrderNumber = entry.getValue();
			orderedProperties[propertyOrderNumber] = propertyReferenceName;
		}
		
		// String[] orderedProperties: ["p1", "p3", "p2"]
		for (String s : orderedProperties) {
			int propertyPositionInDistinctProperties = Integer.parseInt(s.substring(1));
			out2.write(s
					+ ":	"
					+ distinctProperties.get(propertyPositionInDistinctProperties - 1)
					+ "\n");
		}
		// Close the file
		out2.flush();
		out2.close();
	}
	
	private static void writePropertyFrequenceTxtFile(HashMap<String, Integer> propertyFrequenceCount, String fileName) throws IOException {
		String filePath = GlobalSettings.PROPERTY_FREQUENCE_FILE_FOLDER_PATH + fileName + ".csv";
		// Write the properties in property_frequence.csv file
		// Open the file
		File file = new File(filePath);
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		System.out.println("################# Generate " + fileName + ".txt file #################");
		out.write(propertyFrequenceCount.size() + "\n");
		for (Entry<String, Integer> entry : propertyFrequenceCount.entrySet()) {
			String pN = entry.getKey();
			Integer frequence = entry.getValue();
			out.write(pN + ":\t" + frequence + "\n");
		}
		// Close the file
		out.flush();
		out.close();
	}

	
	
//	public static void main(String[] args) {
//		HashMap<String, Integer> propertyFrequenceCount = new HashMap<String, Integer>();
//		propertyFrequenceCount.put("p1", 4);
//		propertyFrequenceCount.put("p2", 2);
//		propertyFrequenceCount.put("p3", 3);
//		propertyFrequenceCount.put("p4", 2);
//		propertyFrequenceCount.put("p5", 4);
//		propertyFrequenceCount.put("p6", 2);
//		propertyFrequenceCount.put("p8", 0);
//		propertyFrequenceCount.put("p9", 10);
//		System.out.println("Sorted propertyFrequenceCount: " + PostgresDatabaseHandler_Plus.sortHashMapByValues(propertyFrequenceCount));
//	}
}
