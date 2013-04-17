package br.unb.unbiquitous.ubiquitos.uos.applicationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import br.unb.unbiquitous.ubiquitos.uos.application.UosApplication;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.Ontology;
import br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.exception.ReasonerNotDefinedException;

public class ApplicationManager {

	private List<UosApplication> apps = new ArrayList<UosApplication>();
	private final ResourceBundle properties;

	public ApplicationManager(ResourceBundle properties) {
		this.properties = properties;
	}

	public void add(UosApplication app) {
		this.apps.add(app);
	}

	public void startApplications() {
		for (UosApplication app : apps) {
			// Deve criar uma ontologia para cada um usando o properties
			Ontology ontology;
			try {
				ontology = new Ontology(properties);
				//TODO: mais regras
				app.init(ontology);
				app.start(null, null);
			} catch (ReasonerNotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
