package br.unb.unbiquitous.ubiquitos.uos.driverManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;

public class DriverDao {
	private HashMap<String, UpDriver> driverMap;
	private HashMap<String, Integer> driverCount;
	private HashMap<String, List<DriverModel>> driverByDeviceMap;
	private HashMap<String, List<DriverModel>> driverByTypeMap;
	private HashMap<Long, DriverModel> modelMap;
	private HashMap<String, DriverModel> modelByIdMap;
	
	public DriverDao(ResourceBundle bundle) {
		createMaps();
	}

	private static long rowid = 0;
	
	private static synchronized long newId(){ return rowid ++; }

	private void createMaps() {
		driverMap = new HashMap<String, UpDriver>();
		driverCount = new HashMap<String, Integer>();
		driverByDeviceMap = new HashMap<String, List<DriverModel>>();
		modelMap =  new HashMap<Long, DriverModel>();
		driverByTypeMap = new HashMap<String, List<DriverModel>>();
		modelByIdMap = new HashMap<String, DriverModel>();
	}
	
	
	public void insert(DriverModel driver) {
		if (retrieve(driver.id(), driver.device()) == null){
			driver.rowid(newId());
			insertOnMap(driver);
		}else{
			throw new RuntimeException("Device "+driver.device()+
					" already has a driver with id "+ driver.id());
		}
	}

	public List<DriverModel> list() {
		return list(null, null);
	}

	public List<DriverModel> list(String name) {
		return list(name, null);
	}

	public List<DriverModel> list(String name, String device) {
		List<DriverModel> result = null;
		if (device == null) {
			if(name == null) {
				result =  listAll();
			}else{
				List<DriverModel> listByDriver = listByDriver(name);
				result = listByDriver;
			}
		}else{
			List<DriverModel> listByDevice = listByDevice(device);
			
			if (name != null){
				result =  listByDeviceAndDriver(name, listByDevice);
			}else{
				result = listByDevice;
			}
		}
		
		Collections.sort(result, new Comparator<DriverModel>() {
			public int compare(DriverModel m1, DriverModel m2) {
				return m1.id().compareTo(m2.id());
			}
		});
		
		return new ArrayList<DriverModel>(result);
	}

	private ArrayList<DriverModel> listAll() {
		return new ArrayList<DriverModel>(modelMap.values());
	}
	
	private List<DriverModel> listByDriver(String name) {
		List<DriverModel> listByDriver = driverByTypeMap.get(name.toLowerCase());
		if (listByDriver == null) return new ArrayList<DriverModel>();
		return listByDriver;
	}
	
	private List<DriverModel> listByDevice(String device) {
		List<DriverModel> listByDevice = driverByDeviceMap.get(device.toLowerCase());
		if (listByDevice == null) return new ArrayList<DriverModel>();
		return listByDevice;
	}

	private List<DriverModel> listByDeviceAndDriver(String name,
			List<DriverModel> listByDevice) {
		List<DriverModel> listByDeviceAndDriver = new ArrayList<DriverModel>();
		for (DriverModel d: listByDevice){
			if(name.equalsIgnoreCase((d.driver().getName()))){
				listByDeviceAndDriver.add(d);
			}
		}
		return listByDeviceAndDriver;
	}

	public void clear() {
		createMaps();
	}

	public void delete(String id, String device) {
		DriverModel driver = retrieve(id, device);
		removeFromMap(driver);
	}


	private void removeFromMap(DriverModel driver) {
		modelMap.remove(driver.rowid());
		modelByIdMap.remove(driver.id());
		
		String name = driver.driver().getName().toLowerCase();
		if (driverCount.get(name).equals(0)){
			driverCount.remove(name);
			driverMap.remove(name);
		}
		driverCount.put(name, driverCount.get(name)-1);
		driverByTypeMap.get(name).remove(driver.rowid());
		
		String deviceName = driver.device().toLowerCase();
		driverByDeviceMap.get(deviceName).remove(driver);
	}
	
	private void insertOnMap(DriverModel driver) {
		modelMap.put(driver.rowid(), driver);
		modelByIdMap.put(driver.id(), driver);
		
		String name = driver.driver().getName().toLowerCase();
		if (!driverMap.containsKey(name)){
			driverMap.put(name, driver.driver());
			driverCount.put(name, 0);
			driverByTypeMap.put(name, new ArrayList<DriverModel>());
		}
		driverCount.put(name, driverCount.get(name)+1);
		driverByTypeMap.get(name).add(driver);
		
		String deviceName = driver.device().toLowerCase();
		if (!driverByDeviceMap.containsKey(deviceName)){
			driverByDeviceMap.put(deviceName, new ArrayList<DriverModel>());
		}
		driverByDeviceMap.get(deviceName).add(driver);
		
	}
	
	public DriverModel retrieve(String id, String device) {
		// find by id
		if (id != null && device == null){
			return modelByIdMap.get(id);
		}else if (device != null){ 
			List<DriverModel> drivers = driverByDeviceMap.get(device.toLowerCase());
			if (drivers != null && !drivers.isEmpty()){
				// find by driver
				if (id == null){
					return drivers.get(0);
				}
				// find by driver and id
				for (DriverModel d : drivers){
					if (id.equals(d.id())){
						return d;
					}
				}
			}
		}
		return null;
	}

}
