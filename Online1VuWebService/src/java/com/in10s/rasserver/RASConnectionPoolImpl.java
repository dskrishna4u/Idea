package com.in10s.rasserver;

import com.in10s.onevu.CRSOnline1VuWebService;
import com.in10s.onevu.CRSPropertyFileAndDbReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;

public class RASConnectionPoolImpl implements Runnable, RASConnectionPool {

	private int m_nMaxConnections;
	private boolean m_bwaitIfBusy;
	private List<RASConnection> m_availableConnections, m_busyConnections;
	private boolean m_bconnectionPending = false;
        private int m_nGetConnectionTimeout = 300000;
        private boolean m_bTrace = false;

	public RASConnectionPoolImpl(int initialConnections,
			int maxConnections, boolean waitIfBusy) throws Exception{

		if (maxConnections <= 0) {
			throw new IllegalArgumentException(
					"The maximum number of connections must be greater than 0.");
		}

		this.m_nMaxConnections = maxConnections;
		this.m_bwaitIfBusy = waitIfBusy;

		if (initialConnections > maxConnections) {
			initialConnections = maxConnections;
		}

		m_availableConnections = Collections.synchronizedList(new ArrayList<RASConnection>(initialConnections));
		m_busyConnections = Collections.synchronizedList(new ArrayList<RASConnection>());

		for (int i = 0; i < initialConnections; i++) {
			m_availableConnections.add(makeNewConnection());
		}

             try {
                  String strTimeOut = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Server.ConnectionTimeOut");
                  if(strTimeOut.isEmpty())
                  {
                        CRSOnline1VuWebService.logger.info("[ ConnectionPool ] [Property] Server.ConnectionTimeOut :Property is Empty, default value is - 300 secs" );
                  }
                  else
                  {
                        CRSOnline1VuWebService.logger.info("[ ConnectionPool ] [Property] Server.ConnectionTimeOut :"+strTimeOut );
                  }
                  
                  if ((strTimeOut!=null) && (!strTimeOut.isEmpty())) {
                       int nTimeOut = Integer.parseInt(strTimeOut);
                       if (nTimeOut > 0) {
                            m_nGetConnectionTimeout = nTimeOut * 1000;
                       }
                  }
                 
				 int nTraceLevel = CRSPropertyFileAndDbReader.getTrace(); //not Tested     
                 if (nTraceLevel > 0) { 
                       m_bTrace = true;
                       CRSOnline1VuWebService.logger.info("[ ConnectionPool ] TraceLog is Enabled :" + nTraceLevel);
                  }
                  else
                  {
                       CRSOnline1VuWebService.logger.info("[ ConnectionPool ] TraceLog is Disabled :" + nTraceLevel );
                  
                  }
             } catch (Exception ex) {
                  throw ex;
             }
	}

	public synchronized RASConnection getConnection(Logger logger, long currReqNum) throws Exception {

		if (!m_availableConnections.isEmpty()) {

			int lastIndex = m_availableConnections.size() - 1;
                        if(m_bTrace && logger != null)
                        {
                            logger.info("[" + currReqNum + "] Available LastIndex : " + lastIndex);
                        }
			RASConnection existingConnection = (RASConnection) m_availableConnections.get(lastIndex);

			m_availableConnections.remove(lastIndex);

			if(existingConnection.getRASClientSk() != null && existingConnection.getRASClientSk().isClosed()) {
				//notifyAll();
                                notify();
                               if(m_bTrace && logger != null)
                                {
                                       logger.info("[" + currReqNum + "] Available Connection is closed try for another connection: " + lastIndex);
                                }
				return (getConnection(logger,currReqNum));
			} else {

				m_busyConnections.add(existingConnection);
                               if(m_bTrace && logger != null)
                                {
                                     logger.info("[" + currReqNum + "] Available Connection added to busy : " + lastIndex);
                                }
				return (existingConnection);
			}

		} else {
                        if(m_bTrace && logger != null)
                        {
                            logger.info("[" + currReqNum + "] Available list is empty, Available:" + getNumberOfAvailableConnections() + ", Busy:"+ getNumberOfBusyConnections());
                        }
			if ((getNumberOfAvailableConnections() + getNumberOfBusyConnections()) < m_nMaxConnections
					&& !m_bconnectionPending)
				//makeBackgroundConnection();
                            try{
                                RASConnection connection1 = makeNewConnection();
                                m_busyConnections.add(connection1);
                                if(m_bTrace && logger != null)
                                {
                                    logger.info("[" + currReqNum + "] New Connection is created and returned.");
                                }
                                return connection1;
                            }catch(Exception e){
                               // System.out.println("Error in makeNewConnection");
                                e.printStackTrace();
                            }

			else if (!m_bwaitIfBusy) {
                                if(m_bTrace && logger != null)
                                {
                                    logger.info("[" + currReqNum + "]WB-Connection limit reached");
                                }
				throw new Exception("WB-Connection limit reached");
			}

			try {
				m_bconnectionPending = true;
                                if(m_bTrace && logger != null)
                                {
                                    logger.info("[" + currReqNum + "]Wait for connection Start");
                                }
				wait(m_nGetConnectionTimeout);	//  wait
                                if(m_bTrace && logger != null)
                                {
                                    logger.info("[" + currReqNum + "]Wait for connection End");
                                }
			} catch (InterruptedException ie) {
				//System.out.println(ie.getMessage());
			}

                        if(m_bconnectionPending)
                        {
                                if(m_bTrace && logger != null)
                                {
                                    logger.info("[" + currReqNum + "]CP-Connection limit reached");
                                }
				throw new Exception("CP-Connection limit reached");
                        }
                        // Someone freed up a connection, so try again.
                        if(m_bTrace && logger != null)
                        {
                            logger.info("[" + currReqNum + "]Someone freed up a connection, trying again");
                        }
			return (getConnection(logger,currReqNum));
		}
	}

	private void makeBackgroundConnection() {
		m_bconnectionPending = true;
		try {
			Thread connectThread = new Thread(this);
			connectThread.start();
		} catch (OutOfMemoryError oome) {
			// Give up on new connection
		}
	}

	public void run() {
		try {
			RASConnection connection = makeNewConnection();
			synchronized (this) {
				m_availableConnections.add(connection);
				m_bconnectionPending = false;
				//notifyAll();
                                notify();
			}
		} catch (Exception e) {
			// Give up on new connection and wait for existing one to free up.
		}
	}

	private RASConnection makeNewConnection() {

		// Establish network connection to appropriate Server
		RASConnection connection = new RASConnection();
		return (connection);
	}

	public synchronized void releaseConnection(RASConnection connection)
	throws Exception {
		//connection.closeConnection();
		m_busyConnections.remove(connection);

		//connection = makeNewConnection();
		m_availableConnections.add(connection);
		m_bconnectionPending = false;
		// Wake up threads that are waiting for a connection
		//notifyAll();
                notify();
	}

	public synchronized void closeAllConnections() {
		closeConnections(m_availableConnections);
		m_availableConnections = Collections.synchronizedList(new ArrayList<RASConnection>());
		closeConnections(m_busyConnections);
		m_busyConnections = Collections.synchronizedList(new ArrayList<RASConnection>());
	}

	private void closeConnections(List<RASConnection> connections) {
		try {
			for (RASConnection connection : connections) {
				connection.closeConnection();
			}
		} catch (Exception e) {
			// Ignore errors; garbage collect anyhow
		}
	}

	public synchronized int getNumberOfAvailableConnections() {
		return m_availableConnections.size();
	}

	public synchronized int getNumberOfBusyConnections() {
		return m_busyConnections.size();
	}

	@Override
	public synchronized String toString() {
		StringBuilder result = new StringBuilder();
		result.append("Class: ").append(this.getClass().getName()).append("\n");
		result.append(" available: ").append(m_availableConnections.size())
		.append("\n");
		result.append(" busy: ").append(m_busyConnections.size()).append("\n");
		result.append(" max: ").append(m_nMaxConnections).append("\n");
		return result.toString();
	}

}
