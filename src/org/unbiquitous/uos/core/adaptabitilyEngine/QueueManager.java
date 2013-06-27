package org.unbiquitous.uos.core.adaptabitilyEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.List;

import org.unbiquitous.uos.core.messageEngine.MessageEngine;
import org.unbiquitous.uos.core.messageEngine.MessageEngineException;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;

public class QueueManager {
	
	MessageEngine messageEngine;

	private static Map<String, Queue<Notify>> queueMap = 
										new HashMap<String, Queue<Notify>>();
	private static Map<String, List<UpDevice>> queueSubscribers = 
										new HashMap<String, List<UpDevice>>();
	
	Thread sender;
	
	public QueueManager(MessageEngine messageEngine){
		this.messageEngine = messageEngine;
		//TODO: initialize queue (from db)
		sender = new Thread(new MessageSender(messageEngine));
	}
	
	public void addMessage(Notify notify, String queueId){
		Queue<Notify> targetQueue = queueMap.get(queueId);
		
		if( targetQueue == null ){
			targetQueue = new LinkedList<Notify>();
			queueMap.put(queueId, targetQueue);
		}
		
		targetQueue.add(notify);
		
		//if sender thread not running, run it
		if(!sender.isAlive()){
			sender.start();
		}
		
	}
	
	
	private static class MessageSender implements Runnable {
		
		MessageEngine messageEngine;
		
		public MessageSender(MessageEngine messageEngine){
			this.messageEngine = messageEngine;
		}
	    public void run() {
	    	
	    	for(String queueId : queueMap.keySet()){
	    		Queue<Notify> currentQueue = queueMap.get(queueId);
	    		while(!currentQueue.isEmpty()){
	    			boolean removeFromQueue = true;
	    			Notify msg = currentQueue.peek();
	    			
	    			List<UpDevice> subscribersList;
	    			subscribersList = queueSubscribers.get(queueId);
	    			for(UpDevice subscriber : subscribersList){
	    				try{
	    					messageEngine.notifyEvent(msg, subscriber);
	    				}catch (MessageEngineException mee){
	    					removeFromQueue = false;
	    				}
	    			}
	    			
	    			//if all goes fine remove from the queue
	    			if(removeFromQueue){
	    				currentQueue.poll();
	    			}
	    		}
	    		
	    	}
	    }
	}
	
}
