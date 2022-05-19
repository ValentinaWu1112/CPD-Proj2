package node.rmi;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import crypto.Crypto;
import java.lang.Thread;
import node.tcp.*;
import node.multicast.*;

/* 
    While RMIServer thread is responsible for receiving nodes info (communication addresses),
    RMIServerBrain implements the API interface handling node tasks. The RMIServerBrain thread 
    starts the Multicast and TCP 'server sides' since it needs access to these objects.
*/

public class RMIServer extends Thread{
    private final String node_key;
    private String tcp_ip;
    private int tcp_port;
    private String multicast_ip;
    private int multicast_port;

    public RMIServer(String tcp_ip, int tcp_port, String multicast_ip, int multicast_port, String node_key){
        this.tcp_ip = tcp_ip;
        this.tcp_port = tcp_port;
        this.multicast_ip = multicast_ip;
        this.multicast_port = multicast_port;
        this.node_key = node_key;
    }

    @Override
    public void run(){
        try{
            RMIServerBrain rmiServer = new RMIServerBrain(tcp_ip, tcp_port, multicast_ip, multicast_port, node_key);
            rmiServer.start();
            RMIServerAPI stubRMIAPI = (RMIServerAPI) UnicastRemoteObject.exportObject(rmiServer, 0);
            Registry registryClusterMembership = LocateRegistry.getRegistry(1090);
            registryClusterMembership.bind(tcp_ip+"RMIAPI", stubRMIAPI);
            System.out.println("RMI Server activated!");
        } catch (Exception e){
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

class RMIServerBrain extends Thread implements RMIServerAPI{
    private final String node_key;
    private String tcp_ip;
    private int tcp_port;
    private NodeTCPServer ntcps;
    private NodeTCPClient ntcpc;
    private String multicast_ip;
    private int multicast_port;
    private NodeMulticastClient nmc;
    private NodeMulticastServer nms;

    public RMIServerBrain(String tcp_ip, int tcp_port, String multicast_ip, int multicast_port, String node_key){
        this.tcp_ip = tcp_ip;
        this.tcp_port = tcp_port;
        this.multicast_ip = multicast_ip;
        this.multicast_port = multicast_port;
        this.node_key = node_key;
    }

    public boolean joinMulticastGroup(){
        if(nms.getInGroup() == 1){
            System.err.println("Already in group.");
            return true;
        }
        System.out.println("joinMulticastGroup");
        /* 
            The node joins the group.
        */
        nms.joinMulticastGroup();
        /* 
            The node starts listening for TCP connections. It must know the
            current state of the cluster.
        */
        if(ntcps == null){
            ntcps = new NodeTCPServer(this.tcp_ip, this.tcp_port);
            ntcps.start();
        }
        /* 
            Multicast client is initialized, meaning the node is able to
            iteract within the cluster.
        */
        if(nmc == null){
            nmc = new NodeMulticastClient(this.multicast_ip, this.multicast_port);
            nmc.start();
        }
        /* 
            Since threads run asynchronously, this makes sure UDP Socket
            is ready before multicast message is sent.
        */
        while(!nmc.getUDPSocket()){}
        /* 
            After start listening for TCP connections, the node multicasts
            that it joined the group. This message causes the other cluster 
            nodes to, after a random time length, connect via TCP and transfer
            the current membership status, i.e, their most recent 32 membership
            log messages aswell as a list of the current membership members.

            TODO: Part of the 'Membership Protocol (Theory) task'. Define the
            multicast messages format.
        */
        nmc.setInGroup(1);
        nmc.sendMulticastMessage("message");
        return true;
    }

    public boolean leaveMulticastGroup(){
        if(nms.getInGroup() == 0){
            System.err.println("Already out of group.");
            return true;
        }
        System.out.println("leaveMulticastGroup");
        nmc.setInGroup(0);
        nms.leaveMulticastGroup();
        return false;
    }

    public boolean getValue(String key){
        System.out.println("getValue");
        return true;
    }

    public boolean putValue(String key, String value){
        System.out.println("putValue");
        return true;
    }

    public boolean deleteValue(String key){
        System.out.println("deleteValue");
        return false;
    }

    public void run() {
        System.out.println("RMIServerBrain");
        /* 
            Although the Multicast Server is initialized the moment the node is created, 
            it is not able to interact with the cluster, it must join the group first. 
            This could also be done when the node joins the cluster group, but (i think)
            it results in a lighter JOIN operation.
        */
        nms = new NodeMulticastServer(this.multicast_ip, this.multicast_port);
        nms.start();
    }
}
