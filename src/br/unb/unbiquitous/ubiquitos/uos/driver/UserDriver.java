package br.unb.unbiquitous.ubiquitos.uos.driver;

import br.unb.unbiquitous.ubiquitos.uos.application.UOSMessageContext;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.UosEventDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

/**
 * This class represents a user information from the middleware view.
 * 
 * @author Tales <talesap@gmail.com>
 * @author Danilo <daniloavilaf@gmail.com>
 * 
 * @version 1.0
 * 
 * @since 2011.11.07
 */
public interface UserDriver extends UosEventDriver {

	public static final String USER_DRIVER = UserDriverNativeSupport.class.getCanonicalName();

	// possible events
	public static final String NEW_USER_EVENT_KEY = "NEW_USER_EVENT_KEY";
	public static final String CHANGE_INFORMATION_TO_USER_KEY = "CHANGE_INFORMATION_TO_USER_KEY";
	public static final String LOST_USER_EVENT_KEY = "LOST_USER_EVENT_KEY";

	public static final String NAME_PARAM = "name";
	public static final String EMAIL_PARAM = "email";
	public static final String LAST_LABEL_NAME = "lastlabelname";
	public static final String LAST_LABEL_EMAIL = "lastlabelemail";
	public static final String CONFIDENCE_PARAM = "confidence";
	public static final String POSITION_X_PARAM = "positionX";
	public static final String POSITION_Z_PARAM = "positionZ";
	public static final String POSITION_Y_PARAM = "positionY";

	public static final String SPECIFIC_FIELD_PARAM = "specificField";
	public static final String EVENT_KEY_PARAM = "eventKey";
	public static final String USER_PARAM = "user";

	public static final String BYTES_IMAGE_PARAM = "bytesImage";
	public static final String INDEX_IMAGE_PARAM = "indexImage";
	public static final String LENGTH_IMAGE_PARAM = "lengthImage";

	public static final String RETURN_PARAM = "return";

	public static final String SPECIAL_CHARACTER_SEPARATOR = ":";

	/* ******************
	 * Services 
	 * ******************/

	/**
	 * Retrieve information of the user with email
	 * 
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public abstract void retrieveUserInfo(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext);

	/**
	 * Save image for the user with email in parameter
	 * 
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public abstract void saveUserImage(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext);

	/**
	 * Remove images of user
	 * 
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public abstract void removeUserImages(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext);

	/**
	 * List all known users
	 * 
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public abstract void listKnownUsers(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext);

	/**
	 * Retrain the recognition algorithm
	 * 
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public abstract void retrain(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext);

}
