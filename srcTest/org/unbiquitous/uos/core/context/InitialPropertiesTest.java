package org.unbiquitous.uos.core.context;

import static org.fest.assertions.api.Assertions.*;

import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;

import org.fest.assertions.data.MapEntry;
import org.junit.Test;
import org.unbiquitous.uos.core.InitialProperties;

public class InitialPropertiesTest {

	@Test public void createAMapBasedOnABundle(){
		ResourceBundle testBundle = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] { { "a.a", "100" }, 
						{ "b.b", "30" },
						{ "c.c", 1 }, };
			}
		};
		InitialProperties props = new InitialProperties(testBundle);
		assertThat(props)
			.hasSize(3)
			.contains(MapEntry.entry("a.a", "100"))
			.contains(MapEntry.entry("b.b", "30"))
			.contains(MapEntry.entry("c.c", 1))
			;
	}
	
	@SuppressWarnings("serial")
	@Test public void createAMapBasedOnAnotherMap(){
		Map<String, Object> testMap = new HashMap<String, Object> (){{
			put("b.b","foobar");
			put("c",123);
		}};
			
		InitialProperties props = new InitialProperties(testMap);
		assertThat(props)
			.hasSize(2)
			.contains(MapEntry.entry("b.b", "foobar"))
			.contains(MapEntry.entry("c", 123))
			;
	}
	
	@SuppressWarnings("serial")
	@Test public void canChangeMapProperties(){
		Map<String, Object> testMap = new HashMap<String, Object> (){{
			put("a","abacate");
		}};
			
		InitialProperties props = new InitialProperties(testMap);
		assertThat(props.get("a")).isEqualTo("abacate");
		props.put("a", "apple");
		assertThat(props.get("a")).isEqualTo("apple");
	}
	
	@Test public void dontFailafterMarkedAsReadOnly(){
		InitialProperties props = new InitialProperties();
		props.markReadOnly();
	}
	
	@SuppressWarnings("serial")
	@Test(expected=IllegalAccessError.class) 
	public void cantChangeMapPropertiesAfterMarkedAsReadOnly(){
		Map<String, Object> testMap = new HashMap<String, Object> (){{
			put("a","abacate");
		}};
			
		InitialProperties props = new InitialProperties(testMap);
		props.markReadOnly();
		props.put("a", "apple");
	}
	
}

