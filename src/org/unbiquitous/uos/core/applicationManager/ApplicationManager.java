package org.unbiquitous.uos.core.applicationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.driverManager.ReflectionServiceCaller;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Call.ServiceType;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;
import org.unbiquitous.uos.core.ontologyEngine.Ontology;
import org.unbiquitous.uos.core.ontologyEngine.exception.ReasonerNotDefinedException;

public class ApplicationManager {
	private static final Logger logger = UOSLogging.getLogger();

	private Map<String, UosApplication> toInitialize = new HashMap<String, UosApplication>();
	private Map<String, UosApplication> deployed = new HashMap<String, UosApplication>();
	private final InitialProperties properties;

	private final Gateway gateway;
	private ConnectionManagerControlCenter connectionManagerControlCenter;

	public ApplicationManager(InitialProperties properties, Gateway gateway,
			ConnectionManagerControlCenter connectionManagerControlCenter) {
		this.properties = properties;
		this.gateway = gateway;
		this.connectionManagerControlCenter = connectionManagerControlCenter;
	}

	public void add(UosApplication app) {
		add(app, assignAName(app, null));
	}

	public void add(UosApplication app, String id) {
		toInitialize.put(id, app);
	}

	public void startApplications() {
		for (Entry<String, UosApplication> e : toInitialize.entrySet()) {
			// TODO: The ontology rules are not enforced by tests
			deploy(e.getValue(), e.getKey());
		}
		toInitialize.clear();
	}

	public void deploy(UosApplication app) {
		deploy(app, null);
	}

	public void deploy(UosApplication app, String id) {
		id = assignAName(app, id);
		initApp(app, id);
		startApp(app);
		deployed.put(id, app);
	}

	private String assignAName(UosApplication app, String id) {
		int appCount = appCount();
		if (id == null) {
			id = app.getClass().getName() + appCount;
		}
		return id;
	}

	private int _appCount = 0;

	private synchronized int appCount() {
		return _appCount++;
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
		} catch (Throwable ex) {
			logger.fine(ex.getMessage());
			logger.info("Ontology component disabled.");
			return null;
		}
	}

	private void initApp(final UosApplication app, String id) {
		Ontology initOntology = createInitOntology(app);
		app.init(initOntology, properties, id);
		if (initOntology != null) {
			initOntology.saveChanges();
		}
	}

	private Ontology createInitOntology(UosApplication app) {
		try {
			Ontology ontology = new Ontology(properties);
			if (ontology.getOntologyReasoner() == null) {
				return null;
			}
			if (!ontology.getOntologyDeployInstance().hasInstanceOf(
					app.getClass().getName(), "application")) {
				ontology.getOntologyDeployInstance().addInstanceOf(
						app.getClass().getName(), "application");
				return ontology;
			} else {
				logger.severe("ApplicationClass '" + app.getClass().getName()
						+ " is already deployed.");
			}
		} catch (Throwable e) {
			logger.fine(e.getMessage());
			logger.info("Ontology component disabled.");
		}
		return null;
	}

	public void tearDown() throws Exception {
		for (final UosApplication app : deployed.values()) {
			// TODO: disabling ontology during tear down.
			// some kind of concurrency over the ontology database file
			// is going on.
			// Ontology ontology = createUndeployOntology(app);
			app.stop();
			app.tearDown(null);
			// app.tearDown(ontology);
			// if (ontology != null){
			// ontology.saveChanges();
			// }
		}
		deployed.clear();
	}

	private Ontology createUndeployOntology(final UosApplication app) {
		Ontology ontology;
		try {
			ontology = new Ontology(properties);
			if (ontology.getOntologyReasoner() == null) {
				return null;
			}
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

	public Response handleServiceCall(Call serviceCall,
			CallContext messageContext) {
		ReflectionServiceCaller caller = new ReflectionServiceCaller(null);
		UosApplication app = findApplication(serviceCall.getInstanceId());
		// TODO: [Fabs] This is duplicated with ReflectionServiceCaller
		// TODO: Untested code
		if (serviceCall.getServiceType().equals(ServiceType.STREAM)) {
			logger.fine(String
					.format("Stream call requires to establish passive channels"));
			try {
				NetworkDevice networkDevice = messageContext
						.getCallerNetworkDevice();

				String host = connectionManagerControlCenter
						.getHost(networkDevice.getNetworkDeviceName());
				for (int i = 0; i < serviceCall.getChannels(); i++) {
					// TODO: this is TCP oriented
					String hostAddress = host + ":"
							+ serviceCall.getChannelIDs()[i];

					ClientConnection con = null;
					int waitTime = 100;
					while (con == null && waitTime < 500) {
						con = connectionManagerControlCenter
								.openActiveConnection(hostAddress,
										serviceCall.getChannelType());
						try {
							Thread.sleep(waitTime *= 2);
						} catch (Exception e) {
						}
					}
					if (con == null) {
						String msg = String
								.format("Not possible to open passive channel with %s.",
										hostAddress);
						logger.severe(msg);
						throw new RuntimeException(msg);
					} else {
						logger.fine(String.format("Opened connection with %s",
								hostAddress));
					}
					messageContext.addDataStreams(con.getDataInputStream(),
							con.getDataOutputStream());
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return caller.callServiceOnApp(app, serviceCall, messageContext);
	}

}
