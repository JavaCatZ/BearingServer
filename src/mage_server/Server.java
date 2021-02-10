package mage_server;

import java.sql.SQLException;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import com.mysql.jdbc.PreparedStatement;

import base_lib.MageConnection;


/**
 * @author CatDevil
 *
 */

public class Server 
{
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;  
    public static MageConnection mageConnection = null;
    public static PreparedStatement prepSt = null;
	
	public static void main(String[] args) 
	{

		/****************************************dBase connection****************************************/
		
			mageConnection = new MageConnection();
			
			try 
			{
				mageConnection.getConnection();
				mageConnection.conn.setAutoCommit(false);
			} 
				catch (SQLException sqle) 
				{
					sqle.printStackTrace();
				} 
				
				catch (ClassNotFoundException cnfe) 
				{
					cnfe.printStackTrace();
				}
		
		/***********************************************************************************************/
			
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        connectionFactory.setTrustAllPackages(true);
        Connection connection = null;
        
		try 
		{
		   connection = connectionFactory.createConnection();
		   connection.start();
		   
		   Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	       DeskTask deskTask = new DeskTask(session);
	       		deskTask.start();
	       PhoneTask phoneTask = new PhoneTask(session);
	       		phoneTask.start();
		}
			catch (JMSException jmse) 
			{
				jmse.printStackTrace();
			}
		
		    finally
		    {
		    	try 
		        {	
		    		prepSt.close();	
			        connection.close();	
				} 
		        	catch (SQLException sqle) 
		        	{
		        		sqle.printStackTrace();
		        	} 
		    	
		    		catch (JMSException jmse) 
		    		{
		    			jmse.printStackTrace();
					}   	
		    }  
    }     	
}