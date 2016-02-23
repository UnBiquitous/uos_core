package org.unbiquitous.uos.core.adaptabitilyEngine;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.messageEngine.MessageEngine;
import org.unbiquitous.uos.core.messageEngine.MessageEngineException;
import org.unbiquitous.uos.core.messageEngine.NotifyHandler;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.unbiquitous.uos.core.ClassLoaderUtils.chainHashCode;

/**
 * Class responsible for managing the events received and the event listeners in the current device.
 *
 * @author Fabricio Nogueira Buzeto
 */
public class EventManager implements NotifyHandler {

    private static Logger logger = UOSLogging.getLogger();

    private static final String REGISTER_EVENT_LISTENER_EVENT_KEY_PARAMETER = "eventKey";

    private static final String REGISTER_LISTENER_SERVICE = "registerListener";
    private static final String UNREGISTER_LISTENER_SERVICE = "unregisterListener";

    private ListenerDao listenerDao = new ListenerDao();

    private MessageEngine messageEngine;

    public EventManager(MessageEngine messageEngine) {
        this.messageEngine = messageEngine;
    }

    /**
     * Sends a notify message to the device informed.
     *
     * @param notify Notify message to be sent.
     * @param device Device which is going to receive the notofy event
     * @throws MessageEngineException
     */
    public void notify(Notify notify, UpDevice device) throws NotifyException {
        try {
            if (device == null) {
                handleNofify(notify, device);
            } else {
                messageEngine.notify(notify, device);
            }
        } catch (MessageEngineException e) {
            throw new NotifyException(e);
        }
    }

    /**
     * Register a Listener for a event, driver and device specified.
     *
     * @param listener   UosEventListener responsible for dealing with the event.
     * @param device     Device which event must be listened
     * @param driver     Driver responsible for the event.
     * @param instanceId Instance Identifier of the driver to be registered upon. (Optional)
     * @param eventKey   EventKey that identifies the wanted event to be listened.
     * @throws NotifyException In case of an error.
     */
    public void register(
            UosEventListener listener,
            UpDevice device,
            String driver,
            String instanceId,
            String eventKey,
            Map<String, Object> parameters) throws NotifyException {

        if (listener == null)
            throw new NullPointerException("listener");
        EventFilter filter = new EventFilter(device, driver, instanceId, eventKey);
        logger.fine("Registering listener for event : " + filter);

        try {
            if (device != null)
                sendRegister(device, parameters, filter);
            listenerDao.insert(listener, filter);
            logger.fine("Registered listener for event :" + filter);
        } catch (MessageEngineException e) {
            throw new NotifyException(e);
        }
    }

    private void sendRegister(UpDevice device, Map<String, Object> parameters, EventFilter filter) throws NotifyException {
        Call serviceCall = new Call(filter.driver, REGISTER_LISTENER_SERVICE, filter.id);
        serviceCall.addParameter(REGISTER_EVENT_LISTENER_EVENT_KEY_PARAMETER, filter.key);
        addExtraParameters(parameters, serviceCall);

        Response response = messageEngine.callService(device, serviceCall);
        if (response == null) {
            throw new NotifyException("No response received during register process.");
        } else if (response.getError() != null && !response.getError().isEmpty()) {
            throw new NotifyException(response.getError());
        }
    }

    private void addExtraParameters(Map<String, Object> parameters, Call serviceCall) throws NotifyException {
        if (parameters != null) {
            for (String key : parameters.keySet()) {
                if (key.equalsIgnoreCase(REGISTER_EVENT_LISTENER_EVENT_KEY_PARAMETER))
                    throw new NotifyException("Can't use reserved keys as parameters for registerForEvent");
                serviceCall.addParameter(key, parameters.get(key));
            }
        }
    }

    /**
     * Removes a listener for receiving Notify events and notifies the event driver of its removal.
     *
     * @param listener   Listener to be removed.
     * @param driver     Driver from which the listener must be removed (If not informed all drivers will be considered).
     * @param instanceId InstanceId from the Driver which the listener must be removed (If not informed all instances will be considered).
     * @param eventKey   EventKey from which the listener must be removed (If not informed all events will be considered).
     * @throws NotifyException
     */
    public void unregister(
            UosEventListener listener,
            UpDevice device,
            String driver,
            String instanceId,
            String eventKey) throws NotifyException {

        NotifyException e = null;
        Map<UpDevice, Set<EventFilter>> devices = listenerDao.remove(listener, device, driver, instanceId, eventKey);
        for (Map.Entry<UpDevice, Set<EventFilter>> entry : devices.entrySet()) {
            for (EventFilter filter : entry.getValue()) {
                try {
                    Call serviceCall = new Call(filter.driver, UNREGISTER_LISTENER_SERVICE, filter.id);
                    serviceCall.addParameter(REGISTER_EVENT_LISTENER_EVENT_KEY_PARAMETER, filter.key);
                    Response response = messageEngine.callService(entry.getKey(), serviceCall);
                    if (response == null || (response.getError() != null && !response.getError().isEmpty()))
                        throw new NotifyException(response.getError());
                } catch (NotifyException ex) {
                    logger.log(Level.SEVERE, "Failed to unregister for event: " + filter, ex);
                    e = ex;
                }
            }
        }

        // In case of an error, throw it
        if (e != null)
            throw e;
    }

    public void handleNofify(Notify notify, UpDevice device) {
        Set<UosEventListener> listeners = listenerDao.list(device, notify.getDriver(), notify.getInstanceId(), notify.getEventKey());
        if (listeners.isEmpty()) {
            logger.fine("No listeners waiting for notify events.");
            return;
        }
        for (UosEventListener listener : listeners)
            listener.handleEvent(notify);
    }

    /**
     * If any of the entries in this data object is not null, it acts as a restriction. If it's null, all values
     * for that entry are accepted.
     */
    private static class EventFilter {
        UpDevice device;
        String driver;
        String id;
        String key;

        public EventFilter(UpDevice device, String driver, String id, String key) {
            this.device = device;
            this.driver = driver;
            this.id = id;
            this.key = key;
        }

        public boolean accepts(UpDevice deviceToCheck, String driverToCheck, String idToCheck, String keyToCheck) {
            if ((device != null) && (!device.equals(deviceToCheck)))
                return false;
            if ((driver != null) && (!driver.equals(driverToCheck)))
                return false;
            if ((id != null) && (!id.equals(idToCheck)))
                return false;
            if ((key != null) && (!key.equals(keyToCheck)))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            int hash = chainHashCode(0, this.device);
            hash = chainHashCode(hash, this.driver);
            hash = chainHashCode(hash, this.id);
            hash = chainHashCode(hash, this.key);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof EventFilter))
                return false;

            EventFilter other = (EventFilter) obj;
            return Objects.equals(device, other.device)
                    && Objects.equals(driver, other.driver)
                    && Objects.equals(id, other.id)
                    && Objects.equals(key, other.key);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(device != null ? device.getName() : "*");
            sb.append("/");
            sb.append(driver != null ? driver : "*");
            sb.append("/");
            sb.append(id != null ? id : "*");
            sb.append("/");
            sb.append(key != null ? key : "*");
            return sb.toString();
        }
    }

    private static class ListenerDao {
        private Map<EventFilter, Set<UosEventListener>> data;

        public ListenerDao() {
            data = new HashMap<EventFilter, Set<UosEventListener>>();
        }

        public void insert(UosEventListener listener, EventFilter filter) {
            Set<UosEventListener> listeners = data.get(filter);
            if (listeners == null)
                listeners = new HashSet<UosEventListener>();

            listeners.add(listener);
            data.put(filter, listeners);
        }

        public Set<UosEventListener> list(UpDevice device, String driver, String instanceId, String eventKey) {
            Set<UosEventListener> result = new HashSet<UosEventListener>();

            // Witch entries have a filter that accepts these parameters?
            for (Map.Entry<EventFilter, Set<UosEventListener>> filterToListeners : data.entrySet())
                if (filterToListeners.getKey().accepts(device, driver, instanceId, eventKey))
                    result.addAll(filterToListeners.getValue());

            return result;
        }

        public Map<UpDevice, Set<EventFilter>> remove(UosEventListener listener, UpDevice device, String driver, String instanceId, String eventKey) {
            Map<UpDevice, Set<EventFilter>> result = new HashMap<UpDevice, Set<EventFilter>>();
            EventFilter query = new EventFilter(device, driver, instanceId, eventKey);

            // Witch entries have a filter that accepts these parameters?
            Iterator<Map.Entry<EventFilter, Set<UosEventListener>>> i = data.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<EventFilter, Set<UosEventListener>> entry = i.next();
                EventFilter filter = entry.getKey();
                Set<UosEventListener> filterListeners = entry.getValue();
                if (query.accepts(filter.device, filter.driver, filter.id, filter.key)) {
                    // Removes the listener and stores the device, if not null.
                    if (filterListeners.remove(listener) && (filter.device != null)) {
                        Set<EventFilter> deviceFilters = result.get(filter.device);
                        if (deviceFilters == null)
                            deviceFilters = new HashSet<EventFilter>();
                        deviceFilters.add(filter);
                        result.put(filter.device, deviceFilters);
                    }
                    // Clears empty filter sets.
                    if (filterListeners.isEmpty())
                        i.remove();
                }
            }
            return result;
        }
    }
}
