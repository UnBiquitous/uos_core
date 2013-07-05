package org.unbiquitous.uos.core.deviceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;


public class DeviceDao {
	private Map<String,UpDevice>		deviceMap;
	private Map<String,UpDevice>		interfaceMap;
	private Map<String,List<UpDevice>> networkTypeMap;
	private Map<String,List<UpDevice>> addressMap;
	
	public DeviceDao(ResourceBundle bundle) {
		deviceMap		= new HashMap<String, UpDevice>();
		interfaceMap	= new HashMap<String, UpDevice>();
		networkTypeMap	= new HashMap<String, List<UpDevice>>();
		addressMap	= new HashMap<String, List<UpDevice>>();
	}
	
	public void save(UpDevice device) {
		if (find(device.getName()) != null){
			throw new RuntimeException("Atempt to insert a device with same name.");
		}
		if (device.getNetworks() != null){
			for (UpNetworkInterface ni : device.getNetworks()){
				interfaceMap.put(createInterfaceKey(ni), device);
				
				if(!networkTypeMap.containsKey(ni.getNetType())){
					networkTypeMap.put(ni.getNetType(), new ArrayList<UpDevice>());
				}
				networkTypeMap.get(ni.getNetType()).add(device);
				
				if(!addressMap.containsKey(ni.getNetworkAddress())){
					addressMap.put(ni.getNetworkAddress(), new ArrayList<UpDevice>());
				}
				addressMap.get(ni.getNetworkAddress()).add(device);
			}
		}
		deviceMap.put(device.getName().toLowerCase(),device);
	}

	private static String createInterfaceKey(UpNetworkInterface ni) {
		return ni.getNetworkAddress()+"@"+ni.getNetType();
	}

	public void update(String oldname, UpDevice device) {
		delete(oldname);
		save(device);
	}
	
	public void delete(String name) {
		UpDevice device = find(name);
		if (device.getNetworks() != null){
			for (UpNetworkInterface ni : device.getNetworks()){
				interfaceMap.remove(createInterfaceKey(ni));
				networkTypeMap.get(ni.getNetType()).remove(device);
				addressMap.get(ni.getNetworkAddress()).remove(device);
			}
		}
		deviceMap.remove(name.toLowerCase());
	}
	
	public List<UpDevice> list() {
		return new ArrayList<UpDevice>(deviceMap.values());
	}
	
	public List<UpDevice> list(String address, String networktype) {
		if (address != null && networktype != null){
			String key = createInterfaceKey(new UpNetworkInterface(networktype, address));
			UpDevice upDevice = interfaceMap.get(key);
			List<UpDevice> ret = new ArrayList<UpDevice>();
			if (upDevice != null){
				ret.add(upDevice);
			}
			return ret;
		}else if (address != null ){
			if(addressMap.containsKey(address)){
				return new ArrayList<UpDevice>(new HashSet<UpDevice>(addressMap.get(address)));
			}
		}else if (networktype != null ){
			if(networkTypeMap.containsKey(networktype)){
				return new ArrayList<UpDevice>(new HashSet<UpDevice>(networkTypeMap.get(networktype)));
			}
		}else{
			return list();
		}
		return new ArrayList<UpDevice>();
	}

	public UpDevice find(String name) {
		return deviceMap.get(name.toLowerCase());
	}
	
	public void clear(){
		deviceMap.clear();
		interfaceMap.clear();
		addressMap.clear();
		networkTypeMap.clear();
	}
}
