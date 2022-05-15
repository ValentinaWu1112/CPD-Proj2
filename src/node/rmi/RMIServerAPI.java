package node.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServerAPI extends Remote{
    boolean joinMulticastGroup() throws RemoteException;
    boolean leaveMulticastGroup() throws RemoteException;
    boolean getValue() throws RemoteException;
    boolean putValue() throws RemoteException;
    boolean deleteValue() throws RemoteException;
}
