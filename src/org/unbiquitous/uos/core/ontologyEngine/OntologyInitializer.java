package org.unbiquitous.uos.core.ontologyEngine;

import java.util.ResourceBundle;

import org.unbiquitous.uos.core.UOSComponent;
import org.unbiquitous.uos.core.UOSComponentFactory;
import org.unbiquitous.uos.core.ontologyEngine.exception.ReasonerNotDefinedException;

public class OntologyInitializer implements UOSComponent{

	private ResourceBundle properties;

	@Override
	public void create(ResourceBundle properties) {
		this.properties = properties;
	}

	@Override
	public void init(UOSComponentFactory factory) {
		try {
			if (properties.containsKey("ubiquitos.ontology.path")){
				Ontology ontology = new Ontology(properties);
				ontology.initializeOntology();
			}
		} catch (ReasonerNotDefinedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void start() {}

	@Override
	public void stop() {}

	
}
