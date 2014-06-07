package org.unbiquitous.uos.core;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;
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

    private InitialProperties properties;

	private UOSComponentFactory factory;
	private List<UOSComponent> components ;
	
	public static void main(String[] args) throws Exception{
		UOS uos = new UOS();
		if (hasSpecificPropertiesFile(args)){
			uos.start(args[1]);
		}else if(hasDefaultFile(args)){
			uos.start(DEFAULT_UBIQUIT_BUNDLE_FILE+".properties");
		}else{
			String helpMessage = String.format(
				"Create a %s.properties file on the root folder or use '-f' "
			  + "to point to another path.", DEFAULT_UBIQUIT_BUNDLE_FILE);
			System.out.println(helpMessage);
			
		}
	}

	private static boolean hasDefaultFile(String[] args) {
		return args.length == 0 && new File(DEFAULT_UBIQUIT_BUNDLE_FILE+".properties").exists();
	}

	private static boolean hasSpecificPropertiesFile(String[] args) {
		return args.length == 2 && args[0].equals("-f");
	}
	
	/**
	 * Initializes the components of the uOS middleware using 'ubiquitos' as the
	 * name of the resouce bundle to be used.
	 * 
	 * @throws ContextException
	 */
	public void start() throws ContextException {
		start(DEFAULT_UBIQUIT_BUNDLE_FILE);
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
	public void start(String resourceBundleName) throws ContextException {
		logger.fine("Retrieving Resource Bundle Information");
		File possibleResourceFile = new File(resourceBundleName);
		if(possibleResourceFile.exists()){
			try {
				start(new PropertyResourceBundle(new FileInputStream(possibleResourceFile)));
			} catch (IOException e) {
				throw new ContextException(e);
			}
		}else{
			start(ResourceBundle.getBundle(resourceBundleName));
		}
		
	}
	
	public void start(ResourceBundle resourceBundle) throws ContextException {
		start(new InitialProperties(resourceBundle));
	}
		
	@SuppressWarnings("serial")
	public void start(InitialProperties properties) throws ContextException {
		try {
			this.properties	= properties;
			this.properties.markReadOnly();
			this.factory		= new UOSComponentFactory(properties);
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
	public void stop() {
		logger.info("Stopping UOS");
		for(UOSComponent component:components){
			logger.finer("Stopping "+component.getClass().getSimpleName());
			component.stop();
		}
		logger.fine("Stopped UOS");
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
