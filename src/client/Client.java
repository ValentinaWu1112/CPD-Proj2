package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import node.rmi.RMIServerAPI;

public class Client {
    private static String tcp_ip;

    private Client() {}

    public static void main(String[] args) {
        try {
            tcp_ip = args[0];
            Registry registryRMIServerAPI = LocateRegistry.getRegistry(1090);
            RMIServerAPI stubRMIServerAPI = (RMIServerAPI) registryRMIServerAPI.lookup(tcp_ip+"RMIAPI");
            boolean response = stubRMIServerAPI.joinMulticastGroup();
            System.out.println("joinmc - reponse should be true and it is: " + response);
            //response = stubRMIServerAPI.leaveMulticastGroup();
            //System.out.println("leavemc - reponse should be false and it is: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
