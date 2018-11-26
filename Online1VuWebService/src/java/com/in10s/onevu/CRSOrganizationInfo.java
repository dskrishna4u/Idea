/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.in10s.onevu;

/**
 * 
 * @author krishnarao
 */
public class CRSOrganizationInfo
{

    private int    thresholdLimit = 0;
    private String dbSchema       = "";
    private String dbType	 = "";
    private String dbUserName     = "";
    private String dbPwd	  = "";
    private String dbIPAddr       = "";
    private String dbPortNo       = "";

    public int getThresholdLimit()
    {
	return thresholdLimit;
    }

    public void setThresholdLimit(int thresholdLimit)
    {
	this.thresholdLimit = thresholdLimit;
    }

    public String getDbSchema()
    {
	return dbSchema;
    }

    public void setDbSchema(String dbSchema)
    {
	this.dbSchema = dbSchema;
    }

    public String getDbType()
    {
	return dbType;
    }

    public void setDbType(String dbType)
    {
	this.dbType = dbType;
    }

    public String getDbUserName()
    {
	return dbUserName;
    }

    public void setDbUserName(String dbUserName)
    {
	this.dbUserName = dbUserName;
    }

    public String getDbPwd()
    {
	return dbPwd;
    }

    public void setDbPwd(String dbPwd)
    {
	this.dbPwd = dbPwd;
    }

    public String getDbIPAddr()
    {
	return dbIPAddr;
    }

    public void setDbIPAddr(String dbIPAddr)
    {
	this.dbIPAddr = dbIPAddr;
    }

    public String getDbPortNo()
    {
	return dbPortNo;
    }

    public void setDbPortNo(String dbPortNo)
    {
	this.dbPortNo = dbPortNo;
    }

}
