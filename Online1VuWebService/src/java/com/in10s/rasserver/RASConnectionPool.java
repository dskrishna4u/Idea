package com.in10s.rasserver;

import org.apache.log4j.Logger;
public interface RASConnectionPool {

	RASConnection getConnection(Logger logger, long currReqNum) throws Exception;

	void releaseConnection(RASConnection connection) throws Exception;

	public int getNumberOfAvailableConnections();

	public int getNumberOfBusyConnections();

	public void closeAllConnections();

}
