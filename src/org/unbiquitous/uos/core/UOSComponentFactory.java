package org.unbiquitous.uos.core;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;

public class UOSComponentFactory {
	private Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();
	private ResourceBundle properties;
	
	public UOSComponentFactory(ResourceBundle properties) {
		this.properties = properties;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T> T get(Class<T> clazz){
		if (!instances.containsKey(clazz)){
			try {
				try{
					Constructor<T> propBuilder = clazz.getConstructor(ResourceBundle.class);
					instances.put(clazz, propBuilder.newInstance(properties));
				}catch (NoSuchMethodException e) {
					instances.put(clazz, clazz.newInstance());
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return (T) instances.get(clazz);
	}
	
	public <T> void set(Class<T> clazz, T object){
		instances.put(clazz, object);
	}
	
	/*********/
	
	private UpDevice currentDevice;
	
	public UpDevice currentDevice(){
		return currentDevice;
	}
	
	public UpDevice currentDevice(UpDevice currentDevice){
		return this.currentDevice = currentDevice;
	}
	
	private SmartSpaceGateway gateway;
	
	public SmartSpaceGateway gateway(){
		return gateway;
	}
	
	public SmartSpaceGateway gateway(SmartSpaceGateway gateway){
		return this.gateway = gateway;
	}
}
