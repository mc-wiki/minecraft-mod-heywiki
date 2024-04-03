# ![](./fabric/src/main/resources/icon.png)<br>Hey Wiki

[![CurseForge](https://img.shields.io/curseforge/dt/997027?label=CurseForge&color=orange&logoColor=orange&labelColor=black&logo=curseforge)](https://curseforge.com/minecraft/mc-mods/hey-wiki)
[![Modrinth](https://img.shields.io/modrinth/dt/6DnswkCZ?label=Modrinth&color=darkgreen&labelColor=black&logo=modrinth)](https://modrinth.com/mod/hey-wiki)

Hey Wiki is an official Fabric/NeoForge Minecraft mod by [the Minecraft Wiki](https://minecraft.wiki)
that allows you to press H (customizable) to open the Minecraft Wiki page of the block/item/entity you're aiming at.

## Features

- Press H (customizable) to open the Minecraft Wiki page of the block/item/entity you're aiming at.
- Adds several commands.

### Commands

- `/wiki <pageName>` - Opens the Minecraft Wiki page of the specified page name.
    - Example: `/wiki creeper` -> `https://minecraft.wiki/w/?search=creeper`
        - Redirect: `/whatis`
- `/whatbiome` - Opens the Minecraft Wiki page of the biome you're currently in.
- `/whatcommand <command>` - Opens the Minecraft Wiki page of the specified command.
    - Example: `/whatcommand give` -> `https://minecraft.wiki/w/?search=%2Fgive`
    - Redirect: `/whatcmd`
- `/whatisthis` - Opens the Minecraft Wiki page of the block/item/entity you're aiming at.
- `/whatisthisitem` - Opens the Minecraft Wiki page of the item you're holding in you main hand.

## Dependencies

For Fabric:

- (Required) [Fabric API](https://modrinth.com/mod/fabric-api)
- (Required) [Architectury API](https://modrinth.com/mod/architectury-api): for multi-modloader compatibility
- (Required) [Cloth Config](https://modrinth.com/mod/cloth-config): for configuration
- (Recommended) [Mod Menu](https://modrinth.com/mod/mod-menu): to access the config menu

For NeoForge:

- (Required) [Architectury API](https://modrinth.com/mod/architectury-api): for multi-modloader compatibility
- (Required) [Cloth Config](https://modrinth.com/mod/cloth-config): for configuration
