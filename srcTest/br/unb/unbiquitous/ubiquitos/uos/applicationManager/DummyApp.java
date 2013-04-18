package br.unb.unbiquitous.ubiquitos.uos.applicationManager;

import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.application.UosApplication;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyDeploy;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyStart;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyUndeploy;

public class DummyApp implements UosApplication{
	
	public boolean inited;
	public int	initedCount;
	public boolean started;
	public int	startedCount;
	public boolean stoped;
	public int	stopedCount;
	public boolean finished;
	public int	finishedCount;
	public OntologyDeploy initOntology;
	public OntologyStart startOntology;
	public OntologyUndeploy teardownOntology;
	public Gateway gateway;
	
	public static DummyApp lastInstance;
	
	public DummyApp() {
		lastInstance = this;
	}
	
	@Override
	public void init(OntologyDeploy ontology) {
		this.initOntology = ontology;
		inited = true;
		initedCount ++;
	}
	
	public void start(Gateway gateway, OntologyStart ontology) {
		this.gateway = gateway;
		this.startOntology = ontology;
		started= true;
		startedCount++;
	}

	@Override
	public void stop() throws Exception {
		stoped = true;
		stopedCount++;
	}

	@Override
	public void tearDown(OntologyUndeploy ontology) throws Exception {
		this.teardownOntology = ontology;
		finished = true;
		finishedCount++;
	}
	
}