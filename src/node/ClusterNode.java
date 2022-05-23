package node;

import node.rmi.*;
import java.lang.Thread;

import crypto.Crypto;
import file.FileHandler;

/* 
    ClustedNode runs ClustedNodeBrain thread, the latter starts nodes RMI server allowing 
    the node to be controlled by Client.
*/

public class ClusterNode{
    public static void main(String args[]){
        ClusterNodeBrain nb = new ClusterNodeBrain(args[0], args[1], args[2], args[3]);
        nb.start();
    }
}

class ClusterNodeBrain extends Thread{
    private final String node_key;
    private String node_tcp_ip;
    private int node_tcp_port;
    private String node_multicast_ip;
    private int node_multicast_port;
    private RMIServer nrmis;

    public ClusterNodeBrain(String multicast_ip, String multicast_port, String tcp_ip, String tcp_port){
        this.node_multicast_ip = multicast_ip;
        this.node_multicast_port = Integer.parseInt(multicast_port);
        this.node_tcp_ip = tcp_ip;
        this.node_tcp_port = Integer.parseInt(tcp_port);
        this.node_key = Crypto.encodeValue(this.node_tcp_ip);
    }

    private boolean initNodeFileSystem(){
        try {
            FileHandler.createDirectory("../global/", this.node_key);
            FileHandler.createDirectory("../global/" + this.node_key + "/", "membership");
            FileHandler.createDirectory("../global/" + this.node_key + "/", "storage");
            FileHandler.createFile("../global/" + this.node_key + "/membership/", "cluster_members.txt");
            FileHandler.createFile("../global/" + this.node_key + "/membership/", "log.txt");
            FileHandler.createFile("../global/" + this.node_key + "/membership/", "counter.txt");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void run(){
        System.out.println("ClusterNodeBrain");
        initNodeFileSystem();
        nrmis = new RMIServer(this.node_tcp_ip, this.node_tcp_port, this.node_multicast_ip, this.node_multicast_port, this.node_key);
        nrmis.start();
    }
}