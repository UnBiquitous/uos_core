package br.unb.unbiquitous.ubiquitos.uos.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Utilitary class to handle the database connection.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class HsqldbConnectionController {
	
    private static final String DB_PROP = "ubiquitos.persistence.hsqldb.database";
	private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";	
    private static final String BD_URL = "jdbc:hsqldb:mem:";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private String DATABASE = "memoryBD";
    
    public HsqldbConnectionController(ResourceBundle resourceBundle) {
        if(resourceBundle != null && resourceBundle.containsKey(DB_PROP)){
			DATABASE = resourceBundle.getString(DB_PROP);
		} 
	}
    
	/** 
	 * Connects to database. The parameters url, user and password are constants initialized in the constructor.
	 * 
	 * @return database connection
	 * */
    public Connection connect() throws SQLException {
    	try {
			Class.forName(JDBC_DRIVER);
			String database = DATABASE;
			String user = USER;
			String password = PASSWORD;
			return DriverManager.getConnection(BD_URL + database , user ,password);
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
    }
    
    public void clear() throws SQLException{
    	connect().prepareStatement("SHUTDOWN;").executeUpdate();
    }
    
}
