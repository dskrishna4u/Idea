package com.in10s.rasserver;


import com.in10s.onevu.CRSPropertyFileAndDbReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class RASPMSServerclient {

    String m_datatoServer = "";
    RASWebClientConnector PMSsocket = null;
    String m_strWebUserName = "";

    long lRequestNum = 0;

    public RASPMSServerclient(String webUserName) {
        m_strWebUserName = webUserName;
    }
    public RASPMSServerclient(){

    }

    public void setlRequestNum(long lRequestNum) {
        this.lRequestNum = lRequestNum;
    }

    public boolean ValidatePMSToken(String PMSTokenData, String RASUserName,int org_id) {
       
        String strTemp[] = PMSTokenData.split("\1");

        if (org_id == 1)
        {
            org_id = 0;
        }


        m_datatoServer = "0" + "\1" + "1" + "\1" + RASUserName + "\1" + strTemp[0]+"\1"+org_id;

        pr("in ValidatePMSToken()::"+m_datatoServer);

        PMSsocket = new RASWebClientConnector( m_strWebUserName);
        PMSsocket.setlRequestNum(lRequestNum);

        PMSsocket.SetServer(strTemp[1], Integer.parseInt(strTemp[2]));

        if (PMSsocket.SendCommand(RSLOGINCMD.RSVALIDATE.getLOGINCMDValue(), -1, m_datatoServer)) {
            return true;
        } else {
            return false;
        }

    }

    public String sendJobToPMS(String strJobData) {
        String strReturn = "";
        if (PMSsocket == null) {
            PMSsocket = new RASWebClientConnector(m_strWebUserName);
        }
        if (PMSsocket.SendCommand(RSPMSCMD.JOB_ADD.getPMSCMDValue(), -1, strJobData)) {

            strReturn =  "1\1" + PMSsocket.getServerResponse();
        } else {
            strReturn =  "0\1" + PMSsocket.getServerResponse();
        }
       pr("In sendJobToPMS() strReturn:: "+strReturn);
        return strReturn;
    }
    public String StopPMSJOB(String strJobData) {
        if (PMSsocket == null) {
            PMSsocket = new RASWebClientConnector(m_strWebUserName);
        }
        if (PMSsocket.SendCommand(RSPMSCMD.JOB_RM.getPMSCMDValue(), -1, strJobData)) {

            return "1\1" + PMSsocket.getServerResponse();
        } else {
            return "0\1" + PMSsocket.getServerResponse();
        }
    }
    public String WaitForJob(String str_JobID) {
        //str_JobID = "146";
        m_datatoServer = str_JobID + "\1" + " 3"+"\1"+" 0"; //Job tracking 3 is to specify levels

        String pdfoutput = "";

        if (PMSsocket == null) {
            PMSsocket = new RASWebClientConnector(m_strWebUserName);
        }
        String waitingTimeStr = CRSPropertyFileAndDbReader.getPropertyValueFromDb("app.pmstoken.waiting.time");
        int waitingTime = Integer.parseInt(waitingTimeStr);
        pr("Socket Waiting Time :: "+waitingTime);
        PMSsocket.setTimeOut(waitingTime);	//wait for 3mins


        if (PMSsocket.SendCommand(RSPMSCMD.JOB_TRACK.getPMSCMDValue(), -1, m_datatoServer)) {
            pdfoutput = PMSsocket.getServerResponse();
        } else {
            pdfoutput = "0\1Error occured while processing";
        }
        PMSsocket.setTimeOut(30);
        pr("WaitForJob() pdfoutput : "+pdfoutput);
        return pdfoutput;
    }

    public String GeneratePDF(String RASUserName, String RASPassword, String ATNServerIP, String ATNServerPort,
            //String templateVersion, String templateName, String JOB_PACKET, String serverFlag, String processFlag, ServletContext contxt, String dataToServer,int job_Action,int org_id,String org_name) {
            String templateVersion, String templateName, String JOB_PACKET,String processFlag, String dataToServer,int job_Action,int org_id,String org_name) {
        String strreturnVal = "";
        Boolean PMSFlag = false;
        String strTemp[] = null;
        FileInputStream fstream = null;
        DataInputStream dis = null;

        FileWriter fileStream = null;
        BufferedWriter bWriter = null;
        pr("GeneratePDF called...");
        try {
            Object objPMSToken = null;// contxt.getAttribute("PMSToken");	//PMSToken data that is stored in PMSToken attribute contains PMS token PMS serverIP and PMS port number
            Properties objProperties = new Properties();
            objProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("OneVuConfigurations.properties"));
            String pmsTokenFilePath = objProperties.getProperty("location");
            pmsTokenFilePath = pmsTokenFilePath+"/"+"pmsToken.txt";

            File pmsTokenFile = new File(pmsTokenFilePath);
            if (!pmsTokenFile.exists()) {
                pmsTokenFile.createNewFile();
            }
            pr("pmsTokenFile :: " + pmsTokenFile);

            fstream = new FileInputStream(pmsTokenFilePath);
            dis = new DataInputStream(fstream);
            String strLine = "";
            while ((strLine = dis.readLine()) != null) {
                objPMSToken = strLine;
            }
            dis.close();
            fstream.close();
            fileStream = new FileWriter(pmsTokenFile);
            bWriter = new BufferedWriter(fileStream);
            pr("file content objPMSToken ::: " + objPMSToken);
            String strPMSToken = "";
            if (objPMSToken != null && !objPMSToken.equals("")) {
                strPMSToken = (String) objPMSToken;
                pr("PMS  Token retrieved from context  for user : " + m_strWebUserName);
            } else {
                RASATNServerclient atnclient = new RASATNServerclient(m_strWebUserName);
                atnclient.setlRequestNum(lRequestNum);
                pr(RASUserName+"--"+ RASPassword+"--"+ ATNServerIP+"--"+ ATNServerPort+"--"+org_name);
                strPMSToken = atnclient.getPMSData(RASUserName, RASPassword, ATNServerIP, ATNServerPort,org_name);
                pr("strPMSToken ***********:: " + strPMSToken);
                pr("PMS  Token created  for user : " + m_strWebUserName);
                PMSFlag = true;
            }

            if (PMSFlag) {
                strTemp = strPMSToken.split("\1");
                if (strTemp[0].equals("1")) //PMS Token generated
                {
                    strPMSToken = strPMSToken.substring(strPMSToken.indexOf("\1") + 1);	//remove tha flag to get only the token
                    // contxt.setAttribute("PMSToken", strPMSToken);

                    bWriter.write(strPMSToken);
                    bWriter.flush();
                } else {
                    strPMSToken = "";
                    strreturnVal = "3\1PMS  Token not found or not generated ";
                    pr("PMS  Token not found or not generated for user : " + m_strWebUserName);
                }
            }

            pr("PMS Token for user : " + m_strWebUserName + " : " + strPMSToken + ":");

            if (!strPMSToken.equals("")) //PMS token exists
            {
                if (ValidatePMSToken(strPMSToken, RASUserName,org_id)) {
                    m_datatoServer = dataToServer;
                    pr("JOB PACKET for user : " + m_strWebUserName + " : " + m_datatoServer);

                    if(job_Action ==2)  //for submiting the job
                    {
                        String strJOBID = sendJobToPMS(m_datatoServer);
                        pr(job_Action+":: job id from pms is::"+strJOBID);
                        
                        strTemp = strJOBID.split("\1");

                        if (strTemp[0].equals("1")) //JOB submitted
                        {
                             pr("job Submited sucessfully with job ID:"+strTemp);
                            //strreturnVal = "1\1Job Submited successfully with jobID:"+strTemp[1];
                            strreturnVal = strJOBID;
                        }
                        else
                        {
                            //strreturnVal = "2\1Error occured while submiting the job.";	//error occured while submiting job
                            strreturnVal = strJOBID;	//error occured while submiting job
                            pr("error occured while submiting job	for user : " + m_strWebUserName);
                        }
                    }
                    else if(job_Action ==3) //for stopping the job
                    {
                        String strJOBStatus = StopPMSJOB(m_datatoServer);
                        pr("job stop request result::"+strJOBStatus);

                        strTemp = strJOBStatus.split("\1");
                        pr("job Submited sucessfully with job ID:"+strTemp);
                        strreturnVal = "1\1Job Stopped successfully";
                    }
                    else if(job_Action ==4) //for dry run
                    {
                        String strJOBID = sendJobToPMS(m_datatoServer);
                        pr("job stop request result::"+strJOBID);

                        strTemp = strJOBID.split("\1");
                        pr("job Submited sucessfully with job ID:"+strTemp);
                        strreturnVal = "1\1Job Stopped successfully";
                        if (strTemp[0].equals("1")) //JOB submitted
                        {
                            strJOBID = strTemp[1];
                            pr("JOB ID for user : " + m_strWebUserName + " : " + strJOBID);
                            pr("started waiting for job");
                            strreturnVal = WaitForJob(strJOBID);    //for dry run process has to wait for the output
                            pr("job waiting done with result"+strreturnVal);
                            pr("PATH  for user : " + m_strWebUserName + " : " + strreturnVal + ":");
                        } else {
                            strreturnVal = "2\1Error occured while submiting the job.";	//error occured while submiting job
                            pr("error occured while submiting job	for user : " + m_strWebUserName);
                        }
                    }
                } else {
                    strreturnVal = "5\1PMS Token validation failed";		//invalid token
                    //contxt.setAttribute("PMSToken", null);
                    bWriter.write("");
                    bWriter.flush();
                    pr("Invalid PMS token for user : " + m_strWebUserName);
                }
            }
            /*else							//no PMS token
            {
            strreturnVal = "3";
            contxt.setAttribute("PMSToken" , null);
            pr("PMS  Token not found or not generated for user : "+m_strWebUserName);
            }*/
            bWriter.close();
            fileStream.close();
        } catch (Exception Excp) {
            strreturnVal = "4\1Exception occured while processing";		//Exception occured
            pr("Exception occured while  processing for user : " + m_strWebUserName + " : "+Excp);
            Excp.printStackTrace();
             StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            Excp.printStackTrace(pw);
            String strErrMsg = sw.toString();
            prError("Exception occured in GeneratePDF::"+strErrMsg);
        }finally{
            if(PMSsocket !=null){
                try {
                    PMSsocket.closeConnection();
                } catch (Exception e) {
                    pr("Exception in socket closing ::");
                    e.printStackTrace();
                }

            }
        }
        return strreturnVal;
    }
    public void pr(String str){
    //System.out.println("[" + lRequestNum + "]" + str);
    }
    public void prError(String str){
    //System.out.println("[" + lRequestNum + "]" +str);
    }
}
