# Pre/Post-push Checklist

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
  - Upload `OUT/claimchunk-VERSION.jar` as GitHub release with changelog.
- Post push
  - Add new release on [Spigot](https://www.spigotmc.org/resources/claimchunk.44458/) with changelog.
  - Add new release on [Modrinth](https://modrinth.com/plugin/claimchunk) with changelog.
  - Push to Maven Central:
    - `./gradlew publishToMavenCentral --no-configuration-cache`
  - Publish release on [Maven Central](https://central.sonatype.com/artifact/com.cjburkey.claimchunk/claimchunk).
- Done!

Make sure to be able to provide support and bugfix releases shortly after :/
