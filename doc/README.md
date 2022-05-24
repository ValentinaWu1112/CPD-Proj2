# Project design decisions documentation (Report Helper)

### File system structure

```
global/
    node1_key/
        membership/
            counter.txt
            log.txt
        storage/
            file79_key.txt
    node2_key/
        membership/
            counter.txt
            log.txt
        storage/
            file32_key.txt
```

Since demo is executed in a single computer, global directory contains all information kept by every node following the above structure.


### Messages structure: 
All messages will be encapsulated inside an universal wrap, used accross all kinds of messages:<br/>

``` header:nodeId body:joinMessage ```<br/>

Here, the header contains, separated by the ':' character, the issuer of the message (the Id of the node that is sending the message), then, separated by a space character '\0', is the body. Identically shaped compared to the header, the body contains the actual message content. This message content varies depending on the message type. These types are explained bellow..

### Message Types:

#### JoinReq Message (the message the node broadcasts when it joins) [UDP]
- joinReq_counter

#### Membership Information Message (the message a node sends to the joining node / the message one cluster node broadcasts every second) [TCP & UDP]
- memshipInfo_nodeid1-nodeid2_nodeid counter\nnodeid2 counter

#### LeaveReq Message (the message the node broadcasts when it leaves) [UDP]
- leaveReq_counter

#### StoreKeyValue Message (the message a node sends to other node containing key-values) [TCP]
- storeKeyValue_key1+value1-key2+value2
