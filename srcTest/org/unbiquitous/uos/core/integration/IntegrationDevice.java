package org.unbiquitous.uos.core.integration;

import org.unbiquitous.uos.core.network.model.NetworkDevice;

public class IntegrationDevice extends NetworkDevice {

	private String name;
	
	public IntegrationDevice(String name) {
		this.name = name;
	}
	
	@Override
	public String getNetworkDeviceName() {
		return name;
	}

	@Override
	public String getNetworkDeviceType() {
		return "Integration";
	}

}
