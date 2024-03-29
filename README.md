# Distributed and Partitioned Key-Value Store

### TODO
- [ ] Membership Protocol (Theory)
- [x] Node Multicast communication
- [x] Node TCP communication
- [x] Node filesystem structure
- [x] Node file I/O
- [x] RMI Server implementation
- [x] Node RMI task JOIN
- [x] Node RMI task JOIN with Replication
- [x] Node RMI task LEAVE
- [x] Node RMI task LEAVE with Replication
- [x] Node RMI task GET
- [x] Node RMI task GET with Replication
- [x] Node RMI task PUT
- [x] Node RMI task PUT with Replication
- [x] Node RMI task DELETE
- [x] Node RMI task DELETE with Replication
- [x] SHA-256 Encode (ex.: file content -> hash key)

#### How to execute:
    cd src/
    ./compile.sh

    Initialize registry:
        rmiregistry 1090

    Create a node:
        ./startadd 127.0.0.1
        java node/ClusterNode 224.0.0.1 6666 127.0.0.1 7999

    Interact with nodes:
        java client/Client 127.0.0.1 join

#### Crypto class usage:
    - Crypto.encodeValue("value");

#### FileHandler class usage:
    - FileHandler.createFile("../global/", "file1");
    - FileHandler.createDirectory("../global/", "filesnode1");
    - FileHandler.writeFile("../global/", "file1", "content");
