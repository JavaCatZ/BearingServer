package mage_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.SQLException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import com.mysql.jdbc.PreparedStatement;
import org.apache.activemq.command.ActiveMQBytesMessage;

import base_lib.*;

public class PhoneTask extends Thread
{
	private String queue_name = "MAGE_phone_queue";
	private Session session = null;
	
	public PhoneTask(Session session) 
	{
		this.session = session;	
	}
	
	@Override
	public void run() 
	{
		Destination destination = null;
		MessageConsumer consumer = null;
		
		try 
		{
		  destination = session.createQueue(queue_name);
		  consumer = session.createConsumer(destination);
		} 
		  catch (JMSException jmse) 
		  {
			jmse.printStackTrace();
		  }
        
		System.out.println("PhoneThread started...");
		
		while(true)
        {
        	ActiveMQBytesMessage receivedMsg = null;
        	
			try 
			{
				receivedMsg = (ActiveMQBytesMessage) consumer.receive();
				byte[] msgBytes = receivedMsg.getContent().data;
	        	Message currentMsg = (Message) Serializer.deserialize(msgBytes);
	        	
	        	switch(currentMsg.getMsgType())
	        	{
		        	case 20:
	    				Server.prepSt = (PreparedStatement) Server.mageConnection.conn.prepareStatement("INSERT INTO h136894_mage_base.coord_packets (id_sender, sender_role, id_packet, vessel_info, latitude, longitude, date_time)"
	    						+ " VALUES(?, ?, ?, ?, ?, ?, ?)");
	    				Server.prepSt.setInt(1, ((CoordMessage) currentMsg).getIdSender());
	    				Server.prepSt.setString(2, ((CoordMessage) currentMsg).getSenderRole());
	    				Server.prepSt.setInt(3, ((CoordMessage) currentMsg).getIdPacket());
	    				Server.prepSt.setString(4, ((CoordMessage) currentMsg).getVessel());
	    				Server.prepSt.setString(5, ((CoordMessage) currentMsg).getLatitude());
	    				Server.prepSt.setString(6, ((CoordMessage) currentMsg).getLongitude());
	    				Server.prepSt.setString(7, ((CoordMessage) currentMsg).getDateTime());
	    				Server.prepSt.executeUpdate();
	    				Server.mageConnection.conn.commit();
	    				receivedMsg.clearBody();
	    				System.out.println("PhoneThread saved pack #" + ((CoordMessage) currentMsg).getIdSender());
	    			break;
	    		
	    			case 40:
	    				Server.prepSt = (PreparedStatement) Server.mageConnection.conn.prepareStatement("INSERT INTO h136894_mage_base.message_packets (id_sender, sender_role, id_packet, vessel_info, packet_theme, packet_desc, date_time)"
	    						+ " VALUES(?, ?, ?, ?, ?, ?, ?)");
	    				Server.prepSt.setInt(1, ((MsgMessage) currentMsg).getIdSender());
	    				Server.prepSt.setString(2, ((MsgMessage) currentMsg).getSenderRole());
	    				Server.prepSt.setInt(3, ((MsgMessage) currentMsg).getIdPacket());
	    				Server.prepSt.setString(4, ((MsgMessage) currentMsg).getVessel());
	    				Server.prepSt.setString(5, ((MsgMessage) currentMsg).getPacketTheme());
	    				Server.prepSt.setString(6, ((MsgMessage) currentMsg).getPacketDesc());
	    				Server.prepSt.setString(7, ((MsgMessage) currentMsg).getDateTime());
	    				Server.prepSt.executeUpdate();
	    				Server.mageConnection.conn.commit();
	    				receivedMsg.clearBody();
	    				System.out.println("PhoneThread saved pack #" + ((MsgMessage) currentMsg).getIdSender());
	    			break;
	        		
	    			case 60:
	    				Server.prepSt = (PreparedStatement) Server.mageConnection.conn.prepareStatement("INSERT INTO h136894_mage_base.full_packets (id_sender, sender_role, id_packet, vessel_info, latitude,"
	    						+ " longitude, packet_cat, packet_theme, packet_desc, packet_images, date_time)"
	    						+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
	    				Server.prepSt.setInt(1, ((FullMessage) currentMsg).getIdSender());
	    				Server.prepSt.setString(2, ((FullMessage) currentMsg).getSenderRole());
	    				Server.prepSt.setInt(3, ((FullMessage) currentMsg).getIdPacket());
	    				Server.prepSt.setString(4, ((FullMessage) currentMsg).getVessel());
	    				Server.prepSt.setString(5, ((FullMessage) currentMsg).getLatitude());
	    				Server.prepSt.setString(6, ((FullMessage) currentMsg).getLongitude());
	    				Server.prepSt.setString(7, ((FullMessage) currentMsg).getPacketCat());
	    				Server.prepSt.setString(8, ((FullMessage) currentMsg).getPacketTheme());
	    				Server.prepSt.setString(9, ((FullMessage) currentMsg).getPacketDesc());
	    			
	    				String[] file_names = new String[((FullMessage) currentMsg).getPacketImages().getImages().length];
	    			
	    				try 
	    				{
	    					FTP_Client.ftpConn();
	    				}
	    			 		catch (FileNotFoundException ex) 
	    			 		{
	    			 			Logger.getLogger(FTP_Client.class.getName()).log(Level.SEVERE, null, ex);
	    			 		}     
	    			
	    				int i = 0;
	    				for(File file : ((FullMessage) currentMsg).getPacketImages().getImages())
	    				{
	    					FTP_Client.ftpFileStore(file, String.valueOf(((FullMessage) currentMsg).getIdPacket()));
	    					file_names[i] = file.getName();
	    					System.out.println("num: " + i + " - " + file_names[i]);
	    					i++;
	    				}
	    			
	    				Server.prepSt.setObject(10, file_names); 
	    				Server.prepSt.setString(11, ((FullMessage) currentMsg).getDateTime());
	    				Server.prepSt.executeUpdate();
	    				Server.mageConnection.conn.commit();
	    				receivedMsg.clearBody();
	    				System.out.println("PhoneThread saved pack #" + ((FullMessage) currentMsg).getIdSender());
	    			break;
	        	}
			} 
				catch (SQLException sqle) 
				{	
					sqle.printStackTrace();
				}
			
				catch (JMSException jmse) 
				{
					jmse.printStackTrace();
				}
			
				catch (ClassNotFoundException cnfe) 
				{
					cnfe.printStackTrace();
				}
        	
				catch (IOException ioe) 
				{
					ioe.printStackTrace();
				}
        }
	}
}
