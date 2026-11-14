# Jade 1.12.2 移植复查报告

> 对比基准：`Jade-1.20.3-fabric`（实际标注 1.20.4）↔ `jade1.12.2`（Ornithe / Fabric Loom 1.18.4 / OSL）
> 复查日期：2026-11-14
> 构建：`./gradlew build --offline` → `build/libs/Jade-1.0.0+mc1.12.2.jar`

---

## 1. 总览

| 指标 | 原版 1.20.4 | 移植版 1.12.2 |
|---|---|---|
| Java 文件 | 213 | 181 |
| 代码行数 | 18,422 | 16,207 |
| addon 文件 | vanilla 37 / core 5 / debug 3 / harvest 4 / universal 11 | vanilla 29 / core 5 / debug 3 / harvest 4 / universal 5 |
| mixin | 9 | 13（多了 4 个 1.12 专用访问器，少了 7 个 Fabric 专用钩子）|
| 语言文件 | 11 | 11（内容相同，占位符已从 `%d` 改为 `%s`）|
| 内置主题 | 4 | 4（`dark` / `create` / `top` / `waila`，**逐字节相同**）|
| 贴图 | 2 个进度条贴图 | **逐字节相同** |
| 注册项（`registration.*`）| — | vanilla 110 / core 20 / debug 10 |

结论：**核心 API、渲染管线、配置系统、主题系统全部到位**；缺口集中在「1.12 没有对应概念」的
流体/能量/数据包/命令/1.13+ 方块这几类。

---

## 2. 缺失功能完整清单

### 2.1 平台能力缺失（1.12 根本没有，属于不可移植）

| 文件 | 原因 |
|---|---|
| `api/fluid/JadeFluidObject` | 1.12 没有流体注册表/流体栈 |
| `api/view/FluidView`、`EnergyView`、`ProgressView` | 依赖流体/能量/进度视图 API |
| `impl/ui/FluidStackElement` | 同上 |
| `util/FluidTextHelper` | 同上 |
| `util/JadeFabricUtils` | 封装 Fabric Transfer API |
| `util/TechRebornEnergyCompat` | 封装能量 API |
| `addon/universal/FluidStorageProvider` + `EntityFluidStorageProvider` | 同上 |
| `addon/universal/EnergyStorageProvider` + `EntityEnergyStorageProvider` | 同上 |
| `addon/universal/ProgressProvider` + `EntityProgressProvider` | 依赖 `ProgressView` 视图体系 |
| `impl/ui/TooltipComponentsElement` | 1.20 的 `TooltipComponent` 体系 |
| `impl/theme/ThemeCodecs` | DFU Codec；移植版改成手写 JSON 解析 |
| `gui/config/BelowOrAboveListEntryTooltipPositioner` | 1.20 的屏幕/提示框定位 API |
| `mixin/FontMixin`、`StringRenderOutputMixin`、`FullTextSearchTreeMixin`、`KeyAccess` | 1.20 字体/搜索树/按键 API 内部实现 |
| `mixin/ThemeHelperMixin`、`HarvestToolProviderMixin` | Fabric 资源重载监听器注册；1.12 改用 OSL 事件 |
| `mixin/AbstractHorseAccess` | 由 `mixin/HorseBaseEntityAccess` 代替 |
| `util/CodecJsonSerializer`、`DumpGenerator` | 开发期工具 |

### 2.2 1.13+ 游戏内容（对应方块在 1.12 不存在）

`BeehiveProvider`（1.15）、`CampfireProvider`（1.14）、`LecternProvider`（1.14）、
`WaxedProvider`（1.17）、`ChiseledBookshelfProvider`（1.20）、`BlockDisplayProvider` /
`ItemDisplayProvider`（1.19.4）、`MobSpawnerCooldownProvider`（1.21）。

### 2.3 主动裁剪

| 文件 | 说明 |
|---|---|
| `command/*`（2 个） | Jade 的 `/jade` 客户端/服务端命令，未移植 |
| `compat/JEICompat`、`REICompat` | 1.12 没有 REI，JEI 兼容未做 |
| `overlay/DatapackBlockManager` | 数据包伪装方块，未移植 |
| `test/*`（11 个） | 按要求排除 |

### 2.4 新增文件（移植版独有，18 个）

| 文件 | 用途 |
|---|---|
| `util/RgbText` | 绕过 1.12 `Style` 只有 16 色的限制，实现任意 RGB 文字 |
| `util/Networking` | OSL 网络通道封装 |
| `api/ui/Rect2i` | 1.12 没有 `org.joml` 的矩形类 |
| `gui/config/widget/{JadeWidget,JadeButton,JadeTextField,CycleButtonWidget}` | 1.12 屏幕控件基类 |
| `mixin/TextRendererMixin`、`ChatScreenMixin`、`GameGuiMixin`、`ItemStackMixin`、`ReloadableIdSearchTreeMixin` | 1.12 渲染/搜索树适配 |
| `mixin/PlayerTabOverlayAccess`、`BossEventOverlayAccess` | 玩家列表/Boss 栏访问器 |
| `mixin/HorseBaseEntityAccess`、`SpawnerMinecartEntityAccess`、`ZombieVillagerEntityAccess` | 1.12 私有字段访问器（马背包、刷怪笼矿车、僵尸村民转化时间）|
| `mixin/ClientPlayerInteractionManagerAccess` | 破坏进度访问器 |

---

## 3. API 差异

### 3.1 完全一致（方法集合与语义）

`Accessor`、`AccessorImpl`、`BlockAccessor`、`EntityAccessor`、`AccessorClientHandler`、
`IBlockComponentProvider`、`IEntityComponentProvider`、`IServerDataProvider`、`IToggleableProvider`、
`IJadeProvider`、`IWailaPlugin`、`WailaPlugin`、`TooltipPosition`、`ITooltip`、`IElement`、
`IElementHelper`（除流体）、`IBoxElement`、`ITextElement`、`ProgressStyle`、`BoxStyle`、
`ColorPalette`、`Theme`、`TextSetting`、`IThemeHelper`、`IWailaConfig`、`IPluginConfig`、
`IWailaClientRegistration`、`IWailaCommonRegistration`、`CallbackContainer` 及各回调接口。

### 3.2 仅类型改名（结构 1:1）

| 1.20.4 | 1.12.2 |
|---|---|
| `ResourceLocation` | `Identifier` |
| `CompoundTag` / `ListTag` / `Tag` | `NbtCompound` / `NbtList` / `NbtElement` |
| `Component` / `MutableComponent` | `Text` / `BaseText` |
| `Style`（含 RGB `TextColor`）| `Style`（仅 16 色）|
| `Level` / `ServerLevel` | `World` |
| `Player` / `ServerPlayer` | `PlayerEntity` / `ServerPlayerEntity` |
| `Container` | `Inventory` |
| `BlockHitResult` / `EntityHitResult` | 统一的 `HitResult` |
| `FriendlyByteBuf` | `PacketBuffer`（OSL）|
| `GuiGraphics` | 直接 `GlStateManager` + `Tessellator` 绘制 |
| `record` | 普通类（手写 `equals`/`hashCode`）|
| `Codec`（DFU）| 手写 JSON 解析 |

### 3.3 删除 / 新增的 API 成员

| 成员 | 状态 | 影响 |
|---|---|---|
| `IElementHelper#fluid(...)` | 删除 | 1.12 无流体，第三方若调用会编译失败 |
| `api/view/{Energy,Fluid,Progress}View` | 删除 | 同上 |
| `api/fluid/JadeFluidObject` | 删除 | 同上 |
| `IWailaConfig.FluidMode` | 只剩 `NONE` / `ANY` | `SOURCE_ONLY` 删除（1.12 射线打不到流动流体）|
| `ThemeHelper#colorStyle(int)` | 改成 `color(Text, int)` | 1.20 的 `Style.EMPTY.withColor(int)` 在 1.12 无法表达；改为返回带精确颜色的 `RgbText` |
| `ThemeHelper#color(Object, int)` | 语义不变 | 内部转调上面的方法 |
| `api/ui/Rect2i` | 新增 | 1.12 缺少 joml |
| `util/RgbText` | 新增 | 任意 RGB 文字 |
| `util/Networking` | 新增 | OSL 通道封装 |

> 换言之：**插件作者面向的 API 形状基本没变**，只有流体/能量/进度这三块整体缺失。

---

## 4. 已知行为差异（1.12 平台限制，不可消除）

| # | 差异 | 说明 |
|---|---|---|
| 1 | **文字颜色** | 1.12 `Style` 只能存 16 个 `Formatting` 颜色。移植版新增 `RgbText`，由 `DisplayHelper#drawText` 遍历文本树把精确 ARGB 交给 `TextRenderer#draw`，主题色/坐标色均为真彩色；只在「第三方只读 `Style`」的路径退化为 16 色 |
| 2 | **流体** | `LiquidBlock#canRayTrace` 只接受 `LEVEL == 0`，`World#rayTrace` 永远拿不到流动流体；不做自建流体射线，因此流体设置只剩 ON/OFF，且只有流体源会被命中 |
| 3 | **主题发现** | 1.12 `ResourceManager` 没有目录遍历，只能读内置的 `dark/create/top/waila` 四个主题；资源包里的自定义主题不会被自动发现（可通过把 json 放进 jar 的方式扩展）|
| 4 | **九宫格贴图** | 1.20 的 GUI Sprite / NineSlice 缩放 1.12 没有；主题里的 `iconSlotSprite` 用普通贴图平铺近似，`BoxStyle.getSprite(...)` 的 padding 按原样使用 |
| 5 | **进度条贴图** | 贴图文件本身与原版逐字节相同，但 1.20 的 nine-slice 拉伸在 1.12 用 `drawTexture` + 平铺实现，极窄/极宽时观感略有差异 |
| 6 | **复述（TTS）** | 1.12 的 `text2speech` 1.10.3 只有英文音色、只有 `say/clear/active`；`Narrator#getNarrator()` 每次返回新实例，移植版做了 memoize |
| 7 | **物品数量颜色** | 1.20 的 `renderGuiItemDecorations` 是自绘的，移植版复刻了 `renderAmount`（缩放 0.75/0.5、主题 `itemAmountColor`、带阴影）|
| 8 | **数据包伪装** | `DatapackBlockManager` 未移植；`builtInCamouflage`（虫蚀方块/陷阱箱）已实现 |
| 9 | **命令** | `/jade` 命令未移植 |
| 10 | **JEI/REI** | 未做兼容 |
| 11 | **进度视图** | `ProgressView` / `ProgressProvider` 未移植（1.12 没有对应场景）|

---

## 5. 已修复的 Bug（按类别）

### 5.1 数据同步链路（最严重）

| Bug | 现象 | 修复 |
|---|---|---|
| 服务端 `accessor.getServerData()` 是**副本** | 物品存储永远不显示 | 按原版重写 `BlockAccessorImpl` / `EntityAccessorImpl`，补回 `SyncData#unpack`：构造 accessor 不传 `serverData`，让 `AccessorImpl` 自己 new 一个，provider 写入的就是最终发出去的那个 tag |

### 5.2 1.12 语义差异导致的 Bug

| Bug | 现象 |
|---|---|
| `ItemCollector.ItemDefinition` 丢 metadata | 箱子里金合欢木头显示成橡木（1.12 所有木头共用一个 item，靠 metadata 区分）|
| 方块图标兜底 / 掉落方块图标丢 metadata | 同类问题 |
| `NoteBlockProvider` 乐器判定顺序 | 音符盒下面垫骨块显示成「贝斯鼓」而非「木琴」|
| `NoteBlockProvider` 音高不同步 | 调音后仍显示旧音高（1.12 音符存在方块实体且不发送更新，改为走服务端数据）|
| `CropProgressProvider` 缺作物 | 胡萝卜/马铃薯/可可豆没有生长进度 |
| `HorseStatsProvider` 最大速度常量 | 用了 0.225（默认基础速度）而不是 0.3375（1.12 随机上限）|
| `BlockAccessorClientHandler` 兜底判断 | `Item.byBlock(...) != null` 在 1.12 恒真 |
| 缺液体图标 | 水/岩浆没有图标（补了桶图标近似）|
| `ObjectNameProvider` 缺「大箱子」名字 | 双箱子显示 "Chest" 而不是 "Large Chest" |
| `ObjectNameProvider` 缺 `shouldPick` 分支 | 可拾取实体的名字不对 |
| `CommonProxy#isMultipartEntity` 语义相反 | `instanceof EnderDragonPart` vs 原版 `instanceof EnderDragon` |
| 上锁容器 | 1.12 有 `InventoryLock`，之前完全没处理 |
| `HarvestToolProvider` 工具匹配 | 用 `canMineBlock`（1.12 是特例表）导致斧/锹永远不显示 |

### 5.3 文本 / 语言

| Bug | 现象 |
|---|---|
| `%d` 占位符 | 1.12 `Locale#translate` 即使无参也会 `String.format`，`%d` 键返回 `Format error: ...`；`TranslatableText` 更是直接抛 `TranslationException`。11 个语言文件里的 57 处 `%d` 全部改为 `%s` |
| `jade.error` 无参调用 | 异常提示永远显示 `Format error: ...` |
| 16 处把带颜色文本拍平 | `getFormattedString()` 退化成 16 色 `§` 码，丢失 `RgbText` 精确颜色；改为 `TranslatableText(key, text)` |
| `itemAmountColor` 未使用 | 物品数量根本不渲染；补了 `DisplayHelper#renderAmount` |
| 非 `%s` 占位符审计 | 脚本全量检查，现为 0 |

### 5.4 GUI / 配置

选项列表行高、搜索框不刷新、Home 文字无阴影、按钮文字颜色、Reset 按钮文案、
雪花粒子透明度、行首 +2 / 导航滚动条 114、`setScrollAmount` 未钳制、`☰` 缺 tooltip、
主题数组长度未校验、`changeOpacity` 未钳制、`modName` 缺缓存、图标槽尺寸缓存不失效。

### 5.5 其他

配置文件里写错 id 导致启动崩溃（`new Identifier` 抛 `InvalidIdentifierException`）、
配置/主题/用户名缓存未用 UTF-8、`DataPack` 主题校验、`UsernameCache` 全量补齐。

---

## 6. 剩余风险 / 建议人工验证

| 区域 | 建议验证内容 |
|---|---|
| 物品存储 | 混放不同 metadata 的木板/原木/羊毛；大箱子名称；上锁箱子（`/setblock ... {Lock:"x"}`）；马/驴背包；战利品箱 |
| 流体 | 只看流体源（1.12 限制）；流体开关为 ON/OFF |
| 主题 | 四个内置主题切换、`changeOpacity` / `changeRoundCorner` 生效、`itemAmountColor` |
| 颜色 | 距离坐标（三段不同颜色）、主题自定义颜色、`info/success/danger` 文字 |
| 语言 | 中文环境下中继器/比较器/马详情/刷怪笼等 `%s` 文案 |
| GUI | 选项列表滚动、搜索、按钮状态 |
| 渲染 | 大量 tooltip 时的 GL 状态、物品图标深度、进度条贴图 |

---

## 7. 构建与自检方法

```bash
# 构建（需要 Java 25 运行 Gradle）
JAVA_HOME='C:\Users\Royan\.jdks\azul-25.0.4.1' ./gradlew build --offline
# 产物
build/libs/Jade-1.0.0+mc1.12.2.jar
```

本次复查使用的静态检查脚本（可复现）：

1. **文件清单对比**：`comm -23/-13` 比较两边的 `.java` 路径
2. **API 方法对比**：正则抽取两边 `api/` 下所有类的公开方法名，做差集
3. **翻译键审计**：
   - 找出「无参数 `I18n.translate(key)` 但键里有占位符」→ 会 Format error
   - 统计「占位符数量 vs 实参数量」→ 不匹配会吞参数
   - 找出「非 `%s` 占位符」→ `TranslatableText` 抛异常
4. **NBT 键审计**：每个 provider 的 `appendServerData` 写入键 vs `appendTooltip` 读取键
5. **注册类 vs 强转类**：确认每个 provider 注册的类与 `appendTooltip` 里的强转一致
6. **`state.get(...)` 守卫检查**：确认每个属性访问都有 `instanceof` / 方块判断保护

---

## 8. 方法级统计（脚本自动比对）

比对方式：抽取两边**同路径文件**中所有带修饰符的方法声明（排除构造器与关键字），
按「方法名 + 参数个数」比对。

| 指标 | 数值 |
|---|---|
| 共同文件 | 163 |
| 原版方法总数 | 912 |
| 移植版方法总数 | 926 |
| 两边都有的方法 | 795（87%）|
| **缺失方法** | 117 |
| **新增方法** | 131 |
| 签名不同（参数个数变化）| 58 |

### 8.1 缺失方法分类（117）

| 类别 | 数量（约）| 说明 |
|---|---|---|
| 1.20 屏幕/控件 API 重写 | ~55 | `OptionsList#renderItem/narratables/updateNarration`、`NotUglyEditBox#setFormatter/getWordPosition/...`、`BaseOptionsScreen#mouseScrolled/onClose` 等 —— 1.12 的 GUI 基类完全不同 |
| 1.20 渲染 API | ~18 | `DisplayHelper#renderGuiItemDecorations`（→ `renderAmount`）、`drawTiledSprite`、`drawFluid`、`enableBetterTextShadow`、`drawTextureWithMasking`、`BoxStyle#blitNineSlicedSprite` 等 |
| 流体 / 能量 / 进度 | ~12 | `registerFluidStorage*`、`registerEnergyStorage*`、`registerProgress*`、`ElementHelper#fluid`、`CommonProxy#getFluidName/wrapFluidStorage/wrapEnergyStorage`、`getFluidSpriteAndColor` |
| 生命周期钩子（Fabric → OSL）| ~12 | `ClientProxy#onInitializeClient/registerKeyBinding/registerReloadListener/onEntityJoin/onPlayerLeave`、`CommonProxy#onInitialize/playerJoin/registerServerCommand` |
| 命令 | 3 | `registerClientCommand` / `registerServerCommand` |
| 已用其他方式实现 | ~10 | `JadeClient#builtInOverrides`（**本轮补上**）、`createSwordToolHandler`（内联）、`getProfressionName`（内联职业表）、`RayTracing#getEntityHitResult`（改名 `getEntityHit`）、`OverlayRenderer#renderOverlay`（改名 `renderOverlay478757`）、`HarvestToolProvider#onResourceManagerReload`（OSL 事件）、`getBoundKeyOf`（直接用 `KeyBinding#getKeyCode`）|
| 1.20 独有 API | ~7 | `TextSetting`/`ColorPalette`/`PluginConfig.Category` 的 record 构造器、`SyncData` 的 4 参 record 构造器、`SimpleProgressStyle#RGBtoHSV`（→ `brightness`）、`ThemeHelper#apply` |

### 8.2 新增方法分类（131）

| 类别 | 数量（约）| 说明 |
|---|---|---|
| 1.12 控件 / 屏幕 API | ~45 | `BaseOptionsScreen#renderHoverTooltips/drawTooltipAt/handleMouse`、`KeybindOptionButton#setPos/getX`、`JadeWidget` 系列 |
| 构造器误报（`new LiteralText(...)` 等）| ~25 | 脚本把 `LiteralText[1]`、`TranslatableText[2]` 当成方法名 |
| 1.12 语义辅助 | ~20 | `SimpleToolHandler#isEffective`、`HarvestToolProvider#clearCache`、`NoteBlockProvider#getInstrument/appendServerData`、`ItemBERProvider#addSkullOwner`、`BlockStatesProvider#getValue`、`JadeClient#builtInOverrides`（本轮新增）|
| 手写 JSON 解析 | 4 | `BoxStyle#fromJson/readIntArray/readFloatArray/getColor`（替代 DFU Codec）|
| 1.12 渲染辅助 | 3 | `IDisplayHelper#drawTexture`、`drawGradientProgress`、`DisplayHelper#renderAmount` |
| record → 类 | 11 | `ColorPalette`/`TextSetting` 的 7 个访问器、`ItemDefinition#equals/hashCode`、`Element#getCachedMessage` |
| 1.12 平台类 | 3 | `RgbText`、`Networking`、`Rect2i` |

### 8.3 签名变化分类（58）

| 原因 | 数量（约）| 例子 |
|---|---|---|
| 去掉 `GuiGraphics` / `PoseStack` / `Font` 参数 | ~42 | `IElement#render(guiGraphics, ...)` → `render(...)`、`IDisplayHelper#drawText` 等 |
| 去掉 `int modifiers`（1.12 输入 API）| ~8 | `keyPressed` / `mouseScrolled` / `mouseDragged` |
| record 构造器差异 | ~4 | `SyncData`、`TextSetting` |
| 有意变化 | 2 | `ItemDefinition` 多一个 `metadata`（1.12 靠 metadata 区分木头）；`RayTracing#wrapBlock` 去掉 `CollisionContext` |
| 1.12 额外重载 | 2 | `BoxStyle.GradientBorder(2)`、`SpriteBase(2)` |

### 8.4 本轮因统计查出的 Bug

**`builtInOverrides`（内置伪装）完全缺失**：

- 配置项 `general.builtinCamouflage` 存在，但**没有任何代码使用它**
- 原版在 `VanillaPlugin.registerClient` 里注册了 `registration.addRayTraceCallback(-10, JadeClient::builtInOverrides)`
- 缺失导致：**陷阱箱显示成陷阱箱**（而不是箱子）、**虫蚀方块显示成虫蚀方块**（而不是石头/圆石/石砖）

已按原版补回：

```java
// JadeClient
@Nullable
public static Accessor<?> builtInOverrides(HitResult hitResult, @Nullable Accessor<?> accessor, @Nullable Accessor<?> originalAccessor) {
    if (!IWailaConfig.get().getGeneral().getBuiltinCamouflage() || !(accessor instanceof BlockAccessor)) {
        return accessor;
    }
    BlockAccessor target = (BlockAccessor) accessor;
    PlayerEntity player = accessor.getPlayer();
    if (player != null && (player.abilities.creativeMode || player.isSpectator())) {
        return accessor;
    }
    if (target.getBlock() == Blocks.TRAPPED_CHEST) {
        BlockState state = VanillaPlugin.getCorrespondingNormalChest(target.getBlockState());
        if (state != target.getBlockState()) {
            return WailaClientRegistration.instance().blockAccessor().from(target).blockState(state).build();
        }
    } else if (target.getBlock() == Blocks.MONSTER_EGG) {
        //port: 1.12 stores the host block state right in the variant property
        BlockState state = target.getBlockState().get(InfestedBlock.VARIANT).getHostState();
        return WailaClientRegistration.instance().blockAccessor().from(target).blockState(state).blockEntity(() -> null).build();
    }
    return accessor;
}

// VanillaPlugin
public static BlockState getCorrespondingNormalChest(BlockState state) {
    //port: 1.20 has a CHEST_TYPE property, 1.12 only has FACING
    return Blocks.CHEST.defaultState().set(ChestBlock.FACING, state.get(ChestBlock.FACING));
}
```

并在 `VanillaPlugin#registerClient` 里恢复注册：

```java
registration.addRayTraceCallback(-10, snownee.jade.JadeClient::builtInOverrides);
```

> 1.12 没有粉末雪（1.17）和可刷方块（1.20），原版那两个分支不需要移植。

### 8.5 统计脚本

```python
# 关键逻辑：按「方法名 + 参数个数」比对同路径文件
METHOD = re.compile(r'^[ \t]*(?:(?:public|protected|private|static|final|abstract|...)\s+)+'
                    r'(?:<[^>{};]+>\s*)?([A-Za-z_][\w$.<>\[\], ?]*?)\s+([A-Za-z_]\w*)\s*\(([^)]*)\)', re.M)
```

---

## 9. 键位修改不生效（需重启）

### 现象

在 Jade 的设置界面里改键位，按钮上显示新键，但**按新键没反应**，重启游戏后才生效。

### 根因

1.12 的 `KeyBinding` 用一个**静态映射表**把「键码 → 键位」缓存起来：

```java
private static final Int2ObjectHashMap<KeyBinding> BY_KEY_CODE = new Int2ObjectHashMap<>();

public static void set(int keyCode, boolean pressed) {
    KeyBinding keyBinding = BY_KEY_CODE.get(keyCode);   // ← 键盘事件走这里
    if (keyBinding != null) keyBinding.setPressed(pressed);
}

public static void resetMapping() {
    BY_KEY_CODE.clear();
    for (KeyBinding keyBinding : ALL.values()) {
        BY_KEY_CODE.put(keyBinding.keyCode, keyBinding);
    }
}
```

而 `GameOptions#setKeyCode` **只改实例字段并保存 options.txt**：

```java
public void setKeyCode(KeyBinding keyBinding, int code) {
    keyBinding.setKeyCode(code);
    this.save();
}
```

没有重建 `BY_KEY_CODE` —— 所以新键码在表里查不到，老键码仍指向这个键位。
重启时 `GameOptions#load()` 会重建映射表，于是「重启后才生效」。

原版 1.12 的控制界面每次改键都会调 `KeyBinding.resetMapping()`：
`ControlsListWidget:159`、`ControlsOptionsScreen:66/78`。

### 修复（3 处）

| 位置 | 场景 | 修复 |
|---|---|---|
| `gui/config/OptionsList#keyPressed` | 实际改键 | 在 `options.setKeyCode(...)` 之后补 `KeyBinding.resetMapping()` |
| `gui/WailaConfigScreen` 的 `canceller` | 关闭界面不保存、回滚键位 | 回滚后补 `KeyBinding.resetMapping()` |
| `gui/WailaConfigScreen` 的「重置设置」 | 恢复默认键位 | 恢复后补 `KeyBinding.resetMapping()` |

```java
GameOptions options = Minecraft.getInstance().options;
options.setKeyCode(selectedKey, keyCode == 1 ? Keyboard.KEY_NONE : keyCode);
//port: 1.12 caches keyCode -> KeyBinding in a static map, vanilla rebuilds it in the controls
//screen too; without this the new key only works after a restart
KeyBinding.resetMapping();
selectedKey = null;
resetMappingAndUpdateButtons();
```

`GameOptions#setKeyCode` 内部已经调用了 `save()`，所以 `options.txt` 的持久化本来就是对的，
唯一缺的就是内存里的映射表刷新。

