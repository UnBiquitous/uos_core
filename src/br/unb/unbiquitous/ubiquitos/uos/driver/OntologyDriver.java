/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.unbiquitous.ubiquitos.uos.driver;

import br.unb.unbiquitous.ubiquitos.uos.application.UOSMessageContext;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.UosEventDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

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
    public void isInstanceOf(ServiceCall serviceCall,
            ServiceResponse serviceResponse, UOSMessageContext messageContext);

    /* Returns true if subClass axiom is entailed. False otherwise. */
    public void isSubClassOf(ServiceCall serviceCall,
            ServiceResponse serviceResponse, UOSMessageContext messageContext);

    /* Returns true if ontology has object property assertion. False otherwise. */
    public void hasObjectProperty(ServiceCall serviceCall,
            ServiceResponse serviceResponse, UOSMessageContext messageContext);

    /* Returns instances from specified class. */
    public void getInstancesFromClass(ServiceCall serviceCall,
            ServiceResponse serviceResponse, UOSMessageContext messageContext);

    /* Returns subClasses from specified class. */
    public void getSubClassesFromClass(ServiceCall serviceCall,
            ServiceResponse serviceResponse, UOSMessageContext messageContext);

    /* Returns superClasses from specified class. */
    public void getSuperClassesFromClass(ServiceCall serviceCall,
            ServiceResponse serviceResponse, UOSMessageContext messageContext);

    /* Returns data property values from the specified instance. */
    public void getDataPropertyValues(ServiceCall serviceCall,
            ServiceResponse serviceResponse, UOSMessageContext messageContext);

    /* Returns true if classes are disjoint. False otherwise. */
    public void areDisjointClasses(ServiceCall serviceCall,
            ServiceResponse serviceResponse, UOSMessageContext messageContext);

    /* Returns true if classes are equivalent. False otherwise. */
    public void areEquivalentClasses(ServiceCall serviceCall,
            ServiceResponse serviceResponse, UOSMessageContext messageContext);

    public void notifyInstanceOfEvent(String messageType, String className,
            String instanceName);

    public void notifyDataPropertyEvent(String messageType, String instanceName,
            String dataPropertyName);

    public void notifyObjectPropertyEvent(String messageType, String instanceName,
            String objectPropertyName);
}
