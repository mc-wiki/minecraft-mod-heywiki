# v1.1.0

## Changes

- Now defaults to `H` key.
- Now reloads translation on resource reload to prevent reloading during key press.
- Language config now shows language name in addition to language code.
- Added redirect from `/whatis` to `/wiki`.
- Added `pt_br` translation.

## Fixes

- Fix case where language is set to auto and game language does not have a wiki.
- Language used is no longer always set to en now, if set to a value other than auto.
- Now no longer use map to store languages, improving performance.
