package org.unbiquitous.uos.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.unbiquitous.uos.core.driverManager.UosDriver;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManager;
import org.unbiquitous.uos.core.network.radar.Radar;

@SuppressWarnings("serial")
public class InitialProperties extends HashMap<String, Object> {

	private boolean readOnly;

	public InitialProperties() {}
	
	public InitialProperties(ResourceBundle bundle) {
		Enumeration<String> keys = bundle.getKeys();
		while(keys.hasMoreElements()){
			String key = keys.nextElement();
			this.put(key, bundle.getObject(key));
		}
	}
	
	public InitialProperties(Map<String, Object> initMap) {
		super(initMap);
	}

	public String getString(String key) {
		if (!this.containsKey(key)) return null;
		return (String) get(key);
	}
	
	public Integer getInt(String key) {
		if (!this.containsKey(key)) return null;
		Object value = get(key);
		if (value instanceof Integer) return (Integer) value;
		else return Integer.parseInt(value.toString());
	}
	
	public Boolean getBool(String key) {
		if (!this.containsKey(key)) return null;
		Object value = get(key);
		if (value instanceof Boolean) return (Boolean) value;
		else return Boolean.parseBoolean(value.toString());
	}
	
	@Override
	public Object put(String key, Object value) {
		if (readOnly){
			throw new IllegalAccessError("Can't change properties after init");
		}
		return super.put(key, value);
	}
	
	public void markReadOnly(){
		this.readOnly = true;
	}
	
	/*
	 * ubiquitos.connectionManager
	 * ubiquitos.radar
	 * ubiquitos.driver.deploylist
	 * ubiquitos.application.deploylist
	 * ubiquitos.uos.deviceName
	 * ubiquitos.message.response.timeout
	 * ubiquitos.message.response.retry
	 */
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addConnectionManager(Class<ConnectionManager> clazz){
		String key = "ubiquitos.connectionManager";
		if(!containsKey(key)){
			put(key, new ArrayList<Class<ConnectionManager>>());
		}
		List list = (List) get(key);
		list.add(clazz);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Class<ConnectionManager>> getConnectionManagers() throws ClassNotFoundException {
		String key = "ubiquitos.connectionManager";
		if (!this.containsKey(key)) return null;
		Object value = get(key);
		if (value instanceof List) return (List) value;
		else if (value instanceof String){
			return translateToConnectionManagerList(key);
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Class<ConnectionManager>> translateToConnectionManagerList(
			String key) throws ClassNotFoundException {
		String[] managers = getString(key).split(",");
		List list = new ArrayList();
		for(String mng: managers){
			list.add(Class.forName(mng));
		}
		return list;
	}
	
	
	
	public void addRadar(Class<Radar> clazz){
		addRadar(clazz, null);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addRadar(Class<Radar> clazz, Class<ConnectionManager> manager){
		String key = "ubiquitos.radar";
		if(!containsKey(key)){
			put(key, new HashMap<Class<Radar>,Class<ConnectionManager>>());
		}
		Map map = (Map) get(key);
		map.put(clazz,manager);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<Class<Radar>,Class<ConnectionManager>> getRadars() throws ClassNotFoundException {
		String key = "ubiquitos.radar";
		if (!this.containsKey(key)) return null;
		Object value = get(key);
		if (value instanceof Map) return (Map) value;
		else if (value instanceof String){
			return translateToRadarMap(key);
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<Class<Radar>,Class<ConnectionManager>> translateToRadarMap(
			String key) throws ClassNotFoundException {
		String[] radars = getString(key).split(",");
		Map map = new HashMap();
		for(String radar: radars){
			Class manager = null;
			if(radar.contains("(") || radar.contains(")")){
				int beginIndex = radar.indexOf('(');
				int endIndex = radar.indexOf(')');
				String mng = radar.substring(beginIndex+1,endIndex);
				manager = Class.forName(mng);
				radar = radar.substring(0,beginIndex);
			}
			map.put(Class.forName(radar),manager);
		}
		return map;
	}
	
	
	public void addDriver(Class<UosDriver> clazz){
		addDriver(clazz, null);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addDriver(Class<UosDriver> clazz, String id){
		String key = "ubiquitos.driver.deploylist";
		if(!containsKey(key)){
			put(key, new ArrayList<Tuple<Class<UosDriver>,String>>());
		}
		List list = (List) get(key);
		list.add(new Tuple(clazz,id));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Tuple<Class<UosDriver>,String>>getDrivers() throws ClassNotFoundException {
		String key = "ubiquitos.driver.deploylist";
		if (!this.containsKey(key)) return null;
		Object value = get(key);
		if (value instanceof List) return (List) value;
		else if (value instanceof String){
			return translateToDriverMap(key);
		}
		return null;
	}

	
	
	public static class Tuple<X, Y> { 
		  public final X x; 
		  public final Y y; 
		  public Tuple(X x, Y y) { 
		    this.x = x; 
		    this.y = y; 
		  } 
		} 
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Tuple<Class<UosDriver>,String>> translateToDriverMap(
			String key) throws ClassNotFoundException {
		String[] drivers = getString(key).split(";");
		List list = new ArrayList();
		for(String driverClass: drivers){
			String id = null;
			if(driverClass.contains("(") || driverClass.contains(")")){
				int beginIndex = driverClass.indexOf('(');
				int endIndex = driverClass.indexOf(')');
				id = driverClass.substring(beginIndex+1,endIndex);
				driverClass = driverClass.substring(0,beginIndex);
			}
			list.add(new Tuple(Class.forName(driverClass),id));
		}
		return list;
	}
}


