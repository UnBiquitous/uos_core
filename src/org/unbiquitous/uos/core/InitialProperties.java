package org.unbiquitous.uos.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.unbiquitous.uos.core.applicationManager.UosApplication;
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

	public static class Tuple<X, Y> { 
		  public final X x; 
		  public final Y y; 
		  public Tuple(X x, Y y) { 
		    this.x = x; 
		    this.y = y; 
		  } 
		} 
	
	public void addDriver(Class<UosDriver> clazz){
		addDriver(clazz, null);
	}
	
	public void addDriver(Class<UosDriver> clazz, String id){
		addToTupleList(clazz.getCanonicalName(), id, "ubiquitos.driver.deploylist");
	}

	public List<Tuple<String,String>> getDrivers() throws ClassNotFoundException {
		return getTupleList("ubiquitos.driver.deploylist");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addToTupleList(String x, String y, String key) {
		if(!containsKey(key)){
			put(key, new ArrayList<Tuple<String,String>>());
		}
		List list = (List) get(key);
		list.add(new Tuple(x,y));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Tuple<String, String>> getTupleList(String key)
			throws ClassNotFoundException {
		if (!this.containsKey(key)) return new ArrayList();
		Object value = get(key);
		if (value instanceof List) return (List) value;
		else if (value instanceof String){
			return translateToTupleList(key);
		}
		return new ArrayList();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Tuple<String,String>> translateToTupleList(
			String key) throws ClassNotFoundException {
		String[] drivers = getString(key).split(";");
		List list = new ArrayList();
		for(String clazzProp: drivers){
			list.add(translateTuple(clazzProp));
		}
		return list;
	}

	private Tuple<String,String> translateTuple(String clazzProp)
			throws ClassNotFoundException {
		String id = null;
		String classStr = clazzProp;
		if(clazzProp.contains("(") || clazzProp.contains(")")){
			int beginIndex = clazzProp.indexOf('(');
			int endIndex = clazzProp.indexOf(')');
			id = clazzProp.substring(beginIndex+1,endIndex);
			classStr = clazzProp.substring(0,beginIndex);
		}
		return new Tuple<String,String>(classStr,id);
	}
	
	public void addApplication(Class<UosApplication> clazz){
		addApplication(clazz, null);
	}
	
	public void addApplication(Class<UosApplication> clazz, String id){
		addToTupleList(clazz.getCanonicalName(), id, "ubiquitos.application.deploylist");
	}
	
	public List<Tuple<String,String>>getApplications() throws ClassNotFoundException {
		return getTupleList("ubiquitos.application.deploylist");
	}
}


