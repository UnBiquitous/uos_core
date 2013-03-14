package br.unb.unbiquitous.ubiquitos.uos.deviceManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpNetworkInterface;
import br.unb.unbiquitous.ubiquitos.uos.persistence.HsqldbConnectionController;

public class DeviceDao extends br.unb.unbiquitous.ubiquitos.uos.persistence.Dao {
	private static Logger logger = Logger.getLogger(DeviceDao.class);
	
	private static final String NETWORKTYPE = "networktype";
	private static final String ADDRESS = "address";
	private static final String TABLE = "DEVICE";
	private static final String ROWID = "rowid";
	private static final String NAME = "name";
	
	private Map<String,UpDevice> deviceMap = new HashMap<String, UpDevice>();
	
	public DeviceDao(ResourceBundle bundle) {
		super(TABLE,new HsqldbConnectionController(bundle));
		StringBuffer script = new StringBuffer();
		script.append("create table ").append(TABLE).append(" ( ");
		script.append(ROWID).append(" IDENTITY ,");
		script.append(NAME).append(" varchar(256) , ");
		script.append(ADDRESS).append(" varchar(256) , ");
		script.append(NETWORKTYPE).append(" varchar(256) ");
		script.append(");");
		try {create(script.toString());}
		catch (SQLException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public void save(UpDevice device) {
		try {
			Connection con = connectionController.connect();
			if (find(device.getName()) != null){
				throw new RuntimeException("Atempt to insert a device with same name.");
			}
			if (device.getNetworks() != null){
				for (UpNetworkInterface ni : device.getNetworks()){
					insert(device.getName(),ni.getNetworkAddress(),ni.getNetType(),con);
				}
			}else{
				insert(device.getName(),null,null,con);
			}
			deviceMap.put(device.getName(),device);
			con.close();
		} catch (SQLException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	public void update(String oldname, UpDevice device) {
		delete(oldname);
		save(device);
	}
	
	public void delete(String name) {
		executeUpdateQuery("delete from "+TABLE+" where "+NAME+" = ?", name);
		deviceMap.remove(name);
	}
	
	private Connection insert(String name, String address, String networkType, Connection con) throws SQLException {
		String insert = "insert into "+TABLE+" ("+NAME+","+ADDRESS+","+NETWORKTYPE+
				") values (?,?,?)";
		PreparedStatement ps = con.prepareStatement(insert);
		ps.setString(1, name);
		ps.setString(2, address);
		ps.setString(3, networkType);
		ps.executeUpdate();
		return con;
	}
	
	public List<UpDevice> list() {
		return new ArrayList<UpDevice>(deviceMap.values());
	}
	
	public List<UpDevice> list(String address, String networktype) {
		List<UpDevice> ret = new ArrayList<UpDevice>();
		try {
			Connection con = connectionController.connect();
			PreparedStatement ps = createListQuery(null,address,networktype, con);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()){
				ret.add(deviceMap.get(rs.getString(1)));
			}
			con.close();
		} catch (SQLException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		return ret;
	}

	public UpDevice find(String name) {
		try {
			Connection con = connectionController.connect();
			PreparedStatement ps = createListQuery(name,null,null, con);
			ResultSet rs = ps.executeQuery();
			if (rs.next()){
				return deviceMap.get(rs.getString(1));
			}
			con.close();
		} catch (SQLException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
		return null;
	}
	
	public void clear(){
		executeUpdateQuery("delete from "+TABLE);
		deviceMap.clear();
	}
	
	
	
	private PreparedStatement createListQuery(String name, String address, String networkType, Connection con) throws SQLException {
		String query = "select DISTINCT "+NAME+" from "+TABLE +" where 1=1 "; 
		if (name != null)		query += " and UCASE("+NAME+") LIKE ?";
		if (address != null)	query += " and UCASE("+ADDRESS+") LIKE ?";
		if (networkType != null)query += " and UCASE("+NETWORKTYPE+") LIKE ?";

		PreparedStatement ps = con.prepareStatement(query);
		
		int argCount = 1;
		if (name != null)		ps.setString(argCount++, name.toUpperCase());
		if (address != null)	ps.setString(argCount++, address.toUpperCase());
		if (networkType != null)ps.setString(argCount++, networkType.toUpperCase());
		
		return ps;
	}


	
}
