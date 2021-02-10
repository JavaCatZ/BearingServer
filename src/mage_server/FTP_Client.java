package mage_server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPClient;

import base_lib.ServiceInfo;

/**
 * @author CatDevil
 *
 */

public class FTP_Client 
{	
	private static FTPClient fClient = null;
	private static FileInputStream fInput = null;
	
	public static void ftpConn() throws FileNotFoundException 
	{      
		fClient = new FTPClient();
		
		try 
		{
		   fClient.connect(ServiceInfo.FTP_HOST_ADDRESS, ServiceInfo.FTP_HOST_PORT);
		   fClient.login(ServiceInfo.FTP_LOGIN, ServiceInfo.FTP_PASSWORD);
		   fClient.enterLocalPassiveMode();    
		} 
		   catch (IOException ex) 
		   {
		      System.err.println(ex);
		      ex.printStackTrace();
		   }
	}
	
	public static void ftpFileStore(File file, String dirName)
	{
		try 
		{
			fInput = new FileInputStream(file);
			String fs = "/".concat(dirName + "/" + file.getName());
			
			System.out.println(fs);
			
			try 
			{
				fClient.makeDirectory(dirName);
				fClient.storeFile(fs, fInput);
			} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
		} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
	}
	
	public static void ftpConnClose()
	{      
		try 
		{
		   fClient.logout();
		   fClient.disconnect();
		} 
		   catch (IOException e) 
		   {
			 e.printStackTrace();
		   }   
	}
}