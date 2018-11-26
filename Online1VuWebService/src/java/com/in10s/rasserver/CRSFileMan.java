package com.in10s.rasserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CRSFileMan {

	static final int BUFFER_16K = 16384;
	static final int BUFFER_64K = 65536;

	String	m_strFile;
	String m_strError;

	private File 		  m_file;
	private InputStream   m_instrm;
	private OutputStream  m_outstrm;

	private ByteArrayOutputStream m_buffer;	 // temp buffer for file writitng (buffered)

	private long m_nFileSize;		// Will be set only when the File is opened for reading
	private long m_nBytesRead;
	private long m_nBytesWritten;
	private long m_lByteCount;		// size of temp buffer for writing into file

	public void reset() {
		m_strFile = "";

		closeDataFile();
	}

	public boolean openDataFile(String strFilePath, boolean bReadOnly) {

		boolean bSuccess = true;

		m_strFile = strFilePath.trim();

		if(m_strFile.isEmpty() == true) {
			m_strError = "File path Empty";
			return false;
		}
		else {
			if(m_file != null) {
				closeDataFile();
			}
		}

		try {
			m_file = new File(m_strFile);

			if(bReadOnly) {		// READ mode; use input stream only
				m_file.setReadOnly();

				m_instrm = new BufferedInputStream(new FileInputStream(m_file));
				m_outstrm = null;

				m_nFileSize = m_file.length();

				if(m_nFileSize <= 0)
				{
					bSuccess = false;
					m_strError = "File Read Error : File Size 0 bytes";

					this.reset();
				}
			}
			else {				// WRITE mode; use output stream only
				m_file.setWritable(true);

				m_outstrm = new BufferedOutputStream(new FileOutputStream(m_file));
				m_buffer =  new ByteArrayOutputStream();

				m_instrm = null;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			m_strError = e.getMessage();
			bSuccess = false;
			e.printStackTrace();
		}

		return bSuccess;
	}

	public boolean closeDataFile() {
		boolean bClosed = false;

		try {
			if(isWritable()) {

				if(m_lByteCount > 0)  {

					m_buffer.writeTo(m_outstrm);

					m_outstrm.flush();

				}
				m_outstrm.close();
				m_outstrm = null;

				bClosed = true;
			}
			else if(isReadable()) {
				m_instrm.close();
				m_instrm = null;

				bClosed = true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			m_strError = e.getMessage();
			e.printStackTrace();
		}

		//m_file = null;

		m_buffer = null;

		m_lByteCount = 0;

		m_nFileSize = 0;
		m_nBytesRead = 0;
		m_nBytesWritten = 0;

		return bClosed;

	}

	public boolean isWritable() {

		boolean bValid = true;

		if (m_file == null) {
			bValid = false;
			m_strError = "File not open";
		}
		else {
			if(m_outstrm == null) {
				bValid = false;
				m_strError = "File not open for writing";
			}
		}

		return bValid;
	}

	public boolean isReadable() {

		boolean bValid = true;

		if (m_file == null) {
			bValid = false;
			m_strError = "File not open";
		}
		else {
			if(m_instrm == null) {
				bValid = false;
				m_strError = "File not open for reading";
			}
		}

		return bValid;
	}

	public boolean isEOF() {
		return (getBytesRead() == getFileSize());
	}

	public int readFromDataFile(byte[] data, boolean bUnBuffered){
		//boolean bRead = false;

		int nbytesRead = 0;

		if(m_file == null) {
			m_strError = "File not opened";
	    }
		else {
			if(!isReadable())
			{
				m_strError = "File not open for reading";
			}
			else if(isEOF()) {
				m_strError = "End of file";
			}
			else {

				if(bUnBuffered) {
					try {
						nbytesRead = m_instrm.read(data, 0, (int) getFileSize());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						m_strError = "UnBuffered File Read Error: " + e.getMessage();
						e.printStackTrace();
					}
				}
				else {

					long bytesRemaining = getFileSize() - getBytesRead();

					try {
						if(bytesRemaining >= BUFFER_16K)
//							input.read() returns -1, 0 (if len is 0), or more :
							nbytesRead = m_instrm.read(data, 0, BUFFER_16K);
						else
							nbytesRead = m_instrm.read(data, 0, (int) bytesRemaining);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						m_strError = "Buffered File Read Error: " + e.getMessage();
					}
				}
				if (nbytesRead > 0){
					m_nBytesRead += nbytesRead;
					//bRead = true;
				}
			}

			if(nbytesRead <= 0) {
				closeDataFile();
			}
		}

		return nbytesRead;
	}

	public boolean writeToDataFile(byte[] data, int nPacketSize, boolean bUnBuffered) {

		boolean bWritten = true;

		if(m_file == null)
		{
			bWritten = false;
		}
		else
		{
			if(bUnBuffered) {
				if (nPacketSize > 0) {
					m_nBytesWritten += nPacketSize;

					try {
						m_outstrm.write(data, 0, nPacketSize);
						m_outstrm.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						m_strError = "Unbuffered File Write Error: " + e.getMessage();
						bWritten = false;
					}
				}
			}
			else {
				bWritten = writeToFile(data, nPacketSize);
			}
		}

		return bWritten;
	}

	private boolean writeToFile(byte[] data, int nPacketSize) {

		boolean bSuccess = true;

		int nWriteCnt = nPacketSize;

    	try {
			if (nWriteCnt > 0)
			{
				m_nBytesWritten += nWriteCnt;

				if(nWriteCnt > BUFFER_64K) {
					m_outstrm.write(data, 0, nWriteCnt);
					m_outstrm.flush();
				}
				else if((m_lByteCount + nWriteCnt) == BUFFER_64K) {
					m_buffer.write(data, 0, nWriteCnt);

					m_buffer.writeTo(m_outstrm);
					m_outstrm.flush();

					m_buffer.reset();

					m_lByteCount = 0;
				}
				else {
					if((m_lByteCount + nWriteCnt) > BUFFER_64K) {
						m_buffer.writeTo(m_outstrm);

						m_outstrm.flush();

						m_buffer.reset();

						m_lByteCount = 0;
					}

					if((m_lByteCount + nWriteCnt) < BUFFER_64K) {
						m_buffer.write(data, 0, nWriteCnt);

						m_lByteCount += nWriteCnt;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			m_strError = "Buffered File Write Error: " + e.getMessage();
			bSuccess = false;
		}

		return bSuccess;
	}

	public long getFileSize() {
		return m_nFileSize;
	}

	public long getBytesRead() {
		return m_nBytesRead;
	}

	public long getBytesWritten() {
		return m_nBytesWritten;
	}

	public String getFilePath() {
		return m_strFile;
	}

	public String getErrorMessage() {
		return m_strError;
	}

	public File getFileReference() {
		return m_file;
	}
}
