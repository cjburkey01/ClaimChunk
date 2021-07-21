![ClaimChunk Logo](imgs/icon64.png)
![ClaimChunk Title](imgs/logo_carrier.png)

[![Version Info](https://img.shields.io/static/v1?label=Version&message=@LIVE_VERSION@&color=blueviolet&style=for-the-badge)](https://github.com/cjburkey01/ClaimChunk/releases)
[![Download Info](https://img.shields.io/static/v1?label=Spigot&message=@LATEST_MC_VERSION@&color=blueviolet&style=for-the-badge)](https://www.spigotmc.org/resources/claimchunk.44458/)
[![Servers Using Claimchunk](https://img.shields.io/bstats/servers/5179?label=Servers&color=cornflowerblue&style=for-the-badge)](https://bstats.org/plugin/bukkit/ClaimChunk)
[![Players Using Claimchunk](https://img.shields.io/bstats/players/5179?label=Players&color=cornflowerblue&style=for-the-badge)](https://bstats.org/plugin/bukkit/ClaimChunk)

**Join us on our [Discord server](https://discord.gg/swW8xX665Z) (*zGYrqcq*) for bug reports, support, and general chatting!**

Info
----
Spigot plugin for 1.13+ allowing the claiming of chunks.

*The destiny of chunks is to unite not to divide*<br />
*Let's make the world ours.*

Usage and more information can be found [on the wiki](https://github.com/cjburkey01/ClaimChunk/wiki).

* **1.13 - @LATEST_MC_VERSION@** | The latest version works seamlessly.
* **Pre-1.13** | The latest working version is [0.0.20](https://github.com/cjburkey01/ClaimChunk/releases/tag/0.0.20)
  * Newer features of Spigot are used in builds 0.0.21 and later that make it incompatible with older versions.
  * **1.9 - 1.12** | Should work without any trouble.
  * **1.6 - 1.8** | Works when `useTitlesInsteadOfChat` is set to `false` under the `titles` section of the config.

For even more information, the SpigotMC page can be found [here](https://www.spigotmc.org/resources/claimchunk.44458/).

Download
--------
Downloads are available in the [**releases section**](https://github.com/cjburkey01/ClaimChunk/releases).

**The latest compiled version of this repository can be [downloaded via this link](https://nightly.link/cjburkey01/ClaimChunk/workflows/gradle/master/ClaimChunk.zip)**.

Optional Features:
* [Vault](https://www.spigotmc.org/resources/vault.34315/) (for currency support you will also need an economy plugin like [Essentials](https://www.spigotmc.org/resources/essentialsx.9089/)).
* [WorldGuard](https://dev.bukkit.org/projects/worldguard) (VERSION **7.0.0 OR ABOVE** REQUIRED and, as such, [WorldGuard support](https://github.com/cjburkey01/ClaimChunk/wiki/WorldGuard-Integration) is only available for **1.13 OR ABOVE**).
* [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (View the available [placeholders on the wiki](https://github.com/cjburkey01/ClaimChunk/wiki/Placeholder-API-Integration)).

Plugin Integrations
-------------------
If you have developed an addon for ClaimChunk or for another plugin that integrates with ClaimChunk, or have added ClaimChunk support to your plugin, let me know, and I'll put your addon/plugin here.
You can reach me at [`bulletlanguage@gmail.com`](mailto:bulletlanguage@gmail.com) or through the [Discord server](https://discord.gg/swW8xX665Z).

Here are some plugins that either have addons for or integrations with ClaimChunk:
* [Regionerator](https://www.spigotmc.org/resources/regionerator.12219/)
* [Pl3xMap](https://github.com/pl3xgaming/Pl3xMap) via [this addon](https://ci.pl3x.net/view/Pl3xMap/job/Pl3xMap-ClaimChunk/)
* [Dynmap](https://www.spigotmc.org/resources/dynmap.274/) via [this addon](https://www.spigotmc.org/resources/dynmap-claimchunk.71093/)

Building [![Build Status](https://travis-ci.com/cjburkey01/ClaimChunk.svg?branch=master)](https://travis-ci.com/cjburkey01/ClaimChunk)
--------
[![Version Info](https://img.shields.io/static/v1?label=Repository%20Version&message=@PLUGIN_VERSION@&color=ff5555&style=for-the-badge)](https://github.com/cjburkey01/ClaimChunk/archive/master.zip)

If you want to obtain a version of the plugin that isn't available yet (like a snapshot), you can do so by asking on the Discord or building it yourself. Here's how to build it yourself:

First, you'll need to clone the repo either using the Git command:<br />
`git clone git@github.com:cjburkey01/ClaimChunk.git`<br />
or downloading the repository source using the button above that says "Clone or download".

Once you have obtained the repo, inside the local repository, you'll just need to execute this Gradle Wrapper command:<br/>
* `./gradlew build` for Unix/Mac
* `gradlew build` for Windows

Your built jar file can be found at:<br />
`<REPO>/OUT/claimchunk-VERSION.jar`
