package org.unbiquitous.uos.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
import org.unbiquitous.uos.core.network.model.NetworkDevice;

public class CurrentDeviceInitializer implements UOSComponent{
	private static final Logger logger = UOSLogging.getLogger();
	
	private InitialProperties properties;
	
	@Override
	public void create(InitialProperties properties) {
		this.properties = properties;}

	@Override
	public void init(UOSComponentFactory factory) {
		UpDevice currentDevice = factory.currentDevice(new UpDevice());
		if (properties.getDeviceName() != null){
			currentDevice.setName(properties.getDeviceName());
		}else{
			try {
				currentDevice.setName(InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException e) {
				new ContextException(e);
			}
		}

		if (currentDevice.getName().equals("localhost")){
			UUID uuid = UUID.randomUUID();
			currentDevice.setName(uuid.toString());
		}
		
		//get metadata
		currentDevice.addProperty("platform",System.getProperty("java.vm.name"));
		
		// Collect network interface information
		List<NetworkDevice> networkDeviceList = factory.get(ConnectionManagerControlCenter.class).getNetworkDevices();
		List<UpNetworkInterface> networks = new ArrayList<UpNetworkInterface>();
		for (NetworkDevice nd : networkDeviceList) {
			UpNetworkInterface nInf = new UpNetworkInterface();
			nInf.setNetType(nd.getNetworkDeviceType());
			nInf.setNetworkAddress(factory.get(ConnectionManagerControlCenter.class).getHost(nd.getNetworkDeviceName()));
			networks.add(nInf);
			logger.info(nd.getNetworkDeviceType() + " > " + nd.getNetworkDeviceName());
		}

		currentDevice.setNetworks(networks);
	}

	@Override
	public void start() {}

	@Override
	public void stop() {}

}
