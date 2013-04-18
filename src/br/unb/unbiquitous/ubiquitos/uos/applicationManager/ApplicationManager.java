package br.unb.unbiquitous.ubiquitos.uos.applicationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.application.UosApplication;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.Ontology;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.exception.ReasonerNotDefinedException;

public class ApplicationManager {
	private static final Logger logger = Logger.getLogger(ApplicationManager.class); 
	
	private List<UosApplication> toInitialize = new ArrayList<UosApplication>();
	private Map<String, UosApplication> deployed = new HashMap<String, UosApplication>();
	private final ResourceBundle properties;

	private final Gateway gateway;

	public ApplicationManager(ResourceBundle properties, Gateway gateway) {
		this.properties = properties;
		this.gateway = gateway;
	}

	public void add(UosApplication app) {
		this.toInitialize.add(app);
	}

	public void startApplications() {
		for (final UosApplication app : toInitialize) {
			//TODO: The ontology rules are not enforced by tests
			deploy(app);
		}
		toInitialize.clear();
	}

	public void deploy(UosApplication app) {
		deploy(app,null);
	}
	
	public void deploy(UosApplication app, String id) {
		initApp(app);
		startApp(app);
		deployed.put(id, app);
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
		for (final UosApplication app : toInitialize) {
			Ontology ontology = createUndeployOntology(app);
			app.stop();
			app.tearDown(ontology);
			if (ontology != null){
				ontology.saveChanges();
			}
		}
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
}
