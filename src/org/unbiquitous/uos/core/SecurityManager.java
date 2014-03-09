package org.unbiquitous.uos.core;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.messageEngine.TranslationHandler;


/**
 * Class responsible for dealing with the security handler, access and permissions of the middleware
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class SecurityManager {

	private static final Logger logger = UOSLogging.getLogger();
	
	private static final String AUTHENTICATION_HANDLER_LIST_RESOURCE_KEY = "ubiquitos.security.authenticationHandlerList";
	private static final String TRANSLATION_HANDLER_LIST_RESOURCE_KEY = "ubiquitos.security.translationHandlerList";
	private static String LIST_SEPARATOR = ",";
	
	
	/**
	 * Map containing the AuthenticationHandlers available in the middleware.
	 */
	private Map<String, AuthenticationHandler> authenticationHandlers = new HashMap<String, AuthenticationHandler>();
	/**
	 * Map containing the TranslationHandlers available in the middleware.
	 */
	private Map<String, TranslationHandler> translationHandlers = new HashMap<String, TranslationHandler>();
	
	private InitialProperties properties;
	
	
	public SecurityManager(InitialProperties properties) throws SecurityException {
		this.properties = properties;
		
		loadAuthenticationHandlers();
		loadTranslationHandlers();
		
	}
	
	/**
	 * Method responsible for loading the list of AuthenticationHandlers from the resource bundle.
	 * 
	 * @throws SecurityException
	 */
	private void loadAuthenticationHandlers() throws SecurityException{
		
		if (properties != null){
			String authenticationHandlerProperty = null;
			if(properties.containsKey(AUTHENTICATION_HANDLER_LIST_RESOURCE_KEY)) {
				authenticationHandlerProperty = properties.getString(AUTHENTICATION_HANDLER_LIST_RESOURCE_KEY);
			} else {
				String erroMessage = "No "+AUTHENTICATION_HANDLER_LIST_RESOURCE_KEY+" specified.";
				logger.fine(erroMessage);
				return;
			}
			
			if (authenticationHandlerProperty != null && !authenticationHandlerProperty.isEmpty()){
				String[] authenticationHandlerList = authenticationHandlerProperty.split(LIST_SEPARATOR);
				
				if (authenticationHandlerList != null && authenticationHandlerList.length != 0){
					for (String authHandler : authenticationHandlerList){
						
						try {
							Class<?> clazz = Class.forName(authHandler);
							AuthenticationHandler ah = (AuthenticationHandler)clazz.newInstance();
							if (ah instanceof AuthenticationHandler){
								logger.fine("Loading AuthenticationHandler '"+authHandler+"'");
								authenticationHandlers.put(ah.getSecurityType(), ah);
							}else{
								logger.severe("The AuthenticationHandler '"+authHandler+"' does not implements the apropriated interface and will not be used.");
							}
						} catch (ClassNotFoundException e) {
							logger.log(Level.SEVERE,"The AuthenticationHandler '"+authHandler+"' will not be used because of the following errors.",e);
						} catch (InstantiationException e) {
							logger.log(Level.SEVERE,"The AuthenticationHandler '"+authHandler+"' will not be used because of following errors.",e);
						} catch (IllegalAccessException e) {
							logger.log(Level.SEVERE,"The AuthenticationHandler '"+authHandler+"' will not be used because of following errors.",e);
						}
					}
				}
			}else{
				String erroMessage = "No "+AUTHENTICATION_HANDLER_LIST_RESOURCE_KEY+" specified.";
				logger.severe(erroMessage);
				throw new SecurityException(erroMessage);
			}
		}else{
			String erroMessage = "No resource bundle informed.";
			logger.severe(erroMessage);
			throw new SecurityException(erroMessage);
		}
	}
	
	/**
	 * Method responsible for loading the list of TranslationHandlers from the resource bundle.
	 * 
	 * @throws SecurityException
	 */
	private void loadTranslationHandlers() throws SecurityException{
		
		if (properties != null){
			String translationHandlerProperty = null;
			if(properties.containsKey(TRANSLATION_HANDLER_LIST_RESOURCE_KEY)) {
				translationHandlerProperty = properties.getString(TRANSLATION_HANDLER_LIST_RESOURCE_KEY);
			} else {
				String erroMessage = "No "+TRANSLATION_HANDLER_LIST_RESOURCE_KEY+" specified.";
				logger.fine(erroMessage);
				return;
			}
			
			if (translationHandlerProperty != null && !translationHandlerProperty.isEmpty()){
				String[] translationHandlerList = translationHandlerProperty.split(LIST_SEPARATOR);
				
				if (translationHandlerList != null && translationHandlerList.length != 0){
					for (String tranlationHandler : translationHandlerList){
						
						try {
							Class<?> clazz = Class.forName(tranlationHandler);
							TranslationHandler th = (TranslationHandler)clazz.newInstance();
							if (th instanceof TranslationHandler){
								logger.fine("Loading TranslationHandler '"+tranlationHandler+"'");
								translationHandlers.put(th.getSecurityType(), th);
							}else{
								logger.severe("The TranslationHandler '"+tranlationHandler+"' does not implements the apropriated interface and will not be used.");
							}
						} catch (ClassNotFoundException e) {
							logger.log(Level.SEVERE,"The TranslationHandler '"+tranlationHandler+"' will not be used because of the following errors.",e);
						} catch (InstantiationException e) {
							logger.log(Level.SEVERE,"The TranslationHandler '"+tranlationHandler+"' will not be used because of following errors.",e);
						} catch (IllegalAccessException e) {
							logger.log(Level.SEVERE,"The TranslationHandler '"+tranlationHandler+"' will not be used because of following errors.",e);
						}
					}
				}
			}else{
				String erroMessage = "No "+TRANSLATION_HANDLER_LIST_RESOURCE_KEY+" specified.";
				logger.severe(erroMessage);
				throw new SecurityException(erroMessage);
			}
		}else{
			String erroMessage = "No resource bundle informed.";
			logger.severe(erroMessage);
			throw new SecurityException(erroMessage);
		}
	}
	
	/**
	 * Method responsible for retrieving the <code>AuthenticationHandler</code> responsible for a security type. 
	 * 
	 * @param securityType Key of the security type.
	 * @return <code>AuthenticationHandler</code> responsible for the informed securityType.
	 */
	public AuthenticationHandler getAuthenticationHandler(String securityType){
		return authenticationHandlers.get(securityType);
	}
	
	/**
	 * Method responsible for retrieving the <code>TranslationHandler</code> responsible for a security type. 
	 * 
	 * @param securityType Key of the security type.
	 * @return <code>TranslationHandler</code> responsible for the informed securityType.
	 */
	public TranslationHandler getTranslationHandler(String securityType){
		return translationHandlers.get(securityType);
	}
	
}
