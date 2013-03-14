package br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType;

import java.util.HashMap;
import java.util.Map;

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
}
