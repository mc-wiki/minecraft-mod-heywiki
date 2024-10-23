# v1.7.0+1.21.2

Hey Wiki now supports BoB!

## Changes

- Ported to 1.21.2. Please report any issues you may have!
- Now supports searching the AE2 guide

### APIs

- Now supports the `search_provider` field for wikis
  - Values can be `mediawiki` and `algolia`
  - All wikis that support autocompleted search should fill this field
  - If the field is not filled, and both `search_url` and `mw_api_url` are present, the search will default to `mediawiki`. However, this is deprecated and will be removed in v2
- `search_url` is now optional for wikis that supports search, as long as they have `search_provider` field filled
- Added Agolia search support
  - This requires you to configure the following fields in the `algolia` object:
    - `api_url`
    - `index_name`
    - `api_key`
    - `app_id`
