# Pre-push Checklist

Things to make sure I do before I push a new release version:
- `build.gradle.kts`
  - [ ] Update `THIS_VERSION`
  - [ ] Update `LIVE_VERSION`
  - [ ] Update [wiki](https://claimchunk.cjburkey.com/) with new information.
  - [ ] Create changelog
- Building
  - [ ] Run `./gradlew clean googleFormat`
  - [ ] Run `./gradlew build`
    - Run separate from others because of stupid error
  - Upload `OUT/claimchunk-VERSION-plugin.jar` as GitHub release with changelog
  - Add new release on [Spigot](https://www.spigotmc.org/resources/claimchunk.44458/) with changelog.
- Done!

Make sure to be able to provide support and bugfix releases shortly after :/
