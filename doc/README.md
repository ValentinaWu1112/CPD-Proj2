# Project design decisions documentation (Report Helper)

### Membership Protocol
#### Cluster Members Format
- The cluster members list will follow the specified format show bellow: <br/>
    ``` node1ip-node2ip-...```<br/>
Essentially, all node TCP addresses are delimited by the '-' character.

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

    - Since demo is executed in a single computer, global directory contains
        all information kept by every node following the above structure.