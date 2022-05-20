Messages: (multicast)
- membership counter (how represent?)
    - 0 | increment by leave or join cluster
    - par - join | impar - leave  

- membership log (how represent?)
    - each record on this logs includes the node id and the value of the membership counter (?)
    - The membership log does not need to keep more than one event per node: that node's event with the largest membership counter

Join: 
- some of the cluster member will send the new member a MemberShip message: (with TCP)
    - list of the current cluster member 
    - the most recent 32 membership events in its logs

- Before sending the JOIN message, the new member starts accepting connections on a IP whose number it sends in its JOIN message (?)

- the successor of the joining node should transfer to the latter the keys that are smaller or equal to the id of the joining node;

Leave:
- before leaving the cluster, i.e. multicasting the LEAVE message, the node should transfer its key-value pairs to its successor.

</br>
</br>

- these MEMBERSHIP messages should be sent by nodes whose membership information is up-to-date.
- diff multicast vs TCP
- why needs messages