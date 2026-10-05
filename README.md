# TradeShuffler 🔄

TradeShuffler improves villager trading:

- **Shuffle trades:** re-roll the trades of a villager you have not traded with yet, right from the trading screen.
- **Trade preview:** see the trades a villager will unlock at its future levels, darkened and not selectable.

TradeShuffler is in development. The features above are not available yet.

## Quick start for players 🎮

1. Put `TradeShuffler` and `TurtleLib` (required dependency) in the `mods` folder.
2. Install it on the server too: TradeShuffler's features need it on both the client and the server. In single-player this happens automatically.
3. Start the game.

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
