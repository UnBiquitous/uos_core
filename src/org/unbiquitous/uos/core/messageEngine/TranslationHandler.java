package org.unbiquitous.uos.core.messageEngine;


/**
 * Interface responsible for abstracting the operation of translating an encapsulated message.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public interface TranslationHandler {

	/**
	 * This method is responsible for converting a encapsulated message into a plain message string.
	 * 
	 * @param originalMessage <code>String</code> containing the message to be decoded.
	 * @param deviceName <code>String</code> with the name of the device responsible for the call.
	 * @return <code>String</code> with the translated (decoded) message.
	 */
	public String decode(String originalMessage, String deviceName);
	
	/**
	 * This method is responsible for converting a plain message string into a encapsulated message.
	 * 
	 * @param originalMessage <code>String</code> containing the message to be encoded.
	 * @param deviceName <code>String</code> with the name of the device responsible for the call.
	 * @return <code>EncapsulatedServiceCall</code> containing the data about the response of the message (the encoded message).
	 */
	public String encode(String originalMessage, String deviceName);
	
	/**
	 * @return Returns the <code>String</code> id of the security type of the translation.
	 */
	public String getSecurityType();
	
}
