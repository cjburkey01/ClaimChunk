# 1.0.0 Final Product Goals:

## Switch to Paper API

Things that need to change:
- Move commands over to Brigadier command system
  - https://docs.papermc.io/paper/dev/command-api/basics/introduction/
- Upgrade to translatable system included in Paper
  - https://docs.papermc.io/paper/dev/component-api/i18n/
  - Load from external files for owner customization
  ```java
  TranslationStore englishStore = TranslationStore.builder()
                                      .name(Key.key("myplugin", "en_us"))
                                      .build();
  loadTranslationsFromFile(englishStore, "en_us.properties");
  registry.addSource(englishStore);
  // -- snip -- //
  ResourceBundle bundle = new PropertyResourceBundle(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
  bundle.keySet().forEach(key -> store.put(key, bundle.getString(key)));
  ```
- Work on including a `paper-plugin.yml` file

## Flags

Flags are defined by the server administrators in the `flags.yml` (An
unmodifiable copy of the default list can be found in
`defaults/default-flags.yml`).

The format of the flags file is as follows:

```yml
permissionFlags:
  PROTECT_FLAG_EXAMPLE:
    # Message shown to players when they have been denied an action.
    # [Optional] No message shown by default if not supplied.
    denyMessage: '&4Default message.'
    # At least one effect needs to be provided.
    effects:
      # For each effect, the `for`s and `type`s are required.
      - for: FLAG_TARGET        # May be BLOCKS, ENTITIES, PLAYERS, (etc.?)
        type: FLAG_ACTION_TYPE  # Action relating to the target, such as BREAK
                                # for blocks or DAMAGE for entities.
        adminOnly: true         # [Optional] If provided and `true`, only
                                # players with admin level privileges can
                                # Enable/disable for claims (or server claims)
        # If neither include/exclude is provided, the effect will apply to
        # *all* blocks/entities (so you should probably provide at least one
        # or both!).
        # Include/exclude may contain Minecraft namespaced block/entity names
        # or Bukkit/Spigot Material/EntityType enum values.
        include: ['minecraft:example_block', 'EXAMPLE_BLOCK']
        # They may also contain references to @CLASSES (the list for which can
        # be found in `defaults/default-classes`)
        # Custom classes may be defined in `custom-classes.yml`
        # Finally, they can *also* reference the "tags" introduced some while
        # ago.
        exclude: ['@BLOCK_CLASS', '#minecraft:block_tag']
        protectWhen: ENABLED    # Whether this protection *should apply* when
                                # this flag is ENABLED or DISABLED.
                                # [Optional] Default is to protect when *disabled*
                                # (If the flag is turned off, the protection
                                # effect will apply.)
        # This error message is shown if this effect is what resulted in the
        # player being denied an action.
        # This error overrides the denyMessage supplied for the flag above.
        denyMessage: '&4Specific error message.'
      # This random effect will just apply to all of the target type but will
      # only prevent the action if this flag is disabled.
      - for: DIFFERENT_TARGET
        type: DIFFERENT_ACTION
        protectWhen: DISABLED
```

To keep the same feature set as provided by the existing world profiles, I'll
need to make sure the following things have effect action types:
* Preventing adjacent blocks of same types (like chests, trapped chests, etc.).
* Spread protections for liquids/pistons/fire/etc.
  * Protection for into the chunk, out of the chunk, and within the chunk.
* Ender pearls into/out of chunks.

So, with that in mind, here are some effect targets and their associated valid
actions:
* `BLOCKS`: `BREAK`, `PLACE`, `INTERACT`, `EXPLODE`, `SPREAD_INTO`,
  `SPREAD_OUT_OF`, `SPREAD_WITHIN`, `GROW`
* `ENTITIES`: `DAMAGE`, `INTERACT`, `EXPLODE`
* `PLAYERS`: `ATTACK_OTHERS`, `ATTACK_OWNER`, `PEARL_INTO`, `PEARL_OUT_OF`,
  `PEARL_WITHIN`
  * Include and exclude are ignored for player targets.

To control the flags, players will use the flag command:

```
/chunk flag set [here] [player <PLAYER NAME>] <SOME FLAG>:<true/false> [<OTHER FLAG>:<true/false>]
```
or
```
/chunk flag <enable/disable> [here] [player <PLAYER NAME>] <all/SOME FLAG> [SOME OTHER FLAG]
```

To view flags, something like
```
/chunk flag list [here] [player <PLAYER NAME>] [true/false]
```
should be good

## Event Handling

The inner flag event handler takes the target & flag type, optionally a player, and
either the affected block or entity.

Pseudocode:
```
EVENT_RESULT checkEventProtected(FLAG_TARGET_TYPE, FLAG_ACTION_TYPE, Player? ACTOR, Block/Entity AFFECTED)
```

This function needs to:
1) Get the owner of the chunk containing the affect block/entity
2) Check if any flags have any effect that covers the affected block/entity.
    * If the chunk is not owned, check against the default flags for that world.
    * If the chunk is owned, check for the first **set** flag from most to least
      specific for whether explicitly allowed/denied. Check in this order:
      1) Player-specific, chunk-specific flags
      2) Player-specific flags
      3) Chunk-specific flags
      4) Owner's flags
3) Return whether allowed/denied & optionally the deny message if specified
   for the specific effect/flag.

The event handler should also have a wrapper function that can take the event
result/maybe deny message combo, with the cancellable event, and handle that
how it's needed.
Something else I'd like to do is have the message handler filter duplicate
messages for a period of time.
