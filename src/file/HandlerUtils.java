package file;
import java.util.*;
import java.util.LinkedList;
import crypto.Crypto;

public final class HandlerUtils {
    private HandlerUtils(){}

    public static String updateCounter(String counter){
        int ret = Integer.parseInt(counter);
        ret++;
        return Integer.toString(ret);
    }

    public static String updateLog(String log, String newLog){
        /*node_id counter*/
        Map <String,String> logs = toMap(log);
        Map <String,String> newLogs = toMap(newLog);

        for (Map.Entry<String,String> entry : newLogs.entrySet()) {
            //System.out.println("Key: " + entry.getKey() + ". Value: " + entry.getValue());
            if(logs.containsKey(entry.getKey())){
                if(Integer.parseInt(entry.getValue()) > Integer.parseInt(logs.get(entry.getKey()))){
                    logs.remove(entry.getKey());
                    logs.put(entry.getKey(),entry.getValue());
                }
            }
            else logs.put(entry.getKey(),entry.getValue());
        }
        return MaptoString(logs);
    }

    public static Map<String,String> toMap (String log){
        Map<String,String> ret = new LinkedHashMap<>();
        String[] logs = log.split("\n");
        for(String l: logs){
            String[] tmp = l.split(" ");
            ret.put(tmp[0],tmp[1]);
        }
        return ret;
    }

    public static String MaptoString(Map <String,String> logs){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> entry : logs.entrySet()) {
            sb.append(entry.getKey() + " " + entry.getValue() + "\n");
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
        String message = "joinReq_"+node_counter;
        return message;
    }

    public static String getRawClusterMembers(String node_key){
        String raw_cluster_members = FileHandler.readFile("../global/"+node_key+"/membership/", "cluster_members.txt");
        return raw_cluster_members;
    }

    public static LinkedList<String> loadClusterMembers(String node_key){
        String raw_cluster_members = FileHandler.readFile("..global/"+node_key+"/membership/", "cluster_members.txt");
        String[] cluster_members_array = raw_cluster_members.split("-");
        LinkedList<String> cluster_members = new LinkedList<>();
        for(int i=0; i<cluster_members_array.length; i++){
            cluster_members.add(cluster_members_array[i]);
        }
        return cluster_members;
    }

    public static String getRawLogs(String node_key){
        String raw_logs = FileHandler.readFile("../global/"+node_key+"/membership/", "log.txt");
        return raw_logs;
    }

    public static String getRawKeyValues(String node_key){
        String key_values = "";
        LinkedList<String> files = FileHandler.getDirectoryFiles("../global"+ node_key + "/", "storage");
        for(String file : files){
            String value = FileHandler.readFile("../global/"+node_key+"/storage/", file+".txt");
            key_values = key_values.concat(file+"+"+value);
        }
        return key_values;
    }
}
