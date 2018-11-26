package com.in10s.rasserver;

import java.net.*;
import java.io.*;

public class RASClientSk {

	private Socket m_clientSocket = null;

	private String m_strServerIpAdd = "";

	private int m_nServerPortNumber = -1;

	private int m_nTimeOut;

	private InputStream m_Istrm = null;

	private DataInputStream m_distrm = null;

	public enum SocketMode {
		MODE_DATA, MODE_FILEGET, MODE_FILESEND;
	}

	private boolean m_bWaitFlag;

	private boolean m_bError;

	private boolean m_bStopWaiting;

	private boolean m_bSocketTimeout;

	private boolean m_bDataAfterFile;

	private SocketMode m_eSocketMode = SocketMode.MODE_DATA;

	private long m_nFileSizeToReceive;

	private String m_strError = "";

	private String m_strFilePath = "";

	private int m_nReplyCode;

	private String m_strServerResponseData = "";

	// CRSLogWriter m_logger = null;
	//private String m_strWebUserName;

	private CRSFileMan m_RSFileMan;

	public RASClientSk(/* CRSLogWriter logger, String webUserName*/) {
		// m_logger = logger;
		//m_strWebUserName = webUserName;
		m_RSFileMan = new CRSFileMan();
	}

	public void setServer(String strIP, int nPort) {
		m_strServerIpAdd = strIP;
		m_nServerPortNumber = nPort;
	}

	/** Sets the timeout of the connection * */
	public void setTimeOut(int nTimeInSeconds) {
		// A timeout of zero is interpreted as an infinite timeout.

		if (nTimeInSeconds <= 0) {
			m_nTimeOut = 0;

		} else {
			m_nTimeOut = nTimeInSeconds * 1000;
		}
	}

	private boolean getConnection() {
		m_bError = false;

		m_bSocketTimeout = false;

		int nTimeOut = m_nTimeOut;

		//setTimeOut(30);

		if (m_clientSocket == null) {

			try {
				SocketAddress sockaddr = new InetSocketAddress(
						m_strServerIpAdd, m_nServerPortNumber);

				m_clientSocket = new Socket();

				//System.out.println("Connecting to " + m_strServerIpAdd + " port = " + m_nServerPortNumber);

				m_clientSocket.connect(sockaddr, m_nTimeOut);

				//System.out.println("!!! Connected !!!");

			} catch (SocketTimeoutException e) {
				// TODO Auto-generated catch block
				m_bError = true;
				m_bSocketTimeout = true;
				m_strError = e.getMessage();
				//e.printStackTrace();
				return !m_bError;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				m_bError = true;
				m_strError = e.getMessage();
				//e.printStackTrace();
				return !m_bError;
			}
		}
		else if(!m_clientSocket.isConnected())
			m_bError = true;

		if (!m_bError) {
                try {
                    waitForResponse();
                } catch (IOException ex) {
                    
                }
		}

		setTimeOut(nTimeOut/1000);

		return !m_bError;
	}

	/** Waits until the server sends the response* */
	public void waitForResponse() throws IOException{

		m_bWaitFlag = true;

		try {
			m_Istrm = m_clientSocket.getInputStream();

			m_distrm = new DataInputStream(m_Istrm);

			// m_logger.writeAuditLog("socket wait time for : " +
			// m_strWebUserName + " : " + ServerTimeOut);

			m_clientSocket.setSoTimeout(m_nTimeOut);

		} catch (SocketException se) {
			// TODO Auto-generated catch block
			m_bWaitFlag = false;
			m_bError = true;
			m_strError = se.getMessage();
			se.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			m_bWaitFlag = false;
			m_bError = true;
			m_strError = e.getMessage();
			e.printStackTrace();
                        throw new IOException(e.getMessage());
			//return;
		}

		int nPacketSize = 0, nRetry = 0;

		while(m_bWaitFlag) {
			while (!m_bStopWaiting)
			{
				try {

					 m_distrm.readShort();
					 break;

				} catch (SocketTimeoutException e1) {
					if(!m_clientSocket.isConnected()) {
						m_strError = e1.getMessage();
						m_bWaitFlag = false; /* No more waiting for data...bcz socket terminated unexpectedly */
						e1.printStackTrace();
						break;
					} else {
						nRetry++;
						if(nRetry == 2) {
							m_strError = "Socket Timeout - Retry Limit reached.";
							m_bStopWaiting = true;
							e1.printStackTrace();
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					m_strError = e.getMessage();
					m_bWaitFlag = false;
					e.printStackTrace();
                                        throw new IOException(e.getMessage());
					//break;
				}
			}

			if (m_bStopWaiting)
			{
				m_bWaitFlag = false;
			}

			if (m_bWaitFlag) {
				readServerData();
			}
			else
			{
				//m_strError = errorString();
				m_bError = true;
			}
		}
	}


	public boolean isClosed() {

		boolean bTempWaitFlag = false;
		boolean bTempError = false;
		if(m_clientSocket != null) {
			try {
				//m_clientSocket.sendUrgentData(1);
				bTempWaitFlag = m_bWaitFlag;
				bTempError = m_bError;
				sendCommand(-1, -1, "", false);
			}catch (IOException e) {
				m_bWaitFlag = bTempWaitFlag;
				m_bError = bTempError;
				return true;
			}
		} else {
			return true;
		}
		m_bWaitFlag = bTempWaitFlag;
		m_bError = bTempError;

		return false;
		//return (m_clientSocket != null && m_clientSocket.isClosed());
	}
	/** Closes the current connection * */
	public int closeConnection() {
		int nReturnValue = -1;

		try {
			m_clientSocket.close();

			m_clientSocket = null;

			nReturnValue = 1; // Connection closed successfully
		} catch (IOException IOExcp) {
			nReturnValue = 2; // IOException occured while closing connection

			// m_logger.writeErrorLog("IOException oocured while closing
			// connection for : " + m_strWebUserName + " : " + IOExcp);
		} catch (Exception Excp) {
			nReturnValue = 3; // Exception occured while closing

			// m_logger.writeErrorLog("Exception oocured while closing
			// connection for : " + m_strWebUserName + " : " + Excp);
		}
		return nReturnValue;
	}

	/** Sends commands to the specified server for execution * */
	public boolean sendCommand(int nMainCMD, int nSubCMD, String CommandParams,
			boolean bDataAfterFile) throws IOException {

		m_bStopWaiting = false;

		if (isConnected()) { // check connection exists or not

			try {

				if(!m_bWaitFlag) {
					reset(false);
				}

				if (bDataAfterFile) {
					m_bDataAfterFile = bDataAfterFile;
				}

				ByteArrayOutputStream buf = new ByteArrayOutputStream();

				DataOutputStream dosBuf = new DataOutputStream(buf);

				dosBuf.writeInt(nMainCMD); // Writing main command

				if (nSubCMD != -1) {
					dosBuf.writeInt(nSubCMD); // Writing sub command
				}

				if(!CommandParams.isEmpty()) {

					dosBuf.writeInt(CommandParams.length() * 2);

					dosBuf.writeChars(CommandParams);

					dosBuf.flush();
				}

				ByteArrayOutputStream bufFinal = new ByteArrayOutputStream();

				dosBuf = new DataOutputStream(bufFinal);

				dosBuf.writeShort(buf.size());

				dosBuf.flush();

				bufFinal.write(buf.toByteArray(), 0, buf.size());

				buf.close();

				bufFinal.writeTo(m_clientSocket.getOutputStream());

				bufFinal.close();

				if (!m_bWaitFlag && nMainCMD != RSCMD.RSERROR.getCMDValue())
					waitForResponse();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				m_bError = true;
				m_strError = e.getMessage();
				throw new IOException(e.getMessage());
				//e.printStackTrace();
			}
		}

		//System.out.println("MCMD="  + nMainCMD + " SUBCMD=" + nSubCMD + " PARAMS=" + CommandParams + " Result=" + !m_bError);

		return !m_bError;
	}

	public void reset(boolean bClose) {
		m_bWaitFlag = false;
		m_bError = false;
		m_bDataAfterFile = false;

		m_eSocketMode = SocketMode.MODE_DATA;

		m_strError = "";
		m_strFilePath = "";

		m_nReplyCode = -1;
		m_strServerResponseData = "";

		if(bClose) {
			if (m_clientSocket != null)
				try {
					m_clientSocket.close();
					m_clientSocket = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		else {
			if (m_clientSocket != null) {
				try {
					if(m_distrm != null && m_distrm.available() > 0) {
						m_distrm.skipBytes(m_distrm.available());
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block

				}
			}
		}
	}

	public boolean sendFile(String strFile) {

		m_bStopWaiting = false;

		if (isConnected()) {
			reset(false);

			m_strFilePath = strFile;

			m_bWaitFlag = true;

			if (openDataFile(m_strFilePath, true)) {	
                            try 
                            {
                                waitForResponse();      
                            } 
                            catch (IOException ex) 
                            {
                                
                            }
			}
		}

		return !m_bError;
	}

	private boolean openDataFile(String strFilePath, boolean bReadOnly) {
		m_RSFileMan.reset();

		if (m_RSFileMan.openDataFile(strFilePath, bReadOnly) == true) {
			if (bReadOnly) { 	// Send Mode

				m_eSocketMode = SocketMode.MODE_FILESEND;
				String strResponse = String.valueOf(m_RSFileMan.getFileSize());
				try {
					sendCommand(RSMAINCMD.RSDATAFILE.getMainCMDValue(), -1, strResponse, false);
				} catch(IOException e) {

				}
			} else { 			// Receive Mode
				m_eSocketMode = SocketMode.MODE_FILEGET;
				try {
					sendCommand(RSCMD.RSACK.getCMDValue(), -1, "", false);
				} catch(IOException e) {

				}
			}
		} else {
			m_eSocketMode = SocketMode.MODE_DATA;
			m_bError = true;
			m_strError += m_RSFileMan.getErrorMessage();

			if (!bReadOnly) {
				try {
					sendCommand(RSCMD.RSERROR.getCMDValue(), -1, "", false);
				} catch(IOException e) {

				}
			}
		}

		if (m_bError)
			m_bWaitFlag = false;

		return !m_bError;
	}

	/** Reads the data from server * */
	public void readServerData() {

		m_bSocketTimeout = false;

		if (m_distrm != null) {

			m_nReplyCode = -1;
			try {
				//int nPacketSize = m_distrm.readShort(); // Read the data block size

				m_nReplyCode = m_distrm.readInt();

				//System.out.println("nCommand :: " + m_nReplyCode);
			} catch (SocketTimeoutException e1) {
				m_strError = "Socket Timeout while reading server response";
				m_bWaitFlag = false;
				m_bError = true;
				m_bSocketTimeout = true;

				e1.printStackTrace();
				return;

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				m_strError = "Unable to read Server response.";
				m_bWaitFlag = false;
				m_bError = true;

				e1.printStackTrace();
				return;
			}

			if (m_nReplyCode == RSCMD.CONNECTED.getCMDValue()) {
				m_strServerResponseData = "Connected Successfully";
				m_bWaitFlag = false;
				m_bError = false;

			} else if (m_nReplyCode == RSCMD.MAXCONNLIMIT.getCMDValue()) {
				m_strError = "Maximum connection limit reached";
				m_bWaitFlag = false;
				m_bError = true;

			} else if (m_nReplyCode == RSCMD.AUTHENTICATED.getCMDValue()) {
				m_strServerResponseData = "Authenticated Successfully";
				m_bWaitFlag = false;
				m_bError = false;

			} else if (m_nReplyCode == RSCMD.UNAUTHENTICATED.getCMDValue()) {
				m_strError = readDataString(m_distrm);
				if(m_strError.isEmpty())
					m_strError = "Not Logged In";
				m_bWaitFlag = false;
				m_bError = true;

			} else if (m_nReplyCode == RSCMD.DISCONNECTING.getCMDValue()) {
				m_strError = "Closing connection";
				m_bWaitFlag = false;
				m_bError = true;

			} else if (m_nReplyCode == RSCMD.CMDERR.getCMDValue()) {
				m_strError = "Syntax Error, Unknown command";
				m_bWaitFlag = false;
				m_bError = true;

			} else if (m_nReplyCode == RSCMD.RSERROR.getCMDValue()) {
				m_strError = readDataString(m_distrm);
				m_bWaitFlag = false;
				m_bError = true;

			} else if (m_nReplyCode == RSCMD.RSSUCCESS.getCMDValue()) {

				try {
					if (m_eSocketMode == SocketMode.MODE_DATA) {
						m_strServerResponseData = readDataString(m_distrm);

						m_bWaitFlag = false;

					} else if (m_eSocketMode == SocketMode.MODE_FILEGET) {

						int nDataSize = 0;
						byte []dataarr;

						try {
							 nDataSize = m_distrm.readInt();
							 dataarr = new byte[nDataSize];

//							 for (int nInti = 0; nInti < nDataSize; nInti++) {
//									dataarr[nInti] = (byte) m_distrm.read();
//							 }
							 m_distrm.readFully(dataarr, 0, nDataSize);

						} catch (EOFException  e1) {
							 m_strError = "EOFException while receiving file data";
							 m_bWaitFlag = false;
							 m_bError = true;

							 e1.printStackTrace();
							 return;
						} catch (SocketTimeoutException e1) {
							 m_strError = "Socket Timeout while reading server response";
							 m_bWaitFlag = false;
							 m_bError = true;
							 m_bSocketTimeout = true;

							 e1.printStackTrace();
							 return;
						}
						writeToDataFile(dataarr, nDataSize, false);

					} else if (m_eSocketMode == SocketMode.MODE_FILESEND) {
						m_eSocketMode = SocketMode.MODE_DATA;
						m_RSFileMan.reset();

						m_bWaitFlag = false;
						m_bError = false;

						//System.out.println("File sent successfully");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					m_strError = "TCP Error.";
					m_bWaitFlag = false;
					m_bError = true;

					e.printStackTrace();
				}
			} else if (m_nReplyCode == RSCMD.RSACK.getCMDValue()) {

				// m_strServerResponseData = readDataString(m_distrm);
				if (m_eSocketMode == SocketMode.MODE_FILESEND) {
					sendDataFile(m_nReplyCode);
				} else {
					m_bWaitFlag = false;
					m_bError = false;
				}
			} else if (m_nReplyCode == RSMAINCMD.RSDATAFILE.getMainCMDValue()) {

				String strSize = readDataString(m_distrm);

				if(!m_bError) {
					m_nFileSizeToReceive = Long.valueOf(strSize);

					File temp;
					try {
						temp = File.createTempFile("JCLI", ".tmp");
						m_strFilePath = temp.getAbsolutePath();
						openDataFile(m_strFilePath, false);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						m_strError = "Unable to create temporary file path";
						m_bWaitFlag = false;
						m_bError = true;

						try {
							sendCommand(RSCMD.RSERROR.getCMDValue(), -1, "", false);
						} catch(IOException ie) {
						}

						e.printStackTrace();
					}
				}
				else
					m_bWaitFlag = false;
			} else {
				m_strError = "Unknown server reply";
				m_bWaitFlag = false;
				m_bError = true;
			}
		}
	}

	private boolean sendDataFile(int nCmd) {
		boolean bSuccess = false;

		if (nCmd == RSCMD.RSACK.getCMDValue()) {
			byte[] data = new byte[CRSFileMan.BUFFER_16K];

			int nBytesRead = 0;

			if ((nBytesRead = m_RSFileMan.readFromDataFile(data, false)) > 0) {
				if (m_RSFileMan.getBytesRead() == m_RSFileMan.getFileSize()) {
					m_RSFileMan.reset();
				}

				try {
					// Write to buf in format: [CMD][Size of file data to be
					// sent(nBytesRead)][Actual File Data(equal to nBytesRead)]
					ByteArrayOutputStream buf = new ByteArrayOutputStream();

					DataOutputStream dosBuf = new DataOutputStream(buf);

					dosBuf.writeInt(RSCMD.RSSUCCESS.getCMDValue()); // CMD

					dosBuf.writeInt(nBytesRead); // Size of data to be sent

					dosBuf.write(data, 0, nBytesRead); // Actual File Data

					dosBuf.flush();

					ByteArrayOutputStream bufFinal = new ByteArrayOutputStream();

					dosBuf = new DataOutputStream(bufFinal);

					dosBuf.writeShort(buf.size());

					//System.out.println(buf.size());

					dosBuf.flush();

					bufFinal.write(buf.toByteArray(), 0, buf.size());

					// System.out.println(bufFinal.size());

					buf.close();

					bufFinal.writeTo(m_clientSocket.getOutputStream());

					bSuccess = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					m_bError = true;
					m_bWaitFlag = false;
					m_strError = e.getMessage();

					try {
						sendCommand(RSCMD.RSERROR.getCMDValue(), -1, "", false);
					} catch(IOException ie) {

					}
					e.printStackTrace();
				}
			} else {
				m_bError = true;
				m_bWaitFlag = false;
				m_strError += m_RSFileMan.getErrorMessage();

				try {
					sendCommand(RSCMD.RSERROR.getCMDValue(), -1, "", false);
				} catch(IOException e) {

				}
			}
		} else if (nCmd == RSCMD.RSERROR.getCMDValue()) {
			m_bError = true;
			m_strError = "File sending failed";
		}

		if (!bSuccess) {
			m_eSocketMode = SocketMode.MODE_DATA;
			m_RSFileMan.reset();
		}

		return bSuccess;
	}

	public boolean writeToDataFile(byte[] data, int nDataSize, boolean buffering) {
		boolean bSuccess = true;
		boolean bReset = true;

		if (m_RSFileMan.writeToDataFile(data, nDataSize, false)) {
			if (m_RSFileMan.getBytesWritten() == m_nFileSizeToReceive) // Success
			{
				try {
					sendCommand(RSCMD.RSSUCCESS.getCMDValue(), -1, "", false);
				} catch(IOException e) {

				}
				//System.out.println("File of size " + m_nFileSizeToReceive + " bytes received successfully. ");
			} else if (m_RSFileMan.getBytesWritten() > m_nFileSizeToReceive) // Error
			{
				bSuccess = false;
				m_strError = "File Size Overflow";

				try {
					sendCommand(RSCMD.RSERROR.getCMDValue(), -1, "", false);
				} catch(IOException e) {
				}
			} else {
				bReset = false;
				try {
					sendCommand(RSCMD.RSACK.getCMDValue(), -1, "", false);
				} catch(IOException e) {
				}
			}
		} else // Error Writing to file
		{
			bSuccess = false;
			m_strError = "File Write Error : " + m_RSFileMan.getErrorMessage();

			try {
				sendCommand(RSCMD.RSERROR.getCMDValue(), -1, "", false);
			} catch(IOException e) {

			}
		}

		if (!bSuccess || bReset) {
			m_eSocketMode = SocketMode.MODE_DATA;
			m_RSFileMan.reset();
		}

		if (!bSuccess
				|| (m_eSocketMode == SocketMode.MODE_DATA && !m_bDataAfterFile))
			m_bWaitFlag = false;

		return bSuccess;
	}

	private String readDataString(DataInputStream datastream) {

		String dataString = "";

		try {
			if (datastream.available() > 0) {
				int nDataSize = datastream.readInt();

				byte dataarr[] = new byte[nDataSize];
				for (int nInti = 0; nInti < nDataSize; nInti++) {
					dataarr[nInti] = (byte) datastream.read();
				}

				dataString = new String(dataarr, "UTF-16");

			}
		} catch (SocketTimeoutException e) {
			m_strError = "Socket Timeout while reading server response";
			m_bError = true;
			m_bSocketTimeout = true;

			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			m_strError = "IOException occurred while reading server response";
			m_bError = true;
			e.printStackTrace();
		}

		return dataString;
	}

	public boolean isConnected() {

		boolean bReturnValue = false;

		if (m_clientSocket != null) {
			bReturnValue = m_clientSocket.isConnected();
		}

		if (!bReturnValue) {
			m_clientSocket = null;

			bReturnValue = getConnection();
		}

		return bReturnValue;
	}

	void stopWaiting()
	{
		m_bStopWaiting = true;
	}

	public String getServerResponse() {
		return m_strServerResponseData;
	}

	public String getError() {
		return m_strError;
	}

	public String getFilePath() {
		return m_strFilePath;
	}

	public boolean isSocketTimeout() {
		return m_bSocketTimeout;
	}

	public int getReplyCode() {
		return m_nReplyCode;
	}

	public Socket getClientSocket() {
		return m_clientSocket;
	}
        
        public String getServerIP(){
            return m_strServerIpAdd;
        }
                

}
