/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api;


/**
 *
 * @author anaozaki
 */
public interface OntologyStart extends OntologyDeploy {
    

    public StartReasoner getOntologyReasoner();
    
    /**
     * Get ontology instance entity. 
     * 
     * @return StartInstance Instance entity with an interface that defines the 
     * methods related to an instance that are allowed while running the application. 
     */
    public StartInstance getOntologyInstance();
    
    /**
     * Save changes made in the ontology. Changes are accepted only if 
     * consistence is preserved.
     */
    public void saveChanges();
    
}
