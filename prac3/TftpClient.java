import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class TftpClient
{
	public static void main(String[] args)
	{
		if (args.length != 3)
		{
			System.out.println("Usage:java TftpClient ipAddress PortNumber filename");
		}
		else
		{
			try
			{
				DatagramSocket ds = new DatagramSocket();
				DatagramPacket rq;
				File targetFile = new File("C:/Users/GGPC/Desktop/prac3/test.txt");
				FileOutputStream streamer = new FileOutputStream(targetFile);
				rq = TftpUtility.packRRQDatagramPacket(args[2].getBytes());
				rq.setAddress(InetAddress.getByName(args[0]));
				rq.setPort(Integer.parseInt(args[1]));
				ds.send(rq);
				byte blockNumber = 1;

				while(true)
				{
					DatagramPacket dp = new DatagramPacket(new byte[512], 512);
					//ack arrives
					ds.receive(dp);
					// System.out.println("Length: " + dp.getLength());
					int packetLength = dp.getLength();
					//Testing purposes
					byte[] payload = dp.getData();
					byte blockSeq = TftpUtility.extractBlockSeq(dp);
					System.out.println("received block #" + blockSeq);
					if (blockSeq == blockNumber || blockSeq == blockNumber-1) {
						if (blockSeq == blockNumber)
						{
							ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
							byteStream.write(payload, 2, packetLength-2);
							System.out.println("Byte Stream Size: " + byteStream.size());
							streamer.write(byteStream.toByteArray());
						}
						byte[] acknowledge = {3, blockNumber};
						blockNumber++;
						dp.setData(acknowledge);
						ds.send(dp);
					}
					else
					{
						if(blockSeq == -1)
						{
							System.out.println("Requested File doesn't exist. Try again.");
						}
						else
						{
							System.out.println("This isn't the block number we were expecting");
						}
						break;
					}
					if (packetLength < 512)
					{
						System.out.println("End of File");
						break;
					}
				}
				ds.close();
				streamer.close();
			}
			catch(Exception e)
			{
				System.out.println(e.toString());
			}
		}			
	}
}
