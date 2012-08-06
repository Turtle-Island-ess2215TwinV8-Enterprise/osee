package org.eclipse.osee.framework.messaging.internal;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.osee.framework.messaging.MessageID;
import org.eclipse.osgi.framework.console.CommandInterpreter;

public class ConsoleDebugSupport {

	private boolean printSends;
	private boolean printReceives;
	private Map<MessageID, Stats> sends;
	private Map<String, Stats> receives;
	
	public ConsoleDebugSupport(){
		sends = new ConcurrentHashMap<MessageID, Stats>();
		receives = new ConcurrentHashMap<String, Stats>();
	}

	protected void setPrintSends(boolean printSends){
		this.printSends = printSends;
	}
	
	public boolean getPrintSends() {
		return printSends;
	}

	public boolean getPrintReceives() {
		return printReceives;
	}

	public void setPrintReceives(boolean printReceives) {
		this.printReceives = printReceives;
	}

	public void addSend(MessageID messageId) {
		Stats stats = sends.get(messageId);
		if(stats == null){
			stats = new Stats();
			sends.put(messageId, stats);
		}
		stats.add(messageId);
	}

	public void addReceive(Message jmsMessage) {
		String id = null;
		try{
			id = jmsMessage.getJMSMessageID();
		} catch (JMSException ex){
			ex.printStackTrace();
		}
		if(id != null){
			Stats stats = receives.get(id);
			if(stats == null){
				stats = new Stats();
				receives.put(id, stats);
			}
			stats.add(jmsMessage);
		}
	}
	
	private class Stats {
		private int count = 0;
		private Date lastReceipt;
		
		public void add(MessageID messageId) {
			lastReceipt = new Date();
			count++;
		}

		public void add(Message jmsMessage) {
			lastReceipt = new Date();
			count++;
		}
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append(count);
			sb.append(" : ");
			sb.append(lastReceipt);
			return sb.toString();
		}
	}

	public void printAllStats(CommandInterpreter ci) {
		printTxStats(ci);
		printRxStats(ci);
	}

	public void printTxStats(CommandInterpreter ci) {
		ci.println("TxStats:");
		for(MessageID id:sends.keySet()){
			Stats status = sends.get(id);
			ci.println(id);
			ci.println(status);
			ci.println("------------------------------");
		}
	}

	public void printRxStats(CommandInterpreter ci) {
		ci.println("RxStats:");
		for(String id:receives.keySet()){
			Stats status = sends.get(id);
			ci.println(id);
			ci.println(status);
			ci.println("------------------------------");
		}
	}

}