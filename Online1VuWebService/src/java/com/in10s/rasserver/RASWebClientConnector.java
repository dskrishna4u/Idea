package com.in10s.rasserver;

import java.net.*;
import java.io.*;

class RASWebClientConnector
{
	Socket clientSocket = null;
	String ServerIpAdd = "";
	int nServerPortNumber = -1;
	int ServerTimeOut = 30000;
	InputStream Istrm = null;
	DataInputStream distrm = null;
	boolean WaitFlag = false;
	boolean m_bError = false;
	private String strServerResponseData = "";
	String m_strWebUserName = "" ;

        private long lRequestNum = 0;
        public void setlRequestNum(long lRequestNum) {
        this.lRequestNum = lRequestNum;
        }
	
	public RASWebClientConnector(String webUserName )
	{
		
		m_strWebUserName	= webUserName;
	}

	public void SetServer(String strIP, int nPort)
	{
		ServerIpAdd = strIP;
		nServerPortNumber = nPort;
	}

	/** Sets the timeout of the connection  **/
	public void setTimeOut(int nTimeInSeconds)
	{
		if (nTimeInSeconds <= 0)
		{
			nTimeInSeconds = 0;
		}
		else
		{
			nTimeInSeconds = nTimeInSeconds * 1000; 
		}
		ServerTimeOut = nTimeInSeconds;
	}

	/** Gets the connection with the specified server  **/
	public boolean getConnection( )
	{
		m_bError = true;
		
		try
		{
			if ( clientSocket == null )
			{
				clientSocket = new Socket( ServerIpAdd,nServerPortNumber );
			}

			WaitForResponse();
		
		}
		catch( UnknownHostException UKHExcp )
		{
			clientSocket = null;

			prErr("UnKnownHostException oocured while connecting for : "+m_strWebUserName+" : " + UKHExcp);
		}
		catch( Exception Excp )
		{
			clientSocket = null;

			prErr("Exception oocured while connecting for : "+m_strWebUserName+" : " + Excp);
		}

		return !m_bError;
	}

	

	/** Closes the current connection  **/
	public int closeConnection( )
	{
		int nReturnValue = -1;

		try
		{
                    if(clientSocket != null){
                    clientSocket.close();
                    }
                    clientSocket = null;

			nReturnValue = 1;									//Connection closed successfully
		}
		catch( IOException IOExcp )
		{
			nReturnValue = 2;									//IOException occured while closing connection

			prErr("IOException oocured while closing connection for : "+m_strWebUserName+" : " +IOExcp);
		}
		catch( Exception Excp )
		{
			nReturnValue = 3;									//Exception occured while closing

			prErr("Exception oocured while closing connection for : "+m_strWebUserName+" : "+ Excp);
		}
		return nReturnValue;
	}
	

	/** Sends commands to the specified server for execution **/
	public boolean SendCommand( int nMainCMD , int nSubCMD , String CommandParams )
	{
		strServerResponseData = "";

		m_bError = true;
		
		pr( "commands for server for  : "+m_strWebUserName+" : " + (nMainCMD+"@!@"+nSubCMD+"@!@"+CommandParams) );

		if( isConnected() )													//check connection exists or not
		{
            pr("Enter into isConnected() : "+isConnected());
			try
			{
				ByteArrayOutputStream buf = new ByteArrayOutputStream();	//Holds the data block that has to be sent to server

                DataOutputStream dosBuf = new DataOutputStream(buf);		//Used for writing data into bytearray

				dosBuf.writeInt(nMainCMD);									//Writing main command

				if (nSubCMD != -1)
				{
					dosBuf.writeInt(nSubCMD);								//Writing sub command
				}
												
				dosBuf.writeInt(CommandParams.length()*2);					//Writing the length of the param

				dosBuf.writeChars(CommandParams);							//writing the param data(i,e data required for executing the command)
				
				dosBuf.flush();

				ByteArrayOutputStream bufFinal = new ByteArrayOutputStream();//Holds the size of data block and data block

				dosBuf = new DataOutputStream(bufFinal);

                dosBuf.writeShort(buf.size());						//Writing the size of data block first
                
				dosBuf.flush();

                bufFinal.write(buf.toByteArray(), 0, buf.size());	//Writing the data block

				buf.close();
				
				bufFinal.writeTo(clientSocket.getOutputStream());	//Sending data to server
				
				bufFinal.close();
				WaitForResponse();					//Waiting for the response
			}
			catch(Exception Excp)
			{
				prErr("Exception oocured while sending command to server for : "+m_strWebUserName+" : " + Excp);
			}
		}

		return !m_bError;
	}
	/**Waits until the server sends the response**/
	public void WaitForResponse( )
	{
		WaitFlag = true;

		try
		{
			Istrm = clientSocket.getInputStream();

			distrm = new DataInputStream(Istrm);		
			pr( "socket wait time for  : "+m_strWebUserName+" : " + ServerTimeOut );
			clientSocket.setSoTimeout( ServerTimeOut );

			while( WaitFlag )
			{
                ReadServerData();
			}
		}
		catch(Exception Excp)
		{
			prErr("Exception oocured while waiting for response for : "+m_strWebUserName+" : " + Excp);
		}
	}
	/**Reads the data from server **/
	public void ReadServerData()
	{		
		m_bError = true;
        pr("Enter into ReadServerData() : "+distrm);
		if( distrm != null )
		{
			try
			{				
				int nPacketSize = distrm.readShort();				//Read the data block size
				
				int nCommand = distrm.readInt();					//Read response command from server
				pr( "Response command from server for : "+m_strWebUserName+" : " + nCommand );
				
				if( nCommand == RSCMD.CONNECTED.getCMDValue() )		//Received CONNECTED signal
				{
					strServerResponseData = "Connected Successfully";
					WaitFlag = false;
					m_bError = false;
				}
				else if (nCommand == RSCMD.MAXCONNLIMIT.getCMDValue())
				{
					strServerResponseData = "Maximum connection limit reached";
					WaitFlag = false;
				}
				else if (nCommand == RSCMD.AUTHENTICATED.getCMDValue())
				{
					strServerResponseData = "Authenticated";
					WaitFlag = false;
					m_bError = false;
				}
				else if (nCommand == RSCMD.UNAUTHENTICATED.getCMDValue())
				{
					strServerResponseData = "Not Logged In";
					WaitFlag = false;
				}
				else if (nCommand == RSCMD.DISCONNECTING.getCMDValue())
				{
					strServerResponseData = "Closing connection";
					WaitFlag = false;
				}
				else if (nCommand == RSCMD.CMDERR.getCMDValue())
				{
					strServerResponseData = "Syntax Error, Unknown command";
					WaitFlag = false;
				}
				else if (nCommand == RSCMD.RSERROR.getCMDValue())	//read error msg
				{
					strServerResponseData = readDataString(distrm);
					WaitFlag = false;
				}
				else if (nCommand == RSCMD.RSSUCCESS.getCMDValue())	//read success msg
				{
					strServerResponseData = readDataString(distrm);
					WaitFlag = false;
					m_bError = false;								//no error occured
				}
				else if (nCommand == RSCMD.RSACK.getCMDValue())		//read acknowlegment msg
				{
					strServerResponseData = readDataString(distrm);
                   // strServerResponseData = "45";
					WaitFlag = false;
					m_bError = false;
				}
				else
				{
					strServerResponseData = "Unknown server reply";	
					WaitFlag = false;
				}
                                pr("Control in ReadServerData:: strServerResponseData ::"+strServerResponseData);
			}
			catch (java.net.SocketTimeoutException eTimeOut)
			{
				WaitFlag = false;
				strServerResponseData = "Socket Time out Exception";
				prErr("Socket TimeOut Exception occured while reading response from server for : "+m_strWebUserName+" : " + eTimeOut);
			}
			catch(Exception Excp)
			{
				WaitFlag = false;
				strServerResponseData = "Exception occured while reading server Response";
				prErr("Exception occured while reading response from server for : "+m_strWebUserName+" : " + Excp);
		    }
		}
	}
	private String readDataString(DataInputStream datastream)
	{
		try
		{
			if ( datastream.available() > 0 )
			{
				int nDataSize =  datastream.readInt();				//Read the Response data length from server
				byte dataarr[] = new byte[nDataSize];
				for( int nInti=0 ; nInti<nDataSize ; nInti++ )
				{
					dataarr[nInti] = (byte)datastream.read();
				}
				return new String(dataarr, "UTF-16");
			}
			else
			{
				return "";
			}
		}
		catch (Exception Excp)
		{
			prErr( "Exception occured while reading String data from server for : "+m_strWebUserName+" : " + Excp );
			return "";
		}
	}
	public boolean isConnected()
	{
		boolean ReturnValue = false;
		
		if(clientSocket != null)
		{
			ReturnValue = clientSocket.isConnected();
		}

		if (!ReturnValue)
		{
			clientSocket = null;

			ReturnValue = getConnection();
		}

		return ReturnValue;
	}
	public String getServerResponse()
	{
		return strServerResponseData;
	}
    public void pr(String str){
    //logger.info(str);
       // System.out.println("[" + lRequestNum + "]" + str);
    }
    public void prErr(String str){
    //logger.error(str);
       // System.out.println("[" + lRequestNum + "]" +str);
    }
}
