package br.unb.unbiquitous.ubiquitos.uos.applicationManager;

import static org.fest.assertions.api.Assertions.*;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Test;

import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.application.UosApplication;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyDeploy;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyStart;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyUndeploy;

public class ApplicationManagerTest {

	private ApplicationManager manager;

	@Before public void setUp(){
		ResourceBundle bundle = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
						{"ubiquitos.ontology.path","resources/owl/uoscontext.owl"},
						{"ubiquitos.ontology.reasonerFactory","br.unb.unbiquitous.ubiquitos.ontology.OntologyReasonerTest"},
				};
			}
		};
		manager = new ApplicationManager(bundle);
	}
	
	@Test public void addingAnApplicationDoesNothingToIt(){
		DummyApp app = new DummyApp();
		manager.add(app);
		assertThat(app.inited).isFalse();
		assertThat(app.started).isFalse();
		assertThat(app.stoped).isFalse();
		assertThat(app.finished).isFalse();
	}
	
	@Test public void startInitsAndStartsAllApplications(){
		DummyApp app1 = new DummyApp();
		DummyApp app2 = new DummyApp();
		manager.add(app1);
		manager.add(app2);
		manager.startApplications();
		assertThat(app1.inited).isTrue();
		assertThat(app1.started).isTrue();
		assertThat(app1.stoped).isFalse();
		assertThat(app1.finished).isFalse();
		assertThat(app2.inited).isTrue();
		assertThat(app2.started).isTrue();
		assertThat(app2.stoped).isFalse();
		assertThat(app2.finished).isFalse();
	}
	
	@Test public void initsTheAppWithTheProperOntology(){
		DummyApp app = new DummyApp();
		manager.add(app);
		manager.startApplications();
		assertThat(app.initOntology).isNotNull();
	}
	
	
	//add
	//start
	//deploy
	
}

class DummyApp implements UosApplication{
	
	boolean started;
	boolean stoped;
	boolean inited;
	boolean finished;
	OntologyDeploy initOntology;
	
	@Override
	public void init(OntologyDeploy ontology) {
		this.initOntology = ontology;
		inited = true;
	}
	
	public void start(Gateway gateway, OntologyStart ontology) {
		started= true;
	}

	@Override
	public void stop() throws Exception {
		stoped = true;
	}

	@Override
	public void tearDown(OntologyUndeploy ontology) throws Exception {
		finished = true;
	}
	
}
