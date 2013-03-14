package br.unb.unbiquitous.ubiquitos.uos.integration;

import br.unb.unbiquitous.ubiquitos.network.model.NetworkDevice;

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
