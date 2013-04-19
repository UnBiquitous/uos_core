package org.unbiquitous.uos.core.messageEngine.dataType.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;


public class JSONService extends JSONObject {
	
	private static final String PROP_NAME = "name";
	private static final String PROP_PARAMETERS = "parameters";

	public JSONService(String source) throws JSONException {
		super(source);
	}

	public JSONService(UpService bean) throws JSONException {
		this.put(PROP_NAME,bean.getName());
		this.put(PROP_PARAMETERS,bean.getParameters());
	}
	
	@SuppressWarnings("unchecked")
	public UpService getAsObject() throws JSONException{
		UpService service = new UpService();
		
		service.setName(this.getString(PROP_NAME));
		
		if (!this.isNull(PROP_PARAMETERS)){
			Map<String,UpService.ParameterType> map = new HashMap<String,UpService.ParameterType>();
			JSONObject obj = (JSONObject)this.get(PROP_PARAMETERS);
			if (obj != null){
				Iterator<String> it = obj.sortedKeys();
				while (it.hasNext() ){
					String prop = it.next();
					map.put(prop, UpService.ParameterType.valueOf(obj.getString(prop)));
				}
			}
			service.setParameters(map);
		}
		
		return service;
	}
}
