package org.unbiquitous.uos.core.messageEngine.messages;


public class EncapsulatedMessage extends Message {

	private String innerMessage;
	
	private String securityType;
	
	public EncapsulatedMessage() {
		setType(Type.ENCAPSULATED_MESSAGE);
	}

	public EncapsulatedMessage(String securityType, String innerMessage) {
		this();
		this.securityType = securityType;
		this.innerMessage = innerMessage;
	}



	public String getInnerMessage() {
		return innerMessage;
	}

	public void setInnerMessage(String innerMessage) {
		this.innerMessage = innerMessage;
	}

	/**
	 * @return the securityType
	 */
	public String getSecurityType() {
		return securityType;
	}

	/**
	 * @param securityType the securityType to set
	 */
	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}
	
}
