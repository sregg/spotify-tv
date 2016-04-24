[![Circle CI](https://circleci.com/gh/sregg/spotify-tv/tree/master.svg?style=svg)](https://circleci.com/gh/sregg/spotify-tv/tree/master)

# TV Player for Spotify
Unofficial Spotify app for Android TV

# Features
- Login in with your username & password or via Facebook
- List your playlists (including starred), saved albums, artists and songs
- See the tracklist for each playlist and album (thanks to @Dahlgren)
- Stream any track of your library
- Search for artists, albums, songs and playlists
- Controls (Shuffle, Previous/Next Track, Play, Pause, Stop)
- Global search (thanks to @Dahlgren)
- Featured Playlists and New Releases (thanks to @Dahlgren)
- Settings (Bitrate)
- Recommendations in Android TV Home (based on Featured Playlists in the user's country) (thanks to @Dahlgren)
- Last.fm scrobbling
- Browse playlists by categories (thanks to @Dahlgren)
- Setting - Customize UI elements (e.g. show or hide Feature playlist and new releases)

# TODO
- Control buttons in Details screen and Now Playing screen
- Add to playlist button for each track in the tracklist
- Manage Playlist (create, edit, delete, etc...)
- Recently Played
- Friends
- Recommendations in Android TV Home (based on user's recommended artists/albums)
- Setting - dim screen after 2 minutes of playing
- Game controller controls (play, pause, previous, next, etc...)
- Refactor: recommendations using [support library](http://developer.android.com/tools/support-library/features.html#recommendation)

# Release build
Add the values `release.storeFile`, `release.storePassword`, `release.keyAlias`, and `release.keyPassword` to `release.properties`.
`release.storeFile` should be an absolute path to your keystore, the others should be the string value.
See `release.properties.sample` for an example file.

# Pull Requests
I welcome and encourage all pull requests. 
It usually will take me within 24-48 hours to respond to any issue or request. 
Here are some basic rules to follow to ensure timely addition of your request:

- Match coding style (braces, spacing, etc.) This is best achieved using CMD+Option+L (Reformat code) on Mac (not sure for Windows) with Android Studio defaults.
- If its a feature, bugfix, or anything please only change code to what you specify. DO NOT do this: Ex: Title "Fixes Crash Related to Bug" includes other files that were changed without explanation or doesn't relate to the bug you fixed. Or another example is a non-descriptive title "Fixes Stuff".
- Pull requests must be made against latest master branch.
- Have fun!

# License
GPL v2

This open source app complies with the [Spotify TOS](https://developer.spotify.com/developer-terms-of-use/)
