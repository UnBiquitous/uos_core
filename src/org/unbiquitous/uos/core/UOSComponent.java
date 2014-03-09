package org.unbiquitous.uos.core;

public interface UOSComponent {
	
	void create(InitialProperties properties);
	
	void init(UOSComponentFactory factory);
	
	void start();
	
	void stop();
}
