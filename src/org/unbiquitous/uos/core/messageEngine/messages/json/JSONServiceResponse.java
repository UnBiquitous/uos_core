package org.unbiquitous.uos.core.messageEngine.messages.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.messageEngine.messages.Message;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

public class JSONServiceResponse extends JSONMessage  {
	
	protected static final String PROP_RESPONSE_DATA = "responseData";
	
	public JSONServiceResponse(String source) throws JSONException {
		super(source);
	}
	
	
	public JSONServiceResponse(ServiceResponse bean) throws JSONException {
		super((Message)bean);
		this.put(PROP_RESPONSE_DATA,bean.getResponseData());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ServiceResponse getAsObject() throws JSONException{
		ServiceResponse serviceResponse = new ServiceResponse();
		
		
		if (!this.isNull(PROP_RESPONSE_DATA)){
			Map map = new HashMap();
			JSONObject obj = (JSONObject)this.get(PROP_RESPONSE_DATA);
			if (obj != null){
				Iterator<String>it = obj.sortedKeys();
				while (it.hasNext() ){
					String prop = it.next();
					map.put(prop, obj.get(prop));
				}
			}
			serviceResponse.setResponseData(map);
		}
		//serviceResponse.setType(Message.Type.valueOf((this.getString(PROP_TYPE))));
		serviceResponse.setError(this.optString(PROP_ERROR));
		
		return serviceResponse;
	}

}
