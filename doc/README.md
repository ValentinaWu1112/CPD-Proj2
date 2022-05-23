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

- joinReq_nodeId_counter (via multicast)

(some of the cluster member will send the new member a MemberShip message) (via TCP)
- joinMember_listMember
- joinLogs_logs

(send storage values after a join) (via  ?)
- joinValue_value


- LEAVE:

(before leaving the cluster, i.e. multicasting the LEAVE message, the node should transfer its key-value pairs to its successor.)
- leaveValue_key_value (via ?) -> (estou confusa com o key e value)

- leaveReq_nodeId_counter (via ?)


- PUT:
- putReq_key_value (via ?)
