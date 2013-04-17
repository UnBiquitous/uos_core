package br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.unb.unbiquitous.json.JSONArray;
import br.unb.unbiquitous.json.JSONException;
import br.unb.unbiquitous.json.JSONObject;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpNetworkInterface;

//TODO: all conversion from JSON to object is not tested
public class JSONDevice extends JSONObject{

	private static final String 	PROP_NAME	= "name";
	private static final String PROP_NETWORKS	= "networks";
	private static final String 	PROP_META	= "meta";

	public JSONDevice(String source) throws JSONException {
		super(source);
	}
	
	@SuppressWarnings("rawtypes")
	public JSONDevice(Map map) {
		super(map);
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
		
		this.put(PROP_META, bean.getMeta());
	}

	
	
	@SuppressWarnings("rawtypes")
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
		JSONObject metaJson = this.optJSONObject(PROP_META);
		if (metaJson != null){
			Iterator keys = metaJson.keys();
			while (keys.hasNext() ){
				String key = (String) keys.next();
				device.addProperty(key, metaJson.optString(key));
			}
		}
		return device;
	}
	
}
