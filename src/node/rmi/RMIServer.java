package node.rmi;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import crypto.Crypto;
import java.lang.Thread;
import node.tcp.*;
import node.multicast.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import file.*;

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
    private ThreadPoolExecutor executor;
    private String last_joining_nodeid;

    /* 
        Task constituting the process of sending the 
        current membership information to the joining node. 
    */
    class TaskJoinReq implements Runnable{
        private String target_node_id;
        private String counter;

        public TaskJoinReq(String target_node_id, String counter){
            this.target_node_id = target_node_id;
            this.counter = counter;
        }

        public void run(){
            try {
                Random rand = new Random();
                TimeUnit.SECONDS.sleep(rand.nextInt(3));
                MembershipUtils.updateLog(tcp_ip, target_node_id.concat("-").concat(counter).concat(";"));
                MembershipUtils.updateCluster(tcp_ip, target_node_id);
                ntcpc = new NodeTCPClient(this.target_node_id, "7999");
                ntcpc.start();
                try{
                    ntcpc.sendTCPMessage(MembershipUtils.createMessage(tcp_ip, "memshipInfo"));
                } finally{
                    ntcpc.closeTCPConnection();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class TaskLeaveReq implements Runnable{
        private String target_node_id;
        private String counter;

        public TaskLeaveReq(String target_node_id, String counter){
            this.target_node_id = target_node_id;
            this.counter = counter;
        }

        public void run(){
            try {
                Random rand = new Random();
                TimeUnit.SECONDS.sleep(rand.nextInt(3));
                MembershipUtils.updateLog(tcp_ip, target_node_id.concat("-").concat(counter).concat(";"));
                MembershipUtils.updateRemoveCluster(tcp_ip, target_node_id);
                ntcpc = new NodeTCPClient(this.target_node_id, "7999");
                ntcpc.start();
                System.out.println("sending memship info..");
                ntcpc.sendTCPMessage(MembershipUtils.createMessage(tcp_ip, "memshipInfo"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* 
        Task constituting the process of receiving a 
        message containing the membership information and
        update the nodes view of the cluster.. 
    */
    class TaskMemshipInfo implements Runnable{
        private String target_node_id;
        private String raw_cluster_members;
        private String raw_logs;

        public TaskMemshipInfo(String target_node_id, String raw_cluster_members, String raw_logs){
            this.target_node_id = target_node_id;
            this.raw_cluster_members = raw_cluster_members;
            this.raw_logs = raw_logs;
        }

        public void run(){
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* 
        Thread responsible for message scouting on Multicast and TCP servers.
        Both Multicast and TCP servers have a queue structure that contains all
        still unprocessed received messages. The job of this thread is to process 
        and push those messages from the queue to the executor. The executor will 
        then execute the task specified in the message.
    */
    class MessageScout extends Thread{
        String tcp_ip;
        public MessageScout(String tcp_ip){
            this.tcp_ip = tcp_ip;
        }

        private void processMessage(String message){
            String[] message_tokens = message.split(" ");
            String[] message_header = message_tokens[0].split(":");
            String[] message_body = message_tokens[1].split(":");
            String[] body_content = message_body[1].split("_");
            /*
                The node ignores its own messages
            */
            if(message_header[1].equals(tcp_ip)){
                return;
            }
            System.out.println("RECEIVED: " + message);
            //To be changed..'join_request' to 'joinReq'
            if(body_content[0].equals("joinReq")){
                if(last_joining_nodeid == null || !last_joining_nodeid.equals(message_header[1])){
                    last_joining_nodeid = message_header[1];
                    executor.execute(new TaskJoinReq(message_header[1], body_content[1]));
                }
            }
            else if(body_content[0].equals("leaveReq")){
                last_joining_nodeid = message_header[1];
                executor.execute(new TaskLeaveReq(message_header[1], body_content[1]));
            }
            else if(body_content[0].equals("memshipInfo")){
                executor.execute(new TaskMemshipInfo(message_header[1], body_content[1], body_content[2]));
            }
            else if(body_content[0].equals("storeKeyValue")){
                
            }
            return;
        }

        public void run(){
            while(true){
                while(nms != null && nms.messages_queue.size() > 0){
                    String message = nms.messages_queue.remove();
                    processMessage(message);
                }
                while(ntcps != null && ntcps.messages_queue.size() > 0){
                    String message = ntcps.messages_queue.remove();
                    processMessage(message);
                }
            }
        }
    }

    public RMIServerBrain(String tcp_ip, int tcp_port, String multicast_ip, int multicast_port, String node_key){
        this.tcp_ip = tcp_ip;
        this.tcp_port = tcp_port;
        this.multicast_ip = multicast_ip;
        this.multicast_port = multicast_port;
        this.node_key = node_key;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
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
        MembershipUtils.updateCounter(this.tcp_ip);
        nmc.sendMulticastMessage(MembershipUtils.createMessage(this.tcp_ip, "joinReq")); 
        return true;
    }

    public boolean leaveMulticastGroup(){
        if(nms.getInGroup() == 0){
            System.err.println("Already out of group.");
            return false;
        }
        MembershipUtils.updateCounter(this.tcp_ip);
        nmc.sendMulticastMessage(MembershipUtils.createMessage(this.tcp_ip, "leaveReq"));
        System.out.println("leaveMulticastGroup");
        nmc.setInGroup(0);
        nms.leaveMulticastGroup();
        return true;
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
        MessageScout scout = new MessageScout(this.tcp_ip);
        scout.start();
    }
}