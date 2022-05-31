# Project design decisions documentation (Report Helper)

## File system structure

```
global/
    node1_key/
        membership/
            cluster_members.txt
            log.txt
            counter.txt
        storage/
            file79_key.txt
            ..
    node2_key/
        membership/
            cluster_members.txt
            log.txt
            counter.txt
        storage/
            file32_key.txt
            ..
```

Since demo is executed in a single computer, global directory contains all information kept by every node following the above structure.

## Membership Service

### Messages structure: 
All messages will be encapsulated inside an universal wrap, used across all kinds of messages:<br/>

``` header:nodeId#body:joinMessage ```

Here, the header contains, separated by the ':' character, the issuer of the message (the Id of the node that is sending the message), then, separated by a hashtag character '#', is the body. Identically shaped compared to the header, the body contains the actual message content. This message content varies depending on the message type. These types are explained bellow..


### Message Types:

#### JoinReq Message (the message the node broadcasts when it joins) [UDP]
``` joinReq_counter ```

#### Membership Information Message (the message a node sends to the joining node / the message one cluster node broadcasts every second) [TCP & UDP] 
``` memshipInfoUDP_nodeid1-nodeid2_nodeid1-counter1;nodeid2-counter2; ```<br/>
``` memshipInfoTCP_nodeid1-nodeid2_nodeid1-counter1;nodeid2-counter2; ```<br/>
- Since the node stops processing memshipInfo messages, sent via TCP, after receiving 3 of them, this distinction is necessary. memshipInfoUDP messages are always recevied&processed while memshipInfoTCP might not be processed depending on how many it has received.

#### LeaveReq Message (the message the node broadcasts when it leaves) [UDP]
``` leaveReq_counter ```

#### StoreKeyValue Message (the message a node sends to other node containing key-values) [TCP]
``` storeKeyValue_key1+value1-key2+value2 ```

#### GetValue Message
``` getValue_key ```
```returnGet_value ```

#### DeleteKey Message
``` deleteKey_key ```

### Messages processing

In order to prevent clogged nodes, both nodes TCP and Multicast servers have a queue structure field that will serve as message buffers. Everytime a node receives a message through these endpoints that message is pushed into the queue where it waits to be processed. A MessageScout thread is then responsible for constantly checking up on these queues, removing the messages from the queues and process them. After determining the type of message received, the MessageScout thread then passes the task specified in the message to a ThreadPoolExecutor which executes the passed task. This way, in all conections, the node TCP listening thread (server) is only occupied during the message trip time.

### Cluster Consistency

In order to ensure cluster consistency, i.e, all nodes sharing the same cluster view at a given instant, at a constat rate, say 1 second, one of the nodes will broadcast its own view of the cluster making the others to update their view to what they just received. So, the choice mechanism of this single node has to be done through the.. 