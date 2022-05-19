package node.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/* 
    RMI Server API Interface
*/

public interface RMIServerAPI extends Remote{
    boolean joinMulticastGroup() throws RemoteException;
    boolean leaveMulticastGroup() throws RemoteException;
    boolean getValue(String key) throws RemoteException;
    boolean putValue(String key, String value) throws RemoteException;
    boolean deleteValue(String key) throws RemoteException;
}
