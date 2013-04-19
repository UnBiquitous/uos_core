/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.ontologyEngine.api.DeployDataProperty;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyDataType;
import org.unbiquitous.uos.core.ontologyEngine.api.UndeployDataProperty;
import org.unbiquitous.uos.core.ontologyEngine.exception.CycleException;
import org.unbiquitous.uos.core.ontologyEngine.exception.DeclarationException;
import org.unbiquitous.uos.core.ontologyEngine.exception.RedundancyException;
import org.unbiquitous.uos.core.ontologyEngine.exception.RemovalException;


/**
 *
 * @author anaozaki
 */
public class OntologyDataProperty implements DeployDataProperty, UndeployDataProperty {

    private static final Logger logger = Logger.getLogger(OntologyDataProperty.class);
    private OWLOntology localContext;
    private OWLOntologyManager manager;
    private OntologyChangeManager changeManager;
    String prefix;

    OntologyDataProperty(OWLOntologyManager manager, OWLOntology localContext, OntologyChangeManager changeManager) {
        this.manager = manager;
        this.localContext = localContext;
        this.changeManager = changeManager;
        prefix = localContext.getOntologyID().getOntologyIRI() + "#";
    }

    @Override
    public void addDataProperty(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = factory.getOWLDeclarationAxiom(dataProperty);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateAddChange(axiom);
                manager.applyChange(addAxiom);
            }
        } catch (DeclarationException ex) {
            logger.info(ex.getMessage() + "Change ( addDataProperty "
                    + dataPropertyName + " ) will not be applied.");
        }
    }

    @Override
    public void addSubDataProperty(String subDataPropertyName, String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDataProperty subDataProperty = factory.getOWLDataProperty(IRI.create(prefix + subDataPropertyName));
        OWLAxiom axiom = factory.getOWLSubDataPropertyOfAxiom(subDataProperty, dataProperty);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateAddSubDataPropertyChange(axiom, subDataProperty, dataProperty);
                manager.applyChange(addAxiom);
            }
        } catch (DeclarationException ex) {
            logger.info(ex.getMessage() + " Change ( addSubDataProperty "
                    + subDataPropertyName + " of " + dataPropertyName + " ) will not be applied.");
        } catch (RedundancyException ex) {
            synchronized (this) {
                removeSubDataProperty(ex.getSubCls(), ex.getCls());
                manager.applyChange(addAxiom);
            }
        } catch (CycleException ex) {
            logger.error(ex.getMessage() + " Change ( addSubDataProperty " 
                    + subDataPropertyName + " of " + dataPropertyName + ") will not be applied.");
        }
    }

    @Override
    public void addDataPropertyDomain(String dataPropertyName, String domainName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLClass domain = factory.getOWLClass(IRI.create(prefix + domainName));
        OWLAxiom axiom = factory.getOWLDataPropertyDomainAxiom(dataProperty, domain);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        manager.applyChange(addAxiom);
    }

    @Override
    public void addDataPropertyRange(String dataPropertyName, OntologyDataType dataType) {
        switch(dataType){
            case STRING:
                this.addDataPropertyStringRange(dataPropertyName);
            case BOOLEAN:
                this.addDataPropertyBooleanRange(dataPropertyName);
            case REAL:
                this.addDataPropertyFloatRange(dataPropertyName);
            case INTEGER:    
                this.addDataPropertyIntegerRange(dataPropertyName);
        }
    }
    
    @Override
    public void addDataPropertyBooleanRange(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDatatype booleanDatatype = factory.getBooleanOWLDatatype();
        OWLAxiom axiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, booleanDatatype);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        manager.applyChange(addAxiom);
    }

    @Override
    public void addDataPropertyStringRange(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDatatype stringDatatype = factory.getOWLDatatype(IRI.create(prefix + "string"));
        OWLAxiom axiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, stringDatatype);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        manager.applyChange(addAxiom);
    }

    @Override
    public void addDataPropertyIntegerRange(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDatatype intDatatype = factory.getIntegerOWLDatatype();
        OWLAxiom axiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, intDatatype);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        manager.applyChange(addAxiom);
    }

    @Override
    public void addDataPropertyFloatRange(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDatatype floatDatatype = factory.getFloatOWLDatatype();
        OWLAxiom axiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, floatDatatype);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        manager.applyChange(addAxiom);
    }

    @Override
    public void removeDataProperty(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLAxiom axiom = factory.getOWLDeclarationAxiom(dataProperty);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        List<OWLOntologyChange> changes= new ArrayList<OWLOntologyChange>();
        for(OWLAxiom ax : localContext.getReferencingAxioms(dataProperty)){
            ax = ax.getAxiomWithoutAnnotations();
            ax = changeManager.getAnnotatedAxiom(ax);
            changes.add(new RemoveAxiom(localContext, ax));
        }
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChanges(changes);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + 
                    "Data property not defined in Ontology or not defined by " +
                    "this application. Change ( removeDataProperty" + 
                    dataPropertyName + ") will not be applied. ");
        }
    }

//    @Override
//    public void removeDataProperty(String dataPropertyName) {
//        OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(localContext));
//        OWLDataFactory factory = manager.getOWLDataFactory();
//        OWLDataProperty dat = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
//        OWLAxiom axiom = factory.getOWLDeclarationAxiom(dat);
//        axiom = changeManager.getAnnotatedAxiom(axiom);
//        try{
//            synchronized (this) {
//                changeManager.validateRemovalChange(axiom);
//                dat.accept(remover);
//                manager.applyChanges(remover.getChanges());
//            }
//        } catch (RemovalException ex) {
//            logger.error(ex.getMessage() + 
//                    "Data property not defined in Ontology or not defined by " +
//                    "this application. Change ( removeDataProperty" + 
//                    dataPropertyName + ") will not be applied. ");
//        }    
//    }

    @Override
    public void removeSubDataProperty(String subDataPropertyName, String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDataProperty subDataProperty = factory.getOWLDataProperty(IRI.create(prefix + subDataPropertyName));
        OWLAxiom axiom = factory.getOWLSubDataPropertyOfAxiom(subDataProperty, dataProperty);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeSubDataProperty " + 
                    subDataPropertyName + " of " + dataPropertyName + 
                    ") will not be applied.");
        }
    }

    @Override
    public void removeDataPropertyDomain(String dataPropertyName, String domainName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLClass domain = factory.getOWLClass(IRI.create(prefix + domainName));
        OWLAxiom axiom = factory.getOWLDataPropertyDomainAxiom(dataProperty, domain);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeDataPropertyDomain" + 
                    domainName + " of " + dataPropertyName + 
                    ") will not be applied.");
        }
    }

    @Override
    public void removeDataPropertyRange(String dataPropertyName, OntologyDataType dataType) {
        switch(dataType){
            case STRING:
                this.removeDataPropertyStringRange(dataPropertyName);
            case BOOLEAN:
                this.removeDataPropertyBooleanRange(dataPropertyName);
            case REAL:
                this.removeDataPropertyFloatRange(dataPropertyName);
            case INTEGER:    
                this.removeDataPropertyIntegerRange(dataPropertyName);
        }
    }
    
    @Override
    public void removeDataPropertyBooleanRange(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDatatype booleanDatatype = factory.getBooleanOWLDatatype();
        OWLAxiom axiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, booleanDatatype);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeDataPropertyBooleanRange"  
                    + dataPropertyName + 
                    ") will not be applied.");
        }
    }

    @Override
    public void removeDataPropertyIntegerRange(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDatatype integerDatatype = factory.getIntegerOWLDatatype();
        OWLAxiom axiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, integerDatatype);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeDataPropertyIntegerRange"  
                    + dataPropertyName + 
                    ") will not be applied.");
        }
    }

    @Override
    public void removeDataPropertyFloatRange(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDatatype floatDatatype = factory.getFloatOWLDatatype();
        OWLAxiom axiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, floatDatatype);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeDataPropertyFloatRange"  
                    + dataPropertyName + 
                    ") will not be applied.");
        }
    }

    @Override
    public void removeDataPropertyStringRange(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDatatype stringDatatype = factory.getOWLDatatype(IRI.create(prefix + "string"));
        OWLAxiom axiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, stringDatatype);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.error(ex.getMessage() + " Change ( removeDataPropertyStringRange"  
                    + dataPropertyName + 
                    ") will not be applied.");
        }
    }

    @Override
    public boolean hasDataProperty(String dataPropertyName) {
        return localContext.containsDataPropertyInSignature(IRI.create(prefix + dataPropertyName));
    }

    @Override
    public boolean hasSubDataProperty(String subDataPropertyName, String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDataProperty subDataProperty = factory.getOWLDataProperty(IRI.create(prefix + subDataPropertyName));
        OWLAxiom axiom = factory.getOWLSubDataPropertyOfAxiom(subDataProperty, dataProperty);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }

    @Override
    public boolean hasDataPropertyBooleanRange(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDatatype booleanDatatype = factory.getBooleanOWLDatatype();
        OWLAxiom axiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, booleanDatatype);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }

    @Override
    public boolean hasDataPropertyFloatRange(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDatatype floatDatatype = factory.getFloatOWLDatatype();
        OWLAxiom axiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, floatDatatype);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }

    @Override
    public boolean hasDataPropertyIntegerRange(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDatatype intDatatype = factory.getIntegerOWLDatatype();
        OWLAxiom axiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, intDatatype);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }

    @Override
    public boolean hasDataPropertyStringRange(String dataPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLDatatype stringDatatype = factory.getOWLDatatype(IRI.create(prefix + "string"));
        OWLAxiom axiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, stringDatatype);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }
    
    @Override
    public boolean hasDataPropertyDomain(String dataPropertyName, String domainName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDataProperty dataProperty = factory.getOWLDataProperty(IRI.create(prefix + dataPropertyName));
        OWLClass cls = factory.getOWLClass(IRI.create(prefix + domainName));
        OWLAxiom axiom = factory.getOWLDataPropertyDomainAxiom(dataProperty, cls);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }
    
    @Override
    public void moveSubDataProperty(String subDataPropertyName, String fromDataPropertyName, String toDataPropertyName) {
        addSubDataProperty(subDataPropertyName, toDataPropertyName);
        removeSubDataProperty(subDataPropertyName, fromDataPropertyName);
    }

    @Override
    public void changeDataPropertyDomain(String dataPropertyName, String oldDomainName, String newDomainName) {
        addDataPropertyDomain(dataPropertyName, newDomainName);
        removeDataPropertyDomain(dataPropertyName, oldDomainName);
    }
    
    @Override
    public void changeDataPropertyRange(String dataPropertyName, OntologyDataType oldRangeName, OntologyDataType newRangeName) {
        addDataPropertyRange(dataPropertyName, newRangeName);
        removeDataPropertyRange(dataPropertyName, oldRangeName);
    }
}
