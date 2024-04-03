# ![](./fabric/src/main/resources/icon.png)<br>Hey Wiki

[![CurseForge](https://img.shields.io/curseforge/dt/997027?label=CurseForge&color=orange&logoColor=orange&labelColor=black&logo=curseforge)](https://curseforge.com/minecraft/mc-mods/hey-wiki)
[![Modrinth](https://img.shields.io/modrinth/dt/6DnswkCZ?label=Modrinth&color=darkgreen&labelColor=black&logo=modrinth)](https://modrinth.com/mod/hey-wiki)

Hey Wiki is an client mod made by [the Minecraft Wiki](https://minecraft.wiki) that allows you to press H (customizable)
to open the Minecraft Wiki page of the block/item/entity you're aiming at. Hey Wiki supports Fabric and NeoForge.

## Features

- Press H (customizable) to open the Minecraft Wiki page of the block/item/entity you're aiming at.
- Adds several [commands](#commands).

### How to use

1. Install the mod. You can download it from [CurseForge](https://curseforge.com/minecraft/mc-mods/hey-wiki)
   or [Modrinth](https://modrinth.com/mod/hey-wiki). Don't forget to install the [dependencies](#dependencies).
2. Point at a block/entity with your crosshair or hover over an item in your inventory with your pointer.
3. Press the keybind (default: H).
4. Either confirm the action or copy the link to your clipboard in the screen that pops up.

Optionally, you can change these behaviors in the config menu:

> [!TIP]
> You need to install [Mod Menu](https://modrinth.com/mod/modmenu) to access the config menu on Fabric.

- If confirmation is required to open the wiki page: default is true
- Which wiki language you prefer, overriding your game language: default is your game language
- The keybind to open the wiki page: default is H

### Commands

These commands are available:

- `/wiki <pageName>` - Opens the Minecraft Wiki page of the specified page name.
    - Example: `/wiki creeper` -> `https://minecraft.wiki/w/?search=creeper`
    - Redirect: `/whatis`
- `/whatbiome` - Opens the Minecraft Wiki page of the biome you're currently in.
- `/whatcommand <command>` - Opens the Minecraft Wiki page of the specified command.
    - Example: `/whatcommand give` -> `https://minecraft.wiki/w/?search=%2Fgive`
    - Redirect: `/whatcmd`
- `/whatisthis` - Opens the Minecraft Wiki page of the block/item/entity you're aiming at.
- `/whatisthisitem` - Opens the Minecraft Wiki page of the item you're holding in you main hand.

Note that if you open a page with a command, the confirmation screen will not appear.

## Dependencies

For Fabric:

- (Required) [Fabric API](https://modrinth.com/mod/fabric-api): provides basic modding utilities
- (Required) [Architectury API](https://modrinth.com/mod/architectury-api): for multi-modloader compatibility
- (Required) [Cloth Config](https://modrinth.com/mod/cloth-config): for configuration
- (Recommended) [Mod Menu](https://modrinth.com/mod/modmenu): to access the config menu

For NeoForge:

- (Required) [Architectury API](https://modrinth.com/mod/architectury-api): for multi-modloader compatibility
- (Required) [Cloth Config](https://modrinth.com/mod/cloth-config): for configuration
