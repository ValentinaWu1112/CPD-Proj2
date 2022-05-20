package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import node.rmi.RMIServerAPI;
import crypto.Crypto;
import file.FileHandler;
import file.JSONHandler;

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
            JSONHandler.jsonTest();
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
                    String value = args[2];
                    String hash_value = Crypto.encodeValue(value);
                    FileHandler.createFile("../global/", "file1");
                    FileHandler.createDirectory("../global/", "filesnode1");
                    FileHandler.writeFile("../global/", "file1", "olaxd");
                    System.out.print(FileHandler.readFile("../global/", "file1"));
                    System.out.println("value: " + value + "\nhashed: " + hash_value);
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
