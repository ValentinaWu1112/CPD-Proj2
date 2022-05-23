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

#### JOIN:

(Before sending the JOIN message, the new member starts accepting connections on a IP whose number it sends in its JOIN message)

- joinReq_nodeId_counter (via multicast)

(some of the cluster member will send the new member a MemberShip message) (via TCP)
- joinMember_listMember
- joinLogs_logs

(send storage values after a join) (via  ?)
- joinValue_value


#### LEAVE:

(before leaving the cluster, i.e. multicasting the LEAVE message, the node should transfer its key-value pairs to its successor.)
- leaveValue_key_value (via ?) -> (estou confusa com o key e value)

- leaveReq_nodeId_counter (via ?)


- PUT:
- putReq_key_value (via ?)
