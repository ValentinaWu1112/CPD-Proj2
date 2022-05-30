package file;
import java.util.*;
import java.util.LinkedList;
import crypto.Crypto;

public final class MembershipUtils {
    private MembershipUtils(){}

    public static boolean updateStorage(String node_id, String store){
        String node_key = Crypto.encodeValue(node_id);
        LinkedList<String> files = FileHandler.getDirectoryFiles("../global/"+ node_key + "/", "storage");
        
        String[] pairs = store.split("-");
        for(String pair: pairs){
            String[] key_value = pair.split("\\+");
            if(!files.contains(key_value[0])){
                FileHandler.createFile("../global/"+node_key+"/storage/", key_value[0]);
                return FileHandler.writeFile("../global/"+node_key+"/storage/", key_value[0], key_value[1]);
            } 
        }
        return false;
    }

    /* 
        Returns node that is responsible for some other node keys.
        The node_id param corresponds to the node we trying to find 
        the successor of, so, for example, if we want to get the
        successor of the node with id 1, simply call this method with
        1 as an argument. 

        TODO: optimize time complexity from linear to logarithmic search!
    */
    public static String getResponsibleNode(String node_id){
        String node_key = Crypto.encodeValue(node_id);
        LinkedList<String> members = loadClusterMembers(node_key);
        if(members.size() == 0){
            return null;
        }
        LinkedList<String> hashed_members_list = new LinkedList<>();
        TreeMap<String,String> hashed_members = new TreeMap<>();
        for(String member : members){
            String hashed_member = Crypto.encodeValue(member);
            hashed_members_list.add(hashed_member);
            hashed_members.put(hashed_member, member);
        }
        Collections.sort(hashed_members_list);
        boolean flag = false;
        String respondible_node_id = null;
        for(String hm : hashed_members_list) {
            if(flag){
                respondible_node_id =  hashed_members.get(hm);
                break;
            }
            if(hm.equals(node_key)){
                flag = true;
            }
        }
        if(respondible_node_id == null) respondible_node_id = hashed_members.get(hashed_members_list.get(0));
        return respondible_node_id;
    }

    /* 
        Returns responsible node given a file key.

        TODO: optimize time complexity from linear to logarithmic search!
    */
    public static String getResponsibleNodeGivenKey(String node_id, String key){
        String node_key = Crypto.encodeValue(node_id);
        LinkedList<String> members = loadClusterMembers(node_key);
        LinkedList<String> hashed_members_list = new LinkedList<>();
        TreeMap<String,String> hashed_members = new TreeMap<>();

        if(members.size() > 0){
            for(String member : members){
                String hashed_member = Crypto.encodeValue(member);
                hashed_members_list.add(hashed_member);
                hashed_members.put(hashed_member, member);
            }
            Collections.sort(hashed_members_list);

            String responsible_node = null;

            /* for(String hm : hashed_members_list) {
                if(node_key.compareTo(hm)<=0) responsible_node = hashed_members.get(hm);
            } */
            for(int i=0; i<hashed_members_list.size(); i++){
                if(key.compareTo(hashed_members_list.get(i)) <= 0){
                    responsible_node = hashed_members.get(hashed_members_list.get(i));
                    break;
                }
            }
            
            if(responsible_node == null) responsible_node = hashed_members.get(hashed_members_list.get(0));
            return responsible_node;
        }
        else{
            return null;
        }
    }

    public static boolean updateCounter(String node_id){
        String node_key = Crypto.encodeValue(node_id);
        String counter = getRawCounter(node_key);
        
        int ret = 0;
        if(counter.equals("")) ret = 0;
        else {
            ret = Integer.parseInt(counter);
            ret++;
        }
        
        return FileHandler.writeFile("../global/".concat(node_key).concat("/membership"), "/counter.txt", Integer.toString(ret));
    }

    /* 
        Compares Log registrys. Returns 1 if current is more up to date, 2 otherwise. 
    */
    /* public static int compareLogs(Map<String,String> current, Map<String,String> received){
        //Assuming that a larger Log means more up to date
        if(received.size() < current.size()){
            return 1;
        }
        else if(received.size() > current.size()){
            return 2;
        }
        //If both have the same size, a counter score is required.
        //Incrementing by one current_score (or received_score) if the counter
        //of a specific id is higher.
        else{
            int current_score = 0;
            int received_score = 0;
            for (Map.Entry<String,String> entry : current.entrySet()) {
                //Counting only the matching Log registrys (very naive but whatever)
                if(received.containsKey(entry.getKey())){
                    int cur_current_log_counter = Integer.parseInt(current.get(entry.getKey()));
                    int cur_received_log_counter = Integer.parseInt(received.get(entry.getKey()));
                    if(cur_current_log_counter > cur_received_log_counter){
                        current_score++;
                    }
                    else if(cur_current_log_counter < cur_received_log_counter){
                        received_score++;
                    }
                    else{
                        //They equal so nothing is done
                    }
                }
            }
            return current_score > received_score ? 1 : 2;
        }
    } */

    /* 
        Rewrites Log to the received one. To ensure cluster consistency
        when a node receives a memshipInfo message it will rewrite all
        info it contains at that moment, this way when a node broadcasts
        his current view of the cluster all other nodes will acquire that
        same view.
        Comparing logs is a nice idea to prevent stalled info but that would
        cause nodes to have different cluster views, resulting in a very
        complicated situation to manage when, for example, we need one single
        cluster node to broadcast the memshipInfo (how would they all 'agree'
        that a single node is broadcasting his info if they dont share the same
        view? doesnt seem feasible).
    */
    public static boolean rewriteLog(String node_id, String raw_log){
        try {
            String node_key = Crypto.encodeValue(node_id);
            FileHandler.writeFile("../global/"+node_key+"/membership/", "log.txt", raw_log);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateLog(String node_id, String newLog){
        /*node_id-counter;*/
        String node_key = Crypto.encodeValue(node_id);
        String log = getRawLogs(node_key);
        Map <String,String> logs = toMap(log);
        Map <String,String> newLogs = toMap(newLog);

        for (Map.Entry<String,String> entry : newLogs.entrySet()) {
            if(logs.containsKey(entry.getKey())){
                if(Integer.parseInt(entry.getValue()) > Integer.parseInt(logs.get(entry.getKey()))){
                    logs.remove(entry.getKey());
                    logs.put(entry.getKey(),entry.getValue());
                }
            }
            else logs.put(entry.getKey(),entry.getValue());
        }
        return FileHandler.writeFile("../global/"+node_key+"/membership/", "log.txt", MaptoString(logs));
    }

    /* 
        Overwrites cluster members list to whats given on 'raw_cluster_members' argument.
        This method differs from updateCluster method, since it rewrites everything in the file
        while updateCluster simply add or remove the given member passed as an argument.
    */
    public static boolean rewriteClusterMembers(String node_id, String raw_cluster_members){
        try {
            String node_key = Crypto.encodeValue(node_id);
            FileHandler.writeFile("../global/"+node_key+"/membership/", "cluster_members.txt", raw_cluster_members);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateCluster(String node_id, String cluster){
        String node_key = Crypto.encodeValue(node_id);
        LinkedList <String> memberList = loadClusterMembers(node_key);
        if(!memberList.contains(cluster)) memberList.add(cluster);
        String newList = storeClusterMembers(memberList);
        return FileHandler.writeFile("../global/"+node_key+"/membership/", "cluster_members.txt", newList);
    }

    public static boolean updateRemoveCluster(String node_id, String cluster){
        String node_key = Crypto.encodeValue(node_id);
        LinkedList <String> memberList = loadClusterMembers(node_key);
        memberList.remove(cluster);
        String newList = storeClusterMembers(memberList);
        return FileHandler.writeFile("../global/"+node_key+"/membership/", "cluster_members.txt", newList);
    }

    public static Map<String,String> toMap (String log){
        if(log.length() == 0) {
            return new LinkedHashMap<String,String>();
        }
        Map<String,String> ret = new LinkedHashMap<>();
        String[] logs = log.split(";");
        for(String l: logs){
            String[] tmp = l.split("-");
            ret.put(tmp[0],tmp[1]);
        }
        return ret;
    }

    public static String MaptoString(Map <String,String> logs){
        StringBuilder sb = new StringBuilder();
        int i=0;
        for (Map.Entry<String,String> entry : logs.entrySet()) {
            if(i!=0) sb.append(";");
            sb.append(entry.getKey() + "-" + entry.getValue());
            i++;
        }
        return sb.toString();
    }

    /* 
        Creates any kind of message given the specified operation.

        The protocol parameter is only used to distinguish memshipInfo 
        messages, so when calling this method and the operation argument 
        isn't 'memshipInfo' simply pass a dummy string, eg.: "", as the 
        protocol argument value.

        Identically to the protocol parameter, the dest_node_id parameter
        is only used for storeKeyValue messages, so whenever protocol does 
        not equal 'storeKeyValue' simply pass it as "".
    */
    public static String createMessage(String node_id, String operation, String protocol, String dest_node_id, int store_flag){
        String message = "header:"+node_id+"#body:";
        switch(operation){
            case "joinReq":
                message = message.concat(createJoinReqMessage(node_id));
                break;
            case "memshipInfo":
                message = message.concat(createMembershipInfoMessage(node_id, protocol));
                break;
            case "storeKeyValue":
                message = message.concat(createStoreKeyValueMessage(node_id, dest_node_id, store_flag));
                break;
            case "leaveReq":
                message = message.concat(createLeaveReqMessage(node_id));
                break;
            case "deleteKey":
                message = message.concat(createDeleteKeyMessage(protocol));
                break;
            case "getValue":
                message = message.concat(createGetValueMessage(protocol));
                break;
            case "putValue":
                message = message.concat(createPutValueMessage(protocol,dest_node_id));
                break;
            case "getReturn":
                message = message.concat(createGetReturnMessage(protocol));
                break;
        }
        
        return message;
    }

    public static String createJoinReqMessage(String node_id){
        String node_key = Crypto.encodeValue(node_id);
        String node_counter = FileHandler.readFile("../global/"+node_key+"/membership/", "counter.txt");
        String message = "joinReq_"+node_counter;
        return message;
    }
    
    public static String createDeleteKeyMessage(String key){
        /*protocol has the value of the key */
        return "deleteKey_"+key;
    }

    public static String createGetValueMessage(String key){
        /*protocol has the value of the key */
        return "getValue_"+key;
    }

    public static String createPutValueMessage(String key, String value){
        /*protocol is the key and dest_node_id */
        return "storeKeyValue_"+key+"+"+value;
    }

    public static String createGetReturnMessage(String value){
        return "getReturn_"+value;
    }

    public static String createMembershipInfoMessage(String node_id, String protocol){
        String node_key = Crypto.encodeValue(node_id);
        String cluster_members = getRawClusterMembers(node_key);
        String logs = getRawLogs(node_key);
        String message = "memshipInfo"+protocol+"_"+cluster_members+"_"+logs;
        return message;
    }

    public static String createStoreKeyValueMessage(String node_id, String dest_node_id, int store_flag){
        String successor_raw_key_values = store_flag == 0 ? getRawKeyValuesSuccessor(node_id, dest_node_id) : getRawKeyValues(node_id);
        String message = "storeKeyValue_"+successor_raw_key_values;
        return message;
    }

    public static String createLeaveReqMessage(String node_id){
        String node_key = Crypto.encodeValue(node_id);
        String node_counter = FileHandler.readFile("../global/"+node_key+"/membership/", "counter.txt");
        String message = "leaveReq_"+node_counter;
        return message;
    }

    public static String getRawClusterMembers(String node_key){
        String raw_cluster_members = FileHandler.readFile("../global/"+node_key+"/membership/", "cluster_members.txt");
        return raw_cluster_members;
    }

    public static LinkedList<String> loadClusterMembers(String node_key){
        String raw_cluster_members = getRawClusterMembers(node_key);
        if(raw_cluster_members.equals("")) return new LinkedList<String>();
        String[] cluster_members_array = raw_cluster_members.split("-");
        LinkedList<String> cluster_members = new LinkedList<>();
        for(int i=0; i<cluster_members_array.length; i++){
            //System.out.println("member: " + cluster_members_array[i]);
            cluster_members.add(cluster_members_array[i]);
        }
        Collections.sort(cluster_members);  
        return cluster_members;
    }

    public static String storeClusterMembers(LinkedList<String> cluster_members){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<cluster_members.size(); i++){
            if(i!=0) sb.append("-");
            sb.append(cluster_members.get(i));
        }
        return sb.toString();
    }

    public static String getRawLogs(String node_key){
        String raw_logs = FileHandler.readFile("../global/"+node_key+"/membership/", "log.txt");
        return raw_logs;
    }

    public static String getRawCounter(String node_key){
        String raw_counter = FileHandler.readFile("../global/"+node_key+"/membership/", "counter.txt");
        return raw_counter;
    }

    /* 
        Returns a raw data string containing all key-values that
        a node contains.
    */
    public static String getRawKeyValues(String node_id){
        String node_key = Crypto.encodeValue(node_id);
        String key_values = "";
        LinkedList<String> files = FileHandler.getDirectoryFiles("../global/"+ node_key + "/", "storage");
        for(String file : files){
            String value = FileHandler.readFile("../global/"+node_key+"/storage/", file);
            key_values = key_values.concat(file+"+"+value);
            key_values = key_values.concat("-");
            FileHandler.deleteUnity("../global/"+node_key+"/storage/", file);
        }
        if(key_values.length() > 0) key_values = key_values.substring(0, key_values.length()-1);
        return key_values;
    }

    /* 
        Returns a raw data string containing all key-values that a
        specific node, given its key on resp_node_key parameter, is
        responsible for.
    */
    public static String getRawKeyValuesSuccessor(String node_id, String resp_node_id){
        String node_key = Crypto.encodeValue(node_id);
        String resp_node_key = Crypto.encodeValue(resp_node_id);
        String key_values = "";
        LinkedList<String> files = FileHandler.getDirectoryFiles("../global/"+ node_key + "/", "storage");
        for(String file : files){
            if(getResponsibleNodeGivenKey(node_id, file).equals(resp_node_id)){
                String value = FileHandler.readFile("../global/"+node_key+"/storage/", file);
                key_values = key_values.concat(file+"+"+value);
                key_values = key_values.concat("-");
                FileHandler.deleteUnity("../global/"+node_key+"/storage/", file);
            }
        }
        if(key_values.length() > 0) key_values = key_values.substring(0, key_values.length()-1);
        return key_values;
    }
}
