# Jade 1.12.2 移植审查报告

对照源：`Jade-1.20.3-fabric`（实际 1.20.4 代码）
移植目标：`jade1.12.2`（Minecraft 1.12.2 / Ornithe Fabric / OSL 0.21.0 / Java 8）
审查方式：文件集合对比、行数/注释/方法签名结构对比、核心文件逐方法 diff、1.12 API 可用性核对、clean build 验证。

## 1. 总体结论

- 原版未排除包共 **145** 个 Java 文件，移植版对应 **119** 个，另有 **44** 个 1.12 专用/移植新增文件。
- 已配对文件中，原版注释行 **579**，移植版 **549**，保留率约 **95%**；API 接口的主要 Javadoc 已补回。
- 发现并修复 **32 类实际逻辑/效果问题**，其中影响最明显的几类：
  1. 物品 tooltip 不显示堆叠数量；
  2. tooltip/overlay/进度条动画速度不正确；
  3. 语言文件 `{0}` 占位符不生效（繁殖/成长/下蛋倒计时全部显示成字面量）；
  4. 主题颜色映射错误（先按索引、后按 RGB 距离都不对），info/success/warning/danger/title/坐标颜色全部偏色；
  5. 主题关闭文字阴影时颜色被一起剥离，彩色文字变纯白；
  6. `hide-entities.json` 实体黑名单从未生效；
  7. 配置列表行高错位 2px（悬停条比文字高）；
  8. 搜索框“×”清空后列表不刷新；
  9. 首页与配置界面文字缺少阴影、按钮无悬停高亮。
- 仍有若干**功能性缺口**（流体视图、TTS、数据包伪装、1.12 不存在的内容等），见第 5 节。
- 当前 clean build 成功。

## 2. 包级统计

| 包 | 已配对文件 |
|---|---:|
| `api` | 44 |
| `impl` | 35 |
| `gui` | 15 |
| `addon`（已移植部分） | 8 |
| `util` | 7 |
| `overlay` | 5 |
| `mixin` | 2 |
| 根类（`Jade`、`JadeClient`、`Internals`） | 3 |

## 3. 文件级对照（119 个已配对文件）

> 列说明：`原/移行数` 为源码行数；`注释 原/移` 为注释行数；`缺失方法` 为原版 public/protected/private 方法名在移植版中缺失的数量（很多是 1.20 API 或未移植功能导致）。

| 文件 | 原/移行数 | 注释 原/移 | 缺失方法 | 结论 / 差异 |
|---|---:|---:|---:|---|
| `Internals.java` | 28 / 28 | 0 / 0 | 0 | 对应基本一致 |
| `Jade.java` | 53 / 38 | 4 / 3 | 0 | 对应基本一致 |
| `JadeClient.java` | 298 / 204 | 0 / 0 | 1 | format 改 MessageFormat；模组名样式/去重、breaking 世界重置已修；mob effect fog 仍 no-op。 |
| `addon/core/BlockFaceProvider.java` | 44 / 45 | 0 / 0 | 0 | 对应基本一致 |
| `addon/core/CorePlugin.java` | 83 / 79 | 0 / 0 | 0 | hide-entities 之前未生效，已接上；默认隐藏 area_effect_cloud/fireworks_rocket。 |
| `addon/core/DistanceProvider.java` | 93 / 96 | 0 / 0 | 0 | 坐标改为 TranslatableText + 三色样式，与原版同构；浅色主题配色已修。 |
| `addon/core/ModNameProvider.java` | 58 / 51 | 0 / 0 | 0 | 对应基本一致 |
| `addon/core/ObjectNameProvider.java` | 130 / 102 | 0 / 1 | 0 | 对应基本一致 |
| `addon/debug/BlockStatesProvider.java` | 57 / 62 | 0 / 0 | 0 | 对应基本一致 |
| `addon/debug/DebugPlugin.java` | 30 / 33 | 0 / 0 | 0 | 对应基本一致 |
| `addon/debug/RegistryNameProvider.java` | 74 / 62 | 0 / 1 | 0 | 对应基本一致 |
| `api/Accessor.java` | 44 / 43 | 6 / 6 | 0 | 对应基本一致 |
| `api/AccessorClientHandler.java` | 18 / 18 | 0 / 0 | 0 | 对应基本一致 |
| `api/AccessorImpl.java` | 74 / 73 | 6 / 6 | 0 | 对应基本一致 |
| `api/BlockAccessor.java` | 76 / 75 | 6 / 6 | 0 | 对应基本一致 |
| `api/EntityAccessor.java` | 63 / 62 | 7 / 7 | 0 | 对应基本一致 |
| `api/IBlockComponentProvider.java` | 54 / 53 | 36 / 36 | 0 | 对应基本一致 |
| `api/IEntityComponentProvider.java` | 53 / 52 | 35 / 35 | 0 | 对应基本一致 |
| `api/IJadeProvider.java` | 22 / 22 | 9 / 9 | 0 | 对应基本一致 |
| `api/IServerDataProvider.java` | 15 / 15 | 7 / 7 | 0 | 对应基本一致 |
| `api/IToggleableProvider.java` | 16 / 16 | 3 / 3 | 0 | 对应基本一致 |
| `api/ITooltip.java` | 150 / 175 | 46 / 74 | 0 | API Javadoc 已补回并去重。 |
| `api/IWailaClientRegistration.java` | 204 / 197 | 47 / 67 | 0 | API Javadoc 已补回；仍缺 storage/progress/fluid 注册 API。 |
| `api/IWailaCommonRegistration.java` | 38 / 24 | 12 / 12 | 0 | 结构差异较大 |
| `api/IWailaPlugin.java` | 16 / 16 | 3 / 3 | 0 | 对应基本一致 |
| `api/Identifiers.java` | 91 / 91 | 0 / 0 | 0 | 对应基本一致 |
| `api/TooltipPosition.java` | 7 / 7 | 0 / 0 | 0 | 对应基本一致 |
| `api/WailaPlugin.java` | 21 / 21 | 7 / 7 | 0 | 对应基本一致 |
| `api/callback/JadeAfterRenderCallback.java` | 13 / 12 | 0 / 0 | 0 | 对应基本一致 |
| `api/callback/JadeBeforeRenderCallback.java` | 13 / 12 | 0 / 0 | 0 | 对应基本一致 |
| `api/callback/JadeBeforeTooltipCollectCallback.java` | 13 / 12 | 0 / 0 | 0 | 对应基本一致 |
| `api/callback/JadeItemModNameCallback.java` | 13 / 12 | 0 / 0 | 0 | 对应基本一致 |
| `api/callback/JadeRayTraceCallback.java` | 14 / 13 | 0 / 0 | 0 | 对应基本一致 |
| `api/callback/JadeTooltipCollectedCallback.java` | 11 / 11 | 0 / 0 | 0 | 对应基本一致 |
| `api/config/IPluginConfig.java` | 46 / 43 | 17 / 17 | 0 | 对应基本一致 |
| `api/config/IWailaConfig.java` | 197 / 184 | 0 / 0 | 0 | 对应基本一致 |
| `api/config/TargetBlocklist.java` | 9 / 10 | 0 / 0 | 0 | 对应基本一致 |
| `api/platform/CustomEnchantPower.java` | 12 / 11 | 0 / 0 | 0 | 对应基本一致 |
| `api/platform/PlatformWailaClientRegistration.java` | 9 / 9 | 0 / 0 | 0 | 对应基本一致 |
| `api/theme/IThemeHelper.java` | 46 / 45 | 0 / 0 | 0 | 对应基本一致 |
| `api/theme/TextSetting.java` | 16 / 39 | 0 / 0 | 1 | 缺方法 TextSetting；结构差异较大 |
| `api/theme/Theme.java` | 39 / 38 | 0 / 0 | 0 | 对应基本一致 |
| `api/ui/BoxStyle.java` | 237 / 227 | 0 / 0 | 2 | 缺方法 blitNineSlicedSprite, blitSprite |
| `api/ui/Color.java` | 446 / 443 | 165 / 172 | 0 | API Javadoc 已补回；1.12 增加默认色构造。 |
| `api/ui/ColorPalette.java` | 30 / 70 | 0 / 0 | 1 | 缺方法 ColorPalette；结构差异较大 |
| `api/ui/Direction2D.java` | 21 / 22 | 0 / 0 | 0 | 对应基本一致 |
| `api/ui/Element.java` | 92 / 92 | 5 / 5 | 0 | 对应基本一致 |
| `api/ui/IBoxElement.java` | 26 / 25 | 0 / 0 | 0 | 对应基本一致 |
| `api/ui/IDisplayHelper.java` | 49 / 40 | 0 / 0 | 0 | 对应基本一致 |
| `api/ui/IElement.java` | 76 / 73 | 29 / 28 | 0 | 对应基本一致 |
| `api/ui/IElementHelper.java` | 51 / 47 | 6 / 6 | 0 | 对应基本一致 |
| `api/ui/ITextElement.java` | 8 / 8 | 0 / 0 | 0 | 对应基本一致 |
| `api/ui/MessageType.java` | 13 / 13 | 0 / 0 | 0 | 对应基本一致 |
| `api/ui/ProgressStyle.java` | 59 / 57 | 0 / 0 | 0 | 对应基本一致 |
| `api/ui/TooltipRect.java` | 9 / 7 | 0 / 0 | 0 | 对应基本一致 |
| `gui/BaseOptionsScreen.java` | 169 / 282 | 0 / 0 | 4 | 保存/取消流程与原版一致；1.12 手工布局/tooltip。 |
| `gui/HomeConfigScreen.java` | 374 / 427 | 0 / 0 | 1 | 缺方法 onClose |
| `gui/JadeFont.java` | 8 / 9 | 0 / 0 | 0 | 对应基本一致 |
| `gui/PluginsConfigScreen.java` | 75 / 74 | 0 / 0 | 0 | 对应基本一致 |
| `gui/PreviewOptionsScreen.java` | 185 / 186 | 0 / 0 | 2 | 缺方法 keyReleased, mouseScrolled |
| `gui/WailaConfigScreen.java` | 188 / 193 | 5 / 0 | 0 | 原注释未保留 |
| `gui/config/KeybindOptionButton.java` | 35 / 124 | 0 / 0 | 0 | 结构差异较大 |
| `gui/config/NotUglyEditBox.java` | 547 / 257 | 0 / 0 | 37 | 基于 1.12 TextFieldWidget 重写，方法集差异大。 |
| `gui/config/OptionButton.java` | 34 / 34 | 0 / 0 | 0 | 对应基本一致 |
| `gui/config/OptionsList.java` | 540 / 732 | 0 / 3 | 9 | 手工 ListWidget 实现，结构差异大；搜索/保存态/滚动已对齐。 |
| `gui/config/OptionsNav.java` | 107 / 147 | 0 / 0 | 6 | 手工导航列表渲染；锚点/滚动一致。 |
| `gui/config/value/CycleOptionValue.java` | 26 / 56 | 0 / 0 | 0 | 结构差异较大 |
| `gui/config/value/InputOptionValue.java` | 70 / 70 | 1 / 1 | 0 | 对应基本一致 |
| `gui/config/value/OptionValue.java` | 103 / 98 | 0 / 0 | 1 | 缺方法 updateNarration |
| `gui/config/value/SliderOptionValue.java` | 77 / 140 | 2 / 0 | 1 | 缺方法 applyValue；原注释未保留；结构差异较大 |
| `impl/BlockAccessorClientHandler.java` | 95 / 87 | 0 / 0 | 0 | 对应基本一致 |
| `impl/BlockAccessorImpl.java` | 282 / 291 | 3 / 3 | 2 | SyncData 1.12 化；方块状态未传输、服务端用世界状态；fakeBlock/hit 点已传。 |
| `impl/CallbackContainer.java` | 32 / 32 | 0 / 0 | 0 | 对应基本一致 |
| `impl/EntityAccessorClientHandler.java` | 90 / 83 | 0 / 0 | 0 | 对应基本一致 |
| `impl/EntityAccessorImpl.java` | 219 / 225 | 3 / 3 | 2 | 服务端距离用 BlockPos 近似；part 实体逻辑保留。 |
| `impl/HierarchyLookup.java` | 105 / 97 | 0 / 0 | 0 | 对应基本一致 |
| `impl/ObjectDataCenter.java` | 83 / 82 | 0 / 0 | 0 | 对应基本一致 |
| `impl/PriorityStore.java` | 101 / 97 | 0 / 0 | 0 | 对应基本一致 |
| `impl/Tooltip.java` | 299 / 313 | 2 / 0 | 0 | 行对齐一致；debug border 已恢复；TTS/narration 未移植。 |
| `impl/WailaClientRegistration.java` | 413 / 364 | 4 / 0 | 4 | 缺 storage/progress/fluid 注册。 |
| `impl/WailaCommonRegistration.java` | 112 / 68 | 1 / 0 | 4 | 同上。 |
| `impl/config/PluginConfig.java` | 332 / 346 | 1 / 0 | 1 | 缺方法 Category；原注释未保留 |
| `impl/config/WailaConfig.java` | 427 / 430 | 5 / 3 | 0 | 对应基本一致 |
| `impl/config/entry/BooleanConfigEntry.java` | 24 / 24 | 0 / 0 | 0 | 对应基本一致 |
| `impl/config/entry/ConfigEntry.java` | 69 / 66 | 0 / 0 | 0 | 对应基本一致 |
| `impl/config/entry/EnumConfigEntry.java` | 52 / 51 | 0 / 0 | 0 | 对应基本一致 |
| `impl/config/entry/FloatConfigEntry.java` | 43 / 43 | 0 / 0 | 0 | 对应基本一致 |
| `impl/config/entry/IntConfigEntry.java` | 42 / 42 | 0 / 0 | 0 | 对应基本一致 |
| `impl/config/entry/StringConfigEntry.java` | 29 / 29 | 0 / 0 | 0 | 对应基本一致 |
| `impl/theme/ThemeHelper.java` | 209 / 333 | 0 / 8 | 1 | 颜色改为色相分带映射（幂等）；支持 modNameStyle；fallback 防递归；ThemeCodecs 缺失。 |
| `impl/ui/ArmorElement.java` | 86 / 96 | 0 / 0 | 0 | 对应基本一致 |
| `impl/ui/BoxElement.java` | 373 / 320 | 7 / 0 | 0 | 动画 delta 修正；rect 计算顺序对齐原版；debug border 恢复；无 fluid sprite/九宫格。 |
| `impl/ui/CompoundElement.java` | 34 / 30 | 0 / 0 | 0 | 对应基本一致 |
| `impl/ui/ElementHelper.java` | 119 / 97 | 5 / 0 | 2 | 缺 fluid/sub 元素。 |
| `impl/ui/HealthElement.java` | 94 / 102 | 0 / 0 | 0 | 心形/文本偏移与 narration 已对齐原版；1.12 用 icons.png。 |
| `impl/ui/HorizontalLineElement.java` | 33 / 32 | 0 / 0 | 0 | 对应基本一致 |
| `impl/ui/ItemStackElement.java` | 60 / 59 | 0 / 0 | 0 | drawItem 修复后物品数量恢复。 |
| `impl/ui/ProgressElement.java` | 85 / 78 | 0 / 0 | 0 | track tick 改为真实 frame delta。 |
| `impl/ui/SimpleProgressStyle.java` | 142 / 112 | 1 / 0 | 1 | RGBtoHSV 用 HSV V 等价实现。 |
| `impl/ui/SlimProgressStyle.java` | 44 / 43 | 0 / 0 | 0 | 对应基本一致 |
| `impl/ui/SpacerElement.java` | 23 / 22 | 0 / 0 | 0 | 对应基本一致 |
| `impl/ui/SpecialTextElement.java` | 55 / 50 | 0 / 0 | 0 | 对应基本一致 |
| `impl/ui/SpriteElement.java` | 31 / 31 | 0 / 0 | 0 | 对应基本一致 |
| `impl/ui/StyledElement.java` | 10 / 10 | 0 / 0 | 0 | 对应基本一致 |
| `impl/ui/TextElement.java` | 57 / 50 | 0 / 0 | 0 | 对应基本一致 |
| `mixin/BossHealthOverlayMixin.java` | 25 / 23 | 0 / 0 | 0 | 对应基本一致 |
| `mixin/ClientLevelMixin.java` | 21 / 23 | 0 / 0 | 0 | 对应基本一致 |
| `overlay/DisplayHelper.java` | 439 / 298 | 2 / 0 | 7 | 物品光照/数量、sprite UV 裁剪、drawText 颜色保留已修；缺 fluid/tiled/text shadow。 |
| `overlay/OverlayRenderer.java` | 215 / 208 | 10 / 10 | 1 | frame delta、HOLD_KEY、theme 空值、license 注释；lwjgl FQN 已清理。 |
| `overlay/ProgressTracker.java` | 96 / 95 | 0 / 0 | 0 | spd 除数 max(1) 防 0。 |
| `overlay/RayTracing.java` | 201 / 191 | 1 / 5 | 1 | AABB clip、实体命中点、流体模式、距离比较已重写。 |
| `overlay/WailaTickHandler.java` | 200 / 167 | 6 / 0 | 0 | theme null 校验已补；narration/TTS 为空实现。 |
| `util/ClientProxy.java` | 319 / 188 | 3 / 0 | 16 | 缺 fluid/recipe viewer/storage；新增 getDeltaFrameTime；补 ModIdentification 重载。 |
| `util/CommonProxy.java` | 460 / 296 | 0 / 0 | 10 | 缺 fluid/storage/command/username 下载；附魔书模组名已修。 |
| `util/JsonConfig.java` | 137 / 132 | 2 / 0 | 0 | 原注释未保留 |
| `util/ModIdentification.java` | 81 / 77 | 0 / 0 | 0 | FallingBlock 方块模组名已补；Painting motive 无法映射模组。 |
| `util/SmoothChasingValue.java` | 54 / 54 | 0 / 0 | 0 | 对应基本一致 |
| `util/UsernameCache.java` | 232 / 83 | 58 / 1 | 6 | 仅本地缓存；缺 Mojang 下载/校验。 |
| `util/WailaExceptionHandler.java` | 56 / 60 | 1 / 1 | 0 | 对应基本一致 |

## 4. 原版存在但未移植的文件（26 个）

| 文件 | 原因 |
|---|---|
| `api/fluid/JadeFluidObject.java` | 1.12 无 FluidState/流体注册表；流体对象 API 未移植 |
| `api/view/ClientViewGroup.java` | 1.13+ 容器/扩展视图 API，未移植 |
| `api/view/EnergyView.java` | 同上（能量视图） |
| `api/view/FluidView.java` | 同上（流体视图） |
| `api/view/IClientExtensionProvider.java` | 同上（扩展视图） |
| `api/view/IServerExtensionProvider.java` | 同上（扩展视图） |
| `api/view/ItemView.java` | 同上（物品视图） |
| `api/view/ProgressView.java` | 同上（进度视图） |
| `api/view/ViewGroup.java` | 同上（视图组） |
| `gui/config/BelowOrAboveListEntryTooltipPositioner.java` | 1.20 tooltip positioner API；1.12 用 BaseOptionsScreen 手工 tooltip |
| `impl/theme/ThemeCodecs.java` | 1.20 Codec 主题序列化；1.12 用 GSON/ThemeHelper |
| `impl/ui/FluidStackElement.java` | 依赖流体 API，未移植 |
| `impl/ui/TooltipComponentsElement.java` | 1.20 Component/文本组件 API，1.12 用 Text |
| `mixin/AbstractHorseAccess.java` | 1.20 马属性访问；1.12 直接 getCustomJumpStrength |
| `mixin/FontMixin.java` | better text shadow 功能未移植 |
| `mixin/FullTextSearchTreeMixin.java` | 1.20 创造模式搜索树 API；1.12 ReloadableIdSearchTree 自行处理 |
| `mixin/HarvestToolProviderMixin.java` | 服务器数据 reload mixin；1.12 用 reload 事件清缓存 |
| `mixin/KeyAccess.java` | 1.20 InputConstants 键名重映射；1.12 用 GameOptions.getKeyName |
| `mixin/StringRenderOutputMixin.java` | better text shadow 功能未移植 |
| `mixin/ThemeHelperMixin.java` | 1.20 主题资源 reload 接入；1.12 用 ClientResourceLoaderEvents |
| `overlay/DatapackBlockManager.java` | 1.12 无数据包方块伪装 API |
| `util/CodecJsonSerializer.java` | 1.20 Codec 序列化；1.12 不需要 |
| `util/DumpGenerator.java` | 调试导出功能未移植 |
| `util/FluidTextHelper.java` | 流体文本 API，未移植 |
| `util/JadeFabricUtils.java` | 1.20 Fabric API 工具，1.12 用 OSL/Feather |
| `util/TechRebornEnergyCompat.java` | 1.12 兼容模组能量 API，未移植 |

## 5. 移植版新增/专用的文件（44 个）

其中 `addon/vanilla` 下 26 个 provider、`addon/harvest` 下 4 个类属于按 1.12 内容重写的移植实现（原版对应包在任务范围外）。其余主要新增：

| 文件 | 原因 |
|---|---|
| `api/ui/Rect2i.java` | 1.12 无 net.minecraft.client.renderer.Rect2i |
| `gui/config/widget/CycleButtonWidget.java` | 1.12 无 CycleButton，等价实现 |
| `gui/config/widget/JadeButton.java` | 1.12 无 Button，等价实现 |
| `gui/config/widget/JadeTextField.java` | 1.12 无 EditBox，封装 TextFieldWidget |
| `gui/config/widget/JadeWidget.java` | 统一 1.12 widget 接口 |
| `mixin/BossEventOverlayAccess.java` | 1.12 boss overlay 字段访问 |
| `mixin/ChatScreenMixin.java` | 1.12 ChatScreen 兼容 |
| `mixin/ClientPlayerInteractionManagerAccess.java` | 1.12 破坏进度私有字段访问 |
| `mixin/GameGuiMixin.java` | 1.12 GameGui 渲染挂载 |
| `mixin/ItemStackMixin.java` | 1.12 ItemStack tooltip 挂载 |
| `mixin/PlayerTabOverlayAccess.java` | 1.12 tab overlay 可见性访问 |
| `mixin/ReloadableIdSearchTreeMixin.java` | 1.12 创造搜索树适配 |
| `mixin/TextRendererMixin.java` | 1.12 文字渲染适配 |
| `util/Networking.java` | OSL 通道/PacketBuffer 适配，原版用 Fabric Networking |

## 6. 本次审查发现并修复的问题

### P1-1 物品 tooltip 不显示堆叠数量

- 文件：`overlay/DisplayHelper.java:103`
- 原版：`renderGuiItemDecorations(..., stack, ...)` 直接传真实 `ItemStack`。
- 移植版曾复制 `ItemStack` 并 `setSize(1)`，导致数量装饰被隐藏；所有方块/物品 tooltip 图标都受影响。
- 修复：直接传 `stack`，恢复数量/耐久等装饰。
- 影响：熔炉输入输出、箱子物品、掉落物等 tooltip 的数量显示。

### P1-2 动画帧时间全部用错

原版统一使用 `Minecraft#getDeltaFrameTime()`；1.12 没有该 API，移植版分别用了：

- `Minecraft#getTickDelta()`（是 partial tick，不是帧 delta）
- 固定 `1`
- `20 / FPS` 近似

受影响位置：

| 位置 | 原版 | 修复前 | 后果 |
|---|---|---|---|
| `OverlayRenderer.java` | `getDeltaFrameTime()` | `getTickDelta()` | overlay 淡入淡出、隐藏延迟速度不对 |
| `BoxElement.java`（chase） | `getDeltaFrameTime() * 2` | `getTickDelta() * 2` | tooltip 追帧/缩放动画速度不对 |
| `BoxElement.java`（track） | `track.tick(getDeltaFrameTime())` | `tick(1)` | 底部进度条瞬间跳变 |
| `ProgressElement.java` | `track.tick(getDeltaFrameTime())` | `tick(1)` | 进度条平滑动画过快 |
| `JadeClient.java`（breaking） | `getDeltaFrameTime()` | FPS 近似 | 破坏进度条淡入淡出速度偏差 |

- 修复：`ClientProxy.getDeltaFrameTime()` 用真实帧间隔（纳秒差 × 20 ticks/s，按帧缓存），统一替换上述位置。
- 影响：tooltip 出现/消失、进度条填充、破坏进度条淡入淡出现在都与原版时间基准一致。

### P1-3 `{0}` 时间文本不生效

- 文件：`JadeClient.java:198`
- 原版：

```java
MessageFormat.format(I18n.get(s), objects)
```

- 移植版使用了 `String.format`，但语言文件：

```json
"jade.seconds": "{0}s",
"jade.minutes": "{0}m",
"jade.minutes_seconds": "{0}m {1}s"
```

- 结果：`ThemeHelper.seconds()` 生成 `{0}s` 之类的字面文本。
- 修复：改用 `MessageFormat.format`。
- 影响：动物繁殖/成长倒计时、鸡下蛋倒计时等所有时间文本。

### P1-4 破坏进度条跨世界残留

- 文件：`JadeClient.java:140`
- 问题：静态 `progressAlpha` / `savedProgress` 不随世界切换重置，进新存档可能短暂显示上一个世界的破坏进度。
- 修复：记录 `breakingWorld`，`mc.world` 变化时清零。

### P1-5 主题颜色映射完全错误（第三轮发现）

- 文件：`impl/theme/ThemeHelper.java`
- 1.12 `Formatting.getId()` 返回的是 0-15 的颜色**索引**，不是 RGB。移植版却把索引当作 RGB 做距离计算：
  ```java
  int c = formatting.getId();          // 0..15
  int cr = (c >> 16) & 0xFF;           // 永远是 0
  int cg = (c >> 8) & 0xFF;            // 永远是 0
  int cb = c & 0xFF;                   // 等于索引
  ```
  结果颜色距离只与蓝色通道有关，主题里的 info/success/warning/danger/title/坐标颜色会被映射到错误的 16 色之一。
- 修复：加入 16 个原版颜色的真实 RGB 表 `VANILLA_COLORS`，按 RGB 距离选择最近的 `Formatting`。
- 影响：所有主题文字颜色、坐标三色、状态效果颜色、繁殖/成长倒计时颜色。

### P1-6 `drawText` 在关闭阴影时把颜色也剥掉了（第三轮发现）

- 文件：`overlay/DisplayHelper.java`
- 移植版：
  ```java
  String s = shadow ? text.getFormattedString() : Formatting.strip(text.getFormattedString());
  ```
  主题 `text.shadow = false` 时（部分主题如此），所有带颜色的文本会变成纯白。
- 原版 `drawString(font, sequence, x, y, color, shadow)` 只切换阴影，保留每段颜色。
- 修复：始终传 `getFormattedString()`，只用 `shadow` 控制阴影。

### P1-7 `hide-entities.json` 从未生效（第三轮发现）

- 文件：`addon/core/CorePlugin.java`
- 原版 `registerClient` 里同时遍历 `createEntityBlocklist()` 和 `createBlockBlocklist()` 并调用 `hideTarget`。
- 移植版只遍历了方块黑名单，**实体黑名单完全没有应用**，`hide-entities.json` 形同虚设；默认值也是空的。
- 修复：补上实体遍历（`Entities.REGISTRY.get(id)` → `hideTarget(Class)`），默认值设为 1.12 的 `minecraft:area_effect_cloud`、`minecraft:fireworks_rocket`。

### P1-8 主题颜色映射到错误的原版颜色（第四轮发现）

- 文件：`impl/theme/ThemeHelper.java`
- 上一轮把 `Formatting.getId()` 换成了真实 RGB 表 + RGB 距离，但 RGB 距离对**柔和色**无效：
  - `0xef9a9a`（浅红）到 GRAY 的距离比到 RED 更近 → 变成灰色
  - `0xa5d6a7`（浅绿）到 GRAY 更近 → 变成灰色
  - `0x90caf9`（浅蓝）到 AQUA 最近 → 只有它看起来有颜色
  这正是“坐标只有 Z 是蓝色，X/Y 像白色”的原因。
- 修复：改为**按色相分带**映射（红/橙/黄/绿/青/蓝/紫），再按亮度选亮色或暗色变体；灰度色单独按亮度选 BLACK/DARK_GRAY/GRAY/WHITE。
- 校验：对 16 个原版颜色自身做映射是幂等的（不会漂移）；坐标 6 色映射为 RED/GREEN/BLUE 与 DARK_RED/DARK_GREEN/DARK_BLUE。
- 影响：所有主题色（info/success/warning/danger/title/坐标/状态效果/繁殖倒计时）。

### P2-1 debug 元素边框没有渲染

- 原版：`BoxElement` 和 `Tooltip.Line` 都调用 `Tooltip.drawDebugBorder`。
- 移植版：方法存在但从未调用，按住 Ctrl + debug 模式看不到元素边框。
- 修复：在 `Tooltip.Line.renderAligned`、`BoxElement` 图标和根元素处补回调用。

### P2-2 HOLD_KEY 显示模式绕过了按键绑定

- 文件：`OverlayRenderer.java:53`
- 原版：`showOverlay.isDown()`。
- 移植版曾用 `Keyboard.isKeyDown(showOverlay.getKeyCode())`，不尊重按键冲突/绑定状态。
- 修复：改用 1.12 `KeyBinding.isPressed()`。

### P2-3 Theme 空指针缺少原版防御

- 文件：`overlay/WailaTickHandler.java:141`
- 原版有 `Preconditions.checkNotNull(theme, "Theme cannot be null")`。
- 已补回，避免主题加载失败时在后面出现无上下文 NPE。

### P2-4 原版 license/注意事项注释缺失

- 文件：`overlay/OverlayRenderer.java:103`
- 原版在 `renderOverlay` 前有“不要用 Mixin 整体替换、注意许可证”的大段注释。
- 已原样恢复该注释。

### P2-5 Theme 回退可能递归

- 文件：`overlay/OverlayRenderer.java`
- 移植版 `OverlayRenderer.theme()` 在 `theme` 为空时调用 `IThemeHelper.getTheme(DEFAULT)`；若 `ThemeHelper.fallback` 尚未初始化，会再次回到 `ThemeHelper.theme()`，存在递归风险。
- 原版 `OverlayRenderer.theme()` 直接返回 `theme.getValue()`，由 `ThemeHelper.theme()` 负责 fallback。
- 已改为原版结构，避免启动/资源重载早期的递归。

### P2-6 下落方块模组名错误

- 文件：`util/ModIdentification.java`
- 原版对 `FallingBlockEntity` 使用其内部方块的模组名；移植版只按实体注册名 `minecraft:falling_block` 判断。
- 已补 `FallingBlockEntity -> getBlock().getBlock()` 分支。
- 仍无法完全对齐的是画作：1.12 `PaintingEntity.Motive` 不是注册表项，无法像 1.20 的 `PaintingVariant` 一样取到实际模组命名空间。

### P2-7 物品模组名样式被硬编码、且可能重复插入

- 文件：`JadeClient.java`
- 原版使用 `IWailaConfig.Formatting#getItemModNameStyle()`，移植版硬编码 `BLUE + ITALIC`，配置修改无效。
- 原版去重时比较 `tooltip.get(i).getString()`（忽略格式码），移植版直接比较带 `§` 格式码的字符串，可能重复插入模组名。
- 已改为读取配置样式，并用 `Formatting.strip(...)` 去重。

### P2-8 主题颜色覆盖已有文本样式

- 文件：`impl/theme/ThemeHelper.java`
- 原版 `color()` 只在文本 style 为空时应用主题色；已有样式时保留原样式，只替换颜色。
- 移植版直接 `setStyle(colorStyle(color))`，会丢掉粗体/斜体/下划线等已有样式。
- 已改为 `style.copy().setColor(...)`，保留原样式属性。

### P2-9 附魔书模组名判断缺失

- 文件：`util/CommonProxy.java`
- 原版对 `EnchantedBookItem` 读取 `StoredEnchantments`，按附魔注册名判断来源模组。
- 移植版只检查 NBT `id` 和物品注册名，附魔书会显示 `minecraft`。
- 已按 1.12 `Enchantment.REGISTRY` 补回逻辑。

### P2-10 物品 tooltip 保留了本应移除的颜色

- 文件：`addon/vanilla/ItemTooltipProvider.java`
- 原版会移除物品 tooltip 行的颜色，只保留文本；移植版直接把带 `§` 颜色码的字符串放进 `LiteralText`，Jade 的掉落物 tooltip 会显示彩色行。
- 已改为 `Formatting.strip(line)` 后再做模组名过滤和宽度裁剪。
- 1.12 的 `Formatting.strip` 会同时去掉粗体/斜体，原版只清颜色；这是 1.12 字符串 API 的近似。

### P2-11 空熔炉仍发送并显示空物品槽

- 文件：`addon/vanilla/FurnaceProvider.java`
- 原版 `appendServerData` 在 `furnace.isEmpty()` 时直接返回，不发送数据。
- 移植版会发送 3 个空槽和 `0/0` 进度，导致空熔炉 tooltip 出现空物品图标/进度。
- 已补 `if (furnace.isEmpty()) return;`。

### P2-12 红石通用 power 属性未显示

- 文件：`addon/vanilla/RedstoneProvider.java`
- 原版会显示任何带 `POWER` 属性方块的信号强度（红石线、红石块等）；移植版只处理拉杆、中继器、比较器、漏斗。
- 已补：遍历 `BlockState.properties()`，对名为 `power` 的整数属性显示 `tooltip.jade.power`。
- 红石线等方块现在能显示信号强度。

### P2-13 坐标显示的分隔符与浅色主题配色（第三轮发现）

- 文件：`addon/core/DistanceProvider.java`
- 移植版用 `", "` 拼接坐标，语言文件 `jade.blockpos` 实际是 `"%s %s %s"`（空格分隔），显示效果与原版不一致。
- `display(int, int)` 丢掉了浅色主题 `colorIndex += 3` 的偏移（新增的 `colorIndex()` 辅助方法也没有被调用）。
- 修复：改为空格分隔；`display` 内恢复浅色主题偏移。

### P2-14 输入框 `setText` 不触发 responder（第三轮发现）

- 文件：`gui/config/widget/JadeTextField.java`
- 1.12 `TextFieldWidget.setText()` 不调用 `onTextChanged`，而 1.20 `EditBox.setValue()` 会触发 responder。
- 因此 `InputOptionValue.setValue(T)` 只改了显示文本，`value` 字段和配置都没有更新。
- 修复：`JadeTextField.setText` 覆盖后主动通知 responder。

### P2-15 主题 `modNameStyle` 未解析（第三轮发现）

- 文件：`impl/theme/ThemeHelper.java`
- 原版会读取主题 `text.modNameStyle` 并叠加到全局物品模组名样式上；移植版解析时直接传 `null`。
- 修复：用 1.12 `Style.Serializer` 解析，`themeStyle.copy().setParent(itemStyle)` 合并（主题未设置的属性回退到全局样式）。

### P2-16 tooltip 矩形计算顺序（第三轮发现）

- 文件：`impl/ui/BoxElement.java`
- 原版先取整宽高，再用**取整后的宽高**乘锚点计算 x/y；移植版用未取整的浮点值乘锚点，个别缩放下会差 1px。
- 修复：按原版顺序 `setWidth/setHeight` 后再 `setX/setY`，并给 `Rect2i` 补上对应的 setter。

### P2-17 其他对齐修正（第三轮）

- `gui/config/OptionsList`、`OverlayRenderer`、`SliderOptionValue` 中残留的 `org.lwjgl.input.*` 全限定名改为 import。
- `ClientProxy.isShowDetailsPressed` 改用 `KeyBinding.isPressed()`（原来直接读键盘，忽略按键绑定冲突）。
- `ClientProxy` 资源重载补上 `ModIdentification` 缓存失效（原版注册了 reload listener）。
- `HealthElement` 文本偏移改为 `x + 8`、补回 `narration.jade.health` 消息（与原版一致）。
- `ThemeHelper.defaultTheme()` 补上 `Theme.DEFAULT_THEME_ID`，避免早期 `activeTheme` 变成 null。

### P2-18 坐标文本结构未对齐原版（第四轮发现）

- 文件：`addon/core/DistanceProvider.java`
- 原版是 `Component.translatable("jade.blockpos", display(x), display(y), display(z))`；移植版手工拼 `LiteralText("")` + `", "` 分隔。
- 1.12 的 `TranslatableText` 同样支持 `Text` 参数并保留各自样式，已改为与原版同构的写法，同时正确使用语言文件里的 `"%s %s %s"` 格式。

## 6.4 GUI 专项审查（逐文件）

对照 `Jade-1.20.3-fabric/.../gui/**` 的 16 个文件与移植版 19 个文件，逐方法核对后修复了以下问题。

### 修复：行高与悬停条错位（`OptionsList`）

- 1.20 `ContainerObjectSelectionList.renderItem` 传给条目的 `height` 是 **`itemHeight - 4`**（本配置界面为 22），
  悬停条也按 `rowTop - 2 ~ rowTop + height + 2` 绘制。
- 移植版把 `entryHeight`（26）当成条目高度传给 `Entry.render`，但悬停条仍用 `height`（22），
  于是文字/控件的中心在 `rowTop + 13`、白条中心在 `rowTop + 11` —— 这就是“文字看起来偏下”的根因。
  之前的修法是把白条拉长到 `entryHeight`，虽然对齐了，但白条比原版高 4px。
- 现在两者都用 `height`（22）：白条 `rowTop-2 ~ rowTop+24`（正好等于行距 26），文字/按钮中心 `rowTop+11`，与原版完全一致。

### 修复：搜索框“×”清空后列表不刷新（`NotUglyEditBox`）

- 1.20 `EditBox.setValue` 会触发 responder；1.12 `TextFieldWidget.setText` **不会**。
- 移植版点击“×”只调用了 `setText("")`，`updateSearch` 没有被调用，输入框清空了但列表仍是过滤结果。
- 已在 `NotUglyEditBox.setText` 中补上 responder 通知。

### 修复：首页所有文字缺少阴影（`HomeConfigScreen`）

- 原版用 `guiGraphics.drawString(...)`（默认 `shadow = true`）绘制 “Jade” 标题、两行副标题、credit 和所有粒子。
- 移植版用 1.12 `textRenderer.draw(String,int,int,int)`，该重载 `shadow = false`，文字是平的。
- 已全部改为 `draw(..., true)`。

### 修复：按钮文字颜色与悬停高亮（`JadeButton` / `CycleButtonWidget`）

- 1.20 `Button#getMessageColor`：`!active → 0xA0A0A0`、`hovered → 0xFFFFA0`、否则 `0xE0E0E0`。
  移植版是恒定白色、无悬停变化。
- 现在：显式 `textColor(...)`（Save 的绿色、Reset 的红色）保持恒定（等价于带样式的 Component）；
  其余按钮恢复 1.20 的灰/黄高亮。
- `CycleButtonWidget`：标签与分隔符用 `0xFFFFFF`（1.20 `CycleButton` 用 16777215，不随悬停变色），
  ON/OFF 仍为 `0xFFB9F6CA` / `0xFFFF8A80`，并补上阴影。

### 修复：重置确认框的按钮文字（`WailaConfigScreen`）

- 原版把 `reset`（红色 “Reset”）作为确认按钮的文本传入 `ConfirmScreen`；移植版用了 `gui.yes`。
- 已改为红色 “Reset”。

### 修复：整数配置输入溢出崩溃（`IntConfigEntry`）

- `InputOptionValue.INTEGER.and($ -> isValidValue(Integer.valueOf($)))`：输入超过 `Integer.MAX_VALUE` 的数字时
  `Integer.valueOf` 抛 `NumberFormatException`，异常会从 `onTextChanged` 一路冒到游戏主循环导致崩溃（上游同样存在）。
- 已加 try/catch 返回 `false`，其余行为不变。

### 其他对齐

- 所有 GUI 文字（选项标题、`Title`、左侧导航标题、滑条文字、输入框文字/提示/“×”、`OptionButton`）补上阴影，与 1.20 `drawString` 一致。
- `OptionsNav.renderBackground` 是永远不会被调用的死代码（`render()` 已整体重写），已清空并加注释。
- `WailaConfigScreen` 中残留的 `snownee.jade.gui.config.widget.CycleButtonWidget`、`com.google.common.base.Preconditions` 全限定名改为 import。

### 仍然存在但属于 1.12 平台差异（未改）

| 项目 | 原版 1.20 | 移植版 |
|---|---|---|
| 行起点 | `getRowLeft()` | `getRowLeft() + 2`（整体右移 2px） |
| 导航滚动条 | `getRowLeft() + getRowWidth() - 8` = 112 | `2 + getRowWidth() - 8` = 114 |
| 未绑定按键名 | “Not Bound”（`key.keyboard.unknown`） | “NONE”（1.12 `GameOptions.getKeyName(0)`） |
| 服务器功能 `*` 提示 | 列表位置器（条目下方/上方） | 跟随鼠标 |
| 叙述/narration | 完整实现 | 无（1.12 无 NarratableEntry） |
| 列表滚动条 | 原版 sprite | 手工绘制（外观一致） |
| 提示框 | 1.20 tooltip 管线 | 手工复刻 1.12 tooltip 外观 |
| 彩蛋音效 | festival 99 播放烟花音 | 未播放（按之前要求省略音效） |
| `Title.mouseClicked` | 返回 `false` | 返回 `true`（仅影响 `widgetInteracting`，无可见差异） |

### 逐文件结论

| 文件 | 结论 |
|---|---|
| `BaseOptionsScreen` | 保存/取消流程、`render` 顺序、滚轮分发均与原版一致；搜索框与列表的手工焦点管理是 1.12 必需 |
| `HomeConfigScreen` | 节日判定、粒子、credit 悬停、glint 均一致；已补阴影 |
| `PreviewOptionsScreen` | 位置调整拖拽、吸附、引导线、`forcePreview` 一致（`isDragging` → `isWidgetInteracting` 适配） |
| `WailaConfigScreen` | 选项顺序与内容、主题切换、重置流程一致；确认框按钮文字已修 |
| `PluginsConfigScreen` | 分类排序、`jumpTo`、同步项禁用、服务器特性标记一致 |
| `OptionsList` | 搜索、保存态、选中、滚动平滑一致；行高已修 |
| `OptionsNav` | 锚点动画、`ensureVisible` 等价实现、滚动条一致 |
| `OptionValue` / `OptionButton` | 文本位置、缩进、删除线禁用态一致 |
| `CycleOptionValue` / `InputOptionValue` / `SliderOptionValue` | 取值、保存、校验一致；滑条纹理按 1.12 原生 |
| `NotUglyEditBox` | 内边距、选区、光标、清除按钮一致；已修清空不刷新 |
| `JadeButton` / `CycleButtonWidget` / `JadeTextField` / `JadeWidget` | 1.12 等价实现；颜色与 responder 已修 |

## 6.5 网络层审查

已对照原版 Fabric Networking，逐项检查移植版 OSL 通道：

| 项目 | 状态 |
|---|---|
| 玩家加入发送 `server_ping` | 已使用 `ServerConnectionEvents.PLAY_READY`，并用 `sendNoCheck` 发送 |
| 配置 JSON 同步 | `PluginConfig.getServerConfigs()` / `applyServerConfigs()` 对应 |
| 客户端断开重置 `serverConnected` | 已注册 `ClientConnectionEvents.DISCONNECT` |
| OSL 非主线程重放 | 所有 listener 已改为先 `ctx.ensureOnMainThread()` 再读包 |
| 请求方块/实体数据 | 距离、区块加载、实体存在校验已保留 |
| 方块请求 fakeBlock | 已随请求发送并在服务端 accessor 使用 |
| 方块命中点/朝向 | 已发送 facePos 与 `face.getId()` |
| 方块状态同步 | 1.12 无稳定 state id；服务端使用世界真实状态（差异见 7.2） |
| 1.20 的 `show_overlay` 命令 | 网络收发已保留，命令模块未移植，暂无调用方 |

## 7. 仍需处理的功能/效果差异

### 7.1 明确未移植

1. **`JadeClient.limitMobEffectFog` 是空实现**  
   原版会按迷雾效果距离隐藏远处 tooltip；1.12 移植版直接返回 accessor。  
   影响：隐身/失明等迷雾下仍可能显示较远目标 tooltip。

2. **TTS / narration 为空实现**  
   `WailaTickHandler.narrate`、`clearLastNarration` 没有真正朗读逻辑；原版 TTS 配置项存在但无效果。

3. **流体/容器/能量视图 API**  
   `api/view/*`、`api/fluid/*`、`DisplayHelper.drawFluid`、`drawTiledSprite`、`ElementHelper.fluid` 等未移植。  
   影响：通用物品/流体/能量存储 tooltip、流体罐、储罐等扩展不可用。

4. **数据包伪装**  
   `DatapackBlockManager`、`JadeClient.builtInOverrides` 未移植；1.12 无对应数据包 API。

5. **原版 mixin 功能**  
   `FontMixin` / `StringRenderOutputMixin`（更好的文字阴影）、`FullTextSearchTreeMixin`、`KeyAccess`、`ThemeHelperMixin`、`HarvestToolProviderMixin` 等未移植。

6. **`UsernameCache` 的 Mojang 名称下载**  
   仅保留本地缓存，缺少 `download` / `isValidName` / `removeUsername` 等；服务器上首次出现的玩家名可能只能显示 UUID。

7. **1.12 不存在的内容**  
   蜂蜜/蜂巢、营火、讲台、打蜡、雕纹书架、展示实体、Trial Spawner、僵尸村民治疗、物品 BER 等 provider 未移植（详见之前 vanilla 移植说明）。

8. **命令模块**  
   `command` 包未移植，因此 `showOrHideFromServer` 目前没有命令调用方。

### 7.2 有实现但与原版有语义差异

1. **方块状态不随请求传输**  
   1.12 没有 1.20 的稳定 BlockState 全局 ID；服务端使用世界真实状态，客户端 fake state 可能不一致。

2. **方块命中点传输精度**  
   现在传 `facePos` 的 float，原版是 double 精度；差距很小。

3. **`RayTracing` 流体 SOURCE_ONLY**  
   1.12 用流动/静止液体方块近似；SOURCE_ONLY 命中流动液体时会重新 trace，极少数情况下可能跳过后面的源液体。

4. **`CommonProxy.isShearable` 硬编码方块列表**  
   1.12 没有 shearable 标签，只能列 LEAVES/LEAVES2/WEB/VINE/TALLGRASS/DEADBUSH/TRIPWIRE/WOOL；模组方块无法自动识别。

5. **`ModIdentification` 的画作模组名**  
   附魔书特判已补回；但 1.12 `PaintingEntity.Motive` 不是注册表项，无法像 1.20 的 `PaintingVariant` 那样取到画作实际模组命名空间。

6. **`ProgressTracker` 速度计算加了 `max(1, ticks)`**  
   原版除数为 `ticksSinceValueChanged`，初始可能为 0；移植版避免除零，极端情况下动画速度略有差异。

7. **`DisplayHelper.drawItem` 的 GUI 光照**  
   已改用 1.12 `Lighting.turnOnGui()`；与原版 1.20 的 baked lighting 不可能完全一致，但方块顶面白/整体暗的问题已修。

8. **状态效果持续时间格式**  
   原版用 `StringUtil.formatTickDuration` 并区分无限时间；移植版用 `m:ss`、`duration >= 1000000` 显示 `∞`，文本格式略有差异。

9. **`AnimalOwnerProvider` 单机跳过条件更宽**  
   原版只在“请求者是单机房主”时跳过服务端名称；移植版单机下跳过所有 Tameable，单人游戏效果相同，LAN 场景可能有差异。

10. **`ItemTooltipProvider` 去格式更彻底**  
   原版只移除颜色、保留粗体/斜体；1.12 用 `Formatting.strip` 会同时移除这些样式。

11. **GUI 列表/导航是手写实现**  
   1.12 `ListWidget` API 与 1.20 `ContainerObjectSelectionList` 差异很大；`OptionsList`、`OptionsNav`、`NotUglyEditBox`、`BaseOptionsScreen` 的行为已经调平，但代码结构不可能一一对应。

## 8. 注释缺失审查

- 原版配对文件注释行：**579**
- 移植版：**549**（约 95%）
- 已补回 API 接口的主要 Javadoc（8 个文件、约 42 个注释块），并清理了自动插入时产生的重复/错位注释。
- 仍有 12 个文件完全没有原注释：

  `impl/ui/BoxElement.java`、
  `overlay/WailaTickHandler.java`、
  `gui/WailaConfigScreen.java`、
  `impl/ui/ElementHelper.java`、
  `impl/WailaClientRegistration.java`、
  `gui/config/value/SliderOptionValue.java`、
  `impl/Tooltip.java`、
  `overlay/DisplayHelper.java`、
  `util/JsonConfig.java`、
  `impl/WailaCommonRegistration.java`、
  `impl/config/PluginConfig.java`、
  `impl/ui/SimpleProgressStyle.java`

- 这些文件丢失的主要是方法内实现注释，而不是 public API 的 Javadoc；若要进一步贴近原版，建议按原版逐段补回这些实现注释。

## 9. 结构贴近度

| 类型 | 文件 | 差异 |
|---|---|---|
| 结构基本一致 | `api/AccessorImpl`、`impl/ObjectDataCenter`、`overlay/ProgressTracker`、`impl/theme/*` | 字段/方法顺序接近 |
| 平台适配型差异 | `impl/BlockAccessorImpl`、`EntityAccessorImpl`、`util/Networking`、`mixin/*` | 1.12 API 必需 |
| 重写型差异 | `gui/config/OptionsList`（540→730）、`OptionsNav`、`NotUglyEditBox`（547→257）、`BaseOptionsScreen` | 1.12 widget/布局 API 完全不同 |
| 功能裁剪型差异 | `DisplayHelper`（439→298）、`ClientProxy`（319→181）、`CommonProxy`（460→283） | 流体/存储/命令/TTS 未移植 |

## 10. 建议的后续顺序

1. 补 `limitMobEffectFog`（可见性效果）。
2. 补 API Javadoc（`api/` 优先）。
3. 若需要通用存储/流体 tooltip，移植 `api/view` + `DisplayHelper.drawFluid` + `ElementHelper.fluid`。
4. 补 `UsernameCache` 的 Mojang 名称下载（可选）。
5. 补命令模块（`showOrHideFromServer` 已有网络支持）。
6. 逐个补 1.12 可实现的 vanilla provider（`NoteBlock`/`Villager` 已移植，`ItemBER`/`TotalEnchantmentPower` 仍缺）。

## 11. 构建验证

```text
./gradlew clean build --offline
BUILD SUCCESSFUL
build/libs/Jade-1.0.0+mc1.12.2.jar
```
