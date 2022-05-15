#!/bin/bash

javac client/Client.java node/multicast/NodeMulticastServer.java node/multicast/NodeMulticastClient.java node/rmi/RMIServerAPI.java node/tcp/NodeTCPServer.java node/tcp/NodeTCPClient.java node/ClusterNode.java node/RMIServer.java

#(make sure to 'chmod 700 compile' previously)
