/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine;

import java.io.File;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.ontologyEngine.api.DeployClass;
import org.unbiquitous.uos.core.ontologyEngine.api.DeployDataProperty;
import org.unbiquitous.uos.core.ontologyEngine.api.DeployInstance;
import org.unbiquitous.uos.core.ontologyEngine.api.DeployObjectProperty;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyDeploy;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyStart;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyUndeploy;
import org.unbiquitous.uos.core.ontologyEngine.api.StartInstance;
import org.unbiquitous.uos.core.ontologyEngine.api.StartReasoner;
import org.unbiquitous.uos.core.ontologyEngine.api.UndeployClass;
import org.unbiquitous.uos.core.ontologyEngine.api.UndeployDataProperty;
import org.unbiquitous.uos.core.ontologyEngine.api.UndeployInstance;
import org.unbiquitous.uos.core.ontologyEngine.api.UndeployObjectProperty;
import org.unbiquitous.uos.core.ontologyEngine.exception.ReasonerNotDefinedException;


/**
 *
 * @author anaozaki
 */
public class Ontology implements OntologyDeploy, OntologyUndeploy, OntologyStart {

    private static String ONTOLOGY_PATH_RESOURCE_KEY = "ubiquitos.ontology.path";
    private static String ONTOLOGY_PATH = "/tmp/local.owl";
    private OWLOntology localContext;
    private OWLOntologyManager manager;
    private OntologyChangeManager changeManager;
    private OntologyClass ontologyClass;
    private OntologyDataProperty ontologyDataProperty;
    private OntologyObjectProperty ontologyObjectProperty;
    private OntologyInstance ontologyInstance;
    private OntologyReasoner ontologyReasoner;
    private static final Logger logger = Logger.getLogger(Ontology.class);
    OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
    public static final String ANNOTATION_PROPERTY = "createdBy";
    IRI documentIRI = IRI.create("file:" + ONTOLOGY_PATH);
    File file = null;
    private final Lock lock = new ReentrantLock();
    static int protectArea = 0;

    public Ontology(ResourceBundle resourceBundle) throws ReasonerNotDefinedException {

        manager = OWLManager.createOWLOntologyManager();

        if (resourceBundle.containsKey(ONTOLOGY_PATH_RESOURCE_KEY)) {
            do {
                try {
                    if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                        protectArea++;
                        try {
                            while (protectArea > 1) {
                                protectArea--;
                                Thread.sleep(100);
                                protectArea++;
                            }
                          
                            file = new File(
                                    resourceBundle.getString(ONTOLOGY_PATH_RESOURCE_KEY));

                            localContext = manager.loadOntologyFromOntologyDocument(file);
                        } catch (OWLOntologyCreationException ex) {

                            logger.error(ex);
                        } finally {
                            protectArea--;
                            lock.unlock();

                        }
                        break;
                    }
                } catch (InterruptedException e) {
                }
            } while (true);
        }else{
        	return;
        }
        ontologyReasoner = new OntologyReasoner(manager, localContext, resourceBundle);
        changeManager = new OntologyChangeManager(manager, localContext, this);
        ontologyClass = new OntologyClass(manager, localContext, changeManager);
        ontologyDataProperty = new OntologyDataProperty(manager, localContext, changeManager);
        ontologyObjectProperty = new OntologyObjectProperty(manager, localContext, changeManager);
        ontologyInstance = new OntologyInstance(manager, localContext, changeManager, ontologyReasoner);

    }

    @Override
    public DeployClass getOntologyDeployClass() {
        return ontologyClass;
    }

    @Override
    public void saveChanges() {
        do {
            try {

               if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                   protectArea++;
                    try {
                        while (protectArea > 1) {
                            protectArea--;
                            Thread.sleep(100);
                            protectArea++;
                        }

                        if (file == null) {
                            manager.saveOntology(localContext, owlxmlFormat, documentIRI);
                        } else {
                            manager.saveOntology(localContext);
                        }

                    } catch (OWLOntologyStorageException ex) {
                        logger.error(ex);
                    } finally {
                  
                        protectArea--;
                        lock.unlock();

                    }
                    break;
                }
            } catch (InterruptedException e) {
            }
        } while (true);
    }

    @Override
    public UndeployClass getOntologyUndeployClass() {
        return ontologyClass;
    }

    @Override
    public DeployDataProperty getOntologyDeployDataProperty() {
        return ontologyDataProperty;
    }

    @Override
    public DeployInstance getOntologyDeployInstance() {
        return ontologyInstance;
    }

    @Override
    public DeployObjectProperty getOntologyDeployObjectProperty() {
        return ontologyObjectProperty;
    }

    @Override
    public UndeployDataProperty getOntologyUndeployDataProperty() {
        return ontologyDataProperty;
    }

    @Override
    public UndeployInstance getOntologyUndeployInstance() {
        return ontologyInstance;
    }

    @Override
    public UndeployObjectProperty getOntologyUndeployObjectProperty() {
        return ontologyObjectProperty;
    }

    @Override
    public StartReasoner getOntologyReasoner() {
        return ontologyReasoner;
    }

    @Override
    public StartInstance getOntologyInstance() {
        return ontologyInstance;
    }

    public OntologyChangeManager getOntologyChangeManager() {
        return changeManager;
    }

    public void initializeOntology() {
        //Location classes
        this.getOntologyDeployClass().addClass("location");
        this.getOntologyDeployClass().addSubClass("indoor", "location");
        this.getOntologyDeployClass().addSubClass("floor", "indoor");
        this.getOntologyDeployClass().addSubClass("room", "indoor");
        this.getOntologyDeployClass().addSubClass("corridor", "indoor");
        this.getOntologyDeployClass().addSubClass("building", "indoor");
        this.getOntologyDeployClass().addSubClass("outdoor", "location");
        //Location properties
        this.getOntologyDeployObjectProperty().addObjectProperty("isLocatedIn");
        this.getOntologyDeployObjectProperty().addObjectProperty("isLocationOf");
        this.getOntologyDeployObjectProperty().addObjectProperty("isDriverOf");
        this.getOntologyDeployObjectProperty().addObjectProperty("isResourceOf");
        this.getOntologyDeployObjectProperty().addTransitiveProperty("isLocatedIn");
        this.getOntologyDeployObjectProperty().addSubObjectProperty("isFloorInBuilding", "isLocatedIn");
        this.getOntologyDeployObjectProperty().addSubObjectProperty("isRoomInFloor", "isLocatedIn");
        this.getOntologyDeployObjectProperty().addSubObjectProperty("isEntityInLocation", "isLocatedIn");
        this.getOntologyDeployObjectProperty().addObjectPropertyDomain("isEntityInLocation", "entity");
        this.getOntologyDeployObjectProperty().addObjectPropertyRange("isEntityInLocation", "location");
        this.getOntologyDeployObjectProperty().addObjectPropertyDomain("isFloorInBuilding", "location");
        this.getOntologyDeployObjectProperty().addObjectPropertyRange("isFloorInBuilding", "location");
        this.getOntologyDeployObjectProperty().addObjectPropertyDomain("isRoomInFloor", "location");
        this.getOntologyDeployObjectProperty().addObjectPropertyRange("isRoomInFloor", "location");
        this.getOntologyDeployObjectProperty().addObjectPropertyDomain("isDriverOf", "driver");
        this.getOntologyDeployObjectProperty().addObjectPropertyRange("isDriverOf", "resource");
        this.getOntologyDeployObjectProperty().addInverseProperty("isLocatedIn", "isLocationOf");
        this.getOntologyDeployObjectProperty().addInverseProperty("isDriverOf", "isResourceOf");
        //Entity classes
        this.getOntologyDeployClass().addClass("entity");
        this.getOntologyDeployClass().addSubClass("physical", "entity");
        this.getOntologyDeployClass().addSubClass("logical", "entity");
        this.getOntologyDeployClass().addSubClass("application", "logical");
        this.getOntologyDeployClass().addSubClass("driver", "logical");
        this.getOntologyDeployClass().addSubClass("ontology", "logical");
        this.getOntologyDeployClass().addSubClass("user", "physical");
        this.getOntologyDeployClass().addSubClass("resource", "physical");
        //Default driver classes
        this.getOntologyDeployClass().addSubClass("input", "resource");
        this.getOntologyDeployClass().addSubClass("output", "resource");
        this.getOntologyDeployClass().addSubClass("pointer", "input");
        this.getOntologyDeployClass().addSubClass("keyboard", "input");

        //Annotation property: used to protect changes made by the applications.
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLAnnotationProperty annotationProperty = factory.getOWLAnnotationProperty(
                IRI.create(localContext.getOntologyID().getOntologyIRI() + "#" + ANNOTATION_PROPERTY));
        OWLAxiom axiom = factory.getOWLDeclarationAxiom(annotationProperty);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        manager.applyChange(addAxiom);
        saveChanges();
    }

    public int getNumberOfAxioms() {
        return localContext.getAxiomCount();
    }
}
