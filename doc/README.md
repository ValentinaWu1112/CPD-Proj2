# Project design decisions documentation (Report Helper)

File system structure

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

    - Since demo is executed in a single computer, global directory contains
        all information kept by every node following the above structure.


Messages structure: 

- JOIN:

(Before sending the JOIN message, the new member starts accepting connections on a IP whose number it sends in its JOIN message)
- join_nodeId:hashNode (via ?)

- join_counter:n (via multicast)

(some of the cluster member will send the new member a MemberShip message) (via TCP)
- join_listMember:contentList
- join_logs:contentLogs

(send storage values after a join) (via  ?)
- join_value:contentValue


- LEAVE:

(before leaving the cluster, i.e. multicasting the LEAVE message, the node should transfer its key-value pairs to its successor.)
- leave_key:hash_value:value (via ?) -> (estou confusa com o key e value)

- leave_counter:n


- PUT:
- put_key:key_value:value