# v1.6.2+1.21.1

## Changes

- When you select a wiki when you search, it is now remembered for the next search.
- Now supports double-clicking a search result to open it in addition to pressing Enter.

### APIs

- The item stack component format now supports an additional `heywiki:fallback_title` field.
  - `heywiki:translation_key` is now optional. If it does not exist, the `heywiki:fallback_title` will be used instead.
  - If both are missing, the path of `heywiki:identifier` will be used instead.

## Fixes

- Fixed an issue where using Enter on the input does not work when your search includes spaces.
- If a namespace is not supported, `/wiki` no longer hard fails.

### APIs

- In URLs, spaces are now replaced with `%20` instead of `+` to match the behavior of the browser. We are not
  considering this a breaking change because the this was a regression from the previous behavior.
