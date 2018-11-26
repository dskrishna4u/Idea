/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.in10s.onevu;

import java.util.Date;
import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author krishnarao
 */
public class CRSOnline1VuRequest
{

    private String strTransactionID      = "";
    private String strMobileNo	   = "";
    private String strSIMNo	      = ""; // SIM/CAFNO
    private String strCustomer_Category;
    private String strProductType	= "";
    private String strFullName	   = "";
    private String strDOB		= "";
    private String strFatherName	 = "";
    private String strCircle_Code	= "";
    private String strPOI_Type	   = "";
    private String strPOI_Id	     = "";
    private String strPOA_Type	   = "";
    private String strPOA_Id	     = "";
    private String strPresentAddress     = "";
    private String strPresent_HNo	= "";
    private String strPresent_City       = "";
    private String strPresent_District   = "";
    private String strPresent_Pin	= "";
    private String strPresent_State      = "";
    private String strPermanent_Address  = "";
    private String strPermanent_HNo      = "";
    private String strPermanent_City     = "";
    private String strPermanent_District = "";
    private String strPermanent_Pin      = "";
    private String strPermanent_State    = "";
    private String strLocal_Ref_Msisdn   = "";
    private String strLocal_Ref_name     = "";
    private String strJobType	    = "";
    private String strOraNumber = "";
    private String strCustomerId = "";
    private String strBlackListFlag = "";
    private String strOtherInfo1 = "";
    private String strOtherInfo2 = "";
    private String strOtherInfo3 = "";
    private String strAdharCard = "";
    private String strPanCard   = "";
    
    private String strOtherInfo4 = "";
    
    
//    private String strUserName= "" ;
//    private String strPwd= "" ;
    
//    @XmlElement(required=true)
//    public void setUserName(String strUserName) {
//        this.strUserName = strUserName;
//    }
//
//    @XmlElement(required=true)
//    public void setPwd(String strPwd) {
//        this.strPwd = strPwd;
//    }
//    
//     public String getUserName() {
//        return strUserName;
//    }
//
//    public String getPwd() {
//        return strPwd;
//    }
    
    @XmlElement(required=true)
    public void setJobType(String strJobType)
    {
	this.strJobType = strJobType;
    }

    public String getJobType()
    {
	return strJobType;
    }
     
    // @XmlElement(required=true)

    public String getTransactionID()
    {
	return strTransactionID;
    }

    @XmlElement(required=true)
    public void setTransactionID(String strTransactionID)
    {
	this.strTransactionID = strTransactionID;
    }

    public String getMobileNo()
    {
	return strMobileNo;
    }

    // @XmlElement(required=true)
    public void setMobileNo(String strMobileNo)
    {
	this.strMobileNo = strMobileNo;
    }

    public String getSIMNo()
    {
	return strSIMNo;
    }

    // @XmlElement(required=true)
    public void setSIMNo(String strSIMNo)
    {
	this.strSIMNo = strSIMNo;
    }

    public String getCustomer_Category()
    {
	return strCustomer_Category;
    }

    // @XmlElement(required=true)
    public void setCustomer_Category(String strCustomer_Category)
    {
	this.strCustomer_Category = strCustomer_Category;
    }

    public String getProductType()
    {
	return strProductType;
    }

    // @XmlElement(required=true)
    public void setProductType(String strProductType)
    {
	this.strProductType = strProductType;
    }

    public String getFullName()
    {
	return strFullName;
    }

    //@XmlElement(required = true)
    public void setFullName(String strFullName)
    {
	this.strFullName = strFullName;
    }

    public String getDOB()
    {
	return strDOB;
    }

    // @XmlElement(required=true)
    public void setDOB(String strDOB)
    {
	this.strDOB = strDOB;
    }

    public String getFatherName()
    {
	return strFatherName;
    }

    //@XmlElement(required = true)
    public void setFatherName(String strFatherName)
    {
	this.strFatherName = strFatherName;
    }

    public String getCircle_Code()
    {
	return strCircle_Code;
    }

    @XmlElement(required=true)
    public void setCircle_Code(String strCircle_Code)
    {
	this.strCircle_Code = strCircle_Code;
    }

    public String getPOI_Type()
    {
	return strPOI_Type;
    }

    // @XmlElement(required=true)
    public void setPOI_Type(String strPOI_Type)
    {
	this.strPOI_Type = strPOI_Type;
    }

    public String getPOI_Id()
    {
	return strPOI_Id;
    }

    //@XmlElement(required = true)
    public void setPOI_Id(String strPOI_Id)
    {
	this.strPOI_Id = strPOI_Id;
    }

    public String getPOA_Type()
    {
	return strPOA_Type;
    }

    // @XmlElement(required=true)
    public void setPOA_Type(String strPOA_Type)
    {
	this.strPOA_Type = strPOA_Type;
    }

    public String getPOA_Id()
    {
	return strPOA_Id;
    }

    // @XmlElement(required=true)
    public void setPOA_Id(String strPOA_Id)
    {
	this.strPOA_Id = strPOA_Id;
    }

    public String getPresentAddress()
    {
	return strPresentAddress;
    }

    // @XmlElement(required=true)
    public void setPresentAddress(String strPresentAddress)
    {
	this.strPresentAddress = strPresentAddress;
    }

    public String getPresent_HNo()
    {
	return strPresent_HNo;
    }

    // @XmlElement(required=true)
    public void setPresent_HNo(String strPresent_HNo)
    {
	this.strPresent_HNo = strPresent_HNo;
    }

    public String getPresent_City()
    {
	return strPresent_City;
    }

    // @XmlElement(required=true)
    public void setPresent_City(String strPresent_City)
    {
	this.strPresent_City = strPresent_City;
    }

    public String getPresent_District()
    {
	return strPresent_District;
    }

    // @XmlElement(required=true)
    public void setPresent_District(String strPresent_District)
    {
	this.strPresent_District = strPresent_District;
    }

    public String getPresent_Pin()
    {
	return strPresent_Pin;
    }

    // @XmlElement(required=true)
    public void setPresent_Pin(String strPresent_Pin)
    {
	this.strPresent_Pin = strPresent_Pin;
    }

    public String getPresent_State()
    {
	return strPresent_State;
    }

    // @XmlElement(required=true)
    public void setPresent_State(String strPresent_State)
    {
	this.strPresent_State = strPresent_State;
    }

    public String getPermanent_Address()
    {
	return strPermanent_Address;
    }

    // @XmlElement(required=true)
    public void setPermanent_Address(String strPermanent_Address)
    {
	this.strPermanent_Address = strPermanent_Address;
    }

    public String getPermanent_HNo()
    {
	return strPermanent_HNo;
    }

    // @XmlElement(required=true)
    public void setPermanent_HNo(String strPermanent_HNo)
    {
	this.strPermanent_HNo = strPermanent_HNo;
    }

    public String getPermanent_City()
    {
	return strPermanent_City;
    }

    // @XmlElement(required=true)
    public void setPermanent_City(String strPermanent_City)
    {
	this.strPermanent_City = strPermanent_City;
    }

    public String getPermanent_District()
    {
	return strPermanent_District;
    }

    // @XmlElement(required=true)
    public void setPermanent_District(String strPermanent_District)
    {
	this.strPermanent_District = strPermanent_District;
    }

    public String getPermanent_Pin()
    {
	return strPermanent_Pin;
    }

    // @XmlElement(required=true)
    public void setPermanent_Pin(String strPermanent_Pin)
    {
	this.strPermanent_Pin = strPermanent_Pin;
    }

    public String getPermanent_State()
    {
	return strPermanent_State;
    }

    // @XmlElement(required=true)
    public void setPermanent_State(String strPermanent_State)
    {
	this.strPermanent_State = strPermanent_State;
    }

    public String getLocal_Ref_Msisdn()
    {
	return strLocal_Ref_Msisdn;
    }

    // @XmlElement(required=true)
    public void setLocal_Ref_Msisdn(String strLocal_Ref_Msisdn)
    {
	this.strLocal_Ref_Msisdn = strLocal_Ref_Msisdn;
    }

    public String getLocal_Ref_name()
    {
	return strLocal_Ref_name;
    }

    // @XmlElement(required=true)
    public void setLocal_Ref_name(String strLocal_Ref_name)
    {
	this.strLocal_Ref_name = strLocal_Ref_name;
    }

    public String getOraNumber()
    {
	return strOraNumber;
    }

    public void setOraNumber(String strOraNumber)
    {
	this.strOraNumber = strOraNumber;
    }
    
    public String getCustomerId()
    {
	return strCustomerId;
    }

    public void setCustomerId(String strCustomerId)
    {
	this.strCustomerId = strCustomerId;
    }
    
    public String getOtherInfo1()
    {
	return strOtherInfo1;
    }

    public void setOtherInfo1(String strOtherInfo1)
    {
	this.strOtherInfo1 = strOtherInfo1;
    }
    
    public String getOtherInfo2()
    {
	return strOtherInfo2;
    }

    public void setOtherInfo2(String strOtherInfo2)
    {
	this.strOtherInfo2 = strOtherInfo2;
    }
    
    public String getBlackListFlag()
    {
	return strBlackListFlag;
    }

    public void setBlackListFlag(String strBlackListFlag)
    {
	this.strBlackListFlag = strBlackListFlag;
    }
    
    public void setOtherInfo3(String strOtherInfo3)
    {
        this.strOtherInfo3 = strOtherInfo3;
    }
    public String getOtherInfo3()
    {
        return strOtherInfo3;
    }
    public void setuidai(String strAdharCard)
    {
        this.strAdharCard = strAdharCard;
    }
    public String getuidai()
    {
        return strAdharCard;
    }
    public void setPan(String strPanCard)
    {
        this.strPanCard = strPanCard;
    }
    public String getPan()
    {
        return strPanCard;
    }
    
    public void setOtherInfo4(String strOtherInfo4)
    {
        this.strOtherInfo4 = strOtherInfo4;
    }
    public String getOtherInfo4()
    {
        return strOtherInfo4;
    }
}
