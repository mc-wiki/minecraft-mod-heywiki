# Hey Wiki

Hey Wiki is a Fabric Minecraft mod that allows you to press H (customizable) to open
the [Minecraft Wiki](https://minecraft.wiki/) page of the block/item/entity you're aiming at.

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

- Fabric API
- YetAnotherConfigLib
- (Optional) Mod Menu
