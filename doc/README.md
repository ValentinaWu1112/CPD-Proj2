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