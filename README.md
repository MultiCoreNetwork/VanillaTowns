# VanillaTowns

#### A very simple and minimalistic Spigot plugin to create towns in vanilla and survival servers.

## Description

With this plugin your players can create their towns. Towns can have a common home, a shared bank and a private chat.
Each town has a leader and can have one or more admins.

The members of the towns have different permissions depending on their role in the town or depending on the leader
choices.

This plugin is highly configurable by changing its config file. PlaceholdersAPI and MVdWPlaceholderAPI are supported to
give you the ability to integrate it in other plugins.

### Permissions

* `vanillatowns.player` Gives the ability to use all player town commands (Give this to the player for a regular use).
    * `vanillatowns.town` Gives access only to /town command.
    * `vanillatowns.broadcast` Gives the ability to receive town creation/deletion broadcasts.
    * `vanillatowns.info` Gives the ability to see your town info.
    * `vanillatowns.info.other` Gives the ability to see other players' town info.
    * `vanillatowns.baltop` Gives the ability to see the 10 richest towns.
    * `vanillatowns.create` Gives the ability to create/delete a town.
    * `vanillatowns.rename` Gives the ability to rename a town.
    * `vanillatowns.invite` Gives the ability to invite a player into the town (Must be the leader or an admin).
    * `vanillatowns.join` Gives the ability to join/leave a town.
    * `vanillatowns.kick` Gives the ability to kick a player from the town (Must be the leader or an admin).
    * `vanillatowns.give` Gives the ability to give the town leader to another member (Must be the leader).
    * `vanillatowns.home` Gives the ability to teleport to the town home.
    * `vanillatowns.home.edit` Gives the ability to set/delete the town home (Must be the leader or an admin).
    * `vanillatowns.balance` Gives the ability to see the balance of the town.
    * `vanillatowns.deposit` Gives the ability to deposit money to the town balance (Must be the leader or an admin or
      have the permission).
    * `vanillatowns.withdraw` Gives the ability to withdraw money from the town balance (Must be the leader or an admin
      or have the permission).
    * `vanillatowns.chat` Gives the ability to use the town chat.
* `vanillatowns.chat` Gives the ability to send messages in the town chat.
* `vanillatowns.staff` Gives the ability to manage the towns (This permission is meant for the staff, it
  inherits `vanillatowns.player` permissions).
    * `vanillatowns.staff.info` Gives the ability to see the town staff info.
    * `vanillatowns.staff.reload` Gives the ability to reload the plugin.
    * `vanillatowns.staff.invite` Gives the ability to invite a player to a town.
    * `vanillatowns.staff.join` Gives the ability to add a player to a town.
    * `vanillatowns.staff.kick` Gives the ability to kick a player from a town.
    * `vanillatowns.staff.rename` Gives the ability to rename a town.
    * `vanillatowns.staff.delete` Gives the ability to delete a town.
    * `vanillatowns.staff.roles` Gives the ability to edit roles in a town.
    * `vanillatowns.staff.home` Gives the ability to teleport to a town home.
    * `vanillatowns.staff.home.edit` Gives the ability to set/delete a town home.
    * `vanillatowns.staff.instanttp` Gives the ability to ignore teleport timer.

### Placeholders

This plugin support both PlaceholderAPI and MVdWPlaceholderAPI

#### PlaceholderAPI

* `vanillatowns_town_name`
* `vanillatowns_town_balance`
* `vanillatowns_town_role`
* `vanillatowns_role_color`
* `vanillatowns_town_home_w`
* `vanillatowns_town_home_x`
* `vanillatowns_town_home_y`
* `vanillatowns_town_home_z`
* `vanillatowns_town_rank`

#### MVdWPlaceholderAPI

* `{vt_town_name}`
* `{vt_town_balance}`
* `{vt_town_role}`
* `{vt_role_color}`
* `{vt_town_home_w}`
* `{vt_town_home_x}`
* `{vt_town_home_y}`
* `{vt_town_home_z}`
* `{vt_town_rank}`

### Configuration

The configuration file is saved as config.json during the first initialization of the plugin. You can edit it according
your needs, if an entry is missing it will be recreated automatically. The config is quite self-explanatory: the main
section contains all the settings, the colors section contains the colors of the ranks and the string section contains
all the strings and the messages of the plugin.

**One important thing:** like all my plugins, this plugin uses [MBCore](https://github.com/MultiCoreNetwork/MBCore) as a
library. With MBCore you can do some cool things in your config like using all hexadecimal colors (only Minecraft 1.16+)
in strings simply by replacing the adding`#ab01cd` instead of `&c` colors. You can use both at the same time. You can
also use json formatted messages (the ones used in tellraw command) by simply putting `\j` at the start of the message 
(You can find an example of this in the default config).

## Download

You can download this plugin [here](https://multicoredev.it/job/BungeeCompact/).

This plugin requires Vault to be installed. You can find Vault [here](https://www.spigotmc.org/resources/vault.34315).

## Contributing

To contribute to this repository just fork this repository make your changes or add your code and make a pull request.
If you find an error or a bug you can open an issue [here](https://github.com/MultiCoreNetwork/VanillaTowns/issues).

## License

VanillaTowns is released under "The 3-Clause BSD License". You can find a
copy [here](https://github.com/MultiCoreNetwork/VanillaTowns/blob/master/LICENSE).