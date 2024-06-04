<!-- Don't forget to update the page on Minecraft Wiki! -->

# ![](https://github.com/mc-wiki/minecraft-mod-heywiki/blob/master/fabric/src/main/resources/icon.png?raw=true) <br> Hey Wiki

[![CurseForge](https://img.shields.io/curseforge/dt/997027?label=CurseForge&color=orange&logoColor=orange&labelColor=black&logo=curseforge)](https://curseforge.com/minecraft/mc-mods/hey-wiki)
[![Modrinth](https://img.shields.io/modrinth/dt/6DnswkCZ?label=Modrinth&color=darkgreen&labelColor=black&logo=modrinth)](https://modrinth.com/mod/hey-wiki)
[![Crowdin](https://badges.crowdin.net/hey-wiki/localized.svg)](https://crowdin.com/project/hey-wiki)

[<img alt="fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg">](https://fabricmc.net/)
[<img alt="neoforge" height="56" src="https://github.com/mc-wiki/minecraft-mod-heywiki/blob/master/docs/supports_neoforge.svg?raw=true">](https://neoforged.net/)
[<img alt="crowdin" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/translate/crowdin_vector.svg">](https://crowdin.com/project/hey-wiki)

Hey Wiki is a client mod made by [the Minecraft Wiki](https://minecraft.wiki) that allows you to press H (customizable)
to open the wiki page of the block/item/entity you're aiming at, no matter it's from vanilla or a mod.
Hey Wiki supports Fabric and NeoForge.

## Features

- Press H (customizable) to open the wiki page (Minecraft Wiki or the respective mod wiki) of the block/item/entity
  you're aiming at.
- Press Alt+H or Opt+H to open the wiki page of the item in your main hand.
- Adds several [commands](#commands).
- [Link to a wiki page in chat](#wiki-links-in-chat) using `[[wiki link]]` syntax.

~~I didn't know that this mod is kinda like *Lexica Botania* but it is.~~

### How to use

1. Install the mod. You can download it from [CurseForge](https://curseforge.com/minecraft/mc-mods/hey-wiki)
   or [Modrinth](https://modrinth.com/mod/hey-wiki). Don't forget to install the [dependencies](#dependencies).
2. Point at a block/entity with your crosshair or hover over an item in your inventory with your pointer.
3. Press the keybind (default: H).
4. Either confirm the action or copy the link to your clipboard in the screen that pops up.

### Configuration

Optionally, you can change these behaviors in the config menu:

> [!TIP]
> You need to install [Mod Menu](https://modrinth.com/mod/modmenu) to access the config menu on Fabric.

- If confirmation is required to open the wiki page: default is true
- Which wiki language you prefer, overriding your game language: default is your game language
- The keybind to open the wiki page: default is H
- Reach distance: default is 5.2 blocks (same as creative mode reach)
- Whether fluid is allowed: default is false

### Commands

These commands are available:

- `/imfeelinglucky [<namespace>]` - Takes you wherever the mod decides.
    - Example: `/imfeelinglucky minecraft` -> `https://minecraft.wiki/????????`
- `/wiki <pageName>` - Opens the Minecraft Wiki page of the specified page name. `pageName` can include a namespace.
    - Example: `/wiki minecraft:creeper` -> `https://minecraft.wiki/w/?search=creeper`
        - Redirect: `/whatis`
- `/whatbiome` - Opens the Minecraft Wiki page of the biome you're currently in.
- `/whatcommand <command>` - Opens the Minecraft Wiki page of the specified command.
    - Example: `/whatcommand give` -> `https://minecraft.wiki/w/?search=%2Fgive`
    - Redirect: `/whatcmd`
- `/whatisthis` - Opens the Minecraft Wiki page of the block/item/entity you're aiming at.
- `/whatisthisitem` - Opens the Minecraft Wiki page of the item you're holding in you main hand.
    - `whatisthisitem offhand` - Same, but for the offhand.

Note that if you open a page with a command, the confirmation screen will not appear.

### `[[Wiki links]]` in chat

Hey Wiki supports `[[wiki links]]` in chat. When you send a message with `[[wiki link]]` syntax, Hey Wiki will replace
it with a clickable link. The page name can include a namespace.

This also supports interwiki links. For example:

- `[[aether:lore]]` will link to the Aether Wiki page of "lore" (interwiki)
- `[[en:enderman]]` will link to the English Minecraft Wiki page of "enderman" (interlanguage)
- `[[minecraft:en:enderman]]` will link to the English Minecraft Wiki page of "enderman" (interwiki and interlanguage)

## Supported wikis

Currently, Hey Wiki supports the following wikis:

- [Minecraft Wiki](https://minecraft.wiki)
- [Aether Wiki](https://aether.wiki.gg)

If you want to add support for other wikis, you can
[file an issue](https://github.com/mc-wiki/minecraft-mod-heywiki/issues/new?labels=new+wiki%2Ctriage+needed&template=new_wiki.yml).
In addition, you can also add support for other wikis by using a resource pack.

### Resource pack

> [!WARNING]  
> Please note that the JSON schema is not stable and is not semantically versioned yet.
> We very well might break it in minor versions.

Hey Wiki supports using resource pack to add support for other wikis. To do so, create a JSON file in the
`assets/<namespace>/wiki_family/` folder in your resource pack with the following format:

```json
{
  // A unique identifier for the wiki family
  "id": "minecraft",
  // The namespaces that is bound to the wiki family
  "namespace": [
    "minecraft"
  ],
  // List of different language wikis
  "wikis": [
    {
      // The URL pattern for articles. %s will be replaced with the query
      "article_url": "https://minecraft.wiki/?search=%s",
      // (Optional) If the wiki is a MediaWiki wiki, the API URL
      "mw_api_url": "https://ja.minecraft.wiki/api.php",
      // (Optional) The page name for the random article
      "random_article": "Special:RandomRootPage/Main",
      // (Optional) How Hey Wiki should fetch excerpts from the wiki. Either "text_extracts" or "none".
      // "text_extracts" only works for MediaWiki sites with the TextExtracts extension installed. You can check this by visiting Special:Version on the wiki.
      "excerpt": "text_extracts",
      "language": {
        // The language code of the wiki
        "wiki_language": "en",
        // Whether this is the main language. If true, this language will be fallback if no other language matches
        "main": true,
        // The default in-game language that the wiki supports. This will be fallback if the in-game language is not supported by the wiki
        "default": "en_us",
        // If the regex matches the in-game language, this wiki will be used when config is auto
        "regex": "^en_.*",
        // (Optional) Ditto, but for exclusion
        "exclude": null,
        // (Optional) A custom language code that allows you to override translation keys to specific pages. A translation file should exist at assets/<namespace>/lang/<lang_override>.json
        "lang_override": "minecraft_en"
      }
    },
    {
      // Another language
      "article_url": "https://de.minecraft.wiki/?search=%s",
      "language": {
        "wiki_language": "de",
        "default": "de_de",
        "regex": "^de_.*",
        "exclude": null
      }
    }
  ]
}
```

## Dependencies

For Fabric:

- (Required) [Fabric API](https://modrinth.com/mod/fabric-api): provides basic modding utilities
- (Required) [Architectury API](https://modrinth.com/mod/architectury-api): for multi-modloader compatibility
- (Required) [Cloth Config](https://modrinth.com/mod/cloth-config): for configuration
- (Recommended) [Mod Menu](https://modrinth.com/mod/modmenu): to access the config menu
- (Optional) [Roughly Enough Items (REI)](https://modrinth.com/mod/roughly-enough-items): for REI integration
- (Optional) [EMI](https://modrinth.com/mod/emi): for EMI integration

For NeoForge:

- (Required) [Architectury API](https://modrinth.com/mod/architectury-api): for multi-modloader compatibility
- (Required) [Cloth Config](https://modrinth.com/mod/cloth-config): for configuration
- (Optional) [Roughly Enough Items (REI)](https://modrinth.com/mod/roughly-enough-items): for REI integration
- (Optional) [EMI](https://modrinth.com/mod/emi): for EMI integration

## Version support

Hey Wiki supports multiple versions of Minecraft.

The current release and the master branches receive all new features and bug fixes. Pull requests should almost always
go to `master`. If they're accepted, they should be cherry-picked to the current stable release branch.

When a new snapshot releases, master branch is updated to that snapshot. Snapshots might receive only one version or no
version at all. Only Fabric is supported for snapshots.

Some select old MC versions receive LTS – they may or may not receive new bug fixes and new features, but they will
receive critical bug fixes. Old snapshots are not supported.

The following table shows which versions are supported:

| Git branch  | Minecraft version    | Supported?            | Is LTS? | Modloader        |
|-------------|----------------------|-----------------------|---------|------------------|
| `master`    | 1.21                 | Yes (latest snapshot) | TBD     | Fabric           |
| `mc/1.20.6` | 1.20.6               | Yes (latest release)  | TBD     | Fabric, Neoforge |
| `mc/1.20.5` | 1.20.5               | No                    | No      | Fabric, Neoforge |
| `mc/1.20.4` | 1.20.4               | Yes (LTS)             | Yes     | Fabric, Neoforge |
| N/A         | *Outdated snapshots* | EOL                   | No      | Fabric           |
