package node;

import node.rmi.*;
import java.lang.Thread;

/* 
    ClustedNode runs ClustedNodeBrain thread, the latter starts nodes RMI server allowing 
    the node to be controlled by Client.
*/

public class ClusterNode{
    public static void main(String args[]){
        ClusterNodeBrain nb = new ClusterNodeBrain(args[0], args[1], args[2]);
        nb.start();
    }
}

class ClusterNodeBrain extends Thread{
    private String node_tcp_ip;
    private int node_tcp_port;
    private String node_multicast_ip;
    private int node_multicast_port;
    private RMIServer nrmis;

    public ClusterNodeBrain(String multicast_ip, String tcp_ip, String tcp_port){
        this.node_multicast_ip = multicast_ip;
        this.node_multicast_port = 6666;
        this.node_tcp_ip = tcp_ip;
        this.node_tcp_port = Integer.parseInt(tcp_port);
    }

    public void run(){
        System.out.println("ClusterNodeBrain");
        nrmis = new RMIServer(this.node_tcp_ip, this.node_tcp_port, this.node_multicast_ip, this.node_multicast_port);
        nrmis.start();
    }
}