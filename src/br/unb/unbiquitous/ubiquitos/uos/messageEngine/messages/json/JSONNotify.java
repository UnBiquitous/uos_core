package br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import br.unb.unbiquitous.json.JSONException;
import br.unb.unbiquitous.json.JSONObject;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.Message;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.Notify;

public class JSONNotify extends JSONMessage {
	
	protected static final String PROP_PARAMETERS = "parameters";
	
	protected static final String PROP_EVENT_KEY = "eventKey";
	protected static final String PROP_DRIVER = "driver";
	protected static final String PROP_INSTANCE_ID = "instanceId";
	
	
	public JSONNotify(String source) throws JSONException {
		super(source);
	}
	
	
	public JSONNotify(Notify bean) throws JSONException {
		super((Message)bean);
		this.put(PROP_EVENT_KEY,bean.getEventKey());
		this.put(PROP_PARAMETERS,bean.getParameters());
		this.put(PROP_DRIVER,bean.getDriver());
		this.put(PROP_INSTANCE_ID,bean.getInstanceId());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Notify getAsObject() throws JSONException{
		Notify notify = new Notify();
		
		notify.setEventKey(this.getString(PROP_EVENT_KEY));
		notify.setDriver(this.optString(PROP_DRIVER));
		notify.setInstanceId(this.optString(PROP_INSTANCE_ID));
		
		if (!this.isNull(PROP_PARAMETERS)){
			Map map = new HashMap();
			JSONObject obj = (JSONObject)this.get(PROP_PARAMETERS);
			if (obj != null){
				Iterator<String>it = obj.sortedKeys();
				while (it.hasNext() ){
					String prop = it.next();
					map.put(prop, obj.get(prop));
				}
			}
			notify.setParameters(map);
		}
		
		return notify;
	}
}
