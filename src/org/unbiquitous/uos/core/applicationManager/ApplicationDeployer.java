package org.unbiquitous.uos.core.applicationManager;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.ClassLoaderUtils;
import org.unbiquitous.uos.core.ContextException;
import org.unbiquitous.uos.core.UOSLogging;


/**
 * Class responsible for managing the process of deploying and undeploying an 
 * application into the middleware.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class ApplicationDeployer {

    private static Logger logger = UOSLogging.getLogger();
    public final static String APPLICATION_LIST = "ubiquitos.application.deploylist";
    public final static String APPLICATION_DEFAULT_PATH = "ubiquitos.application.path";
    public final static String APPLICATION_SEPARATOR = ";";
    public final static String INSTANCE_ID_INDICATOR_BEGIN = "(";
    public final static String INSTANCE_ID_INDICATOR_END = ")";
    private static String APPLICATION_PATH;
    private static String DEFAULT_APPLICATION_PATH = "applications/";
    private ResourceBundle resourceBundle;
	private final ApplicationManager manager;

    /**
     * Default Constructor
     * 
     * @param resourceBundle ResourceBundle containing the information about 
     * the applications to load.
     */
    public ApplicationDeployer(ResourceBundle resourceBundle, ApplicationManager manager) {
        this.resourceBundle = resourceBundle;
		this.manager = manager;
    }

    /**
     * Method responsible to load the Applications from the property 
     * APPLICATION_LIST_RESOURCE_KEY on the resoruceBundle.
     * 
     * @throws ContextException
     */
    public void deployApplications() throws ContextException {
        logger.info("Iniatialize Application Deploy.");
        if (resourceBundle != null) {
            String applicationList = null;
            try {
                applicationList = resourceBundle.getString(APPLICATION_LIST);
            } catch (Exception e) {
                String erroMessage = "No " + APPLICATION_LIST + " specified.";
                logger.severe(erroMessage);
                return;
            }

            if (applicationList != null && !applicationList.isEmpty()) {
                String[] applicationsList = applicationList.split(APPLICATION_SEPARATOR);

                if (applicationsList != null && applicationsList.length != 0) {
                    for (String applicationData : applicationsList) {
                        String applicationClass;
                        String instanceId = null;
                        if (applicationData.contains(INSTANCE_ID_INDICATOR_BEGIN)
                                && applicationData.contains(INSTANCE_ID_INDICATOR_END)) {
                            // Application data with specified instanceID
                            instanceId = applicationData.substring(
                                    applicationData.indexOf(INSTANCE_ID_INDICATOR_BEGIN) + 1,
                                    applicationData.indexOf(INSTANCE_ID_INDICATOR_END));
                            applicationClass = applicationData.substring(
                                    0,
                                    applicationData.indexOf(INSTANCE_ID_INDICATOR_BEGIN));
                        } else {
                            if (applicationData.contains(INSTANCE_ID_INDICATOR_BEGIN)
                                    || applicationData.contains(INSTANCE_ID_INDICATOR_BEGIN)) {
                                // Application data with malformed specified instanceID
                                String erroMessage = "ApplicationData '" + applicationData + "' in " + APPLICATION_LIST + " is malformed.";
                                logger.severe(erroMessage);
                                throw new ContextException(erroMessage);
                            } else {
                                // Application data without instanceId
                                applicationClass = applicationData;
                            }
                        }

                        deployApplication(applicationClass, instanceId);
                    }
                } else {
                    logger.fine("Data specified for " + APPLICATION_LIST + " is empty.");
                }
            } else {
                logger.fine("No " + APPLICATION_LIST + " specified.");
            }
        } else {
            logger.fine("No parameters (ResourceBundle) informed to Deployer.");
        }
    }

    /**
     * Method responsible for instantiating and deploying a specific 
     * application into the middleware.
     * 
     * @param applicationClass Name of the Application <code>Class</code> to be instantiated.
     * @param instanceId Name of the InstanceID of the Application do be Deployed (optional).
     * @throws ContextException
     */
    //TODO: Untested
    public void deployApplication(String applicationClass, String instanceId)
            throws ContextException {
        try {
            APPLICATION_PATH = resourceBundle.getString(APPLICATION_DEFAULT_PATH);
        } catch (Exception e) {
            APPLICATION_PATH = DEFAULT_APPLICATION_PATH;
        }
        try {
        	ClassLoader classLoader = ClassLoaderUtils.builder
											.createClassLoader(APPLICATION_PATH);
			
			Class<?> clazz = classLoader.loadClass(applicationClass);
			UosApplication applicationInstance = (UosApplication) clazz.newInstance();
			logger.info("Deploying Application " + applicationInstance.getClass().getName());
//			deployApplication(applicationInstance,instanceId);
			this.manager.add(applicationInstance, instanceId);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Not possible to deploy "+applicationClass,e);
		} 
    }

	

}

