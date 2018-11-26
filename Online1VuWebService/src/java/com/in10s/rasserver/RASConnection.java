package com.in10s.rasserver;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class RASConnection {

	private boolean m_bValidated;

	private int m_nTimeOut;

	private int m_nReplyCode;

	private String m_strData = "";
	private String m_strInFile = "";
	private String m_strOutFile = "";
	private String m_strError = "";

        private String m_strOrgID = "";
	private String m_strServerID = "";
	private RASClientSk  m_pRASClientSk;
	private RASSessionManager m_pRASSession;

	private Properties m_serverProperties;
        
        private Logger logger = null;
        private long currRequest = -1;

	int m_nMainCmd;
	int m_nLoginCmd;

	public RASConnection() {

		m_nMainCmd = RSMAINCMD.RSLOGIN.getMainCMDValue();
		m_nLoginCmd = RSLOGINCMD.RSVALIDATE.getLOGINCMDValue();

		m_nTimeOut = 60;

                m_bValidated = false;
                m_pRASClientSk = null;

	}

	public RASConnection(String strServerID, RASSessionManager pRSSession) {
		this();
		m_strServerID = strServerID;
		m_pRASSession = pRSSession;

                m_bValidated = false;
                m_pRASClientSk = null;
	}

        public void setLogger(Logger loggerVar){
            logger = loggerVar;
        }
        
        public void setCurrReq(long currReq){
            currRequest = currReq;
        }
	public void reset(boolean bClose) {
		m_nMainCmd = RSMAINCMD.RSLOGIN.getMainCMDValue();
		m_nLoginCmd = RSLOGINCMD.RSVALIDATE.getLOGINCMDValue();

		m_nTimeOut = 60;

		//m_bValidated = false;

		m_nReplyCode = -1;

		m_strData = "";
		m_strInFile = "";
		m_strOutFile = "";
		m_strError = "";

		m_strServerID = "";

		m_serverProperties = null;

		if (m_pRASClientSk != null) {
			m_pRASClientSk.reset(bClose);
		}

	}

	public void closeConnection() {

		if (m_pRASClientSk != null) {

			if(m_pRASClientSk.getClientSocket() != null && !m_pRASClientSk.getClientSocket().isClosed()) {

				m_pRASClientSk.closeConnection();

				m_pRASClientSk = null;
			}
		}
	}

	private boolean initClientSk() {

            captureLog("initClientSk:" + m_strOrgID);

		if (!m_bValidated) {

			if (m_pRASSession != null) {
				StringBuilder strError = new StringBuilder ();

				m_serverProperties = m_pRASSession.getServerProperties(m_strServerID, strError, true);

				if (m_serverProperties != null && !m_serverProperties.isEmpty()) {

                                     captureLog("initClientSkA:");

					if (m_pRASClientSk == null) {
						m_pRASClientSk = new RASClientSk();

						m_pRASClientSk.setTimeOut(m_nTimeOut);

						int nTimeOut = Integer.parseInt(m_serverProperties.getProperty("timeout"));

						//if (nTimeOut >= 60)
						{
							m_pRASClientSk.setTimeOut(nTimeOut);
						}
					}

					String strHostIP = m_serverProperties.getProperty("ip");
					int  nPort = Integer.parseInt(m_serverProperties.getProperty("port"));

                                        boolean bFos = true;
					do {
						m_pRASClientSk.reset(true);
						//m_pRASClientSk.reset(false);

						m_pRASClientSk.setServer(strHostIP, nPort);

                                                captureLog("IP-"+strHostIP+ " Port-"+ nPort);

						validateSocket();

						if (m_bValidated || !bFos) {
                                                    break;
                                                }
                                                else if(bFos){
                                                    bFos = false;
						    strHostIP = m_serverProperties.getProperty("fip");
						    nPort = Integer.parseInt(m_serverProperties.getProperty("fport"));
                                                    
                                                    if (strHostIP == "" || nPort == 0) {
                                                        break;
                                                    }
                                                }
					}while(true);

				}
				else {
					m_strError = strError.toString();
				}
			}
		}

		return m_bValidated;
	}

	// TODO
	private boolean findNextServer(String strHostIP, int nPort, int nFos) {
            boolean bStatus = true;
            if(nFos == 1){
                return false;
            }
            else{
            strHostIP = m_serverProperties.getProperty("fip");
            nPort = Integer.parseInt(m_serverProperties.getProperty("fport"));
            return bStatus;
            }
            
	}

	private void validateSocket()
	{
                captureLog("validateSocket:");

                if (m_strServerID.equalsIgnoreCase("PMS"))
                {
                   m_nMainCmd  = 46;
                   m_nLoginCmd = -1;
                }

		if (m_nMainCmd == -1 && m_nLoginCmd == -1)
		{
                    if(m_pRASClientSk.isConnected()) {
                        m_bValidated = true;
                      }
                    return;
		}

		if (!m_bValidated && m_pRASClientSk != null)
		{
			// Integer.parseInt(serverProperties.getProperty("DomainId"));
			int nDomainId = m_pRASSession.getDomainID();

			// serverProperties.getProperty("UserId");
			String strUserId = m_pRASSession.getUserID();

			String strToken = m_serverProperties.getProperty("token");

			// Integer.parseInt(serverProperties.getProperty("OrgId"));
			//int nOrgId = m_pRASSession.getOrgID();

			String strParams = "0" + "\1" + nDomainId + "\1" + strUserId + "\1" + strToken + "\1" + m_strOrgID/*nOrgId*/;

                        captureLog("ValidateSocket:" + strParams);

			try {
				if (m_pRASClientSk.sendCommand(m_nMainCmd, m_nLoginCmd, strParams, false)) {

					m_bValidated = true;
				}
				else {
					m_strError = m_pRASClientSk.getError();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	}

	public boolean sendFile(String strFile) {
		m_strInFile = strFile;

		return true;
	}
        
        public void captureLog(String str) {
        //String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSSSS :").format(new Date());
        //System.out.println(timeStamp+str);
        if (logger!= null) {
            logger.info("[" + currRequest + "]"+str);
        } else {
            //System.out.println(str);
        }
    }

	public boolean sendCommand(int nCmdMain, int nCmdSub, String strParams, boolean bDataAfterFile)	{

		boolean bSuccess = false;
		int nretry = 0;

		while (initClientSk() && nretry++ < 5) {					// Change from UniServe -> while to if
			bSuccess = true;

			if (!m_strInFile.isEmpty())	{
				bSuccess = m_pRASClientSk.sendFile(m_strInFile);
			}

			if (bSuccess) {
				//m_bValidated = false;
				//validateSocket();
				try {
					bSuccess = m_pRASClientSk.sendCommand(nCmdMain, nCmdSub, strParams, bDataAfterFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}

				if (bSuccess) {
					m_nReplyCode = m_pRASClientSk.getReplyCode();
					m_strData    = m_pRASClientSk.getServerResponse();//.trim();
					m_strOutFile = m_pRASClientSk.getFilePath();//.trim();
					captureLog("MCMD="  + nCmdMain + " SUBCMD=" + nCmdSub + " PARAMS=" + strParams + " Result=" + bSuccess);
				}
			}

			if(!bSuccess) {
				m_strError = m_pRASClientSk.getError();
				captureLog("MCMD="  + nCmdMain + " SUBCMD=" + nCmdSub + " PARAMS=" + strParams + " Result=" + bSuccess);
			}

			if (bSuccess || !isSocketDisconnected()) {	// As long as socket is disconnected keep rolling
				break;
			}
		}

		m_strInFile = "";

		return bSuccess;
	}

	private boolean isSocketDisconnected()
	{
		if (!m_pRASClientSk.getClientSocket().isConnected())
		{
			m_bValidated = false;
		}
		else if (m_pRASClientSk.isSocketTimeout())
		{
			m_bValidated = false;
		}
		else if (m_pRASClientSk.getReplyCode() == RSCMD.DISCONNECTING.getCMDValue() ||
				m_pRASClientSk.getReplyCode() == RSCMD.UNAUTHENTICATED.getCMDValue())
		{
			m_bValidated = false;
		}

		return !m_bValidated;
	}

	public String getData() {
		return m_strData;
	}

	public String getOutFile() {
		return m_strOutFile;
	}

	public String getError() {
		return m_strError;
	}

	public int getReplyCode() {
		return m_nReplyCode;
	}

        public String getOrgID() {
            return m_strOrgID;
        }

        public void setOrgID(String strOrgID) {
            this.m_strOrgID = strOrgID;
        }

	public String getServerID() {
		return m_strServerID;
	}

	public void setServerID(String serverID) {
		m_strServerID = serverID;
	}

	public RASSessionManager getSession() {
		return m_pRASSession;
	}

	public void setSession(RASSessionManager session) {
		m_pRASSession = session;
	}

	public RASClientSk getRASClientSk() {
		return m_pRASClientSk;
	}

}