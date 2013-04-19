/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine;

import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.driver.OntologyDriver;
import org.unbiquitous.uos.core.driverManager.UosDriver;
import org.unbiquitous.uos.core.ontologyEngine.api.DeployInstance;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyDataType;
import org.unbiquitous.uos.core.ontologyEngine.api.StartInstance;
import org.unbiquitous.uos.core.ontologyEngine.api.UndeployInstance;
import org.unbiquitous.uos.core.ontologyEngine.exception.DeclarationException;
import org.unbiquitous.uos.core.ontologyEngine.exception.RemovalException;


/**
 *
 * @author anaozaki
 */
public class OntologyInstance implements DeployInstance, UndeployInstance, StartInstance {

    private static final Logger logger = Logger.getLogger(OntologyInstance.class);
    private OWLOntology localContext;
    private OntologyReasoner ontologyReasoner;
    private OntologyChangeManager changeManager;
    private UosDriver ontologyDriver;
    private OWLOntologyManager manager;
    String prefix;

    OntologyInstance(OWLOntologyManager manager, OWLOntology localContext, OntologyChangeManager changeManager, OntologyReasoner ontologyReasoner) {
        this.manager = manager;
        this.localContext = localContext;
        prefix = localContext.getOntologyID().getOntologyIRI() + "#";
        this.ontologyReasoner = ontologyReasoner;
        this.changeManager = changeManager;

    }

    public void setOntologyDriver(UosDriver driver) {
        this.ontologyDriver = driver;
    }

    @Override
    public void addInstanceOf(String instanceName, String className) {
        try {
            OWLDataFactory factory = manager.getOWLDataFactory();
            OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
            OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
            OWLAxiom axiom = factory.getOWLClassAssertionAxiom(cls, instance);
            axiom = changeManager.getAnnotatedAxiom(axiom);
            AddAxiom addAxiom = new AddAxiom(localContext, axiom);
            synchronized (this) {
                changeManager.validateAddChange(axiom);
                manager.applyChange(addAxiom);
            }
        } catch (DeclarationException ex) {
            logger.info(ex.getMessage() + "Change ( addInstanceOf "
                    + instanceName + " ) will not be applied.");
        }
        if (ontologyDriver != null) {
            ((OntologyDriver) ontologyDriver).notifyInstanceOfEvent(OntologyDriver.ADD, className, instanceName);
        }
    }

    @Override
    public void addObjectPropertyAssertion(String instanceName, String objectPropertyName, String instanceName2) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix + objectPropertyName));
        OWLIndividual instance2 = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName2));
        OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(objectProperty, instance, instance2);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateAddChange(axiom);
                manager.applyChange(addAxiom);
            }
        } catch (DeclarationException ex) {
            logger.info(ex.getMessage() + "Change ( addObjectPropertyAssertion "
                    + instanceName + " " + objectPropertyName + " " + instanceName2
                    + " ) will not be applied.");
        }
        if (ontologyDriver != null) {
            ((OntologyDriver) ontologyDriver).notifyObjectPropertyEvent(OntologyDriver.ADD, instanceName, objectPropertyName);
        }
    }

    @Override
    public void addDataPropertyAssertion(String instanceName, String dataPropertyName, OntologyDataType dataType, Object value) {
        switch (dataType) {
            case STRING:
                this.addDataPropertyAssertion(instanceName, dataPropertyName, (String) value);
            case BOOLEAN:
                this.addDataPropertyAssertion(instanceName, dataPropertyName, (Boolean) value);
            case REAL:
                this.addDataPropertyAssertion(instanceName, dataPropertyName, (Float) value);
            case INTEGER:
                this.addDataPropertyAssertion(instanceName, dataPropertyName, (Integer) value);
            //default lanca excecao
        }
    }

    @Override
    public void addDataPropertyAssertion(String instanceName, String dataPropertyName, boolean boolean_value) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(dataProperty, instance, boolean_value);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateAddChange(axiom);
                manager.applyChange(addAxiom);
            }
        } catch (DeclarationException ex) {
            logger.info(ex.getMessage() + "Change ( addDataPropertyAssertion "
                    + instanceName + " " + dataPropertyName + " " + boolean_value
                    + " ) will not be applied.");
        }
        if (ontologyDriver != null) {
            ((OntologyDriver) ontologyDriver).notifyDataPropertyEvent(OntologyDriver.ADD, instanceName, dataPropertyName);
        }

    }

    @Override
    public void addDataPropertyAssertion(String instanceName, String dataPropertyName, float float_value) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(dataProperty, instance, float_value);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateAddChange(axiom);
                manager.applyChange(addAxiom);
            }
        } catch (DeclarationException ex) {
            logger.info(ex.getMessage() + "Change ( addDataPropertyAssertion "
                    + instanceName + " " + dataPropertyName + " " + float_value
                    + " ) will not be applied.");
        }
        if (ontologyDriver != null) {
            ((OntologyDriver) ontologyDriver).notifyDataPropertyEvent(OntologyDriver.ADD, instanceName, dataPropertyName);
        }

    }

    @Override
    public void addDataPropertyAssertion(String instanceName, String dataPropertyName, int int_value) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(dataProperty, instance, int_value);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateAddChange(axiom);
                manager.applyChange(addAxiom);
            }
        } catch (DeclarationException ex) {
            logger.info(ex.getMessage() + "Change ( addDataPropertyAssertion "
                    + instanceName + " " + dataPropertyName + " " + int_value
                    + " ) will not be applied.");
        }
        if (ontologyDriver != null) {
            ((OntologyDriver) ontologyDriver).notifyDataPropertyEvent(OntologyDriver.ADD, instanceName, dataPropertyName);
        }

    }

    @Override
    public void addDataPropertyAssertion(String instanceName, String dataPropertyName, String str_value) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(dataProperty, instance, str_value);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateAddChange(axiom);
                manager.applyChange(addAxiom);
            }
        } catch (DeclarationException ex) {
            logger.info(ex.getMessage() + "Change ( addDataPropertyAssertion "
                    + instanceName + " " + dataPropertyName + " " + str_value
                    + " ) will not be applied.");
        }
        if (ontologyDriver != null) {
            ((OntologyDriver) ontologyDriver).notifyDataPropertyEvent(OntologyDriver.ADD, instanceName, dataPropertyName);
        }

    }

    @Override
    public void removeObjectPropertyAssertion(String instanceName, String objectPropertyName, String instanceName2) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix + objectPropertyName));
        OWLIndividual instance2 = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName2));
        OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(objectProperty, instance, instance2);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeObjectPropertyAssertion"
                    + instanceName + " " + objectPropertyName + " " + instanceName2
                    + ") will not be applied.");
        }
        if (ontologyDriver != null) {
            ((OntologyDriver) ontologyDriver).notifyObjectPropertyEvent(OntologyDriver.REMOVE, instanceName, objectPropertyName);
        }

    }

    @Override
    public void removeDataPropertyAssertion(String instanceName, String dataPropertyName, String str_value) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(dataProperty, instance, str_value);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeDataPropertyAssertion"
                    + instanceName + " " + dataPropertyName + " " + str_value
                    + ") will not be applied.");
        }
        if (ontologyDriver != null) {
            ((OntologyDriver) ontologyDriver).notifyDataPropertyEvent(OntologyDriver.REMOVE, instanceName, dataPropertyName);
        }

    }

    @Override
    public void removeDataPropertyAssertion(String instanceName, String dataPropertyName, int int_value) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(dataProperty, instance, int_value);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeDataPropertyAssertion"
                    + instanceName + " " + dataPropertyName + " " + int_value
                    + ") will not be applied.");
        }
        if (ontologyDriver != null) {
            ((OntologyDriver) ontologyDriver).notifyDataPropertyEvent(OntologyDriver.REMOVE, instanceName, dataPropertyName);
        }

    }

    @Override
    public void removeDataPropertyAssertion(String instanceName, String dataPropertyName, float float_value) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(dataProperty, instance, float_value);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeDataPropertyAssertion"
                    + instanceName + " " + dataPropertyName + " " + float_value
                    + ") will not be applied.");
        }
        if (ontologyDriver != null) {
            ((OntologyDriver) ontologyDriver).notifyDataPropertyEvent(OntologyDriver.REMOVE, instanceName, dataPropertyName);
        }
    }

    @Override
    public void removeDataPropertyAssertion(String instanceName, String dataPropertyName, boolean boolean_value) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(dataProperty, instance, boolean_value);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeDataPropertyAssertion "
                    + instanceName + " " + dataPropertyName + " " + boolean_value
                    + ") will not be applied.");
        }
        if (ontologyDriver != null) {
            ((OntologyDriver) ontologyDriver).notifyDataPropertyEvent(OntologyDriver.REMOVE, instanceName, dataPropertyName);
        }
    }

    @Override
    public void removeInstanceOf(String instanceName, String className) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(prefix + instanceName));
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + className));
        OWLAxiom axiom = factory.getOWLClassAssertionAxiom(cls, instance);
        axiom = changeManager.getAnnotatedAxiom(axiom);

        try {
            RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeInstanceOf "
                    + instanceName + " of " + className
                    + ") will not be applied.");
        } 
        if (ontologyDriver != null) {
            ((OntologyDriver) ontologyDriver).notifyInstanceOfEvent(OntologyDriver.REMOVE, className, instanceName);
        }
    }

    @Override
    public boolean hasInstanceOf(String instanceName, String className) {
        return localContext.containsIndividualInSignature(IRI.create(prefix + instanceName));
    }

    @Override
    public boolean hasInstance(String instanceName) {
        return localContext.containsIndividualInSignature(IRI.create(prefix + instanceName));
    }

    @Override
    public void moveInstanceOf(String instanceName, String fromClassName, String toClassName) {
        removeInstanceOf(instanceName, fromClassName);
        addInstanceOf(instanceName, toClassName);
    }

    @Override
    public void moveInstancesOfClass(String fromClassName, String toClassName) {
        List<String> strList = ontologyReasoner.getInstancesFromClass(fromClassName, false);
        for (String str : strList) {
            this.addInstanceOf(str, toClassName);
        }
        removeInstancesOfClass(fromClassName);
    }

    @Override
    public void removeInstancesOfClass(String className) {
        List<String> strList = ontologyReasoner.getInstancesFromClass(className, false);
        for (String str : strList) {
            this.removeInstanceOf(str, className);
        }
    }
}
