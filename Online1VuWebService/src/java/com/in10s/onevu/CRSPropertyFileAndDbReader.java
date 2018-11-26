/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.in10s.onevu;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

//import snaq.db.ConnectionPool;

public class CRSPropertyFileAndDbReader
{

    private String JobDelimiter = "";
    private String JobQualifier = "";
    private Map<String, String> circleOrgName = null;
    private Map<String, String> circleOrgId = null;
    private Map<String, String> circleJobTemplate = null;
    private Map<String, String> blkLstJobTemplate = null;
    private String CircleSearchJobPkt = "";
    private String CircleAddJobPkt = "";
    private String CircleChurnJobPkt = "";
    private String BlkLstSearchJobPkt = "";
    private String BlkLstAddJobPkt = "";
    private String BlkLstChurnJobPkt = "";
    private String SubscriptionChangeJobPkt = "";
    private String CircleSearchAddDynamicThresholdJobPkt = "";
    private String[] leanStartTime = null;
    private String[] leanEndTime = null;
    private boolean CircleSearchNAddMode = false;
    private boolean CircleSearchNAddVthBlkMode = false;
    private boolean CircleChkPrevPmsRespForErrJob = false;
    private String CircleSearchAddJobPkt = "";
    private String CircleSearchAddVthBlkJobPkt = "";
    private String CircleSearchVthBlkJobPkt = "";
    private static String m_strPropertyFileTableName;
    private static String m_strPropertyFileModuleName;
    private static Logger logger = null;
    
    //private static ConnectionPool PropertyDbconnectionPool = null;
    private static BoneCP PropertyDbconnectionPool = null; 
    private boolean CircleWiseDBConnectionPool    = false;
    private static int TraceLevel;
    //private  String TransactionIdRegexp = "";
    private  Pattern TransactionIdRegexp ;
    
    private int nPartitioncount ;
    
    public  int getnPartitioncount() {
        return nPartitioncount;
    }

    public void setnPartitioncount(int nPartitioncount) {
        this.nPartitioncount = nPartitioncount;
    }
    
    public void setTrace(String TraceLevel) 
    {
        if(TraceLevel.isEmpty())
            this.TraceLevel = 0;    
        else
            this.TraceLevel = Integer.parseInt(TraceLevel) < 0 ? 0 : Integer.parseInt(TraceLevel);
    }

    public Pattern getTransactionIdRegexp() {
        return TransactionIdRegexp;
    }

    public void setTransactionIdRegexp(String TransactionIdval) {
        TransactionIdRegexp = Pattern.compile(TransactionIdval);
    }
    public static int getTrace() 
    {
	return TraceLevel;
    }
    public void setLogger(Logger loggerVar)
    {
        logger = loggerVar;
    }
    
    public boolean isCircleSearchNAddMode()
    {
	return CircleSearchNAddMode;
    }

    public void setCircleSearchNAddMode(boolean CircleSearchNAddMode)
    {
	this.CircleSearchNAddMode = CircleSearchNAddMode;
    }
    public boolean isCircleWiseDBConnectionPool()
    {
	return CircleWiseDBConnectionPool;
    }
    public void setCircleWiseDBConnectionPool(boolean CircleWiseDBConnectionPool)
    {
	this.CircleWiseDBConnectionPool = CircleWiseDBConnectionPool;
    }

    public boolean isCircleChkPrevPmsRespForErrJob()
    {
	return CircleChkPrevPmsRespForErrJob;
    }

    public void setCircleChkPrevPmsRespForErrJob(boolean CircleChkPrevPmsRespForErrJob)
    {
	this.CircleChkPrevPmsRespForErrJob = CircleChkPrevPmsRespForErrJob;
    }

    public String getCircleSearchAddJobPkt()
    {
	return CircleSearchAddJobPkt;
    }

    public void setCircleSearchAddJobPkt(String CircleSearchAddJobPkt)
    {
	this.CircleSearchAddJobPkt = CircleSearchAddJobPkt;
    }

    public boolean isCircleSearchNAddVthBlkMode()
    {
	return CircleSearchNAddVthBlkMode;
    }

    public void setCircleSearchNAddVthBlkMode(boolean CircleSearchNAddVthBlkMode)
    {
	this.CircleSearchNAddVthBlkMode = CircleSearchNAddVthBlkMode;
    }

    public String getCircleSearchAddVthBlkJobPkt()
    {
	return CircleSearchAddVthBlkJobPkt;
    }

    public void setCircleSearchAddVthBlkJobPkt(String CircleSearchAddVthBlkJobPkt)
    {
	this.CircleSearchAddVthBlkJobPkt = CircleSearchAddVthBlkJobPkt;
    }

    public String getCircleSearchVthBlkJobPkt()
    {
	return CircleSearchVthBlkJobPkt;
    }

    public void setCircleSearchVthBlkJobPkt(String CircleSearchVthBlkJobPkt)
    {
	this.CircleSearchVthBlkJobPkt = CircleSearchVthBlkJobPkt;
    }

    public String[] getLeanStartTime()
    {
	return leanStartTime;
    }

    public void setLeanStartTime(String[] leanStartTime)
    {
	this.leanStartTime = leanStartTime;
    }

    public String[] getLeanEndTime()
    {
	return leanEndTime;
    }

    public void setLeanEndTime(String[] leanEndTime)
    {
	this.leanEndTime = leanEndTime;
    }

    public String getJobDelimiter()
    {
	return JobDelimiter;
    }

    public void setJobDelimiter(String JobDelimiter)
    {
	if(JobDelimiter==null)
	{
	    JobDelimiter = "";
	}
	this.JobDelimiter = JobDelimiter;
    }

    public String getJobQualifier()
    {
	return JobQualifier;
    }

    public void setJobQualifier(String JobQualifier)
    {
	if(JobQualifier==null)
	{
	    JobQualifier = "";
	}
	this.JobQualifier = JobQualifier;
    }

    public String getCircleOrgName(String Circle)
    {
	String OrgName = "";
	if(circleOrgName != null)
	{
	    OrgName = circleOrgName.get(Circle);
	}
	return OrgName;
    }

    public void setCircleOrgName(String Circle, String OrgName)
    {
	if(circleOrgName == null)
	{
	    circleOrgName = new HashMap<String, String>();
	}

	circleOrgName.put(Circle, OrgName);

    }

    public String getCircleOrgId(String Circle)
    {
	String OrgId = "";
	if(circleOrgId != null)
	{
	    OrgId = circleOrgId.get(Circle);
	}
	return OrgId;
    }

    public void setCircleOrgId(String Circle, String OrgId)
    {
	if(circleOrgId == null)
	{
	    circleOrgId = new HashMap<String, String>();
	}

	circleOrgId.put(Circle, OrgId);

    }

    public Map<String, String> getcircleOrgId()
    {
	return circleOrgId;
    }

    public String getCircleJobTemplate(String Circle)
    {
	String jobTemplate = "";
	if(circleJobTemplate != null)
	{
	    jobTemplate = circleJobTemplate.get(Circle);
	}
	return jobTemplate;
    }

    public void setCircleJobTemplate(String Circle, String jobTemplate)
    {
	if(circleJobTemplate == null)
	{
	    circleJobTemplate = new HashMap<String, String>();
	}

	circleJobTemplate.put(Circle, jobTemplate);
    }

    public String getBlkJobTemplate(String Circle)
    {
	String jobTemplate = "";
	if(blkLstJobTemplate != null)
	{
	    jobTemplate = blkLstJobTemplate.get(Circle);
	}
	return jobTemplate;
    }

    public void setBlkJobTemplate(String Circle, String jobTemplate)
    {
	if(blkLstJobTemplate == null)
	{
	    blkLstJobTemplate = new HashMap<String, String>();
	}
	blkLstJobTemplate.put(Circle, jobTemplate);

    }

    public String getCircleSearchJobPkt()
    {
	return CircleSearchJobPkt;
    }

    public void setCircleSearchJobPkt(String CircleSearchJobPkt)
    {
	this.CircleSearchJobPkt = CircleSearchJobPkt;
    }

    public String getCircleAddJobPkt()
    {
	return CircleAddJobPkt;
    }

    public void setCircleAddJobPkt(String CircleAddJobPkt)
    {
	this.CircleAddJobPkt = CircleAddJobPkt;
    }

    public String getCircleChurnJobPkt()
    {
	return CircleChurnJobPkt;
    }

    public void setCircleChurnJobPkt(String CircleChurnJobPkt)
    {
	this.CircleChurnJobPkt = CircleChurnJobPkt;
    }

    public String getCircleSearchAddDynamicThresholdJobPkt() {
        return CircleSearchAddDynamicThresholdJobPkt;
    }

    public void setCircleSearchAddDynamicThresholdJobPkt(String CircleSearchAddDynamicThresholdJobPkt) {
        this.CircleSearchAddDynamicThresholdJobPkt = CircleSearchAddDynamicThresholdJobPkt;
    }

    public String getSubscriptionChangeJobPkt() {
        return SubscriptionChangeJobPkt;
    }

    public void setSubscriptionChangeJobPkt(String SubscriptionChangeJobPkt) {
        this.SubscriptionChangeJobPkt = SubscriptionChangeJobPkt;
    }
    
    public String getBlkLstSearchJobPkt()
    {
	return BlkLstSearchJobPkt;
    }

    public void setBlkLstSearchJobPkt(String BlkLstSearchJobPkt)
    {
	this.BlkLstSearchJobPkt = BlkLstSearchJobPkt;
    }

    public String getBlkLstAddJobPkt()
    {
	return BlkLstAddJobPkt;
    }

    public void setBlkLstAddJobPkt(String BlkLstAddJobPkt)
    {
	this.BlkLstAddJobPkt = BlkLstAddJobPkt;
    }

    public String getBlkLstChurnJobPkt()
    {
	return BlkLstChurnJobPkt;
    }

    public void setBlkLstChurnJobPkt(String BlkLstChurnJobPkt)
    {
	this.BlkLstChurnJobPkt = BlkLstChurnJobPkt;
    }  
    public static String getPropertyValueFromFile(/*String strPropertyFileName,*/ String strPropertyKey)
    {
	String strReturnValue = "";
	try
	{
	    Properties props = new Properties();
            Map<String, String> env = System.getenv();
            String configFilePath = env.get("APP_CONFIG_PROPERTIES");
            
            if(configFilePath != null)
            {
                logger.info("PropertyFilePath : " + configFilePath + " , EnvName : APP_CONFIG_PROPERTIES"); 
                File configFile = new File(configFilePath);
                InputStream is;
                if(configFile.exists())
                {
                    is = new FileInputStream(configFile);
                    //props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(strPropertyFileName));
                    props.load(is);
                    //String Path = Thread.currentThread().getContextClassLoader().getResource(strPropertyFileName).toString();
                    //prDebug(Path);
                    prDebug(configFilePath);
                    
                    if(props.containsKey(strPropertyKey))
                    {
                        strReturnValue = props.getProperty(strPropertyKey);
                    }
                }
                else
                {
                    logger.error("[ Start ] Error : PropertyFile Not Exists in : "+configFilePath); 
                    System.out.println("[ Online1VuWebservice ] Error : PropertyFile Not Exists in : "+configFilePath);
                }
            }
            else
            {
                 logger.error("[ Start ] Error : PropertyFilePath Not Found in Environment Variables (EnvName : APP_CONFIG_PROPERTIES)"); 
                 System.out.println("[ Online1VuWebservice ] Error : PropertyFilePath Not Found in Environment Variables (EnvName : APP_CONFIG_PROPERTIES)");
            }
	}
	catch (Exception exception)
	{
	    prErr("Error in getPropertyValueFromFile method :" + exception.getMessage());
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    exception.printStackTrace(pw);
	    String strErrMsg = sw.toString();
	    prDebug(strErrMsg);
	}
	if(strReturnValue == null)
	    strReturnValue = "";
	return strReturnValue;

    }
    
    public void  initPropertyDbConnectionPool() throws ClassNotFoundException
    {
	if(PropertyDbconnectionPool == null)
	{
	    
            int nPropertyDbPoolSize = 20;
            int nHubNumber = 0;
            String strHubNumber="";
            try 
            {
                strHubNumber = CRSPropertyFileAndDbReader.getPropertyValue("OneVuConfigurations.properties", "1VU.HubNumber");
                nHubNumber = Integer.parseInt(strHubNumber);
                logger.info("[start] [Property] Property.HubNumber:"+nHubNumber);
            }
            catch (NumberFormatException e) 
            {
                logger.info("[Exception] Invalid Hub Number : " + strHubNumber);
            }
            
            
            String strPropertyFileDB = CRSPropertyFileAndDbReader.getPropertyValueFromFile(/*"OneVuConfigurations.properties",*/ "1VU.DB_"+nHubNumber);
            if(strPropertyFileDB.isEmpty())
            {
               logger.info("[ Start ] [Property] Property.DB : Property is Empty"); 
            }
            else
            {
                logger.info("[ Start ] [Property] Property.DB_"+strHubNumber+":" + strPropertyFileDB); 
            }
            
	    m_strPropertyFileTableName = CRSPropertyFileAndDbReader.getPropertyValueFromFile(/*"OneVuConfigurations.properties",*/ "1VU.TableName_"+nHubNumber);
            if(m_strPropertyFileTableName.isEmpty())
            {
               logger.info("[ Start ] [Property] Property.TableName : Property is Empty"); 
            }
            else
            {
                logger.info("[ Start ] [Property] Property.TableName_"+strHubNumber+":" + m_strPropertyFileTableName); 
            }
            
	    m_strPropertyFileModuleName = CRSPropertyFileAndDbReader.getPropertyValueFromFile(/*"OneVuConfigurations.properties",*/ "1VU.ModuleName_"+nHubNumber);
            if(m_strPropertyFileModuleName.isEmpty())
            {
               logger.info("[ Start ] [Property] Property.ModuleName : Property is Empty"); 
            }
            else
            {
                logger.info("[ Start ] [Property] Property.ModuleName_"+strHubNumber+" :" + m_strPropertyFileModuleName); 
            }
            
	    String strPropertyPoolSize = CRSPropertyFileAndDbReader.getPropertyValueFromFile(/*"OneVuConfigurations.properties",*/ "1VU.PoolSize_"+nHubNumber);
            if(strPropertyPoolSize.isEmpty())
            {
               logger.info("[ Start ] [Property] Property.PoolSize : Property is Empty, default value is - 20"); 
            }
            else
            {
                logger.info("[ Start ] [Property] Property.PoolSize_"+strHubNumber+" :" + strPropertyPoolSize); 
            }

	    if(!strPropertyPoolSize.isEmpty())
	    {
		try
		{
		    nPropertyDbPoolSize = Integer.parseInt(strPropertyPoolSize);
		}
		catch (NumberFormatException exp)
		{
		    logger.error("initPropertyDbConnectionPool::" + exp);
		}
	    }
	    // create connection to db.
	    // dbType~~dbhost~~dbport~~dbservice~~dbuser~~dbpwd(java
	    // encryption)~~url~~dbschema
	    String strPropertyFileDBarr[] = strPropertyFileDB.split("\\#\\$\\#", -1);
	    if(strPropertyFileDBarr.length == 8)
	    {
		String strdbType = strPropertyFileDBarr[0];
		String strdbHost = strPropertyFileDBarr[1];
		String strdbPort = strPropertyFileDBarr[2];
		String strdbService = strPropertyFileDBarr[3];
		String strdbUser = strPropertyFileDBarr[4];
		String strdbPwd = strPropertyFileDBarr[5];
		String strdbUrl = strPropertyFileDBarr[6];
		String strSchema = strPropertyFileDBarr[7];
		String strDriver = CRSOnline1VuWebService.getDriver(strdbType);
		String strUrl = CRSOnline1VuWebService.getUrl(strdbType, strdbHost, strdbPort, strdbService, strdbUrl, strSchema);

		CRSAuthentication objAuth = new CRSAuthentication();
		String pwd = objAuth.Decrypt(strdbPwd);
		try
		{
		    Class c = Class.forName(strDriver);
                     BoneCPConfig config = new BoneCPConfig();
                     config.setJdbcUrl(strUrl); //<url>
                     config.setUsername(strdbUser); // <username>
                     config.setPassword(pwd);// <password>
                     config.setPartitionCount(1);//no of partitions need
                     config.setMinConnectionsPerPartition(1);//Partitions count
                     config.setMaxConnectionsPerPartition(nPropertyDbPoolSize); // <maxpool>
                     config.setConnectionTimeoutInMs(1800000); //<idleTimeout>, Milli seconds (1800000)/(1000*60)=30 mins
                     try {
                        PropertyDbconnectionPool = new BoneCP(config);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        //System.out.println(ex.getMessage());
                    }
//                     PropertyDbconnectionPool = new ConnectionPool("PropertyDb",
//                      //PropertyDbconnectionPool = new BoneCP("PropertyDb",// <poolname>,
//			    1, // <minpool>,
//			    nPropertyDbPoolSize, // <maxpool>,
//			    0, // <maxsize>,
//			    1800, // <idleTimeout>, seconds
//			    strUrl, // <url>,
//			    strdbUser, // <username>,
//			    pwd// <password>
//		    );
		}
		catch (ClassNotFoundException exp)
		{
		    logger.error("initPropertyDbConnectionPool::" + exp);
		    throw exp;
		}
                
                if(PropertyDbconnectionPool == null)
                {
                    logger.info("PropertyDbconnectionPool Initialized Failed.");
                }
                else
                {
                    logger.info("PropertyDbconnectionPool Initialized");
                }
	    }
	    else
	    {
		logger.error("initPropertyDbConnectionPool::PropertyFile.DB not correct");
	    }
	
        }
        

    }
    
    public static String getPropertyValueFromDb(String strPropertyKey)
    {
	String strReturnValue = "";
	String strPropertyKeyarr[]  = strPropertyKey.split("\\.");
	if(strPropertyKeyarr.length == 2)
	{
        	try
        	{      
        	       if(PropertyDbconnectionPool!=null)
        	       {
        		       Connection oPropertyDbconnection=null;
                	       oPropertyDbconnection =  PropertyDbconnectionPool.getConnection();
                	       if(oPropertyDbconnection !=null)
                	       {        		   
                		   String query = "SELECT VALUE FROM " + m_strPropertyFileTableName
                   	    		+ " WHERE MODULENAME ='"+m_strPropertyFileModuleName+"' AND GROUPNAME ='"+strPropertyKeyarr[0]
                   	    		+"' AND PROPERTY ='"+strPropertyKeyarr[1] +"'";
                                   Statement stmt = oPropertyDbconnection.createStatement();
                               	   ResultSet res = stmt.executeQuery(query);
                        
                               	   if(res.next())//assuming only one row multiple row need to think.
                               	   {
                               	       strReturnValue = res.getString(1);
                               	   }
                               	   res.close();
                               	   stmt.close();
                               	   oPropertyDbconnection.close();
                	       }
                	       else
                	       {
                		   logger.error("getPropertyValueFromDb(property key :"+strPropertyKey+")::oPropertyDbconnection is not available");
                	       }
        	       }
        	       else
        	       {
        		   logger.error("getPropertyValueFromDb(property key :"+strPropertyKey+")::PropertyDbconnectionPool is not available");
        	       }
        	}
        	catch (Exception exception)
        	{
        	    logger.error("getPropertyValueFromDb(property key :"+strPropertyKey+")::Error in getPropertyValueFromDb method :" + exception.getMessage());
        	    prErr("Error in getPropertyValueFromDb method :" + exception.getMessage());
        	    StringWriter sw = new StringWriter();
        	    PrintWriter pw = new PrintWriter(sw);
        	    exception.printStackTrace(pw);
        	    String strErrMsg = sw.toString();
        	    prDebug(strErrMsg);
        	}
	}

	else
	{
	    logger.error("getPropertyValueFromDb::PropertyName is not Given");
	}
	if(strReturnValue == null)
	{
	    strReturnValue = "";
	}
	return strReturnValue;

    }
    public static String getPropertyValue(String strPropertyFileName,String strPropertyKey) {
        String strReturnValue = "";
        try {
            Properties props = new Properties();
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(strPropertyFileName));
            String Path = Thread.currentThread().getContextClassLoader().getResource(strPropertyFileName).toString();
            //prDebug(Path);
            logger.info("[ Start ] [Property] Hub Number Configured Property " + Path);
            if (props.containsKey(strPropertyKey)) {
                strReturnValue = props.getProperty(strPropertyKey);
            }
            else
            {
                logger.error("Hub Number property key :"+strPropertyKey+"::is not available");
            }
        }
        catch(Exception exception) {
            logger.error("Reading Hub Number from Property File Exception  :"+exception);
            //prErr("Error in getPropertyValue method :"+exception.getMessage());
            //StringWriter sw = new StringWriter();
            //PrintWriter pw = new PrintWriter(sw);
            //exception.printStackTrace(pw);
            //String strErrMsg = sw.toString();
            //prDebug(strErrMsg);
        }
        return strReturnValue;

    }
  
    public static void prDebug(String str)
    {
	//System.out.println(str);
    }

    public static void prErr(String str)
    {
	//System.out.println(str);
    }

    public static void pr(String str)
    {
	//System.out.println(str);
    }

}
