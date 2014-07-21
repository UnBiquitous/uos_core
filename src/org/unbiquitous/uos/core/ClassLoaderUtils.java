package org.unbiquitous.uos.core;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

public class ClassLoaderUtils {
	
	public static ClassLoaderBuilder builder = new DefaultClassLoaderBuilder();
	
	public abstract static class ClassLoaderBuilder{
		public abstract ClassLoader createClassLoader(String path) throws Exception;
		public abstract ClassLoader getParentClassLoader();
	}
	
	public static class DefaultClassLoaderBuilder extends ClassLoaderBuilder{
		@Override
		public ClassLoader createClassLoader(String path) throws Exception {
			return new URLClassLoader(
					new URL[] { new File(path).toURI().toURL() },
					getParentClassLoader());
		}

		@Override
		public ClassLoader getParentClassLoader() {
			return ClassLoader.getSystemClassLoader();
		}
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean compare(Object a, Object b){
		if(a != null && b != null && 
				a.getClass().isArray() && b.getClass().isArray()){
			List _a = Arrays.asList((Object[])a);
			List _b = Arrays.asList((Object[])b);
			return a == b || (  _a.containsAll(_b) && _b.containsAll(_a) );
		}
		return a == b || (a != null && a.equals(b));
	}
}
