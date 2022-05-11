import java.lang.Thread;
import java.util.Scanner;

//Multicast imports
import java.io.IOException; 
import java.net.DatagramPacket; 
import java.net.InetAddress; 
import java.net.MulticastSocket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.*;

//TCP import
import java.net.*;

public class Node{
    public static void main(String args[]){
        NodeMulticastServer nms = new NodeMulticastServer();
        nms.start();
        NodeMulticastClient nmc = new NodeMulticastClient();
        nmc.start();
        if(Integer.parseInt(args[0]) == 0){
            NodeTCPServer ntcps = new NodeTCPServer();
            ntcps.start();
        }
        else{
            NodeTCPClient ntcpc = new NodeTCPClient();
            ntcpc.start();
        }
    }
}

class NodeMulticastClient extends Thread{
    public void run() {
        Scanner scan = new Scanner(System.in); 
        DataInputStream input = null;
        DataOutputStream out = null;
        int mcPort = 42069; 
        String mcIPStr = "224.0.0.1";
        try{
            DatagramSocket udpSocket = new DatagramSocket(); 
            // Prepare a message
            InetAddress mcIPAddress = InetAddress.getByName(mcIPStr);

            // string to read message from input
            String line = "";
            String server = "";

            // keep reading until "Over" is input
            while (!line.equals("Over")){
                System.out.println("waiting for input: ");
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

class NodeMulticastServer extends Thread{
    public void run() {
        int mcPort = 42069;
        //System.out.println("node multicast");
        try{
            InetAddress mcIPAddress = InetAddress.getByName("224.0.0.1");
            SocketAddress soc_add = new InetSocketAddress(mcIPAddress, mcPort);
            MulticastSocket  mcSocket = new MulticastSocket(mcPort);
            // Join the group 
            mcSocket.joinGroup(soc_add, NetworkInterface.getByIndex(0)); 

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

class NodeTCPServer extends Thread{
    public void run(){
        int port = 8000;
        Socket socket = null;
        ServerSocket server = null;
        DataInputStream in =  null;
        DataOutputStream out = null;

        // starts server and waits for a connection
        try{
            System.out.println("Preparing");
            server = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client on port " + server.getLocalPort());

            socket = server.accept();
            System.out.println("Client accepted");

            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            String line = "";

            // reads message from client until "Over" is sent
            while (!line.equals("Over")){
                try{
                    line = in.readUTF();
                    System.out.println(line);
                    String receive = "Server received";
                    out.writeUTF(receive);
                }
                catch(IOException i){
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");

            // close connection
            socket.close();
            in.close();
        }
        catch(IOException i){
            System.out.println(i);
        }
    }
}

class NodeTCPClient extends Thread{
    public void run(){
        //BufferedReader input = null;
        DataInputStream input = null;
        DataOutputStream out = null;
        DataInputStream input_server = null;
        String mess = "";
        String rec = "";
        String address = "127.0.0.1";
        int port = 8000;
        Socket socket = null;
        // establish a connection
        try{
            System.out.println("Preparing as a TCP client");
            socket = new Socket(address, port);
            System.out.println("Connected");

            //input = new BufferedReader(new InputStreamReader(System.in));
            input  = new DataInputStream(System.in);
            out    = new DataOutputStream(socket.getOutputStream());
            input_server = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        }
        catch(UnknownHostException u){
            System.out.println("UnknownHostException: " + u);
        }
        catch(IOException i){
            System.out.println("IOException: " + i);
        }

        // string to read message from input
        String line = "";
        String server = "";

        // keep reading until "Over" is input
        while (!line.equals("Over")){
            try{
                line = input.readLine();
                out.writeUTF(line);
                server = input_server.readUTF();
                System.out.println(server);
            }
            catch(IOException i){
                System.out.println(i);
            }
        }

        // close the connection
        try{
            input.close();
            out.close();
            socket.close();
        }
        catch(IOException i){
            System.out.println(i);
        }
    }
}