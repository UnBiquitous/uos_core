package br.unb.unbiquitous.ubiquitos.uos.security.basic;

import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.authentication.Cipher;
import br.unb.unbiquitous.ubiquitos.authentication.SessionData;
import br.unb.unbiquitous.ubiquitos.authentication.SessionKeyDao;
import br.unb.unbiquitous.ubiquitos.authentication.SessionKeyDaoHSQLDB;
import br.unb.unbiquitous.ubiquitos.authentication.exception.ExpiredSessionKeyException;
import br.unb.unbiquitous.ubiquitos.authentication.exception.IdNotFoundException;
import br.unb.unbiquitous.ubiquitos.authentication.messages.FirstMessage;
import br.unb.unbiquitous.ubiquitos.authentication.messages.SecondMessage;
import br.unb.unbiquitous.ubiquitos.authentication.messages.ThirdMessage;
import br.unb.unbiquitous.ubiquitos.uos.application.UOSMessageContext;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.MessageHandler;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.TranslationHandler;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;
import br.unb.unbiquitous.ubiquitos.uos.security.AuthenticationHandler;

/**
 * 
 * Implementation of the basic method of authentication
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class BasicSecurityHandler implements AuthenticationHandler, TranslationHandler {

	private static final Logger logger = Logger.getLogger(BasicSecurityHandler.class);
	private static String SECURITY_TYPE = "BASIC";  
	SessionKeyDao sessionKeyDao = new SessionKeyDaoHSQLDB();;
	
	/**
	 * @see AuthenticationHandler#authenticate(ServiceCall, ServiceResponse, UOSMessageContext);
	 */
	@Override
	public void authenticate(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		
		AuthenticationDaoHSQLDB authenticationDao = new AuthenticationDaoHSQLDB();  
		
		br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler authentication;
		authentication = new br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler(authenticationDao, sessionKeyDao);
		
		sessionKeyDao = authentication.getSessionKeyDao();
		
		SecondMessage secondMessage = null;
		
		if (serviceCall.getParameters().containsKey("hashId")){
			
			logger.debug("Authenticate: middleware executes second step.");
			
			try{
				String hashId = serviceCall.getParameters().get("hashId");
				String idEnc = serviceCall.getParameters().get("idEnc");
				String ra1Enc = serviceCall.getParameters().get("ra1Enc"); 
				String ra2Enc = serviceCall.getParameters().get("ra2Enc");
				String hmacM1 = serviceCall.getParameters().get("hmacM1");
				
				secondMessage = authentication.runSecondStep(hashId, idEnc, ra1Enc, ra2Enc, hmacM1);

				Map<String, String> responseData = new HashMap<String, String>();
				
				responseData.put("hmacM2", secondMessage.getHmac());
				responseData.put("idEnc", secondMessage.getIdEnc());
				responseData.put("ra1IncEnc", secondMessage.getRa1IncEnc());
				responseData.put("ra2IncEnc", secondMessage.getRa2IncEnc());
				responseData.put("rb1Enc", secondMessage.getRb1Enc());
				responseData.put("rb2Enc", secondMessage.getRb2Enc());
				
				serviceResponse.setResponseData(responseData);
	
			} catch (Exception e){
				Map<String, String> responseData = new HashMap<String, String>();
				responseData.put("error", e.toString());
				serviceResponse.setResponseData(responseData);
				logger.fatal(e.toString());
			}
		}
		
		else {
			if (serviceCall.getParameters().containsKey("sessionKeyEnc")){
				
				logger.debug("Authenticate: middleware executes fourth step.");

				try{
						boolean result = authentication.runFourthStep (
								serviceCall.getParameters().get("sessionKeyEnc"), 
								serviceCall.getParameters().get("rb1"), 
								serviceCall.getParameters().get("hmacM3"), 
								serviceCall.getParameters().get("id"));

						Map<String, String> responseData = new HashMap<String, String>();

						if (result){
							responseData.put("result", "true");
							logger.debug("Authentication performed successfully. Service returned value \"true\"");
						} else{
							responseData.put("result", "false");
							logger.debug("Authentication failure. Service returned value \"false\"");
						}

						serviceResponse.setResponseData(responseData);
				} catch (Exception e){
					logger.fatal(e.toString());
				}
			}
		}
	}

	/**
	 * @see AuthenticationHandler#authenticate(String, AdaptabilityEngine);
	 */
	@Override
	public void authenticate(UpDevice upDevice, MessageHandler messageHandler) {

		String deviceName = upDevice.getName();
		
		AuthenticationDaoHSQLDB authenticationDao = new AuthenticationDaoHSQLDB(); 
		String databaseName = "authenticationData" + deviceName;
		DeviceAuthenticationDaoHSQLDB deviceAutenticationDao = new DeviceAuthenticationDaoHSQLDB(databaseName); 
		
		br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler authentication;
		authentication = new br.unb.unbiquitous.ubiquitos.authentication.AuthenticationHandler(authenticationDao, sessionKeyDao);
				
		try{
			logger.debug("Authenticate: device " +deviceName+ " starts authentication proccess.");
			
			String ka;
			
			try{
				logger.debug("Device retrieves key from database.");
				ka = deviceAutenticationDao.findById(deviceName).getKey();
			} catch (NullPointerException e){
				logger.fatal("Id not found in database");
				throw new IdNotFoundException();
			}
			
			logger.debug("Device executes first step of authentication process.");
			FirstMessage firstMessage = authentication.runFirstStep(deviceName, ka);

			ServiceCall serviceCall = new ServiceCall();
			
			Map<String, String> authenticationData = new HashMap<String, String>();

			authenticationData.put("hashId", firstMessage.getHashId());
			authenticationData.put("idEnc", firstMessage.getIdEnc());
			authenticationData.put("ra1Enc", firstMessage.getRa1Enc());
			authenticationData.put("ra2Enc", firstMessage.getRa2Enc());
			authenticationData.put("hmacM1", firstMessage.getHmacM1());
			authenticationData.put("securityType", SECURITY_TYPE);
			
			serviceCall.setParameters(authenticationData);
			serviceCall.setServiceType(ServiceCall.ServiceType.DISCRETE);
			serviceCall.setService("authenticate");
			serviceCall.setDriver("br.unb.unbiquitous.ubiquitos.driver.DeviceDriver");
			
			ServiceResponse serviceResponse = messageHandler.callService(upDevice, serviceCall);
			
			logger.debug("Device executes third step of authentication process.");
			ThirdMessage thirdMessage = authentication.runThirdStep(
					ka, 
					firstMessage.getRa1(), 
					firstMessage.getRa2(), 
					deviceName, 
					serviceResponse.getResponseData().get("hmacM2"),
					serviceResponse.getResponseData().get("idEnc"),
					serviceResponse.getResponseData().get("ra1IncEnc"),
					serviceResponse.getResponseData().get("ra2IncEnc"),
					serviceResponse.getResponseData().get("rb1Enc"),
					serviceResponse.getResponseData().get("rb2Enc"));
			
			authenticationData = new HashMap<String, String>();
			authenticationData.put("sessionKeyEnc", thirdMessage.getSessionKeyEnc());
			authenticationData.put("hmacM3", thirdMessage.getHmac());
			authenticationData.put("id", thirdMessage.getId());
			authenticationData.put("securityType", SECURITY_TYPE);
			
			Cipher c = new Cipher(ka);
			authenticationData.put("rb1", c.decrypt(serviceResponse.getResponseData().get("rb1Enc")));
			
			serviceCall.setParameters(authenticationData);
			serviceResponse = new ServiceResponse();
			
			serviceResponse = messageHandler.callService(upDevice, serviceCall);

			logger.debug("Service Response after the fourth step: "+serviceResponse.getResponseData().values());
			
		} catch (Exception e){
			logger.error(e);
		} 
	}
	
	/**
	 * @see AuthenticationHandler#getSecurityType();
	 * @see TranslationHandler#getSecurityType();
	 */
	@Override
	public String getSecurityType() {
		return SECURITY_TYPE;
	}
	
	/**
	 * @see TranslationHandler#decode(String, String)
	 */
	public String decode(String originalMessage, String deviceName){
		logger.debug("Uncapsulating request (decrypt) : "+originalMessage);
		logger.debug("Device name: "+deviceName);

		// creates new String to store result
		String processedMessage = null;
		
		try{
			//retrieves sessionKey
			SessionData sessionData = sessionKeyDao.findById(deviceName); 
			
			//retrieves expiration date
			Date expirationDate = sessionData.getExpirationDate();
			Time expirationTime = sessionData.getExpirationTime();					
			
			if (sessionKeyDao.isBeforeToday(expirationTime, expirationDate)){
	
//				//retrieves device's key
//				AuthenticationDao authenticationDao = new AuthenticationDaoHSQLDB();
//				AuthenticationData authenticationData = authenticationDao.findByHashId(HashGenerator.generateHash(deviceName));
//				//creates new cipher using device's key 
//								
//				Cipher cipher = new Cipher(authenticationData.getKey());
				
				Cipher cipher = new Cipher(sessionData.getSessionKey());
				
				//decrypts original message
				processedMessage = cipher.decrypt(originalMessage);
				
				logger.debug("into request: "+processedMessage);
				return processedMessage;
			} else{
				throw new ExpiredSessionKeyException();
			}
			
		} catch(Exception ex){ 
			logger.fatal(ex.toString());
    	}
		return processedMessage;
	}
	
	/**
	 * @see TranslationHandler#encode(String, String)
	 */
	public String encode(String originalMessage, String deviceName){
		logger.debug("Encapsulating response : "+originalMessage+", device name:"+deviceName);

		// creates new String to store result
		String processedMessage = null;
		
		try{
			//retrieves sessionKey
			SessionData sessionData = sessionKeyDao.findById(deviceName); 

			//retrieves expiration date
			Date expirationDate = sessionData.getExpirationDate();
			Time expirationTime = sessionData.getExpirationTime();

			if (sessionKeyDao.isBeforeToday(expirationTime, expirationDate)){

//				//retrieves device's key
//				AuthenticationDao authenticationDao = new AuthenticationDaoHSQLDB();
//				AuthenticationData authenticationData = authenticationDao.findByHashId(HashGenerator.generateHash(deviceName));
//				
//				//creates new cipher using device's key 
//				Cipher cipher = new Cipher(authenticationData.getKey());
				
				Cipher cipher = new Cipher(sessionData.getSessionKey());
				
				//decrypts original message
				processedMessage = cipher.encrypt(originalMessage);
				logger.debug("into response (encrypt) : "+processedMessage);
				
				return processedMessage;
			} else{
				throw new ExpiredSessionKeyException();
			}
			
		} catch(Exception ex){ 
			logger.fatal(ex.toString());
    	}
		
		return processedMessage;
	}
}
