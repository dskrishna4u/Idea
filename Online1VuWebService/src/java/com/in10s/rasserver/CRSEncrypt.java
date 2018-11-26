package com.in10s.rasserver; 

import java.util.*;
public class CRSEncrypt
{
	public String Encrypt( String strKey , String strSrc )	//pword,token,data
	{
		int nSrcSize = strSrc.length();
		int nKeyLen  = strKey.length(); 
		int nKeyPos  = 0;
		int nSrcAsc  = 0;
		int nOffset  = 0;
		String strDest = ""; 
		String strOut = "";
		if( nSrcSize == 0 || nKeyLen == 0 ) 
		{ 
			return ""; 
		}
		Date currDate = new Date();
		
		long randomSeed =  currDate.getTime();

		
		nOffset =(int) (randomSeed * 10000 % 255) + 1;
		
		strOut = Integer.toHexString( nOffset ).toUpperCase();

		if( strOut.length() == 1 )
		{ 
			strOut = "0"+strOut; 
		} 
		
		for( int i=0; i<nSrcSize; i++ )
		{
			
			nSrcAsc = ( (int)strSrc.charAt(i) + nOffset ) % 255;

			//System.out.println( "char at "+(int)strSrc.charAt(i) );
			//System.out.println( "noffset "+nOffset );
			//System.out.println( "value before"+nSrcAsc );
			
			nSrcAsc = nSrcAsc ^ (int)strKey.charAt(nKeyPos);

			//System.out.println("pword char at"+(int)strKey.charAt(nKeyPos));
			//System.out.println("value is two"+nSrcAsc);

			nKeyPos++;

			if( nKeyPos == nKeyLen ) 
			{ 
				nKeyPos = 0;
			}
			
			strDest = Integer.toHexString(nSrcAsc).toUpperCase();

			if( strDest.length() == 1 )
			{
			
				strDest = "0"+strDest; 
			
			}

			strOut += strDest.toUpperCase();
			
			strDest = ""; 

			nOffset =  nSrcAsc;
		}	
		return strOut; 
	}
	public String Decrypt( String strKey , String strSrc )	//pword,token,data
	{
		
		int nSrcSize	= strSrc.length();
		int nKeyLen		= strKey.length(); 
		int nKeyPos		= 0;
		int nSrcAsc		= 0;
		int nTempSrcAsc = 0;
		int nOffset		= 0;
		String strOut = "";
		String strTemp = "";
		if(nSrcSize == 0 || nKeyLen == 0) { return ""; }

		//nOffset = strSrc.left(2).toInt(&bOk, 16);

		strTemp = strSrc.substring(0,2);

		nOffset = Integer.valueOf(strTemp,16).intValue() ;	//convert hex to decimal value

		//System.out.println(nOffset);

		for( int i=2; i<nSrcSize; i+=2 )
		{
			//nSrcAsc = strSrc.mid(i, 2).toInt(&bOk, 16);
			strTemp = strSrc.substring(i,i+2);
			//System.out.println("temp str is"+strTemp);

			nSrcAsc = Integer.valueOf(strTemp,16).intValue() ;

			nTempSrcAsc = nSrcAsc ^ (int)strKey.charAt(nKeyPos);

			nKeyPos++;
			
			//System.out.println( "temp"+nTempSrcAsc );
			if( nKeyPos == nKeyLen ) { nKeyPos = 0; }

			if( nTempSrcAsc <= nOffset )
			{
				nTempSrcAsc = 255 + nTempSrcAsc - nOffset;
			}
			else
			{
				nTempSrcAsc = nTempSrcAsc - nOffset;
			}

			//System.out.println( "char"+(char)nTempSrcAsc );
			strOut += (char)nTempSrcAsc;

			nOffset =  nSrcAsc;

			//System.out.println(strOut);
		}
		return strOut; 		
	}
	public static void main(String args[])
	{
            String strArr = "SUCESS,1/1";
            String strResponse = strArr.substring(strArr.indexOf(",")+1);
                    strResponse = strResponse.substring(0,strResponse.indexOf("/"));
		CRSEncrypt crs = new CRSEncrypt();
		String ecryptedStr = crs.Encrypt("RSV62006","In10s1Vu"); 
//		System.out.println("encrypted string"+ecryptedStr);
//		System.out.println("decrypted value is"+crs.Decrypt("RSV62006","BA56979EF85EBF26AD"));
	}
}