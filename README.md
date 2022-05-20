# Distributed and Partitioned Key-Value Store

### TODO
- [ ] Membership Protocol (Theory)
- [x] Node Multicast communication
- [x] Node TCP communication
- [ ] Node filesystem structure
- [x] Node file I/O
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
        java node/ClusterNode 224.0.0.1 6666 127.0.0.1 7999

    Interact with nodes: 
        java client/Client 127.0.0.1 join

### How install the package org.json
    - https://www.tutorialspoint.com/org_json/org_json_quick_guide.htm
    - (no link cuidado com os espaços, no JAVA_HOME utilizar o comando de baixo)
    - export JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:/bin/java::")
    - guardar todos os exports no ficheiro "~/.bashrc", para não correr sempre de novo
    - se estão a utilizar o vscode podem exportar a biblioteca para o ide :
       https://youtu.be/g6vvEEm2hhs

#### Crypto class usage:
    - Crypto.encodeValue("value");

#### FileHandler class usage:
    - FileHandler.createFile("../global/", "file1");
    - FileHandler.createDirectory("../global/", "filesnode1");
    - FileHandler.writeFile("../global/", "file1", "content");