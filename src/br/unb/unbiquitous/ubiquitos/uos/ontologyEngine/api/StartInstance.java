/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.ontologyEngine.api;

/**
 *
 * @author anaozaki
 */
public interface StartInstance {
    /**
     * Add instance to ontology. 
     * 
     * @param instanceName Name of the instance.
     * @param className Name of the class's instance.
     */
    public void addInstanceOf(String instanceName, String className);
    
    public boolean hasInstanceOf(String className, String instanceName);
    public boolean hasInstance(String instanceName);
    /**
     * Add an assertion relating two instances by an object property.
     * 
     * @param instanceName Name of the instance. The subject.
     * @param objetPropertyName Name of the object property.
     * @param instanceName2 Name of the second instance. The object.
     */
    public void addObjectPropertyAssertion(String instanceName, String objectPropertyName, String instanceName2);
    public void addDataPropertyAssertion(String instanceName, String dataPropertyName, boolean int_value);
    public void addDataPropertyAssertion(String instanceName, String dataPropertyName, float int_value);
    public void addDataPropertyAssertion(String instanceName, String dataPropertyName, int int_value);
    public void addDataPropertyAssertion(String instanceName, String dataPropertyName, String str_value);
    
    /**
     * Remove instance from ontology.
     * 
     * @param instanceName Name of the instance that will be removed.
     * @param className Name of the class that the instance refers.
     */
    public void removeInstanceOf(String instanceName, String className);
    
    /**
     * Remove an assertion relating two instances by an object property.
     * 
     * @param instanceName Name of the instance. The subject.
     * @param objetPropertyName Name of the object property.
     * @param instanceName2 Name of the second instance. The object.
     */
    public void removeObjectPropertyAssertion(String instanceName, String objectPropertyName, String instanceName2);
    
    /**
     * Move all instances of a class to another. 
     * 
     * @param fromClassName Name of the class that will lose it's instances.
     * @param toClassName Name of the class that will receive the instances.
     */
    public void moveInstancesOfClass(String fromClassName, String toClassName);
    
    public void moveInstanceOf(String instanceName, String fromClassName, String toClassName);
}
