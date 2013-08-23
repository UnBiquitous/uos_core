package org.unbiquitous.uos.core.messageEngine.dataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;

public class UpService {
	
	public enum ParameterType{MANDATORY,OPTIONAL}
	
	private String name;
	
	private Map<String, ParameterType> parameters;
	
	public UpService() {}
	
	public UpService(String name) {
		this.name = name;
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the parameters
	 */
	public Map<String, ParameterType> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Map<String, ParameterType> parameters) {
		this.parameters = parameters;
	}

	public UpService addParameter(String paramName, ParameterType paramType){
		if (parameters == null){
			parameters = new HashMap<String, ParameterType>();
		}
		
		parameters.put(paramName, paramType);
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || ! (obj instanceof UpService))
			return false;
		
		UpService d = (UpService) obj;
		return this.name.equals(d.name);
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name",this.getName());
		
		addParameters(json, "parameters", this.parameters);
		return json;
	}

	private void addParameters(JSONObject json, String propName, Map<String, ParameterType> parameterMap) throws JSONException {
		JSONObject parameters = new JSONObject();
		json.put(propName, parameters);
		if (parameterMap != null){
			for (Entry<String, ParameterType> p:  this.parameters.entrySet()){
				parameters.put(p.getKey(),p.getValue().name());
			}
		}
	}
	
	public static UpService fromJSON(JSONObject s_json) throws JSONException {
		UpService s = new UpService();
		s.setName(s_json.getString("name"));
		
		JSONObject p_map = s_json.optJSONObject("parameters");
		if (p_map != null){
			for (Entry<String, Object> p:  p_map.toMap().entrySet()){
				ParameterType parameterType = ParameterType.valueOf(p.getValue().toString());
				s.addParameter(p.getKey(), parameterType);
			}
		}
		return s;
	}
}
