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
package org.eclipse.osee.framework.database.internal.core;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.database.core.IConnectionFactory;
import org.eclipse.osee.framework.database.core.OseeConnection;
import org.eclipse.osee.framework.database.internal.Activator;
import org.eclipse.osee.framework.database.internal.IDbConnectionFactory;
import org.eclipse.osee.framework.logging.OseeLog;

public class OseeConnectionPoolImpl {
   private static final int MAX_CONNECTIONS_PER_CLIENT = Math.max(8, 2 * Runtime.getRuntime().availableProcessors());
   private final List<OseeConnectionImpl> connections = new CopyOnWriteArrayList<OseeConnectionImpl>();
   private final String dbUrl;
   private final Properties properties;
   private final IDbConnectionFactory connectionFactory;
   private final String driver;

   public OseeConnectionPoolImpl(IDbConnectionFactory connectionFactory, String driver, String dbUrl, Properties properties) {
      this.connectionFactory = connectionFactory;
      this.driver = driver;
      this.dbUrl = dbUrl;
      this.properties = properties;
   }

   private IConnectionFactory createConnection(String driver) throws OseeCoreException {
      return connectionFactory.get(driver);
   }

   public synchronized boolean hasOpenConnection() {
      return connections.size() > 0;
   }

   /**
    * at a minimum this should be called on jvm shutdown
    */
   public synchronized void closeConnections() {
      for (OseeConnection connection : connections) {
         connection.close();
      }
      connections.clear();
   }

   synchronized void removeConnection(OseeConnection conn) {
      connections.remove(conn);
   }

   public synchronized OseeConnectionImpl getConnection() throws OseeDataStoreException {
      for (OseeConnectionImpl connection : connections) {
         if (connection.lease()) {
            return connection;
         }
      }

      if (connections.size() >= MAX_CONNECTIONS_PER_CLIENT) {
         throw new OseeDataStoreException(
               "This client has reached the maximum number of allowed simultaneous database connections of : " + MAX_CONNECTIONS_PER_CLIENT);
      }
      try {
         OseeConnectionImpl connection = getOseeConnection();
         connections.add(connection);
         OseeLog.log(Activator.class, Level.INFO, String.format("DbConnection: [%s] - [%d]", dbUrl, connections.size()));
         return connection;
      } catch (Throwable th) {
         throw new OseeDataStoreException("Unable to get a database connection: ", th);
      }
   }

   private OseeConnectionImpl getOseeConnection() throws Exception {
      IConnectionFactory connectionDriver = createConnection(driver);
      Connection connection = connectionDriver.getConnection(properties, dbUrl);
      connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      return new OseeConnectionImpl(connection, this);
   }

   synchronized void returnConnection(OseeConnectionImpl connection) {
      try {
         if (connection.isClosed()) {
            removeConnection(connection);
         } else {
            connection.expireLease();
         }
      } catch (OseeDataStoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         removeConnection(connection);
      }
   }

   synchronized void releaseUneededConnections() throws OseeDataStoreException {
      for (OseeConnectionImpl connection : connections) {
         if (connection.isStale()) {
            connection.destroy();
         }
      }
   }
}