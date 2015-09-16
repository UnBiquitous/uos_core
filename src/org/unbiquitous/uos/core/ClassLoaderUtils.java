package org.unbiquitous.uos.core;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

public class ClassLoaderUtils {

	public static ClassLoaderBuilder builder = new DefaultClassLoaderBuilder();

	public abstract static class ClassLoaderBuilder {
		public abstract ClassLoader createClassLoader(String path) throws Exception;

		public abstract ClassLoader getParentClassLoader();
	}

	public static class DefaultClassLoaderBuilder extends ClassLoaderBuilder {
		@Override
		public ClassLoader createClassLoader(String path) throws Exception {
			return new URLClassLoader(new URL[] { new File(path).toURI().toURL() }, getParentClassLoader());
		}

		@Override
		public ClassLoader getParentClassLoader() {
			return ClassLoader.getSystemClassLoader();
		}
	}

	@SuppressWarnings("rawtypes")
	public static boolean compare(Object a, Object b) {
		if (a == b)
			return true;
		if ((a != null) && (b != null)) {
			if (a.getClass().isArray() && b.getClass().isArray())
				return compare((Object[]) a, (Object[]) b);
			if ((a instanceof Collection) && (b instanceof Collection))
				return compare((Collection) a, (Collection) b);
			return a.equals(b);
		}
		return false;
	}

	private static boolean compare(Object[] a, Object[] b) {
		int len = a.length;
		if (b.length == len) {
			int i, j;
			for (i = 0; i < len; ++i) {
				for (j = 0; (j < len) && !compare(a[i], b[j]); ++j)
					;
				if (j == len)
					return false;
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private static boolean compare(Collection a, Collection b) {
		if (a.size() == b.size()) {
			for (Object e1 : a) {
				boolean found = false;
				for (Object e2 : b)
					if (compare(e1, e2)) {
						found = true;
						break;
					}
				if (!found)
					return false;
			}
			return true;
		}
		return false;
	}

	public static int chainHashCode(int hash, Object obj) {
		if (obj != null)
			hash ^= obj.hashCode();
		return hash;
	}

	public static <T> int chainHashCode(int hash, Collection<T> c) {
		if (c != null)
			for (T e : c)
				hash = chainHashCode(hash, e);
		return hash;
	}
}
