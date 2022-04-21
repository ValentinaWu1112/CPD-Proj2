import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.*;
import java.util.Scanner;

public class MulticastClient {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in); 
        DataInputStream input = null;
        DataOutputStream out = null;
        int mcPort = 18777; 
        String mcIPStr = "230.1.1.1";
        try{
            DatagramSocket udpSocket = new DatagramSocket(); 
            // Prepare a message 
            InetAddress mcIPAddress = InetAddress.getByName(mcIPStr);

            // string to read message from input
            String line = "";
            String server = "";

            // keep reading until "Over" is input
            while (!line.equals("Over")){
                line = scan.nextLine();
                /* input.readLine();
                out.writeUTF(line); */
                byte[] msg = line.getBytes();
                DatagramPacket packet = new DatagramPacket(msg, msg.length);
                packet.setAddress(mcIPAddress);
                packet.setPort(mcPort);
                udpSocket.send(packet);
            }

            System.out.println("Sent a multicast message."); 
            System.out.println("Exiting application"); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    } 
}