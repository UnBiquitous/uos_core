/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.NotifyException;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.ontologyEngine.OntologyInstance;
import org.unbiquitous.uos.core.ontologyEngine.api.StartReasoner;

/**
 *
 * @author anaozaki
 */
public class OntologyDriverImpl implements OntologyDriver {

    private StartReasoner reasoner;
    private Gateway gateway;
    private List<UpNetworkInterface> instanceOfListenerDevices;
    private List<UpNetworkInterface> dataPropertyListenerDevices;
    private List<UpNetworkInterface> objectPropertyListenerDevices;
    private String instanceId;
    private OntologyInstance ontologyInstance;
    public static final String ADD = "add";
    public static final String REMOVE = "remove";
    
    @Override
    public void isInstanceOf(Call serviceCall,
            Response serviceResponse, CallContext messageContext) {
        JSONObject returned_object = new JSONObject();

        String instanceName = (String) serviceCall.getParameter(INSTANCE_NAME_PARAM);
        String className = (String) serviceCall.getParameter(CLASS_NAME_PARAM);
        if (instanceName != null && className != null) {
            try {
                returned_object.put("queryResult", reasoner.isInstanceOf(instanceName, className));
            } catch (JSONException ex) {
                Logger.getLogger(OntologyDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        serviceResponse.addParameter("isInstanceOf", returned_object.toString());
    }

    @Override
    public void isSubClassOf(Call serviceCall,
            Response serviceResponse, CallContext messageContext) {
        JSONObject returned_object = new JSONObject();

        String subClassName = (String) serviceCall.getParameter(SUBCLASS_NAME_PARAM);
        String className = (String) serviceCall.getParameter(CLASS_NAME_PARAM);
        if (subClassName != null && className != null) {
            try {
                returned_object.put("queryResult", reasoner.isSubClassOf(subClassName, className));
            } catch (JSONException ex) {
                Logger.getLogger(OntologyDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        serviceResponse.addParameter("isSubClassOf", returned_object.toString());
    }

    @Override
    public void hasObjectProperty(Call serviceCall,
            Response serviceResponse, CallContext messageContext) {
        JSONObject returned_object = new JSONObject();
        String instanceName1 = (String) serviceCall.getParameter(INSTANCE_NAME_PARAM+1);
        String objectPropertyName = (String) serviceCall.getParameter(OBJECT_PROPERTY_NAME_PARAM);
        String instanceName2 = (String) serviceCall.getParameter(INSTANCE_NAME_PARAM+2);
        if (instanceName1 != null && objectPropertyName != null
                && instanceName2 != null) {
            try {
                returned_object.put("queryResult", reasoner.hasObjectProperty(instanceName1, objectPropertyName, instanceName2));
            } catch (JSONException ex) {
                Logger.getLogger(OntologyDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        serviceResponse.addParameter("hasObjectProperty", returned_object.toString());
    }

    @Override
    public void getInstancesFromClass(Call serviceCall,
            Response serviceResponse, CallContext messageContext) {
        JSONObject returned_object = new JSONObject();
        String className = (String) serviceCall.getParameter(CLASS_NAME_PARAM);
        String direct = (String) serviceCall.getParameter(DIRECT_PARAM);
        if (direct != null && className != null) {
            boolean directValue = Boolean.parseBoolean(direct);
            try {
                returned_object.put("queryResult", reasoner.getInstancesFromClass(className, directValue));
            } catch (JSONException ex) {
                Logger.getLogger(OntologyDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        serviceResponse.addParameter("getInstancesFromClass", returned_object.toString());
    }

    @Override
    public void getSubClassesFromClass(Call serviceCall,
            Response serviceResponse, CallContext messageContext) {
        JSONObject returned_object = new JSONObject();
        String className = (String) serviceCall.getParameter(CLASS_NAME_PARAM);
        String direct = (String) serviceCall.getParameter(DIRECT_PARAM);
        if (direct != null && className != null) {
            boolean directValue = Boolean.parseBoolean(direct);
            try {
                returned_object.put("queryResult", reasoner.getSubClassesFromClass(className, directValue));
            } catch (JSONException ex) {
                Logger.getLogger(OntologyDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        serviceResponse.addParameter("getSubClassesFromClass", returned_object.toString());
    }

    @Override
    public void getSuperClassesFromClass(Call serviceCall,
            Response serviceResponse, CallContext messageContext) {
        JSONObject returned_object = new JSONObject();
        String className = (String) serviceCall.getParameter(CLASS_NAME_PARAM);
        String direct = (String) serviceCall.getParameter(DIRECT_PARAM);
        if (direct != null && className != null) {
            boolean directValue = Boolean.parseBoolean(direct);
            try {
                returned_object.put("queryResult", reasoner.getSuperClassesFromClass(className, directValue));
            } catch (JSONException ex) {
                Logger.getLogger(OntologyDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        serviceResponse.addParameter("getSuperClassesFromClass", returned_object.toString());
    }

    @Override
    public void getDataPropertyValues(Call serviceCall,
            Response serviceResponse, CallContext messageContext) {
        JSONObject returned_object = new JSONObject();
        String instanceName = (String) serviceCall.getParameter(INSTANCE_NAME_PARAM);
        String dataPropertyName = (String) serviceCall.getParameter(DATA_PROPERTY_NAME_PARAM);
        if (instanceName != null && dataPropertyName != null) {
            try {
                returned_object.put("queryResult", reasoner.getDataPropertyValues(instanceName, dataPropertyName));
            } catch (JSONException ex) {
                Logger.getLogger(OntologyDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        serviceResponse.addParameter("getDataPropertyValues", returned_object.toString());
    }

    @Override
    public void areDisjointClasses(Call serviceCall,
            Response serviceResponse, CallContext messageContext) {
        JSONObject returned_object = new JSONObject();
        String className1 = (String) serviceCall.getParameter(CLASS_NAME_PARAM+1);
        String className2 = (String) serviceCall.getParameter(CLASS_NAME_PARAM+2);
        if (className1 != null && className2 != null) {
            try {
                returned_object.put("queryResult", reasoner.areDisjointClasses(className1, className2));
            } catch (JSONException ex) {
                Logger.getLogger(OntologyDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        serviceResponse.addParameter("areDisjointClasses", returned_object.toString());
    }

    @Override
    public void areEquivalentClasses(Call serviceCall,
            Response serviceResponse, CallContext messageContext) {
        JSONObject returned_object = new JSONObject();
        String className1 = (String) serviceCall.getParameter(CLASS_NAME_PARAM+1);
        String className2 = (String) serviceCall.getParameter(CLASS_NAME_PARAM+2);
        if (className1 != null && className2 != null) {
            try {
                returned_object.put("queryResult", reasoner.areEquivalentClasses(className1, className2));
            } catch (JSONException ex) {
                Logger.getLogger(OntologyDriverImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        serviceResponse.addParameter("areEquivalentClasses", returned_object.toString());
    }

    @Override
    public UpDriver getDriver() {
        UpDriver driver = new UpDriver("br.unb.unbiquitous.ubiquitos.uos.driver.OntologyDriver");

        driver.addService("isInstanceOf").addParameter("instanceName", UpService.ParameterType.MANDATORY).addParameter("className", UpService.ParameterType.MANDATORY);
        driver.addService("isSubClassOf").addParameter("subClassName", UpService.ParameterType.MANDATORY).addParameter("className", UpService.ParameterType.MANDATORY);
        driver.addService("hasObjectProperty").addParameter("instanceName1", UpService.ParameterType.MANDATORY).addParameter("objectPropertyName", UpService.ParameterType.MANDATORY).addParameter("instanceName2", UpService.ParameterType.MANDATORY);
        driver.addService("areDisjointClasses").addParameter("className1", UpService.ParameterType.MANDATORY).addParameter("className2", UpService.ParameterType.MANDATORY);
        driver.addService("areEquivalentClasses").addParameter("className1", UpService.ParameterType.MANDATORY).addParameter("className2", UpService.ParameterType.MANDATORY);
        return driver;
    }

    @Override
    public void init(Gateway gateway, InitialProperties properties, String instanceId) {
        this.gateway = gateway;
        this.instanceId = instanceId;
        this.reasoner = ((SmartSpaceGateway) gateway).getOntologyReasoner();
        if(((SmartSpaceGateway) gateway).getOntology()!=null){
            this.ontologyInstance = (OntologyInstance)((SmartSpaceGateway) gateway).
                    getOntology().getOntologyInstance();
            ontologyInstance.setOntologyDriver(this);
        }
        this.instanceOfListenerDevices = new ArrayList<UpNetworkInterface>();
        this.dataPropertyListenerDevices = new ArrayList<UpNetworkInterface>();
        this.objectPropertyListenerDevices = new ArrayList<UpNetworkInterface>();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void registerListener(Call serviceCall, Response serviceResponse, CallContext messageContext) {
        NetworkDevice networkDevice = messageContext.getCallerNetworkDevice();
        UpNetworkInterface networkInterface = new UpNetworkInterface(networkDevice.getNetworkDeviceType(), networkDevice.getNetworkDeviceName());
        String eventKey = (String) serviceCall.getParameter(EVENT_KEY_PARAM);

        if (INSTANCE_OF_EVENT_KEY.equals(eventKey)) {
            if (!instanceOfListenerDevices.contains(networkInterface)) {
                instanceOfListenerDevices.add(networkInterface);
            }
        } else if (DATA_PROPERTY_EVENT_KEY.equals(eventKey)) {
            if (!dataPropertyListenerDevices.contains(networkInterface)) {
                dataPropertyListenerDevices.add(networkInterface);
            }
        } else if (OBJECT_PROPERTY_EVENT_KEY.equals(eventKey)) {
            if (!objectPropertyListenerDevices.contains(networkInterface)) {
                objectPropertyListenerDevices.add(networkInterface);
            }
        }
    }

    @Override
    public void unregisterListener(Call serviceCall, Response serviceResponse, CallContext messageContext) {
        NetworkDevice networkDevice = messageContext.getCallerNetworkDevice();
        UpNetworkInterface networkInterface = new UpNetworkInterface(networkDevice.getNetworkDeviceType(), networkDevice.getNetworkDeviceName());
        String eventKey = (String) serviceCall.getParameter(EVENT_KEY_PARAM);

        if (eventKey == null) {
            instanceOfListenerDevices.remove(networkInterface);
            dataPropertyListenerDevices.remove(networkInterface);
            objectPropertyListenerDevices.remove(networkInterface);
        } else if (INSTANCE_OF_EVENT_KEY.equals(eventKey)) {
            instanceOfListenerDevices.remove(networkInterface);
        } else if (DATA_PROPERTY_EVENT_KEY.equals(eventKey)) {
            dataPropertyListenerDevices.remove(networkInterface);
        } else if (OBJECT_PROPERTY_EVENT_KEY.equals(eventKey)) {
            objectPropertyListenerDevices.remove(networkInterface);
        }
    }

	@Override
	public List<UpDriver> getParent() {
		return null;
	}
    
        /**
	 * Sets up the Notify message to send to every listener.
	 *
	 * @param messageType Description of which type of 
         *                    action was taken (add or remove).
	 * @param className class name.
         * @param instanceName instance name.
	 */
	public void notifyInstanceOfEvent(String messageType, String className, String instanceName) {
		Notify notify = new Notify(INSTANCE_OF_EVENT_KEY,DRIVER_NAME,instanceId);
                
		notify.addParameter(ADD_REMOVE_PARAM, messageType);
		notify.addParameter(CLASS_NAME_PARAM, className);
                notify.addParameter(INSTANCE_NAME_PARAM, instanceName);

		notifyRegisteredDevices(notify);
	}
        
        /**
	 * Sets up the Notify message to send to every listener.
	 *
	 * @param messageType Description of which type of 
         *                    action was taken (add or remove).
	 
         * @param instanceName instance name.
         * @param dataPropertyName data property name.
	 */
	public void notifyDataPropertyEvent(String messageType, String instanceName, String dataPropertyName) {
		Notify notify = new Notify(DATA_PROPERTY_EVENT_KEY,DRIVER_NAME,instanceId);
                
		notify.addParameter(ADD_REMOVE_PARAM, messageType);
		notify.addParameter(INSTANCE_NAME_PARAM, instanceName);
                notify.addParameter(DATA_PROPERTY_NAME_PARAM, dataPropertyName);

		notifyRegisteredDevices(notify);
	}
        
        /**
	 * Sets up the Notify message to send to every listener.
	 *
	 * @param messageType Description of which type of 
         *                    action was taken (add or remove).
	 
         * @param instanceName instance name.
         * @param objectPropertyName object property name.
	 */
	public void notifyObjectPropertyEvent(String messageType, String instanceName, String objectPropertyName) {
		Notify notify = new Notify(OBJECT_PROPERTY_EVENT_KEY,DRIVER_NAME,instanceId);
                
		notify.addParameter(ADD_REMOVE_PARAM, messageType);
		notify.addParameter(INSTANCE_NAME_PARAM, instanceName);
                notify.addParameter(OBJECT_PROPERTY_NAME_PARAM, objectPropertyName);

		notifyRegisteredDevices(notify);
	}
        
    /**
     * Notify all listeners when a certain event occurs.
     * 
     * @param notify
     */
    private void notifyRegisteredDevices(Notify notify) {
        List<UpNetworkInterface> listenerDevices = null;

        if (INSTANCE_OF_EVENT_KEY.equals(notify.getEventKey())) {
            listenerDevices = instanceOfListenerDevices;
        } else if (DATA_PROPERTY_EVENT_KEY.equals(notify.getEventKey())) {
            listenerDevices = dataPropertyListenerDevices;
        } else if (OBJECT_PROPERTY_EVENT_KEY.equals(notify.getEventKey())) {
            listenerDevices = objectPropertyListenerDevices;
        }

        for (UpNetworkInterface networkInterface : listenerDevices) {
            UpDevice device = new UpDevice("Anonymous");
            device.addNetworkInterface(networkInterface.getNetworkAddress(), networkInterface.getNetType());

            try {
                this.gateway.sendEventNotify(notify, device);
            } catch (NotifyException e) {
                e.printStackTrace();
            }
        }
    }
}
