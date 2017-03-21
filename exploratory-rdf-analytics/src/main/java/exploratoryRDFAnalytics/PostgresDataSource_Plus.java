package exploratoryRDFAnalytics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//import fr.inria.oak.commons.db.PostgresDataSource;

/**
 * PostgresDataSource (commons-db) function extension.
 * 
 * @author Zheng ZHANG
 */

//public class PostgresDataSource_Plus extends PostgresDataSource {
public class PostgresDataSource_Plus{

//	// TODO Not sure whether it's useful
//	public PostgresDataSource_Plus(String serverName, int portNumber,
//			String databaseName, String user, String password) {
//		super(serverName, portNumber, databaseName, user, password);
//	}

//	@Override
	public Connection getConnection() throws SQLException {
		Connection c = null;
		try {
			Class.forName(GlobalSettings.SERVER_CLASS_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		c = DriverManager.getConnection(GlobalSettings.SERVER_URL,
				GlobalSettings.SERVER_USER, GlobalSettings.SERVER_PASSWORD);
		System.out.println("################# Connect to Postgresql server successfully #################");
		return c;
	}
}