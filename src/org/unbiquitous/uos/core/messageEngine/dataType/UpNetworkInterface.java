package org.unbiquitous.uos.core.messageEngine.dataType;


public class UpNetworkInterface {

	private String netType;
	
	private String networkAddress;
	
	public UpNetworkInterface() {}
	
	public UpNetworkInterface(String netType, String networkAddress) {
		this.netType = netType;
		this.networkAddress = networkAddress;
	}



	public String getNetType() {
		return netType;
	}

	public void setNetType(String netType) {
		this.netType = netType;
	}

	public String getNetworkAddress() {
		return networkAddress;
	}

	public void setNetworkAddress(String networkAddress) {
		this.networkAddress = networkAddress;
	} 
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || ! (obj instanceof UpNetworkInterface) ){
			return false;
		}
		
		UpNetworkInterface d = (UpNetworkInterface) obj;
		
		return this.networkAddress == d.networkAddress || this.networkAddress.equals(d.networkAddress);
	}
	
}
