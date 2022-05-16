package node.multicast;

import java.io.*;
import java.net.*;
import java.lang.Thread;

/* 
    Thread responsible for multicast interaction, i.e, sending multicast messages.. 
*/

public class NodeMulticastClient extends Thread{
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

    public void run(){
        try{
            udpSocket = new DatagramSocket(); 
            this.ip = InetAddress.getByName(this.raw_ip);
        } catch (Exception e){ 
            e.printStackTrace(); 
        }
    } 
}
