/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine.api;

/**
 * This interface contains the methods related to instances that could be invoked 
 * at the undeploy of an application.
 * 
 * @author anaozaki
 */
public interface UndeployInstance {
    
    public static final String FLOAT = "float";
    public static final String INT = "int";
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";
    
    /**
     * Remove instance from ontology.
     * 
     * @param instanceName Name of the instance that will be removed.
     * @param className Name of the class that the instance refers.
     */
    public void removeInstanceOf(String instanceName, String className);
    public boolean hasInstanceOf(String className, String instanceName);
    public boolean hasInstance(String instanceName);
    /**
     * Remove an assertion relating two instances by an object property.
     * 
     * @param instanceName Name of the instance. The subject.
     * @param objetPropertyName Name of the object property.
     * @param instanceName2 Name of the second instance. The object.
     */
    public void removeObjectPropertyAssertion(String instanceName, String objectPropertyName, String instanceName2);
    
    public void removeDataPropertyAssertion(String instanceName, String dataPropertyName, String str_value);
    public void removeDataPropertyAssertion(String instanceName, String dataPropertyName, int int_value);
    public void removeDataPropertyAssertion(String instanceName, String dataPropertyName, float float_value);
    public void removeDataPropertyAssertion(String instanceName, String dataPropertyName, boolean boolean_value);
    /**
     * Remove all the instances of a class. 
     * 
     * @param className Name of the class with will have the instances removed
     */
    public void removeInstancesOfClass(String className);
    
}
