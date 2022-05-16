package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import node.rmi.RMIServerAPI;



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
            }
            //response = stubRMIServerAPI.leaveMulticastGroup();
            //System.out.println("leavemc - reponse should be false and it is: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}