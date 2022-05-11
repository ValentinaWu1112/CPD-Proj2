package node;

import java.io.*;
import java.net.*;
import java.lang.Thread;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ClusterNode{
    public static void main(String args[]){
        ClusterNodeBrain nb = new ClusterNodeBrain(args[0], args[1], args[2]);
        nb.start();
    }
}

class ClusterNodeBrain extends Thread{
    private String node_tcp_ip;
    private int node_tcp_port;
    private NodeTCPServer ntcps;
    private NodeTCPClient ntcpc;
    private String node_multicast_ip;
    private int node_multicast_port;
    private NodeMulticastServer nms;
    private NodeMulticastClient nmc;

    public ClusterNodeBrain(String multicast_ip, String tcp_ip, String tcp_port){
        this.node_multicast_ip = multicast_ip;
        this.node_multicast_port = 6666;
        this.node_tcp_ip = tcp_ip;
        this.node_tcp_port = Integer.parseInt(tcp_port);
    }

    public void run(){
        Scanner scan = new Scanner(System.in);
        nms = new NodeMulticastServer(this.node_multicast_ip, this.node_multicast_port);
        nms.start();
        ntcps = new NodeTCPServer(this.node_tcp_ip, this.node_tcp_port);
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
                else if(scanned[1].equals("ssend")){
                    if(ntcps != null){
                        List<String> list = new ArrayList<String>(Arrays.asList(scanned));
                        list.remove(scanned[0]);
                        list.remove(scanned[1]);
                        ntcps.sendTCPMessage(String.join(" ", list));
                    }
                    else System.out.println("Unable to send TCP message, you must establish a connection first.");
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
                    nmc = new NodeMulticastClient("224.0.0.1", 6666);
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
        scan.close();
    }
}

class NodeMulticastClient extends Thread{
    private String raw_ip;
    private int multicast_port;
    private InetAddress ip;
    private DatagramSocket udpSocket;
    private volatile int in_group = 1;

    public NodeMulticastClient(String address, int port){
        this.raw_ip = address;
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
        packet.setAddress(this.ip);
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
            this.ip = InetAddress.getByName(this.raw_ip);
        } catch (Exception e){ 
            e.printStackTrace(); 
        }
    } 
}

class NodeMulticastServer extends Thread{
    private InetAddress ip;
    private SocketAddress soc_add;
    private MulticastSocket mcSocket;
    private DatagramPacket packet;
    private String raw_ip;
    private int multicast_port;
    private volatile int in_group = 0;

    public NodeMulticastServer(String address, int port){
        this.raw_ip = address;
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
            System.out.println("Multicast Server On at " + this.raw_ip + ":" + this.multicast_port);
            this.ip = InetAddress.getByName(this.raw_ip);
            soc_add = new InetSocketAddress(this.ip, this.multicast_port);
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
    private String raw_ip;
    private InetAddress ip;
    private int port;
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in =  null;
    private DataOutputStream out = null;

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

    public NodeTCPServer(String ip, int port){
        this.raw_ip = ip;
        this.port = port;
    }

    public void run(){
        try{
            this.ip = InetAddress.getByName(this.raw_ip);
            server = new ServerSocket(this.port, 50, this.ip);
            while(true){
                System.out.println("Accepting TCP connections at " + server.getLocalSocketAddress());
                socket = server.accept();
                System.out.println("Client accepted");

                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());

                String tcp_message = "";
                while (!tcp_message.equals("close")){
                    try{
                        tcp_message = in.readUTF();
                        System.out.println("[TCP]: " + tcp_message);
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

            String tcp_message = "";
            while (!socket.isClosed()){
                try{
                    tcp_message = input_server.readUTF();
                    System.out.println("[TCP]: " + tcp_message);
                }
                catch(IOException i){
                    System.out.println(i);
                }
            }
        }
        catch(UnknownHostException u){
            System.out.println("UnknownHostException: " + u);
        }
        catch(IOException i){
            System.out.println("IOException: " + i);
        }
    }
}