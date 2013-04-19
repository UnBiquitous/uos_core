/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine.api;

import java.util.List;

/**
 *
 * @author anaozaki
 */
public interface StartReasoner {
    
    /* Returns true if instance checking is correct. False otherwise. */
    public boolean isInstanceOf(String instanceName, String className);
    
    /* Returns true if subClass axiom is entailed. False otherwise. */
    public boolean isSubClassOf(String subClassName, String className);
    
    /* Returns true if ontology has object property assertion. False otherwise. */
    public boolean hasObjectProperty(String instanceName1, String objectPropertyName, String instanceName2);
    public boolean hasDataProperty(String instanceName1, String dataPropertyName, String string);
    public boolean hasDataProperty(String instanceName1, String dataPropertyName, boolean booleanValue);
    public boolean hasDataProperty(String instanceName1, String dataPropertyName, int intNumber);
    public boolean hasDataProperty(String instanceName1, String dataPropertyName, float floatNumber);
    public boolean isConsistent();
    
    /* Returns instances from specified class. */
    public List<String> getInstancesFromClass(String className, boolean direct);
    
    /* Returns subClasses from specified class. */
    public List<String> getSubClassesFromClass(String className, boolean direct);
    
    /* Returns superClasses from specified class. */
    public List<String> getSuperClassesFromClass(String className, boolean direct);
    
    /* Returns true if classes are disjoint. False otherwise. */
    public boolean areDisjointClasses(String className1, String className2);
    
    /* Returns true if classes are equivalent. False otherwise. */
    public boolean areEquivalentClasses(String className1, String className2);
    
    /* Returns subClasses from specified class. */
    public List<String> getSubDataPropertiesFromDataProperty(String dataPropertyName, boolean direct);
    
    /* Returns superDataProperties from specified data Property. */
    public List<String> getSuperDataPropertiesFromDataProperty(String dataPropertyName, boolean direct);

     /* Returns data property values from the specified instance. */
    public List<String> getDataPropertyValues(String instanceName, String dataPropertyName);
    
     /* Returns subClasses from specified class. */
    public List<String> getSubObjectPropertiesFromObjectProperty(String className, boolean direct);
    
    /* Returns superObjectProperties from specified object Property. */
    public List<String> getSuperObjectPropertiesFromObjectProperty(String objectPropertyName, boolean direct);
    
    /* Returns object property values from the specified instance. */
    public List<String> getObjectPropertyValues(String instanceName, String objectPropertyName);
}
