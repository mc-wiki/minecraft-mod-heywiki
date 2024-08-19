# v1.6.0+1.21

## Changes

- New feature: wiki search screen.
  - You can now search for wiki pages by pressing `B` in the game.
  - The search screen will show a list of pages that match the search query as you type.
  - You can use arrow keys to navigate the list and press `Enter` to open the selected page.
  - You can switch between wikis using the button available. Note not all wikis are available.
- Added a new option "Requires confirmation when using commands", in case you still want the preview screen when using
  the command. This option is off by default.
- Internal codebase refactor to improve maintainability.

### API
- BREAKING: The `id` field in the wiki family resource pack definition is now removed. It is now inferred from the
  directory name. This also means that they are now namespaced.
- BREAKING: Wiki families are now translatable at `wiki_family.<namespace>.<family>`.
- Wiki family resource pack definition is now stable. Any breaking changes will result in a major version bump.
