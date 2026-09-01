# Carpet Odd Addition 规则

提示：可以使用Ctrl+F快速查找自己想要的规则

所有规则均属于 ODD 分类，使用 `/carpet <规则名> <true|false>` 开启或关闭。

---

## 假人纯盒投掷 (autoDrop)
扫描假人背包中的潜影盒（含染色潜影盒），当盒内27个槽位只有同一种物品时，将该盒子从假人背包中扔出。
可以通过命令 `/player <name> autodrop` 触发，也可以手持仙人掌左键点击假人触发。

类型: boolean
默认值: false
参考选项: false, true
分类: ODD

---

## 骨粉催熟孢子花 (bonemealSporeBlossom)
对已放置的孢子花使用骨粉，会弹出孢子花物品并在附近符合条件的天花板方块上随机生成新的孢子花，使原版不可再生的孢子花能够自动化繁殖。消耗1个骨粉，创造模式不消耗。

类型: boolean
默认值: false
参考选项: false, true
分类: ODD

---

## 火把花掉落种子 (torchflowerDropSeeds)
破坏火把花时，在原版掉落的基础上额外掉落1~3个火把花种子。

类型: boolean
默认值: false
参考选项: false, true
分类: ODD

---

## 仙人掌氧化铜 (cactusOxidizeCopper)
手持仙人掌右键点击未完全氧化的铜制品，会使其氧化到下一个阶段，与斧头右键除锈相反。不消耗仙人掌，也不消耗耐久。铜块已完全氧化时，右键恢复为原版行为（正常放置仙人掌）。

类型: boolean
默认值: false
参考选项: false, true
分类: ODD

---

## 批量假人命令 (batchPlayerCommand)
启用 `/playerManager batch <前缀> <起始> <结束> <操作>` 命令，按 `前缀_序号` 命名规则批量生成（单批上限256个）、杀死和使用地毯动作包控制假人。支持 spawn、kill、use、attack、jump、drop、drop_stack、swap_hands、move、sneak、unsneak、sprint、unsprint、look、turn、hotbar、mount、dismount、stop 等操作。其中 perTick 与 randomly 模式需要安装 Carpet TIS Addition。

类型: boolean
默认值: false
参考选项: false, true
分类: ODD

---

## 玩家背包自定义堆叠 (playerInventoryStack)
允许使用 `/playerInventoryStack` 自定义物品在玩家背包内的最大堆叠数量，地面掉落物和其他容器保持原版行为。
子命令：`set <物品谓词> <数量1-99>`、`set filled_shulker_box <数量1-99>`（所有槽位均达到原版最大堆叠数量的潜影盒）、`remove "<谓词>"`、`list`、`clear`。
物品谓词支持NBT/组件匹配，如 `minecraft:potion[minecraft:potion_contents=potion_type:healing]`。

类型: boolean
默认值: false
参考选项: false, true
分类: ODD
