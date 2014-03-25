package org.unbiquitous.uos.core.adaptabitilyEngine;

import java.util.List;
import java.util.Map;

import org.unbiquitous.uos.core.driverManager.DriverData;
import org.unbiquitous.uos.core.messageEngine.MessageEngineException;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

/**
 * The Gateway represents a way to access the Smart Space capabilities 
 * provided through the middleware.
 * 
 * @author Fabricio Buzeto
 *
 */
public interface Gateway {

	/**
	 * Makes a Service Call according to the informed parameters.
	 * 
	 * @param device The Device which you'll make the call. Null if local.
	 * @param serviceName Name of the service to be called.
	 * @param driverName Driver Name on which the service resides.
	 * @param instanceId Optional instance id. If not informed, the middleware will choose.
	 * @param securityType Optional if crypto is being used.
	 * @param parameters Service Parameters.
	 * @return Response of the execution.
	 */
	public Response callService(UpDevice device,
			String serviceName, String driverName, String instanceId,
			String securityType, Map<String, Object> parameters)
			throws ServiceCallException;

	/**
	 * Makes a Service Call according to the informed parameters.
	 * 
	 * @param device The Device which you'll make the call. Null if local.
	 * @param serviceCall	Call Object.
	 * @return Response of the execution.
	 */
	public Response callService(UpDevice device,Call serviceCall) throws ServiceCallException;

	/**
	 * @see Gateway#register(UosEventListener, UpDevice, String, String, String, Map)
	 */
	public void register(UosEventListener listener,
			UpDevice device, String driver, String eventKey)
			throws NotifyException;

	
	/**
	 * @see Gateway#register(UosEventListener, UpDevice, String, String, String, Map)
	 */
	public void register(UosEventListener listener,
			UpDevice device, String driver, String instanceId, String eventKey)
			throws NotifyException;
	
	/**
	 * Register a Listener for a event, driver and device specified.
	 * 
	 * @param listener UosEventListener responsible for dealing with the event.
	 * @param device Device which event must be listened
	 * @param driver Driver responsible for the event.
	 * @param instanceId Instance Identifier of the driver to be registered upon.
	 * @param eventKey EventKey that identifies the wanted event to be listened.
	 * @throws NotifyException In case of an error.
	 */
	public void register(UosEventListener listener,UpDevice device, 
			String driver, String instanceId, String eventKey,
			Map<String, Object> parameters)
			throws NotifyException;

	/**
	 * Removes a listener for receiving Notify events and notifies the event driver of its removal.
	 * 
	 * @param listener Listener to be removed.
	 * @throws NotifyException
	 */
	public void unregister(UosEventListener listener) throws NotifyException;
	
	/**
	 * Removes a listener for receiving Notify events and notifies the event driver of its removal.
	 * 
	 * @param listener Listener to be removed.
	 * @param driver Driver from which the listener must be removed (If not informed all drivers will be considered).
	 * @param instanceId InstanceId from the Driver which the listener must be removed (If not informed all instances will be considered).
	 * @param eventKey EventKey from which the listener must be removed (If not informed all events will be considered).
	 * @throws NotifyException
	 */
	public void unregister(UosEventListener listener, UpDevice device, String driver, String instanceId, String eventKey) throws NotifyException;
	
	/**
	 * Sends a notify message to the device informed.
	 * 
	 * @param notify Notify message to be sent.
	 * @param device Device which is going to receive the notofy event
	 * @throws MessageEngineException
	 */
	public void notify(Notify notify, UpDevice device) throws NotifyException;
	
	/**
	 * @return Data about the Current Device uOS is running on.
	 */
	public UpDevice getCurrentDevice();
	
	/**
	 * Returns the list of drivers known in the database.
	 * 
	 * @param driverName Optional filter for the query.
	 * @return list of drivers known in the database.
	 */
	public List<DriverData> listDrivers(String driverName);
	
	/**
	 * Returns all known devices in the Smart Space.
	 */
	public List<UpDevice> listDevices();

}