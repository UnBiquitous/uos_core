/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.unbiquitous.uos.core.application.UosApplication;
import org.unbiquitous.uos.core.ontologyEngine.exception.CycleException;
import org.unbiquitous.uos.core.ontologyEngine.exception.DeclarationException;
import org.unbiquitous.uos.core.ontologyEngine.exception.DisjunctionException;
import org.unbiquitous.uos.core.ontologyEngine.exception.RedundancyException;
import org.unbiquitous.uos.core.ontologyEngine.exception.RemovalException;


/**
 *
 * @author anaozaki
 */
public class OntologyChangeManager {
    
    private OWLOntology localContext;
    private OWLOntologyManager manager;
    private OntologyReasoner ontologyReasoner;
    private String prefix;
    
    OntologyChangeManager(OWLOntologyManager manager, OWLOntology localContext, Ontology ontology) {
        this.manager = manager;
        this.localContext = localContext;
        this.ontologyReasoner = (OntologyReasoner) ontology.getOntologyReasoner();
        prefix = localContext.getOntologyID().getOntologyIRI() + "#";
    }
    
    public void validateAddChange(OWLAxiom axiom) throws DeclarationException{
        //Constraint 1
        if(localContext.containsAxiom(axiom)){
            throw new DeclarationException("Entity already defined.");
        }
    }
    
    public void validateAddSubClassChange(OWLAxiom axiom, OWLClass owlClass, OWLClass owlClass2) throws RedundancyException, CycleException, DeclarationException {
        //Constraint 2 - Prevents cycles
        List<String> strList = ontologyReasoner.getSuperClassesFromClass(owlClass2.getIRI().getFragment(), false);    
        if(strList.contains(owlClass.getIRI().getFragment()))
            throw new CycleException("Cycle detected by inserion of subClass axiom. ");
        List<String> strList2 = ontologyReasoner.getSuperClassesFromClass(owlClass.getIRI().getFragment(), true);
        //Constraint 4 - Prevents redundancy of subclass
        for(String str2 : strList2)
            for(String str : strList){
                if(str2.compareTo(str) == 0){
                    if(str.compareTo("Thing") != 0){    
                        throw new RedundancyException("SubClass redundancy detected between classes "+owlClass.getIRI().getFragment()+" and " +str,owlClass.getIRI().getFragment(),str);
                    }    
                }  
            }    
        //Constraint 1
        if(localContext.containsAxiomIgnoreAnnotations(axiom)){
            throw new DeclarationException("SubClass already defined.");
        }
    }
    
    public void validateDisjointChange(AddAxiom addAxiom, OWLClass owlClass, OWLClass owlClass2) throws DisjunctionException{
        //Constraint 3
        if(owlClass.getIRI().getFragment().compareTo(owlClass2.getIRI().getFragment()) == 0)
            throw new DisjunctionException("Same class disjunction exception. ");
        
    }
    
    public void validateAddSubDataPropertyChange(OWLAxiom axiom, OWLDataProperty owlDataProperty, OWLDataProperty owlDataProperty2) throws RedundancyException, CycleException, DeclarationException {
        //Constraint 1
        if(localContext.containsAxiomIgnoreAnnotations(axiom)){
            throw new DeclarationException("SubDataProperty already defined.");
        }
        //Constraint 2 - Prevents cycles
        List<String> strList = ontologyReasoner.getSuperDataPropertiesFromDataProperty(owlDataProperty2.getIRI().getFragment(), false);    
        if(strList.contains(owlDataProperty.getIRI().getFragment()))
            throw new CycleException("Cycle detected by inserion of subDataProperty axiom. ");
        List<String> strList2 = ontologyReasoner.getSuperDataPropertiesFromDataProperty(owlDataProperty.getIRI().getFragment(), true);
        //Constraint 4 - Prevents redundancy of subclass
        for(String str2 : strList2)
            for(String str : strList)
                if(str2.compareTo(str) == 0){
                    //if(str.compareTo("Thing") != 0)
                        throw new RedundancyException("SubDataProperty redundancy detected between data properties "+owlDataProperty.getIRI().getFragment()+" and " +str,owlDataProperty.getIRI().getFragment(),str);
                }         
    }
    
    public void validateAddSubObjectPropertyChange(OWLAxiom axiom, OWLObjectProperty owlObjectProperty, OWLObjectProperty owlObjectProperty2) throws RedundancyException, CycleException, DeclarationException {
        //Constraint 1
        if(localContext.containsAxiomIgnoreAnnotations(axiom)){
            throw new DeclarationException("SubObjectProperty already defined.");
        }
        //Constraint 2 - Prevents cycles
        List<String> strList = ontologyReasoner.getSuperObjectPropertiesFromObjectProperty(owlObjectProperty2.getIRI().getFragment(), false);    
        if(strList.contains(owlObjectProperty.getIRI().getFragment()))
            throw new CycleException("Cycle detected by inserion of subObjectProperty axiom. ");
        List<String> strList2 = ontologyReasoner.getSuperObjectPropertiesFromObjectProperty(owlObjectProperty.getIRI().getFragment(), true);
        //Constraint 4 - Prevents redundancy of subclass
        for(String str2 : strList2)
            for(String str : strList)
                if(str2.compareTo(str) == 0){
                    //if(str.compareTo("Thing") != 0)
                        throw new RedundancyException("SubObjectProperty redundancy detected between object properties "+owlObjectProperty.getIRI().getFragment()+" and " +str,owlObjectProperty.getIRI().getFragment(),str);
                }         
    }
    
    public void validateRemovalChange(OWLAxiom axiom) throws RemovalException{
        //Constraint 5
        if(!localContext.containsAxiom(axiom))
           throw new RemovalException("Ontology "+prefix+" does not contain the axiom.");    
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes", "restriction" })
    protected String getApplicationName() {
//        Stack Trace Method       
//        StackTraceElement[] cause = Thread.currentThread().getStackTrace();    
//        int i;
//        for(i = 0; i < cause.length; i++){
//            Class cls;
//            try {
//                cls = Class.forName(cause[i].getClassName());
//                cls.asSubclass(UosApplication.class);
//                return cause[i].getClassName();
//            } catch (ClassCastException ex){
//                
//            } catch (ClassNotFoundException ex) {
//                return "uOS";
//            }
//        }
//        return "uOS";
// Reflection is faster than stack trace        
 
        Class cls;
        int i = 2;
        cls = sun.reflect.Reflection.getCallerClass(1);       
        while(cls != null){
            try{
                cls.asSubclass(UosApplication.class);
                return cls.getName();   
            } catch (ClassCastException ex){
               cls = sun.reflect.Reflection.getCallerClass(i);       
               i++; 
            } 
        }
        return "uOS";
    }
    
    public OWLAxiom getAnnotatedAxiom(OWLAxiom axiom) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLAnnotation annotation = factory.getOWLAnnotation(
                factory.getOWLAnnotationProperty(IRI.create(prefix + Ontology.ANNOTATION_PROPERTY)),
                factory.getOWLLiteral(this.getApplicationName()));
        Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
        annotations.add(annotation);
        return axiom.getAnnotatedAxiom(annotations);
    }
    
      public OWLAnnotation getAxiomAnnotation(OWLAxiom axiom) {
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLAnnotation annotation = factory.getOWLAnnotation(
                factory.getOWLAnnotationProperty(IRI.create(prefix + Ontology.ANNOTATION_PROPERTY)),
                factory.getOWLLiteral(this.getApplicationName()));
        return annotation;
    }
}
