# v1.6.2+1.21.1

## Fixes

### APIs

- In URLs, spaces are now replaced with `%20` instead of `+` to match the behavior of the browser. We are not
  considering this a breaking change because the this was a regression from the previous behavior.
