package com.in10s.rasserver;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
public class  RASServerSettings
{
	/** Reads the server data from XML file and assigns IP address port
	*	number and timeout values of server
	**/
	DocumentBuilderFactory factory;
	DocumentBuilder parser;
	Document XmlDoc = null;
	NodeList XmlNodeList = null;
	Node XmlNode =null;
	String XmlFilePath = "ServerConfig.xml";
	File XmlFile = null;
	int nReturnValue = -1;
	String ServerIpAdd = "";
	int nServerPortNumber = -1; 
	String ServerTimeOut = "";
	
	public RASServerSettings()
	{
		
		try
		{
			factory = DocumentBuilderFactory.newInstance();
			parser = factory.newDocumentBuilder();
			XmlFile = new File(XmlFilePath);
			if(XmlFile.exists())
			{
				XmlDoc = parser.parse(XmlFile);
				XmlNodeList = XmlDoc.getElementsByTagName("IPADDRESS");
				XmlNode = XmlNodeList.item(0);
				ServerIpAdd = XmlNode.getTextContent();
				XmlNodeList = XmlDoc.getElementsByTagName("PORT");
				XmlNode = XmlNodeList.item(0);
				nServerPortNumber = Integer.parseInt(XmlNode.getTextContent());
				XmlNodeList = XmlDoc.getElementsByTagName("TIMEOUT");
				XmlNode = XmlNodeList.item(0);
				ServerTimeOut = XmlNode.getTextContent();
			}
		}
		catch(Exception excp)
		{
			//System.out.println("exception while reading server settings");
		}
	}
	public String GetServer()
	{
		return ServerIpAdd;
	}
	public int GetPort()
	{
		return nServerPortNumber;
	}
	public int GetTimeOut()
	{
		return Integer.parseInt(ServerTimeOut);
	}
}
