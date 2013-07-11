package org.unbiquitous.uos.core.ontologyEngine;

import java.util.ResourceBundle;

import org.unbiquitous.uos.core.UOSComponent;
import org.unbiquitous.uos.core.UOSComponentFactory;

public class OntologyInitializer implements UOSComponent{

	private ResourceBundle properties;

	@Override
	public void create(ResourceBundle properties) {
		this.properties = properties;
	}

	@Override
	public void init(UOSComponentFactory factory) {
		if (properties.containsKey("ubiquitos.ontology.path")){
			Ontology ontology = factory.get(Ontology.class);
			ontology.initializeOntology();
		}
	}

	@Override
	public void start() {}

	@Override
	public void stop() {}

	
}
