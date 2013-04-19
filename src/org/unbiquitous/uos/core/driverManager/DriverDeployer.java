package org.unbiquitous.uos.core.driverManager;

import java.util.ResourceBundle;

import org.unbiquitous.uos.core.ClassLoaderUtils;
import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.application.UOSMessageContext;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

/**
 * Class responsible for loading the specified driver in the Driver Manager.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
//TODO: Untested class
public class DriverDeployer {
	
	private static Logger logger = Logger.getLogger(DriverDeployer.class);
	
	private static final String DRIVER_LIST_RESOURCE_KEY = "ubiquitos.driver.deploylist";
        
    private static final String DRIVER_PATH_RESOURCE_KEY = "ubiquitos.driver.path";

    private static String DRIVER_PATH;

    private static String DEFAULT_DRIVER_PATH = "drivers/";
	
	private static String DRIVER_SEPARATOR = ";";
	
	private static String INSTANCE_ID_INDICATOR_BEGIN = "(";
	
	private static String INSTANCE_ID_INDICATOR_END = ")";

	private DriverManager driverManager;
	
	private ResourceBundle resourceBundle;
	
	/**
	 * Default Constructor
	 * 
	 * @param driverManager DriverManager which to load the drivers
	 * @param resourceBundle ResourceBundle containing the information about the drivers to load 
	 */
	public DriverDeployer(DriverManager driverManager, ResourceBundle resourceBundle) {
		this.driverManager = driverManager;
		this.resourceBundle = resourceBundle;
	}
	
	/**
	 * Method responsible to load the Driver from the property DRIVER_LIST_RESOURCE_KEY on the resoruceBundle.
	 * 
	 * @throws DriverManagerException
	 * @throws DriverNotFoundException 
	 */
	public void deployDrivers() throws DriverManagerException {
		logger.info("Deploying Drivers.");
		if (driverManager != null  && resourceBundle != null){
			String deployList = null;
			try {
				if (!resourceBundle.containsKey(DRIVER_LIST_RESOURCE_KEY)){
					logger.warn("No '"+DRIVER_LIST_RESOURCE_KEY+"' property defined. This implies on no drivers for this instance.");
	    			return;
				}
				deployList = resourceBundle.getString(DRIVER_LIST_RESOURCE_KEY);
			} catch (Exception e) {
				String errorMessage = "No "+DRIVER_LIST_RESOURCE_KEY+" specified.";
				logger.error(errorMessage,e);
				throw new DriverManagerException(errorMessage,e);
			}
			
			if (deployList != null && !deployList.isEmpty()){
				String[] driversList = deployList.split(DRIVER_SEPARATOR);
				
				if (driversList != null && driversList.length != 0){
					for (String driverData : driversList){
						try {
							deployDriverByProperty(driverData);
						} catch (InterfaceValidationException e) {
							String errorMessage = "The driver could not be deployed due to invalid interface specification.";
							logger.error(errorMessage,e);
							throw new DriverManagerException(errorMessage,e);
						}
					}
				}else{
					logger.debug("Data specified for "+DRIVER_LIST_RESOURCE_KEY+" is empty.");
				}
			}else{
				logger.debug("No "+DRIVER_LIST_RESOURCE_KEY+" specified.");
			}
		}else{
			logger.debug("No parameters (DriverManager or ResourceBundle) informed to Deployer.");
		}
	}

	/**
	 * Method responsible for deploying a single driver based on the property description of it
	 * 
	 * @param driverData The property line configuration of the driver to be deployed
	 * @return the Class of the informed driver.
	 * @throws DriverManagerException
	 * @throws DriverNotFoundException 
	 */
	private String deployDriverByProperty(String driverData)
			throws DriverManagerException, InterfaceValidationException {
		String driverClass;
		String instanceId = null;
		if (driverData.contains(INSTANCE_ID_INDICATOR_BEGIN) &&
				driverData.contains(INSTANCE_ID_INDICATOR_END)){
			// Driver data with specified instanceID
			instanceId = driverData.substring(
						driverData.indexOf(INSTANCE_ID_INDICATOR_BEGIN)+1,
						driverData.indexOf(INSTANCE_ID_INDICATOR_END)
						);
			driverClass = driverData.substring(
					0,
					driverData.indexOf(INSTANCE_ID_INDICATOR_BEGIN)
					);
		}else{
			if (driverData.contains(INSTANCE_ID_INDICATOR_BEGIN) ||
					driverData.contains(INSTANCE_ID_INDICATOR_BEGIN)){
				// Driver data with malformed specified instanceID
				String erroMessage = "DriverData '"+driverData+"' in "+DRIVER_LIST_RESOURCE_KEY+" is malformed.";
				logger.error(erroMessage);
				throw new DriverManagerException(erroMessage);
			}else{
				// Driver data without instanceId
				driverClass = driverData;
			}
		}		

		deployDriver(driverClass, instanceId);

		return driverClass;
	}

	/**
	 * 
	 * Method responsible for instantiating and deploying a specific driver into the middleware.
	 * 
	 * @param driverClass Name of the driver <code>Class</code> to be instantiated.
	 * @param instanceId Name of the InstanceID of the Driver do be Deployed (optional).
	 * @throws DriverManagerException
	 * @throws DriverNotFoundException 
	 */
	private void deployDriver(String driverClass, String instanceId)
			throws DriverManagerException, InterfaceValidationException {
		try {
			DRIVER_PATH = resourceBundle.getString(DRIVER_PATH_RESOURCE_KEY);
		} catch (Exception e) {
			DRIVER_PATH = DEFAULT_DRIVER_PATH;
		}
		
		try {
			ClassLoader classLoader = ClassLoaderUtils.builder
												.createClassLoader(DRIVER_PATH);
			Class<?> clazz = classLoader.loadClass(driverClass);
			UosDriver driver = (UosDriver) clazz.newInstance();
			try {
				driverManager.deployDriver(driver.getDriver(), driver, instanceId);
			} catch (DriverNotFoundException e) {
				try {
					driverManager.addToEquivalenceTree(driver.getParent());
					driverManager.deployDriver(driver.getDriver(), driver, instanceId);
				} catch (DriverNotFoundException ex) {
					logger.error("Problems ocurred in the registering of driver '"+driver.getDriver().getName()+
							"' and it will not be registered.", ex);
				}
			}
		} catch (Exception e) {
			logger.error(e);
			// TODO Auto-generated catch block
			new RuntimeException(e);
		} 
	}

	/**
	 * Method responsible for validating the interface of a driver's instance
	 * 
	 * @param driverInstance Driver Instance to be validated 
	 * @return <code>true</code> if the instance is valid. <code>false</code> otherwise.
	 */
	private boolean validateServiceInterface(UosDriver driverInstance) {
		if (driverInstance.getDriver() == null){
			String erroMessage = "DriverClass '"+driverInstance.getClass().getName()+"' does not inform a valid UPDriver instance.";
			logger.debug(erroMessage);
			return false;
		}
		if (driverInstance.getDriver().getServices() == null||
				driverInstance.getDriver().getServices().isEmpty()){
			String erroMessage = "DriverClass '"+driverInstance.getClass().getName()+"' does informs a empty or non-existent list of services.";
			logger.debug(erroMessage);
			return false;
		}
		for (UpService ups : driverInstance.getDriver().getServices()){
			try {
				driverInstance.getClass().getDeclaredMethod(ups.getName(), ServiceCall.class , ServiceResponse.class, UOSMessageContext.class);
			} catch (SecurityException e) {
				String erroMessage = "Service '"+ups.getName()+"' on DriverClass '"+driverInstance.getClass().getName()+"' has security acces issues.";
				logger.debug(erroMessage,e);
				return false;
			} catch (NoSuchMethodException e) {
				String erroMessage = "Service '"+ups.getName()+"' on DriverClass '"+driverInstance.getClass().getName()+"' does not exist.";
				logger.debug(erroMessage,e);
				return false;
			}
		}
		return true;
	}
	
}
