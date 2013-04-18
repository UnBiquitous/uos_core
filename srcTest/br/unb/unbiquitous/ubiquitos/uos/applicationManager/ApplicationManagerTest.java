package br.unb.unbiquitous.ubiquitos.uos.applicationManager;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.application.UosApplication;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyDeploy;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyStart;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api.OntologyUndeploy;

public class ApplicationManagerTest {

	private ApplicationManager manager;
	private Gateway gateway;

	@Before public void setUp() throws Exception{
		gateway = mock(Gateway.class);
		new File("resources/owl/uoscontext.owl").createNewFile();
		ResourceBundle bundle = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
						{"ubiquitos.ontology.path","resources/owl/uoscontext.owl"},
						{"ubiquitos.ontology.reasonerFactory","br.unb.unbiquitous.ubiquitos.ontology.OntologyReasonerTest"},
				};
			}
		};
		manager = new ApplicationManager(bundle,gateway);
	}
	
	private void createOntologyDisabledManager() {
		ResourceBundle bundle = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
						{"ubiquitos.ontology.path","resources/owl/uoscontext.owl"},
				};
			}
		};
		manager = new ApplicationManager(bundle,gateway);
	}
	
	@After public void tearDown(){
		new File("resources/owl/uoscontext.owl").delete();
	}
	
	@Test public void addingAnApplicationDoesNothingToIt(){
		DummyApp app = new DummyApp();
		manager.add(app);
		assertThat(app.inited).isFalse();
		assertThat(app.started).isFalse();
		assertThat(app.stoped).isFalse();
		assertThat(app.finished).isFalse();
	}
	
	@Test public void startInitsAndStartsAllApplications() throws InterruptedException{
		final DummyApp app1 = new DummyApp();
		final DummyApp app2 = new DummyApp();
		manager.add(app1);
		manager.add(app2);
		manager.startApplications();
		waitToStart(app1);
		waitToStart(app2);
		assertThat(app1.inited).isTrue();
		assertThat(app1.started).isTrue();
		assertThat(app1.stoped).isFalse();
		assertThat(app1.finished).isFalse();
		assertThat(app2.inited).isTrue();
		assertThat(app2.started).isTrue();
		assertThat(app2.stoped).isFalse();
		assertThat(app2.finished).isFalse();
	}
	
	@Test public void startInitsAndStartsAllApplicationsOnlyOnce() throws InterruptedException{
		final DummyApp app = new DummyApp();
		manager.add(app);
		manager.startApplications();
		manager.startApplications();
		waitToStart(app);
		assertThat(app.initedCount).isEqualTo(1);
		assertThat(app.startedCount).isEqualTo(1);
	}
	
	@Test public void initsTheAppWithTheProperOntology(){
		DummyApp app = new DummyApp();
		manager.add(app);
		manager.startApplications();
		assertThat(app.initOntology).isNotNull();
	}
	
	@Test public void initsTheAppWithNoOntologyWhenDisabled(){
		createOntologyDisabledManager();
		DummyApp app = new DummyApp();
		manager.add(app);
		manager.startApplications();
		assertThat(app.initOntology).isNull();
	}

	@Test public void startsTheAppWithTheProperOntologyAndGateway() throws InterruptedException{
		final DummyApp app = new DummyApp();
		manager.add(app);
		manager.startApplications();
		waitToStart(app);
		assertThat(app.startOntology).isNotNull();
		assertThat(app.gateway).isSameAs(gateway);
	}
	
	@Test public void startsTheAppWithNoOntologyWhenDisableButStillWithGateway() throws InterruptedException{
		createOntologyDisabledManager();
		final DummyApp app = new DummyApp();
		manager.add(app);
		manager.startApplications();
		waitToStart(app);
		assertThat(app.startOntology).isNull();
		assertThat(app.gateway).isSameAs(gateway);
	}
	
	@Test public void startsTheAppInADifferentThread() throws InterruptedException{
		final DummyApp app = new DummyApp(){
			@Override
			public void start(Gateway gateway, OntologyStart ontology) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
				super.start(gateway, ontology);
			}
		};
		manager.add(app);
		manager.startApplications();
		Thread.sleep(50);
		assertThat(app.started).isFalse();
		waitToStart(app);
	}
	
	@Test public void deployDoesInitsAndStarts() throws InterruptedException{
		final DummyApp app = new DummyApp();
		manager.deploy(app);
		waitToStart(app);
		assertThat(app.inited).isTrue();
		assertThat(app.started).isTrue();
		assertThat(app.stoped).isFalse();
		assertThat(app.finished).isFalse();
	}
	
	@Test public void tearDownStopsAndTearDownAllApplications() throws Exception{
		final DummyApp app1 = new DummyApp();
		final DummyApp app2 = new DummyApp();
		manager.add(app1);
		manager.add(app2);
		manager.startApplications();
		manager.tearDown();
		assertThat(app1.stoped).isTrue();
		assertThat(app1.finished).isTrue();
		assertThat(app2.stoped).isTrue();
		assertThat(app2.finished).isTrue();
	}
	
	@Test public void tearDownStopsAndTearDownApplicationstwice() throws Exception{
		final DummyApp app = new DummyApp();
		manager.add(app);
		manager.startApplications();
		manager.tearDown();
		manager.tearDown();
		assertThat(app.stopedCount).isEqualTo(1);
		assertThat(app.finishedCount).isEqualTo(1);
	}
	
	@Test public void tearsDownTheAppWithTheProperOntology() throws Exception{
		final DummyApp app = new DummyApp();
		manager.add(app);
		manager.startApplications();
		manager.tearDown();
		assertThat(app.teardownOntology).isNotNull();
	}
	
	@Test public void tearsDownTheAppWithNoOntologyWhenDisable() throws Exception{
		createOntologyDisabledManager();
		final DummyApp app = new DummyApp();
		manager.add(app);
		manager.tearDown();
		assertThat(app.teardownOntology).isNull();
	}
	
	@Test public void findDeployedApps() throws Exception{
		DummyApp app1 = new DummyApp();
		manager.deploy(app1,"app1");
		DummyApp app2 = new DummyApp();
		manager.deploy(app2,"app2");
		assertThat(manager.findApplication("app1")).isSameAs(app1);
		assertThat(manager.findApplication("app2")).isSameAs(app2);
	}
	
	@Test public void findAddedApps() throws Exception{
		DummyApp app1 = new DummyApp();
		manager.add(app1,"app1");
		DummyApp app2 = new DummyApp();
		manager.add(app2,"app2");
		manager.startApplications();
		assertThat(manager.findApplication("app1")).isSameAs(app1);
		assertThat(manager.findApplication("app2")).isSameAs(app2);
	}
	
	@Test public void deployingAppsAutoAsingIds() throws Exception{
		DummyApp app = new DummyApp();
		manager.deploy(app);
		assertThat(manager.findApplication(DummyApp.class.getName()+"0"))
															.isSameAs(app);
	}
	
	@Test public void addingAppsAutoAsingIds() throws Exception{
		DummyApp app = new DummyApp();
		manager.add(app);
		manager.startApplications();
		assertThat(manager.findApplication(DummyApp.class.getName()+"0"))
															.isSameAs(app);
	}
	
	private void waitToStart(final DummyApp app) throws InterruptedException {
		assertEventuallyTrue("Must start a Thread with the app", 1000, 
				new EventuallyAssert() {
					public boolean assertion() {
						return app.started;
					}
				});
	}
	
	static interface EventuallyAssert{
		boolean assertion();
	}
	
	private void assertEventuallyTrue(String msg, long wait, EventuallyAssert assertion) throws InterruptedException{
		long time = 0;
		while (time <= wait && !assertion.assertion()){
			Thread.sleep(10);
			time += 10;
		}
		assertTrue(msg,assertion.assertion());
	}
	
	
	
}

class DummyApp implements UosApplication{
	
	boolean inited;
	int	initedCount;
	boolean started;
	int	startedCount;
	boolean stoped;
	int	stopedCount;
	boolean finished;
	int	finishedCount;
	OntologyDeploy initOntology;
	OntologyStart startOntology;
	OntologyUndeploy teardownOntology;
	Gateway gateway;
	
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
