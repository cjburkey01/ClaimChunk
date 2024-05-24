![ClaimChunk Logo](imgs/icon64.png)
![ClaimChunk Title](imgs/logo_carrier.png)

[![Plugin Version](https://img.shields.io/static/v1?label=Version&message=0.0.25&color=blueviolet&style=for-the-badge)](https://github.com/cjburkey01/ClaimChunk/releases)
[![Maven Central Version](https://img.shields.io/maven-central/v/com.cjburkey.claimchunk/claimchunk?label=Maven%20Central&color=blueviolet&style=for-the-badge)](https://central.sonatype.com/artifact/com.cjburkey.claimchunk/claimchunk)
[![Minecraft Version](https://img.shields.io/static/v1?label=Spigot&message=1.20.6&color=blueviolet&style=for-the-badge)](https://www.spigotmc.org/resources/claimchunk.44458/)
![Java Version](https://img.shields.io/static/v1?label=Java&message=17&color=blueviolet&style=for-the-badge)
[![Servers Using Claimchunk](https://img.shields.io/bstats/servers/5179?label=Servers&color=cornflowerblue&style=for-the-badge)](https://bstats.org/plugin/bukkit/ClaimChunk)
[![Players Using Claimchunk](https://img.shields.io/bstats/players/5179?label=Players&color=cornflowerblue&style=for-the-badge)](https://bstats.org/plugin/bukkit/ClaimChunk)

**Join us on our [Discord server](https://discord.gg/swW8xX665Z) for bug reports, support, and general chatting!**

Info
----
Spigot plugin for 1.20+ allowing the claiming of chunks.

*The destiny of chunks is to unite not to divide*<br />
*Let's make the world ours.*

Usage and more information can be found [on the wiki](https://github.com/cjburkey01/ClaimChunk/wiki).

* **1.20-1.20.6+** | The latest version works seamlessly (excluding bugs, of course).
* **1.17 - 1.20** | The latest *known* working version is [0.0.23-RC8](https://github.com/cjburkey01/ClaimChunk/releases/tag/0.0.23-RC8).
  * Newer versions of the plugin will require Java 17, but may still work.
* **1.13 - 1.16.5** | The latest working version is [0.0.22](https://github.com/cjburkey01/ClaimChunk/releases/tag/0.0.22).
  * Note: If you disable titles, 0.0.23 might work on version older than 1.17. If you experience issues, however, they may be more difficult to address.
* **Pre-1.13** | The latest working version is [0.0.20](https://github.com/cjburkey01/ClaimChunk/releases/tag/0.0.20)
  * Newer features of Spigot are used in builds 0.0.21 and later that make it incompatible with older versions.
  * **1.9 - 1.12** | Should work without any trouble.
  * **1.6 - 1.8** | Works when `useTitlesInsteadOfChat` is set to `false` under the `titles` section of the config.

If you have issues running an old version of the plugin on a Minecraft version within the above supported ranges, you 
may make an issue, and I should be able to help, but I won't be bringing newer features over. They officially have 
"minor bug support" status. Version 0.0.23 will be supported for slightly longer though, as this transition period may 
get funky.

For even more information, the SpigotMC page can be found [here](https://www.spigotmc.org/resources/claimchunk.44458/).

Download
--------
Downloads are available in the [**releases section**](https://github.com/cjburkey01/ClaimChunk/releases) and separately on [Modrinth](https://modrinth.com/plugin/claimchunk).

**The latest compiled version of this repository can be [downloaded via this link](https://nightly.link/cjburkey01/ClaimChunk/workflows/gradle/main/ClaimChunk.zip)**.

Optional Features:
* [Vault](https://www.spigotmc.org/resources/vault.34315/) (for currency support you will also need an economy plugin like [Essentials](https://www.spigotmc.org/resources/essentialsx.9089/)).
* [WorldGuard](https://dev.bukkit.org/projects/worldguard) (VERSION **7.0.0 OR ABOVE** REQUIRED and, as such, [WorldGuard support](https://github.com/cjburkey01/ClaimChunk/wiki/WorldGuard-Integration) is only available for **1.13 OR ABOVE**).
* [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (View the available [placeholders on the wiki](https://github.com/cjburkey01/ClaimChunk/wiki/Placeholder-API-Integration)).

News
----
> This section is being written on the 600th commit! Happy 600 everyone :) May our years going forward be full of joy!
> 
> We've had ClaimChunk among us for 2473 days (as of today), thanks to the effort of 9 other contributors supporting me along the way!
> 
> If you'd like to help out, open a pull request or submit a GitHub issue with a feature request and/or bugs you've come across!

Guess what! I'm sorta back :) Life has been crazy for the last couple of years, but here I am :)
I hope to continue working on this plugin and making existing features more stable (and maybe a
small thing here or there)!

Please do join the Discord server if you have any trouble with anything or want to know what I'm currently doing. I'll 
be a little more online, I hope!

Plugin Integrations
-------------------
If you have developed an addon for ClaimChunk or for another plugin that integrates with ClaimChunk, or have added 
ClaimChunk support to your plugin, let me know, and I'll put your addon/plugin here. You can reach me at 
[`bulletlanguage@gmail.com`](mailto:bulletlanguage@gmail.com) or through the [Discord server](https://discord.gg/swW8xX665Z).

Here are some plugins that either have addons for or integrations with ClaimChunk:
* MongoDB integration with [this addon](https://github.com/LowBudgetCraft/ClaimChunkMongoDB) by [Glowman554](https://github.com/Glowman554)
* [Regionerator](https://www.spigotmc.org/resources/regionerator.12219/) (prevent claimed chunks from being regenerated)

Addons that aren't actively maintained but could be useful:
* [Dynmap](https://www.spigotmc.org/resources/dynmap.274/) via [this addon](https://www.spigotmc.org/resources/dynmap-claimchunk.71093/)
* Archived: [*Pl3xMap*](https://github.com/pl3xgaming/Pl3xMap) via [this addon](https://github.com/pl3xgaming/Pl3xMap-ClaimChunk)
* (Old, maybe functional, repository deleted) [ClaimFly](https://www.spigotmc.org/resources/claimfly-claimchunk-addon-1-18-x.99189/) (allowing players to fly in claimed territory).

Now on Maven Central! To make a plugin, use one of these snippets:

Maven:

```xml
<dependency>
    <groupId>com.cjburkey.claimchunk</groupId>
    <artifactId>claimchunk</artifactId>
    <version>0.0.25</version>
</dependency>
```

Gradle (Groovy):

```groovy
implementation 'com.cjburkey.claimchunk:claimchunk:0.0.25'
```

Gradle (Kotlin):

```kotlin
implementation("com.cjburkey.claimchunk:claimchunk:0.0.25")
```

Building
--------
[![Automatic Build](https://img.shields.io/github/actions/workflow/status/cjburkey01/ClaimChunk/gradle.yml?branch=main&style=for-the-badge)](https://claimchunk.cjburkey.com/server/Downloads.html#snapshot-downloads)
[![Version Info](https://img.shields.io/static/v1?label=Repository%20Version&message=0.0.25&color=ff5555&style=for-the-badge)](https://github.com/cjburkey01/ClaimChunk/archive/main.zip)

If you want to obtain a version of the plugin that isn't available yet (like a snapshot), you can do so by asking on the 
Discord or building it yourself. Here's how to build it yourself:

First, you'll need to clone the repo either using the Git command:<br />
`git clone git@github.com:cjburkey01/ClaimChunk.git`<br />
or downloading the repository source using the button above that says "Clone or download".

Once you have obtained the repo, inside the local repository, you'll just need to execute this Gradle Wrapper command:
* `./gradlew build` for Unix/Mac
* `gradlew build` for Windows

Your built jar file can be found at:<br />
`<REPO>/OUT/claimchunk-VERSION.jar`
