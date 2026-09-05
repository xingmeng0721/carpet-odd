# Carpet Odd Addition Rules

> **Tip:** Use `Ctrl + F` to quickly find the rule you are looking for.

All rules belong to the **ODD** category and can be enabled or disabled with:

```text
/carpet <rule> <true|false>
```

---

## `autoDrop` — Dummy Player Shulker Box Drop

Scans a dummy player's inventory for shulker boxes, including dyed shulker boxes. If all 27 slots inside a shulker box contain only the same item, the shulker box is dropped from the dummy player's inventory.

**Trigger methods:**

* `/player <name> autodrop`
* Left-click the dummy player while holding a cactus

| Property | Value            |
| -------- | ---------------- |
| Type     | `boolean`        |
| Default  | `false`          |
| Options  | `false` / `true` |
| Category | `ODD`            |

---

## `bonemealSporeBlossom` — Bonemeal Spore Blossom

Using bonemeal on a placed spore blossom drops the spore blossom as an item and randomly generates new spore blossoms on nearby eligible ceiling blocks.

| Property | Value            |
| -------- | ---------------- |
| Type     | `boolean`        |
| Default  | `false`          |
| Options  | `false` / `true` |
| Category | `ODD`            |

---

## `torchflowerDropSeeds` — Torchflower Seed Drops

When a torchflower is broken, it additionally drops **1–3 torchflower seeds** on top of the normal vanilla drops.

| Property | Value            |
| -------- | ---------------- |
| Type     | `boolean`        |
| Default  | `false`          |
| Options  | `false` / `true` |
| Category | `ODD`            |

---

## `cactusOxidizeCopper` — Cactus Oxidizes Copper

Right-click an incompletely oxidized copper block or item while holding a cactus to advance it to the next oxidation stage.

| Property | Value            |
| -------- | ---------------- |
| Type     | `boolean`        |
| Default  | `false`          |
| Options  | `false` / `true` |
| Category | `ODD`            |

---

## `batchPlayerCommand` — Batch Player Command

Enables the `/playerManager batch <prefix> <start> <end> <action>` command.

Batch-generates and kills dummy players using the `prefix_number` naming pattern, and controls them using Carpet action packs. A single batch can operate on up to **256 dummy players**.

| Property | Value            |
| -------- | ---------------- |
| Type     | `boolean`        |
| Default  | `false`          |
| Options  | `false` / `true` |
| Category | `ODD`            |

---

## `playerInventoryStack` — Custom Player Inventory Stack Size

Allows `/playerInventoryStack` to customize the maximum stack size of items in the player's inventory.

Dropped items and other containers retain their vanilla behavior.

**Subcommands:**

```text
set <item predicate> <amount 1-99>
set filled_shulker_box <amount 1-99>
remove "<predicate>"
list
clear
```

| Property | Value            |
| -------- | ---------------- |
| Type     | `boolean`        |
| Default  | `false`          |
| Options  | `false` / `true` |
| Category | `ODD`            |
|          |                  |
