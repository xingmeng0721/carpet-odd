# Carpet Odd Addition Rules

Tip: Use Ctrl+F to quickly find the rule you want

All rules belong to the ODD category. Toggle them with `/carpet <rule> <true|false>`.

---

## Fake Player Auto-Drop Pure Shulker Boxes (autoDrop)
Scans a fake player's inventory for shulker boxes (including dyed ones) and throws out any box whose 27 slots contain only a single type of item.
Triggered by the command `/player <name> autodrop`, or by left-clicking a fake player while holding a cactus.

Type: boolean
Default: false
Suggested values: false, true
Category: ODD

---

## Bone Meal Spore Blossoms (bonemealSporeBlossom)
Applying bone meal to a placed Spore Blossom pops the flower as an item and randomly spawns new Spore Blossoms on suitable ceiling blocks nearby, making the vanilla non-renewable Spore Blossom renewable through automation. Consumes 1 bone meal; not consumed in Creative mode.

Type: boolean
Default: false
Suggested values: false, true
Category: ODD

---

## Torchflowers Drop Seeds (torchflowerDropSeeds)
Breaking a Torchflower additionally drops 1~3 Torchflower Seeds on top of the vanilla drops.

Type: boolean
Default: false
Suggested values: false, true
Category: ODD

---

## Cactus Oxidizes Copper (cactusOxidizeCopper)
Right-clicking a non-fully-oxidized copper block while holding a Cactus advances its oxidation state to the next stage, acting as the exact opposite of axe scraping. The cactus is not consumed and no durability is used. On fully oxidized blocks, right-click falls back to vanilla behavior (placing the cactus normally).

Type: boolean
Default: false
Suggested values: false, true
Category: ODD

---

## Batch Player Command (batchPlayerCommand)
Enables the `/playerManager batch <prefix> <start> <end> <action>` command for batch spawning (max 256 per batch), killing, and controlling fake players with Carpet action pack commands, with names built as `<prefix>_<index>`. Supported actions include spawn, kill, use, attack, jump, drop, drop_stack, swap_hands, move, sneak, unsneak, sprint, unsprint, look, turn, hotbar, mount, dismount, and stop. The perTick and randomly modes require Carpet TIS Addition.

Type: boolean
Default: false
Suggested values: false, true
Category: ODD

---

## Custom Player Inventory Stack Size (playerInventoryStack)
Allows using `/playerInventoryStack` to customize the maximum stack size of items inside player inventories only; dropped items and other containers keep vanilla behavior.
Subcommands: `set <item predicate> <count 1-99>`, `set filled_shulker_box <count 1-99>` (shulker boxes whose every slot is filled to its vanilla max stack size), `remove "<predicate>"`, `list`, `clear`.
Item predicates support NBT/component matching, e.g. `minecraft:potion[minecraft:potion_contents=potion_type:healing]`.

Type: boolean
Default: false
Suggested values: false, true
Category: ODD
