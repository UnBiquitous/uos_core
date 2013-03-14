/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api;

/**
 * This interface contains the methods related to classes that could be invoked 
 * at the deploy of an application.
 * 
 * @author anaozaki
 */
public interface DeployClass {
    
    /**
     * Add class to ontology. The class is subclass of Thing.
     * 
     * @param className Name of the class.
     */
    public void addClass(String className);
    
    /**
     * Add subclass to ontology. The class is subclass of class. If class doesn't
     * exist it is created under Thing.
     * 
     * @param subClassName Name of the subclass.
     * @param className Name of the class.
     */
    public void addSubClass(String subClassName, String className);
    
    /**
     * Add axiom that states a class is equivalent to another. 
     * 
     * @param className Name of the class.
     * @param className Name of the equivalent class.
     */
    public void addEquivalentClass(String className, String equivClassName);
    
    /**
     * Add axiom that states a class is disjoint of another. 
     * 
     * @param className Name of the class.
     * @param className Name of the disjoint class.
     */
    //public void addDisjointClass(String className, String disjClassName) throws ClassCycleException;
    public void addDisjointClass(String className, String disjClassName);
    
    
    /**
     * Move class from one parent to another. 
     * 
     * @param fromClassName Name of the class that will lose it's subclass.
     * @param subClassName Name of the subclass.
     * @param toClassName Name of the class that will receive the subclass.
     */
    public void moveSubClass(String subClassName, String fromClassName, String toClassName);
    
    public boolean hasClass(String className);
    public boolean hasSubClass(String subClassName, String className);
    public boolean hasEquivalentClass(String equivClassName, String className);
    public boolean hasDisjointClass(String disjClassName, String className);
}
