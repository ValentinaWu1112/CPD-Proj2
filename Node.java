import java.io.*;
import java.net.*;
import java.lang.Thread;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Node{
    public static void main(String args[]){
        NodeBrain nb = new NodeBrain(args[0], args[1]);
        nb.start();
    }
}

class NodeBrain extends Thread{
    private String node_name;
    private int node_tcp_port;

    public NodeBrain(String name, String tcp_port){
        this.node_name = name;
        this.node_tcp_port = Integer.parseInt(tcp_port);
    }

    public void run(){
        Scanner scan = new Scanner(System.in);
        NodeMulticastServer nms = new NodeMulticastServer();
        nms.start();
        NodeMulticastClient nmc = new NodeMulticastClient();
        nmc.start();
        //if(Integer.parseInt(args[0]) == 0){
        NodeTCPServer ntcps = new NodeTCPServer(this.node_tcp_port);
        ntcps.start();
        //}
        //else{
            //NodeTCPClient ntcpc = new NodeTCPClient();
            //ntcpc.start();
        //}

        String[] scanned = {""};
        NodeTCPClient ntcpc = null;
        while(!scanned[0].equals("stop")){
            String raw_scanned = scan.nextLine();
            scanned = raw_scanned.split(" ");
            if(scanned[0].equals("tcp")){
                if(scanned[1].equals("connect")){
                    ntcpc = new NodeTCPClient(scanned[2], scanned[3]);
                    ntcpc.start();
                }
                else if(scanned[1].equals("send")){
                    List<String> list = new ArrayList<String>(Arrays.asList(scanned));
                    list.remove(scanned[0]);
                    list.remove(scanned[1]);
                    ntcpc.sendTCPMessage(String.join(" ", list));
                }
            }
            else if(scanned[0].equals("multicast")){
                if(scanned[1].equals("leave")){
                    nms.leaveMulticastGroup();
                    break;
                }
                else{
                    List<String> list = new ArrayList<String>(Arrays.asList(scanned));
                    list.remove(scanned[0]);
                    nmc.sendMulticastMessage(String.join(" ", list));
                }
            }
            else{

            }
        }
    }
}

class NodeMulticastClient extends Thread{
    private Scanner scan = new Scanner(System.in); 
    private DataInputStream input;
    private DataOutputStream out;
    private final int mcPort = 42069; 
    private final String mcIPStr = "224.0.0.1";
    private InetAddress mcIPAddress;
    private DatagramSocket udpSocket;

    public void sendMulticastMessage(String message){
        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length);
        packet.setAddress(mcIPAddress);
        packet.setPort(mcPort);
        try{
            udpSocket.send(packet);
        } catch(IOException e){
            System.out.println(e);
        }
        return;
    }

    public void run() {
        try{
            udpSocket = new DatagramSocket(); 
            mcIPAddress = InetAddress.getByName(mcIPStr);
        } catch (Exception e){ 
            e.printStackTrace(); 
        }
    } 
}

class NodeMulticastServer extends Thread{
    private InetAddress mcIPAddress;
    private SocketAddress soc_add;
    private MulticastSocket mcSocket;
    private DatagramPacket packet;
    private final int mcPort = 42069;
    private String received_message = "";
    private int inGroup = 1;

    public void leaveMulticastGroup(){
        try{
            inGroup = 0;
            mcSocket.leaveGroup(soc_add, NetworkInterface.getByIndex(0)); 
            System.out.println("group was left, i should not be receiving any more messages");
        } catch(IOException e){
            System.out.println(e);
        }
        return;
    }

    public void run(){
        try{
            System.out.println("Multicast Server On");
            mcIPAddress = InetAddress.getByName("224.0.0.1");
            soc_add = new InetSocketAddress(mcIPAddress, mcPort);
            mcSocket = new MulticastSocket(mcPort);
            // Join the group 
            mcSocket.joinGroup(soc_add, NetworkInterface.getByIndex(0)); 

            packet = new DatagramPacket(new byte[1024], 1024);

            while(true){
                this.mcSocket.receive(packet);
                if(inGroup == 0) break;
                String msg = new String(this.packet.getData(), 
                                        this.packet.getOffset(), 
                                        this.packet.getLength());
                System.out.println("[Multicast] Message:" + msg); 
            }
        } catch(Exception e){ 
            e.printStackTrace(); 
        }
    }
}

class NodeTCPServer extends Thread{
    int port;

    public NodeTCPServer(int port){
        this.port = port;
    }

    public void run(){
        Socket socket = null;
        ServerSocket server = null;
        DataInputStream in =  null;
        DataOutputStream out = null;

        // starts server and waits for a connection
        try{
            System.out.println("TCP Server On");
            server = new ServerSocket(this.port);
            System.out.println("TCP Socket Address: " + server.getLocalSocketAddress());
            System.out.println("TCP Socket Port: " + server.getLocalPort());

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
    String ip_target;
    int port_target;
    //BufferedReader input = null;
    //DataInputStream input;
    DataOutputStream out;
    DataInputStream input_server;
    String mess = "";
    String rec = "";
    Socket socket;
    boolean is_connected = false;

    public NodeTCPClient(String target, String port){
        this.ip_target = target;
        this.port_target = Integer.parseInt(port);
    }

    public boolean sendTCPMessage(String message){
        try{
            this.out.writeUTF(message);
            return true;
        }
        catch(IOException e){
            System.err.println(e);
            return false;
        }
    }

    public boolean closeTCPConnection(){
        try{
            out.close();
            socket.close();
            return true;
        }
        catch(IOException e){
            System.err.println(e);
            return false;
        }
    }

    public void run(){
        // establish a connection
        try{
            System.out.println("Preparing as a TCP client");
            socket = new Socket(this.ip_target, this.port_target);
            is_connected = true;
            System.out.println("Connected");

            //input = new BufferedReader(new InputStreamReader(System.in));
            //input  = new DataInputStream(System.in);
            out = new DataOutputStream(socket.getOutputStream());
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
        /* while (!line.equals("Over")){
            try{
                //line = input.readLine();
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
            //input.close();
            out.close();
            socket.close();
        }
        catch(IOException i){
            System.out.println(i);
        } */
    }
}