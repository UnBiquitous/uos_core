/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api;

/**
 * This interface is intended to get the entities defined by OWL and return it 
 * only with the methods allowed at deploy. 
 * 
 * @author anaozaki
 */
public interface OntologyDeploy extends OntologyUndeploy {
   
    /**
     * Get ontology class entity. 
     * 
     * @return DeployClass Class entity with an interface that defines the 
     * methods related to a class that are allowed at deploy. 
     */
    public DeployClass getOntologyDeployClass();
    
    /**
     * Get ontology data property entity. 
     * 
     * @return DeployDataProperty Data property entity with an interface that 
     * defines the methods related to a data property that are allowed at deploy. 
     */
    public DeployDataProperty getOntologyDeployDataProperty();
    
    /**
     * Get ontology instance entity. 
     * 
     * @return DeployInstance Instance entity with an interface that defines the 
     * methods related to an instance that are allowed at deploy. 
     */
    public DeployInstance getOntologyDeployInstance();
    
    /**
     * Get ontology object property entity. 
     * 
     * @return DeployObjectProperty Object property entity with an interface 
     * that defines the methods related to an object property that are allowed 
     * at deploy. 
     */
    public DeployObjectProperty getOntologyDeployObjectProperty();
    
    /**
     * Save changes made in the ontology. Changes are accepted only if 
     * consistence is preserved.
     */
    public void saveChanges();
    
    /**
     * Returns the number of axioms present in the ontology.
     */
    public int getNumberOfAxioms();
}
