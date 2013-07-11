/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.ontologyEngine.api.DeployObjectProperty;
import org.unbiquitous.uos.core.ontologyEngine.api.UndeployObjectProperty;
import org.unbiquitous.uos.core.ontologyEngine.exception.CycleException;
import org.unbiquitous.uos.core.ontologyEngine.exception.DeclarationException;
import org.unbiquitous.uos.core.ontologyEngine.exception.RedundancyException;
import org.unbiquitous.uos.core.ontologyEngine.exception.RemovalException;


public class OntologyObjectProperty implements DeployObjectProperty, UndeployObjectProperty{
    
    private static final Logger logger = UOSLogging.getLogger();
    private OWLOntology localContext;
    private OWLOntologyManager manager;
    private OntologyChangeManager changeManager;
    String prefix;
    OntologyObjectProperty(OWLOntologyManager manager, OWLOntology localContext,  OntologyChangeManager changeManager) {
        this.manager = manager;
        this.localContext = localContext;
        this.changeManager = changeManager;
        prefix = localContext.getOntologyID().getOntologyIRI() + "#";
    }

    @Override
    public void addObjectProperty(String objectPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLAxiom axiom = factory.getOWLDeclarationAxiom(objectProperty);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateAddChange(axiom);
                manager.applyChange(addAxiom);
            }
        } catch (DeclarationException ex) {
            logger.info(ex.getMessage() + "Change ( addObjectProperty "
                    + objectPropertyName + " ) will not be applied.");
        }

    }

    @Override
    public void addSubObjectProperty(String subObjectPropertyName, String objectPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLObjectProperty subObjectProperty = factory.getOWLObjectProperty(IRI.create(prefix + subObjectPropertyName));
        OWLAxiom axiom = factory.getOWLSubObjectPropertyOfAxiom(subObjectProperty, objectProperty);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateAddSubObjectPropertyChange(axiom, subObjectProperty, objectProperty);
                manager.applyChange(addAxiom);
            }
        } catch (DeclarationException ex) {
            logger.info(ex.getMessage() + " Change ( addSubObjectProperty "
                    + subObjectPropertyName + " of " + objectPropertyName + " ) will not be applied.");
        } catch (RedundancyException ex) {
            synchronized (this) {
                removeSubObjectProperty(ex.getSubCls(), ex.getCls());
                manager.applyChange(addAxiom);
            }
        } catch (CycleException ex) {
            logger.severe(ex.getMessage() + " Change ( addSubObjectProperty " 
                    + subObjectPropertyName + " of " + objectPropertyName + ") will not be applied.");
        }
    }

    @Override
    public void addObjectPropertyDomain(String objectPropertyName, String domainName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLClass domain = factory.getOWLClass(IRI.create(prefix + domainName));
        OWLAxiom axiom = factory.getOWLObjectPropertyDomainAxiom(objectProperty, domain);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        manager.applyChange(addAxiom);  
    }

    @Override
    public void addObjectPropertyRange(String objectPropertyName, String rangeName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLClass range = factory.getOWLClass(IRI.create(prefix + rangeName));
        OWLAxiom axiom = factory.getOWLObjectPropertyRangeAxiom(objectProperty, range);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        manager.applyChange(addAxiom);  
    }

    @Override
    public void addTransitiveProperty(String objectPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLAxiom axiom = factory.getOWLTransitiveObjectPropertyAxiom(objectProperty);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        manager.applyChange(addAxiom);     
    }
    
    @Override
    public void addSymmetricProperty(String objectPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLAxiom axiom = factory.getOWLSymmetricObjectPropertyAxiom(objectProperty);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        manager.applyChange(addAxiom);     
    }
    
    @Override
    public void addInverseProperty(String objectPropertyName, String inverseObjectPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLObjectProperty inverseObjectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ inverseObjectPropertyName));
        OWLAxiom axiom = factory.getOWLInverseObjectPropertiesAxiom(objectProperty, inverseObjectProperty);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        AddAxiom addAxiom = new AddAxiom(localContext, axiom);
        manager.applyChange(addAxiom);     
    }
    
    @Override
    public void removeObjectProperty(String objectPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty obj = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLAxiom axiom = factory.getOWLDeclarationAxiom(obj);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        List<OWLOntologyChange> changes= new ArrayList<OWLOntologyChange>();
        for(OWLAxiom ax : localContext.getReferencingAxioms(obj)){
            ax = ax.getAxiomWithoutAnnotations();
            ax = changeManager.getAnnotatedAxiom(ax);
            changes.add(new RemoveAxiom(localContext, ax));
        }
        try{
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);      
                manager.applyChanges(changes);
            }
        } catch (RemovalException ex) {
            logger.severe(ex.getMessage() + 
                    "Object property not defined in Ontology or not defined by " +
                    "this application. Change ( removeObjectProperty" + 
                    objectPropertyName + ") will not be applied. ");
        }    
        
    }

    @Override
    public void removeSubObjectProperty(String subObjectPropertyName, String objectPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty obj = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLObjectProperty subObj = factory.getOWLObjectProperty(IRI.create(prefix + subObjectPropertyName));
        OWLAxiom axiom = factory.getOWLSubObjectPropertyOfAxiom(subObj, obj);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.severe(ex.getMessage() + " Change ( removeSubObjectProperty " + 
                    subObjectPropertyName + " of " + objectPropertyName + 
                    ") will not be applied.");
        }
    }

    @Override
    public void removeObjectPropertyDomain(String objectPropertyName, String domainName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLClass domain = factory.getOWLClass(IRI.create(prefix + domainName));
        OWLAxiom axiom = factory.getOWLObjectPropertyDomainAxiom(objectProperty, domain);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.severe(ex.getMessage() + " Change ( removeObjectPropertyDomain" + 
                    domainName + " of " + objectPropertyName + 
                    ") will not be applied.");
        }
    }

    @Override
    public void removeObjectPropertyRange(String objectPropertyName, String rangeName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLClass range = factory.getOWLClass(IRI.create(prefix + rangeName));
        OWLAxiom axiom = factory.getOWLObjectPropertyRangeAxiom(objectProperty, range);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.severe(ex.getMessage() + " Change ( removeObjectPropertyBooleanRange"  
                    + objectPropertyName + 
                    ") will not be applied.");
        }
    }
    
    @Override
    public void removeTransitiveProperty(String objectPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLAxiom axiom = factory.getOWLTransitiveObjectPropertyAxiom(objectProperty);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.severe(ex.getMessage() + " Change ( removeTransitiveProperty "  
                    + objectPropertyName + 
                    ") will not be applied.");
        }     
    }
    
    @Override
    public void removeSymmetricProperty(String objectPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLAxiom axiom = factory.getOWLSymmetricObjectPropertyAxiom(objectProperty);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.severe(ex.getMessage() + " Change ( removeSymmetricProperty "  
                    + objectPropertyName + 
                    ") will not be applied.");
        }     
    }
    
    @Override
    public void removeInverseProperty(String objectPropertyName, String inverseObjectPropertyName) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ objectPropertyName));
        OWLObjectProperty inverseObjectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ inverseObjectPropertyName));
        OWLAxiom axiom = factory.getOWLInverseObjectPropertiesAxiom(objectProperty, inverseObjectProperty);
        axiom = changeManager.getAnnotatedAxiom(axiom);
        RemoveAxiom removeAxiom = new RemoveAxiom(localContext, axiom);
        try {
            synchronized (this) {
                changeManager.validateRemovalChange(axiom);
                manager.applyChange(removeAxiom);
            }
        } catch (RemovalException ex) {
            logger.severe(ex.getMessage() + " Change ( removeInverseProperty "  
                    + objectPropertyName + " of " + inverseObjectPropertyName + 
                    ") will not be applied.");
        }     
    }
    
    @Override
    public boolean hasObjectProperty(String objectPropertyName){
        return localContext.containsObjectPropertyInSignature(IRI.create(prefix+ objectPropertyName));
    }
    
    @Override
    public boolean hasSubObjectProperty(String subObjectPropertyName, String objectPropertyName){
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create( prefix+ objectPropertyName));
        OWLObjectProperty subObjectProperty = factory.getOWLObjectProperty(IRI.create(prefix+ subObjectPropertyName));
        OWLAxiom axiom = factory.getOWLSubObjectPropertyOfAxiom(subObjectProperty, objectProperty);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }
    
    @Override
    public boolean hasObjectPropertyDomain(String objectPropertyName, String domainName){
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create( prefix+ objectPropertyName));
        OWLClass domain = factory.getOWLClass(IRI.create( prefix+ domainName));
        OWLAxiom axiom = factory.getOWLObjectPropertyDomainAxiom(objectProperty, domain);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }
    
    @Override
    public boolean hasObjectPropertyRange(String objectPropertyName, String rangeName){
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create( prefix+ objectPropertyName));
        OWLClass range = factory.getOWLClass(IRI.create( prefix+ rangeName));
        OWLAxiom axiom = factory.getOWLObjectPropertyRangeAxiom(objectProperty, range);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }

    @Override
    public boolean hasTransitiveProperty(String objectPropertyName){
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create( prefix+ objectPropertyName));
        OWLAxiom axiom = factory.getOWLTransitiveObjectPropertyAxiom(objectProperty);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }
    
    @Override
    public boolean hasInverseProperty(String objectPropertyName, String inverseObjectPropertyName){
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLObjectProperty objectProperty = factory.getOWLObjectProperty(IRI.create( prefix+ objectPropertyName));
        OWLObjectProperty inverseObjectProperty = factory.getOWLObjectProperty(IRI.create( prefix+ inverseObjectPropertyName));   
        OWLAxiom axiom = factory.getOWLInverseObjectPropertiesAxiom(objectProperty, inverseObjectProperty);
        return localContext.containsAxiomIgnoreAnnotations(axiom);
    }
    
    @Override
    public void moveSubObjectProperty(String subObjectPropertyName, String fromObjectPropertyName, String toObjectPropertyName) {
        addSubObjectProperty(subObjectPropertyName, toObjectPropertyName);
        removeSubObjectProperty(subObjectPropertyName, fromObjectPropertyName);
    }
    
    @Override
    public void changeObjectPropertyDomain(String objectPropertyName, String oldDomainName, String newDomainName) {
        addObjectPropertyDomain(objectPropertyName, newDomainName);
        removeObjectPropertyDomain(objectPropertyName, oldDomainName);
    }
    
    @Override
    public void changeObjectPropertyRange(String objectPropertyName, String oldRangeName, String newRangeName) {
        addObjectPropertyRange(objectPropertyName, newRangeName);
        removeObjectPropertyRange(objectPropertyName, oldRangeName);
    }
}
