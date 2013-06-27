package org.unbiquitous.uos.core.adaptabitilyEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.List;

import org.unbiquitous.uos.core.messageEngine.MessageEngine;
import org.unbiquitous.uos.core.messageEngine.MessageEngineException;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;

public class QueueManager {
	
	MessageEngine messageEngine;

	private static final int QUEUE_MAX_SIZE = 100;
	
	private static Map<String, LinkedList<Notify>> queueMap = 
									new HashMap<String, LinkedList<Notify>>();
	private static Map<String, List<UpDevice>> queueSubscribers = 
									new HashMap<String, List<UpDevice>>();
	private static Map<String, Integer> queueSentIndex = 
									new HashMap<String, Integer>();
	
	Thread sender;
	
	public QueueManager(MessageEngine messageEngine){
		this.messageEngine = messageEngine;
		//TODO: initialize queue (from db)
		sender = new Thread(new MessageSender(messageEngine));
	}
	
	public void addMessage(Notify notify, String queueId){
		LinkedList<Notify> targetQueue = queueMap.get(queueId);
		
		if( targetQueue == null ){
			//create and call method createQueue(String queueId)
			targetQueue = new LinkedList<Notify>();
			queueMap.put(queueId, targetQueue);
			queueSubscribers.put(queueId, new ArrayList<UpDevice>());
			queueSentIndex.put(queueId, 0); //TODO: is zero the queue tail?
		}
		
		targetQueue.addFirst(notify);
		
		
		//check queue max size constraint
		adjustQueueSize(targetQueue, QUEUE_MAX_SIZE);
		
		
		//if sender thread not running, run it
		if(!sender.isAlive()){
			sender.start();
		}
		
	}
	
	/**
	 *
	 * Adjust queue size by removing its oldest elements.
	 * 
	 */
	private static void adjustQueueSize(LinkedList<Notify> queue, int size){
		while(queue.size() > size){
			queue.removeLast();
		}
	}
	
	
	/**
	 * 
	 * 
	 * 
	 * @param upDevice
	 * @param queueId
	 * @return false if there is no such queue and true otherwise.
	 */
	public boolean addSubscriber(UpDevice subscriber, String queueId){
		boolean success = true;
		List<UpDevice> subscribers = queueSubscribers.get(queueId);
		
		if(subscribers == null){
			//there is no such queue
			success = false;
		}
		else{
			subscribers.add(subscriber);
			//TODO: Bassani: retreive and send the last X messages to the
			//               subscriber.
		}
		
		return success;
	}
	
	
	private static class MessageSender implements Runnable {
		
		MessageEngine messageEngine;
		
		public MessageSender(MessageEngine messageEngine){
			this.messageEngine = messageEngine;
		}
	    public void run() {
	    	
	    	for(String queueId : queueMap.keySet()){
	    		LinkedList<Notify> currentQueue = queueMap.get(queueId);
	    		ListIterator<Notify> iterator;
	    		int index = queueSentIndex.get(queueId);
	    		iterator = currentQueue.listIterator(index);
	    		
	    		while(iterator.hasNext()){
	    			Notify msg = iterator.next();
	    			
	    			List<UpDevice> subscribersList;
	    			subscribersList = queueSubscribers.get(queueId);
	    			for(UpDevice subscriber : subscribersList){
	    				try{
	    					messageEngine.notifyEvent(msg, subscriber);
	    				}catch (MessageEngineException mee){
	    					//assuming the best case scenario
	    				}
	    			}// for each subscriber
	    			
    				index ++;
    				queueSentIndex.put(queueId, index);
	    		}//while
	    		
	    	}//for each queue
	    }//run()
	}//MessageSender
	
}//QueueManager
