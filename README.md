# Distributed and Partitioned Key-Value Store

### TODO
- [ ] Membership Protocol (Theory)
- [x] Node Multicast communication
- [x] Node TCP communication
- [ ] Node filesystem structure
- [ ] Node file I/O
- [x] RMI Server implementation
- [ ] Node RMI task JOIN
- [ ] Node RMI task LEAVE
- [ ] Node RMI task GET 
- [ ] Node RMI task PUT
- [ ] Node RMI task DELETE
- [x] SHA-256 Encode (ex.: file content -> hash key)

#### How to execute:
    cd src/
    ./compile.sh

    Initialize registry:
        rmiregistry 1090

    Create a node:
        .startadd 127.0.0.1
        java node/ClusterNode 224.0.0.1 127.0.0.1 7999

    Interact with nodes: 
        java client/Client 127.0.0.1 join
