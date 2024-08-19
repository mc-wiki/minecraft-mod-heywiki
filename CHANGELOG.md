# v1.6.0+1.21.1

## Changes

- New feature: wiki search screen.
  - You can now search for wiki pages by pressing `B` in the game.
  - The search screen will show a list of pages that match the search query as you type.
  - You can use arrow keys to navigate the list and press `Enter` to open the selected page.
  - You can switch between wikis using the button available. Note that not all wikis are available.
- Added a new option "Requires confirmation when using commands", in case you still want the preview screen when using
  the command. This option is off by default.
- Internal codebase refactor to improve maintainability.

### API
- BREAKING: The `id` field in the wiki family resource pack definition is now removed. It is now inferred from the
  directory name. This also means that they are now namespaced.
- BREAKING: Wiki families are now translatable at `wiki_family.<namespace>.<family>`.
- Adds a new `search_url` field in the wiki family resource pack definition. This field is used to search for pages in the
  wiki. If the field is not present, the search feature mentioned above will not be available for this wiki.
- Wiki family resource pack definition is now stable. Any breaking changes will result in a major version bump.

## Fixes

- Another attempt at fixing conflict with RRLS and Zoomify (#58)
  - Note that this is a workaround and the underlying issue is still not fixed.
