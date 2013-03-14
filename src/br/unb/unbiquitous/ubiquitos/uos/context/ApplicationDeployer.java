package br.unb.unbiquitous.ubiquitos.uos.context;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.application.UosApplication;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.Ontology;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyStart;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.exception.ReasonerNotDefinedException;

/**
 * Class responsible for managing the process of deploying and undeploying an 
 * application into the middleware.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class ApplicationDeployer {

    private static Logger logger = Logger.getLogger(ApplicationDeployer.class);
    private static String APPLICATION_LIST_RESOURCE_KEY = "ubiquitos.application.deploylist";
    private static String APPLICATION_PATH_RESOURCE_KEY = "ubiquitos.application.path";
    private static String APPLICATION_SEPARATOR = ";";
    private static String INSTANCE_ID_INDICATOR_BEGIN = "(";
    private static String INSTANCE_ID_INDICATOR_END = ")";
    private static String APPLICATION_PATH;
    private static String DEFAULT_APPLICATION_PATH = "applications/";
    private ResourceBundle resourceBundle;
    private Gateway gateway;
    private Ontology ontology;
    private Map<String, UosApplication> applicationsLauncherMap = new HashMap<String, UosApplication>();

    /**
     * Default Constructor
     * 
     * @param resourceBundle ResourceBundle containing the information about 
     * the applications to load.
     */
    public ApplicationDeployer(ResourceBundle resourceBundle, Gateway gateway) {
        this.resourceBundle = resourceBundle;
        this.gateway = gateway;

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
                applicationList = resourceBundle.getString(APPLICATION_LIST_RESOURCE_KEY);
            } catch (Exception e) {
                String erroMessage = "No " + APPLICATION_LIST_RESOURCE_KEY + " specified.";
                logger.error(erroMessage);
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
                                String erroMessage = "ApplicationData '" + applicationData + "' in " + APPLICATION_LIST_RESOURCE_KEY + " is malformed.";
                                logger.error(erroMessage);
                                throw new ContextException(erroMessage);
                            } else {
                                // Application data without instanceId
                                applicationClass = applicationData;
                                instanceId = applicationClass + incRunningApplicationsCount();

                            }
                        }

                        deployApplication(applicationClass, instanceId);
                    }
                } else {
                    logger.debug("Data specified for " + APPLICATION_LIST_RESOURCE_KEY + " is empty.");
                }
            } else {
                logger.debug("No " + APPLICATION_LIST_RESOURCE_KEY + " specified.");
            }
        } else {
            logger.debug("No parameters (ResourceBundle) informed to Deployer.");
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
            APPLICATION_PATH = resourceBundle.getString(APPLICATION_PATH_RESOURCE_KEY);
        } catch (Exception e) {
            APPLICATION_PATH = DEFAULT_APPLICATION_PATH;
        }
        try {
			ClassLoader classLoader = new URLClassLoader(
					new URL[] { new File(APPLICATION_PATH).toURI().toURL() },
					ClassLoader.getSystemClassLoader());
			
			Class<?> clazz = classLoader.loadClass(applicationClass);
			UosApplication applicationInstance = (UosApplication) clazz.newInstance();
			logger.info("Deploying Application " + applicationInstance.getClass().getName());
			deployApplication(applicationInstance,instanceId);
		} catch (Exception e) {
			logger.error("Not possible to deploy "+applicationClass,e);
		} 
    }

	public void deployApplication(UosApplication applicationInstance, String instanceId) throws ContextException {
		initApplication(applicationInstance, instanceId);
        startApplication(applicationInstance, instanceId);
	}

    
    
    /**
     * 
     * Method responsible for undeploying a specific 
     * application from the middleware.
     * 
     * @param applicationClass Name of the Application <code>Class</code> to be instantiated.
     * @param instanceId Name of the InstanceID of the Application do be Deployed (optional).
     * @throws ContextException
     */
    public void undeployApplication(String instanceId)
            throws ContextException, Exception {
        UosApplication applicationInstance = applicationsLauncherMap.get(instanceId);
        tearDownApplication(applicationInstance, instanceId);
        stopApplication(applicationInstance, instanceId);
    }

    /**
     *
     * Method responsible for instantiating and deploying a specific Application into the middleware.
     *
     * @param applicationClass Name of the Application <code>Class</code> to be instantiated.
     * @param instanceId Name of the InstanceID of the Application do be Deployed (optional).
     * @throws ContextException
     */
    public void startApplication(Object instance, String instanceId)
            throws ContextException {
        ApplicationThreadLauncher atl = null;
        if (instance instanceof UosApplication) {
            logger.info("Starting Application " + instance.getClass().getName() + ".");
            applicationsLauncherMap.put(instanceId, (UosApplication) instance);
            try {
                ontology = new Ontology(resourceBundle);
                atl = new ApplicationThreadLauncher((UosApplication) instance, gateway, ontology);
            } catch (ReasonerNotDefinedException ex) {
                logger.info("Ontology component disabled.");
                atl = new ApplicationThreadLauncher((UosApplication) instance, gateway, null);
            }          
            Thread t = new Thread(atl);
            t.start();
        } else {
            String erroMessage = "ApplicationClass '" + instance.getClass().getName() + "' specified in " + APPLICATION_LIST_RESOURCE_KEY + " is not a UbiqtoOSApplication.";
            logger.error(erroMessage);
            throw new ContextException(erroMessage);
        }
    }

    /**
     *
     * Method responsible for returning a collection of Applications from the middleware.
     * @return Collection of applications.
     **/
    @SuppressWarnings("rawtypes")
    public Collection listApplications() {
        return applicationsLauncherMap.values();
    }

    /**
     *
     * Method responsible for stopping a specific application in the middleware.
     *
     * @param instanceId Name of the InstanceID of the Application do be Deployed (optional).
     * @throws ContextException
     */
    public void stopApplication(String instanceId)
            throws ContextException {
        logger.info("Stoping application with InstanceId : '" + instanceId + "'");

        UosApplication uosAL = applicationsLauncherMap.get(instanceId);
        try {
            uosAL.stop();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ApplicationDeployer.class.getName()).log(Level.SEVERE, null, ex);
        }
        applicationsLauncherMap.remove(instanceId);
    }

    /**
     *
     * Method responsible for stopping a specific application in the middleware.
     *
     * @param applicationClass Name of the Application <code>Class</code> to be instantiated.
     * @param instanceId Name of the InstanceID of the Application do be Deployed (optional).
     * @throws ContextException
     */
    public void stopApplication(Object instance, String instanceId)
            throws ContextException {
        UosApplication application;
        if (instance instanceof UosApplication) {
            logger.info("Stop Application='" + instance.getClass().getName() + "' with instanceId='" + instanceId + "'.");
            application = (UosApplication) instance;
            try {
                application.stop();
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(ApplicationDeployer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            String erroMessage = "ApplicationClass '" + instance.getClass().getName() + "' specified in " + APPLICATION_LIST_RESOURCE_KEY + " is not a UbiquitoOSApplication.";
            logger.error(erroMessage);
            throw new ContextException(erroMessage);
        }
        logger.info("Stoping application with InstanceId : '" + instanceId + "'");
        applicationsLauncherMap.remove(instanceId);
    }

    /**
     * Method responsible for stopping execution of all applications within 
     * the middleware.
     * 
     * @throws ContextException
     */
    public void stopApplications() throws ContextException {

        Set<String> keySet = applicationsLauncherMap.keySet();
        if (keySet != null && !keySet.isEmpty()) {
            for (String appKey : keySet) {
                try {
                    UosApplication uosAL = applicationsLauncherMap.get(appKey);
                    uosAL.stop();
                } catch (Exception e) {
                    throw new ContextException(e);
                }

            }
        }
    }

    public void initApplication(Object instance, String instanceId) throws ContextException {
        UosApplication application;
        application = (UosApplication) instance;
        if (instance instanceof UosApplication) {
            try {              
                /* Creates one ontology object for each init application, in order 
                 * to ensure that changes intended by the applications will not be 
                 * mixed. */
                ontology = new Ontology(resourceBundle);      
                if (!ontology.getOntologyDeployInstance().hasInstanceOf(instance.getClass().getName(), "application")) {
                    ontology.getOntologyDeployInstance().addInstanceOf(instance.getClass().getName(), "application");
                    application.init(ontology);
                    ontology.saveChanges();
                } else {
                    String erroMessage = "ApplicationClass '" + instance.getClass().getName() + " is already deployed.";
                    logger.error(erroMessage);
                }
            } catch (ReasonerNotDefinedException ex) {
                logger.info("Ontology component disabled.");
                application.init(null);
            }
        } else {
            String erroMessage = "ApplicationClass '" + instance.getClass().getName() + "' specified in " + APPLICATION_LIST_RESOURCE_KEY + " is not a UbiqtoOSApplication.";
            logger.error(erroMessage);
            throw new ContextException(erroMessage);
        }
    }

    public void tearDownApplication(Object instance, String instanceId) throws ContextException, Exception {
        UosApplication application;
        application = (UosApplication) instance;    
        if (instance instanceof UosApplication) {
            logger.info("Tear Down Application='" + instance.getClass().getName() + "' with instanceId='" + instanceId + "'.");
            try {   
                ontology = new Ontology(resourceBundle);
                ontology.getOntologyUndeployInstance().removeInstanceOf(instance.getClass().getName(), "application");
                application.tearDown(ontology);
                ontology.saveChanges();
           } catch (ReasonerNotDefinedException ex) {
                application.tearDown(null);
           }
        } else {
            String erroMessage = "ApplicationClass '" + instance.getClass().getName() + "' specified in " + APPLICATION_LIST_RESOURCE_KEY + " is not a UbiqtoOSApplication.";
            logger.error(erroMessage);
            throw new ContextException(erroMessage);
        }
    }
    static long dCount = 0;

    synchronized private long incRunningApplicationsCount() {
        return ++dCount;
    }
}

/**
 * Each application must be launched in a separated Thread. 
 * This class is responsible for launching each application instance.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
class ApplicationThreadLauncher implements Runnable {

    private UosApplication launcher;
    private Gateway gateway;
    private OntologyStart ontology;

    public ApplicationThreadLauncher(UosApplication launcher, Gateway gateway, OntologyStart ontology) {
        this.launcher = launcher;
        this.gateway = gateway;
        this.ontology = ontology;
    }

    @Override
    public void run() {
        launcher.start(gateway, ontology);
    }
}
