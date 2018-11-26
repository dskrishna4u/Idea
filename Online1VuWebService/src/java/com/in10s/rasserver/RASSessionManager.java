package com.in10s.rasserver;

//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.WebResource;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;

//import org.w3c.dom.DOMException;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.Logger;
import com.in10s.onevu.CRSPropertyFileAndDbReader;
import com.in10s.onevu.CRSOnline1VuWebService;
//import net.sf.json.JSONObject;
//import net.sf.json.JSONSerializer;

public class RASSessionManager {

	private String m_strUserID = "";
	private String m_strWebUser = "";
	private String m_strPwd = "";
	private String m_strTGT = "";
	private String m_strOrgName = "";

	private int m_nOrgID;

	private String m_strLocalIP;

	private int m_nServerDomainID;
	private int m_nDomainID;

	private	boolean m_bAdministrator;
	private boolean m_bAuthenticated;

	ConcurrentMap<String, Properties> m_hashServerInfo;
	private Properties m_serverProperties;

	private int m_nPropertySize;

	private Set hashSet = null;

	private RASConnectionPool []m_ConnectionPool = null;//new RASConnectionPool[rsserver.NServers.NSERVERS.getNumOfServers()];
	private ConcurrentMap<String, RASConnectionPool[]> m_hashConnectionPoolManager;

	private static RASSessionManager m_SessionMan = null;
        private boolean m_bCircleWiseConnectionPool = true;
        private boolean m_bTrace = false;

	private RASSessionManager() throws Exception {						// !!!private!!! - cannot be subclassed

	     m_nDomainID = 1;
             String strTemp = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Server.CircleWiseConnectionPool");
             if(strTemp.isEmpty())
             {
                 CRSOnline1VuWebService.logger.info("[ SessionManager ] [Property] Server.CircleWiseConnectionPool :Property is Empty, default value is - 1(True)" );
             }
             else
             {
                 CRSOnline1VuWebService.logger.info("[ SessionManager ] [Property] Server.CircleWiseConnectionPool :"+strTemp );
             }
             if (strTemp != null && !strTemp.isEmpty() && strTemp.equals("0")) {
                  m_bCircleWiseConnectionPool = false;
             }
			 int nTraceLog = CRSPropertyFileAndDbReader.getTrace();  // not Tested
                  if (nTraceLog > 0 ) { //not Tested
                       m_bTrace = true;
                       CRSOnline1VuWebService.logger.info("[ SessionManager ] TraceLog is Enabled :"+nTraceLog );
             }
             else
             {
                       CRSOnline1VuWebService.logger.info("[ SessionManager ] TraceLog is Disabled :"+nTraceLog );
                  }
             m_hashServerInfo = new ConcurrentHashMap<String, Properties>();
             if (m_bCircleWiseConnectionPool) {
                  m_hashConnectionPoolManager = new ConcurrentHashMap<String, RASConnectionPool[]>();
             }else {
                  if (m_ConnectionPool == null) {
                       try {
                            m_ConnectionPool = initConnectionPool();                                                   
                       } catch (Exception e) {
                            throw new Exception("Connection Pool Initialization Failed");
                       }
                  }
             }

             Properties defaultSettings = new Properties();

             // MUST: Initialize all default properties here; if any new property, first add its default here.
             // Property names starts with small letter case

             defaultSettings.setProperty("ip", "");
             defaultSettings.setProperty("port", "");

             defaultSettings.setProperty("fip", "");
             defaultSettings.setProperty("fport", "");

             defaultSettings.setProperty("timeout", "30");

             defaultSettings.setProperty("token", "");

             m_nPropertySize = defaultSettings.size();

             m_serverProperties = new Properties(defaultSettings);

             if (!setATNServerProperties("", 0)) {
                  throw new Exception("Session Initialization failed");
             }

             String[] serverIDs = {rsserver.serverIDs.ATN.toString(),
                  rsserver.serverIDs.PDS.toString(),
                  rsserver.serverIDs.PMS.toString(),
                  rsserver.serverIDs.IDX.toString(),
                  rsserver.serverIDs.FOS.toString()
             };

             hashSet = new HashSet(Arrays.asList(serverIDs));
	}

	public synchronized static RASSessionManager getInstance() throws Exception {
		if(m_SessionMan == null) {
			m_SessionMan = new RASSessionManager();
		}

		return m_SessionMan;
	}

	public RASConnectionPool[] initConnectionPool() throws Exception {

		RASConnectionPool []pool = null;
		try {
		    	String strPoolSize = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Server.CONNPOOLSIZE");
                        if(strPoolSize.isEmpty())
                        {
                            CRSOnline1VuWebService.logger.info("[ ConnectionPool Init ] [Property] Server.CONNPOOLSIZE :Property is Empty, default value is - 50" );
                        }
                        else
                        {
                            CRSOnline1VuWebService.logger.info("[ ConnectionPool Init ] [Property] Server.CONNPOOLSIZE :"+strPoolSize );
                        }
                        
		    	int poolSize = 0;
		    	if(strPoolSize != null && !strPoolSize.isEmpty())
		    	{
		    	    poolSize = Integer.parseInt(strPoolSize);
		    	}
                        if (poolSize <= 0) poolSize = 50;

			pool = new RASConnectionPool[rsserver.NServers.NSERVERS.getNumOfServers()];

			//	ATN Connection Pool
			pool[rsserver.serverIDs.ATN.getserverID()] = new RASConnectionPoolImpl(20, poolSize, true);

			//	PDS Connection Pool
			pool[rsserver.serverIDs.PDS.getserverID()] = new RASConnectionPoolImpl(20, poolSize, true);

			//	PMS Connection Pool
			pool[rsserver.serverIDs.PMS.getserverID()] = new RASConnectionPoolImpl(20, poolSize, true);

			//	IDX Connection Pool
			pool[rsserver.serverIDs.IDX.getserverID()] = new RASConnectionPoolImpl(20, poolSize, true);

			//	FOS Connection Pool
			pool[rsserver.serverIDs.FOS.getserverID()] = new RASConnectionPoolImpl(20, poolSize, true);

		} catch(Exception e) {
			//System.out.println(e.getMessage());
			throw new Exception(e.getMessage());
		}
		return pool;
	}

	public RASConnection getConnection(String strOrg, String strServerID, boolean bClose, Logger logger, long currReqNum) throws Exception {

		StringBuilder strError = new StringBuilder();

             //String user = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Server.ATNUSERNAME");        
             //String pwd = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Server.ATNPASSWORD");
             
             if (!login("Administrator", "Fortune", 1, strError, "intense", logger, currReqNum)) {
			throw new Exception("***************  Login Failed: " + strError + " ****************");
             }
		RASConnection rasConnection = null;
		RASConnectionPool cPool = null;

		/* get connectionpool */
        String connectionPoolKey = strOrg;
        if(m_bCircleWiseConnectionPool){
                  RASConnectionPool[] connectionPool = m_hashConnectionPoolManager.get(connectionPoolKey);


                  if (connectionPool == null) {

        	try {
                            connectionPool = initConnectionPool();
                            if(connectionPool != null){                                                            
                                 if (logger != null) {
                                      logger.info("[" + currReqNum + "] CONNECTION POOL INIT SUCCESS :" + connectionPoolKey);
                                 }
                            }
        	} catch(Exception e) {
        		throw new Exception("Connection Pool Initialization Failed");
        	}

                       m_hashConnectionPoolManager.putIfAbsent(connectionPoolKey, connectionPool);
        	//System.out.println(m_hashConnectionPoolManager.keySet());
                }


		if(hashSet.contains(strServerID)) {
                       connectionPool = m_hashConnectionPoolManager.get(connectionPoolKey);
                       cPool = connectionPool[rsserver.serverIDs.valueOf(strServerID).getserverID()];
                  } else {
                       throw new Exception("Invalid Server ID: " + strServerID);
                     }
             } else {      
                  if (hashSet.contains(strServerID)) {
			cPool = m_ConnectionPool[rsserver.serverIDs.valueOf(strServerID).getserverID()];
		} else {
			throw new Exception("Invalid Server ID: " + strServerID);
		}
             }
		try {
                  if (m_bTrace && logger != null) {
                       logger.info("[" + currReqNum + "] GET CONNECTION START");
                  }
                  rasConnection = cPool.getConnection(logger, currReqNum);
                  if (m_bTrace && logger != null) {
                       logger.info("[" + currReqNum + "] GET CONNECTION END");
                  }

			 rasConnection.reset(bClose);

			 rasConnection.setServerID(strServerID);
                         rasConnection.setOrgID(strOrg);

			 rasConnection.setSession(this);

		} catch(Exception e) {
			throw new Exception(e.getMessage());
		}

		return rasConnection;
	}

	public void releaseConnection(RASConnection connection, String strOrg, Logger logger, long currReqNum,boolean bReset) throws Exception {

		RASConnectionPool cPool = null;

		String strServerID = connection.getServerID();
                 if(m_bCircleWiseConnectionPool){
                  RASConnectionPool[] connectionPool = m_hashConnectionPoolManager.get(strOrg);
                  if (connectionPool == null) {
                       throw new Exception("No Connection Pool exist for organization " + strOrg);
                 }
                  if (hashSet.contains(strServerID)) {
                       cPool = connectionPool[rsserver.serverIDs.valueOf(strServerID).getserverID()];
                  } else {
                       throw new Exception("Invalid Server ID: " + strServerID);
                  }
             } else {
                  if (m_ConnectionPool == null) {
                       throw new Exception("Centralized Connection Pool not exist");
                  }
                  if (hashSet.contains(strServerID)) {
			cPool = m_ConnectionPool[rsserver.serverIDs.valueOf(strServerID).getserverID()];
                  } else {
			throw new Exception("Invalid Server ID: " + strServerID);
		}
             }
		//System.out.print("Available Connections :: " + connection.getServerID());
                if(bReset)
                {
                    if(logger != null)
                    {
                        logger.info("[" + currReqNum + "] Resetting Connection before release ");
                    }
                    connection.reset(true);
                }
		cPool.releaseConnection(connection);
                if(logger != null){
                    logger.info("[" + currReqNum + "] Available Connections :: " + connection.getServerID() + ": " + cPool.getNumberOfAvailableConnections());
             } else {
		//System.out.println("Available Connections :: " + connection.getServerID() + ": " + cPool.getNumberOfAvailableConnections());
                }
	}

     /*    public String getPropertyValue(String strPropertyKey) {
        String strReturnValue = "";
        try {
            Properties props = new Properties();
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("OneVuConfigurations.properties"));
            strReturnValue = props.getProperty(strPropertyKey);
        }
        catch(Exception exception) {
            //prErr("Error in getPropertyValue method :"+exception.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            String strErrMsg = sw.toString();
            //prDebug(strErrMsg);
        }
        return strReturnValue;

    }*/

	public boolean setATNServerProperties(String strIP, int nPort) {

                boolean bSuccess = false;

                try {
                    String orgID = "0";
                    String struName="Administrator";

                    String ip = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Server.ATNSERVERIP");
                    if(ip.isEmpty())
                    {
                        CRSOnline1VuWebService.logger.info("[ Set ATN Server Properties ] [Property] Server.ATNSERVERIP :Property is Empty" );
                    }
                    else
                    {
                        CRSOnline1VuWebService.logger.info("[ Set ATN Server Properties ] [Property] Server.ATNSERVERIP :"+ip );
                    }
                    
                    String port = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Server.ATNSERVERPORT");
                    if(port.isEmpty())
                    {
                        CRSOnline1VuWebService.logger.info("[ Set ATN Server Properties ] [Property] Server.ATNSERVERPORT :Property is Empty" );
                    }
                    else
                    {
                        CRSOnline1VuWebService.logger.info("[ Set ATN Server Properties ] [Property] Server.ATNSERVERPORT :"+port );
                    }
                    
                    String fosIp = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Server.FOSSERVERIP");
                    if(fosIp.isEmpty())
                    {
                        CRSOnline1VuWebService.logger.info("[ Set ATN Server Properties ] [Property] Server.FOSSERVERIP :Property is Empty" );
                    }
                    else
                    {
                        CRSOnline1VuWebService.logger.info("[ Set ATN Server Properties ] [Property] Server.FOSSERVERIP :"+fosIp );
                    }
                    
                    String fosPort = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Server.FOSSERVERPORT");
                    if(fosPort.isEmpty())
                    {
                        CRSOnline1VuWebService.logger.info("[ Set ATN Server Properties ] [Property] Server.FOSSERVERPORT :Property is Empty" );
                    }
                    else
                    {
                        CRSOnline1VuWebService.logger.info("[ Set ATN Server Properties ] [Property] Server.FOSSERVERPORT :"+fosPort );
                    }
                    
                    if(ip == null || port == null || fosIp == null || fosPort == null)
                    {
                	return false;
                    }
                    //AppLogger.debug("list :: " + list);
                    //JSONObject result = (JSONObject) JSONSerializer.toJSON(list);
                    //System.out.println("serverinfo:" + result);

                    m_serverProperties.setProperty("ip", ip.trim());
                    m_serverProperties.setProperty("port", port.trim());
					
					//FailOver Properties
                    m_serverProperties.setProperty("fip", fosIp.trim());
                    m_serverProperties.setProperty("fport", fosPort.trim());


                    String timeout = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Server.TIMEOUT");
                    
                    if(timeout.isEmpty())
                    {
                        CRSOnline1VuWebService.logger.info("[ Set ATN Server Properties ] [Property] Server.TIMEOUT :Property is Empty, default value is - 60 " );
                    }
                    else
                    {
                        CRSOnline1VuWebService.logger.info("[ Set ATN Server Properties ] [Property] Server.TIMEOUT :"+timeout );
                    }
                    
                    if (timeout == null || Integer.parseInt(timeout) <= 0)
                        timeout = "60";

                    m_serverProperties.setProperty("timeout", timeout);

                    System.out.println("serverProps:" + m_serverProperties);

                    if (validateProperties(m_serverProperties)) {
                     bSuccess = true;
                        m_hashServerInfo.put("ATN", m_serverProperties);
                    }

                }catch(Exception ex) {
                    ex.printStackTrace();
                }

                return bSuccess;

//                m_serverProperties.setProperty("ip", strIP.trim());
//                m_serverProperties.setProperty("port", Integer.toString(nPort));
//                m_serverProperties.setProperty("timeout", Integer.toString(30));
//
//                if (validateProperties(m_serverProperties)) {
//                    bSuccess = true;
//                    m_hashServerInfo.put("ATN", m_serverProperties);
//                }

//                return bSuccess;

		//String strPath = System.getProperty("user.dir");
//                String strPath = "D:\\iECCM_BASE_VERSION_CLOUD_6.13\\bin\\rsfiles";
//
//		System.out.println(strPath);
//
//		String strXMLFile = new String(strPath + "\\serverinfo.xml");
//
//		File f = new File(strXMLFile.trim());
//
//		if(f != null && f.canRead()) {
//
//			try {
//				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//				DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
//
//				Document doc = dbBuilder.parse(f);
//				doc.getDocumentElement().normalize();
//
//				if(doc.getDocumentElement().getNodeName().equals("SERVERINFO")) {
//
//					//System.out.println("Root element : " + doc.getDocumentElement().getNodeName());
//
//					String strTemp = "";
//
//					NodeList nList = doc.getElementsByTagName("PRIMARY");		// <PRIMARY>
//
//					if(nList != null) {
//
//						Node nNode = nList.item(0);
//
//						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//
//							Element eElement = (Element) nNode;
//
//							strTemp = getTagValue("IPADDR", eElement);		// <IPADDR>
//							m_serverProperties.setProperty("ip", strTemp.trim());
//							//System.out.println("IP Addrs : " + strTemp);
//
//							strTemp = getTagValue("PORT", eElement);		// <PORT>
//							m_serverProperties.setProperty("port", strTemp.trim());
//							//System.out.println("Port : " + strTemp);
//
//						}
//					}
//
//					nList = doc.getElementsByTagName("FAILOVER");				// <FAILOVER>
//
//					if(nList != null) {
//						//CRSServerInfo* pRSServerInfo = new CRSServerInfo();
//						Node nNode = nList.item(0);
//
//						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//
//							Element eElement = (Element) nNode;
//
//							strTemp = getTagValue("IPADDR", eElement);		// <IPADDR>
//							m_serverProperties.setProperty("fip", strTemp.trim());
//							//System.out.println("IP Addrs : " + strTemp);
//
//							strTemp = getTagValue("PORT", eElement);		// <PORT>
//							m_serverProperties.setProperty("fport", strTemp.trim());
//							//System.out.println("Port : " + strTemp);
//
//						}
//					}
//
//					nList = doc.getElementsByTagName("TIMEOUT");				// <TIMEOUT>
//
//					if(nList != null) {
//
//						Node nNode = nList.item(0).getFirstChild();
//						strTemp = nNode.getNodeValue();
//
//						int nTimeOut = Integer.parseInt(strTemp.trim());
//
//						//System.out.println("TIMEOUT : " + nTimeOut);
//
//						if(nTimeOut <= 0) {
//							nTimeOut = 30;
//						}
//
//						m_serverProperties.setProperty("timeout", "" + nTimeOut);
//					}
//
//					///////////////////////////////////////////////////////////////////////////////////////
//					/*						GROUP INFO NOT IMPLEMENTED YET								 */
//					///////////////////////////////////////////////////////////////////////////////////////
//
//					if(validateProperties(m_serverProperties))	{
//						bSuccess = true;
//						m_hashServerInfo.put("ATN", m_serverProperties);
//					}
//
//				}
//			} catch (ParserConfigurationException e) {
//				// TODO Auto-generated catch block
//				bSuccess = false;
//				//e.printStackTrace();
//			} catch (SAXException e) {
//				// TODO Auto-generated catch block
//				bSuccess = false;
//				//e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				bSuccess = false;
//				//e.printStackTrace();
//			}
//
//		}
//
//		return bSuccess;
	}

//	private static String getTagValue(String sTag, Element eElement) {
//		try {
//			NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
//
//			Node nValue = (Node) nlList.item(0);
//
//			return nValue.getNodeValue();
//		} catch (DOMException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return "";
//	}

	public boolean validateProperties(Properties ppt) {
		return (ppt != null && !ppt.isEmpty() /*&& ppt.size() == m_nPropertySize*/ );
	}

	// Get server properties if already hashed;
	// Otherwise; get server properties by sending command 'LOGIN-SERVICETOKEN'
	// to ATN server and if successful hashmap the corresponding server properties.
	public Properties getServerProperties(String strServer, StringBuilder strError, boolean bFromServer) {

		if(!hashSet.contains(strServer)) {
			strError.append("Invalid Server ID");
			return null;
		}

		Properties ppt = null;

		synchronized (this) {

			ppt = m_hashServerInfo.get(strServer.trim().toUpperCase());

			if(ppt == null) {
				if(!bFromServer)
				{
					strError.append(strServer + " server details not found");
				}
				else {
					ppt = getServerProperties("ATN", strError, false);

					if(ppt != null && !ppt.isEmpty()) {

						RASConnection csk = new RASConnection("ATN", this);

                                                String timeout = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Server.TIMEOUT");
                                                if(timeout.isEmpty())
                                                {
                                                    CRSOnline1VuWebService.logger.info("[ Get ATN Server Properties ] [Property] Server.TIMEOUT :Property is Empty" );
                                                }
                                                else
                                                {
                                                    CRSOnline1VuWebService.logger.info("[ Get ATN Server Properties ] [Property] Server.TIMEOUT :"+timeout );
                                                }
                                                
                                                if (timeout == null || Integer.parseInt(timeout) <= 0) 
                                                    timeout = "60";

						ppt = null;

						if(csk.sendCommand(RSMAINCMD.RSLOGIN.getMainCMDValue(), RSLOGINCMD.RSSERVICETOKEN.getLOGINCMDValue(),
								"" + m_nServerDomainID + "\1" + strServer.trim().toUpperCase() + "\1" + m_strUserID.trim(), false))
						{
							String strData = csk.getData().trim();

							if(strData.length() > 0) {
								String[] strList = strData.split("\1");

								if(strList.length == 5) {
									ppt = new Properties();

									ppt.setProperty("token", strList[0]);

									ppt.setProperty("ip", strList[1]);
									ppt.setProperty("port", strList[2]);

									ppt.setProperty("fip", strList[3]);
									ppt.setProperty("fport", strList[4]);

									ppt.setProperty("timeout", timeout);

									if(validateProperties(ppt)) {
										m_hashServerInfo.putIfAbsent(strServer.trim().toUpperCase(), ppt);
										//System.out.println(m_hashServerInfo.keySet());
									}
									else {
										strError.append(strServer.trim() + " server details not found");
									}
								}
							}
						}
						else {
							strError.append(csk.getError());
						}

						csk.closeConnection();
					}
					else {
						strError.append("Authentication Server details not configured on client");
					}
				}
			}
		}

		return ppt;
	}

	public synchronized boolean login(String strUserID, String strPwd, int nDomainID,
			StringBuilder  strError, String strOrgName,Logger logger, long currReqNum) {
		boolean bSuccess  = false;

		String strToken = "";
		String strData = "";

		m_bAdministrator = false;

		if(m_bAuthenticated)
			return true;

                 if(logger != null){
                    logger.info("[" + currReqNum + "] ATN LOGIN START");
                }
		Properties ppt = getServerProperties("ATN", strError, false);
		//System.out.println(ppt.size() + "\t" + ppt.isEmpty());

		if(ppt != null && validateProperties(ppt)) {

			RASConnection csk = new RASConnection("ATN", this);

			csk.m_nMainCmd = csk.m_nLoginCmd = -1; //No need to validate this socket

			if(csk.sendCommand(RSMAINCMD.RSLOGIN.getMainCMDValue(), RSLOGINCMD.RSUSERLOGINAUTH.getLOGINCMDValue(),
					"" + nDomainID + "\1" + strUserID.trim() + "\1" + strOrgName.trim(), false)) {

				//boolean bIntError = false;

				if (strUserID.equalsIgnoreCase("Administrator")) {
					m_bAdministrator = true;
				}

				strToken = csk.getData();

				if(strToken.length() > 0)
				{
                                        CRSEncrypt encrypt = new CRSEncrypt() ;
					strData = encrypt.Decrypt(strPwd, strToken);

					String[] strList = strData.split("\1");

					if(strList.length >= 7)
					{
						boolean bCheckPwdSize = (strList.length >= 8)?(Integer.parseInt(strList[7]) == strPwd.length()) : true;

						if( strUserID.equals(strList[0]) && bCheckPwdSize ) {
							m_nDomainID = nDomainID;
							m_strUserID = strUserID;
							m_strPwd = strPwd;
							m_strOrgName = strOrgName;
							m_strLocalIP = strList[1];
							m_strTGT = strList[2];
							m_nServerDomainID = Integer.parseInt(strList[3]);
//							m_strGroupAddress = strList[4];
//							m_nMasterPort     = Integer.parseInt(strList[5]);
//							m_nMemberPort     = Integer.parseInt(strList[6]);

							if (strList.length > 8)
								m_nOrgID = Integer.parseInt(strList[8]);

							ppt.setProperty("token", m_strTGT);

							m_bAuthenticated = true;

							bSuccess = true;
						}
						else {
							strError.append("Invalid Userid/Password");
						}
					}
				}
			}
			else
			{
				strError.append(csk.getError());
			}

			csk.closeConnection();
		}
		else
		{
			strError.append("Authentication Server details not configured on client");
		}

                 if(logger != null){
                     if(bSuccess){
                        logger.info("[" + currReqNum + "] ATN LOGIN Success");
                     }
                     else{
                        logger.info("[" + currReqNum + "] ATN LOGIN Failed");
                     }
                }
		return bSuccess;
	}

	public int getDomainID() {
		return m_nDomainID;
	}

	public String getUserID() {
		return m_strUserID;
	}

	public int getOrgID() {
		return m_nOrgID;
	}

	public String getOrgName() {
		return  m_strOrgName.isEmpty() ? "" + m_nOrgID : m_strOrgName;
	}
}




