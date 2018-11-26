/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.in10s.onevu;

/**
 * 
 * @author amar.budidha
 */
public class CRSAuthentication
{

    public String Encrypt(String Param1)
    {
	/*-------------------------------------------------------------------------------------------------
	Anilkumar Velichety      31-JAN-2007       Encrypt the string passed as parameter

	Input Parameters
	Param1 -- String to be encrypted
	Output parameters             NIL

	Returns                        Encrypted string

	-------------------------------------------------------------------------------------------------*/
	String key = "1COLD5";
	String dest = "";
	int len = key.length();
	int SrcAsc, SrcPos, KeyPos = -1;
	int offset = ((int) (Math.random() * 10000) % 255) + 1;
	dest = Integer.toHexString(offset);
	if(dest.length() == 1)
	{
	    dest = "0" + dest;
	}
	for (SrcPos = 0; SrcPos < Param1.length(); SrcPos++)
	{
	    int ascii = (Param1.substring(SrcPos, SrcPos + 1)).charAt(0);
	    SrcAsc = (ascii + offset) % 255;
	    if(KeyPos < len - 1)
	    {
		KeyPos++;
	    }
	    else
	    {
		KeyPos = 0;
	    }
	    ascii = (key.substring(KeyPos, KeyPos + 1)).charAt(0);
	    SrcAsc = SrcAsc ^ ascii;
	    if(SrcAsc <= 15)
	    {
		dest = dest + " " + Integer.toHexString(SrcAsc);
	    }
	    else
	    {
		dest = dest + Integer.toHexString(SrcAsc);
	    }
	    offset = SrcAsc;
	}
	dest = dest.toUpperCase();
	return dest;
    }

    public String Decrypt(String Param1)
    {
	/*-------------------------------------------------------------------------------------------------
	Anilkumar Velichety      31-JAN-2007       Decrypt the string passed as parameter

	Input Parameters
	Param1 -- String to be decrypted
	Output parameters             NIL

	Returns     -                   Decrypted string

	-------------------------------------------------------------------------------------------------*/

	String key = "1COLD5";
	String dest = "";

	int len = key.length();
	int SrcAsc, SrcPos, KeyPos = -1;
	int offset = (Integer.decode("#" + Param1.substring(0, 2))).intValue();
	for (SrcPos = 2; SrcPos < Param1.length() - 1; SrcPos += 2)
	{
	    SrcAsc = (Integer.decode("#" + (Param1.substring(SrcPos, SrcPos + 2)).trim())).intValue();
	    if(KeyPos < len - 1)
	    {
		KeyPos++;
	    }
	    else
	    {
		KeyPos = 0;
	    }
	    int ascii = (key.substring(KeyPos, KeyPos + 1)).charAt(0);
	    int TmpSrcAsc = SrcAsc ^ ascii;
	    if(TmpSrcAsc <= offset)
	    {
		TmpSrcAsc = 255 + TmpSrcAsc - offset;
	    }
	    else
	    {
		TmpSrcAsc = TmpSrcAsc - offset;
	    }
	    char c = (char) TmpSrcAsc;
	    dest = dest + c;
	    offset = SrcAsc;
	}

	return dest;
    }

    public static void main(String args[])
    {
	CRSAuthentication crs = new CRSAuthentication();
	String ecryptedStr = crs.Encrypt("Uninor");
//	System.out.println("encrypted string" + ecryptedStr);
//	System.out.println("decrypted value is" + crs.Decrypt("A53EEF2BDC FB72C"));
    }

}
