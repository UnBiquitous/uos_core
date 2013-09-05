/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.driver;

import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.driverManager.UosEventDriver;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

/**
 *
 * @author anaozaki
 */
public interface OntologyDriver extends UosEventDriver {

    public static final String CLASS_NAME_PARAM = "className";
    public static final String SUBCLASS_NAME_PARAM = "subClassName";
    public static final String INSTANCE_NAME_PARAM = "instanceName";
    public static final String DATA_PROPERTY_NAME_PARAM = "dataPropertyName";
    public static final String OBJECT_PROPERTY_NAME_PARAM = "objectPropertyName";
    public static final String DIRECT_PARAM = "direct";
    public static final String ADD_REMOVE_PARAM = "addRemoveParam";
    public static final String ADD = "add";
    public static final String REMOVE = "remove";
    public static final String EVENT_KEY_PARAM = "eventKey";
    public static final String INSTANCE_OF_EVENT_KEY = "instanceOfEventKey";
    public static final String DATA_PROPERTY_EVENT_KEY = "dataPropertyEventKey";
    public static final String OBJECT_PROPERTY_EVENT_KEY = "objectPropertyEventKey";
    public static final String DRIVER_NAME = "br.unb.unbiquitous.ubiquitos.uos.driver.OntologyDriver";

    /* Returns true if instance checking is correct. False otherwise. */
    public void isInstanceOf(Call serviceCall,
            Response serviceResponse, CallContext messageContext);

    /* Returns true if subClass axiom is entailed. False otherwise. */
    public void isSubClassOf(Call serviceCall,
            Response serviceResponse, CallContext messageContext);

    /* Returns true if ontology has object property assertion. False otherwise. */
    public void hasObjectProperty(Call serviceCall,
            Response serviceResponse, CallContext messageContext);

    /* Returns instances from specified class. */
    public void getInstancesFromClass(Call serviceCall,
            Response serviceResponse, CallContext messageContext);

    /* Returns subClasses from specified class. */
    public void getSubClassesFromClass(Call serviceCall,
            Response serviceResponse, CallContext messageContext);

    /* Returns superClasses from specified class. */
    public void getSuperClassesFromClass(Call serviceCall,
            Response serviceResponse, CallContext messageContext);

    /* Returns data property values from the specified instance. */
    public void getDataPropertyValues(Call serviceCall,
            Response serviceResponse, CallContext messageContext);

    /* Returns true if classes are disjoint. False otherwise. */
    public void areDisjointClasses(Call serviceCall,
            Response serviceResponse, CallContext messageContext);

    /* Returns true if classes are equivalent. False otherwise. */
    public void areEquivalentClasses(Call serviceCall,
            Response serviceResponse, CallContext messageContext);

    public void notifyInstanceOfEvent(String messageType, String className,
            String instanceName);

    public void notifyDataPropertyEvent(String messageType, String instanceName,
            String dataPropertyName);

    public void notifyObjectPropertyEvent(String messageType, String instanceName,
            String objectPropertyName);
}
