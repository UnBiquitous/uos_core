package org.unbiquitous.uos.core.applicationManager;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.ClassLoaderUtils;
import org.unbiquitous.uos.core.ContextException;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.InitialProperties.Tuple;
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
    public final static String APPLICATION_DEFAULT_PATH = "ubiquitos.application.path";
    private static String APPLICATION_PATH;
    private static String DEFAULT_APPLICATION_PATH = "applications/";
    private InitialProperties properties;
	private final ApplicationManager manager;

    public ApplicationDeployer(InitialProperties properties, ApplicationManager manager) {
        this.properties = properties;
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
        if (properties != null) {
        	try {
				for(Tuple<Class<UosApplication>,String> t : properties.getApplications()){
					deployApplication(t.x.getCanonicalName(), t.y);
				}
			} catch (ClassNotFoundException e) {
				logger.log(Level.SEVERE, "Problems deploying applications.",e);
			}
        	
        } else {
            logger.fine("No parameters informed to Deployer.");
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
        if (properties.containsKey(APPLICATION_DEFAULT_PATH)){
        	APPLICATION_PATH = properties.getString(APPLICATION_DEFAULT_PATH);
        }else {
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

