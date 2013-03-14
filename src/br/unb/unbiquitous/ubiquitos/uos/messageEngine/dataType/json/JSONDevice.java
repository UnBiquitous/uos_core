package br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.json;

import java.util.ArrayList;
import java.util.List;

import br.unb.unbiquitous.json.JSONArray;
import br.unb.unbiquitous.json.JSONException;
import br.unb.unbiquitous.json.JSONObject;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpNetworkInterface;

public class JSONDevice extends JSONObject{

	private static final String PROP_NETWORKS = "networks";
	private static final String PROP_NAME = "name";

	public JSONDevice(String source) throws JSONException {
		super(source);
	}



	public JSONDevice(UpDevice bean) throws JSONException {
		this.put(PROP_NAME,bean.getName());
		
		//Set<NetInterface> networks;
		if (bean.getNetworks() != null && !bean.getNetworks().isEmpty()){
			for (UpNetworkInterface ni : bean.getNetworks()){
				this.append(PROP_NETWORKS,new JSONNetInterface(ni));
			}
		}else{
			this.put(PROP_NETWORKS, (Object)null);
		}
		
	}

	
	
	public UpDevice getAsObject() throws JSONException{
		UpDevice device = new UpDevice();
		
		device.setName(this.getString(PROP_NAME));
		//Set<NetInterface> networks;
		if (this.get(PROP_NETWORKS) != null && this.get(PROP_NETWORKS) instanceof JSONArray){
			List<UpNetworkInterface> networks = new ArrayList<UpNetworkInterface>();
			JSONArray array = (JSONArray)this.get(PROP_NETWORKS);
			for (int i = 0 ; i < array.length() ; i ++){
				Object o = array.get(i);
				JSONNetInterface jsonNI = new JSONNetInterface(o.toString());
				networks.add(jsonNI.getAsObject());
			}
			device.setNetworks(networks);
		}
		return device;
	}
	
}
