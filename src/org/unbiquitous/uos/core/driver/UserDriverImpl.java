package org.unbiquitous.uos.core.driver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.NotifyException;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.unbiquitous.uos.core.network.model.NetworkDevice;

/**
 * This class represents a user information from the middleware view.
 * 
 * @author Tales <talesap@gmail.com>
 * @author Danilo <daniloavilaf@gmail.com>
 * 
 * @version 1.0
 * 
 * @since 2011.10.01
 */
public class UserDriverImpl extends UserDriverNativeSupport {

	private static Logger logger = UOSLogging.getLogger();

	private volatile Tracker tracker;
	private volatile Daemon daemon;
	private volatile Map<String, JSONObject> labels;
	private Gateway gateway;
	private String instanceId;
	private List<UpNetworkInterface> newUserListenerDevices;
	private List<UpNetworkInterface> changeInformationListenerDevices;
	private List<UpNetworkInterface> lostUserListenerDevices;

	private static final int TIME_IN_SLEEP_BEFORE_START = 500;

	public UserDriverImpl() {
		userDriver = this;
	}

	public static UserDriverImpl getInstance() {
		if (userDriver == null)
			userDriver = new UserDriverImpl();
		return (UserDriverImpl) userDriver;
	}

	/**
	 * Constructor
	 */
	@Override
	public void init(Gateway gateway, String instanceId) {
		this.gateway = gateway;
		this.instanceId = instanceId;
		this.newUserListenerDevices = new ArrayList<UpNetworkInterface>();
		this.changeInformationListenerDevices = new ArrayList<UpNetworkInterface>();
		this.lostUserListenerDevices = new ArrayList<UpNetworkInterface>();
		this.labels = new HashMap<String, JSONObject>();

		// starting the TRUE system
		startTracker();

		// starting a new thread to handle the incoming messages of events from
		// the TRUE system
		if (this.tracker == null) {
			this.tracker = new Tracker();
			this.tracker.setName("TrackerRunnable");
			this.tracker.start();
		}

		if (this.daemon == null) {
			this.daemon = new Daemon();
			this.daemon.setName("Daemon");
			this.daemon.start();
		}
	}

	/**
	 * Destructor
	 */
	@Override
	public void destroy() {
		stopTracker();
		this.tracker.end();
		this.daemon.end();
		// FIXME : UserDriver TRUE : message queue used to communicate the driver with the True
		// system in not being killed
	}

	/**
	 * Register the driver services and events.
	 */
	@Override
	public UpDriver getDriver() {
		UpDriver driver = new UpDriver(USER_DRIVER);

		driver.addService("retrieveUserInfo").addParameter(EMAIL_PARAM, ParameterType.MANDATORY).addParameter(SPECIFIC_FIELD_PARAM, ParameterType.OPTIONAL);

		driver.addService("registerListener").addParameter(EVENT_KEY_PARAM, ParameterType.MANDATORY);

		driver.addService("unregisterListener").addParameter(EVENT_KEY_PARAM, ParameterType.OPTIONAL);

		driver.addService("saveUserImage").addParameter(NAME_PARAM, ParameterType.MANDATORY).addParameter(EMAIL_PARAM, ParameterType.MANDATORY)
				.addParameter(INDEX_IMAGE_PARAM, ParameterType.MANDATORY).addParameter(LENGTH_IMAGE_PARAM, ParameterType.MANDATORY);

		driver.addService("removeUserImages").addParameter(NAME_PARAM, ParameterType.MANDATORY).addParameter(EMAIL_PARAM, ParameterType.MANDATORY);

		driver.addService("listKnownUsers");

		driver.addService("retrain");

		UpService newUserEvent = new UpService(NEW_USER_EVENT_KEY);
		driver.addEvent(newUserEvent);

		UpService lostUserEvent = new UpService(LOST_USER_EVENT_KEY);
		driver.addEvent(lostUserEvent);

		UpService updateUserEvent = new UpService(CHANGE_INFORMATION_TO_USER_KEY);
		driver.addEvent(updateUserEvent);

		return driver;
	}

	/**
	 * Retrieve information of the user with email
	 * 
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void retrieveUserInfo(Call serviceCall, Response serviceResponse, CallContext messageContext) {

		String email = (String) serviceCall.getParameter(EMAIL_PARAM);

		JSONObject returned_user = null;

		returned_user = labels.get(email);
		String specificField = (String) serviceCall.getParameter(SPECIFIC_FIELD_PARAM);

		if (specificField != null && returned_user != null) {
			try {
				Object specificValue = returned_user.get(specificField);
				returned_user = new JSONObject();
				returned_user.put(specificField, specificValue);
			} catch (JSONException e) {
				throw new RuntimeException("Json Error", e);
			}
		}

		if (returned_user == null)
			serviceResponse.addParameter(USER_PARAM, null);
		else
			serviceResponse.addParameter(USER_PARAM, returned_user.toString());
	}

	/**
	 * Save image for the user with email in parameter
	 * 
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void saveUserImage(Call serviceCall, Response serviceResponse, CallContext messageContext) {
		String name = (String) serviceCall.getParameter(NAME_PARAM);
		String email = (String) serviceCall.getParameter(EMAIL_PARAM);
		Integer index = Integer.parseInt((String) serviceCall.getParameter(INDEX_IMAGE_PARAM));
		Integer length = Integer.parseInt((String) serviceCall.getParameter(LENGTH_IMAGE_PARAM));

		ReceiveImageData receiveImageData = new ReceiveImageData(name, email, index, length, messageContext);

		new Thread(receiveImageData).start();

		serviceResponse.addParameter(RETURN_PARAM, "Get stream and send frame.");
	}

	/**
	 * Remove images of user
	 * 
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void removeUserImages(Call serviceCall, Response serviceResponse, CallContext messageContext) {
		String name = (String) serviceCall.getParameter(NAME_PARAM);
		String email = (String) serviceCall.getParameter(EMAIL_PARAM);
		String id = name + SPECIAL_CHARACTER_SEPARATOR + email;

		boolean result = removeUser(id);

		if (!result) {
			serviceResponse.setError("Not possible to remove the user. Try again.");
		}
	}

	/**
	 * List all known users
	 * 
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void listKnownUsers(Call serviceCall, Response serviceResponse, CallContext messageContext) {
		List<String> users = listUsers();

		List<JSONObject> userJson = new ArrayList<JSONObject>(users.size());

		try {
			for (String string : users) {
				String[] split = string.split(SPECIAL_CHARACTER_SEPARATOR);

				JSONObject jsonObject = new JSONObject();
				jsonObject.put(NAME_PARAM, split[0]);
				jsonObject.put(EMAIL_PARAM, split[1]);

				userJson.add(jsonObject);
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		serviceResponse.addParameter(RETURN_PARAM, userJson.toString());
	}

	/**
	 * Retrain the recognition algorithm
	 * 
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	public void retrain(Call serviceCall, Response serviceResponse, CallContext messageContext) {
		train();
		stopTracker();
		try {
			Thread.sleep(TIME_IN_SLEEP_BEFORE_START);
		} catch (InterruptedException e) {
			logger.severe(e.getMessage());
		}
		startTracker();
	}

	/**
	 * Register listener for the driver events.
	 */
	@Override
	public void registerListener(Call serviceCall, Response serviceResponse, CallContext messageContext) {
		NetworkDevice networkDevice = messageContext.getCallerDevice();
		UpNetworkInterface networkInterface = new UpNetworkInterface(networkDevice.getNetworkDeviceType(), networkDevice.getNetworkDeviceName());

		String eventKey = (String) serviceCall.getParameter(EVENT_KEY_PARAM);

		if (NEW_USER_EVENT_KEY.equals(eventKey)) {
			if (!newUserListenerDevices.contains(networkInterface)) {
				newUserListenerDevices.add(networkInterface);
			}
		} else if (CHANGE_INFORMATION_TO_USER_KEY.equals(eventKey)) {
			if (!changeInformationListenerDevices.contains(networkInterface)) {
				changeInformationListenerDevices.add(networkInterface);
			}
		} else if (LOST_USER_EVENT_KEY.equals(eventKey)) {
			if (!lostUserListenerDevices.contains(networkInterface)) {
				lostUserListenerDevices.add(networkInterface);
			}
		}
	}

	/**
	 * Unregister listeners for the drivers events.
	 */
	@Override
	public void unregisterListener(Call serviceCall, Response serviceResponse, CallContext messageContext) {
		NetworkDevice networkDevice = messageContext.getCallerDevice();
		UpNetworkInterface networkInterface = new UpNetworkInterface(networkDevice.getNetworkDeviceType(), networkDevice.getNetworkDeviceName());

		String eventKey = (String) serviceCall.getParameter(EVENT_KEY_PARAM);

		if (eventKey == null) {
			newUserListenerDevices.remove(networkInterface);
			changeInformationListenerDevices.remove(networkInterface);
			lostUserListenerDevices.remove(networkInterface);
		} else if (NEW_USER_EVENT_KEY.equals(eventKey)) {
			newUserListenerDevices.remove(networkInterface);
		} else if (CHANGE_INFORMATION_TO_USER_KEY.equals(eventKey)) {
			changeInformationListenerDevices.remove(networkInterface);
		} else if (LOST_USER_EVENT_KEY.equals(eventKey)) {
			lostUserListenerDevices.remove(networkInterface);
		}
	}

	/**
	 * Used internally to update user information. If the user identity is changed, a new parameter containing the last label is added to the notification event generated.
	 * 
	 * @param label
	 * @param lastlabel
	 * @param confidence
	 * @param positionX
	 * @param positionY
	 * @param positionZ
	 */
	synchronized protected void registerRecheckUserEvent(String label, String lastLabel, float confidence, float positionX, float positionY, float positionZ) {
		try {
			String name = extractNameFromLabel(label);
			String email = extractEmailFromLabel(label);

			JSONObject user = createJson(name, email, confidence, positionX, positionY, positionZ);
			labels.put(email, user);

			Notify notify = createNotify(CHANGE_INFORMATION_TO_USER_KEY, user);
			if (lastLabel != null && !label.equals(lastLabel)) {
				getUserDriver().labels.remove(email);
				notify.addParameter(LAST_LABEL_NAME, extractNameFromLabel(lastLabel));
				notify.addParameter(LAST_LABEL_EMAIL, extractEmailFromLabel(lastLabel));
			}

			getUserDriver().notifyAllListerners(notify);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Used for register user in the scene.
	 * 
	 * @param label
	 * @param confidence
	 * @param positionX
	 * @param positionY
	 * @param positionZ
	 */
	synchronized protected void registerNewUserEvent(String label, float confidence, float positionX, float positionY, float positionZ) {
		try {
			String name = extractNameFromLabel(label);
			String email = extractEmailFromLabel(label);

			JSONObject user = createJson(name, email, confidence, positionX, positionY, positionZ);
			labels.put(email, user);

			Notify notify = createNotify(NEW_USER_EVENT_KEY, user);
			getUserDriver().notifyAllListerners(notify);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Used internally to record a loss of users
	 * 
	 * @param name
	 */
	synchronized protected void registerLostUserEvent(String label) {
		String email = extractEmailFromLabel(label);

		try {
			Notify notify = createNotify(LOST_USER_EVENT_KEY, labels.remove(email));
			getUserDriver().notifyAllListerners(notify);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

		labels.remove(email);
	}

	/**
	 * Create a notification and add specific parameters.
	 * 
	 * @param eventKey
	 * @param user
	 * @return
	 * @throws JSONException
	 */
	private Notify createNotify(String eventKey, JSONObject user) throws JSONException {
		Notify notify = new Notify(eventKey, USER_DRIVER, instanceId);

		notify.addParameter(EMAIL_PARAM, user.getString(EMAIL_PARAM));
		notify.addParameter(NAME_PARAM, user.getString(NAME_PARAM));
		notify.addParameter(CONFIDENCE_PARAM, user.getString(CONFIDENCE_PARAM));
		notify.addParameter(POSITION_X_PARAM, user.getString(POSITION_X_PARAM));
		notify.addParameter(POSITION_Y_PARAM, user.getString(POSITION_Y_PARAM));
		notify.addParameter(POSITION_Z_PARAM, user.getString(POSITION_Z_PARAM));

		return notify;
	}

	/**
	 * Notify all listeners when a certain event occurs.
	 * 
	 * @param notify
	 */
	private void notifyAllListerners(Notify notify) {
		List<UpNetworkInterface> listenerDevices = null;

		if (NEW_USER_EVENT_KEY.equals(notify.getEventKey())) {
			listenerDevices = newUserListenerDevices;
		} else if (CHANGE_INFORMATION_TO_USER_KEY.equals(notify.getEventKey())) {
			listenerDevices = changeInformationListenerDevices;
		} else if (LOST_USER_EVENT_KEY.equals(notify.getEventKey())) {
			listenerDevices = lostUserListenerDevices;
		}

		for (UpNetworkInterface networkInterface : listenerDevices) {
			UpDevice device = new UpDevice("Anonymous"); // TODO ??
			device.addNetworkInterface(networkInterface.getNetworkAddress(), networkInterface.getNetType());

			try {
				this.gateway.sendEventNotify(notify, device);
			} catch (NotifyException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Extracts of the email received label.
	 * 
	 * The label usually has "<name>:<email>" but can has "<email>"
	 * 
	 * @param label
	 * @return email
	 */
	private static String extractEmailFromLabel(String label) {
		String email;

		String[] labelPartida = label.split(SPECIAL_CHARACTER_SEPARATOR);

		if (labelPartida.length > 1)
			email = labelPartida[1];
		else
			email = labelPartida[0];
		return email;
	}

	/**
	 * Extracts of the name received label.
	 * 
	 * The label usually has "<name>:<email>" but can has "<email>"
	 * 
	 * @param label
	 * @return email
	 */
	private static String extractNameFromLabel(String label) {
		return label.split(SPECIAL_CHARACTER_SEPARATOR)[0];
	}

	/**
	 * Create the json object represent of user information
	 * 
	 * @param name
	 * @param email
	 * @param confidence
	 * @param positionX
	 * @param positionY
	 * @param positionZ
	 * @return json object
	 * @throws JSONException
	 */
	private JSONObject createJson(String name, String email, float confidence, float positionX, float positionY, float positionZ) throws JSONException {
		JSONObject user = new JSONObject();

		user.put(NAME_PARAM, name);
		user.put(EMAIL_PARAM, email);
		user.put(CONFIDENCE_PARAM, confidence);
		user.put(POSITION_X_PARAM, positionX);
		user.put(POSITION_Y_PARAM, positionY);
		user.put(POSITION_Z_PARAM, positionZ);

		return user;
	}

	private class ReceiveImageData implements Runnable {
		private String name;
		private String email;
		private Integer index;
		private Integer length;
		private CallContext messageContext;

		public ReceiveImageData(String name, String email, Integer index, Integer length, CallContext messageContext) {
			this.name = name;
			this.email = email;
			this.index = index;
			this.length = length;
			this.messageContext = messageContext;
		}

		@Override
		public void run() {
			try {
				int channel = 0;

				DataInputStream in = messageContext.getDataInputStream(channel);
				DataOutputStream out = messageContext.getDataOutputStream(channel);

				while (in.available() < length) {
					Thread.sleep(100);
				}

				byte imageData[] = new byte[length];

				in.read(imageData);

				String id = name + SPECIAL_CHARACTER_SEPARATOR + email;

				boolean result = saveImage(id, index, imageData);

				if (!result)
					out.write("Not possible to save the image of user. Try again.".getBytes());
				else
					out.write("Image of user saved.".getBytes());
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * Clear the driver buffer
	 */
	@Override
	protected void clear() {
		labels.clear();
	}

	private UserDriverImpl getUserDriver() {
		return (UserDriverImpl) userDriver;
	}

	public List<UpDriver> getParent() {
		return null;
	}

}