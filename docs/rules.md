# Carpet Odd Addition 规则

> **提示：** 可以使用 `Ctrl + F` 快速查找规则。

所有规则均属于 **ODD** 分类，使用以下命令开启或关闭：

```text
/carpet <规则名> <true|false>
```

---

## `autoDrop` — 假人纯盒投掷

扫描假人背包中的潜影盒（包括染色潜影盒）。当盒内 27 个槽位仅包含同一种物品时，将该潜影盒从假人背包中扔出。

**触发方式：**

* `/player <name> autodrop`
* 手持仙人掌左键点击假人

| 属性  | 值                |
| --- | ---------------- |
| 类型  | `boolean`        |
| 默认值 | `false`          |
| 可选值 | `false` / `true` |
| 分类  | `ODD`            |

---

## `bonemealSporeBlossom` — 骨粉催熟孢子花

对已放置的孢子花使用骨粉，会弹出孢子花物品，并在附近符合条件的天花板方块上随机生成新的孢子花。

| 属性  | 值                |
| --- | ---------------- |
| 类型  | `boolean`        |
| 默认值 | `false`          |
| 可选值 | `false` / `true` |
| 分类  | `ODD`            |

---

## `torchflowerDropSeeds` — 火把花掉落种子

破坏火把花时，在原版掉落的基础上额外掉落 **1～3 个火把花种子**。

| 属性  | 值                |
| --- | ---------------- |
| 类型  | `boolean`        |
| 默认值 | `false`          |
| 可选值 | `false` / `true` |
| 分类  | `ODD`            |

---

## `cactusOxidizeCopper` — 仙人掌氧化铜

手持仙人掌右键点击未完全氧化的铜制品，使其氧化到下一个阶段。

| 属性  | 值                |
| --- | ---------------- |
| 类型  | `boolean`        |
| 默认值 | `false`          |
| 可选值 | `false` / `true` |
| 分类  | `ODD`            |

---

## `batchPlayerCommand` — 批量假人命令

启用 `/playerManager batch <前缀> <起始> <结束> <操作>` 命令。

按照 `前缀_序号` 的命名规则批量生成、杀死假人，并使用地毯动作包控制假人。单批最多操作 **256 个假人**。

| 属性  | 值                |
| --- | ---------------- |
| 类型  | `boolean`        |
| 默认值 | `false`          |
| 可选值 | `false` / `true` |
| 分类  | `ODD`            |

---

## `playerInventoryStack` — 玩家背包自定义堆叠

允许使用 `/playerInventoryStack` 自定义物品在玩家背包中的最大堆叠数量。

地面掉落物和其他容器仍保持原版行为。

**子命令：**

```text
set <物品谓词> <数量1-99>
set filled_shulker_box <数量1-99>
remove "<谓词>"
list
clear
```

| 属性  | 值                |
| --- | ---------------- |
| 类型  | `boolean`        |
| 默认值 | `false`          |
| 可选值 | `false` / `true` |
| 分类  | `ODD`            |
