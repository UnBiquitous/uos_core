package org.unbiquitous.uos.core.driver;

import java.util.List;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;

/**
 * This class represents a user information from the middleware view.
 * 
 * @author Tales <talesap@gmail.com>
 * @author Danilo <daniloavilaf@gmail.com>
 * 
 * @version 1.0
 * 
 * @since 2011.11.07
 */
public abstract class UserDriverNativeSupport implements UserDriver {
	private static Logger logger = UOSLogging.getLogger();

	public static UserDriverNativeSupport userDriver;
	
	static {
		System.loadLibrary("TrackerRunnable");
	}

	/* ******************
	 * Natives
	 * ****************** */
	
	/**
	 * Native Method implemented by True System. Start the True System.
	 */
	protected native void startTracker();

	/**
	 * Native Method implemented by True System. Stops the True System.
	 */
	protected native void stopTracker();

	/**
	 * Native Method implemented by True System. Train the True System.
	 */
	protected native void train();

	/**
	 * Native Method implemented by True System. Save user images in True System
	 * data, for future recognition.
	 * 
	 * @param id
	 * @param index
	 * @param image
	 * @return <code>true</code> caso tenha conseguido salvar e
	 *         <code>false</code> caso contrario
	 */
	protected native boolean saveImage(String id, int index, byte[] image);

	/**
	 * Native Method implemented by True System. Remove user images in True
	 * System data, for future recognition.
	 * 
	 * @param id
	 * @return <code>true</code> caso tenha conseguido remover e
	 *         <code>false</code> caso contrario
	 */
	protected native boolean removeUser(String id);

	/**
	 * Native Method implemented by True System. Return knowns users by True
	 * System
	 * 
	 * @return knownUsers
	 */
	protected native List<String> listUsers();

	/* ******************
	 * Auxiliar methods for natives call
	 * ****************** */
	
	/**
	 * Used internally to update user information. If the user identity is
	 * changed, a new parameter containing the last label is added to the
	 * notification event generated.
	 * 
	 * @param label
	 * @param lastlabel
	 * @param confidence
	 * @param positionX
	 * @param positionY
	 * @param positionZ
	 */
	protected abstract void registerRecheckUserEvent(String label, String lastLabel, float confidence, float positionX, float positionY, float positionZ);

	/**
	 * Used for register user in the scene.
	 * 
	 * @param label
	 * @param confidence
	 * @param positionX
	 * @param positionY
	 * @param positionZ
	 */
	protected abstract void registerNewUserEvent(String label, float confidence, float positionX, float positionY, float positionZ);

	/**
	 * Used internally to record a loss of users
	 * 
	 * @param name
	 */
	protected abstract void registerLostUserEvent(String label);
	
	/**
	 * Clear the driver buffer
	 */
	protected abstract void clear();
	
	/* ******************
	 * Runnables
	 * ****************** */
	
	/**
	 * Thread that runs in the background to keep updated with the middleware
	 * information on the context.
	 */
	protected class Tracker extends Thread {
		private native void doTracker();

		private boolean canContinue;
		
		@Override
		public void run() {
			canContinue = true;
			while (canContinue) {
				doTracker();
			}
		}
		
		public void end() {
			canContinue = false;
		}
	}
	
	/**
	 * Thread that runs in the background to check process is alive
	 */
	protected class Daemon extends Thread {
		private native boolean isProcessRunning();
		private static final int TIME_IN_SLEEP = 5000;

		private boolean canContinue;
		
		@Override
		public void run() {
			while (canContinue) {
				if (!isProcessRunning()) {
					clear();
					stopTracker();
					sleep();
					startTracker();
				}
				
				sleep();
			}
		}

		private void sleep() {
			try {
				Thread.sleep(TIME_IN_SLEEP);
			} catch (InterruptedException e) {
				logger.severe(e.getMessage());
			}
		}
		
		public void end() {
			canContinue = false;
		}
	}

}