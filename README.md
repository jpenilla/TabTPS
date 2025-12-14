<img src="https://i.imgur.com/gtxPU4S.png" width="128">

# TabTPS
![plugin version badge](https://img.shields.io/github/v/release/jmanpenilla/TabTPS?color=blue&label=version&style=plastic) [![Crowdin](https://badges.crowdin.net/tabtps/localized.svg)](https://crowdin.com/project/tabtps)

Minecraft server mod/plugin to show TPS, MSPT, and other information in the tab menu, boss bar, and action bar.

Current supported platforms:
- [Paper](https://papermc.io) (Minecraft versions 1.8.8-1.21.11+)
- [Sponge](https://spongepowered.org) 12+
- [Fabric](https://fabricmc.net/) (Minecraft 1.21.11, requires [Fabric API](https://modrinth.com/mod/fabric-api))
- [NeoForge](https://neoforged.net/) (Minecraft 1.21.11)

## Features

### Live information displays

- Configure what information will be shown using display configs (`/plugins/TabTPS/display-configs/`)
  - Each display config has a permission associated, and players with that permission will use that display config.
    - The default display config uses the permission `tabtps.defaultdisplay`, and allows for using all three display types.
    - Only one display config can be assigned to each player, even if they have permission for multiple. Set priorities for different display configs in the main config (`plugins/TabTPS/main.conf`)
    
- Configure colors for displays using theme configs (`/plugins/TabTPS/themes/`)

#### Tab menu
* Command: ``/tabtps toggle tab``
* ![tab menu](https://i.imgur.com/93NmuUA.png)

#### Action bar
* Command: ``/tabtps toggle actionbar``
* ![action bar](https://i.imgur.com/aMzzNRR.png)

#### Boss bar
 * Command: ``/tabtps toggle bossbar``
 * ![boss bar](https://i.postimg.cc/xCJnGYfb/bossbar.png)

### Commands

#### Improved TPS command
* Command: ``/tickinfo`` or ``/mspt``
* Permission required: ``tabtps.tps``
* ![tps command](https://i.imgur.com/d87Z80z.png)

#### Memory command
* Command: ``/memory``, `/mem`, or ``/ram``
* View information about the current memory pools of the server jvm.
  * Note: the output and usefulness of this command varies depending on the type of garbage collection used, garbage collection settings, and many other factors.
* Permission required: ``tabtps.tps``
* ![tps command](https://i.imgur.com/eYeUNMc.png)

#### Ping command
* Commands: ``/ping``, `/ping [username]`, or ``/pingall``
* View the ping of yourself, or another user. ``/pingall`` will show a summary of all connected player's pings.
* Permissions: ``tabtps.ping`` to view your own ping, ``tabtps.ping.others`` to view other users ping and the ping summary.
* ![ping command](https://i.imgur.com/0agY7lB.png)
* ![ping all](https://i.imgur.com/t1lBt2b.png)

#### Reload command
* Command: ``/tabtps reload``
* Permission required: ``tabtps.reload``
