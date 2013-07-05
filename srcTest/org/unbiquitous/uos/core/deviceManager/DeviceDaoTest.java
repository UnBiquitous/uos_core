package org.unbiquitous.uos.core.deviceManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.fest.assertions.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.uos.core.deviceManager.DeviceDao;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;



public class DeviceDaoTest {

	private DeviceDao dao;
	
	@Before public void setup(){
		dao = new DeviceDao(null);
	}
	
	@After public void teardown(){
		dao.clear();
	}
	
	@Test public void should_return_a_saved_device(){
		UpDevice device = new UpDevice("my.device");
		device.addNetworkInterface("192.168.1.100:7000", "Ethernet:TCP");
		dao.save(device);
		List<UpDevice> list = dao.list();
		assertNotNull(list);
		UpDevice ret = list.get(0);
		assertEquals(device,ret);
		assertNotNull(ret.getNetworks());
		assertEquals(device.getNetworks().get(0),ret.getNetworks().get(0));
	}
	
	@Test public void should_return_a_saved_device_with_multiple_interfaces(){
		UpDevice device = new UpDevice("my.device");
		device.addNetworkInterface("192.168.1.100:7000", "Ethernet:TCP");
		device.addNetworkInterface("10.0.0.10:7000", "Ethernet:TCP");
		device.addNetworkInterface("AAAAAAAAAAAAAB", "Bluetooth");
		dao.save(device);
		List<UpDevice> list = dao.list();
		assertNotNull(list);
		UpDevice ret = list.get(0);
		assertEquals(device,ret);
		assertNotNull(ret.getNetworks());
		assertEquals(device.getNetworks().get(0),ret.getNetworks().get(0));
		assertEquals(device.getNetworks().get(1),ret.getNetworks().get(1));
		assertEquals(device.getNetworks().get(2),ret.getNetworks().get(2));
	}
	
	@Test public void should_return_multiple_saved_devices(){
		List<UpDevice> devices = new ArrayList<UpDevice>(); 
		devices.add(new UpDevice("my.device1"));
		devices.add(new UpDevice("my.device2"));
		devices.add(new UpDevice("my.device3"));
		for (int i = 0; i < devices.size(); i++) dao.save(devices.get(i));
		List<UpDevice> list = dao.list();
		assertNotNull(list);
		assertEquals(devices.size(),list.size());
		for (UpDevice d: devices) assertTrue(list.contains(d));
	}
	
	@Test(expected=Exception.class) public void should_not_save_a_device_with_the_same_name(){
		dao.save(new UpDevice("my.device"));
		dao.save(new UpDevice("my.device"));
	}
	
	//public UpDevice find(String deviceName);
	@Test public void should_find_by_devicename(){
		List<UpDevice> devices = new ArrayList<UpDevice>(); 
		devices.add(new UpDevice("my.device1"));
		devices.add(new UpDevice("my.device2"));
		devices.add(new UpDevice("my.device3"));
		for (int i = 0; i < devices.size(); i++) dao.save(devices.get(i));
		UpDevice ret = dao.find(devices.get(1).getName());
		assertNotNull(ret);
		assertEquals(devices.get(1),ret);
	}
	
	@Test public void should_find_by_devicename_ignoringcase(){
		List<UpDevice> devices = new ArrayList<UpDevice>(); 
		devices.add(new UpDevice("my.device1"));
		devices.add(new UpDevice("my.device2"));
		devices.add(new UpDevice("my.device3"));
		for (int i = 0; i < devices.size(); i++) dao.save(devices.get(i));
		UpDevice ret = dao.find("My.DeViCe2");
		assertNotNull(ret);
		assertEquals(devices.get(1),ret);
	}
	
	@Test public void should_find_by_devicename_but_not_find_unwanted_ones(){
		List<UpDevice> devices = new ArrayList<UpDevice>(); 
		devices.add(new UpDevice("my.device1"));
		devices.add(new UpDevice("my.device2"));
		devices.add(new UpDevice("my.device3"));
		for (int i = 0; i < devices.size(); i++) dao.save(devices.get(i));
		UpDevice ret = dao.find("my.bazzinga");
		assertNull(ret);
	}
	
	//public UpDevice find(String networkType, String networkAddress);
	@Test public void should_list_by_networktype_or_networkaddress(){
		List<UpDevice> devices = new ArrayList<UpDevice>(); 
		devices.add(new UpDevice("my.device1").addNetworkInterface("192.168.1.100", "Etherenet:TCP"));
		devices.add(new UpDevice("my.device2").addNetworkInterface("AAAAAAAAAAAAB", "Bluetooth"));
		devices.add(new UpDevice("my.device3").addNetworkInterface("192.168.1.101", "Etherenet:TCP"));
		devices.add(new UpDevice("my.device4").addNetworkInterface("AAAAAAAAAAAAB", "NotBluetooth"));
		for (int i = 0; i < devices.size(); i++) dao.save(devices.get(i));
		
		//check for network type
		List<UpDevice> ret = dao.list(null,"Etherenet:TCP");
		assertNotNull(ret);
		assertEquals(2,ret.size());
		assertTrue(ret.contains(devices.get(0)));
		assertTrue(ret.contains(devices.get(2)));
		
		//check for address
		ret = dao.list("AAAAAAAAAAAAB",null);
		assertNotNull(ret);
		assertEquals(2,ret.size());
		assertTrue(ret.contains(devices.get(1)));
		assertTrue(ret.contains(devices.get(3)));
		
		//check for both
		ret = dao.list("AAAAAAAAAAAAB","Bluetooth");
		assertNotNull(ret);
		assertEquals(1,ret.size());
		assertTrue(ret.contains(devices.get(1)));
	}
	
	//public UpDevice find(String networkType, String networkAddress);
	@Test public void should_list_by_networktype_or_networkaddress_considering_all_interfaces(){
		List<UpDevice> devices = new ArrayList<UpDevice>(); 
		devices.add(new UpDevice("my.device1")
			.addNetworkInterface("192.168.1.100", "Etherenet:TCP")
			.addNetworkInterface("10.0.0.10", "Etherenet:TCP")
			.addNetworkInterface("AAAAAAAAAAAAC", "Bluetooth"));
		devices.add(new UpDevice("my.device2").addNetworkInterface("AAAAAAAAAAAAB", "Bluetooth"));
		devices.add(new UpDevice("my.device3").addNetworkInterface("192.168.1.101", "Etherenet:TCP"));
		for (int i = 0; i < devices.size(); i++) dao.save(devices.get(i));
		
		//check for network type
		List<UpDevice> ret = dao.list(null,"Etherenet:TCP");
		assertNotNull(ret);
		assertEquals(2,ret.size());
		assertTrue(ret.contains(devices.get(0)));
		assertTrue(ret.contains(devices.get(2)));
		
		assertThat(dao.list("nonexists","Etherenet:TCP")).isEmpty();
		
		//check for address
		ret = dao.list("10.0.0.10",null);
		assertNotNull(ret);
		assertEquals(1,ret.size());
		assertTrue(ret.contains(devices.get(0)));
		
		assertThat(dao.list("10.0.0.10","nonexists")).isEmpty();
		
		//check for both
		ret = dao.list("AAAAAAAAAAAAC", "Bluetooth");
		assertNotNull(ret);
		assertEquals(1,ret.size());
		assertTrue(ret.contains(devices.get(0)));
	}
		
	@Test public void should_allow_to_update_a_device_name(){
		dao.save(new UpDevice("oldname"));
		UpDevice device = new UpDevice("newname");
		dao.update("oldname",device);
		assertNull(dao.find("oldname"));
		UpDevice ret = dao.find("newname");
		assertNotNull(ret);
		assertEquals(device,ret);
	}
	
	@Test public void should_delete_device_data(){
		dao.save(new UpDevice("d"));
		dao.delete("d");
		assertNull(dao.find("d"));
		assertTrue(dao.list().isEmpty());
	}
	
	@Test public void should_delete_only_the_requested_device_data(){
		dao.save(new UpDevice("a"));
		dao.save(new UpDevice("b"));
		dao.save(new UpDevice("c"));
		dao.delete("b");
		assertNull(dao.find("b"));
		assertEquals(2,dao.list().size());
	}
	
}
