# TradeShuffler 🔄

TradeShuffler improves villager trading: re-roll the trades of a novice villager you have not traded with yet, right from the trading screen, and see the trades it will unlock at its future levels. The preview is binding: the villager unlocks exactly what it shows.

## Quick start for players 🎮

1. Put `TradeShuffler` and `TurtleLib` (required dependency) in the `mods` folder.
2. For multiplayer, install both on the server too: the server generates the trades, so the features only appear when TradeShuffler runs there as well. In single-player this happens automatically.
3. Start the game and open a villager's trading screen.
4. Optional: bind the shuffle key under Controls → Key Binds → TradeShuffler, and adjust the settings via Mod Menu (Fabric) or the mod list (NeoForge).

## What TradeShuffler provides ✨

- **Shuffle trades:** a button next to the trading window re-rolls the trades of a novice villager you have never traded with
- **Trade preview:** the trading window also lists the trades of the villager's future levels (Apprentice to Master), darkened, with a colored level stripe and an "unlocks at" tooltip
- **Binding preview:** on level-up the villager unlocks exactly the previewed trades, also after saving and reloading, and after a zombie infection and cure
- **Clear reasons:** when shuffling is not possible, the button is greyed out and its tooltip says why
- optional key binding for shuffling (unbound by default)
- in-game settings for button position, preview darkening and level stripe, on Fabric and NeoForge
- server settings for admins: turn shuffling off, require a job site, set a cooldown
- update check via Modrinth and CurseForge (Stable / Beta / Alpha), can be turned off (see below)

## Runtime behavior 🧭

- Shuffling works only while **no trade has ever been made** with that villager. After the first trade, the button stays greyed out for good, also after restocks and restarts.
- Only novice villagers with a job site can be shuffled. Wandering traders, nitwits and unemployed villagers have no button.
- After shuffling, the preview appears about two seconds after the last shuffle, so quick re-rolls stay smooth on the server.
- Preview rows cannot be selected. Their prices are base prices; discounts (reputation, Hero of the Village) apply once the trade is unlocked.
- A villager nobody traded with can still be shuffled after a zombie infection and cure.
- Mixed installations are safe: without TradeShuffler on the server, there is no button and no preview; players without TradeShuffler see vanilla trading on a server that has it.

## Configuration ⚙️

Client settings are managed in the in-game config screen (Mod Menu on Fabric, the mod list on NeoForge). Server admins set `shuffleEnabled`, `previewEnabled`, `requireJobSite` and `shuffleCooldownTicks` in `config/tradeshuffler-server.json`; details in the [wiki](https://github.com/hrobasti/tradeshuffler-mod/wiki).

## Supported languages 🌍

TradeShuffler bundles each locale as a JSON file. Included languages:

- 🇺🇸 English (en\_US)
- 🇩🇪 German (de\_DE)
- 🇸🇦 Arabic (ar\_SA)
- 🇪🇸 Spanish (es\_ES)
- 🇫🇷 French (fr\_FR)
- 🇮🇹 Italian (it\_IT)
- 🇯🇵 Japanese (ja\_JP)
- 🇰🇷 Korean (ko\_KR)
- 🇳🇱 Dutch (nl\_NL)
- 🇵🇱 Polish (pl\_PL)
- 🇵🇹 Portuguese (pt\_PT)
- 🇹🇷 Turkish (tr\_TR)
- 🇺🇦 Ukrainian (uk\_UA)
- 🇨🇳 Simplified Chinese (zh\_CN)

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

To work on both at once, clone TurtleLib next to this repository (`../turtle-lib-mod`) and add `-PuseLocalTurtleLib=true`. The server game tests (`gameTestTradeShuffler`) need the full development workspace; see the Dev Guide.

## AI support & privacy transparency 🤖

Parts of the source code and documentation were created with AI assistance.

- **No personal player data is used** in this process.
- Ideas, design decisions, and quality standards come from humans.
- Quality assurance is reviewed and validated by humans.

## Developer notes 🧩

TradeShuffler uses TurtleLib only through `UpdateFacade` in `common`; no other code imports TurtleLib. Translations use Minecraft's lang files. Minecraft code shared by both loaders lives in `shared`; module layout, mixins and the trade-plan design are described in the [Dev Guide](https://github.com/hrobasti/tradeshuffler-mod/wiki).

## License 📄

MIT

Full license text:

- [`LICENSE`](LICENSE)

Third-party license texts/references:

- [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md)
