package org.unbiquitous.uos.core.adaptabitilyEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.messageEngine.MessageEngine;
import org.unbiquitous.uos.core.messageEngine.MessageEngineException;
import org.unbiquitous.uos.core.messageEngine.NotifyHandler;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

/**
 * Class responsible for managing the events received and the event listeners in the current device.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class EventManager implements NotifyHandler {
	
	private static Logger logger = UOSLogging.getLogger();
	
	private static final String REGISTER_EVENT_LISTENER_EVENT_KEY_PARAMETER = "eventKey";

	private static final String REGISTER_LISTENER_SERVICE = "registerListener";
	private static final String UNREGISTER_LISTENER_SERVICE = "unregisterListener";

	private Map<String, List<ListenerInfo>> listenerMap = new HashMap<String, List<ListenerInfo>>();
	
	private MessageEngine messageEngine;
	
	public EventManager(MessageEngine messageEngine) {
		this.messageEngine = messageEngine;
	}
	
	private static class ListenerInfo{
		UosEventListener listener;
		UpDevice device;
		String driver;
		String instanceId;
		String eventKey;
	}
	
	/**
	 * Sends a notify message to the device informed.
	 * 
	 * @param notify Notify message to be sent.
	 * @param device Device which is going to receive the notofy event
	 * @throws MessageEngineException
	 */
	public void notify(Notify notify, UpDevice device) throws NotifyException{
		try {
			if(device == null){
				handleNofify(notify, device);
			}else{
				messageEngine.notify(notify, device);
			}
		} catch (MessageEngineException e) {
			throw new NotifyException(e);
		}
	}
	
	/**
	 * This method formats a unique identifier for the event.
	 */
	private static String getEventIdentifier(UpDevice device, String driver, String instanceId, String eventKey){
		StringBuilder id =  new StringBuilder();
		if (device != null 
				&& device.getName() != null
				&& !device.getName().isEmpty()){
			id.append("@"+device.getName());
		}
		if (driver != null
				&& !driver.isEmpty()){
			id.append("*"+driver);
		}
		if (eventKey != null
				&& !eventKey.isEmpty()){
			id.append("."+eventKey);
		}
		if (instanceId != null
				&& !instanceId.isEmpty()){
			id.append("#"+instanceId);
		}
		return id.toString();
	}
	
	/**
	 * Register a Listener for a event, driver and device specified.
	 * 
	 * @param listener UosEventListener responsible for dealing with the event.
	 * @param device Device which event must be listened
	 * @param driver Driver responsible for the event.
	 * @param instanceId Instance Identifier of the driver to be registered upon. (Optional)
	 * @param eventKey EventKey that identifies the wanted event to be listened.
	 * @throws NotifyException In case of an error.
	 */
	public void register(UosEventListener listener, UpDevice device, 
			String driver, String instanceId, String eventKey,
			Map<String,Object> parameters) throws NotifyException{
		
		// If the listener is already registered it cannot be registered again
		String eventIdentifier = getEventIdentifier(device, driver, instanceId, eventKey);
		logger.fine("Registering listener for event :"+eventIdentifier);
		
		if (findListener(listener, listenerMap.get(eventIdentifier))== null){
			ListenerInfo info = new ListenerInfo();
			
			info.driver = driver;
			info.instanceId = instanceId;
			info.eventKey = eventKey;
			info.listener = listener;
			info.device = device;
			
			registerNewListener(device, parameters, eventIdentifier, info);
		}
	}

	private void registerNewListener(UpDevice device,
			Map<String, Object> parameters, String eventIdentifier,
			ListenerInfo info) throws NotifyException {
		try {
			if (device != null){
				sendRegister(device, parameters, info);
			}
			// If the registry process goes ok, then add the listenner to the listener map
			addToListenerMap(eventIdentifier, info);				
			logger.fine("Registered listener for event :"+eventIdentifier);
		} catch (MessageEngineException e) {
			throw new NotifyException(e);
		}
	}

	private void sendRegister(UpDevice device, Map<String, Object> parameters,
			ListenerInfo info) throws NotifyException {
		// Send the event register request to the called device
		Call serviceCall = buildRegisterCall(parameters, info);
		Response response = messageEngine.callService(device, serviceCall);
		if (response == null || (response.getError() != null && !response.getError().isEmpty())){
			throw new NotifyException(response.getError());
		}
	}

	private Call buildRegisterCall(Map<String, Object> parameters,
			ListenerInfo info) throws NotifyException {
		Call serviceCall = new Call(info.driver,REGISTER_LISTENER_SERVICE,info.instanceId);
		serviceCall.addParameter(REGISTER_EVENT_LISTENER_EVENT_KEY_PARAMETER, info.eventKey);
		addExtraParameters(parameters, serviceCall);
		return serviceCall;
	}

	private void addExtraParameters(Map<String, Object> parameters,
			Call serviceCall) throws NotifyException {
		if (parameters != null){
			for(String key : parameters.keySet()){
				checkIfKeyIsValid(key);
				serviceCall.addParameter(key, parameters.get(key));
			}
		}
	}

	private void checkIfKeyIsValid(String key) throws NotifyException {
		if (key.equalsIgnoreCase(REGISTER_EVENT_LISTENER_EVENT_KEY_PARAMETER)){
			throw new NotifyException("Can't use reserved keys as parameters for registerForEvent");
		}
	}
	
	private void addToListenerMap(String eventIdentifier, ListenerInfo info) {
		if (listenerMap.get(eventIdentifier) == null){
			listenerMap.put(eventIdentifier,new ArrayList<ListenerInfo>());
		}
		listenerMap.get(eventIdentifier).add(info);
	}

	
	/**
	 * Removes a listener for receiving Notify events and notifies the event driver of its removal.
	 * 
	 * @param listener Listener to be removed.
	 * @param driver Driver from which the listener must be removed (If not informed all drivers will be considered).
	 * @param instanceId InstanceId from the Driver which the listener must be removed (If not informed all instances will be considered).
	 * @param eventKey EventKey from which the listener must be removed (If not informed all events will be considered).
	 * @throws NotifyException
	 */
	public void unregisterForEvent(UosEventListener listener, UpDevice device, String driver, String instanceId, String eventKey) throws NotifyException{
		
		List<ListenerInfo> listeners = findListeners(	device, driver,
														instanceId, eventKey);
		
		if (listeners == null){
			return;
		}
		
		NotifyException exception = null;
		
		// Secondly filter listenners by the other data [listener, driver and instanceId]
		Iterator<ListenerInfo> it = listeners.iterator();
		ListenerInfo li;
		while(it.hasNext() ){
			li = it.next();
			
			// only if its the same listener, it should be removed
			if (li.listener.equals(listener) ){
				boolean remove = true;
				
				// If the driver name is informed, and it's not the same, it must not be removed
				if (driver != null && li.driver != null){
					remove = li.driver.equals(driver);
				}
				
				// If the instanceId is informed, and it's not the same, it must not be removed
				if (instanceId != null && li.instanceId != null){
					remove = li.instanceId.equals(instanceId);
				}
				
				if (remove){
					try {
						//Notify device of the listener removal
						unregisterForEvent(li);
						
						//remove listener from the list
						it.remove();
						
					} catch (NotifyException e) {
						logger.log(Level.SEVERE,"Failed to unregisterForEvent",e);
						exception = e;
					}
				}
			}
		}
		
		// In case of an error, throw it
		if (exception != null){
			throw exception;
		}
	}

	private List<ListenerInfo> findListeners(UpDevice device, String driver,
			String instanceId, String eventKey) {
		// First filter the listeners by the event key 
		List<ListenerInfo> listeners = null;
		
		if (eventKey == null){
			// In this case all eventKeys must be checked for the listener to be removed.
			listeners = new ArrayList<ListenerInfo>();
			for (List<ListenerInfo> list : listenerMap.values()){
				listeners.addAll(list);
			}
		}else{
			// In case a eventKey is informed, then only the listeners for that event key must be used
			String eventIdentifier = getEventIdentifier(device, driver, instanceId, eventKey);
			listeners = listenerMap.get(eventIdentifier);
		}
		return listeners;
	}
	
	/**
	 * Unregister an single listener from a DeviceDriver sending the apropriate message.
	 * 
	 * @param listenerInfo Information about the listener.
	 * 
	 * @throws NotifyException
	 */
	private void unregisterForEvent(ListenerInfo listenerInfo) throws NotifyException{
		// Send the event register request to the called device
		Call serviceCall = new Call(listenerInfo.driver,UNREGISTER_LISTENER_SERVICE,listenerInfo.instanceId);
		
		serviceCall.addParameter(REGISTER_EVENT_LISTENER_EVENT_KEY_PARAMETER, listenerInfo.eventKey);
		
		try {
			Response response = messageEngine.callService(listenerInfo.device, serviceCall);
			if (response == null || (response.getError() != null && !response.getError().isEmpty())){
				throw new NotifyException(response.getError());
			}
			
		} catch (MessageEngineException e) {
			throw new NotifyException(e);
		}
	}
	
	
	/**
	 * @see NotifyHandler#handleNofify(Notify)
	 */
	public void handleNofify(Notify notify, UpDevice device) {
		if (notify == null || notify.getEventKey() == null || notify.getEventKey().isEmpty()){
			logger.fine("No information in notify to handle.");
		}
		
		if (listenerMap == null || listenerMap.isEmpty()){
			logger.fine("No listeners waiting for notify events.");
		}
		
		//Notifying listeners from more specific to more general entries
		
		String eventIdentifier;
		// First full entries (device, driver, event, intanceId)
		eventIdentifier = getEventIdentifier(device, notify.getDriver(), notify.getInstanceId(), notify.getEventKey());
		List<ListenerInfo> listeners = listenerMap.get(eventIdentifier);
		handleNotify(notify, listeners, eventIdentifier);
		
		// After less general entries (device, driver, event)
		eventIdentifier = getEventIdentifier(device, notify.getDriver(), null, notify.getEventKey());
		listeners = listenerMap.get(eventIdentifier);
		handleNotify(notify, listeners, eventIdentifier);

		// An then the least general entries (driver, event)
		eventIdentifier = getEventIdentifier(null, notify.getDriver(), null, notify.getEventKey());
		listeners = listenerMap.get(eventIdentifier);
		handleNotify(notify, listeners, eventIdentifier);
		
	}

	private void handleNotify(Notify notify, List<ListenerInfo> listeners, String eventIdentifier) {
		if (listeners == null || listeners.isEmpty()){
			logger.fine("No listeners waiting for notify events for the key '"+eventIdentifier+"'.");
			return;
		}
		
		// call handlers in each listener
		for(ListenerInfo li : listeners){
			if (li.listener != null){
				li.listener.handleEvent(notify);
			}
		}
	}
	
	/**
	 * Find a listener instance in a list of ListenerInfos
	 * 
	 * @param listener Listener to be found.
	 * @param list List to search in.
	 * @return LinstenerInfo for the listener, if found.
	 */
	private ListenerInfo findListener(UosEventListener listener, List<ListenerInfo> list){
		ListenerInfo info = null;
		
		if (list != null && !list.isEmpty() && listener != null){
			for (ListenerInfo li : list){
				if (li.listener != null && li.listener.equals(listener)){
					info = li;
					break;
				}
			}
		}
		
		return info;
	}
	
}
