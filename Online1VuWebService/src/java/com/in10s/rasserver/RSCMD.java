package com.in10s.rasserver;

public enum RSCMD
{ 
	ABORT(666),				// Server Command to Abort any operation in progress
	RSACK(1),				// General Acknowledgement
	RSSUCCESS(200),			// Command Okey.
	CONNECTED(220),			// Connected Successfully
	MAXCONNLIMIT(221),		// Max connection limit reached
	AUTHENTICATED(230),		// Authenticated
	UNAUTHENTICATED(231),	// Not logged in
	RSERROR(420),			// Internal Error
	DISCONNECTING(421),		// Closing connection
	CMDERR(500);			// Syntax Error, Unknown command

	int CMDValue;

	RSCMD(int CMDNum)
	{
		CMDValue = CMDNum;
	}

	public int getCMDValue()
	{
		return CMDValue;
	}

	public static String getCMDName( int CMDNum )
	{
		String CMD = "";
		for( RSCMD p : RSCMD.values() )
		{
			if( p.getCMDValue() == CMDNum )
				CMD = p.toString();
		}
		return CMD;
	} 
}

enum RSMAINCMD
{
	RSLOGIN(11),
	RSSERVERINFO(12),
	RSUSER(13),
	RSREPORT(14),
	RSVERSION(15),
	RSOPPROFILES(16),
	RSPROTEMPLATES(17),
	RSCLIENTINFO(18),
	RSDBINFO(19),
	RSDATAFILE(20),
	RSAUDIT(21),
	RSCOMPONENTINFO(22),
    RSCCM(23),
    RSLICENSETREE(24),
    RSALSTEMPLATES(25),
    RSALSOBJECTS(26),
    RSALSPOLICIES(27),
    RSALSACTIONINFO(28);
	
	int MainCMDValue;

	RSMAINCMD(){}

	RSMAINCMD(int MainCMDNum)
	{
		MainCMDValue = MainCMDNum;
	}

	public int getMainCMDValue()
	{
		return MainCMDValue;
	}
}

enum RSLOGINCMD
{
	RSUSERLOGINAUTH(41),
	RSUSERLOGINSTATUS(42),
	RSSERVERLOGINAUTH(43),
	RSCLIENTLOGINAUTH(44),
	RSSERVICETOKEN(45),
	RSVALIDATE(46);

	int LoginCMDValue;

	RSLOGINCMD(){}

	RSLOGINCMD(int LoginCMDNum)
	{
		LoginCMDValue = LoginCMDNum;
	}

	public int getLOGINCMDValue()
	{
		return LoginCMDValue;
	}
}

enum RSPMSCMD
{
	TOKEN(1),			// Authentication purpose
	JOB_ADD(2),			// Job to PMS
	JOB_RM(3),				// Job to Remove from PMS
	JOB_QUERY(4),			// Job Status Query
	JOB_UPDATE(5),			// [ INTERNAL ] Job status send by clients
	JOB_GET(6),				// [ INTERNAL ] Job pull mechanism
	JOB_TRACK(7);

	int PMSCMDValue;

	RSPMSCMD(){}

	RSPMSCMD(int PMSCMDNum)
	{
		PMSCMDValue = PMSCMDNum;
	}

	public int getPMSCMDValue()
	{
		return PMSCMDValue;
	}
};