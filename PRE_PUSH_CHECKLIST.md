# Pre-push Checklist

Things to make sure I do before I push a new release version:
- `build.gradle.kts`
  - [ ] Update `THIS_VERSION`
  - [ ] Update `LIVE_VERSION`
  - [ ] Update [wiki](https://claimchunk.cjburkey.com/) with new information.
  - [ ] Create changelog
- Building
  - [ ] Run `./gradlew clean`, `./gradlew googleFormat`, and `./gradlew build`
    - These must be run separately for compatibility reasons (might be fixed in the future).
  - Upload `OUT/claimchunk-VERSION-plugin.jar` as Github release with changelog
  - Add new release on [Spigot](https://www.spigotmc.org/resources/claimchunk.44458/) with changelog.
- Done!

Make sure to be able to provide support and bugfix releases shortly after :/
