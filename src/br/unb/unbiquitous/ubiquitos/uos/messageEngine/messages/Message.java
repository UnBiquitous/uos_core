package br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages;


public class Message {
	
	public enum Type{SERVICE_CALL_REQUEST, SERVICE_CALL_RESPONSE,NOTIFY,ENCAPSULATED_MESSAGE};

	private Type type;
	
	private String error;
	
	public Message() {}
	
	public Message(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}

	protected void setType(Type type) {
		this.type = type;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	
}
