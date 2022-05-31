package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import node.rmi.RMIServerAPI;
import crypto.Crypto;
import file.FileHandler;

/* 
    Client class responsible for node interaction handling.
*/

public class Client {
    private static String tcp_ip;
    private static String operation;
    private static boolean verdict;

    private Client() {}

    public static void main(String[] args) {
        try {
            tcp_ip = args[0];
            operation = args[1];
            Registry registryRMIServerAPI = LocateRegistry.getRegistry(1090);
            RMIServerAPI stubRMIServerAPI = (RMIServerAPI) registryRMIServerAPI.lookup(tcp_ip+"RMIAPI");
            String path_file = new String();
            String file_content = new String();
            /*
                TODO: Add remaining operations 
            */
            switch(operation){
                case "join":
                    verdict = stubRMIServerAPI.joinMulticastGroup();
                    System.out.println("joinmc: " + verdict);
                    break;
                case "leave":
                    verdict = stubRMIServerAPI.leaveMulticastGroup();
                    System.out.println("leavemc: " + verdict);
                    break;
                case "put":
                    path_file = args[2];
                    file_content = FileHandler.readFile(path_file, "");
                    if(file_content != null){
                        String file_key = Crypto.encodeValue(file_content);
                        verdict = stubRMIServerAPI.putValue(file_key, file_content);
                        System.out.println("put: " + verdict);
                    }
                    break;
                case "get":
                    path_file = args[2];
                    file_content = FileHandler.readFile(path_file, "");
                    if(file_content != null){
                        String file_key = Crypto.encodeValue(file_content);
                        String value = stubRMIServerAPI.getValue(file_key);
                        System.out.println("get: " + value);
                    }
                    break;
                case "delete":
                    path_file = args[2];
                    file_content = FileHandler.readFile(path_file, "");
                    if(file_content != null){
                        String file_key = Crypto.encodeValue(file_content);
                        verdict = stubRMIServerAPI.deleteKey(file_key);
                        System.out.println("delete: " + verdict);
                    }
                    break;
                default:
                    System.out.println("'" + operation + "' operation doesn't exist");
                    break;
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
