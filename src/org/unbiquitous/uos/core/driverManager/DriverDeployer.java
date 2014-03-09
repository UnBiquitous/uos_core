package org.unbiquitous.uos.core.driverManager;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.ClassLoaderUtils;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

/**
 * Class responsible for loading the specified driver in the Driver Manager.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
//TODO: Untested class
public class DriverDeployer {
	
	private static Logger logger = UOSLogging.getLogger();
	
	private static final String DRIVER_LIST_RESOURCE_KEY = "ubiquitos.driver.deploylist";
        
    private static final String DRIVER_PATH_RESOURCE_KEY = "ubiquitos.driver.path";

    private static String DRIVER_PATH;

    private static String DEFAULT_DRIVER_PATH = "drivers/";
	
	private static String DRIVER_SEPARATOR = ";";
	
	private static String INSTANCE_ID_INDICATOR_BEGIN = "(";
	
	private static String INSTANCE_ID_INDICATOR_END = ")";

	private DriverManager driverManager;
	
	private InitialProperties properties;
	
	public DriverDeployer(DriverManager driverManager, InitialProperties properties) {
		this.driverManager = driverManager;
		this.properties = properties;
	}
	
	/**
	 * Method responsible to load the Driver from the property DRIVER_LIST_RESOURCE_KEY on the resoruceBundle.
	 * 
	 * @throws DriverManagerException
	 * @throws DriverNotFoundException 
	 */
	public void deployDrivers() throws DriverManagerException {
		logger.info("Deploying Drivers.");
		if (driverManager != null  && properties != null){
			String deployList = null;
			try {
				if (!properties.containsKey(DRIVER_LIST_RESOURCE_KEY)){
					logger.warning("No '"+DRIVER_LIST_RESOURCE_KEY+"' property defined. This implies on no drivers for this instance.");
	    			return;
				}
				deployList = properties.getString(DRIVER_LIST_RESOURCE_KEY);
			} catch (Exception e) {
				String errorMessage = "No "+DRIVER_LIST_RESOURCE_KEY+" specified.";
				logger.log(Level.SEVERE,errorMessage,e);
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
							logger.log(Level.SEVERE,errorMessage,e);
							throw new DriverManagerException(errorMessage,e);
						}
					}
				}else{
					logger.fine("Data specified for "+DRIVER_LIST_RESOURCE_KEY+" is empty.");
				}
			}else{
				logger.fine("No "+DRIVER_LIST_RESOURCE_KEY+" specified.");
			}
		}else{
			logger.fine("No parameters informed to Deployer.");
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
				logger.log(Level.SEVERE,erroMessage);
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
		if(properties.containsKey(DRIVER_PATH_RESOURCE_KEY)) {
			DRIVER_PATH = properties.getString(DRIVER_PATH_RESOURCE_KEY);
		} else {
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
					logger.log(Level.SEVERE,"Problems ocurred in the registering of driver '"+driver.getDriver().getName()+
							"' and it will not be registered.", ex);
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Problems Deploying driver",e);
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
			logger.fine(erroMessage);
			return false;
		}
		if (driverInstance.getDriver().getServices() == null||
				driverInstance.getDriver().getServices().isEmpty()){
			String erroMessage = "DriverClass '"+driverInstance.getClass().getName()+"' does informs a empty or non-existent list of services.";
			logger.fine(erroMessage);
			return false;
		}
		for (UpService ups : driverInstance.getDriver().getServices()){
			try {
				driverInstance.getClass().getDeclaredMethod(ups.getName(), Call.class , Response.class, CallContext.class);
			} catch (SecurityException e) {
				String erroMessage = "Service '"+ups.getName()+"' on DriverClass '"+driverInstance.getClass().getName()+"' has security acces issues.";
				logger.log(Level.FINE,erroMessage,e);
				return false;
			} catch (NoSuchMethodException e) {
				String erroMessage = "Service '"+ups.getName()+"' on DriverClass '"+driverInstance.getClass().getName()+"' does not exist.";
				logger.log(Level.FINE,erroMessage,e);
				return false;
			}
		}
		return true;
	}
	
}
