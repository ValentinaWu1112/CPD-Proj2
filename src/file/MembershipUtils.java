package file;
import java.util.*;
import java.util.LinkedList;
import crypto.Crypto;

public final class MembershipUtils {
    private MembershipUtils(){}

    /* 
        Returns node that is responsible for some other node keys.
    */
    public static String getResponsibleNode(String node_id){
        String node_key = Crypto.encodeValue(node_id);
        LinkedList<String> members = loadClusterMembers(node_key);
        LinkedList<String> hashed_members_list = loadClusterMembers(node_key);
        TreeMap<String,String> hashed_members = new TreeMap<>();
        for(String member : members){
            String hashed_member = Crypto.encodeValue(member);
            hashed_members_list.add(hashed_member);
            hashed_members.put(hashed_member, member);
        }
        boolean flag = false;
        String respondible_node_id = null;
        for(String hm : hashed_members_list) {
            if(flag){
                respondible_node_id =  hashed_members.get(hm);
            }
            if(hm.equals(node_key)){
                flag = true;
            }
        }
        if(respondible_node_id == null) respondible_node_id = hashed_members.get(hashed_members_list.get(0));
        return respondible_node_id;
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

    public static boolean updateCluster(String node_id, String cluster){
        String node_key = Crypto.encodeValue(node_id);
        LinkedList <String> memberList = loadClusterMembers(node_key);
        if(!memberList.contains(cluster)) memberList.add(cluster);
        //System.out.println("list: " + memberList);
        String newList = storeClusterMembers(memberList);
        return FileHandler.writeFile("../global/"+node_key+"/membership/", "cluster_members.txt", newList);
    }

    public static boolean updateRemoveCluster(String node_id, String cluster){
        String node_key = Crypto.encodeValue(node_id);
        LinkedList <String> memberList = loadClusterMembers(node_key);
        //System.out.println("list: " + memberList);
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

    public static String createMessage(String node_id, String operation){
        String message = "header:"+node_id+" body:";
        switch(operation){
            case "joinReq":
                message = message.concat(createJoinReqMessage(node_id));
                break;
            case "memshipInfo":
                message = message.concat(createMembershipInfoMessage(node_id));
                break;
            case "storeKeyValue":
                message = message.concat(createStoreKeyValueMessage(node_id));
                break;
            case "leaveReq":
                message = message.concat(createLeaveReqMessage(node_id));
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

    public static String createMembershipInfoMessage(String node_id){
        String node_key = Crypto.encodeValue(node_id);
        String cluster_members = getRawClusterMembers(node_key);
        String logs = getRawLogs(node_key);
        String message = "memshipInfo_"+cluster_members+"_"+logs;
        System.err.println("memshipInfo: " + message);
        return message;
    }

    public static String createStoreKeyValueMessage(String node_id){
        String node_key = Crypto.encodeValue(node_id);
        String cluster_members = getRawKeyValues(node_key);
        String logs = getRawLogs(node_key);
        String message = "memshipInfo_"+cluster_members+"_"+logs;
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
        String raw_logs = FileHandler.readFile("../global/"+node_key+"/membership/", "counter.txt");
        return raw_logs;
    }

    public static String getRawKeyValues(String node_key){
        String key_values = "";
        LinkedList<String> files = FileHandler.getDirectoryFiles("../global"+ node_key + "/", "storage");
        int i=0;
        for(String file : files){
            String value = FileHandler.readFile("../global/"+node_key+"/storage/", file+".txt");
            if(i != 0) key_values = key_values.concat("-");
            key_values = key_values.concat(file+"+"+value);
            i++;
        }
        return key_values;
    }
}
