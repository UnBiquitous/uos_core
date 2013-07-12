package org.unbiquitous.uos.core;


import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.adaptabitilyEngine.AdaptabilityEngine;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.connectivity.ConnectivityInitializer;
import org.unbiquitous.uos.core.driverManager.DriverManagerException;
import org.unbiquitous.uos.core.messageEngine.MessageEngine;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
import org.unbiquitous.uos.core.ontologyEngine.OntologyInitializer;

/**
 * 
 * This class centralizes the process of initialization e teardown of the
 * middleware and it's dependencies.
 * 
 * @author Fabricio Nogueira Buzeto
 * 
 */
public class UOS {

	private static final Logger logger = UOSLogging.getLogger();

	private static String DEFAULT_UBIQUIT_BUNDLE_FILE = "ubiquitos";

    private ResourceBundle properties;

	private UOSComponentFactory factory;
	private List<UOSComponent> components ;
	
	public static void main(String[] args) throws Exception{
		new UOS().init();
	}
	
	/**
	 * Initializes the components of the uOS middleware using 'ubiquitos' as the
	 * name of the resouce bundle to be used.
	 * 
	 * @throws ContextException
	 */
	public void init() throws ContextException {
		init(DEFAULT_UBIQUIT_BUNDLE_FILE);
	}

	/**
	 * Initializes the components of the uOS middleware acording to the
	 * resourceBundle informed.
	 * 
	 * @param resourceBundleName
	 *            Name of the <code>ResourceBundle</code> to be used for finding
	 *            the properties of the uOS middleware.
	 * @throws ContextException
	 */
	public void init(String resourceBundleName) throws ContextException {
		logger.fine("Retrieving Resource Bundle Information");
		init(ResourceBundle.getBundle(resourceBundleName));
	}
	
	/**
	 * Initializes the components of the uOS middleware acording to the
	 * resourceBundle informed.
	 * 
	 * @param resourceBundleName
	 *            Name of the <code>ResourceBundle</code> to be used for finding
	 *            the properties of the uOS middleware.
	 * @throws ContextException
	 */
	@SuppressWarnings("serial")
	public void init(ResourceBundle resourceBundle) throws ContextException {
		
		try {
			this.properties	= resourceBundle;
			this.factory		= new UOSComponentFactory(resourceBundle);
			this.components = new ArrayList<UOSComponent>(){
				{
					add(factory.get(ConnectionManagerControlCenter.class));
					add(factory.get(CurrentDeviceInitializer.class));
					add(factory.get(MessageEngine.class));
					add(factory.get(AdaptabilityEngine.class));
					add(factory.get(OntologyInitializer.class));
					add(factory.get(ConnectivityInitializer.class));
				}
			};
			
			logger.info("..::|| Starting uOS ||::..");
			
			createComponents();
			initComponents();
			startComponents();

		} catch (DriverManagerException e) {
			logger.log(Level.SEVERE,"Not possible to init uOS", e);
			throw new ContextException(e);
		} catch (Exception e) {
			throw new ContextException(e);
		} 
	}

	private void createComponents() {
		for(UOSComponent component:components){
			component.create(properties);
		}
	}
	
	private void initComponents() {
		for(UOSComponent component:components){
			component.init(factory);
		}
	}
	
	private void startComponents() {
		for(UOSComponent component:components){
			component.start();
		}
	}

	/**
	 * Shutdown the middleware infrastructure.
	 */
	public void tearDown() {
		for(UOSComponent component:components){
			component.stop();
		}
	}

	/**
	 * @return The Gateway used by Drivers and Applications to interact with the
	 *         Smart Space
	 */
	public Gateway getGateway() {
		return factory.gateway();
	}

	public UOSComponentFactory getFactory() {
		return factory;
	}
	
}
