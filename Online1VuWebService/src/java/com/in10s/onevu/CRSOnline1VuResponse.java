/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.in10s.onevu;

import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author krishnarao
 */
public class CRSOnline1VuResponse
{

    private String strComment       = "";
    private String strTransactionID = "";
    private String strJob_Id	= "";
    private String strDedupStatus   = "";
    private String strRule	  = "";
    private int    nDedupCnt	= 0;
    private int    nErrCode	 = 0;
    private String strErrDesc       = "";
    private String strsubStatus     = "";

    @XmlElement(required = true)
    public void setTransactionID(String strTransactionID)
    {
	this.strTransactionID = strTransactionID;
    }

    @XmlElement(required = true)
    public void setJob_Id(String strJob_Id)
    {
	this.strJob_Id = strJob_Id;
    }

    @XmlElement(required = true)
    public void setDedupStatus(String strDedupStatus)
    {
	this.strDedupStatus = strDedupStatus;
    }

    public void setRule(String strRule)
    {
	this.strRule = strRule;
    }

    public void setDedupCnt(int nDedupCnt)
    {
	this.nDedupCnt = nDedupCnt;
    }

    public void setComment(String strComment)
    {
	this.strComment = strComment;
    }

    public void setErrCode(int nErrCode)
    {
	this.nErrCode = nErrCode;
    }

    public void setErrDesc(String strErrDesc)
    {
	this.strErrDesc = strErrDesc;
    }

    public void setsubStatus(String strsubStatus)
    {
	this.strsubStatus = strsubStatus;
    }

    public String getTransactionID()
    {
	return strTransactionID;
    }

    public String getJob_Id()
    {
	return strJob_Id;
    }

    public int getDedupCnt()
    {
	return nDedupCnt;
    }

    public String getComment()
    {
	return strComment;
    }

    public String getRule()
    {
	return strRule;
    }

    public String getDedupStatus()
    {
	return strDedupStatus;
    }

    public int getErrCode()
    {
	return nErrCode;
    }

    public String getErrDesc()
    {
	return strErrDesc;
    }

    public String getsubStatus()
    {
	return strsubStatus;
    }

}
