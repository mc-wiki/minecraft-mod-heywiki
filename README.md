<!-- Don't forget to update the page on Minecraft Wiki! -->

# ![](https://github.com/mc-wiki/minecraft-mod-heywiki/blob/master/fabric/src/main/resources/icon.png?raw=true) <br> Hey Wiki

[![Modrinth](https://img.shields.io/modrinth/dt/6DnswkCZ?label=Modrinth&color=darkgreen&labelColor=black&logo=modrinth)](https://modrinth.com/mod/hey-wiki)
[![Crowdin](https://badges.crowdin.net/hey-wiki/localized.svg)](https://crowdin.com/project/hey-wiki)

[<img alt="fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg">](https://fabricmc.net/)
[<img alt="neoforge" height="56" src="https://github.com/mc-wiki/minecraft-mod-heywiki/blob/master/docs/supports_neoforge.svg?raw=true">](https://neoforged.net/)
[<img alt="crowdin" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/translate/crowdin_vector.svg">](https://crowdin.com/project/hey-wiki)

Hey Wiki is a client mod made by the [the Minecraft Wiki](https://minecraft.wiki) community. It allows you to quickly open the wiki page of
the block, item, or entity you are aiming at by pressing H (customizable), whether it is from the vanilla game or 
another mod. Hey Wiki supports [Fabric](https://fabricmc.net/) and [NeoForge](https://neoforged.net/).

## Features

- Press H (customizable) to open the wiki page (Minecraft Wiki or the respective mod wiki) of the
  block/item/entity/status effect the crosshair/pointer is over.
- Press Alt+H or Opt+H to open the wiki page of the item in your main hand.
- Press B to open the wiki search screen. The search bar can be used to search for wiki pages.
- Adds several [commands](#commands).
- [Link to a wiki page in chat](#wiki-links-in-chat) using `[[wiki link]]` syntax.

It is also supported to use [MCBrowser](https://modrinth.com/mod/mcbrowser) and [MCEF](https://modrinth.com/mod/mcef) for in-game browsing.

~~I didn't know that this mod is kinda like *Lexica Botania* but it is.~~

https://github.com/mc-wiki/minecraft-mod-heywiki/assets/45287180/b0650362-1fe9-46ff-83a1-48219d5fcc05

### How to use

1. Install the mod. You can download it from [Modrinth](https://modrinth.com/mod/hey-wiki). Don't forget to install the [dependencies](#dependencies).
2. Point at a block/entity with your crosshair or hover over an item in your inventory with your cursor.
3. Press the keybind (default is H).
4. Either confirm the action or copy the link to your clipboard in the screen that pops up.

### Configuration

Optionally, you can change these behaviors in the config menu:

> [!TIP]
> You need to install [Mod Menu](https://modrinth.com/mod/modmenu) to access the config menu on Fabric.

- If confirmation is required to open the wiki page: default is true
- If confirmation is required to open the wiki page when you are using an [command](#commands): default is false
- Which wiki language you prefer, overriding your game language: default is your game language
- (For Chinese users) Which Chinese variant you prefer, overriding your game language: default is your game language
- The keybind to open the wiki page: default is H
- Reach distance: default is 5.2 blocks (same as creative mode reach)
- Whether you are allowed to look up fluid: default is false
- Whether to add a tooltip to items in the inventory that can be looked up: default is true.

### Commands

These commands are available:

- `/imfeelinglucky [<namespace>]` - Takes you wherever the mod decides.
    - Example: `/imfeelinglucky minecraft` -> `https://minecraft.wiki/????????`
- `/wiki <pageName>` - Opens the Minecraft Wiki page of the specified page name. `pageName` can include a namespace.
    - Example: `/wiki minecraft:creeper` -> `https://minecraft.wiki/w/?search=creeper`
        - Alias: `/whatis`
- `/whatbiome` - Opens the Minecraft Wiki page of the biome you're currently in.
- `/whatstructure` - Opens the Minecraft Wiki page of the structure you're currently in.
    - This command is only available in singleplayer.
- `/whatcommand <command>` - Opens the Minecraft Wiki page of the specified command.
    - Example: `/whatcommand give` -> `https://minecraft.wiki/w/?search=%2Fgive`
    - Alias: `/whatcmd`
- `/whatisthis` - Opens the Minecraft Wiki page of the block/item/entity you're aiming at.
- `/whatisthisitem` - Opens the Minecraft Wiki page of the item you're holding in you main hand.
    - `whatisthisitem offhand` - Same, but for the offhand.
- `/whatversion` - Opens the Minecraft Wiki page of the current game version.
    - Example: `/whatversion` -> `https://minecraft.wiki/w/Java_Edition_1.21.5`

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
- [Mekanism Wiki](https://wiki.aidancbrady.com)
- [The Twilight Forest Wiki](http://benimatic.com/tfwiki/)
- [Applied Energistics 2 Wiki](https://guide.appliedenergistics.org/)
- [Stardust Labs Wiki](https://stardustlabs.miraheze.org/)
- [Create Wiki](https://create.fandom.com/wiki/)
- [Voidscape Wiki](https://voidscape.tamaized.com/)
- [Advent of Ascension Wiki](https://adventofascension.fandom.com/wiki/)
- [Doggy Talents Next Wiki](https://doggytalentsnext.wiki.gg/)
- [Endertech Infinity Wiki](https://endertechinfinity.wiki.gg/)
- [Marvel Superheroes Mod Wiki](https://marvelsuperheroesmod.wiki.gg/)
- [BlockFront Wiki](https://blockfront.wiki.gg/)
- [Cobblemon Wiki](https://wiki.cobblemon.com/)

If you want to add support for other wikis, you can
[file an issue](https://github.com/mc-wiki/minecraft-mod-heywiki/issues/new?labels=new+wiki%2Ctriage+needed&template=new_wiki.yml).
In addition, you can also add support for other wikis by using a resource pack.

## APIs

### Resource pack

> [!NOTE]
> This API is stable since v1.6.0.

Hey Wiki supports using resource pack to add support for other wikis. To do so, create a JSON file in the
`assets/<namespace>/wiki_family/` folder in your resource pack with the following format:

```json
{
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
      "mw_api_url": "https://minecraft.wiki/api.php",
      // (Optional) The URL pattern for searching. %s will be replaced with the query.
      "search_url": "https://minecraft.wiki/?search=%s",
      // (Optional) The page name for the random article
      "random_article": "Special:RandomRootPage/Main",
      // (Optional) How Hey Wiki should fetch excerpts from the wiki. Either "text_extracts" or "none".
      // "text_extracts" only works for MediaWiki sites with the TextExtracts and PageImages extension installed. You can check this by visiting Special:Version on the wiki.
      "excerpt": "text_extracts",
      // (Optional) Which provider to use for autocompleted search. Either "mediawiki" or "algolia".
      "search_provider": "mediawiki",
      // (Optional) If you use algolia as search provider, you MUST fill out the following fields
      "algolia": {
        "api_url": "https://example-dsn.algolia.net/",
        "index_name": "example",
        "api_key": "efcc14a8c70a1489d18cfd565cce53ca",
        "app_id": "9957A67102"
      },
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

You also want to include a translation file in `assets/<namespace>/lang/` folder with the following format:

```json
{
  // Assuming the definition is located at `assets/heywiki/wiki_family/minecraft.json`
  "wiki_family.heywiki.minecraft": "Minecraft Wiki"
}
```

### Custom item via data component or NBT

> [!WARNING]
> This API is an experimental feature and might change at any time.

Data pack and custom server authors can use `heywiki:identifier`, `heywiki:translation_key` and
`heywiki:fallback_title` in `custom_data` component to provide custom namespace and name for an item. However, this only
accounts for custom items. For 1.20.4, you can populate these fields directly in NBT.

You need to use the method above to register a new wiki with a custom namespace using resource pack. If it is not
feasible to ask your players to download a resource pack, we can also ship it with the mod itself.

For example, on "niceserver", to have a bone item to resolve to the "Drill" page, you will first need to register
the "niceserver" namespace per above. Then you can give the player this item:

1.21:

```mcfunction
/give @s minecraft:bone[minecraft:custom_data={"heywiki:identifier": "niceserver:drill", "heywiki:fallback_title": "Drill", "heywiki:translation_key": "item.niceserver.drill"}]
```

1.20.4:

```mcfunction
/give @s minecraft:bone{"heywiki:identifier": "niceserver:drill", "heywiki:translation_key": "item.niceserver.drill"}
```

## Dependencies

For Fabric:

- (Required) [Fabric API](https://modrinth.com/mod/fabric-api): provides basic modding utilities
- (Required) [Architectury API](https://modrinth.com/mod/architectury-api): for multi-modloader compatibility
- (Required) [Cloth Config](https://modrinth.com/mod/cloth-config): for configuration
- (Recommended) [Mod Menu](https://modrinth.com/mod/modmenu): to access the config menu
- (Optional) [MCBrowser](https://modrinth.com/mod/mcbrowser) and [MCEF](https://modrinth.com/mod/mcef): for in-game browser
- (Optional) [REI](https://modrinth.com/mod/roughly-enough-items)/[EMI](https://modrinth.com/mod/emi)/[JEI](https://modrinth.com/mod/jei): integration supported

For NeoForge:

- (Required) [Architectury API](https://modrinth.com/mod/architectury-api): for multi-modloader compatibility
- (Required) [Cloth Config](https://modrinth.com/mod/cloth-config): for configuration
- (Optional) [REI](https://modrinth.com/mod/roughly-enough-items)/[EMI](https://modrinth.com/mod/emi)/[JEI](https://modrinth.com/mod/jei): integration supported

## Versioning

### Hey Wiki

Hey Wiki itself follows [Semantic Versioning](https://semver.org/). The version number is in the format of
`<major>.<minor>.<patch>[-<prerelease>]`. The version number is incremented based on the following rules:

- **Major**: Incremented when breaking changes are made to stable, public API.
- **Minor**: Incremented when new features to stable, public API are added in a backwards-compatible manner.
- **Patch**: Incremented when bug fixes are made.

### Minecraft

Hey Wiki supports multiple versions of Minecraft.

Every Minecraft version is assigned a support status:

- **Active**: This version receives new features and bug fixes. Features will be backported as much as reasonably
  possible.
- **Maintenance**: This version receives only bug fixes and security patches. Features are generally not backported.
- **End of Life (EOL)**: No further updates should be expected.

The current Minecraft release and the master branches are always Active. Pull requests should almost always
go to `master`. If they're accepted, they should be cherry-picked to other Active branches.

When a new snapshot releases, `master` branch is updated to that snapshot. Snapshots might receive only one version or
no
version at all. Only Fabric is supported for snapshots.

Old Minecraft versions are provided with Long Term Support (LTS) based on their popularity and the community's interest.
LTS versions receive Active support at first. After some time, they will be downgraded to Maintenance.
Old snapshots are not supported.

The following table shows which versions are supported:

| Git branch  | Minecraft version    | Supported?        | Is LTS? | Modloader        |
|-------------|----------------------|-------------------|---------|------------------|
| `master`    | 1.21.6 - 1.21.8      | Active (Latest)   | ?       | Fabric, Neoforge |
| `mc/1.21.5` | 1.21.5               | EOL               | No      | Fabric, Neoforge |
| `mc/1.21.4` | 1.21.4               | EOL               | No      | Fabric, Neoforge |
| `mc/1.21.3` | 1.21.2, 1.21.3       | EOL               | No      | Fabric, Neoforge |
| `mc/1.21.1` | 1.21, 1.21.1         | Maintenance (LTS) | Yes     | Fabric, Neoforge |
| `mc/1.20.6` | 1.20.6               | EOL               | No      | Fabric, Neoforge |
| `mc/1.20.5` | 1.20.5               | EOL               | No      | Fabric, Neoforge |
| `mc/1.20.4` | 1.20.4               | Maintenance (LTS) | Yes     | Fabric, Neoforge |
| N/A         | *Outdated snapshots* | EOL               | No      | Fabric           |
