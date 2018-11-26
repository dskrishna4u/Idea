/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.in10s.onevu;

import java.sql.Connection;
import java.sql.DriverManager;
//import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.in10s.rasserver.RASConnection;
//import com.in10s.rasserver.RASPMSServerclient;
import com.in10s.rasserver.RASSessionManager;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.DatabaseMetaData;
import java.sql.Time;
//import java.io.PrintWriter;
//import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import java.util.logging.Level;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

//import org.apache.tomcat.util.buf.TimeStamp;
//import snaq.db.ConnectionPool;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;
//import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author krishnarao
 */

 class BooleanWrapper 
 {
        private boolean bWrapper;
        public BooleanWrapper(boolean value)
        {
            this.bWrapper = value;
        }
        public void setWrapper(boolean value) { bWrapper = value; }
        public boolean getWrapper() { return bWrapper; }
 }

@WebService(serviceName = "CRSOnline1VuWebService")
public class CRSOnline1VuWebService /* implements ServletContextListener */
{

    static
    {
	String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	System.setProperty("timeStamp", timeStamp);
    }

    private static long reqnum = 1;
    private static Map<String, Boolean> circleStateMap = null;
    private static CRSPropertyFileAndDbReader propertyFileReader = null;
    private static Map<String, CRSOrganizationInfo> circleInfoMap = null;
    private static Map<String, Integer> circleThresholdLmt = null;

    private static final String ORACLE = "oracle";
    private static final String MSSQL = "mssql";
    private static final String MYSQL = "mysql";
    private static final String DB2 = "db2";
    private static final String POSTGRESQL = "postgresql";
    //private static Map<String, ConnectionPool> connectionPoolList = null;
    
    private static Map<String, BoneCP> connectionPoolList = null;
    

    public static Logger logger = null;

    private String trackInsertQuery = "";
    private String requestInsertQuery = "";
    private String updateTableSQL = "";
    private String onlineJobInsertQuery = "";
    private String onlineJobUpdateQuery = "";
    private BooleanWrapper m_obIgnoreFieldFlag;
    private static Map<String, String> CircleUserNameMap = null; // <Key=CircleCode,
    private static Map<String, String> SubscriptionChangeThresholdMap = null; // Added for Subscription Change - Key : Circle code, Value : Threshold
    private Map<String, String> MNPThresholdMap = null; // Added for MNP - Key : Circle code, Value : Threshold
    //input request order according to soap
    private String m_strFieldNameArr[] =
	    { "BlackListFlag","Circle_Code","CustomerId","Customer_Category", "DOB", "FatherName",
              "FullName", "JobType", "Local_Ref_Msisdn", "Local_Ref_name", "MobileNo","OraNumber","OtherInfo1",
              "OtherInfo2","OtherInfo3","OtherInfo4","POA_Id", "POA_Type", "POI_Id", "POI_Type","pan",
	      "Permanent_Address", "Permanent_City", "Permanent_District", "Permanent_HNo", "Permanent_Pin",
              "Permanent_State", "PresentAddress", "Present_City", "Present_District",
	      "Present_HNo", "Present_Pin", "Present_State", "ProductType", "SIMNo", "TransactionID","uidai" };
    
    /*private String m_strSoapFieldNameArr[] =
	    { "BlackListFlag","Circle_Code", "Customer_Category","CustomerId", "DOB", "FatherName", "FullName", "Local_Ref_Msisdn", "Local_Ref_name", "MobileNo","OraNumber","OtherInfo1","OtherInfo2","POA_Id", "POA_Type", "POI_Id", "POI_Type",
		    "Permanent_Address", "Permanent_City", "Permanent_District", "Permanent_HNo", "Permanent_Pin", "Permanent_State", "PresentAddress", "Present_City", "Present_District",
		    "Present_HNo", "Present_Pin", "Present_State", "ProductType", "SIMNo"};*/
    //mapping of online csv to Soap..
   // private int SoapNOnlineMappingarr [] = {28,6,11,27,3,5,4,24,25,0,26,29,30,10,9,8,7,18,20,21,19,22,23,12,14,15,13,16,17,2,1};
     // private int m_OnlinecsvToSoapMapping [] = {9,30,29,4,6,5,1,16,15,14,13,2,23,20,24,25,27,28,17,20,18,19,21,22,7,8,10,3,0,11,12};
     //private int m_OnlineSoapTocsvMapping [] = {10,31,30,4,6,5,1,17,16,15,14,2,24,21,25,26,28,29,18,21,19,20,22,23,8,9,11,3,0,12,13};  UPTO - OTHERINFO2 
    //{11,35,34,5,7,6,2,20,19,18,17,4,28,31,29,30,32,33,22,25,23,24,26,27,9,10,12,3,1,13,14,15,16,0,21};
    private int m_OnlineSoapTocsvMapping [] = {10,34,33,4,6,5,1,19,18,17,16,3,27,30,28,29,31,32,21,24,22,23,25,26,8,9,11,2,0,12,13,14,15,36,20};
            
    private int m_MandatoryFieldIndexs[] = {};
    private int m_MandatoryFieldIndexs3[] = {};
    private int m_MandatoryFieldIndexs4[] = {};
    private int m_MandatoryFieldIndexs6[] = {};
    private  HashSet<Integer> m_hashSetIgnoreField;
    private String m_strChurnColumnName = "";
    private String m_strMasterInputSelectQry = "";
    private String m_strMasterInputTable = "";

    private int m_nTraceLevel;
    public CRSOnline1VuWebService() throws ClassNotFoundException, SQLException
    {

	if(logger == null)
	{
	    logger = Logger.getLogger(CRSOnline1VuWebService.class);
	}
        
        captureLog("[ Start ] FieldNames : " + getStringFromArray(m_strFieldNameArr));

	//initializeCirlceStateMap();

       // getPropertiesFromFile();
	getPropertiesFromFileandDb();
	initializeCirlceStateMap();
	getAllConnectionPool();
        subscriptionChangeThresholdinit();
        MNPThresholdinit();

	// initializeCirlceInfoMap();
	String strMandatoryFieldNames =  CRSPropertyFileAndDbReader.getPropertyValueFromDb("Mandatory.FieldNames");
        
        if(strMandatoryFieldNames != null && !strMandatoryFieldNames.isEmpty() )
        {
            captureLog("[ Start ] [Property] Mandatory.FieldNames : " + strMandatoryFieldNames);
            m_MandatoryFieldIndexs = getFieldIndexes(m_strFieldNameArr,strMandatoryFieldNames);
            captureLog("[ Start ] Mandatory Field Indexs : " + getStringFromArray(m_MandatoryFieldIndexs));
        }
        else
        {
            captureLog("[ Start ] [Property] Mandatory.FieldNames : Property is Empty");           
        }
        
	//m_strMandatoryFieldIndexs = strMandatoryFieldIndexs.split(",");
        
        strMandatoryFieldNames =  CRSPropertyFileAndDbReader.getPropertyValueFromDb("Mandatory.FieldNames_JobType3");
	if(strMandatoryFieldNames != null && !strMandatoryFieldNames.isEmpty() )
        {
            captureLog("[ Start ] [Property] Mandatory.FieldNames_JobType3 : " + strMandatoryFieldNames);
            m_MandatoryFieldIndexs3 = getFieldIndexes(m_strFieldNameArr,strMandatoryFieldNames);
            captureLog("[ Start ] Mandatory Field Indexs : " + getStringFromArray(m_MandatoryFieldIndexs3));
        }
        else
        {
            captureLog("[ Start ] [Property] Mandatory.FieldNames_JobType3 : Property is Empty");           
        }
        
        strMandatoryFieldNames =  CRSPropertyFileAndDbReader.getPropertyValueFromDb("Mandatory.FieldNames_JobType4");
	if(strMandatoryFieldNames != null && !strMandatoryFieldNames.isEmpty() )
        {
            captureLog("[ Start ] [Property] Mandatory.FieldNames_JobType4 : " + strMandatoryFieldNames);
            m_MandatoryFieldIndexs4 = getFieldIndexes(m_strFieldNameArr,strMandatoryFieldNames);
            captureLog("[ Start ] Mandatory Field Indexs : " + getStringFromArray(m_MandatoryFieldIndexs4));
        }
        else
        {
            captureLog("[ Start ] [Property] Mandatory.FieldNames_JobType4 : Property is Empty");           
        }
        
        strMandatoryFieldNames =  CRSPropertyFileAndDbReader.getPropertyValueFromDb("Mandatory.FieldNames_JobType6");
	if(strMandatoryFieldNames != null && !strMandatoryFieldNames.isEmpty() )
        {
            captureLog("[ Start ] [Property] Mandatory.FieldNames_JobType6 : " + strMandatoryFieldNames);
            m_MandatoryFieldIndexs6 = getFieldIndexes(m_strFieldNameArr,strMandatoryFieldNames);
            captureLog("[ Start ] Mandatory Field Indexs : " + getStringFromArray(m_MandatoryFieldIndexs6));
        }
        else
        {
            captureLog("[ Start ] [Property] Mandatory.FieldNames_JobType6 : Property is Empty");           
        }
        
        m_obIgnoreFieldFlag  = new BooleanWrapper(true);
        String strIgnoreFieldNames =  CRSPropertyFileAndDbReader.getPropertyValueFromDb("Ignore.FieldNames_JobType6");
        m_hashSetIgnoreField = new HashSet <Integer>();
        if(strIgnoreFieldNames != null && !strIgnoreFieldNames.isEmpty() )
        {
            captureLog("[ Start ] [Property] Ignore.FieldNames_JobType6 : " + strIgnoreFieldNames);
            int fieldIndexes[] = getFieldIndexes(m_strFieldNameArr,strIgnoreFieldNames,m_obIgnoreFieldFlag);
            captureLog("[ Start ] Ignore Field Indexs : " + getStringFromArray(fieldIndexes));
            for(int i = 0; i < fieldIndexes.length;i++)
            {
                m_hashSetIgnoreField.add(fieldIndexes[i]);
            }
            
        }
        else
        {
            captureLog("[ Start ] [Property] Ignore.FieldNames_JobType6 : Property is Empty");           
        }
        
        String strChurnColumnName =  CRSPropertyFileAndDbReader.getPropertyValueFromDb("Churn.MasterColumnName_JobType6");
	if(strChurnColumnName != null && !strChurnColumnName.isEmpty() )
        {
            captureLog("[ Start ] [Property] Churn.MasterColumnName_JobType6: " + strChurnColumnName);
            m_strChurnColumnName = strChurnColumnName;
        }
        else
        {
            captureLog("[ Start ] [Property] Churn.MasterColumnName_JobType6 : Property is Empty");           
        }
	
        String strMasterInputSelectQry =  CRSPropertyFileAndDbReader.getPropertyValueFromDb("Query.MasterInputSelect_JobType6");
	if(strMasterInputSelectQry != null && !strMasterInputSelectQry.isEmpty() )
        {
            captureLog("[ Start ] [Property] Query.MasterInputSelect_JobType6: " + strMasterInputSelectQry);
            m_strMasterInputSelectQry = strMasterInputSelectQry;
        }
        else
        {
            captureLog("[ Start ] [Property] Query.MasterInputSelect_JobType6 : Property is Empty");           
        }
        
        String strMasterInputTable =  CRSPropertyFileAndDbReader.getPropertyValueFromDb("Master.TableORViewName");
	if(strMasterInputTable != null && !strMasterInputTable.isEmpty() )
        {
            captureLog("[ Start ] [Property] Master.TableORViewName: " + strMasterInputTable);
            m_strMasterInputTable = strMasterInputTable;
        }
        else
        {
            captureLog("[ Start ] [Property] Master.TableORViewName : Property is Empty, default value is - MASTER_INPUT");           
            m_strMasterInputTable = "MASTER_INPUT";
        }
        
        
	String columns = "TRANSACTION_ID,JOB_TYPE,PROCESS_STATUS,COMMENTS," + "BLACKLIST_JOB,JOB_ID,REPLY_DATA";
	String values = "?,?,?,?,?,?,?";
	trackInsertQuery = "INSERT INTO <<USER_NAME>>.ONLINE_REQUEST_TRACKING (" + columns + ") VALUES(" + values + ")";

	columns = "TRANSACTION_ID,MSISDN,SIM_NO" + ",PRODUCT_TYPE,DOB,NAME,FATHER_NAME,CIRCLE_CODE" + ",POI_TYPE,POI_ID,POA_TYPE,POA_ID,CUSTOMER_CATEGORY"
		+ ",PRESENTADDRESS,PRESENT_HNO,PRESENT_CITY,PRESENT_DISTRICT" + ",PRESENT_PIN,PRESENT_STATE,PERMANENT_ADDRESS,PERMANENT_HNO"
		+ ",PERMANENT_CITY,PERMANENT_DISTRICT,PERMANENT_PIN,PERMANENT_STATE" + ",LOCAL_REF_MSISDN,LOCAL_REF_NAME,JOB_TYPE,PROCESS_STATUS"
		+ ",REPLY_CNT,REPLY_STATE,COMMENTS,ERR_CODE,RULE,ERR_DESC,SUBSTATUS," + "REQ_DATETIME,RESP_DATETIME,ORANUMBER,CUSTOMER_ID,BLACKLIST_FLAG,OTHER_INFO1,OTHER_INFO2,OTHER_INFO3,OTHER_INFO4,UIDAI,PAN";

	values = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";

	requestInsertQuery = "INSERT INTO <<USER_NAME>>.ONLINE_REQUEST_RESPONSE (" + columns + ") VALUES(" + values + ")";

	updateTableSQL = "UPDATE <<USER_NAME>>.ONLINE_REQUEST_RESPONSE SET REPLY_STATE = ?," + "REPLY_CNT = ?,ERR_CODE = ?,ERR_DESC = ?,COMMENTS = ?,"
		+ "JOB_TYPE = ?,RULE = ?,SUBSTATUS = ?,REQ_DATETIME = ?,RESP_DATETIME = ?" + "WHERE TRANSACTION_ID = ?";

	columns = "TRANSACTION_ID,REQ_DATETIME,RETRY_PROCESS";

	values = "?,?,?";

	onlineJobInsertQuery = "INSERT INTO <<USER_NAME>>.ONLINE_JOB_INFO (" + columns + ") VALUES(" + values + ")"; // checked

	onlineJobUpdateQuery = "UPDATE <<USER_NAME>>.ONLINE_JOB_INFO SET RETRY_PROCESS = ? " + "WHERE TRANSACTION_ID = ?"; // unchecked

    }
    
    int findIndex(String [] strArr,String strFindString )
    {
        for(int i = 0; i< strArr.length;i++)
        {
           if(strArr[i].equalsIgnoreCase(strFindString))
           {
               return i;
           }
        }
        return -1;
    }

    int [] getFieldIndexes(String strArr[],String strMandatoryFieldNames)
    {
        BooleanWrapper obWrapper = new BooleanWrapper(true);
        return getFieldIndexes(strArr,strMandatoryFieldNames,obWrapper);
    }
    
    int [] getFieldIndexes(String strArr[],String strMandatoryFieldNames,BooleanWrapper obWrapper)
    {
        obWrapper.setWrapper(true);
        
        String strTemp[] = strMandatoryFieldNames.split(",");
        int arr[] = new int [strTemp.length];  
        for(int i = 0; i<strTemp.length;i++)
        {
          arr[i] = findIndex(strArr,strTemp[i]);
          
          if(arr[i] == -1)
          {
              obWrapper.setWrapper(false);
          }
        }
        return arr;
    }

    synchronized long generateRequestNumber()
    {
	if(reqnum > 99999)
	{
	    reqnum = 1;
	}
	return reqnum++;
    }

    final synchronized void initializeCirlceStateMap()
    {
	if(circleStateMap == null)
	{
	    circleStateMap = new HashMap<String, Boolean>();

	    String strTempCircleOrgId = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Circles.Config");
	    if(strTempCircleOrgId == null || strTempCircleOrgId.isEmpty())
	    {
			return;
	    }
	    String strTempCircleOrgIds[] = strTempCircleOrgId.split("\\$\\#\\$");
	    for (int i = 0; i < strTempCircleOrgIds.length; i++)
	    {
		strTempCircleOrgId = strTempCircleOrgIds[i];
		String[] strTemps = strTempCircleOrgId.split(",");
		if(strTemps.length >= 1)
		{
		    circleStateMap.put(strTemps[0], Boolean.TRUE);

		}
	    }
	}
    }

    final synchronized void MNPThresholdinit()
    {
        captureLog("[ Start ] [ *** Reading MNP Separate Threshold Properties Start *** ]");
        MNPThresholdMap = new HashMap<String,String>(); // Key : Circle code, Value : Syntax - << Rulenumber1,Threshold;Rulenumber2,Threshold; >> Example:- "1,9;2,9;3,9;4,9;7,9"
        String strMNPRules = CRSPropertyFileAndDbReader.getPropertyValueFromDb("MNP.Rules").trim();
        captureLog("[ Start ] [Property] MNP.Rules : "+ strMNPRules);
    
        String MNPRulesList[] = null;
        String MNPThresholdList[] = null;
        if(strMNPRules.isEmpty())
        {
            logger.error("[ Start ] MNP.Rules : Property is Empty - MNP Orders will not be processed (Please configure this property and re-start webservice)" );
        }
        else
        {
            boolean initMNPThreshold = true;
            MNPRulesList=strMNPRules.split(",");
            int MNPRulenumList[] = new int[MNPRulesList.length];
            for(int i=0;i<MNPRulesList.length;i++)
            {
                if(!MNPRulesList[i].toUpperCase().trim().startsWith("R"))
                {
                    logger.error("[ Start ] Invalid MNP.Rule configuration : "+ MNPRulesList[i]);
                    initMNPThreshold = false;
                }
                else
                {
                    try
                    {
                        int nRulenum=Integer.parseInt(MNPRulesList[i].trim().substring(1).trim());
                        if(nRulenum>0)
                        {
                            MNPRulenumList[i] = nRulenum;
                        }
                        else
                        {
                            logger.error("[ Start ] Invalid MNP.Rule configuration. Rule number should not be negative : "+ MNPRulesList[i] +" : "+nRulenum);
							MNPRulenumList = null;
                            initMNPThreshold = false;
                            break;
                        }
                    }
                    catch(Exception e)
                    {
                        logger.error("[ Start ] Exception while parsing the Rule number ("+MNPRulesList[i].trim().substring(1).trim()+ ") - MNP Orders will not be processed (Please configure this property and re-start webservice)" +e);
                        initMNPThreshold = false;
                        break;
                    }
                }
            }
            
            String strMNPRuleNumbers = "";
            for(int i=0;i<MNPRulenumList.length;i++)
            {
                strMNPRuleNumbers=strMNPRuleNumbers+MNPRulenumList[i]+";";                
            }
            captureLog("[ Start ] Rules for which Separate MNP Threshold is applicable are : " +strMNPRuleNumbers);
            
            String strMNPThreshold = CRSPropertyFileAndDbReader.getPropertyValueFromDb("MNP.CirclesThreshold").trim();
            captureLog("[ Start ] [Property] MNP CirclesThreshold : "+ strMNPThreshold);
            
            if(initMNPThreshold)
            {
                if(strMNPThreshold.isEmpty())
                {
                    logger.error("[ Start ] [Property] MNP CirclesThreshold : Property is Empty - MNP Orders will not be processed (Please configure this property and re-start webservice)" );
                }
                else
                {
                    MNPThresholdList = strMNPThreshold.split("\\#\\$\\#");
                    for(int i=0;i<MNPThresholdList.length;i++)
                    {
                        captureLog("[ Start ] MNP CircleThreshold["+i+"] : "+ MNPThresholdList[i]);
                        String circleMNPThreshold[]=MNPThresholdList[i].split(",");
                        if(circleMNPThreshold.length!=2)
                        {
                            logger.error("[ Start ] [Property] Invalid MNP CircleThreshold "+MNPThresholdList[i]+" Expected Format << Circlecode,Threshold >> Eg:- AP,5 - Configure the correct Circle Threshold and re-start webservice");
                        }
                        else 
                        {
                            String strCircle = circleMNPThreshold[0].toUpperCase().trim();
                            int nMNPThreshold = 0;
                            try
                            {
                                nMNPThreshold=Integer.parseInt(circleMNPThreshold[1].trim());
                            }
                            catch(Exception e)
                            {
                                logger.error("[ Start ] Exception while parsing Threshold value, Threshold value - " + circleMNPThreshold[1].trim() + ". MNP orders for "+strCircle+" will not be processed - Configure the correct Circle Threshold and re-start webservice : " +e);
                            }
                            if(nMNPThreshold>=0)
                            {
                                String strFinalThreshold="";
                                for(int j=0;j<MNPRulesList.length;j++)
                                {
                                    if(strFinalThreshold.isEmpty())
                                    {
                                        strFinalThreshold=MNPRulenumList[j]+","+nMNPThreshold;
                                    }
                                    else
                                    {
                                        strFinalThreshold=strFinalThreshold+";"+MNPRulenumList[j]+","+nMNPThreshold;
                                    }
                                }
                                captureLog("[ Start ] MNP Threshold for "+ strCircle +" circle is : "+ circleMNPThreshold[1]+" -- "+strFinalThreshold);
                                try
                                {
                                    MNPThresholdMap.put(strCircle, strFinalThreshold);
                                }
                                catch(Exception e)
                                {
                                    logger.error("[ Start ] Exception while placing value in MNPThresholdMap, Invalid MNP threshold Configuration - MNP Orders will not be processed for this circle (Please configure this property and re-start webservice) : "+strCircle +" : "+ strFinalThreshold +" : "+e);
                                }
                            }
                            else
                            {
                                logger.error("[ Start ] MNP Threshold configured is invalid ");
                            }
                        }
                    }
                }    
            }            
        }
        captureLog("[ Start ] [ --- Reading MNP Separate Threshold Properties End ---]");
    }
    
    final synchronized void subscriptionChangeThresholdinit()
    {
        captureLog("[ Start ] [ *** Reading Subscription Change Properties Start *** ]");
        SubscriptionChangeThresholdMap = new HashMap<String, String>(); // Key : Circle code, Value : Syntax - << Rulenumber1,Threshold;Rulenumber2,Threshold; >> Example:- "1,9;2,9;3,9;4,9;7,9"
        String strSubChangeRules = CRSPropertyFileAndDbReader.getPropertyValueFromDb("SubscriptionChange.Rules").trim();
        captureLog("[ Start ] [Property] SubscriptionChange.Rules : "+ strSubChangeRules);
	
        String SubChangeRulesList[] = null;
	String SubChangeThresholdList[] = null;
	if(strSubChangeRules.isEmpty())
        {
            logger.error("[ Start ] SubscriptionChange.Rules : Property is Empty - Subscription Orders will not be processed (Please configure this property and re-start webservice)" );
        }
        else
        {
            boolean initSubChangeThreshold=true;
            SubChangeRulesList=strSubChangeRules.split(",");
            int SubChangeRulenumList[] = new int[SubChangeRulesList.length];
            for(int i=0;i<SubChangeRulesList.length;i++)
            {
                if(!SubChangeRulesList[i].toUpperCase().trim().startsWith("R"))
                {
                    logger.error("[ Start ] Invalid SubscriptionChange.Rule configuration : "+ SubChangeRulesList[i]);
                    initSubChangeThreshold=false;
                }
                else
                {
                    try
                    {
                        int nRulenum=Integer.parseInt(SubChangeRulesList[i].trim().substring(1).trim());
                        if(nRulenum>0)
                        {
                            SubChangeRulenumList[i] = nRulenum;
                        }
                        else
                        {
                            logger.error("[ Start ] Invalid SubscriptionChange.Rule configuration. Rule number should not be negative : "+ SubChangeRulesList[i] +" : "+nRulenum);
							SubChangeRulenumList = null;
                            initSubChangeThreshold = false;
                            break;
                        }
                    }
                    catch(Exception e)
                    {
                        logger.error("[ Start ] Exception while parsing the Rule number ("+SubChangeRulesList[i].trim().substring(1).trim()+ ") - Subscription Orders will not be processed (Please configure this property and re-start webservice)" +e);
                        initSubChangeThreshold = false;
                        break;
                    }
                }
            }

            String strSubChangeRuleNumbers = "";
            for(int i=0;i<SubChangeRulenumList.length;i++)
            {
                strSubChangeRuleNumbers=strSubChangeRuleNumbers+SubChangeRulenumList[i]+";";                
            }
            captureLog("[ Start ] SubscriptionChange Rules : " +strSubChangeRuleNumbers);

            String strSubChangeThreshold = CRSPropertyFileAndDbReader.getPropertyValueFromDb("SubscriptionChange.CirclesThreshold").trim();
            captureLog("[ Start ] [Property] SubscriptionChange CirclesThreshold : "+ strSubChangeThreshold);
            
            if(initSubChangeThreshold)
            {
                if(strSubChangeThreshold.isEmpty())
                {
                    logger.error("[ Start ] SubscriptionChange CirclesThreshold : Property is Empty - Subscription Orders will not be processed (Please configure this property and re-start webservice)" );
                }
                else
                {
                    SubChangeThresholdList = strSubChangeThreshold.split("\\#\\$\\#");
                    for(int i=0;i<SubChangeThresholdList.length;i++)
                    {
                        captureLog("[ Start ] SubscriptionChange CircleThreshold["+i+"] : "+ SubChangeThresholdList[i]);
                        String subChangeThreshold[]=SubChangeThresholdList[i].split(",");
                        if(subChangeThreshold.length!=2)
                        {
                            logger.error("[ Start ] Invalid SubscriptionChange CircleThreshold "+SubChangeThresholdList[i]+" Expected Format << Circlecode,Threshold >> Eg:- AP,5 - Configure the correct Circle Threshold and re-start webservice");
                        }
                        else 
                        {
                            String strCircle = subChangeThreshold[0].toUpperCase().trim();
                            int nSubChangeThreshold = 0;
                            try
                            {
                                nSubChangeThreshold=Integer.parseInt(subChangeThreshold[1].trim());
                            }
                            catch(Exception e)
                            {
                                logger.error("[ Start ] Exception while parsing Threshold value, Threshold value - " + subChangeThreshold[1].trim() + ". Subscription change orders for "+strCircle+" will not be processed - Configure the correct Circle Threshold and re-start webservice : " +e);
                            }
                            if(nSubChangeThreshold>=0)
                            {
                                String strFinalThreshold="";
                                for(int j=0;j<SubChangeRulesList.length;j++)
                                {
                                    if(strFinalThreshold.isEmpty())
                                    {
                                        strFinalThreshold=SubChangeRulenumList[j]+","+nSubChangeThreshold;
                                    }
                                    else
                                    {
                                        strFinalThreshold=strFinalThreshold+";"+SubChangeRulenumList[j]+","+nSubChangeThreshold;
                                    }
                                }
                                captureLog("[ Start ] SubscriptionChange Threshold for "+ strCircle +" circle is : "+ subChangeThreshold[1]+" -- "+strFinalThreshold);
                                try
                                {
                                    SubscriptionChangeThresholdMap.put(strCircle, strFinalThreshold);
                                }
                                catch(Exception e)
                                {
                                    logger.error("[ Start ] Exception while placing value in SubscriptionChangeThresholdMap, Invalid Subscription change threshold Configuration - Subscription Orders will not be processed for this circle (Please configure this property and re-start webservice) : "+strCircle +" : "+ strFinalThreshold +" : "+e);
                                }
                            }
                            else
                            {
                                logger.error("[ Start ] Subscription Change Threshold configured is invalid ");
                            }
                        }
                    }
                }    
            }
        }
        captureLog("[ Start ] [ --- Reading Subscription Change Properties End --- ]");
    }
    
    final synchronized void getAllConnectionPool() throws ClassNotFoundException, SQLException
    {
	if(connectionPoolList == null)
	{
	    connectionPoolList = new HashMap<String, BoneCP>();
            CircleUserNameMap = new HashMap<String, String>(); 
            //connectionPoolList = new HashMap<String, ConnectionPool>();

	    String type = CRSPropertyFileAndDbReader.getPropertyValueFromDb("db.type");
            if(type.isEmpty())
            {
                captureLog("[ Start ] [Property] db.type :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] db.type :" + type);
            }
	    String host = CRSPropertyFileAndDbReader.getPropertyValueFromDb("db.host");
            if(host.isEmpty())
            {
                captureLog("[ Start ] [Property] db.host :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] db.host :" + host);
            }
	    String port = CRSPropertyFileAndDbReader.getPropertyValueFromDb("db.port");
            if(port.isEmpty())
            {
                captureLog("[ Start ] [Property] db.port :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] db.port :" + port);
            }
	    String service = CRSPropertyFileAndDbReader.getPropertyValueFromDb("db.service");
            if(service.isEmpty())
            {
                captureLog("[ Start ] [Property] db.service :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] db.service :" + service);
            }
	    String user = CRSPropertyFileAndDbReader.getPropertyValueFromDb("db.user");
            if(user.isEmpty())
            {
                captureLog("[ Start ] [Property] db.user :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] db.user :" + user);
            }
	    String password = CRSPropertyFileAndDbReader.getPropertyValueFromDb("db.pwd");
            if(password.isEmpty())
            {
                captureLog("[ Start ] [Property] db.pwd :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] db.pwd :" + password);
            }
	    String strUrl = CRSPropertyFileAndDbReader.getPropertyValueFromDb("db.url");
            if(strUrl.isEmpty())
            {
                captureLog("[ Start ] [Property] db.url :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] db.url :" + strUrl);
            }
	    String schema = CRSPropertyFileAndDbReader.getPropertyValueFromDb("db.schema");
            if(schema.isEmpty())
            {
                captureLog("[ Start ] [Property] db.schema :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] db.schema :" + schema);
            }
	    String dbUrl = "";

	    if((strUrl != null) && !(strUrl.isEmpty()))
	    {
		dbUrl = strUrl;
	    }

	    CRSAuthentication objAuth = new CRSAuthentication();
	    String pwd = objAuth.Decrypt(password);
	    String strPoolSize = CRSPropertyFileAndDbReader.getPropertyValueFromDb("db.PoolSize");
            if(strPoolSize.isEmpty())
            {
                captureLog("[ Start ] [Property] db.PoolSize :Property is Empty, default value is - 20" );
            }
            else
            {
                captureLog("[ Start ] [Property] db.PoolSize :" + strPoolSize);
            }
	    int poolSize = 20;
	    if(strPoolSize != null && !strPoolSize.isEmpty())
	    {
		poolSize = Integer.parseInt(strPoolSize);
	    }	    
            String driver = getDriver(type);
	    String url = getUrl(type, host, port, service, dbUrl, schema);
            captureLog("[ Start ] [Property] BoneCP ConnectionPool creation Start  :" + user);
	    try
	    {

		Class c = Class.forName(driver);
                BoneCPConfig config = new BoneCPConfig();
                config.setJdbcUrl(url);// <url>
                config.setUsername(user);// <username>
                config.setPassword(pwd);// <password>
                config.setPartitionCount(propertyFileReader.getnPartitioncount());
                config.setMinConnectionsPerPartition(1);
                config.setMaxConnectionsPerPartition(poolSize);// <maxpool>,
                config.setConnectionTimeoutInMs(1800000);// <ConnectionTimeout>, milli seconds (1800000)/(1000*60) = 30 mins                
                BoneCP pool = null;
                 try {
                        pool = new BoneCP(config);
                        captureLog("[ Start ] [Property] BoneCP ConnectionPool creation End  :" + user);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        captureLog("[ Start ] [Property] BoneCP ConnectionPool creation Failed  :" + user);
                        captureLog(ex.getMessage());
//                        System.out.println(ex.getMessage());
                    }
//		ConnectionPool pool = new ConnectionPool("Parent", // <poolname>,
//			1, // <minpool>,
//			poolSize, // <maxpool>,
//			0, // <maxsize>,
//			1800, // <idleTimeout>, seconds
//			url, // <url>,
//			user, // <username>,
//			pwd// <password>
//		);
		connectionPoolList.put("Parent", pool);

		Connection parentDBConnection = pool.getConnection();

		if(parentDBConnection != null)
		{
		    Statement stmt = null;//parentDBConnection.createStatement();
		    // get all cirlce info by OrgId

		    String strTempCircleOrgId = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Circles.Config");
		    if(strTempCircleOrgId == null || strTempCircleOrgId.isEmpty())
		    {
			return;
		    }
		    String strTempCircleOrgIds[] = strTempCircleOrgId.split("\\$\\#\\$");
		    for (int i = 0; i < strTempCircleOrgIds.length; i++)
		    {
                        stmt = parentDBConnection.createStatement();
                        strTempCircleOrgId = strTempCircleOrgIds[i];
                        String[] strTemps = strTempCircleOrgId.split(",");
                        if (strTemps.length >= 1) 
                                                    {
                            createConnectionPoolforCircle(stmt, strTemps[0], poolSize, url, type);
                        }
                        if (stmt != null) 
                        {
                            stmt.close();
                        }
		    }
                    if(stmt != null)
		       stmt.close();
		    parentDBConnection.close();
		}
	    }
	    catch (SQLException exception)
	    {
		captureLog("SQL Exception while getting circle db details:" + exception);
	    }
	}
    }

    final synchronized void createConnectionPoolforCircle(Statement stmt, String CircleCode, int poolSize, String url, String type)
    {

	String query = "";
	ResultSet res = null;
	//ConnectionPool pool = null;
         BoneCP pool = null;
	String orgId = propertyFileReader.getCircleOrgId(CircleCode);

	if(!(orgId.isEmpty()))
	{
	    try
	    {
		if(type.equalsIgnoreCase(ORACLE))
		{
		    query = "SELECT USER_NAME,PWD FROM UP_CLOUD_ACCOUNT_INFO WHERE ORGID = " + orgId;
		}
		else if(type.equalsIgnoreCase(MYSQL))
		{
		    query = "SELECT USER_NAME,PWD,SCHEMA_NAME FROM UP_CLOUD_ACCOUNT_INFO WHERE ORGID = " + orgId;
		}
		else if(type.equalsIgnoreCase(POSTGRESQL))
		{
		    query = "SELECT USER_NAME,PWD,SCHEMA_NAME FROM UP_CLOUD_ACCOUNT_INFO WHERE ORGID = " + orgId;
		}

		res = stmt.executeQuery(query);
		String user = "";
		String pwd = "";
		String schema = "";
		while (res.next())
		{
		    user = res.getString(1);
		    pwd = res.getString(2);
		    if(type.equalsIgnoreCase(MYSQL) || type.equalsIgnoreCase(POSTGRESQL))
		    {
			schema = res.getString(3);
			int index = url.lastIndexOf('/');
			url = url.substring(0, index + 1);
			url += schema;
		    }

		}
		CircleUserNameMap.put(CircleCode, user);
                captureLog("[ Start ] [Property] BoneCP ConnectionPool creation Start  :" + CircleCode +","+user);
		if(!(user.isEmpty()) && !(pwd.isEmpty()) && (propertyFileReader.isCircleWiseDBConnectionPool()))
		{
		    BoneCPConfig config = new BoneCPConfig();
                    config.setJdbcUrl(url);// <url>
                    config.setUsername(user);// <username>
                    config.setPassword(pwd);// <password>
                    config.setPartitionCount(propertyFileReader.getnPartitioncount());
                    config.setMinConnectionsPerPartition(1);
                    config.setMaxConnectionsPerPartition(poolSize);// <maxpool>
                    config.setConnectionTimeoutInMs(1800000);// <ConnectionTimeout>, Milli seconds (1800000)/(1000*60) = 30 mins
                   
                    try {
                        pool = new BoneCP(config);
                        captureLog("[ Start ] [Property] BoneCP ConnectionPool creation End  :" + CircleCode +","+user);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        captureLog("[ Start ] [Property] BoneCP ConnectionPool creation Failed  :" + CircleCode +","+user);
                        captureLog(ex.getMessage());
//                        System.out.println(ex.getMessage());
                    }

                    
//                    pool = new ConnectionPool(CircleCode, // <poolname>,
//			    1, // <minpool>,
//			    poolSize, // <maxpool>,
//			    0, // <maxsize>,
//			    1800, // <idleTimeout>, seconds
//			    url, // <url>,
//			    user, // <username>,
//			    pwd// <password>
//		    );

		    connectionPoolList.put(CircleCode, pool);
		}
		res.close();
		// stmt.close();

		query = "SELECT CONNECTION_LIMIT,MASTER_SERVICE_INFO FROM " + user + ".DEDUPE_TEMPLATE_MAPPING";
		Connection DbConnection = null;
		if(propertyFileReader.isCircleWiseDBConnectionPool())
		{
		    DbConnection = pool.getConnection();
		}
		else
		{
		    DbConnection = connectionPoolList.get("Parent").getConnection();
		}
		if(DbConnection != null)
		{
		    Statement cirlceStmt = DbConnection.createStatement();
		    res = cirlceStmt.executeQuery(query);

		    int nConnectionLimit = 0;
		    int nCircleActiveState = 0;

		    while (res.next())
		    {
			nConnectionLimit = res.getInt(1);
			nCircleActiveState = res.getInt(2);
		    }

		    if(circleThresholdLmt == null)
		    {
			circleThresholdLmt = new HashMap<String, Integer>();
		    }

		    circleThresholdLmt.put(CircleCode, nConnectionLimit);
		    if(nCircleActiveState == 0)
		    {
			circleStateMap.put(CircleCode, Boolean.FALSE);
		    }

		    res.close();
		    cirlceStmt.close();
		    DbConnection.close();
		}

	    }
	    catch (SQLException exception)
	    {
		captureLog("SQL Exception while getting cirlce information:" + exception);
	    }
	}
    }

    final synchronized void initializeCirlceInfoMap()
    {

	// if (circleInfoMap == null) {
	// circleInfoMap = new HashMap<String, CRSOrganizationInfo>();
	//
	// //To do: get all details from database by each OrgId
	//
	//
	// CRSOrganizationInfo orgInfo = null;
	// //for UP
	// String OrgId = propertyFileReader.getCircleOrgId("UP");
	// //get all corresponding details by Query
	// if (true/*exists*/) {
	// orgInfo = new CRSOrganizationInfo();
	// orgInfo.setDbIPAddr("");
	// orgInfo.setDbPortNo("");
	// orgInfo.setDbPwd("");
	// orgInfo.setDbSchema("");
	// orgInfo.setDbType("");
	// orgInfo.setThresholdLimit(5);
	//
	// circleInfoMap.put("UP", orgInfo);
	// }
	//
	// OrgId = propertyFileReader.getCircleOrgId("UK");
	// //get all corresponding details by Query
	// if (true/*exists*/) {
	// orgInfo = new CRSOrganizationInfo();
	// orgInfo.setDbIPAddr("");
	// orgInfo.setDbPortNo("");
	// orgInfo.setDbPwd("");
	// orgInfo.setDbSchema("");
	// orgInfo.setDbType("");
	// orgInfo.setThresholdLimit(5);
	// circleInfoMap.put("UK", orgInfo);
	// }
	//
	// OrgId = propertyFileReader.getCircleOrgId("GJ");
	// //get all corresponding details by Query
	// if (true/*exists*/) {
	// orgInfo = new CRSOrganizationInfo();
	// orgInfo.setDbIPAddr("");
	// orgInfo.setDbPortNo("");
	// orgInfo.setDbPwd("");
	// orgInfo.setDbSchema("");
	// orgInfo.setDbType("");
	// orgInfo.setThresholdLimit(5);
	// circleInfoMap.put("GJ", orgInfo);
	// }
	//
	// OrgId = propertyFileReader.getCircleOrgId("BJ");
	// //get all corresponding details by Query
	// if (true/*exists*/) {
	// orgInfo = new CRSOrganizationInfo();
	// orgInfo.setDbIPAddr("");
	// orgInfo.setDbPortNo("");
	// orgInfo.setDbPwd("");
	// orgInfo.setDbSchema("");
	// orgInfo.setDbType("");
	// orgInfo.setThresholdLimit(5);
	// circleInfoMap.put("BJ", orgInfo);
	// }
	//
	// OrgId = propertyFileReader.getCircleOrgId("AP");
	// //get all corresponding details by Query
	// if (true/*exists*/) {
	// orgInfo = new CRSOrganizationInfo();
	// orgInfo.setDbIPAddr("");
	// orgInfo.setDbPortNo("");
	// orgInfo.setDbPwd("");
	// orgInfo.setDbSchema("");
	// orgInfo.setDbType("");
	// orgInfo.setThresholdLimit(5);
	// circleInfoMap.put("AP", orgInfo);
	// }
	//
	// OrgId = propertyFileReader.getCircleOrgId("MH");
	// //get all corresponding details by Query
	// if (true/*exists*/) {
	// orgInfo = new CRSOrganizationInfo();
	// orgInfo.setDbIPAddr("");
	// orgInfo.setDbPortNo("");
	// orgInfo.setDbPwd("");
	// orgInfo.setDbSchema("");
	// orgInfo.setDbType("");
	// orgInfo.setThresholdLimit(5);
	// circleInfoMap.put("MH", orgInfo);
	// }
	// }
    }
    
    final synchronized void getPropertiesFromFileandDb() throws ClassNotFoundException
    {
	if(propertyFileReader == null)
	{
	    propertyFileReader = new CRSPropertyFileAndDbReader();
	    propertyFileReader.setLogger(logger);            
	    propertyFileReader.initPropertyDbConnectionPool();
	    propertyFileReader.setJobDelimiter(CRSPropertyFileAndDbReader.getPropertyValueFromDb("app.Delimiter"));
            
            if(propertyFileReader.getJobDelimiter().isEmpty())
            {
                captureLog("[ Start ] [Property] app.Delimiter :Property is Empty, Default value is '|'" );
                propertyFileReader.setJobDelimiter("|");
            }
            else
            {
                captureLog("[ Start ] [Property] app.Delimiter :" + propertyFileReader.getJobDelimiter());
            }
            
	    propertyFileReader.setJobQualifier(CRSPropertyFileAndDbReader.getPropertyValueFromDb("app.Qualifier"));
            if(propertyFileReader.getJobDelimiter().isEmpty())
            {
                captureLog("[ Start ] [Property] app.Qualifier :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] app.Qualifier :" + propertyFileReader.getJobQualifier());
            }
	    /*String strTemp = CRSPropertyFileandDbReader.getPropertyValueFromDb("Circle.SearchNAdd");
	    if(strTemp.equals("1")) propertyFileReader.setCircleSearchNAddMode(true);
	    else
		propertyFileReader.setCircleSearchNAddMode(false);

	    strTemp = CRSPropertyFileandDbReader.getPropertyValueFromDb("Circle.SearchNAddVthBlk");
	    if(strTemp.equals("1")) propertyFileReader.setCircleSearchNAddVthBlkMode(true);
	    else
		propertyFileReader.setCircleSearchNAddVthBlkMode(false);*/

	    String strTemp = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Circle.ChkPrevPmsRespForErrJob");
            if(strTemp.isEmpty())
            {
                captureLog("[ Start ] [Property] Circle.ChkPrevPmsRespForErrJob :Property is Empty, default value is - 0(false)" );
            }
            else
            {
                captureLog("[ Start ] [Property] Circle.ChkPrevPmsRespForErrJob :" + strTemp);
            }
	    if(strTemp != null && strTemp.equals("1")) 
	    {
		propertyFileReader.setCircleChkPrevPmsRespForErrJob(true);
	    }
	    else
	    {
		propertyFileReader.setCircleChkPrevPmsRespForErrJob(false);
	    }
	    String strTempCirclesConfig = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Circles.Config");
	    if(strTempCirclesConfig == null || strTempCirclesConfig.isEmpty())
	    {
                logger.error("[ Start ] [Property] Circles.Config :Property is Empty");
		return;
	    }
            captureLog("[ Start ] [Property] Circle.Config :" + strTempCirclesConfig);
            
	    String strTempCircleConfigarr[] = strTempCirclesConfig.split("\\$\\#\\$");
	    // String strOrgId,strOrgName,strJobTemplate,strBlkJobTemplate;
	    for (int i = 0; i < strTempCircleConfigarr.length; i++)
	    {
		strTempCirclesConfig = strTempCircleConfigarr[i];
		String[] strTemps = strTempCirclesConfig.split(",");

		propertyFileReader.setCircleOrgId(strTemps[0], strTemps[1]);
		propertyFileReader.setCircleJobTemplate(strTemps[0], strTemps[2]);
		//propertyFileReader.setBlkJobTemplate(strTemps[0], strTemps[3]);

	    }

	    propertyFileReader.setCircleAddJobPkt(CRSPropertyFileAndDbReader.getPropertyValueFromDb("Circle.AddJobPkt"));
            if(propertyFileReader.getCircleAddJobPkt().isEmpty())
            {
                captureLog("[ Start ] [Property] Circle.AddJobPkt :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] Circle.AddJobPkt :" + propertyFileReader.getCircleAddJobPkt());
            }
	    propertyFileReader.setCircleSearchJobPkt(CRSPropertyFileAndDbReader.getPropertyValueFromDb("Circle.SearchJobPkt"));
            if(propertyFileReader.getCircleSearchJobPkt().isEmpty())
            {
                captureLog("[ Start ] [Property] Circle.SearchJobPkt :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] Circle.SearchJobPkt :" + propertyFileReader.getCircleSearchJobPkt());
            }
	    propertyFileReader.setCircleChurnJobPkt(CRSPropertyFileAndDbReader.getPropertyValueFromDb("Circle.ChurnJobPkt"));
            if(propertyFileReader.getCircleChurnJobPkt().isEmpty())
            {
                captureLog("[ Start ] [Property] Circle.ChurnJobPkt :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] Circle.ChurnJobPkt :" + propertyFileReader.getCircleChurnJobPkt());
            }

	    propertyFileReader.setCircleSearchAddJobPkt(CRSPropertyFileAndDbReader.getPropertyValueFromDb("Circle.SearchAddJobPkt"));
            if(propertyFileReader.getCircleSearchAddJobPkt().isEmpty())
            {
                captureLog("[ Start ] [Property] Circle.SearchAddJobPkt :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] Circle.SearchAddJobPkt :" + propertyFileReader.getCircleSearchAddJobPkt());
            }

            propertyFileReader.setCircleSearchAddDynamicThresholdJobPkt(CRSPropertyFileAndDbReader.getPropertyValueFromDb("Circle.SearchAddDynamicThresholdJobPkt"));
            if(propertyFileReader.getCircleSearchAddDynamicThresholdJobPkt().isEmpty())
            {
                logger.error("[ Start ] [Property] Circle.SearchAddDynamicThresholdJobPkt :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] Circle.SearchAddDynamicThresholdJobPkt :" + propertyFileReader.getCircleSearchAddDynamicThresholdJobPkt());
            }

            propertyFileReader.setSubscriptionChangeJobPkt(CRSPropertyFileAndDbReader.getPropertyValueFromDb("Circle.SearchWithDynamicThresholdJobPkt"));
            if(propertyFileReader.getSubscriptionChangeJobPkt().isEmpty())
            {
                logger.error("[ Start ] [Property] Circle.SearchWithDynamicThresholdJobPkt :Property is Empty" );
            }
            else
            {
                captureLog("[ Start ] [Property] Circle.SearchWithDynamicThresholdJobPkt :" + propertyFileReader.getSubscriptionChangeJobPkt());
            }
            /*propertyFileReader.setCircleSearchAddVthBlkJobPkt(CRSPropertyFileandDbReader.getPropertyValueFromDb("Circle.SearchAddVthBlkJobPkt"));
	    propertyFileReader.setCircleSearchVthBlkJobPkt(CRSPropertyFileandDbReader.getPropertyValueFromDb("Circle.SearchVthBlkJobPkt"));

	    propertyFileReader.setBlkLstAddJobPkt(CRSPropertyFileandDbReader.getPropertyValueFromDb("Blk.AddJobPkt"));
	    propertyFileReader.setBlkLstSearchJobPkt(CRSPropertyFileandDbReader.getPropertyValueFromDb("Blk.SearchJobPkt"));
	    propertyFileReader.setBlkLstChurnJobPkt(CRSPropertyFileandDbReader.getPropertyValueFromDb("Blk.ChurnJobPkt"));*/

	    String strTime = CRSPropertyFileAndDbReader.getPropertyValueFromDb("L.StartTime");
	    // Calendar cal = Calendar.getInstance();
	    if((strTime != null) && !(strTime.isEmpty()))
	    {

		String[] timeArr = strTime.split(":");

		// cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArr[0]));
		// // Your hour
		// cal.set(Calendar.MINUTE, Integer.parseInt(timeArr[1])); //
		// Your Mintue
		// cal.set(Calendar.SECOND, Integer.parseInt(timeArr[2])); //
		// Your second

		// propertyFileReader.setLeanStartTime(new
		// Time(cal.getTime().getTime()));
		propertyFileReader.setLeanStartTime(timeArr);
                captureLog("[ Start ] [Property] L.StartTime :" + strTime);

	    }
            else
            {
                captureLog("[ Start ] [Property] L.StartTime :Property is Empty");
            }

	    strTime = CRSPropertyFileAndDbReader.getPropertyValueFromDb("L.EndTime");
	    if((strTime != null) && !(strTime.isEmpty()))
	    {

		String[] timeArr = strTime.split(":");

		// cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArr[0]));
		// // Your hour
		// cal.set(Calendar.MINUTE, Integer.parseInt(timeArr[1])); //
		// Your Mintue
		// cal.set(Calendar.SECOND, Integer.parseInt(timeArr[2])); //
		// Your second

		// propertyFileReader.setLeanEndTime(new
		// Time(cal.getTime().getTime()));
		propertyFileReader.setLeanEndTime(timeArr);
                captureLog("[ Start ] [Property] L.EndTime :" + strTime);

	    }
            else
            {
                captureLog("[ Start ] [Property] L.EndTime :Property is Empty");
            }
	    String strcircledbconn = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Circles.CircleWiseDBConnectionPool");
	    if(strcircledbconn.isEmpty())
	    {
		captureLog("[ Start ] [Property] Circles.CircleWiseDBConnectionPool :Property is Empty, default value is - 1(true)");
	}
	    else
	    {
		captureLog("[ Start ] [Property] Circles.CircleWiseDBConnectionPool :" + strcircledbconn);
    }
	    if(strcircledbconn == null || strcircledbconn.isEmpty() || strcircledbconn.equals("1"))
	    {
		propertyFileReader.setCircleWiseDBConnectionPool(true);
	    }
	    else
	    {
		propertyFileReader.setCircleWiseDBConnectionPool(false);
	    }
	
	    String strTraceLog = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Server.TraceLog");
	    if(strTraceLog.isEmpty())
	    {
		captureLog("[ Start ] [Property] Server.TraceLog :Property is Empty, default value is - 0(false)");
            }
	    propertyFileReader.setTrace(strTraceLog); 

	    m_nTraceLevel = propertyFileReader.getTrace();
	    
            CRSOnline1VuWebService.logger.info("[ Start ] [Property] Server.TraceLog :" + m_nTraceLevel);
            
            String strTansactionIdval = CRSPropertyFileAndDbReader.getPropertyValueFromDb("Validation.Transaction_Id");
            
            if(strTansactionIdval.isEmpty())
            {
                propertyFileReader.setTransactionIdRegexp("[^a-zA-Z0-9~]");
                captureLog("[ Start ] [Property] Validation.Transaction_Id :Property is Empty , default Regular Expression : [^a-zA-Z0-9~] ");
            }
            else
            {
               propertyFileReader.setTransactionIdRegexp(strTansactionIdval);
               captureLog("[ Start ] [Property] Validation.Transaction_Id : Regular Expression : "+strTansactionIdval);
            } 
            
            String strpartitionCunt = CRSPropertyFileAndDbReader.getPropertyValueFromDb("db.PARTITION_COUNT");
            if (strpartitionCunt.isEmpty()) 
            {
                captureLog("[ Start ] [Property] db.PARTITION_COUNT :Property is Empty, default value is - 1");
                 propertyFileReader.setnPartitioncount(1);
            } else 
            {
                 propertyFileReader.setnPartitioncount(Integer.parseInt(strpartitionCunt));
            }
            captureLog("[ Start ] [Property] db.PARTITION_COUNT :" + propertyFileReader.getnPartitioncount());            
            
	}
    }
  /*  final synchronized void getPropertiesFromFile()
    {
	if(propertyFileReader == null)
	{
	    propertyFileReader = new CRSPropertyFileandDbReader();

	    propertyFileReader.setJobDelimiter(CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "app.Delimiter"));
	    propertyFileReader.setJobQualifier(CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "app.Qualifier"));

	    String strTemp = CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Circle.SearchNAdd");
	    if(strTemp.equals("1")) propertyFileReader.setCircleSearchNAddMode(true);
	    else
		propertyFileReader.setCircleSearchNAddMode(false);

	    strTemp = CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Circle.SearchNAddVthBlk");
	    if(strTemp.equals("1")) propertyFileReader.setCircleSearchNAddVthBlkMode(true);
	    else
		propertyFileReader.setCircleSearchNAddVthBlkMode(false);

	    strTemp = CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Circle.ChkPrevPmsRespForErrJob");
	    if(strTemp.equals("1")) propertyFileReader.setCircleChkPrevPmsRespForErrJob(true);
	    else
		propertyFileReader.setCircleChkPrevPmsRespForErrJob(false);

	    String strTempCirclesConfig = CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Circles.Config");
	    String strTempCircleConfigarr[] = strTempCirclesConfig.split("\\$\\#\\$");
	    // String strOrgId,strOrgName,strJobTemplate,strBlkJobTemplate;
	    for (int i = 0; i < strTempCircleConfigarr.length; i++)
	    {
		strTempCirclesConfig = strTempCircleConfigarr[i];
		String[] strTemps = strTempCirclesConfig.split(",");

		propertyFileReader.setCircleOrgId(strTemps[0], strTemps[1]);
		propertyFileReader.setCircleJobTemplate(strTemps[0], strTemps[2]);
		propertyFileReader.setBlkJobTemplate(strTemps[0], strTemps[3]);

	    }

	    propertyFileReader.setCircleAddJobPkt(CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Circle.AddJobPkt"));
	    propertyFileReader.setCircleSearchJobPkt(CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Circle.SearchJobPkt"));
	    propertyFileReader.setCircleChurnJobPkt(CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Circle.ChurnJobPkt"));

	    propertyFileReader.setCircleSearchAddJobPkt(CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Circle.SearchAddJobPkt"));
	    propertyFileReader.setCircleSearchAddVthBlkJobPkt(CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Circle.SearchAddVthBlkJobPkt"));
	    propertyFileReader.setCircleSearchVthBlkJobPkt(CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Circle.SearchVthBlkJobPkt"));

	    propertyFileReader.setBlkLstAddJobPkt(CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Blk.AddJobPkt"));
	    propertyFileReader.setBlkLstSearchJobPkt(CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Blk.SearchJobPkt"));
	    propertyFileReader.setBlkLstChurnJobPkt(CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "Blk.ChurnJobPkt"));

	    String strTime = CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "L.StartTime");
	    // Calendar cal = Calendar.getInstance();
	    if(!(strTime.isEmpty()))
	    {

		String[] timeArr = strTime.split(":");

		// cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArr[0]));
		// // Your hour
		// cal.set(Calendar.MINUTE, Integer.parseInt(timeArr[1])); //
		// Your Mintue
		// cal.set(Calendar.SECOND, Integer.parseInt(timeArr[2])); //
		// Your second

		// propertyFileReader.setLeanStartTime(new
		// Time(cal.getTime().getTime()));
		propertyFileReader.setLeanStartTime(timeArr);

	    }

	    strTime = CRSPropertyFileandDbReader.getPropertyValue("OneVuConfigurations.properties", "L.EndTime");
	    if(!(strTime.isEmpty()))
	    {

		String[] timeArr = strTime.split(":");

		// cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArr[0]));
		// // Your hour
		// cal.set(Calendar.MINUTE, Integer.parseInt(timeArr[1])); //
		// Your Mintue
		// cal.set(Calendar.SECOND, Integer.parseInt(timeArr[2])); //
		// Your second
		// propertyFileReader.setLeanEndTime(new
		// Time(cal.getTime().getTime()));
		propertyFileReader.setLeanEndTime(timeArr);

	    }
	}

    }*/
    private String getPreviousResponse(long currRequest, String username, String strTransactionID, BooleanWrapper obWrapper)
    {

	String strResponse = "";
	try
	{
	    //ConnectionPool poolparent = connectionPoolList.get("Parent");
	    
            BoneCP poolparent = connectionPoolList.get("Parent");
            Connection parentDbConnection = null;
	    if(poolparent != null)
	    {
		parentDbConnection = poolparent.getConnection();
	    }

	    if(parentDbConnection != null)
	    {

		// DatabaseMetaData DM = circleDbConnection.getMetaData();
		// String circleSchema = DM.getUserName();
		// /here MASTERINPUT is view of MASTERINPUT table.
		String pms_job_TransactionIDChkQry = "SELECT a.JOB_ID,a.STATUS,a.ACK_PACKET,b.JOBID from pms_job_info a," + username + "." + m_strMasterInputTable + " b where a.job_rt_info= ? and a.job_id = b.jobid(+)";
                captureLog("[" + currRequest + "] pms_job_TransactionIDChkQry:"+ pms_job_TransactionIDChkQry + "- Bind Values : Transaction_Id = "+strTransactionID);
		
                PreparedStatement Stmtpms_job_info = parentDbConnection.prepareStatement(pms_job_TransactionIDChkQry);
                Stmtpms_job_info.setString(1,strTransactionID);
                
		ResultSet respms_job_info = Stmtpms_job_info.executeQuery();
               
                if(respms_job_info.next())
		{// found in pms_job_info
		    int tempStatus = respms_job_info.getInt("STATUS");
		    int tempMasterJobid = respms_job_info.getInt("JOBID");
		    int tempPmsJobid = respms_job_info.getInt("JOB_ID");

		    if((tempStatus < 0) || ((tempStatus == 3) && (tempMasterJobid == 0)))
		    {
			// process again pms is not ok or not present in input
			// table(view);
			captureLog("[" + currRequest + "] previous status:" + tempStatus + "jobid:" + tempPmsJobid + "");
		    }
		    else if(tempStatus == 0 || tempStatus == 2)
		    {
			// dont process for still 1Vu processing.
			captureLog("[" + currRequest + "] previous job is under process, previous status:" + tempStatus + "jobid:" + tempPmsJobid + "");
			obWrapper.setWrapper(false);
		    }
		    else if(tempStatus == 3 && tempMasterJobid != 0)
		    {

			// dont process response is present in pms.
			strResponse = respms_job_info.getString("JOB_ID") + "\1" + "3" + "\1" + "1" + "\1" + respms_job_info.getString("ACK_PACKET");
			captureLog("[" + currRequest + "] previous response is " + strResponse + "");
		    }

		}
		else
		{
		    captureLog("[" + currRequest + "] previous response not found, process again.");
		}

		Stmtpms_job_info.close();
		respms_job_info.close();
		parentDbConnection.close();

	    }
	    else
	    {
		captureLog("[" + currRequest + "] getPreviousReponse failed : Parent Db connection not establish..");
		obWrapper.setWrapper(false);
		strResponse = "";
	    }
	}
	catch (Exception exception)
	{
	    captureLog("[" + currRequest + "] getPreviousResponse failed: " + exception);
	    obWrapper.setWrapper(false);
	    strResponse = "";

	}

	return strResponse;
    }
    
    boolean UpdateMasterInput(long currRequest, Connection circleDbConnection, String strCustomerId, String strOraNumber, StringBuilder stringBuilderTemp, String username)
    {
        boolean bRet = true;
        try
        {
            String strMasterInputUpdateQry = "UPDATE "+username+"."+m_strMasterInputTable+" SET CUSTOMER_ID = ? WHERE ORANUMBER = ?";
            
            
            captureLog("[" + currRequest + "] MasterInputUpdateQry:"+ strMasterInputUpdateQry + "- Bind Values : CUSTOMER_ID = "+strCustomerId + ", ORANUMBER = "+strOraNumber);
            
            PreparedStatement StmtMasterInputUpdate = circleDbConnection.prepareStatement(strMasterInputUpdateQry);
            StmtMasterInputUpdate.setString(1, strCustomerId);
            StmtMasterInputUpdate.setString(2, strOraNumber);
            int  nRowAffected = StmtMasterInputUpdate.executeUpdate();
            stringBuilderTemp.append(nRowAffected);
            StmtMasterInputUpdate.close();
        }
        catch (SQLException ex)
        {
            bRet = false;
            captureLog("[" + currRequest + "] UpdateMasterInput failed: " + ex);
        }
        
        return bRet;
    }
    
    void UpdateInputData(String[][] strMasterData,int nRow,int nCol,String [] inputParamValuearr)
    {
        for(int i = 0; i < nRow;i++)
        {
            for(int j=0;j<nCol;j++)
            {
                if(!m_hashSetIgnoreField.contains(j))
                {
                    if(!inputParamValuearr[j].isEmpty())
                    {
                        strMasterData[i][j] = inputParamValuearr[j];
                    }
                }
            }
        }
    }
    
    String getStringFromArray(String strArr[])
    {
        String strTemp = "";
        for(int i = 0; i<strArr.length;i++)
        {
            strTemp += strArr[i] + " ,";
        }
        strTemp = strTemp.substring(0,strTemp.lastIndexOf(",")-1) ;//remove last ,
        return strTemp;
    }
    
    String getStringFromArray(int strArr[])
    {
        String strTemp = "";
        for(int i = 0; i<strArr.length;i++)
        {
            strTemp += strArr[i] + " ,";
        }
        strTemp = strTemp.substring(0,strTemp.lastIndexOf(",")-1) ;//remove last ,
        return strTemp;
    }
    
    /**
     * Web service operation
     */
    @WebMethod(operationName = "doRequestDedupProcess")
    public CRSOnline1VuResponse[] doRequestDedupProcess(@WebParam(name = "inputRequest") CRSOnline1VuRequest inputRequest[])
    {
        java.sql.Timestamp reqTimeStamp_Batch = new java.sql.Timestamp(new java.util.Date().getTime());
        String strBatchTransactionIds = "", strTempTransactionId = "";
        int len = inputRequest.length;
        CRSOnline1VuResponse[] dedupResponse = new CRSOnline1VuResponse[len];
        for( int j=0; j<len; j++ )
        {
            dedupResponse[j] = new CRSOnline1VuResponse();
            strTempTransactionId = inputRequest[j].getTransactionID();
            dedupResponse[j].setErrCode(1000);
            dedupResponse[j].setDedupStatus("E");
            dedupResponse[j].setTransactionID(strTempTransactionId);            
            strBatchTransactionIds = strBatchTransactionIds.concat(strTempTransactionId+",");           
        }
        if(strBatchTransactionIds.endsWith(","))
           strBatchTransactionIds = strBatchTransactionIds.substring(0, strBatchTransactionIds.length()-1);
         captureLog("Batch Start [ Time : "+ reqTimeStamp_Batch +"] [Batch Count : "+len+"] [Tranacation Id(s) :"+strBatchTransactionIds+"]");
         for(int reqNum = 0 ; reqNum < len ; reqNum++)
            {
	java.sql.Timestamp reqTimeStamp = new java.sql.Timestamp(new java.util.Date().getTime());
	java.sql.Timestamp resTimeStamp = null;
	
	// DOMConfigurator.configure("D:/MyJava/Online1VuWebService/web/WEB-INF/classes/log4j.xml");

	// logger.info("Test Info call::");
	// logger.error("Test error call");
         int nDedupCnt = 0;
         String strRejReason = " ";
         String strJob_Id = "";
         String strRule = " ";
         String strTransactionID = "";
         String strWarning = "";
         
        
	try
	{

	    long currRequest = generateRequestNumber();

	    // initializeCirlceStateMap(); // need to check this

	    captureLog("[" + currRequest + "]  In doRequestDedupProcess method Call :");
	    
	    strTransactionID = inputRequest[reqNum].getTransactionID(); // mandotory
	    String strMobileNo = inputRequest[reqNum].getMobileNo();
	    String strSIMNo = inputRequest[reqNum].getSIMNo();
	    String strProductType = inputRequest[reqNum].getProductType();
	    String strDOB = inputRequest[reqNum].getDOB();
	    String strFullName = inputRequest[reqNum].getFullName(); // mandatory
	    String strFatherName = inputRequest[reqNum].getFatherName(); // mandatory
	    String strCircle_Code = inputRequest[reqNum].getCircle_Code(); // mandatory
	    String strPOI_Type = inputRequest[reqNum].getPOI_Type();
	    String strPOI_Id = inputRequest[reqNum].getPOI_Id(); // mandatory
	    String strPOA_Type = inputRequest[reqNum].getPOA_Type();
	    String strPOA_Id = inputRequest[reqNum].getPOA_Id();
	    String strCustomer_Category = inputRequest[reqNum].getCustomer_Category();
	    String strPresentAddress = inputRequest[reqNum].getPresentAddress();
	    String strPresent_HNo = inputRequest[reqNum].getPresent_HNo();
	    String strPresent_City = inputRequest[reqNum].getPresent_City();
	    String strPresent_District = inputRequest[reqNum].getPresent_District();
	    String strPresent_Pin = inputRequest[reqNum].getPresent_Pin();
	    String strPresent_State = inputRequest[reqNum].getPresent_State();
	    String strPermanent_Address = inputRequest[reqNum].getPermanent_Address();
	    String strPermanent_HNo = inputRequest[reqNum].getPermanent_HNo();
	    String strPermanent_City = inputRequest[reqNum].getPermanent_City();
	    String strPermanent_District = inputRequest[reqNum].getPermanent_District();
	    String strPermanent_Pin = inputRequest[reqNum].getPermanent_Pin();
	    String strPermanent_State = inputRequest[reqNum].getPermanent_State();
	    String strLocal_Ref_Msisdn = inputRequest[reqNum].getLocal_Ref_Msisdn();
	    String strLocal_Ref_name = inputRequest[reqNum].getLocal_Ref_name();
	    String strJobType = inputRequest[reqNum].getJobType();
	    String strOraNumber = inputRequest[reqNum].getOraNumber();
            String strCustomerId = inputRequest[reqNum].getCustomerId();
            String strBlackListFlag = inputRequest[reqNum].getBlackListFlag();
            String strOtherInfo1 = inputRequest[reqNum].getOtherInfo1();
            String strOtherInfo2 = inputRequest[reqNum].getOtherInfo2();
            
            String strOtherInfo3 = inputRequest[reqNum].getOtherInfo3();
            String strPan = inputRequest[reqNum].getPan();
            String strUid = inputRequest[reqNum].getuidai();
            String strOtherInfo4 = inputRequest[reqNum].getOtherInfo4();
            
//            String userName = inputRequest.getUserName();
//            String pwd = inputRequest.getPwd();
            boolean bSucess = true;
	    

	   // java.sql.Timestamp reqTimeStamp = new java.sql.Timestamp(new java.util.Date().getTime());
	  //  java.sql.Timestamp resTimeStamp = null;

	    
	    String strDedupStatus = "";
	    int nErrCode = 0;
	    String strErrDesc = "";
	   
	    
	   
	    
	    //Timestamp insertTimestamp = null;
	    //java.sql.Date dtActDate = null;
	    java.sql.Date dtDOB = null;
	    //boolean bSucess = true;

	    Map<String, String> CircleOrgId = propertyFileReader.getcircleOrgId();// getCircleOrgId();
            
	    captureLog("[" + currRequest + "] Requested TransactionID::" + strTransactionID);
            
//            if (userName == null || userName.trim().equals("") || !(userName.equalsIgnoreCase("1VuAdmin"))) {
//                bSucess = false;
//                nErrCode = 1015;
//                strDedupStatus = "ERROR";
//                strErrDesc = "Unauthorized UserName";
//                captureLog("[" + currRequest + "] Unauthorized UserName");
//            } else if (pwd == null || pwd.trim().equals("") || !(pwd.equalsIgnoreCase("BA56979EF85EBF26AD"))) {
//                bSucess = false;
//                nErrCode = 1016;
//                strDedupStatus = "ERROR";
//                strErrDesc = "Unauthorized Password";
//                captureLog("[" + currRequest + "] Unauthorized Password");
//            } 
           // if(bSucess)
           // {
                if (strTransactionID == null || strTransactionID.trim().equals("")) 
                {
                    bSucess = false;
                    nErrCode = 1015;
                    strDedupStatus = "ERROR";
                    strErrDesc = "Unauthorized Transaction Id";
                    captureLog("[" + currRequest + "] Unauthorized Transaction Id");
                } else 
                {
                    Matcher m = propertyFileReader.getTransactionIdRegexp().matcher(strTransactionID);
                    if (m.find()) 
                    {
                        bSucess = false;
                        nErrCode = 1016;
                        strDedupStatus = "ERROR";
                        strErrDesc = "Invalid Transaction Id";
                        captureLog("[" + currRequest + "] Invalid Transaction Id : " + strTransactionID);
                    }
                }
           // }
            String strFieldValuesArr[] =
                { strBlackListFlag, strCircle_Code,strCustomerId,strCustomer_Category, strDOB,
                  strFatherName, strFullName, strJobType, strLocal_Ref_Msisdn, strLocal_Ref_name, strMobileNo,
                  strOraNumber,strOtherInfo1,strOtherInfo2,strOtherInfo3,strOtherInfo4,strPOA_Id, strPOA_Type, strPOI_Id,
                  strPOI_Type,strPan,strPermanent_Address, strPermanent_City, strPermanent_District, 
                  strPermanent_HNo, strPermanent_Pin, strPermanent_State, strPresentAddress, strPresent_City,
                  strPresent_District, strPresent_HNo, strPresent_Pin, strPresent_State, strProductType, 
                  strSIMNo, strTransactionID,strUid };

	   
            
            int MandatoryFieldIndexes[] = {};
            int i=0;
            if(bSucess)
            {
                if (strJobType.equals("0") || strJobType.equals("1") || strJobType.equals("2") || strJobType.equals("5") || strJobType.equals("7") || strJobType.equals("8")) {
                    MandatoryFieldIndexes = m_MandatoryFieldIndexs;
                } else if (strJobType.equals("3")) {
                    //take index3.  
                    MandatoryFieldIndexes = m_MandatoryFieldIndexs3;
                } else if (strJobType.equals("4")) {
                    //take index4
                    MandatoryFieldIndexes = m_MandatoryFieldIndexs4;

                } else if (strJobType.equals("6")) {
                    //take index6
                    MandatoryFieldIndexes = m_MandatoryFieldIndexs6;
                }
             
                //int i; 
                for (i = 0; i < MandatoryFieldIndexes.length; i++)
                {
                    if(MandatoryFieldIndexes[i]!= -1)
                    {
                        if(strFieldValuesArr[MandatoryFieldIndexes[i]].isEmpty())

                        {
                            bSucess = false;
                            nErrCode = 1004;
                            strDedupStatus = "E";
                            String strParam = m_strFieldNameArr[MandatoryFieldIndexes[i]];
                            strErrDesc = "Insufficient Data Parameters." + strParam + " is Empty";
                            captureLog("[" + currRequest + "] Insufficient Data Parameters" + strParam + " is Empty");
                            break;
                        }
                    }
                    else
                    {
                        bSucess = false;
                        nErrCode = 1011;
                        strDedupStatus = "E";
                        String strParam=getStringFromArray(m_strFieldNameArr); 
                        strErrDesc = "Incorrect Mandatory field name, at Position : " + (i+1);
                        captureLog("[" + currRequest + "] Incorrect Mandatory field name, at Position : " + (i+1));
                        captureLog("[" + currRequest + "] Correct Mandatory field names are : " + strParam);
                        break;
                    }
                }
            }
            if (  bSucess && (i == MandatoryFieldIndexes.length))
            {


                if(!CircleOrgId.containsKey(strCircle_Code))
                {
                    bSucess = false;
                    nErrCode = 1005;
                    strDedupStatus = "E";
                    strErrDesc = "Invalid Circle Code";
                    captureLog("[" + currRequest + "] Invalid Circle Code");
                }
                else
                {
                    if(bSucess && !(strDOB.isEmpty()))
                    {

                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
                        try
                        {
                            dtDOB = new java.sql.Date(formatter.parse(strDOB).getTime());
                        }
                        catch (ParseException e)
                        {
                            bSucess = false;
                            nErrCode = 1003;
                            strDedupStatus = "E";
                            strErrDesc = "Invalid DOB Date format";
                            captureLog("[" + currRequest + "] Invalid DOB Date format " + strDOB + "Exe: " + e);
                        }
                    }
                }
            }

	    if(bSucess)
	    {

		// String strReqJobType = "";
		String strReqProcessStatus = "S";
		// String strReqComments = "";
		//String strTrackJobType = "";
		//String strTrackProcessStatus = "";
		//String strTrackComments = "";
		// int nBlackListJob = 0;
		//String strTrackReplyData = "";
		boolean bProcess = false;
		int nReqType = 0; // 0-Regular 1-Retry(Reg) 2- Retry (Pending)
				  // 3- Retry while processing
		int nRetryProcess = 0; // 0-Default 1-Retry Process running
		String username = CircleUserNameMap.get(strCircle_Code);
		//ConnectionPool pool = null;
                
                BoneCP pool = null;
		if(propertyFileReader.isCircleWiseDBConnectionPool())
		{
		    pool = connectionPoolList.get(strCircle_Code);
		    if(m_nTraceLevel > 1)
		    {
			captureLog("[" + currRequest + "] "+ username + "  Circle wise DB connection");
		    }
		}
		else
		{
		    pool = connectionPoolList.get("Parent");
		    if(m_nTraceLevel > 1)
		    {
			captureLog("[" + currRequest + "] " + username + " Centralized connection");
		    }
		}
		Connection circleDbConnection = null;
		if(pool != null)
		{
		    circleDbConnection = pool.getConnection(); // here  DbConnectionPool is parent ConnectionPool for centralized, DbConnectionPool is circle ConnectionPool for circle wise
		}
		else
		{
		    captureLog("[" + currRequest + "] Connection Pool Not Establishes");
		}

		// Need to handle RETRY mechanism

		if(circleDbConnection != null)
		{
		    String transactionIDChkQry = "SELECT RETRY_PROCESS FROM " + username + ".ONLINE_JOB_INFO WHERE TRANSACTION_ID = ?";
		    PreparedStatement cirlceStmt = circleDbConnection.prepareStatement(transactionIDChkQry);
                    cirlceStmt.setString(1, strTransactionID);
		    ResultSet res = cirlceStmt.executeQuery();
		    if(m_nTraceLevel > 1)
		    {
			captureLog("[" + currRequest + "] Query : " + transactionIDChkQry + "- Bind Values : Transaction_Id = "+strTransactionID); 
		    }
		    if(res.next())
		    {
			nRetryProcess = res.getInt(1);
			if(nRetryProcess == 1)
			{ // If it is processing, wait for some time...
			    nReqType = 3;
			    captureLog("[" + currRequest + "] It is a Retry Request for while Processing job");

			    try
			    {
				Thread.sleep(4000);
			    }
			    catch (InterruptedException ie)
			    {
				// Handle exception
			    }
			}
                        if(res != null)
                           res.close();
                        if(cirlceStmt != null)
                           cirlceStmt.close();
                        
			transactionIDChkQry = "SELECT  A.REPLY_STATE,A.REPLY_CNT,A.ERR_CODE,A.COMMENTS,A.RULE,A.ERR_DESC,A.SUBSTATUS,B.JOB_ID FROM " + username + ".ONLINE_REQUEST_RESPONSE A,"+ username + ".ONLINE_REQUEST_TRACKING B WHERE (A.TRANSACTION_ID = ?) AND (A.TRANSACTION_ID = B.TRANSACTION_ID(+))";
                        cirlceStmt = circleDbConnection.prepareStatement(transactionIDChkQry);
                        cirlceStmt.setString(1,strTransactionID);
			res = cirlceStmt.executeQuery();
			if(m_nTraceLevel > 1)
			{
			    captureLog("[" + currRequest + "] Query : " + transactionIDChkQry + "- Bind Values : Transaction_Id = "+strTransactionID);
 			}
			if(res.next())
			{
			    strDedupStatus = res.getString(1);
			    if(nRetryProcess == 1)
			    { // get the processed job status and sent back.
				nDedupCnt = res.getInt(2);
				nErrCode = res.getInt(3);
				strRejReason = res.getString(4);
				strRule = res.getString(5);
				strErrDesc = res.getString(6);
				strWarning = res.getString(7);
				strJob_Id = Integer.toString(res.getInt(8));
			    }
			    else
			    {

				if(strDedupStatus.equalsIgnoreCase("I") || strDedupStatus.equalsIgnoreCase(("E")) || strDedupStatus.equalsIgnoreCase("L"))
				{
				    nReqType = 2;
				    captureLog("[" + currRequest + "] It is a Retry Request for " + strDedupStatus + " job");
				}
				else
				{
				    nReqType = 1;
				    captureLog("[" + currRequest + "] It is a Retry Request for SUCCESS job");

				    nDedupCnt = res.getInt(2);
				    nErrCode = res.getInt(3);
				    strRejReason = res.getString(4);
				    strRule = res.getString(5);
				    strErrDesc = res.getString(6);
				    strWarning = res.getString(7);
				    strJob_Id = Integer.toString(res.getInt(8));
				}
			    }
			}
			else
			{
			    // nReqType = 0;
			    // captureLog("[" + currRequest +
			    // "] It is a Reguler Request");
			    nReqType = 3;
			    captureLog("[" + currRequest + "] It is a Retry Request for while Processing job");

			    try
			    {
				Thread.sleep(4000);
			    }
			    catch (InterruptedException ie)
			    {
				// Handle exception
			    }

                            if(res != null)
                                res.close();
                            if(cirlceStmt != null)
                               cirlceStmt.close();
			    transactionIDChkQry = "SELECT  A.REPLY_STATE,A.REPLY_CNT,A.ERR_CODE,A.COMMENTS,A.RULE,A.ERR_DESC,A.SUBSTATUS,B.JOB_ID FROM " + username + ".ONLINE_REQUEST_RESPONSE A," + username + ".ONLINE_REQUEST_TRACKING B WHERE (A.TRANSACTION_ID = ?) AND (A.TRANSACTION_ID = B.TRANSACTION_ID(+))";
			    cirlceStmt = circleDbConnection.prepareStatement(transactionIDChkQry);
                            cirlceStmt.setString(1, strTransactionID);
			    res = cirlceStmt.executeQuery();
			    if(m_nTraceLevel > 1)
			    {
				captureLog("[" + currRequest + "] Query : " + transactionIDChkQry + "- Bind Values : Transaction_Id = "+strTransactionID );
			    }
			    if(res.next())
			    {
				strDedupStatus = res.getString(1);
				nDedupCnt = res.getInt(2);
				nErrCode = res.getInt(3);
				// strRule = res.getString(6);
				strErrDesc = strRejReason = res.getString(4);
				strRule = res.getString(5);
				strErrDesc = res.getString(6);
				strWarning = res.getString(7);
				strJob_Id = Integer.toString(res.getInt(8));
			    }
			    else
			    {
				strDedupStatus = "P";
				// strReqJobType = " ";
				nErrCode = 1006;
				strErrDesc = "Request under process already";
				captureLog("[" + currRequest + "] Request under process already");
			    }

			}
                        if(res != null)
                            res.close();
			if(cirlceStmt != null)
                            cirlceStmt.close();
			// circleDbConnection.close();
		    }
		    else
		    {
			if(res != null)
                            res.close();
			if(cirlceStmt != null)
                            cirlceStmt.close();
			nReqType = 0;
			captureLog("[" + currRequest + "] It is a Reguler Request");
			String onlinejobinsertqury = onlineJobInsertQuery.replace("<<USER_NAME>>", username);
			PreparedStatement prepStmt = circleDbConnection.prepareStatement(onlinejobinsertqury);
			if(m_nTraceLevel > 1)
			{
			    captureLog("[" + currRequest + "] Query : " + onlinejobinsertqury);
			}
			prepStmt.setString(1, strTransactionID);
			prepStmt.setTimestamp(2, reqTimeStamp);
			prepStmt.setInt(3, 0);

			if(prepStmt.executeUpdate() >= 1)
			{
			    captureLog("[" + currRequest + "] Insert query success for ONLINE_JOB_INFO table");
			}
			else
			{
			    captureLog("[" + currRequest + "] Insert query failed for ONLINE_JOB_INFO table");
			}

			prepStmt.close();
		    }

		    if(nReqType != 1 && nReqType != 3)
		    {  
                        String[] timeArr = propertyFileReader.getLeanStartTime();
                        Time leanStTime=null,leanETime=null,currTime=null;
                        if(timeArr!=null)
                        {
                            Calendar cal = Calendar.getInstance();

                            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArr[0])); // Your hour
                            cal.set(Calendar.MINUTE, Integer.parseInt(timeArr[1])); // Your Mintue
                            cal.set(Calendar.SECOND, Integer.parseInt(timeArr[2])); // Your second

                            leanStTime = new Time(cal.getTime().getTime());
                           
                            timeArr = propertyFileReader.getLeanEndTime();

                            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArr[0])); // Your hour
                            cal.set(Calendar.MINUTE, Integer.parseInt(timeArr[1])); // Your Mintue
                            cal.set(Calendar.SECOND, Integer.parseInt(timeArr[2])); // Your second

                            leanETime = new Time(cal.getTime().getTime());
                            java.util.Date date1 = new java.util.Date();
                            currTime = new Time(date1.getTime());
                        }
                    
                        if(leanStTime != null && leanETime != null && currTime.after(leanStTime) && currTime.before(leanETime))
                        {
                            strDedupStatus = "L";
                           // strReqJobType  = " ";
                            nErrCode = 1007;
                            strErrDesc = "Unable to Process the request. Service in Lean time";
                            captureLog("[" + currRequest + "] Unable to Process the request. Service in Lean time");

                        }
                        else if(circleStateMap.get(strCircle_Code))
			{

                            bProcess = true;

                            if (nReqType == 2)
                            {
				                String strOnlnebUpdtQuery = onlineJobUpdateQuery.replace("<<USER_NAME>>", username);
			                	PreparedStatement prepStmt = circleDbConnection.prepareStatement(strOnlnebUpdtQuery);
			                	if(m_nTraceLevel > 1)
				                {
				                  captureLog("[" + currRequest + "] Query : " + strOnlnebUpdtQuery);
				                }
                                prepStmt.setInt(1, 1);
                                prepStmt.setString(2, strTransactionID);

                                if (prepStmt.executeUpdate() >= 1)
                                {

                                    captureLog("[" + currRequest + "] Update query success for ONLINE_JOB_INFO table while processing Retry");

                                } else
                                {
                                    captureLog("[" + currRequest + "] Update query failed for ONLINE_JOB_INFO table while processing Retry");
                                }

                                prepStmt.close();
                            }
                            captureLog("[" + currRequest + "] Requested MSISDN::" + strMobileNo);
                            captureLog("[" + currRequest + "] Requested CustomerName::" + strFullName);
                            captureLog("[" + currRequest + "] Requested FatherName::" + strFatherName);
                            String delim = propertyFileReader.getJobDelimiter();
                            String qulfr = propertyFileReader.getJobQualifier();
                            String strOrgId = propertyFileReader.getCircleOrgId(strCircle_Code);
                            String strJobPkt = "";
                            // String strAddJobId = "";
                            // String strChurnJobId = "";
                            // String strSearchJobId = "";
                            // String strBlkLstJobId = "";
                            String masterTemplateName = "";
                            //String blkLstTemplateName = "";

                            int nThresholdLmt = circleThresholdLmt.get(strCircle_Code);
                            //captureLog("[" + currRequest + "] Threshold Limt::" + nThresholdLmt);
                            //according to online csv..
                            String inputParamValue = qulfr + strMobileNo + qulfr + delim + qulfr + strSIMNo + qulfr + delim + qulfr + strProductType + qulfr + delim + qulfr + strDOB + qulfr + delim + qulfr + strFullName
                                    + qulfr + delim + qulfr + strFatherName + qulfr + delim + qulfr + strCircle_Code + qulfr + delim + qulfr + strPOI_Type + qulfr + delim + qulfr
                                    + strPOI_Id + qulfr + delim + qulfr + strPOA_Type + qulfr + delim + qulfr + strPOA_Id + qulfr + delim + qulfr + strCustomer_Category
                                    + qulfr + delim + qulfr + strPresentAddress + qulfr + delim + qulfr + strPresent_HNo + qulfr + delim + qulfr + strPresent_City + qulfr + delim + qulfr
                                    + strPresent_District + qulfr + delim + qulfr + strPresent_Pin + qulfr + delim + qulfr + strPresent_State + qulfr + delim + qulfr + strPermanent_Address + qulfr
                                    + delim + qulfr + strPermanent_HNo + qulfr + delim + qulfr + strPermanent_City + qulfr + delim + qulfr + strPermanent_District + qulfr + delim + qulfr
                                    + strPermanent_Pin + qulfr + delim + qulfr + strPermanent_State + qulfr + delim + qulfr + strLocal_Ref_Msisdn + qulfr
                                    + delim + qulfr + strLocal_Ref_name + qulfr + delim + qulfr + strOraNumber + qulfr + delim + qulfr + strCustomerId + qulfr + delim + qulfr + strBlackListFlag + qulfr + delim + qulfr + strOtherInfo1 + qulfr + delim + qulfr + strOtherInfo2 + qulfr + delim + qulfr + strOtherInfo3+ qulfr + delim + qulfr + strOtherInfo4+ qulfr + delim + qulfr+ strUid + qulfr + delim + qulfr + strPan +qulfr + "$#$1";
                            
                            //according to soap....
                            String inputParamValuearr[] = {
                                                            strBlackListFlag,strCircle_Code, strCustomerId,strCustomer_Category,
                                                             strDOB, strFatherName, strFullName,strJobType,strLocal_Ref_Msisdn, strLocal_Ref_name, 
                                                            strMobileNo,strOraNumber,strOtherInfo1,strOtherInfo2,strOtherInfo3,strOtherInfo4,strPOA_Id, strPOA_Type, strPOI_Id, strPOI_Type,strPan,
                                                            strPermanent_Address, strPermanent_City, strPermanent_District, strPermanent_HNo, strPermanent_Pin, 
                                                            strPermanent_State, strPresentAddress, strPresent_City, strPresent_District,
                                                            strPresent_HNo, strPresent_Pin, strPresent_State, strProductType, strSIMNo,strTransactionID,strUid
                                                           /*strMobileNo,strSIMNo,strProductType,strDOB,strFullName,
                                                           strFatherName,strCircle_Code,inputRequest.getPOI_Type(),
                                                           strPOI_Id,inputRequest.getPOA_Type(),inputRequest.getPOA_Id(),strCustomer_Category,
                                                           strPresentAddress,strPresent_HNo,strPresent_City,strPresent_District,strPresent_Pin,
                                                           strPresent_State,strPermanent_Address,strPermanent_HNo,strPermanent_City,strPermanent_District,
                                                           strPermanent_Pin,strPermanent_State,strLocal_Ref_Msisdn,strLocal_Ref_name,strOraNumber,strCustomerId,strBlackListFlag,
                                                           strOtherInfo1,strOtherInfo2*/
                                                          };

                            inputParamValue = inputParamValue.replaceAll("'", "''");

                            masterTemplateName = propertyFileReader.getCircleJobTemplate(strCircle_Code);
                            // blkLstTemplateName = propertyFileReader.getBlkJobTemplate(strCircle_Code);

                            String strResponse = "";
                            String strPrevDedupStatus = strDedupStatus;
                            strDedupStatus = "S";
                            // strReqComments = "''";
                            // strRule = "R1";
                            if (strJobType.equalsIgnoreCase("0"))
                            {//search
                                strJobPkt = propertyFileReader.getCircleSearchJobPkt();
                                strJobPkt = strJobPkt.replace("<<InputParmas>>", inputParamValue);
                                strJobPkt = strJobPkt.replace("<<TemplateName>>", masterTemplateName);
                                strJobPkt = strJobPkt.replace("<<OrgId>>", strOrgId);
                                strJobPkt = strJobPkt.replace("<<TransctionId>>", strTransactionID);
                                captureLog("[" + currRequest + "] Dedup Master Search JobPacket after replacing  ::" + strJobPkt);

                                strResponse = RunDaemonJob(strOrgId, strJobPkt, currRequest, true);

                                if (strResponse.isEmpty())
                                {
                                    strDedupStatus = "E";
                                    nErrCode = 1002;
                                    strErrDesc = "Connection Not Establishes";
                                    captureLog("[" + currRequest + "] Connection Not Establishes");

                                }
                                else
                                {
                                    captureLog("[" + currRequest + "] Response from 1Vu::" + strResponse);
                                    String[] responseArr = strResponse.split("\\$\\#\\$");
                                    String[] responseArr1 = responseArr[0].split("\1");
                                    strJob_Id = responseArr1[0];
                                    strResponse = responseArr[1];
                                    responseArr = strResponse.split("~");
                                    if (responseArr[0].equalsIgnoreCase("0"))
                                    {
                                        strDedupStatus = "E";
                                        nErrCode = 999;// 1Vu Error.
                                        strErrDesc = responseArr[1];
                                        captureLog("[" + currRequest + "] 1Vu App Error.");
                                    }
                                    else
                                    {
                                        if (responseArr[0].equalsIgnoreCase("1"))
                                        {
                                            strDedupStatus = "S";
                                        }
                                        else if (responseArr[0].equalsIgnoreCase("2")||responseArr[0].equalsIgnoreCase("5"))
                                        {
                                            strDedupStatus = "F";
                                        }
//                                        else if(responseArr[0].equalsIgnoreCase("5"))
//                                        {
//                                           strDedupStatus = "A"; 
//                                        }
                                        else if (responseArr[0].equalsIgnoreCase("3"))
                                        {
                                            strDedupStatus = "R";
                                        }
                                        
                                        if (responseArr[1].equalsIgnoreCase("1"))
                                        {
                                            strWarning = "W";
                                        }
                                        else if(!responseArr[1].equalsIgnoreCase("0"))
                                        {
                                            // 0 - no warning
                                            // 1 - warning
                                            // other - substatus information
                                            strWarning = responseArr[1];
                                        }
                                        
                                        if (!responseArr[2].equalsIgnoreCase("0"))
                                        {
                                            strRule = "R" + responseArr[2];
                                        }
                                        nDedupCnt = Integer.parseInt(responseArr[3]);

                                        captureLog("[" + currRequest + "] Dedup match count::" + nDedupCnt);
					insertintoTrackTable(currRequest, circleDbConnection, strTransactionID, "S", responseArr1[1], "", 0, strJob_Id, responseArr[3], "Search", "Master Data",username);
                                    }
                                }
                            }
                            else if (strJobType.equalsIgnoreCase("1"))
                            {// add

                                strJobPkt = propertyFileReader.getCircleAddJobPkt(); // Add
                                // job
                                // packet
                                strJobPkt = strJobPkt.replace("<<InputParmas>>", inputParamValue);
                                strJobPkt = strJobPkt.replace("<<TemplateName>>", masterTemplateName);
                                strJobPkt = strJobPkt.replace("<<OrgId>>", strOrgId);
                                strJobPkt = strJobPkt.replace("<<TransctionId>>", strTransactionID);
                                captureLog("[" + currRequest + "] Dedup Add JobPacket after replacing  ::" + strJobPkt);

                                //boolean bFlag = true;
                                //m_bFlagPreviousResponse = true;
                                BooleanWrapper obWrapper = new BooleanWrapper(true);
                                if (propertyFileReader.isCircleChkPrevPmsRespForErrJob() && nReqType == 2 && strPrevDedupStatus.equalsIgnoreCase(("E")))
                                {
				                    strResponse = getPreviousResponse(currRequest, username, strTransactionID, obWrapper);
                                    if(!strResponse.isEmpty())
                                    {
                                        String[] responseArr = strResponse.split("\\$\\#\\$");
                                        strJob_Id = responseArr[0].split("\1")[0];
                                    }
                                }
                                if (strResponse.isEmpty() && obWrapper.getWrapper())
                                { // if it processing dont initiate again..
                                    strResponse = RunDaemonJob(strOrgId, strJobPkt, currRequest, true);
                                    //strJob_Id = RunDaemonJob(strOrgId, strJobPkt, currRequest, true);
                                }

                                if (strResponse.isEmpty())
                                { 
                                    strDedupStatus = "E";
                                    nErrCode = 1002;
                                    strErrDesc = "Connection Not Establishes";
                                    captureLog("[" + currRequest + "] Connection Not Establishes");
                                    // insert in ONLINE_REQUEST_TRACKING table

                                }
                                else
                                {
                                    captureLog("[" + currRequest + "]Add Job:: Response from 1Vu::" + strResponse);
                                    captureLog("[" + currRequest + "] It is Dedup Request -> Added");
                                    String[] responseArr = strResponse.split("\1");
                                    strJob_Id = responseArr[0];
                                    
				    insertintoTrackTable(currRequest, circleDbConnection, strTransactionID, "A", responseArr[1], "", 0, strJob_Id, responseArr[3], "Add", "Master Data", username);
                                    
                                    strResponse = responseArr[3].substring(responseArr[3].indexOf(",") + 1);
                                    strResponse = strResponse.substring(strResponse.indexOf("/")+1);

                                    if (Integer.parseInt(strResponse) == 0)
                                    {
                                        strDedupStatus = "E";
                                        nErrCode = 1017;
                                        strErrDesc = " Add request Failed";
                                        captureLog("[" + currRequest + "]  FAILED : Add request Failed");
                                    }
                                }

                            }
                            else if (strJobType.equalsIgnoreCase("2"))
                            {// search n add

                                strJobPkt = propertyFileReader.getCircleSearchAddJobPkt();
                                strJobPkt = strJobPkt.replace("<<InputParmas>>", inputParamValue);
                                strJobPkt = strJobPkt.replace("<<TemplateName>>", masterTemplateName);
                                strJobPkt = strJobPkt.replace("<<Threshold>>", Integer.toString(nThresholdLmt));
                                strJobPkt = strJobPkt.replace("<<OrgId>>", strOrgId);
                                strJobPkt = strJobPkt.replace("<<TransctionId>>", strTransactionID);
                                captureLog("[" + currRequest + "] Dedup Master SearchAdd JobPacket after replacing  ::" + strJobPkt);
                                
                                BooleanWrapper obWrapper = new BooleanWrapper(true);
                                if (propertyFileReader.isCircleChkPrevPmsRespForErrJob() && nReqType == 2 && strPrevDedupStatus.equalsIgnoreCase(("E")))
                                {
				                    strResponse = getPreviousResponse(currRequest, username, strTransactionID, obWrapper);
                                }
                                if (strResponse.isEmpty() && obWrapper.getWrapper())
                                { // if it processing dont initiate again..

                                    strResponse = RunDaemonJob(strOrgId, strJobPkt, currRequest, true);
                                }
                                if (strResponse.isEmpty())
                                {
                                    strDedupStatus = "E";
                                    nErrCode = 1002;
                                    strErrDesc = "Connection Not Establishes";
                                    captureLog("[" + currRequest + "] Connection Not Establishes");

                                }
                                else
                                {
                                    captureLog("[" + currRequest + "] Response from 1Vu::" + strResponse);
                                    String[] responseArr = strResponse.split("\\$\\#\\$");
                                    String[] responseArr1 = responseArr[0].split("\1");
                                    strJob_Id = responseArr1[0];
                                    strResponse = responseArr[1];
                                    responseArr = strResponse.split("~");
                                    if (responseArr[0].equalsIgnoreCase("0"))
                                    {
                                        strDedupStatus = "E";
                                        nErrCode = 999;// 1Vu Error.
                                        strErrDesc = responseArr[1];
                                        captureLog("[" + currRequest + "] 1Vu App Error.");
                                    }
                                    else
                                    {
                                        if (responseArr[0].equalsIgnoreCase("1"))
                                        {
                                            strDedupStatus = "S";
                                        }
                                        else if (responseArr[0].equalsIgnoreCase("2"))
                                        {                                           
                                            strDedupStatus = "F";                                                                                                                  
                                        }
                                        else if(responseArr[0].equalsIgnoreCase("5"))
                                        {
                                           strDedupStatus = "A"; 
                                        }
                                        else if (responseArr[0].equalsIgnoreCase("3"))
                                        {
                                            strDedupStatus = "R";
                                        }
                                        
                                        if (responseArr[1].equalsIgnoreCase("1"))
                                        {
                                            strWarning = "W";
                                        }
                                        else if(!responseArr[1].equalsIgnoreCase("0"))
                                        {
                                            // 0 - no warning
                                            // 1 - warning
                                            // other - substatus information
                                            strWarning = responseArr[1];
                                        }

                                        if (!responseArr[2].equalsIgnoreCase("0"))
                                        {
                                            strRule = "R" + responseArr[2];
                                        }
                                        nDedupCnt = Integer.parseInt(responseArr[3]);
                                        captureLog("[" + currRequest + "] Dedup match count::" + nDedupCnt);
					insertintoTrackTable(currRequest, circleDbConnection, strTransactionID, "SA", responseArr1[1], "", 0, strJob_Id, responseArr[3], "SearchAndAdd", "Master Data",username);
                                    }
                                }
                            }
                            else if (strJobType.equalsIgnoreCase("3"))
                            {// churn

                                captureLog("[" + currRequest + "] It is Churn request");

                                strJobPkt = propertyFileReader.getCircleChurnJobPkt();
                                strJobPkt = strJobPkt.replace("<<InputParmas>>", inputParamValue);
                                strJobPkt = strJobPkt.replace("<<TemplateName>>", masterTemplateName);
                                strJobPkt = strJobPkt.replace("<<TransctionId>>", strTransactionID);
                                strJobPkt = strJobPkt.replace("<<OrgId>>", strOrgId);
                                strJobPkt = strJobPkt.replace("<<TransctionId>>", strTransactionID);
                                strJobPkt = strJobPkt.replace("<<ChurnColumnName>>","");
                                captureLog("[" + currRequest + "] Dedup Master Churn JobPacket after replacing  ::" + strJobPkt);
                                strResponse = RunDaemonJob(strOrgId, strJobPkt, currRequest, true);

                                if (strResponse.isEmpty())
                                {
                                    strDedupStatus = "E";
                                    nErrCode = 1002;
                                    strErrDesc = "Connection Not Establishes";
                                    captureLog("[" + currRequest + "] Connection Not Establishes");

                                }
                                else
                                {

                                    captureLog("[" + currRequest + "]Churn Job:: Response from 1Vu::" + strResponse);
                                    String[] responseArr = strResponse.split("\1");
                                    strJob_Id = responseArr[0];

                                    // insert in ONLINE_REQUEST_TRACKING table

				                    insertintoTrackTable(currRequest, circleDbConnection, strTransactionID, "C", responseArr[1], "", 0, strJob_Id, responseArr[3], "Churn", "Churn", username);

                                    strResponse = responseArr[3].substring(responseArr[3].indexOf(",") + 1);
                                    strResponse = strResponse.substring(0, strResponse.indexOf("/"));

                                    if (Integer.parseInt(strResponse) == 0 /*
                                             * get
                                             * churned
                                             * count
                                             */)
                                    {
                                        strDedupStatus = "F";
                                        nErrCode = 1008;
                                        strErrDesc = "Mobile Number Not Existing";
                                        captureLog("[" + currRequest + "]  FAILED : Mobile Number Not Existing");
                                    }

                                }
                            }
                            else if(strJobType.equalsIgnoreCase("4")) 
                            {//direct update MASTER_INPUT
                                StringBuilder stringBuilderTemp = new StringBuilder();
                                captureLog("[" + currRequest + "] It is direct update reqeust JobType:"+strJobType);
                                captureLog("[" + currRequest + "] strCustomerId :"+strCustomerId+"strOraNumber:"+strOraNumber);
				if(!UpdateMasterInput(currRequest, circleDbConnection, strCustomerId, strOraNumber, stringBuilderTemp, username))
                                {
                                    strDedupStatus = "E";
                                    nErrCode = 1009;
                                    strErrDesc = "Unable to Update Master Input table";
                                    captureLog("[" + currRequest + "] Unable to Update Master Input table");
                                }
                                else
                                {
                                  strWarning = stringBuilderTemp.toString();
                                  if(strWarning.equalsIgnoreCase("0"))
                                  {
                                      strDedupStatus = "E";
                                      nErrCode = 1013;
                                      strErrDesc = "No data found for given OraNumber";
                                      captureLog("[" + currRequest + "] No data found for given OraNumber");
                                  }
                                  else
                                  {
                                      captureLog("[" + currRequest + "]"+ strWarning + ":Row Updated in Master Input table");  
                                  }
                                }
                            }
                            else if (strJobType.equalsIgnoreCase("5"))
                            {//caf_update

                                //strReqJobType = "U";
                                //strDedupStatus = "SUCCESS";
                                // If it is for Update, Prepare Churn n Add job request n submit to master.
                                captureLog("[" + currRequest + "] It is CAF_UPDATE request");

                                //TemplateName = propertyFileReader.getCircleChurnTemplte();
                                strJobPkt = propertyFileReader.getCircleChurnJobPkt();
                                strJobPkt = strJobPkt.replace("<<InputParmas>>", inputParamValue);
                                strJobPkt = strJobPkt.replace("<<TemplateName>>", masterTemplateName);
                                strJobPkt = strJobPkt.replace("<<TransctionId>>", strTransactionID);
                                strJobPkt = strJobPkt.replace("<<OrgId>>", strOrgId);
                                strJobPkt = strJobPkt.replace("<<ChurnColumnName>>","");
                                captureLog("[" + currRequest + "] Dedup Master Churn JobPacket after replacing  ::" + strJobPkt);
                                strResponse = RunDaemonJob(strOrgId, strJobPkt, currRequest, true);

                                if (strResponse.isEmpty())
                                {
                                    strDedupStatus = "E";
                                    nErrCode = 1002;
                                    strErrDesc = "Connection Not Establishes";
                                    captureLog("[" + currRequest + "] Connection Not Establishes");

                                }
                                else
                                {
                                    captureLog("[" + currRequest + "]CAF_UPDATE CHURN Job:: Response from 1Vu::" + strResponse);
                                    String[] responseArr = strResponse.split("\1");
                                    strJob_Id = responseArr[0];

                                    //insert in ONLINE_REQUEST_TRACKING table

				                    insertintoTrackTable(currRequest, circleDbConnection, strTransactionID, "C", responseArr[1], "", 0, strJob_Id, responseArr[3], "Churn", "CAFUpdate", username);

                                    strResponse = responseArr[3].substring(responseArr[3].indexOf(",") + 1);
                                    strResponse = strResponse.substring(0, strResponse.indexOf("/"));

                                    if (Integer.parseInt(strResponse) == 0 /* get churned count*/)
                                    {
                                        strDedupStatus = "F";
                                        nErrCode = 1008;
                                        strErrDesc = "Mobile Number Not Existing";
                                        captureLog("[" + currRequest + "]  CAF_UPDATE request : CHURN FAILED : Mobile Number Not Existing");
                                    }
                                    else
                                    {
                                        captureLog("[" + currRequest + "] It is CAF_UPDATE request -> Churned");
                                        //TemplateName = propertyFileReader.getCircleAddTemplte();
                                        strJobPkt = propertyFileReader.getCircleAddJobPkt(); // Add job packet to master data
                                        strJobPkt = strJobPkt.replace("<<InputParmas>>", inputParamValue);
                                        strJobPkt = strJobPkt.replace("<<TemplateName>>", masterTemplateName);
                                        strJobPkt = strJobPkt.replace("<<TransctionId>>", strTransactionID);
                                        strJobPkt = strJobPkt.replace("<<OrgId>>", strOrgId);
                                        captureLog("[" + currRequest + "] Dedup Add JobPacket after replacing  ::" + strJobPkt);
                                        strResponse = RunDaemonJob(strOrgId, strJobPkt, currRequest, true);

                                        if (strResponse.isEmpty())
                                        {
                                            strDedupStatus = "E";
                                            nErrCode = 1002;
                                            strErrDesc = "Connection Not Establishes";
                                            captureLog("[" + currRequest + "] Connection Not Establishes");
                                        }
                                        else
                                        {
                                            captureLog("[" + currRequest + "]CAF_UPDATE ADD Job:: Response from 1Vu::" + strResponse);
                                            String[] responsecCOFArr = strResponse.split("\1");
                                            strJob_Id = responsecCOFArr[0];
                                            //insert in ONLINE_REQUEST_TRACKING table
                                            insertintoTrackTable(currRequest, circleDbConnection, strTransactionID, "A", responsecCOFArr[1], "", 0, strJob_Id, responsecCOFArr[3], "Add", "CAFUpdate", username);
                                            
                                            strResponse = responsecCOFArr[3].substring(responsecCOFArr[3].indexOf(",") + 1);
                                            strResponse = strResponse.substring(strResponse.indexOf("/")+1);

                                            if (Integer.parseInt(strResponse) == 0)
                                            {
                                                strDedupStatus = "F";
                                                nErrCode = 1018;
                                                strErrDesc = "Add Request Failed";
                                                captureLog("[" + currRequest + "] It is a CAF_UPDATE request : Add request Failed");
                                            }
                                            else
                                            {
                                                captureLog("[" + currRequest + "] It is CAF_UPDATE request -> Added");                                                
                                            }

                                        }


                                    }
                                }
                            }
                            else if(strJobType.equalsIgnoreCase("6"))
                            {
                                if(m_obIgnoreFieldFlag.getWrapper())
                                {
                             
                                strWarning = "0";
                                //direct update MASTER_INPUT but address also so little complex..
                                //  ResultSet resMasterInput  = getDataFromMaster(currRequest,circleDbConnection,strCustomerId,strOraNumber);
                                captureLog("[" + currRequest + "] It is update request based on CustomerID : JobType:" + strJobType);
                                
                                                              /*String strMasterInputSelectQry = "SELECT MSISDN,SIM_NO,PRODUCT_TYPE,DOB,NAME,FATHER_NAME"
                                        + ",CIRCLE_CODE,POI_TYPE,POI_ID,POA_TYPE,POA_ID,CUSTOMER_CATEGORY,PRESENTADDRESS"
                                        + ",PRESENT_HNO,PRESENT_CITY,PRESENT_DISTRICT,PRESENT_PIN,PRESENT_STATE,PERMANENT_ADDRESS"
                                        + ",PERMANENT_HNO,PERMANENT_CITY,PERMANENT_DISTRICT,PERMANENT_PIN,PERMANENT_STATE,"
                                        + "LOCAL_REF_MSISDN,LOCAL_REF_NAME,ORANUMBER,CUSTOMER_ID,BLACKLIST_FLAG,OTHER_INFO1,OTHER_INFO2 FROM "+m_MasterTable+" WHERE"
                                        + " CUSTOMER_ID =" + strCustomerId;*/
                                String strMasterInputSelectQry =  m_strMasterInputSelectQry;
                                strMasterInputSelectQry  = strMasterInputSelectQry.replace("<<MASTER_INPUT>>", m_strMasterInputTable);
				                strMasterInputSelectQry = strMasterInputSelectQry.replace("<<USER_NAME>>", username);
                                captureLog("[" + currRequest + "] strMasterInputSelectQry:"+ strMasterInputSelectQry +" JobType:" + strJobType + "- Bind Values : Customer_Id ="+strCustomerId);
                                
                                
                                PreparedStatement StmtMasterInputSelect = circleDbConnection.prepareStatement(strMasterInputSelectQry,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);//.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                                StmtMasterInputSelect.setString(1, strCustomerId);
                                ResultSet resMasterInput = StmtMasterInputSelect.executeQuery();//executeQuery(strMasterInputSelectQry);
                                                                
                                int nCol = resMasterInput.getMetaData().getColumnCount();
                                resMasterInput.last();
                                int nRow = resMasterInput.getRow();
                                String strMasterData[][]=new String[nRow][nCol];
                                
                                String strMasterInputVal="";
                                if(resMasterInput.first())
                                {
                                    int j = 0;
                                    i = 0;
                                    do
                                    {
                                        for (j = 0; j < nCol; j++)
                                        {
                                            strMasterInputVal = resMasterInput.getString(j + 1);
                                            if(strMasterInputVal==null) // Checking to handlle Null values(Otherwise it will fill 'null' as a string )
                                            {
                                                strMasterInputVal="";
                                            }
                                            strMasterData[i][j] = strMasterInputVal;                                            
                                        }
                                        i++;
                                    }while (resMasterInput.next());
                                }
                                resMasterInput.close();
                                StmtMasterInputSelect.close();
                                
                                if (nRow > 0 && nCol > 0)
                                {
                                    /******************************/
                                    //churn job
                                    strJobPkt = propertyFileReader.getCircleChurnJobPkt();
                                    strJobPkt = strJobPkt.replace("<<InputParmas>>", inputParamValue);
                                    strJobPkt = strJobPkt.replace("<<TemplateName>>", masterTemplateName);
                                    strJobPkt = strJobPkt.replace("<<TransctionId>>", strTransactionID);
                                    strJobPkt = strJobPkt.replace("<<OrgId>>", strOrgId);
                                    strJobPkt = strJobPkt.replace("<<TransctionId>>", strTransactionID);
                                    strJobPkt = strJobPkt.replace("<<ChurnColumnName>>", m_strChurnColumnName);
                                    captureLog("[" + currRequest + "] Dedup Master Churn JobPacket after replacing  ::" + strJobPkt);
                                    strResponse = RunDaemonJob(strOrgId, strJobPkt, currRequest, true);

                                    if (strResponse.isEmpty())
                                    {
                                        strDedupStatus = "E";
                                        nErrCode = 1002;
                                        strErrDesc = "Unable to Churn for JobType:"+strJobType;
                                        captureLog("[" + currRequest + "] Unable to Churn for JobType:"+ strJobType);

                                    }
                                    /******************************churn done*/
                                    else
                                    {
                                        captureLog("[" + currRequest + "] It is update request based on CustomerID -> Churned");
                                        String[] responseArr = strResponse.split("\1");
                                        strJob_Id = responseArr[0];
                                        strResponse = responseArr[3].substring(responseArr[3].indexOf(",") + 1);
                                        strResponse = strResponse.substring(0, strResponse.indexOf("/"));
                                        // insert in ONLINE_REQUEST_TRACKING table
					insertintoTrackTable(currRequest, circleDbConnection, strTransactionID, "C", responseArr[1], "", 0, strJob_Id, responseArr[3], "Churn","Master Data(CustomerId)", username);
                                        
                                        if (Integer.parseInt(strResponse) == 0 /* get churned count*/)
                                        {
                                            strDedupStatus = "F";
                                            nErrCode = 1008;
                                            strErrDesc = "FAILED : Unable to churn based on CustomerId";
                                            captureLog("[" + currRequest + "]  FAILED : Unable to churn based on CustomerId");
                                        }
                                        strJob_Id = "";
                                        //1.ignorefield()
                                        UpdateInputData(strMasterData, nRow, nCol, inputParamValuearr);
                                        //2.for(addJobPacket) with modify data.
                                        for (i = 0; i < nRow; i++)
                                        {
                                            inputParamValue = "";
                                            for(int j=0;j < m_OnlineSoapTocsvMapping.length;j++)
                                            {
                                                inputParamValue += qulfr+strMasterData[i][m_OnlineSoapTocsvMapping[j]]+qulfr;
                                                if(j != m_OnlineSoapTocsvMapping.length-1)
                                                    inputParamValue += delim;
                                            }
                                            
                                            inputParamValue += "$#$1";
                                            //send add job packet
                                            strJobPkt = propertyFileReader.getCircleAddJobPkt(); // Add job packet 
                                            strJobPkt = strJobPkt.replace("<<InputParmas>>", inputParamValue);
                                            strJobPkt = strJobPkt.replace("<<TemplateName>>", masterTemplateName);
                                            strJobPkt = strJobPkt.replace("<<OrgId>>", strOrgId);
                                            strJobPkt = strJobPkt.replace("<<TransctionId>>", strTransactionID);
                                            captureLog("[" + currRequest + "] Dedup Add JobPacket after replacing  ::" + strJobPkt);
                                            strJob_Id = RunDaemonJob(strOrgId, strJobPkt, currRequest, false);

                                            if (strJob_Id.isEmpty())
                                            {
                                                strDedupStatus = "E";
                                                nErrCode = 1002;
                                                strErrDesc = "Unable Add for JobType:"+strJobType;
                                                captureLog("[" + currRequest + "] Unable Add for JobType:"+strJobType);
                                                break;
                                                // insert in ONLINE_REQUEST_TRACKING table

                                            }
                                            else
                                            {
                                                captureLog("[" + currRequest + "] It is update request based on CustomerID -> Added");
						insertintoTrackTable(currRequest, circleDbConnection, strTransactionID, "A", "3", "", 0, strJob_Id, "", "Add", "Master Data(CustomerId)", username);
                                            }

                                        }
                                        strWarning = Integer.toString(i); 

                                    }


                                }
                                else
                                {
                                    //no record found in master input with this customerId.
                                    strDedupStatus = "E";
                                    nErrCode = 1010;
                                    strErrDesc = "No data found for given customerId";
                                    captureLog("[" + currRequest + "] No data found for given customerId");
                                }
                                }
                                else
                                {                               
                                    strDedupStatus = "E";
                                    nErrCode = 1012;
                                    String strParam = getStringFromArray(m_strFieldNameArr);
                                    strErrDesc = "Incorrect Ignore field name, please correct the Ignore Field property value";
                                    captureLog("[" + currRequest + "] Incorrect Ignore field name, please correct the Ignore Field property value");
                                    captureLog("[" + currRequest + "] Correct Mandatory field names are : " + strParam);
                                 }
                            } 
                            else if (strJobType.equalsIgnoreCase("7"))
                            {//search for Subscription Change orders
                                String strDynamicThresholds="";  
                                try
                                {
                                    strDynamicThresholds=SubscriptionChangeThresholdMap.get(strCircle_Code).trim();
                                }
                                catch(Exception e)
                                {
                                    if(SubscriptionChangeThresholdMap.size()==0)
                                    {
                                        logger.error("[" + currRequest + "] Subscription Change Threshold map is empty - Subscription Change Orders will not be processed (Please configure this property and re-start webservice)" +e);
                                    }
                                    else
                                    {
                                        logger.error("[" + currRequest + "] Error : Subscription Change Threshold is not configured for this circle" +strCircle_Code+" : "+ e);
                                    }
                                }
                                if(strDynamicThresholds.isEmpty() || strDynamicThresholds==null)
                                {
                                    strDedupStatus = "E";
                                    nErrCode = 1019;
                                    strErrDesc = "Subscription Change Threshold not configured";
                                    logger.error("[" + currRequest + "] Error : Subscription Change Threshold not configured");
                                }
                                else
                                {
                                    strJobPkt = propertyFileReader.getSubscriptionChangeJobPkt();
                                    strJobPkt = strJobPkt.replace("<<InputParmas>>", inputParamValue);
                                    strJobPkt = strJobPkt.replace("<<TemplateName>>", masterTemplateName);
                                    strJobPkt = strJobPkt.replace("<<OrgId>>", strOrgId);
                                    strJobPkt = strJobPkt.replace("<<TransctionId>>", strTransactionID);
                                    strJobPkt = strJobPkt.replace("<<DynamicThresholds>>",strDynamicThresholds);
                                    captureLog("[" + currRequest + "] Dedup Master SearchWithDynamicThreshold (For Subscription Change Orders) JobPacket after replacing  ::" + strJobPkt);

                                    strResponse = RunDaemonJob(strOrgId, strJobPkt, currRequest, true);

                                    if (strResponse.isEmpty())
                                    {
                                        strDedupStatus = "E";
                                        nErrCode = 1002;
                                        strErrDesc = "Connection Not Establishes";
                                        captureLog("[" + currRequest + "] Connection Not Establishes");
                                    }
                                    else
                                    {
                                        captureLog("[" + currRequest + "] Response from 1Vu::" + strResponse);
                                        String[] responseArr = strResponse.split("\\$\\#\\$");
                                        String[] responseArr1 = responseArr[0].split("\1");
                                        strJob_Id = responseArr1[0];
                                        strResponse = responseArr[1];
                                        responseArr = strResponse.split("~");
                                        if (responseArr[0].equalsIgnoreCase("0"))
                                        {
                                            strDedupStatus = "E";
                                            nErrCode = 999;// 1Vu Error.
                                            strErrDesc = responseArr[1];
                                            captureLog("[" + currRequest + "] 1Vu App Error.");
                                        }
                                        else
                                        {
                                            if (responseArr[0].equalsIgnoreCase("1"))
                                            {
                                                strDedupStatus = "S";
                                            }
                                            else if (responseArr[0].equalsIgnoreCase("2"))
                                            {
                                                strDedupStatus = "F";
                                            }
                                            else if(responseArr[0].equalsIgnoreCase("5"))
                                            {
                                                strDedupStatus = "A"; 
                                            }
                                            else if (responseArr[0].equalsIgnoreCase("3"))
                                            {
                                                strDedupStatus = "R";
                                            }
                                             
                                            if (responseArr[1].equalsIgnoreCase("1"))
                                            {
                                                strWarning = "W";
                                            }
                                            else if(!responseArr[1].equalsIgnoreCase("0"))
                                            {
                                                // 0 - no warning
                                                // 1 - warning
                                                // other - substatus information
                                                strWarning = responseArr[1];
                                            }
                                      
                                            if (!responseArr[2].equalsIgnoreCase("0"))
                                            {
                                                strRule = "R" + responseArr[2];
                                            }
                                            nDedupCnt = Integer.parseInt(responseArr[3]);
                                             
                                            captureLog("[" + currRequest + "] Dedup match count::" + nDedupCnt);
                                            insertintoTrackTable(currRequest, circleDbConnection, strTransactionID, "S", responseArr1[1], "", 0, strJob_Id, responseArr[3], "Search", "Master Data",username);
                                        }
                                    }
                                }
                            }
                            else if (strJobType.equalsIgnoreCase("8"))
                            {// search n add with Dynamic threshold
                                String strDynamicThresholds="";  
                                try
                                {
                                    strDynamicThresholds=MNPThresholdMap.get(strCircle_Code).trim();
                                }
                                catch(Exception e)
                                {
                                    if(MNPThresholdMap.size()==0)
                                    {
                                        logger.error("[" + currRequest + "] MNP Threshold map is empty - MNP Orders will not be processed (Please configure this property and re-start webservice)" +e);
                                    }
                                    else
                                    {
                                        logger.error("[" + currRequest + "] Error : MNP Threshold is not configured for this circle" +strCircle_Code+" : "+ e);
                                    }
                                }
                                if(strDynamicThresholds.isEmpty() || strDynamicThresholds==null)
                                {
                                    strDedupStatus = "E";
                                    nErrCode = 1021;
                                    strErrDesc = "MNP Threshold not configured";
                                    logger.error("[" + currRequest + "] Error : MNP Threshold not configured");
                                }
                                else
                                {
                                    strJobPkt = propertyFileReader.getCircleSearchAddDynamicThresholdJobPkt();
                                    strJobPkt = strJobPkt.replace("<<InputParmas>>", inputParamValue);
                                    strJobPkt = strJobPkt.replace("<<TemplateName>>", masterTemplateName);
                                    strJobPkt = strJobPkt.replace("<<Threshold>>", Integer.toString(nThresholdLmt));
                                    strJobPkt = strJobPkt.replace("<<OrgId>>", strOrgId);
                                    strJobPkt = strJobPkt.replace("<<TransctionId>>", strTransactionID);
                                    strJobPkt = strJobPkt.replace("<<DynamicThresholds>>", strDynamicThresholds);
                                    captureLog("[" + currRequest + "] Dedup Master SearchAddWithDynamicThreshold JobPacket (For MNP Order) after replacing  ::" + strJobPkt);
                                    
                                    BooleanWrapper obWrapper = new BooleanWrapper(true);
                                    if (propertyFileReader.isCircleChkPrevPmsRespForErrJob() && nReqType == 2 && strPrevDedupStatus.equalsIgnoreCase(("E")))
                                    {
                                        strResponse = getPreviousResponse(currRequest, username, strTransactionID, obWrapper);
                                    }
                                    if (strResponse.isEmpty() && obWrapper.getWrapper())
                                    { // if it processing dont initiate again..
                                        strResponse = RunDaemonJob(strOrgId, strJobPkt, currRequest, true);
                                    }
                                    if (strResponse.isEmpty())
                                    {
                                        strDedupStatus = "E";
                                        nErrCode = 1002;
                                        strErrDesc = "Connection Not Establishes";
                                        captureLog("[" + currRequest + "] Connection Not Establishes");
                                    }
                                    else
                                    {
                                        captureLog("[" + currRequest + "] Response from 1Vu::" + strResponse);
                                        String[] responseArr = strResponse.split("\\$\\#\\$");
                                        String[] responseArr1 = responseArr[0].split("\1");
                                        strJob_Id = responseArr1[0];
                                        strResponse = responseArr[1];
                                        responseArr = strResponse.split("~");
                                        if (responseArr[0].equalsIgnoreCase("0"))
                                        {
                                            strDedupStatus = "E";
                                            nErrCode = 999;// 1Vu Error.
                                            strErrDesc = responseArr[1];
                                            captureLog("[" + currRequest + "] 1Vu App Error.");
                                        }
                                        else
                                        {
                                            if (responseArr[0].equalsIgnoreCase("1"))
                                            {
                                                strDedupStatus = "S";
                                            }
                                            else if (responseArr[0].equalsIgnoreCase("2"))
                                            {                                           
                                                strDedupStatus = "F";                                                                                                                  
                                            }
                                            else if(responseArr[0].equalsIgnoreCase("5"))
                                            {
                                                strDedupStatus = "A"; 
                                            }
                                            else if (responseArr[0].equalsIgnoreCase("3"))
                                            {
                                                strDedupStatus = "R";
                                            }
                           
                                            if (responseArr[1].equalsIgnoreCase("1"))
                                            {
                                                strWarning = "W";
                                            }
                                            else if(!responseArr[1].equalsIgnoreCase("0"))
                                            {
                                                // 0 - no warning
                                                // 1 - warning
                                                // other - substatus information
                                                strWarning = responseArr[1];
                                            }

                                            if (!responseArr[2].equalsIgnoreCase("0"))
                                            {
                                                strRule = "R" + responseArr[2];
                                            }
                                            nDedupCnt = Integer.parseInt(responseArr[3]);
                                            captureLog("[" + currRequest + "] Dedup match count::" + nDedupCnt);
                                            
                                            insertintoTrackTable(currRequest, circleDbConnection, strTransactionID, "SA", responseArr1[1], "", 0, strJob_Id, responseArr[3], "SearchAndAdd", "Master Data",username);
                                        }
                                    }
                                }
                            }                            
                            else
                            {
                                strDedupStatus = "E";
                                nErrCode = 1020;
                                strErrDesc = "Invalid Jobtype";
                                logger.error("[" + currRequest + "] Error : Jobtype is invalid : "+strJobType);   
                            }
                        }
			else
			{
			    strDedupStatus = "I";
			    // strReqJobType = " ";
			    nErrCode = 1007;
			    strErrDesc = "Unable to Process the request. Cirlce is inActtive state";
			    logger.error("[" + currRequest + "] Unable to Process the request. Cirlce is inActtive state");

			}
		    }
		    // Index all detatils into into
		    // ‘ONLINE_REQUEST_RESPONSE’ and
		    // ‘ONLINE_REQUEST_TRACKING’ tables for tracking

		    // --> TODO (by thread to do offline if it is taking time )
		    // ConnectionPool pool =
		    // connectionPoolList.get(strCircle_Code);
		    if(nReqType == 0)
		    { // For Reguler Insert case
			try
			{
			    // if (pool != null) {

			    // Connection circleDbConnection =
			    // pool.getConnection();

			    if(circleDbConnection != null)
			    {

				// Statement circleStmt =
				// circleDbConnection.createStatement();

				resTimeStamp = new java.sql.Timestamp(new java.util.Date().getTime());
				// insert in ONLINE_REQUEST_RESPONSE table

				String strrequestInsertquery = requestInsertQuery.replace("<<USER_NAME>>", username);
				if(m_nTraceLevel > 1)
				{
				    captureLog("[" + currRequest + "] Query : " + strrequestInsertquery);
				}
				PreparedStatement prepStmt = circleDbConnection.prepareStatement(strrequestInsertquery);
				prepStmt.setString(1, strTransactionID);
				prepStmt.setString(2, strMobileNo);
				prepStmt.setString(3, strSIMNo);
				prepStmt.setString(4, strProductType);
				prepStmt.setDate(5, dtDOB);
				prepStmt.setString(6, strFullName);
				prepStmt.setString(7, strFatherName);
				//prepStmt.setInt(8, Integer.parseInt(propertyFileReader.getCircleOrgId(strCircle_Code)));
				prepStmt.setString(8, strCircle_Code);
				prepStmt.setString(9, strPOI_Type);
				prepStmt.setString(10, strPOI_Id);
				prepStmt.setString(11, strPOA_Type);
				prepStmt.setString(12, strPOA_Id);
				prepStmt.setString(13, strCustomer_Category);
				prepStmt.setString(14, strPresentAddress);
				prepStmt.setString(15, strPresent_HNo);
				prepStmt.setString(16, strPresent_City);
				prepStmt.setString(17, strPresent_District);
				prepStmt.setString(18, strPresent_Pin);
				prepStmt.setString(19, strPresent_State);
				prepStmt.setString(20, strPermanent_Address);
				prepStmt.setString(21, strPermanent_HNo);
				prepStmt.setString(22, strPermanent_City);
				prepStmt.setString(23, strPermanent_District);
				prepStmt.setString(24, strPermanent_Pin);
				prepStmt.setString(25, strPermanent_State);
				prepStmt.setString(26, strLocal_Ref_Msisdn);
				prepStmt.setString(27, strLocal_Ref_name);
				prepStmt.setString(28, strJobType);
				prepStmt.setString(29, strReqProcessStatus);
				prepStmt.setInt(30, nDedupCnt);
				prepStmt.setString(31, strDedupStatus);
				// prepStmt.setString(32, strReqComments);
				prepStmt.setString(32, strRejReason);
				prepStmt.setInt(33, nErrCode);
				prepStmt.setString(34, strRule);
				prepStmt.setString(35, strErrDesc);
				prepStmt.setString(36, strWarning);
				prepStmt.setTimestamp(37, reqTimeStamp);
				prepStmt.setTimestamp(38, resTimeStamp);
                                prepStmt.setString(39,strOraNumber );
                                prepStmt.setString(40,strCustomerId );
                                prepStmt.setString(41,strBlackListFlag);
                                prepStmt.setString(42,strOtherInfo1);
				prepStmt.setString(43,strOtherInfo2);
                                prepStmt.setString(44,strOtherInfo3);
                                prepStmt.setString(45,strOtherInfo4);
                                prepStmt.setString(46,strUid);
                                prepStmt.setString(47,strPan);

				if(prepStmt.executeUpdate() >= 1)
				{

				    captureLog("[" + currRequest + "] Insert query success for ONLINE_REQUEST_RESPONSE table");

				}
				else
				{
				    captureLog("[" + currRequest + "] Insert query failed for ONLINE_REQUEST_RESPONSE table");
				}

				prepStmt.close();

			    }

			    // }

			}
			catch (SQLException exception)
			{
			    captureLog("[" + currRequest + "] SQL Exception while inserting requested jobs:" + exception);
			}
		    }
		    else if(nReqType == 2 && bProcess)
		    { // For update Retry PENDING Case

			if(circleDbConnection != null)
			{

			    resTimeStamp = new java.sql.Timestamp(new java.util.Date().getTime());

			    String strUpdtTblSQL = updateTableSQL.replace("<<USER_NAME>>", username);
			    if(m_nTraceLevel > 1)
			    {
				captureLog("[" + currRequest + "] Query : " + strUpdtTblSQL);
			    }
			    PreparedStatement prepStmt = circleDbConnection.prepareStatement(strUpdtTblSQL);
			    prepStmt.setString(1, strDedupStatus);
			    prepStmt.setInt(2, nDedupCnt);
			    prepStmt.setInt(3, nErrCode);
			    prepStmt.setString(4, strErrDesc);
			    prepStmt.setString(5, strRejReason);
			    // prepStmt.setString(4, strReqComments);
			    prepStmt.setString(6, strJobType);
			    // prepStmt.setString(7, strJob_Id);
			    prepStmt.setString(7, strRule);
			    prepStmt.setString(8, strWarning);
			    prepStmt.setString(11, strTransactionID);
			    prepStmt.setTimestamp(9, reqTimeStamp);
			    prepStmt.setTimestamp(10, resTimeStamp);
			    //prepStmt.setString(11,strOraNumber);

			    // updateTableSQL =
			    // "UPDATE ONLINE_REQUEST_RESPONSE SET REPLY_STATE = ?,"
			    // +
			    // "REPLY_CNT = ?,ERR_CODE = ?,ERR_DESC = ?,COMMENTS = ?,"
			    // + "REQ_DATETIME = ?,RESP_DATETIME = ? "
			    // + "WHERE TRANSACTION_ID = ?";
			    //
			    if(prepStmt.executeUpdate() >= 1)
			    {

				captureLog("[" + currRequest + "] Update query success for ONLINE_REQUEST_RESPONSE table");

			    }
			    else
			    {
				captureLog("[" + currRequest + "] Update query failed for ONLINE_REQUEST_RESPONSE table");
			    }

			    prepStmt.close();
			    String stronlineJobUpadteQuery = onlineJobUpdateQuery.replace("<<USER_NAME>>", username);
			    prepStmt = circleDbConnection.prepareStatement(stronlineJobUpadteQuery);
			    prepStmt.setInt(1, 0);
			    prepStmt.setString(2, strTransactionID);
			    if(prepStmt.executeUpdate() >= 1)
			    {

				captureLog("[" + currRequest + "] Update query success for ONLINE_JOB_INFO table as Processed");

			    }
			    else
			    {
				captureLog("[" + currRequest + "] Update query failed for ONLINE_JOB_INFO table as Processed");
			    }

			    prepStmt.close();

			}
		    }
		    circleDbConnection.close();
		}
		else
		{
		    strDedupStatus = "E";
		    nErrCode = 1002;
		    strErrDesc = "Connection Not Establishes";
		    captureLog("[" + currRequest + "] Circle DB Connection Not Establishes");
		}
	    }

	    dedupResponse[reqNum].setComment(strRejReason);
	    dedupResponse[reqNum].setDedupCnt(nDedupCnt);
	    dedupResponse[reqNum].setDedupStatus(strDedupStatus);
	    dedupResponse[reqNum].setErrCode(nErrCode);
	    dedupResponse[reqNum].setErrDesc(strErrDesc);
	    dedupResponse[reqNum].setJob_Id(strJob_Id);
	    dedupResponse[reqNum].setRule(strRule);
	    dedupResponse[reqNum].setTransactionID(strTransactionID);
	    dedupResponse[reqNum].setsubStatus(strWarning);

	}
	catch (Exception exception)
	{
            String strError = "Exception in doRequestDedupProcess:" + exception;
            captureLog(strError);
            
            strError = "Exception in doRequestDedupProcess"; // to avoid displaying sql error description in response.
           
            dedupResponse[reqNum].setComment(strRejReason);
	    dedupResponse[reqNum].setDedupCnt(nDedupCnt);
	    dedupResponse[reqNum].setDedupStatus("E");
	    dedupResponse[reqNum].setErrCode(1014);
	    dedupResponse[reqNum].setErrDesc(strError);
	    dedupResponse[reqNum].setJob_Id(strJob_Id);
	    dedupResponse[reqNum].setRule(strRule);
	    dedupResponse[reqNum].setTransactionID(strTransactionID);
	    dedupResponse[reqNum].setsubStatus(strWarning);
	}
    }
        captureLog("Batch End  [Batch Count : "+len+"] [Tranacation Id(s) "+strBatchTransactionIds+"]");
	return dedupResponse;
    }

    void captureLog(String str)
    {
	// String timeStamp = new
	// SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSSSS :").format(new Date());
	// System.out.println(timeStamp+str);
	logger.info(str);
    }
    private void insertintoTrackTable(long currRequest, Connection circleDbConnection, String strTransactionID, String strTrackJobType, String strTrackProcessStatus, String strTrackComments,
	    int nBlackListJob, String strJobId, String strTrackReplyData, String op, String mode, String username)
    {
	try
	{
	    if(circleDbConnection != null)
	    {
		String strTrackInsQury = trackInsertQuery.replace("<<USER_NAME>>", username);
		if(m_nTraceLevel > 1)
		{
		    captureLog("[" + currRequest + "] Query : " + strTrackInsQury);
		}
		PreparedStatement prepSubStmt = circleDbConnection.prepareStatement(strTrackInsQury);
		prepSubStmt.setString(1, strTransactionID);
		prepSubStmt.setString(2, strTrackJobType);
		prepSubStmt.setString(3, strTrackProcessStatus);
		prepSubStmt.setString(4, strTrackComments);
		prepSubStmt.setInt(5, nBlackListJob);
		prepSubStmt.setInt(6, Integer.parseInt(strJobId));
		prepSubStmt.setString(7, strTrackReplyData);

		if(prepSubStmt.executeUpdate() >= 1)
		{
		    captureLog("[" + currRequest + "] Insert " + op + " query success for ONLINE_REQUEST_TRACKING table for " + mode);

		}
		else
		{
		    captureLog("[" + currRequest + "] Insert " + op + " query failed for ONLINE_REQUEST_TRACKING table for " + mode);

		}
		prepSubStmt.close();
	    }
	}
	catch (SQLException exception)
	{
	    captureLog("SQL Exception while inserting tracking jobs:" + exception);
	}
    }

    String RunDaemonJob(String orgId, String jobPacket, long currRequest, boolean bTracking)
    {
	String retVal = "";
        boolean bReset = false; // if send command failed then we should reset connection (i.e close socket) to aviod dirty data read.

	RASSessionManager SessionManager = null;
	RASConnection rasConnection = null;
	try
	{
	    SessionManager = RASSessionManager.getInstance();

	    rasConnection = SessionManager.getConnection(orgId, "PMS", false, logger, currRequest);

	    rasConnection.setLogger(logger);
	    rasConnection.setCurrReq(currRequest);

	    captureLog("[" + currRequest + "] RunDaemonJob:SendingJob.");
	    if(rasConnection.sendCommand(2, -1, jobPacket, false))
	    {
		String jobID = rasConnection.getData();
		captureLog("[" + currRequest + "] Submitted JobId-" + jobID);

		if(bTracking)
		{
		    captureLog("[" + currRequest + "] RunDaemonJob:TrackingJob for " + jobID);
		    String strParams = jobID + "\1" + " 3" + "\1" + " 0"; // Job
									  // tracking
									  // 3
									  // is
									  // to
									  // specify
									  // levels
		    if(rasConnection.sendCommand(7/*
						   * RSPMSCMD.JOB_TRACK.getCMDValue
						   * ()
						   */, -1, strParams, false))
		    {
			retVal = rasConnection.getData();
			captureLog("[" + currRequest + "] SUCCESS-" + retVal);
		    }
		    else
		    {
			captureLog("[" + currRequest + "] RunDaemonJob:Tracking failed-" + rasConnection.getError());
                        bReset = true;
		    }
		}
		else
		{
		    retVal = jobID;
		}
	    }
	    else
	    {
		captureLog("[" + currRequest + "] RunDaemonJob:Sending failed-" + rasConnection.getError());
                bReset = true;
	    }
	}
	catch (Exception ex)
	{
            bReset = true;
	    captureLog("[" + currRequest + "] RunDaemonJob failed: " + ex);
	    ex.printStackTrace();
	}
	finally
	{
	    if(SessionManager != null && rasConnection != null)
	    {
		try
		{
		    SessionManager.releaseConnection(rasConnection, orgId, logger, currRequest,bReset);
		}
		catch (Exception e)
		{
		    captureLog("[" + currRequest + "] RunDaemonJob failed at connection release: " + e);
		    e.printStackTrace();
		}
	    }
	}

	return retVal;
    }

    /*
     * @Override public void contextInitialized(ServletContextEvent sce) {
     * 
     * circleStateMap = new HashMap<String, Boolean>();
     * 
     * // get the status from the database //circleStateMap.put("UP",
     * Boolean.TRUE); //circleStateMap.put("UK", Boolean.TRUE);
     * //circleStateMap.put("GJ", Boolean.TRUE); //circleStateMap.put("BJ",
     * Boolean.TRUE); //circleStateMap.put("AP", Boolean.TRUE);
     * //circleStateMap.put("MH", Boolean.TRUE);
     * 
     * //throw new UnsupportedOperationException("Not supported yet."); }
     * 
     * @Override public void contextDestroyed(ServletContextEvent sce) { //throw
     * new UnsupportedOperationException("Not supported yet."); }
     * 
     * /** Web service operation
     */
    @WebMethod(operationName = "updateCirlceState")
    public Boolean updateCirlceState(@WebParam(name = "Circle_Code") String Circle_Code, @WebParam(name = "ActiveState") Boolean ActiveState)
    {
	// write your implementation code here:
	Boolean bRet = false;

	long currRequest = generateRequestNumber();
	captureLog("[" + currRequest + "]In updateCirlceState method circle:" + Circle_Code + " State:" + ActiveState);

	if(circleStateMap.containsKey(Circle_Code))
	{
	    circleStateMap.put(Circle_Code, ActiveState);
	    captureLog("[" + currRequest + "]In updateCirlceState method circle:Success");
	    bRet = true;
	}

	return bRet;
    }

    static String getUrl(String driver, String host, String port, String sid, String url, String schema)
    {
	String dbUrl = "";
	if(url.trim().equals(""))
	{
	    if(driver.equalsIgnoreCase(ORACLE))
	    {
		dbUrl = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
	    }
	    else if(driver.equalsIgnoreCase(MSSQL))
	    {
		dbUrl = "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + sid + ";";
	    }
	    else if(driver.equalsIgnoreCase(DB2))
	    {
		dbUrl = "jdbc:db2://" + host + ":" + port + "/" + sid + "";
	    }
	    else if(driver.equalsIgnoreCase(MYSQL))
	    {
		dbUrl = "jdbc:mysql://" + host + "/" + schema + "";
	    }
	    else if(driver.equalsIgnoreCase(POSTGRESQL))
	    {
		dbUrl = "jdbc:postgresql://" + host + "/" + schema + "";
	    }
	}
	else
	{
	    dbUrl = url;
	}

	return dbUrl;
    }

    static String getDriver(String type)
    {
	String dbDriver = "";

	if(type.equalsIgnoreCase(ORACLE))
	{
	    dbDriver = "oracle.jdbc.driver.OracleDriver";
	}
	else if(type.equalsIgnoreCase(MSSQL))
	{
	    dbDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	}
	else if(type.equalsIgnoreCase(DB2))
	{
	    dbDriver = "com.ibm.db2.jcc.DB2Driver";
	}
	else if(type.equalsIgnoreCase(POSTGRESQL))
	{
	    dbDriver = "org.postgresql.Driver";
	}
	else if(type.equalsIgnoreCase(MYSQL))
	{
	    dbDriver = "com.mysql.jdbc.Driver";
	}

	return dbDriver;
    }

}
