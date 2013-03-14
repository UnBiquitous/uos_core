/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api;


/**
 * This interface is intended to get the entities defined by OWL and return it 
 * only with the methods allowed at undeploy. 
 * 
 * @author anaozaki
 */
public interface OntologyUndeploy {

    /**
     * Get ontology class entity. 
     * 
     * @return UndeployClass Class entity with an interface that defines the 
     * methods related to a class that are allowed at undeploy. 
     */
    public UndeployClass getOntologyUndeployClass();
    
    /**
     * Get ontology data property entity. 
     * 
     * @return UndeployDataProperty Data property entity with an interface that 
     * defines the methods related to a data property that are allowed at 
     * undeploy. 
     */
    public UndeployDataProperty getOntologyUndeployDataProperty();
    
    /**
     * Get ontology instance entity. 
     * 
     * @return UndeployInstance Instance entity with an interface that defines the 
     * methods related to an instance that are allowed at undeploy. 
     */
    public UndeployInstance getOntologyUndeployInstance();
    
    /**
     * Get ontology object property entity. 
     * 
     * @return UndeployObjectProperty Object property entity with an interface 
     * that defines the methods related to an object property that are allowed 
     * at undeploy. 
     */
    public UndeployObjectProperty getOntologyUndeployObjectProperty();
    
    /**
     * Save changes made in the ontology. Changes are accepted only if 
     * consistence is preserved.
     */
    public void saveChanges();
    
}
