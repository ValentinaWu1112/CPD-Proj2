package node.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/* 
    RMI Server API Interface
*/

public interface RMIServerAPI extends Remote{
    boolean joinMulticastGroup() throws RemoteException;
    boolean leaveMulticastGroup() throws RemoteException;
    String getValue(String key) throws RemoteException;
    boolean putValue(String key, String value) throws RemoteException;
    boolean deleteKey(String key) throws RemoteException;
}
