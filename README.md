# TradeShuffler 🔄

TradeShuffler improves villager trading:

- **Shuffle trades:** re-roll the trades of a novice villager you have not traded with yet, right from the trading screen. If shuffling is not possible, the button tells you why.
- **Trade preview:** see the trades a villager will unlock at its future levels, darkened and not selectable. The preview is binding: the villager unlocks exactly what it shows.

TradeShuffler is in development towards version 1.0.0. Full documentation: [Wiki](https://github.com/hrobasti/tradeshuffler-mod/wiki).

## Quick start for players 🎮

1. Put `TradeShuffler` and `TurtleLib` (required dependency) in the `mods` folder.
2. Install it on the server too: TradeShuffler's features need it on both the client and the server. In single-player this happens automatically.
3. Start the game. Settings: Mod Menu (Fabric) or the mod list (NeoForge). An optional shuffle key can be bound under Controls → Key Binds → TradeShuffler.

## Update check & privacy 🔔

When the game starts, TradeShuffler checks once in the background whether a newer version exists, and shows a chat message with links after you join a world. The check is on by default; turn it off with **Update check** in the settings. The channel (**Stable**: releases only, **Beta**: releases and betas, **Alpha**: everything) decides which versions count.

- **Queried services** (only platforms where TradeShuffler is published): `api.modrinth.com` (Modrinth, official API) and `api.cfwidget.com` (CFWidget, an unofficial third-party service that mirrors CurseForge data; it publishes no privacy policy and states that it stores no identifiable information).
- **Transmitted data:** your IP address (part of every web request), the mod name and version (user agent), the Minecraft version, the loader (Fabric or NeoForge) and the project ID. No account, player or world data.
- Modrinth's handling of request data: [Modrinth privacy policy](https://modrinth.com/legal/privacy).
- With Mod Menu installed (Fabric), Mod Menu additionally checks your installed mods on Modrinth; that check is configured in Mod Menu's own settings.

## Quick start for devs 🛠️

This repository is a standalone Gradle build. Clone it and use its own wrapper; no other repository is needed. A JDK 17+ must be installed to start Gradle. The Java toolchain required for compiling is detected automatically or downloaded.

- Build and test: `./gradlew build`, `./gradlew testTradeShuffler`
- Validate the build matrix: `./gradlew verifyMatrixTargets`
- Full release build (all matrix targets, Fabric + NeoForge) into `dist/`: `./gradlew releaseTradeShuffler`

Minecraft, Java and loader versions come from [`config/matrix-targets.json`](config/matrix-targets.json). Without parameters, builds use the first target in that file.

**TurtleLib:** the build downloads the [TurtleLib](https://github.com/hrobasti/turtle-lib-mod) core jar from its GitHub release. The version is set by `turtlelib_dependency_version` in [`version.properties`](version.properties).

To work on both at once, clone TurtleLib next to this repository (`../turtle-lib-mod`) and add `-PuseLocalTurtleLib=true`.

## AI support & privacy transparency 🤖

Parts of the source code and documentation were created with AI assistance.

- **No personal player data is used** in this process.
- Ideas, design decisions, and quality standards come from humans.
- Quality assurance is reviewed and validated by humans.

## License 📄

MIT

Full license text:

- [`LICENSE`](LICENSE)

Third-party license texts/references:

- [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md)
