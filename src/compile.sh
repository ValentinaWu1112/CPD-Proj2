#!/bin/bash

javac -cp ".:../libs/json-20220320.jar" client/Client.java node/multicast/NodeMulticastServer.java node/multicast/NodeMulticastClient.java node/rmi/RMIServerAPI.java node/tcp/NodeTCPServer.java node/tcp/NodeTCPClient.java node/ClusterNode.java node/rmi/RMIServer.java crypto/Crypto.java file/FileHandler.java file/JSONHandler.java