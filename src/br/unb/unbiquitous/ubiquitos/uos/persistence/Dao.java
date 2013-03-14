package br.unb.unbiquitous.ubiquitos.uos.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.unb.unbiquitous.ubiquitos.Logger;

public abstract class Dao {

	private static Logger logger = Logger.getLogger(Dao.class);
	
	private String table;
	protected HsqldbConnectionController connectionController;
	
	public Dao(String table, HsqldbConnectionController connectionController){
		this.table = table;
		this.connectionController = connectionController;
	}
	
	protected void create(String script) throws SQLException{
		Connection con = connectionController.connect();
		ResultSet tbQuery = con.getMetaData().getTables(null, null, table, new String[]{"TABLE"});
		if (!tbQuery.next()){
			con.prepareStatement(script).executeUpdate();
		}
	}
	
	protected void executeUpdateQuery(String sql, String ... values){
		try {
			Connection con = connectionController.connect();
			PreparedStatement ps = con.prepareStatement(sql);
			if (values != null){
				int i = 1;
				for (String v : values){
					ps.setString(i++, v);
				}
			}
			ps.executeUpdate();
			con.close();
		} catch (SQLException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
}
