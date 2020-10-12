<img src="https://i.imgur.com/gtxPU4S.png" width="128">

# TabTPS ![plugin version badge](https://img.shields.io/github/v/release/jmanpenilla/TabTPS?color=blue&label=plugin%20version&style=plastic)
Spigot/Paper Plugin to show TPS and MSPT in the Tab menu. Supports Minecraft versions 1.8.8-1.16+

## Features

### Show TPS and MSPT in Tab menu
* Toggle on/off with ``/tabtps toggle tab``
* Permission required: ``tabtps.toggle`` and ``tabtps.toggle.tab``
* The plugin will remember which players have the TPS display enabled through restarts and log in/out.
* ![tab menu](https://i.imgur.com/93NmuUA.png)

### Show TPS and MSPT in Action Bar
* Command: ``/tabtps toggle actionbar``
* Permission required: ``tabtps.toggle`` and ``tabtps.toggle.actionbar``
* ![action bar](https://i.imgur.com/aMzzNRR.png)

### Improved TPS command
* Command: ``/tickinfo`` or ``/mspt``
* Permission required: ``tabtps.tps``
* ![tps command](https://i.imgur.com/hTK4Asd.png)

### Memory command
* Command: ``/memory``, `/mem`, or ``/ram``
* View information about the current memory pools of the server jvm.
  * Note: the output and usefulness of this command varies depending on the type of garbage collection used, garbage collection settings, and many other factors.
* Permission required: ``tabtps.tps``
* ![tps command](https://i.imgur.com/M9nb01Z.png)

### Ping command
* Commands: ``/ping``, `/ping [username]`, or ``/pingall``
* View the ping of yourself, or another user. ``/pingall`` will show a summary of all connected player's pings.
* Permissions: ``tabtps.ping`` to view your own ping, ``tabtps.ping.others`` to view other users ping and the ping summary.
* ![ping command](https://i.imgur.com/0agY7lB.png)
* ![ping all](https://i.imgur.com/t1lBt2b.png)

### Reload command
* Command: ``/tabtps reload``
* Permission required: ``tabtps.reload``
