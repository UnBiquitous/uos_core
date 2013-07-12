package org.unbiquitous.uos.core;

import java.util.ResourceBundle;

public interface UOSComponent {
	
	void create(ResourceBundle properties);
	
	void init(UOSComponentFactory factory);
	
	void start();
	
	void stop();
}
