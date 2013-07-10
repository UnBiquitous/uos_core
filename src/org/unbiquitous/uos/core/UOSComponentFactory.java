package org.unbiquitous.uos.core;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class UOSComponentFactory {
	private Map<Class, Object> instances = new HashMap<Class, Object>();
	private ResourceBundle properties;
	
	public UOSComponentFactory(ResourceBundle properties) {
		this.properties = properties;
	}
	
	public <T> T get(Class<T> clazz){
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
}
