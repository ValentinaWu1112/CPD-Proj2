import java.io.*;
import java.net.*;
import java.lang.Thread;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/* 
    Questions:
        1. How does a Node know that the Node he is establishing a TCP connection with
        is already connected, through TCP, to some other Node? 
        Since line 288 (socket = new Socket(this.ip_target, this.port_target);) doesnt
        throw any exception, how could it know that it can't connect and therefore know
        that it should wait for the requested Node availability. Even sending a TCP message
        in this state (line 268) doesnt throw any exception..This is a problem since a Node
        might think he is connected, when in fact he isn't, and start transmitting sensitive 
        information thinking its arriving to the destination Node when in fact it's not, resulting
        in information loss from an apparently reliable channel. 

        Possible answer to 1. - Altough stalled, there is a connection, only when the Node, that
        connected first, disconnects is the 'server' Node starts receiving the second connecting Node
        messages. So a simple protocol of message exchanching before data transmission is required so
        no information is lost.

*/

public class Node{
    public static void main(String args[]){
        NodeBrain nb = new NodeBrain(args[0], args[1]);
        nb.start();
    }
}

class NodeBrain extends Thread{
    private String node_name;
    private int node_tcp_port;
    private NodeTCPServer ntcps;
    private NodeTCPClient ntcpc;
    private NodeMulticastServer nms;
    private NodeMulticastClient nmc;

    public NodeBrain(String name, String tcp_port){
        this.node_name = name;
        this.node_tcp_port = Integer.parseInt(tcp_port);
    }

    public void run(){
        Scanner scan = new Scanner(System.in);
        nms = new NodeMulticastServer("224.0.0.1", 4000);
        nms.start();
        ntcps = new NodeTCPServer(this.node_tcp_port);
        ntcps.start();

        String[] scanned = {""};
        while(!scanned[0].equals("stop")){
            String raw_scanned = scan.nextLine();
            scanned = raw_scanned.split(" ");
            if(scanned[0].equals("tcp")){
                if(scanned[1].equals("connect")){
                    ntcpc = new NodeTCPClient(scanned[2], scanned[3]);
                    ntcpc.start();
                }
                else if(scanned[1].equals("send")){
                    if(ntcpc != null){
                        List<String> list = new ArrayList<String>(Arrays.asList(scanned));
                        list.remove(scanned[0]);
                        list.remove(scanned[1]);
                        ntcpc.sendTCPMessage(String.join(" ", list));
                    }
                    else System.out.println("Unable to send TCP message, you must establish a connection first.");
                }
                else if(scanned[1].equals("close")){
                    ntcpc.sendTCPMessage(scanned[1]);
                    ntcpc.closeTCPConnection();
                    ntcpc = null;
                }
                else{
                    System.err.println("Unknown command.");
                }
            }
            else if(scanned[0].equals("multicast")){
                if(scanned[1].equals("join")){
                    nms.joinMulticastGroup();
                    nmc = new NodeMulticastClient("224.0.0.1", 4000);
                    nmc.start();
                }
                else if(scanned[1].equals("leave")){
                    nmc.setInGroup(0);
                    nms.leaveMulticastGroup();
                }
                else if(scanned[1].equals("send")){
                    List<String> list = new ArrayList<String>(Arrays.asList(scanned));
                    list.remove(scanned[0]);
                    list.remove(scanned[1]);
                    nmc.sendMulticastMessage(String.join(" ", list));
                }
                else{
                    System.err.println("Unknown command.");
                }
            }
            else{
                System.err.println("Unknown command.");
            }
        }
    }
}

class NodeMulticastClient extends Thread{
    private Scanner scan = new Scanner(System.in); 
    private DataInputStream input;
    private DataOutputStream out;
    private String raw_multicast_address;
    private int multicast_port;
    private InetAddress multicast_address;
    private DatagramSocket udpSocket;
    private volatile int in_group = 1;

    public NodeMulticastClient(String address, int port){
        this.raw_multicast_address = address;
        this.multicast_port = port;
    }

    public void setInGroup(int value){
        if(value != 0 && value != 1){
            System.err.println("Invalid in_group value.");
            return;
        }
        in_group = value;
        return;
    }

    public void sendMulticastMessage(String message){
        if(in_group == 0){
            System.err.println("Unable to send message, join the group before any messaging attempt.");
            return;
        }
        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length);
        packet.setAddress(multicast_address);
        packet.setPort(this.multicast_port);
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
            multicast_address = InetAddress.getByName(this.raw_multicast_address);
        } catch (Exception e){ 
            e.printStackTrace(); 
        }
    } 
}

class NodeMulticastServer extends Thread{
    private InetAddress multicast_address;
    private SocketAddress soc_add;
    private MulticastSocket mcSocket;
    private DatagramPacket packet;
    private String raw_multicast_address;
    private int multicast_port;
    private String received_message;
    private volatile int in_group = 0;

    public NodeMulticastServer(String address, int port){
        this.raw_multicast_address = address;
        this.multicast_port = port;
    }

    public void joinMulticastGroup(){
        try{
            in_group = 1;
            mcSocket.joinGroup(soc_add, NetworkInterface.getByIndex(0)); 
        } catch(Exception e){
            e.printStackTrace(); 
        }
        
    }

    public void leaveMulticastGroup(){
        try{
            in_group = 0;
            mcSocket.leaveGroup(soc_add, NetworkInterface.getByIndex(0)); 
        } catch(IOException e){
            System.out.println(e);
        }
        return;
    }

    public void run(){
        try{
            System.out.println("Multicast Server On at " + this.raw_multicast_address + ":" + this.multicast_port);
            this.multicast_address = InetAddress.getByName(this.raw_multicast_address);
            soc_add = new InetSocketAddress(this.multicast_address, this.multicast_port);
            mcSocket = new MulticastSocket(this.multicast_port);

            packet = new DatagramPacket(new byte[1024], 1024);

            while(true){
                this.mcSocket.receive(packet);
                if(in_group == 1){
                    String msg = new String(this.packet.getData(), 
                                            this.packet.getOffset(), 
                                            this.packet.getLength());
                    System.out.println("[Multicast]:" + msg); 
                }
            }
        } catch(Exception e){ 
            e.printStackTrace(); 
        }
    }
}

class NodeTCPServer extends Thread{
    private int port;
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in =  null;
    private DataOutputStream out = null;

    public NodeTCPServer(int port){
        this.port = port;
    }

    public void run(){
        try{
            server = new ServerSocket(this.port);
            while(true){
                System.out.println("Accepting TCP connections at " + server.getLocalSocketAddress());
                socket = server.accept();
                System.out.println("Client accepted");

                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                String line = "";

                while (!line.equals("close")){
                    try{
                        line = in.readUTF();
                        System.out.println("[TCP]: " + line);
                    }
                    catch(IOException i){
                        System.out.println(i);
                    }
                }

                // close connection
                socket.close();
                in.close();
                System.out.println("Connection closed.");
            }
        }
        catch(IOException i){
            System.out.println(i);
        }
    }
}

class NodeTCPClient extends Thread{
    private String ip_target;
    private int port_target;
    private DataOutputStream out;
    private DataInputStream input_server;
    private String mess = "";
    private String rec = "";
    private Socket socket;

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
            System.out.println("Connection closed");
            return true;
        }
        catch(IOException e){
            System.err.println(e);
            return false;
        }
    }

    public void run(){
        try{
            socket = new Socket(this.ip_target, this.port_target);
            System.out.println("Connected");

            out = new DataOutputStream(socket.getOutputStream());
            input_server = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        }
        catch(UnknownHostException u){
            System.out.println("UnknownHostException: " + u);
        }
        catch(IOException i){
            System.out.println("IOException: " + i);
        }
    }
}