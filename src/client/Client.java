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
                    String path_file = args[2];
                    String file_content = FileHandler.readFile(path_file, "");
                    if(file_content != null){
                        String file_key = Crypto.encodeValue(file_content);
                        verdict = stubRMIServerAPI.putValue(file_key, file_content);
                        System.out.println("put: " + verdict);
                    }
                    break;
            }
            //response = stubRMIServerAPI.leaveMulticastGroup();
            //System.out.println("leavemc - reponse should be false and it is: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
