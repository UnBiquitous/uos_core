package br.unb.unbiquitous.ubiquitos.uos.security.basic;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.authentication.AuthenticationDao;
import br.unb.unbiquitous.ubiquitos.authentication.AuthenticationData;
import br.unb.unbiquitous.ubiquitos.authentication.exception.DuplicateIdException;


/** 
 * Class to manipulate the test database. Implements the methods defined in AuthenticationDao
 * interface: findByHashId, delete, update and insert.
 * The parameters needed to initialize and manipulate database are difined as class constants.
 * This parameters constants: url of database, user, password, table name and column names.
 * */
public class AuthenticationDaoHSQLDB implements AuthenticationDao{
	
	private static final Logger logger = Logger.getLogger(AuthenticationDaoHSQLDB.class);

	private static final String JDBC_DRIVER = "org.hsqldb.jdbcDriver";
	private static final String BD_URL = "jdbc:hsqldb:mem:";
	private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static final String DATABASE = "memoryBD";
    
    private static final String AUTHENTICATION_DATA_TABLE = "authenticationData";
    private static final String ID_COLUMN_NAME = "id";
    private static final String KEY_COLUMN_NAME = "key";
    private static final String HASHID_COLUMN_NAME = "hashId";
    
    
    /** 
     * Creates a table to save device's authentication data.
     * */
    public AuthenticationDaoHSQLDB(){
    	Connection con;
    	
    	try {
        	Class.forName(JDBC_DRIVER);
        	
        	// connects to database
			con = connect(); 
			
			// Checks if table already exists.
	    	String [] types = new String[1];
	    	types[0] = "TABLE";
	    	DatabaseMetaData md = con.getMetaData();  
	    	
	    	// checks if the table already exists
	    	ResultSet rs = md.getTables(null, null, AUTHENTICATION_DATA_TABLE.toUpperCase(), types);

	    	// if the table not exists yet it is created
	    	if (!rs.next()) {
	    		PreparedStatement pstmt = con.prepareStatement("create table " + AUTHENTICATION_DATA_TABLE + "(" + ID_COLUMN_NAME + " varchar(40), " + KEY_COLUMN_NAME + " varchar(40), " + HASHID_COLUMN_NAME + " varchar(70));");
				pstmt.executeUpdate();  
	    	} 
	    	
	    	// closes the connection
	    	con.close();
		
    	} catch (ClassNotFoundException e) {
			logger.error(e);
		} catch (SQLException e) {
			logger.error(e);
		} 
    }

    
    /** 
     * Access the database and find the device that has a given hash(id).
     * If there are more than one device with the same hash(id) returns a error.
     * If there are no device with given hash(id) returns null
     * 
     * @param hashId - hash(id) of the device
     * @return authenticationData - object with id, hashId and key retrieved from database.
     * */
	public AuthenticationData findByHashId (String hashId) throws SQLException, DuplicateIdException {   

		// creates an object to store the result
		AuthenticationData authenticationData = new AuthenticationData();
		ResultSet resultSet = null;
		Connection con;
		
		try {
			//connects to database
			con = connect();
            PreparedStatement pstmt = con.prepareStatement(
            "select " + ID_COLUMN_NAME + ", " + KEY_COLUMN_NAME + ", " + HASHID_COLUMN_NAME + " from " + AUTHENTICATION_DATA_TABLE + " where " + HASHID_COLUMN_NAME + " = ?");
            pstmt.setString(1, hashId);
            // executes search and stores result in resultSet
            resultSet = pstmt.executeQuery();

            // moves the cursor to the first line of the result (if this line exists)
            resultSet.next();
            // if there are first line
            if (resultSet.getRow() == 1) {
                authenticationData.setId(resultSet.getString(ID_COLUMN_NAME));
                authenticationData.setKey(resultSet.getString(KEY_COLUMN_NAME));
                authenticationData.setHashId(resultSet.getString(HASHID_COLUMN_NAME));
                
                // if there are more than one line with the same hashId in database throw DuplicateIdException
                if (resultSet.next()!= false){
            		throw new DuplicateIdException();
                }
            // if there are no lines in database with the given id, returns null 
            } else {
                if (resultSet.getRow() == 0){
                	return null;
                }
            }
            
            // closes connection
            con.close();
        }  catch (SQLException ex) {
            logger.error(ex);
            throw new SQLException();
        }
		return authenticationData;
	}
		
	
	/** 
	 * Deletes from database the device that has a given id.
	 * 
	 * @param id - id of the device to be deleted.
	 * */
	public void delete (String id){
		Connection con;
		
		try {
			// connects to database
			con = connect();
			String sql = "";
			PreparedStatement stm = con.prepareStatement(sql);
			sql = "delete from " + AUTHENTICATION_DATA_TABLE + " where " + ID_COLUMN_NAME + " like ?";
			stm = con.prepareStatement(sql);
			stm.setString(1,id);
			// executes deletion
			stm.executeUpdate();
			stm.close();
			//closes connection
			con.close();
		} catch (SQLException e) {
				logger.error(e);
		}
	}

	
	/** 
	 * Updates a device's key in the database.
	 * 
	 * @param id - id of the device whose key will be updated.
	 * @param key - the new key
	 * */
	public void update (String id, String newKey){

		Connection con;
		try {
			//connects to database
			con = connect();
			String sql = "";
			PreparedStatement stm = con.prepareStatement(sql);
			sql = "update " + AUTHENTICATION_DATA_TABLE + " set " + KEY_COLUMN_NAME + " = ? " + " where " + ID_COLUMN_NAME + " like ?";
			stm = con.prepareStatement(sql);
			stm.setString(1, newKey);
			stm.setString(2, id);
			// executes update
			stm.executeUpdate();
			//closes connection
			stm.close();
		} catch (SQLException e) {
				logger.error(e);
		}
	}

	
	/** 
	 * Inserts a record to database. Each record has id, key and hash(id), informed as parameters.
	 * 
	 * @param id - device's identificator
	 * @param key - device's key
	 * @param hashId - hash(id)
	 * */
	public void insert (String id, String hashId , String key){
		Connection con;
		
    	try {
    		//connect to database
			con = connect();
			
			String query = "insert into " + AUTHENTICATION_DATA_TABLE + 
			"(" + ID_COLUMN_NAME + ", " + KEY_COLUMN_NAME + ", " + HASHID_COLUMN_NAME + ") values (?, ?, ?)";
			
			PreparedStatement stm = con.prepareStatement(query);
			
			stm.setString(1, id);
			stm.setString(2, key);
			stm.setString(3, hashId);
			
			//execute insertion
			stm.executeUpdate();
			stm.close();
			con.close();
		} catch (SQLException e) {
				logger.error(e);
		}
	}
	
		
	/** 
	 * Connects to database. The parameters url, user and password are constants initialized in the constructor.
	 * 
	 * @return database connection
	 * */
    protected Connection connect() throws SQLException {
    	String database = DATABASE;
    	String user = USER;
    	String password = PASSWORD;
    	return DriverManager.getConnection(BD_URL + database , user ,password);
    }
}
