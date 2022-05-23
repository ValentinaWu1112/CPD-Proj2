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

    public String createMessage(String node_id, String operation){
        String message = "header:"+node_id+" body:";
        switch(operation){
            case "joinreq":
                message = message.concat(createJoinReqMessage(node_id));
                break;
        }
        return message;
    }

    public String createJoinReqMessage(String node_id){
        String node_key = Crypto.encodeValue(node_id);
        String node_counter = FileHandler.readFile("../global/"+node_key+"/membership/", "counter.txt");
        String message = "joinreq_"+node_counter;
        return message;
    }

    /* public LinkedList<String> loadClusterMembers(String node_key){
        String raw_cluster_members = FileHandler.readFile("..global/"+node_key+"/membership/", "cluster_members.txt");
        String[] cluster_members = raw_cluster_members.split("-");
        return null;
    } */
}
