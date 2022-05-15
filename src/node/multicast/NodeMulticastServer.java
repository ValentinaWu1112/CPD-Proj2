package node.multicast;

import java.io.*;
import java.net.*;
import java.lang.Thread;

public class NodeMulticastServer extends Thread{
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
