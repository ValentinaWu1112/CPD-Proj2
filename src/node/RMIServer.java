package node;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.lang.Thread;
import node.rmi.*;
import node.tcp.*;
import node.multicast.*;

public class RMIServer extends Thread{
    private String tcp_ip;
    private int tcp_port;
    private String multicast_ip;
    private int multicast_port;

    public RMIServer(String tcp_ip, int tcp_port, String multicast_ip, int multicast_port){
        this.tcp_ip = tcp_ip;
        this.tcp_port = tcp_port;
        this.multicast_ip = multicast_ip;
        this.multicast_port = multicast_port;
    }

    @Override
    public void run(){
        try{
            //System.setProperty("java.rmi.server.hostname", "192.168.1.67");
            RMIServerBrain rmiServer = new RMIServerBrain(tcp_ip, tcp_port, multicast_ip, multicast_port);
            rmiServer.start();
            RMIServerAPI stubRMIAPI = (RMIServerAPI) UnicastRemoteObject.exportObject(rmiServer, 0);
            Registry registryClusterMembership = LocateRegistry.createRegistry(1090);
            registryClusterMembership.bind(tcp_ip+"RMIAPI", stubRMIAPI);
        } catch (Exception e){
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

class RMIServerBrain extends Thread implements RMIServerAPI{
    private String tcp_ip;
    private int tcp_port;
    private NodeTCPServer ntcps;
    private NodeTCPClient ntcpc;
    private String multicast_ip;
    private int multicast_port;
    private NodeMulticastClient nmc;
    private NodeMulticastServer nms;

    public RMIServerBrain(String tcp_ip, int tcp_port, String multicast_ip, int multicast_port){
        this.tcp_ip = tcp_ip;
        this.tcp_port = tcp_port;
        this.multicast_ip = multicast_ip;
        this.multicast_port = multicast_port;
    }

    public boolean joinMulticastGroup(){
        System.out.println("joinMulticastGroup");
        nms.joinMulticastGroup();
        nmc = new NodeMulticastClient(this.multicast_ip, this.multicast_port);
        nmc.start();
        return true;
    }

    public boolean leaveMulticastGroup(){
        System.out.println("leaveMulticastGroup");
        nmc.setInGroup(0);
        nms.leaveMulticastGroup();
        return false;
    }

    public boolean getValue(){
        System.out.println("getValue");
        return true;
    }

    public boolean putValue(){
        System.out.println("putValue");
        return true;
    }

    public boolean deleteValue(){
        System.out.println("deleteValue");
        return false;
    }

    public void run() {
        System.out.println("RMIServerBrain");
        nms = new NodeMulticastServer(this.multicast_ip, this.multicast_port);
        nms.start();
        ntcps = new NodeTCPServer(this.tcp_ip, this.tcp_port);
        ntcps.start();
    }
}
