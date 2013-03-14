package br.unb.unbiquitous.ubiquitos.uos.driverManager;

import static br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverModel.DEVICE;
import static br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverModel.ID;
import static br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverModel.NAME;
import static br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverModel.ROW_ID;
import static br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverModel.TABLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;
import br.unb.unbiquitous.ubiquitos.uos.persistence.Dao;
import br.unb.unbiquitous.ubiquitos.uos.persistence.HsqldbConnectionController;

public class DriverDao extends Dao {

	private static Logger logger = Logger.getLogger(DriverDao.class);
	
	private HashMap<String, UpDriver> driverMap;
	private HashMap<String, Integer> driverCount;
	
	public DriverDao(ResourceBundle bundle) {
		super(TABLE, new HsqldbConnectionController(bundle));
		driverMap = new HashMap<String, UpDriver>();
		driverCount = new HashMap<String, Integer>();
		
		StringBuffer script = new StringBuffer();
		script.append("create table ").append(TABLE).append(" ( ");
		script.append(ROW_ID).append(" IDENTITY ,");
		script.append(ID).append(" varchar(256) , ");
		script.append(NAME).append(" varchar(256) ,");
		script.append(DEVICE).append(" varchar(256) ,");
		script.append(" UNIQUE (").append(ID).append(" , ").append(DEVICE).append(") ");
		script.append(");");
		
		try {create(script.toString());	} 
		catch (SQLException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	
	public void insert(DriverModel driver) {
		try {
			String insert = "insert into "+TABLE+" ("+ID+","+NAME+","+DEVICE
								+") values (?,?,?)";
			Connection con = connectionController.connect();
			PreparedStatement ps = con.prepareStatement(insert);
			ps.setString(1, driver.id());
			ps.setString(2, driver.driver().getName());
			ps.setString(3, driver.device());
			ps.executeUpdate();
			ResultSet rs = con.prepareStatement("SELECT "+ROW_ID+" from "+TABLE+" where "+ROW_ID+" = IDENTITY()").executeQuery();
			if(rs.next()){
				driver.rowid(rs.getLong(1));
			}
			insertOnMap(driver);
			
			con.close();
		} catch (SQLException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	public List<DriverModel> list() {
		return list(null);
	}

	public List<DriverModel> list(String name) {
		return list(name, null);
	}

	public List<DriverModel> list(String name, String device) {
		try {
			Connection con = connectionController.connect();
			PreparedStatement ps = createListQuery(null, name, device, con);
			ArrayList<DriverModel> list = populateList(ps.executeQuery());
			con.close();
			return list;
		} catch (SQLException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	private ArrayList<DriverModel> populateList(ResultSet rs)
			throws SQLException {
		ArrayList<DriverModel> list = new ArrayList<DriverModel>();
		while (rs.next()) {
			list.add(new DriverModel(rs.getLong(1), rs.getString(2), driverMap.get(rs.getString(3)), rs.getString(4)));
		}
		return list;
	}

	private PreparedStatement createListQuery(String id, String name,
			String device, Connection con) throws SQLException {
		String query = "select "+ROW_ID+","+ID+","+NAME+","+DEVICE
							+" from "+TABLE +" where 1=1 "; 
		if (name != null)	query += " and UCASE("+NAME+") LIKE ?";
		if (device != null)	query += " and UCASE("+DEVICE+") LIKE ?";
		if (id != null)		query += " and UCASE("+ID+") LIKE ?";

		query += " ORDER BY "+ID+","+NAME+","+DEVICE;
		
		PreparedStatement ps = con.prepareStatement(query);
		
		int argCount = 1;
		if (name != null)	ps.setString(argCount++, name.toUpperCase());
		if (device != null)	ps.setString(argCount++, device.toUpperCase());
		if (id != null)	ps.setString(argCount++, id.toUpperCase());
		
		return ps;
	}

	public void clear() {
		executeUpdateQuery("delete from "+TABLE);
	}

	public void delete(String id, String device) {
		DriverModel driver = retrieve(id, device);
		executeUpdateQuery("delete from "+TABLE+" where "+ID+" = ? and "+DEVICE+" = ?", id,device);
		removeFromMap(driver);
	}


	private void removeFromMap(DriverModel driver) {
		String name = driver.driver().getName();
		driverCount.put(name, driverCount.get(name)-1);
		if (driverCount.get(name).equals(0)){
			driverCount.remove(name);
			driverMap.remove(name);
		}
	}
	
	private void insertOnMap(DriverModel driver) {
		String name = driver.driver().getName();
		if (!driverMap.containsKey(name)){
			driverMap.put(name, driver.driver());
			driverCount.put(name, 0);
		}
		driverCount.put(name, driverCount.get(name)+1);
	}
	
	public DriverModel retrieve(String id, String device) {
		try {
			Connection con = connectionController.connect();
			PreparedStatement ps = createListQuery(id, null, device, con);
			ArrayList<DriverModel> list = populateList(ps.executeQuery());
			con.close();
			if (list.isEmpty())
				return null;
			return list.get(0);
		} catch (SQLException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

}
