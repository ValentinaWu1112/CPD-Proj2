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
    private String tcp_ip;
    private int tcp_port;
    private NodeTCPServer ntcps;
    private NodeTCPClient ntcpc;
    private String multicast_ip;
    private int multicast_port;
    private NodeMulticastClient nmc;
    private NodeMulticastServer nms;
    private ThreadPoolExecutor executor;
    private volatile String last_joining_nodeid;
    MessageScout scout;
    /* 
        Keeps track of number of joinreq messages sent.
    */
    private int joinreq_timeout_counter;
    /* 
        Keeps track of number of memshipinfo messages received. 
    */
    private volatile int received_memshipinfo_messages_counter;

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
                    ntcpc.sendTCPMessage(MembershipUtils.createMessage(tcp_ip, "memshipInfo", "TCP", "", -1));
                    String responsible_node = MembershipUtils.getResponsibleNode(target_node_id);
                    if(responsible_node != null && responsible_node.equals(tcp_ip)){
                        ntcpc.sendTCPMessage(MembershipUtils.createMessage(tcp_ip, "storeKeyValue", "", target_node_id, 0));
                    }
                } finally{
                    ntcpc.closeTCPConnection();
                    ntcpc = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /* 
        Task responsible for nodes join timeout mechanism.
    */
    class TaskJoinTimeoutHandler implements Runnable{

        public TaskJoinTimeoutHandler(){}

        public void run(){
            try {
                /* 
                    Sending at maximum 3 joinReq messages. After that, 
                    the number of 'received_memshipinfo_messages_counter' (rmmc)
                    determines (we hope) the number of nodes in the cluster on
                    a range of [0,..,3], i.e, say 'rmmmc' == 2 after three 
                    successfully sent joinReq messages, then we must assume theres 
                    only 3 nodes in the cluster (the joinReq issuer included).
                */
                while(received_memshipinfo_messages_counter < 3){
                    if(joinreq_timeout_counter >= 3){
                        break;
                    }
                    nmc.sendMulticastMessage(MembershipUtils.createMessage(tcp_ip, "joinReq", "", "", -1)); 
                    TimeUnit.SECONDS.sleep(2);
                    joinreq_timeout_counter++;
                }

                /* 
                    The node's alone in the cluster.
                */
                if(received_memshipinfo_messages_counter == 0){
                    System.err.println("IM ALONE!");
                    /* 
                        Since the node is alone in the cluster, he won't receive
                        any cluster information, so he creates his own containing
                        only him. (yea, a node is a masculine character) 
                    */
                    MembershipUtils.updateLog(tcp_ip, tcp_ip.concat("-").concat(MembershipUtils.getRawCounter(Crypto.encodeValue((tcp_ip)))).concat(";"));
                    MembershipUtils.rewriteClusterMembers(tcp_ip, tcp_ip);
                }
                else if(received_memshipinfo_messages_counter == 1){
                    System.err.println("IM HERE WITH SOMEONE ELSE o.O");
                }
                else if(received_memshipinfo_messages_counter == 2){
                    System.err.println("I GOT 2 FRIENDS, WE 3 HERE!");
                }
                else{
                    System.err.println("WHOLE LOTTA NODES HERE!");
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
                if(last_joining_nodeid != null && last_joining_nodeid.equals(target_node_id)){
                    last_joining_nodeid = null;
                }
                MembershipUtils.updateLog(tcp_ip, target_node_id.concat("-").concat(counter).concat(";"));
                MembershipUtils.updateRemoveCluster(tcp_ip, target_node_id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class TaskReceiveDeleteKey implements Runnable{
        private String key;

        public TaskReceiveDeleteKey(String key){
            this.key = key;
        }

        public void run(){
            try {
                FileHandler.delete("../global/"+Crypto.encodeValue(tcp_ip)+"/storage/"+key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class TaskReceiveGetValue implements Runnable{
        private String target_node_id;
        private String key;

        public TaskReceiveGetValue(String target_node_id, String key){
            this.target_node_id = target_node_id;
            this.key = key;
        }

        public void run(){
            try {
                String out = FileHandler.readFile("../global/"+Crypto.encodeValue(tcp_ip)+"/storage/", key);
                ntcpc = new NodeTCPClient(this.target_node_id, "7999");
                ntcpc.start();
                try{
                    ntcpc.sendTCPMessage(MembershipUtils.createMessage(tcp_ip, "getReturn", out, "", -1));
                    
                } finally{
                    ntcpc.closeTCPConnection();
                    ntcpc = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class TaskStorageValue implements Runnable{
        private String store;

        public TaskStorageValue(String store){
            this.store = store;
        }

        public void run(){
            try{
                MembershipUtils.updateStorage(tcp_ip, store);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class TaskDeleteKey implements Runnable{
        private String target_node_id;
        private String key;

        public TaskDeleteKey(String target_node_id, String key){
            this.target_node_id = target_node_id;
            this.key = key;
        }

        public void run(){
            try{
                ntcpc = new NodeTCPClient(this.target_node_id, "7999");
                ntcpc.start();
                try{
                    ntcpc.sendTCPMessage(MembershipUtils.createMessage(tcp_ip, "deleteKey", key, "", -1));
                }
                finally{
                    ntcpc.closeTCPConnection();
                    ntcpc = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class TaskPutValue implements Runnable{
        private String target_node_id;
        private String key;
        private String value;

        private TaskPutValue(String target_node_id, String key, String value){
            this.target_node_id = target_node_id;
            this.key = key;
            this.value=value;
        }

        public void run(){
            try{
                ntcpc = new NodeTCPClient(this.target_node_id, "7999");
                ntcpc.start();
                try{
                    ntcpc.sendTCPMessage(MembershipUtils.createMessage(tcp_ip, "putValue", key, value, -1));
                }
                finally{
                    ntcpc.closeTCPConnection();
                    ntcpc = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    class TaskGetValue implements Runnable{
        private String target_node_id;
        private String key;

        public TaskGetValue(String target_node_id, String key){
            this.target_node_id = target_node_id;
            this.key = key;
        }

        public void run(){
            try{
                ntcpc = new NodeTCPClient(this.target_node_id, "7999");
                ntcpc.start();
                try{
                    ntcpc.sendTCPMessage(MembershipUtils.createMessage(tcp_ip, "getValue", key, "", -1));
                }
                finally{
                    ntcpc.closeTCPConnection();
                    ntcpc = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* 
        Task constituting the process of receiving a 
        message containing the membership information and
        updating the nodes view of the cluster. 
    */
    class TaskMemshipInfo implements Runnable{
        private String raw_cluster_members;
        private String raw_logs;

        public TaskMemshipInfo(String raw_cluster_members, String raw_logs){
            this.raw_cluster_members = raw_cluster_members;
            this.raw_logs = raw_logs;
        }

        public void run(){
            try {
                received_memshipinfo_messages_counter++;
                MembershipUtils.rewriteClusterMembers(tcp_ip, raw_cluster_members);
                MembershipUtils.rewriteLog(tcp_ip, raw_logs);
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

        /*response of the getValue*/
        String getValue;
        boolean flagGet = false;

        public MessageScout(String tcp_ip){
            this.tcp_ip = tcp_ip;
        }

        public String getValue(){
            return getValue;
        }

        public void resetFlagGet(){
            flagGet=false;
            return;
        }

        public boolean flagGet(){
            return flagGet;
        }

        private void processMessage(String message){
            String[] message_tokens = message.split("#");
            String[] message_header = message_tokens[0].split(":");
            String[] message_body = message_tokens[1].split(":");
            String[] body_content = message_body[1].split("_");
            /*
                The node ignores its own Multicast messages
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
                executor.execute(new TaskLeaveReq(message_header[1], body_content[1]));
            }
            else if(body_content[0].equals("memshipInfoUDP")){
                executor.execute(new TaskMemshipInfo(body_content[1], body_content[2]));
            }
            else if(body_content[0].equals("memshipInfoTCP") && received_memshipinfo_messages_counter < 3){
                executor.execute(new TaskMemshipInfo(body_content[1], body_content[2]));
            }
            else if(body_content[0].equals("deleteKey")){
                executor.execute(new TaskReceiveDeleteKey(body_content[1]));
            }
            else if(body_content[0].equals("getValue")){
                executor.execute(new TaskReceiveGetValue(message_header[1],body_content[1]));
            }
            else if(body_content[0].equals("getReturn")){
                getValue=new String(body_content[1]);
                flagGet=true;
                System.out.println("string: " + getValue + " flag: " + flagGet);
;            }
            else if(body_content[0].equals("storeKeyValue")){
                if(body_content.length < 2) return;
                executor.execute(new TaskStorageValue(body_content[1]));
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
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        this.joinreq_timeout_counter = 0;
        this.received_memshipinfo_messages_counter = 0;
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
        executor.execute(new TaskJoinTimeoutHandler());
        return true;
    }

    public boolean leaveMulticastGroup(){
        if(nms.getInGroup() == 0){
            System.err.println("Already out of group.");
            return false;
        }
        MembershipUtils.updateCounter(this.tcp_ip);
        joinreq_timeout_counter = 0;
        received_memshipinfo_messages_counter = 0;
        String responsible_node = MembershipUtils.getResponsibleNode(tcp_ip);
        if(!responsible_node.equals(tcp_ip)){
            ntcpc = new NodeTCPClient(responsible_node, "7999");
            ntcpc.start();
            try{
                ntcpc.sendTCPMessage(MembershipUtils.createMessage(tcp_ip, "storeKeyValue", "", responsible_node, 1));
            }
            finally{
                ntcpc.closeTCPConnection();
                ntcpc = null;
            }
        }
        nmc.sendMulticastMessage(MembershipUtils.createMessage(this.tcp_ip, "leaveReq", "", "", -1));
        System.out.println("leaveMulticastGroup");
        nmc.setInGroup(0);
        nms.leaveMulticastGroup();
        return true;
    }

    public String getValue(String key){
        try{
            System.out.println("get(" + key + ")");
            if(FileHandler.isFile("../global/" + Crypto.encodeValue(tcp_ip) + "/storage/"+ key)){
                return FileHandler.readFile("../global/"+Crypto.encodeValue(tcp_ip) + "/storage/", key);
            }
            else{
                executor.execute(new TaskGetValue(MembershipUtils.getResponsibleNodeGivenKey(tcp_ip, key),key));
                while(!scout.flagGet()){
                    System.out.println("false");
                }
                
                String get = scout.getValue();
                scout.resetFlagGet();
                System.out.println("get: " + get + " flag: " + scout.flagGet());
                return get;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean putValue(String key, String value){
        try {
            System.out.println("putValue (" +  key + "," + value + ")");
            String nodeResp = MembershipUtils.getResponsibleNodeGivenKey(tcp_ip,key);
            if(nodeResp.equals(tcp_ip)){
                FileHandler.createFile("../global/" + Crypto.encodeValue(tcp_ip) + "/storage/", key);
                FileHandler.writeFile("../global/" + Crypto.encodeValue(tcp_ip) + "/storage/", key, value);
            }
            else{
                executor.execute(new TaskPutValue(nodeResp, key, value));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteKey(String key){
        try{
            System.out.println("delete (" + key + ")");
            if(FileHandler.isFile("../global/" + Crypto.encodeValue(tcp_ip) + "/storage/"+ key)){
                return FileHandler.delete("../global/"+Crypto.encodeValue(tcp_ip) + "/storage/"+ key);
            }
            else{
                executor.execute(new TaskDeleteKey(MembershipUtils.getResponsibleNodeGivenKey(tcp_ip, key),key));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
        scout = new MessageScout(this.tcp_ip);
        scout.start();
    }
}