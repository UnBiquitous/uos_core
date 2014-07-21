package org.unbiquitous.uos.core.connectivity;

import java.util.MissingResourceException;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSComponent;
import org.unbiquitous.uos.core.UOSComponentFactory;
import org.unbiquitous.uos.core.UOSLogging;

public class ConnectivityInitializer implements UOSComponent {
	private static Logger logger = UOSLogging.getLogger();
	
	private InitialProperties properties;

	@Override
	public void create(InitialProperties properties) {
		this.properties = properties;}

	@Override
	public void init(UOSComponentFactory factory) {
		//Read proxying attribute from the resource bundle
		boolean doProxying = false;

		try {
			if (hasDoProxyingProp()) {
				doProxying = true;
			}
		} catch (MissingResourceException e) {
			logger.info("No proxying attribute found in the properties. Proxying set as false.");
		}

		factory.get(ConnectivityManager.class).init(factory.gateway(), doProxying);
	}

	private boolean hasDoProxyingProp() {
		String doProxyingPropKey = "ubiquitos.connectivity.doProxying";
		return properties.containsKey(doProxyingPropKey) && 
				(properties.getString("ubiquitos.connectivity.doProxying")).equalsIgnoreCase("yes");
	}

	@Override
	public void start() {}

	@Override
	public void stop() {}

}
