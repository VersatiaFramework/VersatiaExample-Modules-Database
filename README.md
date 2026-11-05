# VersatiaExample-Modules-Database

Player coins kept in a database with no setup: inject `Database` from the Database module and the plugin gets a SQLite file in its data folder, with the migrations from `database/migrations` in its JAR applied. A player store, `class Profiles(database: Database) : PlayerStore<Profile> by database.playerStore("profiles", Profile::uuid, ...)`, loads each profile before its player joins and saves it on quit; coins change in memory with `profiles.update(player) { ... }`, and work that needs the database runs on its threads and comes back with `thenSync { }` or `await()`.

Part of the [Versatia examples](https://github.com/VersatiaFramework/VersatiaExamples); documentation: [Database module](https://versatia.dev/docs/modules/database).

**Start with** [`Profiles.kt`](src/main/kotlin/dev/versatia/example/database/Profiles.kt) and `src/main/resources/database/`, then [`Rewards.kt`](src/main/kotlin/dev/versatia/example/database/Rewards.kt), [`Bounties.kt`](src/main/kotlin/dev/versatia/example/database/Bounties.kt), [`LeaderboardTask.kt`](src/main/kotlin/dev/versatia/example/database/LeaderboardTask.kt) and [`AutosaveNotices.kt`](src/main/kotlin/dev/versatia/example/database/AutosaveNotices.kt). Every table and query name lives once, in [`Sql.kt`](src/main/kotlin/dev/versatia/example/database/Sql.kt): `playerStore(Tables.PROFILES, ...)`, `named(Queries.TOP_COINS, ...)`.

## What the log shows

```
[VersatiaExample-Modules-Database] Applying V1__create_profiles.sql
[VersatiaExample-Modules-Database] Applying V2__create_payments.sql
[VersatiaExample-Modules-Database] Applying V3__index_payments_by_note.sql
[VersatiaExample-Modules-Database] SQLITE database.db, prefix market_, schema V3 (applied V1, V2, V3 in 38 ms)
[VersatiaExample-Modules-Database] SQLITE database.db, schema V3
[VersatiaExample-Modules-Database] Nobody has coins yet
```

The first four lines are the module's, at the first start only (later starts show `schema V3`);
the last two come from `report()` in [`MarketPlugin.kt`](src/main/kotlin/dev/versatia/example/database/MarketPlugin.kt).
In game, every mob killed pays a coin, dying to a mob costs five when you have them, and killing a
player takes a tenth of their coins: `Steve claims 4 coins, the bounty on Alex (payment #1)`.
Every five minutes everyone reads `Richest: Steve (44), Alex (36)` and their own rank, and whenever
coins changed, the autosave announces itself: `Saving coins...`, then `Saved 2 profiles in 4 ms`.

## Things to try

- In `plugins/VersatiaExample-Modules-Database/database.yml` (the module writes it at the first
  start), change `engine: sqlite` to `engine: h2`, add `import-from: { engine: sqlite }` and restart:
  the plugin now runs on H2, and the log shows the tables copied from `database.db`, which stays as a
  backup. No line of the plugin changes.
- Edit `V1__create_profiles.sql` after it has run, even only a comment, rebuild and restart: the start fails with
  `Plugin VersatiaExample-Modules-Database: migration V1__create_profiles.sql was changed after it was applied (checksum ... != ...); restore it and put the change in a new migration (during development, delete plugins/VersatiaExample-Modules-Database/database.db instead)`.
- Inject `database: Database` into `Rewards` and call `database.named("top-coins", "limit" to 3).list<TopEntry>()`
  in `onKill`: killing a mob fails with
  `Plugin VersatiaExample-Modules-Database: Statement.list blocks the server thread; run it inside database.async { } (main-thread: throw in plugins/VersatiaExample-Modules-Database/database.yml)`.
  The same call in `report()` is fine: while a plugin starts, reloads or stops, blocking is allowed.
- Set `slow-query: 1ms` in `database.yml` and run `/versatia reload market`: the new value applies
  at once, and the queries `report()` runs are logged as `Slow statement (... ms): SELECT name, coins FROM {prefix}profiles ...`.
  Change `engine` and reload instead: the reload only warns that `database.yml now points to` the
  new database and `this takes effect after a restart`.
- Stop the server with players online: `onLeave` runs for each of them before the final save, so
  `last_seen` in `market_profiles` holds the time of the stop.
- Before the first start, write `table-prefix: ''` into `plugins/VersatiaExample-Modules-Database/database.yml`:
  `{prefix}` expands to nothing, so the tables are `profiles` and `payments` instead of
  `market_profiles` and `market_payments`. Fine while no other plugin in the same database has a
  `profiles` table; one that has would stop with `table "profiles" already exists and belongs to plugin ...`.
- Set `defaults: { stores: { autosave: 30s } }` in `plugins/VersatiaModule-Database/config.yml` and
  run `/versatia reload database`: the autosave of this plugin (and of every plugin whose
  `database.yml` leaves `stores.autosave` out) now runs every 30 seconds.

## Building

Requires JDK 25 and, in your local Maven repository, VersatiaStarter with what it exports
(VersatiaAPI), VersatiaModule-Database (the module's API, which the starter does not export) and
VersatiaCatalog, which supplies every version (`./gradlew publishToMavenLocal` in each checkout; see
[Building the framework](https://versatia.dev/docs/contributing/building));
running it requires a Paper 26.3 server with VersatiaCore and VersatiaModule-Database installed,
and the [CommandAPI](https://commandapi.jorel.dev) 12.x plugin for `/versatia reload`.

```bash
./gradlew build
```

To have the JAR copied to your server after every build, copy `local.properties.example` to
`local.properties` and set `pluginsDir`. The generated descriptor is in
`build/generated/ksp/main/kotlin`; open it to see exactly what the runtime executes.

The Versatia version is `versatiaVersion` in `gradle.properties`; `settings.gradle.kts` imports
the matching [VersatiaCatalog](https://versatia.dev/docs/getting-started/installation#versatia-catalog),
so the build script writes `libs.versatia.starter` and
`alias(libs.plugins.ksp)` instead of version numbers.
