/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api;

/**
 * This interface contains the methods related to classes that could be invoked 
 * at the undeploy of an application.
 * 
 * @author anaozaki
 */
public interface UndeployClass {
    
    /**
     * Remove class from ontology. If the class has instances, all the 
     * instances are removed. The subclass and it's instances are also removed.
     * 
     * @param className Name of the class to be removed.
     */
    public void removeClass(String className);
    
    /**
     * Remove the subclass axiom from ontology. Nothing else changes. 
     * 
     * @param subClassName Name of the subclass.
     * @param className Name of the class.
     */
    public void removeSubClass(String subClassName, String className);
    
    /**
     * Remove equivalent class axiom.
     * 
     * @param className Name of the class.
     * @param equivClassName Name of the equivalent class.
     */
    public void removeEquivalentClass(String className, String equivClassName);
    
    /**
     * Remove the disjoint class axiom.
     * 
     * @param className Name of the class.
     * @param disjClassName Name of the disjoint class.
     */
    public void removeDisjointClass(String className, String disjClassName);
    
    public boolean hasClass(String className);
    public boolean hasSubClass(String subClassName, String className);
    public boolean hasEquivalentClass(String equivClassName, String className);
    public boolean hasDisjointClass(String disjClassName, String className);
}
