# v1.5.0+1.20.4

## Changes

- Now shows an excerpt and image from the wiki article when asking for confirmation to open link.
- `random_article` for `WikiIndividual` is no longer required. It defaults to `Special:RandomPage`.
- `/imfeelinglucky` now shows error message when the wiki does not support the random article feature.

We plan to introduce no more breaking changes to the resource pack `wiki_family` format starting the next minor version.
This will stabilize the API for mod pack authors starting 1.6.0. This also means any backward-incompatible change will
receive a major version bump.

## Fixes

- Fix incorrect links for Japanese Minecraft Wiki
