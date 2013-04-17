package br.unb.unbiquitous.ubiquitos;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

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
	
}
