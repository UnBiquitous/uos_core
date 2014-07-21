package org.unbiquitous.uos.core;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UOSTest_Main {
	private ByteArrayOutputStream out;
	File testProperties = new File("resourcesTest/simple_ubiquitos.properties");
	File defaultProperties = new File("ubiquitos.properties");
	
	@Before public void setup(){
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
	}
	
	@After public void teardown(){
		if(defaultProperties.exists()){
			defaultProperties.delete();
		}
	}

	@Test public void mainShowsHelpMessageWhenDefaultFileIsNotFound() throws Exception{
		UOS.main(new String[]{});
		assertThat(out.toString()).contains("Create a");
	}
	
	@Test public void mainShowsHelpMessageWhenPathOptionIsNotProperlyUsed() throws Exception{
		UOS.main(new String[]{"-f"});
		assertThat(out.toString()).contains("Create a");
	}
	
	@Test public void mainShowsHelpMessageWhenPathOptionHasMoreThanOneParameter() throws Exception{
		UOS.main(new String[]{"-f","file","not an option"});
		assertThat(out.toString()).contains("Create a");
	}
	
	@Test public void mainShowsHelpMessageWhenUnkownOptionIsInformed() throws Exception{
		UOS.main(new String[]{"-n","what"});
		assertThat(out.toString()).contains("Create a");
	}
	
	@Test public void mainStartsMiddlewareWithDefaultFile() throws Exception{
		FileUtils.copyFile(testProperties, defaultProperties);
		UOS.main(new String[]{});
		assertThat(out.toString()).isEmpty();;
	}
	
	@Test public void mainStartsMiddlewareInfomedFile() throws Exception{
		defaultProperties = new File("my_strange_file.properties");
		FileUtils.copyFile(testProperties, defaultProperties);
		UOS.main(new String[]{"-f",defaultProperties.getPath()});
		assertThat(out.toString()).isEmpty();;
	}
	
}
