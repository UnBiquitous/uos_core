package br.unb.unbiquitous.ubiquitos.uos.applicationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.application.UOSMessageContext;
import br.unb.unbiquitous.ubiquitos.uos.application.UosApplication;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.ReflectionServiceCaller;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.Ontology;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.exception.ReasonerNotDefinedException;

public class ApplicationManager {
	private static final Logger logger = Logger.getLogger(ApplicationManager.class); 
	
	private Map<String, UosApplication> toInitialize = new HashMap<String, UosApplication>();
	private Map<String, UosApplication> deployed = new HashMap<String, UosApplication>();
	private final ResourceBundle properties;

	private final Gateway gateway;

	public ApplicationManager(ResourceBundle properties, Gateway gateway) {
		this.properties = properties;
		this.gateway = gateway;
	}

	public void add(UosApplication app) {
		add(app, assignAName(app, null));
	}
	
	public void add(UosApplication app, String id) {
		toInitialize.put(id, app);
	}

	public void startApplications() {
		for (Entry<String, UosApplication> e : toInitialize.entrySet()) {
			//TODO: The ontology rules are not enforced by tests
			deploy(e.getValue(),e.getKey());
		}
		toInitialize.clear();
	}

	public void deploy(UosApplication app) {
		deploy(app,null);
	}
	
	public void deploy(UosApplication app, String id) {
		id = assignAName(app, id);
		initApp(app);
		startApp(app);
		deployed.put(id, app);
	}

	private String assignAName(UosApplication app, String id) {
		int appCount = appCount();
		if (id == null){
			id = app.getClass().getName()+appCount;
		}
		return id;
	}
	
	private int _appCount = 0;
	private synchronized int appCount(){
		return _appCount ++;
	}
	
	private void startApp(final UosApplication app) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				app.start(gateway, createStartOntology());
			}
		});
		t.start();
	}

	private Ontology createStartOntology() {
		try {
			return new Ontology(properties);
		} catch (ReasonerNotDefinedException ex) {
			logger.info("Ontology component disabled.");
			return null;
		}
	}
	
	private void initApp(final UosApplication app) {
		Ontology initOntology = createInitOntology(app);
		app.init(initOntology);
		if (initOntology != null){
			initOntology.saveChanges();
		}
	}

	private Ontology createInitOntology(UosApplication app) {
		try {
			Ontology ontology = new Ontology(properties);
			if (!ontology.getOntologyDeployInstance().hasInstanceOf(app.getClass().getName(), "application")) {
		        ontology.getOntologyDeployInstance().addInstanceOf(app.getClass().getName(), "application");
		        return ontology;
		    } else {
		        logger.error("ApplicationClass '" + app.getClass().getName() + " is already deployed.");
		    }
		} catch (ReasonerNotDefinedException e) {
			 logger.info("Ontology component disabled.");
		}
		return null;
	}
	
	public void tearDown() throws Exception {
		for (final UosApplication app : deployed.values()) {
			Ontology ontology = createUndeployOntology(app);
			app.stop();
			app.tearDown(ontology);
			if (ontology != null){
				ontology.saveChanges();
			}
		}
		deployed.clear();
	}

	private Ontology createUndeployOntology(final UosApplication app) {
		Ontology ontology;
		try {
			ontology = new Ontology(properties);
			ontology.getOntologyUndeployInstance().removeInstanceOf(
					app.getClass().getName(), "application");
		} catch (ReasonerNotDefinedException ex) {
			ontology = null;
		}
		return ontology;
	}

	public UosApplication findApplication(String id) {
		return deployed.get(id);
	}

	public ServiceResponse handleServiceCall(ServiceCall serviceCall,
			UOSMessageContext messageContext) {
		ReflectionServiceCaller caller = new ReflectionServiceCaller(null);
		return caller.callServiceOnApp(findApplication(serviceCall.getInstanceId()),serviceCall);
	}

}
