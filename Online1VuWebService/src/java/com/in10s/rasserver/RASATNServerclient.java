package com.in10s.rasserver;

public class RASATNServerclient {

    String m_strReturnValue = "";
    String m_strWebUserName = "";

    public RASATNServerclient(String webUserName) {
        m_strWebUserName = webUserName;
    }

    private long lRequestNum = 0;
    public void setlRequestNum(long lRequestNum) {
    this.lRequestNum = lRequestNum;
    }

    public String getPMSData(String strUserName, String RASPassword, String ATNServerIP, String ATNServerPort,String org_name) {
        pr("getPMSData callled...");

        RASWebClientConnector webClient = null;
        
        try {
        webClient = new RASWebClientConnector( m_strWebUserName);

        pr("server details  : " + ATNServerIP + ":" + ATNServerPort + ":");

        webClient.SetServer(ATNServerIP, Integer.parseInt(ATNServerPort));
        webClient.setlRequestNum(lRequestNum);

        String datatoServer = "1" + "\1" + strUserName+"\1"+org_name;

        pr("Authentication data to server for user : " + m_strWebUserName + ":" + datatoServer + ":");

        if (webClient.SendCommand(RSMAINCMD.RSLOGIN.getMainCMDValue(), RSLOGINCMD.RSUSERLOGINAUTH.getLOGINCMDValue(), datatoServer)) {
            try {
                String strServerData = webClient.getServerResponse();

                pr("Authentication token from server  for user : " + m_strWebUserName + ":" + strServerData + ":");

                CRSEncrypt crsED = new CRSEncrypt();

                strServerData = crsED.Decrypt(RASPassword, strServerData);

                /*String strTemp []		= strServerData.split("\1");
                String strServerToken	= strTemp[2];
                String strDomainId		= strTemp[3];
                String strServerIp		= strTemp[4];
                String strMasterPort	= strTemp[5];
                String strMemberPort	= strTemp[6];*/

                datatoServer = "0" + "\1" + "PMS" + "\1" + strUserName;			//to get PMS details

                if (webClient.SendCommand(RSMAINCMD.RSLOGIN.getMainCMDValue(), RSLOGINCMD.RSSERVICETOKEN.getLOGINCMDValue(), datatoServer)) {
                    strServerData = webClient.getServerResponse();

                    pr("PMS service token from server  for user : " + m_strWebUserName + ":" + strServerData + ":");

                    m_strReturnValue = "1\1" + strServerData;

                } else {
                    m_strReturnValue = "3\1" + webClient.getServerResponse();		//error occured while getting PMS Servive token

                    prErr("error occured while retrieving PMS Servive token : " + m_strWebUserName);
                }

            } catch (Exception Excp) {
                m_strReturnValue = "4\1Exception occured while communicating with server while authentication";		//Exception occured while communicating with server
                prErr("Exception occured while communicating with server while authentication  for : " + m_strWebUserName + " : " +Excp.getMessage());
            }
        } else {
            pr("send command else..");
            m_strReturnValue = "2\1" + webClient.getServerResponse();		//error occured while logging into ras server
            prErr("error occured while logging into ras server  for user : " + m_strWebUserName);
        }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if (webClient != null) {
                webClient.closeConnection();
            }
        }
        
        return m_strReturnValue;
    }
    public void pr(String str){
    //logger.info(str);
        //System.out.println("[" + lRequestNum + "]" + str);
    }
    public void prDebug(String str){
    //logger.debug(str);
       // System.out.println("[" + lRequestNum + "]" + str);
    }
    public void prErr(String str){
    //logger.error(str);
       // System.out.println("[" + lRequestNum + "]" + str);
    }
}
