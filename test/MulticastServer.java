import java.io.IOException; 
import java.net.DatagramPacket; 
import java.net.InetAddress; 
import java.net.MulticastSocket; 

public class MulticastServer { 
    public static void main(String[] args) { 
        int mcPort = 18777;
        try{
            InetAddress mcIPAddress = InetAddress.getByName("230.1.1.1"); 
            MulticastSocket  mcSocket = new MulticastSocket(mcPort);
            System.out.println("Multicast Receiver running at:" 
                    + mcSocket.getLocalSocketAddress()); 
            // Join the group 
            mcSocket.joinGroup(mcIPAddress); 

            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024); 

            while (true) {
                System.out.println("Waiting for a multicast message..."); 
                mcSocket.receive(packet); 
                String msg = new String(packet.getData(), 
                                        packet.getOffset(), 
                                        packet.getLength()); 
                System.out.println("[Multicast Receiver] Received:" + msg); 
            } 
            //mcSocket.leaveGroup(mcIPAddress); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    } 
}