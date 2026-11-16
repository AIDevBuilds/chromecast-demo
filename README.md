# Chromecast audio demo

Legacy Android (Eclipse) demo for casting audio to a Chromecast.

This is an older sample kept for reference. It is not actively maintained.

## What it contains

- `MyServer`: a small NanoHTTPD server that serves a local file with the `audio/mp3` content type
- `ChromecastFragment`: starts the server and lets the user choose a file (`ACTION_GET_CONTENT`), resolving its path through the media store
- Bundled libraries: NanoHTTPD 2.2.0, AppCompat v7, MediaRouter and Google Play services

## Notes

- Eclipse project layout (`src/`, `gen/`, `bin/`). Build output (`bin/`, `.class` files) is committed.
- It uses the old Google Play services cast support and has not been updated for current Cast SDKs.

Third-party libraries bundled as `.jar` files keep their own licences.

## License

MIT. See [LICENSE](LICENSE).
