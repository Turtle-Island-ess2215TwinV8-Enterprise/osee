/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ote.client.msg.core.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.eclipse.osee.connection.service.IServiceConnector;
import org.eclipse.osee.framework.jdk.core.util.network.PortUtil;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.plugin.core.util.ExportClassLoader;
import org.eclipse.osee.ote.client.msg.IOteMessageService;
import org.eclipse.osee.ote.client.msg.core.IMessageSubscription;
import org.eclipse.osee.ote.client.msg.core.db.AbstractMessageDataBase;
import org.eclipse.osee.ote.message.Message;
import org.eclipse.osee.ote.message.MessageDefinitionProvider;
import org.eclipse.osee.ote.message.commands.RecordCommand;
import org.eclipse.osee.ote.message.commands.RecordCommand.MessageRecordDetails;
import org.eclipse.osee.ote.message.enums.DataType;
import org.eclipse.osee.ote.message.interfaces.IMsgToolServiceClient;
import org.eclipse.osee.ote.message.interfaces.IRemoteMessageService;
import org.eclipse.osee.ote.message.interfaces.ITestEnvironmentMessageSystem;
import org.eclipse.osee.ote.message.tool.IFileTransferHandle;
import org.eclipse.osee.ote.message.tool.TransferConfig;
import org.eclipse.osee.ote.message.tool.UdpFileTransferHandler;
import org.eclipse.osee.ote.service.ConnectionEvent;
import org.eclipse.osee.ote.service.IOteClientService;
import org.eclipse.osee.ote.service.ITestConnectionListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Ken J. Aguilar
 */
public class MessageSubscriptionService implements IOteMessageService, ITestConnectionListener, IMsgToolServiceClient {

   /** * Static Fields ** */
   private static final int MAX_CONCURRENT_WORKER_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 4);

   private final InetAddress localAddress;
   private final List<MessageSubscription> subscriptions = new CopyOnWriteArrayList<MessageSubscription>();
   private IMsgToolServiceClient exportedThis = null;
   private AbstractMessageDataBase msgDatabase;
   private UdpFileTransferHandler fileTransferHandler;

   private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CONCURRENT_WORKER_THREADS,
      new ThreadFactory() {
         private final ThreadGroup group =
            new ThreadGroup(Thread.currentThread().getThreadGroup(), "Msg Watch Workers");
         private int count = 1;

         @Override
         public Thread newThread(Runnable arg0) {
            Thread thread = new Thread(group, arg0, "Msg Watch Wrkr - " + count++);
            thread.setDaemon(false);
            return thread;
         }
      });

   /**
    * Monitors a set of channels for message updates and dispatches the updates to worker threads
    */
   private UpdateDispatcher dispatcher = null;
   private IRemoteMessageService service;

   private IOteClientService clientService;

   public void start(){
	   clientService.addConnectionListener(this);
   }
   
   public void stop(){
	   clientService.removeConnectionListener(this);
   }
   
   public void bindOteClientService(IOteClientService clientService){
	   this.clientService = clientService;
   }
   
   public void unbindOteClientService(IOteClientService clientService){
	   this.clientService = null;
   }
   
   public MessageSubscriptionService() throws IOException {
      localAddress = InetAddress.getLocalHost();
      msgDatabase = new MessageDatabase();
      OseeLog.log(Activator.class, Level.INFO,
         "OTE client message service started on: " + localAddress.getHostAddress());
   }

   @Override
   public synchronized IMessageSubscription subscribe(String name) {
      MessageSubscription subscription = new MessageSubscription(this);
      subscription.bind(name);
      if (msgDatabase != null) {
         subscription.attachMessageDb(msgDatabase);
         if (service != null) {
            subscription.attachService(service);
         }
      }
      subscriptions.add(subscription);
      return subscription;
   }

   /**
    * Shuts down the client message service. All worker threads will be terminated and all IO resources will be closed.
    */
   public void shutdown() {
      OseeLog.log(MessageSubscriptionService.class, Level.INFO, "shutting down subscription service");
      clientService.removeConnectionListener(this);
      shutdownDispatcher();
      threadPool.shutdown();
      try {
         threadPool.awaitTermination(5, TimeUnit.SECONDS);
      } catch (InterruptedException ex1) {
         OseeLog.log(Activator.class, Level.WARNING, ex1.toString(), ex1);
      }
   }

   @Override
   public synchronized void onConnectionLost(IServiceConnector connector) {
      OseeLog.log(Activator.class, Level.INFO, "connection lost: ote client message service halted");
      shutdownDispatcher();
      msgDatabase.detachService(null);
      for (MessageSubscription subscription : subscriptions) {
         subscription.detachService(null);
      }
      exportedThis = null;
      service = null;
   }

   @Override
   public synchronized void onPostConnect(ConnectionEvent event) {
      assert msgDatabase != null;
      OseeLog.log(Activator.class, Level.INFO, "connecting OTE client message service");
      if (event.getEnvironment() instanceof ITestEnvironmentMessageSystem) {
         ITestEnvironmentMessageSystem env = (ITestEnvironmentMessageSystem) event.getEnvironment();
         try {
            service = env.getMessageToolServiceProxy();
            if (service == null) {
               throw new Exception("could not get message tool service proxy");
            }
            exportedThis = (IMsgToolServiceClient) event.getConnector().export(this);
         } catch (Exception e) {
        	String message = String.format(
        			"failed to create exported Message Tool Client. Connector class is %s", 
        			event.getConnector().getClass().getName());
            OseeLog.log(MessageSubscriptionService.class, Level.SEVERE,
               message, e);
            service = null;
            exportedThis = null;
            return;
         }

         try {
            dispatcher = new UpdateDispatcher(service.getMsgUpdateSocketAddress());
         } catch (Exception e) {
            OseeLog.log(MessageSubscriptionService.class, Level.SEVERE, "failed to create update dispatcher", e);
            service = null;
            exportedThis = null;
            return;
         }

         try {
            createProccessors();
         } catch (Exception e) {
            OseeLog.log(MessageSubscriptionService.class, Level.SEVERE, "failed to create update processors", e);
            service = null;
            exportedThis = null;
            return;
         }

         msgDatabase.attachToService(service, exportedThis);
         for (MessageSubscription subscription : subscriptions) {
            subscription.attachService(service);
         }
         dispatcher.start();
      }
   }

   private void createProccessors() throws IOException {
      Set<? extends DataType> availableTypes = service.getAvailablePhysicalTypes();

      for (DataType type : availableTypes) {
         final ChannelProcessor handler =
            new ChannelProcessor(1, type.getToolingBufferSize(), threadPool, msgDatabase, type);
         dispatcher.addChannel(localAddress, 0, type, handler);
      }
   }

   private void shutdownDispatcher() {
      if (dispatcher != null && dispatcher.isRunning()) {
         try {
            dispatcher.close();
         } catch (Throwable ex) {
            OseeLog.log(MessageSubscriptionService.class, Level.WARNING, "exception while closing down dispatcher", ex);
         } finally {
            dispatcher = null;
         }
      }
   }

   @Override
   public synchronized void onPreDisconnect(ConnectionEvent event) {
      if (service == null) {
         return;
      }
      msgDatabase.detachService(service);
      for (MessageSubscription subscription : subscriptions) {
         subscription.detachService(service);
      }
      try {
         event.getConnector().unexport(this);
      } catch (Exception e) {
         OseeLog.log(MessageSubscriptionService.class, Level.WARNING, "problems unexporting Message Tool Client", e);
      }
      shutdownDispatcher();
      exportedThis = null;
      service = null;
   }

   @Override
   public void changeIsScheduled(String msgName, boolean isScheduled) throws RemoteException {

   }

   @Override
   public void changeRate(String msgName, double rate) throws RemoteException {

   }

   @Override
   public InetSocketAddress getAddressByType(String messageName, DataType dataType) throws RemoteException {
      final DatagramChannel channel = dispatcher.getChannel(dataType);
      OseeLog.logf(Activator.class, Level.INFO, 
         "callback from remote msg manager: msg=%s, type=%s, ip=%s:%d\n", messageName, dataType.name(),
         localAddress.toString(), channel.socket().getLocalPort());

      return new InetSocketAddress(localAddress, channel.socket().getLocalPort());
   }

   @Override
   public UUID getTestSessionKey() throws RemoteException {
      return clientService.getSessionKey();
   }

   public void addMessageDefinitionProvider(MessageDefinitionProvider provider){
	   for (MessageSubscription subscription : subscriptions) {
		   if(!subscription.isResolved()){
			   subscription.attachMessageDb(msgDatabase);
		   }
	   }
   }
   
   public void removeMessageDefinitionProvider(MessageDefinitionProvider provider){
	   for (MessageSubscription subscription : subscriptions) {
		   if(subscription.isResolved()){
			   Class<? extends Message> msg = null;
			   Bundle hostBundle = null;
			   try {
				   msg = ExportClassLoader.getInstance().loadClass(subscription.getMessageClassName()).asSubclass(Message.class);
				   hostBundle = FrameworkUtil.getBundle(msg.getClass());
			   } catch (ClassNotFoundException e) {
			   } finally{
				   if(msg == null || hostBundle == null){
					   subscription.detachMessageDb(msgDatabase);
				   }
			   }
		   }
	   }
   }
   
   @Override
   public synchronized IFileTransferHandle startRecording(String fileName, List<MessageRecordDetails> list) throws FileNotFoundException, IOException {
      if (service == null) {
         throw new IllegalStateException("can't record: not connected to test server");
      }
      if (fileTransferHandler == null) {
         fileTransferHandler = new UdpFileTransferHandler();
         fileTransferHandler.start();
      }
      int port = PortUtil.getInstance().getValidPort();
      // get the address of the socket the message recorder is going to write
      // data to
      InetSocketAddress recorderOutputAddress = service.getRecorderSocketAddress();

      // setup a transfer from a socket to a file
      TransferConfig config =
         new TransferConfig(fileName, recorderOutputAddress, new InetSocketAddress(InetAddress.getLocalHost(), port),
            TransferConfig.Direction.SOCKET_TO_FILE, 128000);
      IFileTransferHandle handle = fileTransferHandler.registerTransfer(config);

      // send the command to start recording
      RecordCommand cmd =
         new RecordCommand(exportedThis, new InetSocketAddress(InetAddress.getLocalHost(), port), list);
      service.startRecording(cmd);
      OseeLog.log(
         Activator.class,
         Level.INFO,
         "recording started with " + list.size() + " entries, recorder output socket=" + recorderOutputAddress.toString());
      return handle;
   }

   @Override
   public synchronized void stopRecording() throws RemoteException, IOException {
      try {
         service.stopRecording();
      } finally {
         if (fileTransferHandler != null && fileTransferHandler.hasActiveTransfers()) {
            fileTransferHandler.stopAllTransfers();
         }
         fileTransferHandler = null;
      }
   }

   public AbstractMessageDataBase getMsgDatabase() {
      return msgDatabase;
   }

   public IRemoteMessageService getService() {
      return service;
   }

   public void removeSubscription(MessageSubscription subscription) {
      subscriptions.remove(subscription);
   }
}
