# v1.3.1

## Changes

- Adds `/whatisthisitem offhand`
- `/wiki` autocomplete is now cached locally to improve performance
- Now prompts user when `H` is pressed but nothing is being aimed at
- Added configuration options for reach distance and whether fluid is allowed
- Setting configurations other than wiki language no longer requires a resource reload

## Fixes

- Fixes `/wiki` autocomplete showing duplicate entries
- Fixes bad debouncing in `/wiki` autocomplete
- Fixes bad encoding in `/wiki` autocomplete requests. Now forced to UTF-8
- Fix inventory closes after dismissing the url prompt
