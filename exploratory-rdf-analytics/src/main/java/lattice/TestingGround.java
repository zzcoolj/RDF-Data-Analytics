package lattice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import exploratoryRDFAnalytics.GlobalSettings;
import exploratoryRDFAnalytics.PostgresDataSource_Plus;
import exploratoryRDFAnalytics.PostgresDatabaseHandler_Plus;
import fr.inria.oak.commons.db.DatabaseUtils;

// Experiments for new ideas
class TestingGround {

	// Answer a specific question: The number of articles which have x authors in year y.
	static public void numberOfArticlesWhichHaveXAuthorsInYearY(PostgresDatabaseHandler_Plus pgdbh) throws SQLException, IOException, URISyntaxException {
		// Step 1: create query
		/* This query only works for crosstabed_dblp_complete table where:
		 * p1:	http://purl.org/dc/elements/1.1/creator
		 * p25:	http://sw.deri.org/~aharth/2004/07/dblp/dblp.owl#year
		 */
		String query = "SELECT author_year.authors_number, author_year.year, count(*)\n"
				+ "FROM\n"
				+ "(SELECT property_1, property_25 AS year, count(*) AS authors_number\n"
				+ "FROM crosstabed_dblp_complete\n"
				+ "WHERE property_1 IS NOT NULL\n"
				+ "AND property_25 IS NOT NULL\n"
				+ "GROUP BY property_1, property_25) author_year\n"
				+ "GROUP BY 1,2\n"
				+ "ORDER BY 2,1;"; 
		
		
		// Step 2: execute query and write the answer into file
		// Generate the file
		String filePath = "../d3/heatmap/data2.tsv";
		File file = new File(filePath);
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		System.out.println("################# Generate heatmap-data2.tsv file #################");
		out.write("day\thour\tvalue\n");
	
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// fetch size sets to 0.
			stmt = pgdbh.createStatement(0);
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				int authors_number = rs.getInt("authors_number");
				int year = rs.getInt("year");
				int count = rs.getInt("count");
				
				// Write the file
				out.write(authors_number + "\t" + year + "\t" +count+ "\n");
			}
		} finally {
			DatabaseUtils.tryClose(rs);
			DatabaseUtils.tryClose(stmt);
		}
		
		// Close the file
		out.flush();
		out.close();
		
		// Step 3: launch the browser
		System.out.println("################# Launch the browser to display the heatmap #################");
		java.awt.Desktop.getDesktop().browse(new URI("file:///Users/zzcoolj/rdfanalytics/trunk/Codes/d3/heatmap/index.html"));
	}
	
	
	public static void main(String[] args) throws Exception {
		PostgresDataSource_Plus pgds = new PostgresDataSource_Plus(
				GlobalSettings.SERVER_NAME,
				Integer.valueOf(GlobalSettings.SERVER_PORT),
				GlobalSettings.SERVER_DATABASE_NAME,
				GlobalSettings.SERVER_USER, 
				GlobalSettings.SERVER_PASSWORD);
		PostgresDatabaseHandler_Plus pgdbh = new PostgresDatabaseHandler_Plus(pgds.getConnection());
		TestingGround.numberOfArticlesWhichHaveXAuthorsInYearY(pgdbh);
	}
}
