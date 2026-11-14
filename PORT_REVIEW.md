# Jade 1.12.2 移植审查报告

对照源：`Jade-1.20.3-fabric`（实际 1.20.4 代码）
移植目标：`jade1.12.2`（Minecraft 1.12.2 / Ornithe Fabric / OSL 0.21.0 / Java 8）
审查方式：文件集合对比、行数/注释/方法签名结构对比、核心文件逐方法 diff、1.12 API 可用性核对、clean build 验证。

## 1. 总体结论

- 原版未排除包共 **145** 个 Java 文件，移植版对应 **119** 个，另有 **44** 个 1.12 专用/移植新增文件。
- 已配对文件中，原版注释行 **579**，移植版 **549**，保留率约 **95%**；API 接口的主要 Javadoc 已补回。
- 发现并修复 **37 类实际逻辑/效果问题**，其中影响最明显的几类：
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

### 修复：雪花粒子变成了不透明（`HomeConfigScreen`）

- 原版用 `Mth.color(r, g, b)`，返回值**没有 alpha 位**（0x00RRGGBB），随后 `color |= (40..120) << 24` 加上半透明 alpha；
  而 1.20 `drawString` 在 alpha 为 0 时会强制补成 0xFF，所以 credit 粒子仍是实心。
- 移植版的 `color()` 辅助方法直接返回 `0xFF000000 | rgb`，于是雪花的 `|= alpha` 被 0xFF 吞掉，雪花变成**完全不透明**。
- 已改为与原版一致：`color()` 不带 alpha 位，雪花恢复半透明，credit 粒子仍由渲染器补成不透明。

### 修复：选项标题丢失自身样式（`OptionValue`）

- 原版用 `title`（Component）绘制，标题自身的颜色/粗体等样式会保留；移植版用 `title.getString()` 丢掉了样式。
- 已改为 `title.getFormattedString()`；禁用态的删除线+灰色仍按原版覆盖。

### 修复：行起点与导航滚动条偏移（`OptionsList` / `OptionsNav`）

- 1.20 `getRowLeft()` = `getX() + width/2 - getRowWidth()/2`；移植版多加了 `+2`，导致所有行（文字与控件）整体右移 2px，
  悬停条却仍从列表左边界开始，行不再居中。
- 导航滚动条原版在 `getRowLeft() + getRowWidth() - 8` = 112，移植版写死 `2 + ...` = 114。
- 两处都已按原版公式修正；导航锚点仍保持 `getRowLeft() + 2`（与原版一致）。

### 修复：`setScrollAmount` 未按原版钳制目标值（`OptionsList`）

- 1.20 `setScrollAmount(d)` → `super.setScrollAmount(d)`（内部钳制）→ `targetScroll = getScrollAmount()`（钳制后的值）。
- 移植版先写 `targetScroll = 原值` 再 `capScrolling()`，`targetScroll` 仍是未钳制值。
  切换界面（如重置后列表变短）时目标滚动量会越界，需要靠下一帧的 `render` 再纠正一次，期间会产生一次可见的“回弹”。
- 已改为先钳制再赋值，与原版一致。

### 修复：`☰` 编辑名单按钮的提示框不显示（`KeybindOptionButton.JadeButtonWrapper`）

- 原版 `☰` 按钮是带 `Tooltip.create(...)` 的 `Button`，1.20 的 `getChildAt` 会把它算作子控件并显示提示。
- 移植版把按钮包在 `JadeButtonWrapper` 里，`BaseOptionsScreen.renderHoverTooltips` 拿到的是 **wrapper**，
  而 wrapper 没有实现 `getTooltip()`（`JadeWidget` 的默认实现返回 `null`），所以提示框永远不显示。
- 已在 wrapper 里把 `getTooltip()` 委托给内部按钮；`display_mode` / `item_mod_name` 的提示因为直接用 `CycleButtonWidget`，本来就是好的。

### 其他对齐

- 所有 GUI 文字（选项标题、`Title`、左侧导航标题、滑条文字、输入框文字/提示/“×”、`OptionButton`）补上阴影，与 1.20 `drawString` 一致。
- `OptionsNav.renderBackground` 是永远不会被调用的死代码（`render()` 已整体重写），已清空并加注释。
- `WailaConfigScreen` 中残留的 `snownee.jade.gui.config.widget.CycleButtonWidget`、`com.google.common.base.Preconditions` 全限定名改为 import。

### 仍然存在但属于 1.12 平台差异（未改）

| 项目 | 原版 1.20 | 移植版 |
|---|---|---|
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

## 6.7 无障碍复述（TTS / Narration）移植

### 1.12 的复述机制

- 1.12 自带 `com.mojang.text2speech` **1.10.3**（`NarratorChatListener` 用它朗读聊天栏），
  接口只有 `say(String)` / `clear()` / `active()` / `getNarrator()`，**没有** 1.20 的 `say(String, boolean interrupt)`。
- 1.12 的 `options.narrator`（0 关 / 1 全部 / 2 聊天 / 3 系统）只控制**原版聊天栏**朗读，
  Ctrl+B 会把它设成 1。原版 Jade 也不检查这个开关，它用自己的按键 + `Narrator.active()` 判断，这里保持一致。

### 修复：`WailaTickHandler.narrate` / `clearLastNarration` 是空方法

移植版这两个方法只有空壳，导致：
- 切换模式（`tts_mode = TOGGLE`）下打开复述后**什么都不会读**；
- 按键模式（`PRESS`）下按复述键也没有任何反应。

已按原版完整实现：

```java
private static final Supplier<Narrator> NARRATOR = Suppliers.memoize(Narrator::getNarrator);
private static String lastNarration = "";
private static long lastNarrationTime = 0;

public static void narrate(ITooltip tooltip, boolean dedupe) {
    if (!NARRATOR.get().active() || tooltip.isEmpty()) return;
    if (System.currentTimeMillis() - lastNarrationTime < 500) return;   // 500ms 节流
    String narration = tooltip.getMessage();
    if (dedupe && narration.equals(lastNarration)) return;              // 同一段不重复朗读
    CompletableFuture.runAsync(() -> {                                  // 朗读放到异步线程
        Narrator narrator = NARRATOR.get();
        narrator.clear();
        narrator.say(Formatting.strip(narration));
    });
    lastNarration = narration;
    lastNarrationTime = System.currentTimeMillis();
}

public static void clearLastNarration() { lastNarration = ""; }
```

与原版的唯一差异：1.10.3 没有 `say(text, false)`，改为先 `clear()` 再 `say(text)`，听感等价。

### 修复：`PRESS` 模式按键分支缺失

原版 `JadeClient` 的复述按键处理：

```java
while (narrate.consumeClick()) {
    if (general.getTTSMode() == TTSMode.TOGGLE) { ...开关 + 提示 toast... }
    else if (WailaTickHandler.instance().rootElement != null) {
        WailaTickHandler.narrate(WailaTickHandler.instance().rootElement.getTooltip(), false);
    }
}
```

移植版只有 `TOGGLE` 分支，`else if` 整个缺失 —— 按键模式下按复述键毫无反应。已补上。

### 修复：护甲值不参与复述（`ArmorElement`）

- 原版 `ArmorElement.getMessage()` 返回 `I18n.get("narration.jade.armor", ...)`；
- 移植版直接 `return null`，所以护甲值永远不会被朗读。已改为与原版一致。

### 已确认完整的部分

| 环节 | 状态 |
|---|---|
| 复述按键 `key.jade.narrate`（小键盘 5） | ✓ 已注册 |
| `tts_mode` 配置项（PRESS / TOGGLE） | ✓ GUI 可切换 |
| `enableTextToSpeech` + `toggleTTS()` + 500ms 节流 | ✓ |
| `hintNarratorToggle` 提示 toast | ✓ `toast.jade.tts_hint.1/2` |
| `OverlayRenderer` 在 TOGGLE 模式下调用 `narrate(tooltip, true)` | ✓ |
| 悬浮框消失时 `clearLastNarration()` | ✓ 再次悬停会重新朗读 |
| `Tooltip.getMessage()`（按行拼接、跳过模组名、元素 `getMessage()`） | ✓ 与原版一致 |
| `HealthElement.getMessage()` / `ItemStackElement.getMessage()` / `TextElement.getMessage()` / `ProgressElement.getMessage()` / `BoxElement.getMessage()` | ✓ 全部一致 |
| `narration.jade.*` 语言键 | ✓ 全部存在 |
| `maybeLowVisionUser()`（装了 `minecraft_access` 或开了 TTS） | ✓ |
| 元素级复述文案（`.message(...)`） | ✓ 与原版一致（仅未移植的 provider 除外） |

### 复述代码逐行复查结果（第二轮）

逐行对照原版后确认无逻辑偏差，并核对底层库行为：

| 检查点 | 结果 |
|---|---|
| `Narrator.getNarrator()` 是否可缓存 | 1.10.3 **每次调用都新建实例**（`NarratorWindows` 构造会起一个守护线程），因此 `Suppliers.memoize` 是必需的，与原版一致 |
| `getNarrator()` 是否会返回 null | 不会，平台不支持时返回 `NarratorDummy`（`active() == false`） |
| `active()` 是否可能抛异常 | `NarratorWindows`/`NarratorLinux` 用静态 `libraryFound` 标记，`active()` 只读该标记，不会抛 |
| `Formatting.strip` 与 1.20 `StringUtil.stripColor` | 1.12 的 `CODE_PATTERN = (?i)§[0-9A-FK-OR]`，**与 1.20 完全相同的正则** |
| 500ms 节流位置 | 在 `getMessage()` 之前，与原版一致（避免每帧拼字符串） |
| 去重时机 | 去重命中时**不更新** `lastNarrationTime`，与原版一致 |
| `lastNarration` / `lastNarrationTime` 线程安全 | 只在渲染线程读写，异步任务只读 `narration` 局部变量 |
| 异步朗读 | `CompletableFuture.runAsync` 走 ForkJoinPool，与原版一致；异常被吞掉不影响游戏 |
| `tooltip.isEmpty()` | `Tooltip.size()` = 行数，与原版一致 |
| `Element.getCachedMessage()` 哨兵 | `DEFAULT_MESSAGE` 哨兵 + `.message(null)` 显式禁读，与原版逐行相同 |
| `Tooltip.getMessage()` | 按行 `' '` 连接、行间 `'\n'`、跳过 `CORE_MOD_NAME`，与原版相同 |
| `IElement.message/getMessage/getCachedMessage/clearCachedMessage` | 与原版**签名完全一致** |
| `ITooltip.getMessage()` / `isEmpty()` | 与原版完全一致 |
| `IWailaConfig.IConfigGeneral` 全部方法 | 与原版**无差异**（`shouldEnableTextToSpeech` / `toggleTTS` / `getTTSMode` / `setTTSMode`） |
| `IWailaConfig.TTSMode` | `TOGGLE, PRESS` 顺序一致（序列化到 JSON 时用名字，顺序不影响） |
| `IWailaClientRegistration.maybeLowVisionUser()` | 一致 |
| `ClientProxy.maybeLowVisionUser` | 一致（`isModLoaded("minecraft_access")`） |
| `IElementHelper` 全部签名 | 一致（仅缺 `fluid`） |

### 仍未实现（非复述，仅记录）

- `JadeClient.onGui(Screen)` 仍是空方法：原版在开发环境下检查配置项是否缺少语言键并抛 `AssertionError`，
  移植版没有 `ScreenEvents` 之类的钩子调用它。属于开发期校验，与复述无关。

### 仍然无法移植的部分

- 1.12 没有 `NarratableEntry` / `NarratableEntry` 那套 GUI 控件朗读体系，
  所以配置界面里的按钮/列表项朗读（1.20 的 `updateNarration`）无从实现；
  原版 Jade 也只在 `WailaConfigScreen` 里给 `☰` 按钮加了一个 `createNarration`。
- 1.12 没有 `ClientTooltipPositioner`，不影响复述。

## 6.8 `UsernameCache` 完整移植

原先的移植版只保留了最小可用部分，对照原版补齐后 **方法列表完全一致（20 个成员）**。

### 补齐的内容

| 成员 | 原先 | 现在 |
|---|---|---|
| `isValidName(String)` | 缺失（只判 `isEmpty()`，带 § 的名字也会被缓存） | ✓ `!name.isEmpty() && !name.contains("§")` |
| `removeUsername(UUID)` | 缺失 | ✓ |
| `containsUUID(UUID)` | 缺失 | ✓ |
| `getMap()` | 缺失 | ✓ 返回 `ImmutableMap.copyOf(map)` |
| `getLastKnownUsername` 的在线补全 | 缺失（永远返回 null） | ✓ 名字为空且 `MC_ANIMAL_OWNER_FETCH_NAMES` 开启时触发 `download(uuid)` |
| `download(UUID)` + `downloadingList` | 缺失 | ✓ 每个 UUID 只尝试一次，失败后不再重试 |
| `DownloadThread` | 缺失 | ✓ 见下方 1.12 适配 |
| `SaveThread` | 缺失（**在调用线程同步写盘**） | ✓ 后台线程写盘，`synchronized (saveFile)` 串行化 |
| `load()` 的失败处理 | 只打日志 | ✓ 打日志 + `WailaExceptionHandler.handleErr` + **删除损坏文件** |
| `load()` 的 `Map<UUID, String>` | 手工按 `Map<String,String>` 解析 | ✓ 与原版相同的 `TypeToken<Map<UUID, String>>`（Gson 内置 UUID 适配器） |

### 1.12 API 适配（2 处）

1. **下载 GameProfile**

```java
// 原版 1.20
ProfileResult profileResult = Minecraft.getInstance().getMinecraftSessionService().fetchProfile(uuid, true);
if (profileResult == null) return;
GameProfile profile = profileResult.profile();

// 1.12：没有 ProfileResult，用 authlib 1.5.25 的 fillProfileProperties
GameProfile profile = Minecraft.getInstance().getSessionService().fillProfileProperties(new GameProfile(uuid, null), true);
if (profile == null) return;
```

`fillProfileProperties` 查询失败时**返回传入的 profile（name 为 null）而不是 null**，所以后面的
`profile.getName() == null || "???"` 判断正好接住，逻辑与原版等价。

2. **配置文件路径**

```java
// 原版：getConfigDirectory() 返回 Path
private static final Path saveFile = CommonProxy.getConfigDirectory().toPath().resolve(...);
// 1.12：返回 File
private static final Path saveFile = new File(CommonProxy.getConfigDirectory(), Jade.MODID + "/usernamecache.json").toPath();
```

### 另外两处无害增强（已加 `//port:` 注释）

- `Charsets.UTF_8`（Guava，已废弃）→ `StandardCharsets.UTF_8`，编码完全相同。
- `SaveThread` 写盘前 `Files.createDirectories(parent)`：全新安装时 `config/jade/` 可能还不存在，
  原版会打一条 “Failed to save” 错误日志，这里避免掉。

### 兼容性验证

用项目同版本 Gson 实测：

| 场景 | 结果 |
|---|---|
| `Map<UUID, String>` → JSON | `{"069a79f4-...": "Notch"}` |
| JSON → `Map<UUID, String>` | 键类型是 `java.util.UUID` ✓ |
| 旧移植版写出的纯字符串键格式 | 仍能正确读取 ✓ |
| 空文件 | `fromJson` 返回 `null`，`load()` 里 `if (tempMap != null)` 已处理 ✓ |

### 复查：与安全/线程相关逐项验证

| 检查点 | 结果 |
|---|---|
| `handleErr(e, null, null)` 在 mod init 阶段是否安全 | ✓ 只写日志和 `logs/JadeErrorOutput.txt`，**不碰 `Minecraft`**，`provider == null` 时 `ERRORS` 也能容纳 |
| 专用服务器会不会误加载 `Minecraft` | ✓ `getLastKnownUsername` 里 `PluginConfig.get()` 走 serverConfigs 分支返回 `false`（该项是 client feature），`download()` 永不触发 |
| `getConfigDirectory()` 是否客户端专属 | ✓ 用 `FabricLoader.getInstance().getConfigDir()`，双端安全 |
| `SaveThread` 的锁对象 | ✓ 静态 `saveFile`（`Path`）唯一实例，所有线程锁同一对象 |
| `loading` 标志 | ✓ `load()` 期间不写盘，避免逐条 `setUsername` 触发 N 次 `SaveThread` |
| `downloadingList` 并发 | ✓ `Collections.synchronizedSet`，与原版相同的 check-then-add（原版也有该竞态） |
| `map` 是普通 `HashMap` | ✓ 与原版一致（`gson.toJson` 与下载线程可能并发，原版同样存在） |
| Gson 2.8.0（MC 1.12 实际版本）| ✓ 已用该版本实测 `Map<UUID, String>` 往返正确 |
| 损坏 JSON / 非 UUID 键 | ✓ 抛 `JsonSyntaxException` → 记录 + 删除文件（与原版一致；旧移植版只记日志不删） |
| 空文件 | ✓ `fromJson` 返回 `null`，`if (tempMap != null)` 已处理 |

### 与原版仍存在的差异（全部有理由）

| 差异 | 说明 |
|---|---|
| `fetchProfile(uuid, true)` → `fillProfileProperties(new GameProfile(uuid, null), true)` | 1.12 无 `ProfileResult`。语义差别：1.12 网络异常时**抛** `AuthenticationServiceException`（已捕获），404 时返回**入参 profile**（name 为 null）而不是 null。因此 `profile == null` 分支在 1.12 不可达（死代码但无害），真正兜住的是 `getName() == null`。失败后 UUID 留在 `downloadingList` 不再重试 —— 与原版结果相同 |
| `Charsets.UTF_8` → `StandardCharsets.UTF_8` | Guava 已废弃类，编码完全相同 |
| `saveFile` 用 `File.toPath()` 而非 `Path.resolve()` | 1.12 的 `getConfigDirectory()` 返回 `File`，结果路径相同 |
| `SaveThread` 写盘前 `Files.createDirectories` | 全新安装时 `config/jade/` 可能不存在，原版会打一条 “Failed to save” 错误日志 |
| import 顺序 | 项目风格（分组集中） |

### 一个已知的小差异（未改，属 `AnimalOwnerProvider`）

原版 `appendServerData` 用 `server.isSingleplayerOwner(玩家 GameProfile)` 判断“是不是本地主机的宠物”，
1.12 的 `MinecraftServer` 没有 `getServerOwner()`，移植版退化为 `server.isSingleplayer()`。
单人游戏里只有主机玩家，效果相同；局域网联机时其他玩家的宠物不再由服务端下发名字，
但客户端现在会通过 `getLastKnownUsername` 自动在线补全，最终显示一致。

## 6.9 补齐 1.12 可实现的 vanilla provider

对照原版 `addon/vanilla/` 的 37 个文件，之前缺 11 个。逐个核对 1.12 是否有对应机制后，补了 **2 个 provider + 2 处注册 + 2 个已有 provider 的 bug**。

### 新增：`TotalEnchantmentPowerProvider`（附魔台总附魔威力）

- 原版用 1.20 的 `EnchantmentTableBlock.BOOKSHELF_OFFSETS` + `isValidBookShelf`。
- 1.12 的对应逻辑在 `EnchantingTableMenu.onContentsChanged`：`(x,z) ∈ {-1,0,1}² \ {(0,0)}`，
  要求 `(x,0,z)` 与 `(x,1,z)` 两格都是空气，然后统计 `(2x,0/1,2z)`，以及 `x≠0 && z≠0` 时的 `(2x,0/1,z)` 与 `(x,0/1,2z)`。
- 已按 1.12 的几何**逐行复刻**（注意：1.20 的 `offset/2` 间隙判定与 1.12 不同，
  例如 `(2,y,1)` 处 1.12 要求间隙是 `(1,y,1)`，而 1.20 要求 `(1,y,0)`，所以不能直接照搬 1.20 的写法）。
- 威力求和仍走 `CommonProxy.getEnchantPowerBonus(...)`，与原版一致（支持 `registerCustomEnchantPower`）。
- 注册：`registerBlockComponent(..., EnchantingTableBlock.class)`、`markAsClientFeature`、`setConfigCategoryOverride(block)`。

### 新增：`ZombieVillagerProvider`（僵尸村民治愈倒计时）

- 原版直接读 `ZombieVillager.villagerConversionTime`（public 字段）。
- 1.12 的 `ZombieVillagerEntity.conversionTicks` 是 **private**，已新增访问器 mixin
  `ZombieVillagerEntityAccess`（`@Accessor("conversionTicks")`），并在 `jade.mixins.json` 的 `mixins` 数组注册。
- 注册：`registerEntityDataProvider`（服务端）+ `registerEntityComponent`（客户端）+ 配置分类覆盖。

### 修复：`MobSpawnerProvider` 显示的是翻译键而不是方块名

```java
// 修复前
Text name = new LiteralText(accessor.getBlock().getTranslationKey());  // "tile.mobSpawner"
// 修复后（与原版 accessor.getBlock().getName() 等价）
Text name = new LiteralText(accessor.getBlock().getName());            // "Monster Spawner"
```

1.12 的 `Block.getTranslationKey()` 返回 `tile.<name>`，`getName()` 才返回本地化名字。修复前刷怪笼会显示成
`tile.mobSpawner (Pig)`。

### 补齐：矿车刷怪笼（`SpawnerMinecartEntity`）

- 原版 `MobSpawnerProvider` 同时实现 `IEntityComponentProvider`，注册给 `MinecartSpawner`。
- 1.12 有 `SpawnerMinecartEntity`（刷怪笼矿车），其 `spawner` 字段是 private，
  已新增访问器 mixin `SpawnerMinecartEntityAccess`，并让 provider 实现 `IEntityComponentProvider`。
- 注册：`registerEntityComponent(MobSpawnerProvider.INSTANCE, SpawnerMinecartEntity.class)`。

### 修复：`VillagerProfessionProvider` 不支持僵尸村民

- 原版对 `Villager` 和 `ZombieVillager` 都取 `VillagerData` 并显示职业；移植版只处理 `VillagerEntity`。
- 1.12 没有 `VillagerData`，但 `VillagerEntity.getProfession()` 与 `ZombieVillagerEntity.getProfession()`
  都有，已改成两个分支，并补上 `registerEntityComponent(VillagerProfessionProvider.INSTANCE, ZombieVillagerEntity.class)`。

### 判定为 1.12 无法实现的 9 个 provider

| Provider | 原版依赖 | 1.12 情况 |
|---|---|---|
| `BeehiveProvider` | `BeehiveBlock(Entity)` | 1.15+ 才有蜂巢 |
| `CampfireProvider` | `CampfireBlockEntity` | 1.14+ 才有营火 |
| `ChiseledBookshelfProvider` | `ChiseledBookShelfBlockEntity` | 1.20+ |
| `LecternProvider` | `LecternBlock(Entity)` | 1.14+ 才有讲台 |
| `MobSpawnerCooldownProvider` | `TrialSpawnerBlockEntity` | 1.21+ 的试炼刷怪笼（**不是**普通刷怪笼） |
| `WaxedProvider` | 蜂巢打蜡 | 1.17+ |
| `BlockDisplayProvider` | `BlockDisplay` 实体 | 1.19.4+ |
| `ItemDisplayProvider` | `ItemDisplay` 实体 | 1.19.4+ |
| ~~`ItemBERProvider`~~ | ~~`AbstractSkullBlock` / `DecoratedPotBlock`~~ | **已实现**（见 6.12），之前的判断有误 |

### 新增的两个访问器 mixin

```java
@Mixin(ZombieVillagerEntity.class)
public interface ZombieVillagerEntityAccess {
    @Accessor("conversionTicks") int jade$getConversionTicks();
}

@Mixin(SpawnerMinecartEntity.class)
public interface SpawnerMinecartEntityAccess {
    @Accessor("spawner") MobSpawner jade$getSpawner();
}
```

两个字段名都已在重映射后的 MC jar 中核对：`private int conversionTicks`、`private final MobSpawner spawner`。

## 6.10 修复：overlay 渲染头颅等 3D 物品图标时遮挡关系错误

### 现象

悬停玩家头颅 / 生物头颅（以及其他 `gui3d` 物品）时，tooltip 里的图标是 3D 方块模型，
但模型各面的前后关系是乱的：该被挡住的面画在前面，该露出的面反而被盖住。

### 根因

`OverlayRenderer.renderOverlay478757` 在整段 tooltip 渲染前调用了 `GlStateManager.disableDepthTest()`：

```java
GlStateManager.disableDepthTest();   // ← 整段 tooltip 都没有深度测试
GlStateManager.pushMatrix();
...
root.render(0, 0, maxWidth, maxHeight);
```

2D 元素（背景、文字、进度条）都是共面/互不重叠的，关掉深度测试没问题；
但 3D 物品模型的多个面在屏幕上**互相重叠**，没有深度测试就只能按模型里的顶点顺序覆盖，
于是出现“遮挡关系不对”。

原版 1.20 从不关闭深度测试（`renderOverlay` 只在结尾 `RenderSystem.enableDepthTest()`）。

### 修复

不能简单地全局打开深度测试 —— 1.12 的**快捷栏背景**是用 `drawOffset = -90` 画的，
即 z=-90，比 tooltip 的 z=1 **更靠近镜头**（GUI 空间 z 越大越靠前）。
全局开启深度测试会让快捷栏背景把 tooltip 抠掉一块。

所以改成**只在物品图标渲染时局部开启**：

```java
Lighting.turnOnGui();
//port: 3D item models (skulls, blocks) have overlapping faces in GUI space; without the
//depth test they are painted in model order and occlude each other incorrectly
GlStateManager.enableDepthTest();
mc.getItemRenderer().renderGuiItem(stack, 0, 0);
...
GlStateManager.disableDepthTest();
```

物品图标位于 GUI z ≈ 101（tooltip 的 z + `setupGuiTransform` 的 100），
比快捷栏背景（z=-90）更靠前，所以局部开启深度测试后图标依然完整显示，而它自身的面能正确排序。

### 顺带修复：图标之后的状态污染（改为保存/恢复，而不是写死）

`drawItem` 结尾原本是 `GlStateManager.disableBlend()`，而 tooltip 的绘制顺序是 **背景 → 图标 → 文字**，
所以图标之后的所有文字都是在**关闭混合**的状态下画的：

- 文字不参与 tooltip 的淡入淡出（`alpha` 动画只作用于背景）；
- 1.12 字体图集是 1bit alpha，平时看不出，高清字体包里会发硬。

但把结尾直接改成 `enableBlend()` 同样是错的 —— 1.12 的 `ItemRenderer.renderGuiItemModel`
**自己会 `enableBlend()` 且从不恢复**，所以 `drawItem` 不能假设进入时的状态：

| 结尾写法 | 后果 |
|---|---|
| `disableBlend()`（原来） | 调用方原本开着混合 → 被关掉（文字不参与淡入） |
| `enableBlend()`（上一版） | 调用方原本关着混合 → 被打开（反向污染） |
| **保存/恢复（现在）** | 进入什么状态就还原什么状态 ✓ |

```java
boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
...
if (depth) GlStateManager.enableDepthTest(); else GlStateManager.disableDepthTest();
if (blend) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
```

`renderOverlay478757` 也做了同样处理（它需要临时 `disableDepthTest()` 避开快捷栏背景的 z=-90，
结束后按进入时的状态还原，而不是写死 `enableDepthTest()`）。

其余状态确认无需恢复：`enableTexture()`、`Lighting.turnOnGui()/turnOff()`、`alphaFunc(GL_GREATER, 0.1)`
在 1.12 GUI 里都是默认值；`shadeModel` 由 `drawGradientRect` 自己设置并在结束时还原成 FLAT。

## 6.11 全项目渲染状态审计（GL state leak）

把所有调用 `GlStateManager` / `GL11` / `Lighting` 的文件（共 12 个）逐个方法过了一遍，
重点看「改了状态但没恢复」的地方。

### 发现并修复的泄露

| 方法 | 原来改了什么 | 后果 | 现在 |
|---|---|---|---|
| `DisplayHelper.fill` | `enableBlend` → **`disableBlend`**；`disableTexture` → `enableTexture` | 调用方开着混合被关掉：之后的文字不参与 alpha 淡入、字形发硬 | 保存/恢复 blend、texture |
| `DisplayHelper.drawGradientRect` | 同上 + `shadeModel(SMOOTH)` → 硬编码 `FLAT` | 同上；调用方若是 SMOOTH 会被改成 FLAT | 保存/恢复 blend、texture、shadeModel |
| `DisplayHelper.drawTextureRegion` | `enableBlend` → **`disableBlend`**；`disableAlphaTest` → 硬编码 `enableAlphaTest` | 同上；调用方若关着 alpha test 会被打开 | 保存/恢复 blend、alphaTest |
| `DisplayHelper.drawItem` | `enableBlend`/`enableDepthTest` → 硬编码 `disableBlend`/`disableDepthTest` | 上一轮已修：改为保存/恢复 blend、depth | ✓ |
| `OptionsList.drawFooterGradient` | `enableBlend` → **`disableBlend`**；`shadeModel` 硬编码 `FLAT` | 配置界面底栏渐变之后，按钮文字失去混合 | 保存/恢复 blend、texture、shadeModel |
| `OverlayRenderer.renderOverlay` | `disableDepthTest` → 硬编码 `enableDepthTest` | 上一轮已修：改为保存/恢复 blend、depth | ✓ |

### 检查后确认无需修改的

| 位置 | 改动 | 为什么安全 |
|---|---|---|
| `OptionsNav.render` / `OptionsList.render`（脏土、锚点线） | `bind(texture)`、`color4f(0.35/0.125)`、`disableLighting`、`disableFog` | 颜色在方法末尾复位为 `1,1,1,1`；纹理绑定由后续元素自行重绑；关闭光照/雾就是 GUI 默认状态 |
| `JadeButton.render` | `enableBlend()` + `blendFunc` | 只开不关，GUI 默认就是开 |
| `SliderOptionValue.Slider.render` | 同上 | 同上 |
| `HealthElement` / `ArmorElement.drawIcon` | `enableBlend`、`blendFunc`、`color4f(1,1,1,1)` | 只开不关；颜色复位 |
| `SpecialTextElement.render` | `pushMatrix` / `translate` / `scale` / `popMatrix` | 严格成对 |
| `HomeConfigScreen.render` | 5 处 `pushMatrix`/`popMatrix` | 全部成对（其中一处是 pop 后立即 push，栈深不变） |
| `BaseOptionsScreen.drawTooltipAt` | 关 rescaleNormal / lighting / depth，结尾重新开启 | 与 vanilla 1.12 `drawHoveringText` 完全一致的写法（结尾恢复 GUI 物品光照） |
| `DisplayHelper.pushScissor` / `popScissor` | `glEnable/glDisable(GL_SCISSOR_TEST)` | 调用点全部成对（`OptionsList`、`OptionsNav` 各一处）；`popScissor` 无条件关闭，而 GUI 默认就是关闭 |
| `ItemStackMixin` / `BossHealthOverlayMixin` / `GameGuiMixin` / `ChatScreenMixin` / `ClientLevelMixin` | 无任何 GL 调用 | — |

### 有界但未处理的

`TextRendererMixin.jade$applyGlint()` 在每个字形前调用 `color4f(r, g, b, a * factor)`，
绘制结束后颜色会停在最后一个字形的 glint 值。但：

- glint 只在 `HomeConfigScreen` 的标题上启用，画完立刻 `jade$setGlint(NaN, NaN)`；
- 任何后续文字绘制都会在 `TextRenderer.draw` 开头重新 `color4f`；
- 标题之后紧跟的是按钮（自己设 `color4f(1,1,1,1)`）和 credit 文字。

因此实际不可观测，没有加额外注入（在 `draw` 的 RETURN 处还原需要精确匹配 `draw(Ljava/lang/String;FFIZ)I` 描述符，收益不抵风险）。

## 6.12 实现 `ItemBERProvider`（头颅图标显示真实皮肤）

### 之前判断错误的地方

我先前认为「1.12 的 `SkullItem` 没有物品级 BER，塞 `SkullOwner` 也不会改变 GUI 图标」——**这是错的**。
1.12 原版确实有物品级 BER：`net.minecraft.client.render.entity.BlockEntityItemRenderer`
（MCP 名 `TileEntityItemStackRenderer`），调用链是：

```java
// ItemRenderer.renderItem
if (model.isCustomRenderer()) {           // skull 物品模型是 builtin/entity
    BlockEntityItemRenderer.INSTANCE.render(item);
}
```

而 `BlockEntityItemRenderer` 对 `Items.SKULL` 的处理正是读 `SkullOwner`：

```java
if (nbtCompound.contains("SkullOwner", NbtTypes.COMPOUND)) {
    gameProfile = NbtUtils.readProfile(nbtCompound.getCompound("SkullOwner"));
} else if (nbtCompound.contains("SkullOwner", NbtTypes.STRING) && ...) {
    gameProfile = SkullBlockEntity.updateProfile(new GameProfile(null, name));
}
...
SkullRenderer.INSTANCE.render(..., gameProfile, ...);
```

`SkullRenderer` 的玩家头颅分支会拿 `profile` 去 `SkinManager.getTextures(profile)` 取皮肤；
取不到时用 `DefaultSkinUtils.getDefaultSkin(uuid)`（按 UUID 生成确定性默认皮肤）。

### 1.12 的实现

原版 `getIcon` 里是 `Minecraft#addCustomNbtData(stack, blockEntity)`（把整个方块实体 NBT 塞进
`BlockEntityTag`）。1.12 没有这个方法，而且头颅只需要一个字段，所以按你说的**只写特定 NBT**：

```java
@Override
public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
    if (accessor.getBlockEntity() != null) {
        ItemStack stack = accessor.getPickedResult();
        if (stack.isEmpty()) {
            return null;
        }
        if (accessor.getBlockEntity() instanceof SkullBlockEntity) {
            addSkullOwner(stack, (SkullBlockEntity) accessor.getBlockEntity());
        }
        return IElementHelper.get().item(stack);
    }
    return null;
}

private static void addSkullOwner(ItemStack stack, SkullBlockEntity skull) {
    GameProfile profile = skull.getProfile();
    if (profile == null) {
        return;
    }
    NbtCompound nbt = stack.hasNbt() ? stack.getNbt().copy() : new NbtCompound();
    nbt.put("SkullOwner", NbtUtils.writeProfile(new NbtCompound(), profile));
    stack.setNbt(nbt);
}
```

- `NbtUtils.writeProfile` 是 1.12 原版自己用的方法（`SkullItem` 里就有一模一样的调用），
  写出 `{Name, Id, Properties}` 复合标签，正好是 `NbtUtils.readProfile` / `BlockEntityItemRenderer` 期望的格式。
- `stack.getNbt().copy()` 保证不改动方块实体的数据，`accessor.getPickedResult()` 每次都是新 ItemStack。
- 非玩家头颅（骷髅/凋灵/僵尸/苦力怕/龙）的 `profile` 为 null，直接跳过，用 metadata 走默认分支 ✓。
- `isRequired() = true`，所以和原版一样不产生配置项。

### 注册

```java
registration.registerBlockComponent(PlayerHeadProvider.INSTANCE, SkullBlock.class);
registration.registerBlockIcon(ItemBERProvider.INSTANCE, SkullBlock.class);
```

原版是 `AbstractSkullBlock` + `DecoratedPotBlock`；1.12 只有一个 `SkullBlock`（覆盖所有头颅），
装饰陶罐 1.20 才有，跳过。

### 效果

- 玩家头颅：图标显示该玩家的皮肤（皮肤未缓存时先显示按 UUID 生成的默认皮肤，解析完成后变正版皮肤）
- 生物头颅：`profile == null`，走 metadata，图标本来就是正确的
- 与 6.10 的深度测试修复配合，3D 头颅模型的各面遮挡正常

## 6.13 修复 harvest 采集工具：大量方块不显示

### 现象

泥土、草方块、沙子、沙砾、粘土、原木、木板等方块**不显示采集工具**，只有石头类正常。

### 根因

`SimpleToolHandler.test` 用的是：

```java
if (tool.canMineBlock(state) && tool.getMiningSpeed(state) > 1) { return tool; }
```

`ItemStack#canMineBlock` 在 1.12 里**不是**「这个工具能否采集这个方块」，而是原版给少数特例写的覆盖：

| 工具 | `canMineBlock` 的实际实现 |
|---|---|
| `PickaxeItem` | 矿石/黑曜石的**等级判定** + `Material.STONE/IRON/ANVIL` |
| `ShovelItem` | `return block == SNOW_LAYER ? true : block == SNOW;` ← **只有雪** |
| `SwordItem` | `return block == WEB;` |
| `ShearsItem` | `WEB / REDSTONE_WIRE / TRIPWIRE` |
| `AxeItem` / 其他 | 没有覆盖 → `Item.canMineBlock` 返回 **false** |

所以：斧头永远匹配不到原木/木板，铲子除了雪什么都不匹配，锄头（1.12 的锄头根本没有挖掘加速）也永远匹配不到。

原版 1.20 用的是 `tool.isCorrectToolForDrops(state)`（类型对 + 等级够），1.12 的等价判定是「这个工具确实能加快挖掘」：

```java
// ToolItem#getMiningSpeed：命中 effectiveBlocks 就返回 tier 的挖掘速度，否则 1.0
// PickaxeItem#getMiningSpeed：Material.STONE/IRON/ANVIL 直接返回 tier 速度
```

### 修复

```java
private static boolean isEffective(BlockState state, ItemStack tool) {
    if (tool.getMiningSpeed(state) <= 1.0F) {
        return false;                       // 工具类型不对，挖这个方块没有加成
    }
    // 与 PlayerInventory#canMineBlock 相同的短路：需要工具的材料才检查等级
    return state.getMaterial().isToolNotRequired() || tool.canMineBlock(state);
}
```

- `getMiningSpeed > 1` 负责「工具类型对不对」：斧头 vs 原木 = 2.0、铲子 vs 泥土 = 2.0、
  镐子 vs 石头 = 2.0、镐子 vs 玻璃 = 1.0（1.12 镐子对玻璃没有加成，所以不显示，符合 1.12 机制）
- `isToolNotRequired() || canMineBlock(...)` 负责「等级够不够」，只对**需要工具**的材料生效：
  `STONE` / `IRON` / `ANVIL`（镐子等级）、`SNOW` / `SNOW_LAYER`（铲子的覆盖正好在这里返回 true）、
  `COBWEB` / `BARRIER`。`WOOD`、`DIRT`、`SAND`、`GRASS`、`LEAVES`、`WOOL` 等都是 `toolNotRequired`，
  所以斧头/铲子不会再被 `canMineBlock` 的 false 挡住。

### 顺带：移除锄头 handler

1.12 没有 `HoeItem`，锄头就是普通 `Item`（`getMiningSpeed` 恒为 1.0），永远不会匹配。
原版注册锄头是因为 1.20 有 `mineable/hoe` 标签。已删除，避免留下永远不生效的死条目。

### 修复后的对照表

| 方块 | 材料 | 显示的工具 |
|---|---|---|
| 石头 / 圆石 / 矿石 | STONE | 木→石→铁→钻石镐（按等级） |
| 黑曜石 | STONE | 仅钻石镐 |
| 泥土 / 草 / 沙子 / 沙砾 / 粘土 / 灵魂沙 / 耕地 / 草径 / 混凝土粉末 | DIRT/GRASS/SAND… | 木→石→铁→钻石铲 |
| 雪块 / 雪层 | SNOW（需工具） | 铲（`canMineBlock` 正好覆盖） |
| 原木 / 木板 / 书架 / 箱子 / 南瓜 / 西瓜 / 梯子 / 木按钮 / 木压力板 | WOOD 等 | 木→石→铁→钻石斧 |
| 树叶 / 羊毛 / 藤蔓 / 高草 / 枯木 / 蜘蛛网 / 绊线 | LEAVES/WOOL… | 剪刀 |
| 蜘蛛网 | COBWEB | 剑 / 剪刀 |
| 玻璃 / 冰（镐子有加成的冰除外） | GLASS | 不显示（1.12 镐子对玻璃无加成） |
| 基岩 / 屏障 | — | 不显示（`destroySpeed < 0` 提前返回） |

## 6.14 harvest 二次复查：又找到 2 个 Bug

### Bug 1（重要）：`getText` 里的 `canHarvest` 用了同一个错误的 `canMineBlock`

```java
// 修复前
boolean canHarvest = held.canMineBlock(state) && held.getMiningSpeed(state) > 1;
```

这和 6.13 是同一个根因。后果比之前更隐蔽：

| 场景 | `requiresTool` | 修复前 `canHarvest` | 修复前显示 | 原版显示 |
|---|---|---|---|---|
| 泥土 + 手持铲子 | false | **false**（`ShovelItem.canMineBlock(泥土)` = false） | **什么都不显示** | ✔ |
| 原木 + 手持斧子 | false | **false**（`AxeItem` 没覆盖） | **什么都不显示** | ✔ |
| 石头 + 手持镐子 | false→true | true | ✔ | ✔ |
| 泥土 + 空手 | false | false | 不显示 | 不显示 |

因为标记的显示条件是 `if (requiresTool || canHarvest)`，泥土/原木这类 `toolNotRequired` 的方块
在**手持正确工具**时两个条件都是 false，于是连 ✔/✕ 都不会画出来。

修复：复用 6.13 的判定

```java
boolean canHarvest = SimpleToolHandler.isEffective(state, held);
```

`isEffective` 从 `private` 改为 `public static`，让 provider 和 handler 共用同一套逻辑。

### Bug 2：冒险模式判定过宽

```java
// 修复前
GameMode gameType = ClientProxy.getGameMode();
if (gameType == GameMode.ADVENTURE) {
    return;                     // 所有冒险模式一律不显示
}
```

原版是：

```java
if (gameType == GameType.ADVENTURE && player.blockActionRestricted(level, pos, gameType)) return;
```

也就是说：冒险模式下如果手持的物品对该方块有 `CanDestroy` 权限，原版**仍会显示**采集工具。
1.12 的对应逻辑在 `ClientPlayerInteractionManager#tryBreakBlock`：

```java
if (!player.canModifyWorld()) {
    ItemStack stack = player.getMainHandItem();
    if (stack.isEmpty() || !stack.hasMineBlockOverride(block)) return false;
}
```

已按 1.12 自己的写法改成：

```java
if (!player.canModifyWorld()) {
    ItemStack held = player.getItemInMainHand();
    if (held.isEmpty() || !held.hasMineBlockOverride(state.getBlock())) {
        return;
    }
}
```

顺带删掉了不再使用的 `GameMode` / `ClientProxy` 导入。

### 顺带对齐原版的两处细节

| 位置 | 原版 | 移植版（修复前） | 现在 |
|---|---|---|---|
| `SimpleToolHandler` 构造 | `Preconditions.checkArgument(!tools.isEmpty())` / `tools.size() == 1` | 无校验 | 已补回 |
| `ToolHandler` | 只 `extends IJadeProvider`，不重复声明 `getUid()` | 多声明了一个 `@Override Identifier getUid();` | 已删除 |

### 已知但未改的差异

- 1.12 的 `Blocks.DOUBLE_PLANT`（大型草/蕨，对应原版的 `LARGE_FERN`）没有加入剪刀列表。
  原因：`DOUBLE_PLANT` 一个方块涵盖向日葵/丁香/玫瑰/牡丹等花，整体加入会让花也显示剪刀；
  而 1.12 双生植物的掉落逻辑（`onMinedByPlayer` 掉 2 个高草）与原版 `LARGE_FERN` 也不完全对应，
  按变体细分又会让 `matchesBlock` 复杂化，收益不抵风险。矮草/蕨（`TALLGRASS` 的 grass/fern 变体）已覆盖。

### 复查确认无问题的部分

| 检查点 | 结果 |
|---|---|
| 创造/旁观模式开关 `MC_HARVEST_TOOL_CREATIVE` | ✓ |
| 不可破坏方块分支（`destroySpeed < 0 \|\| destroyProgress <= 0`）+ `MC_SHOW_UNBREAKABLE` | ✓ |
| `requiresTool` / `MC_EFFECTIVE_TOOL` 的提前返回 | ✓ 等价于原版 `requiresCorrectToolForDrops()` |
| `resultCache`（5 分钟）+ 资源重载时 `clearCache()` | ✓ 等价于原版 `ResourceManagerReloadListener` |
| 图标尺寸/偏移/缩放（`ITEM_SIZE 10x0`、`scale 0.75`、`translate(-1, offsetY)`） | ✓ |
| `✔/✕` 的 `scale 0.75`、`zOffset 800`、`size ZERO`、`translate(-3, 6.25 + offsetY)` | ✓ |
| `newLine` 两种布局（`tooltip.add` / `align RIGHT` + `append(0, ...)`） | ✓ |
| 优先级 `TooltipPosition.HEAD + 2000` | ✓ |
| 剑 = 蜘蛛网（1.12 `SwordItem.getMiningSpeed` 只对 web 有加成） | ✓ |
| 剪刀：`isShearable` + 显式方块表（vine/tripwire/deadbush/web/tallgrass） | ✓ 对应原版 `SHORT_GRASS/TALL_GRASS/FERN/DEAD_BUSH/COBWEB/VINE/TRIPWIRE` |
| `elements.forEach(e -> e.message(null))`（不进复述） | ✓ |

## 6.15 harvest 三次复查：逻辑无新 Bug，补上最后一块拼图

### 复查方式

把 `addon/harvest/` 的 4 个文件逐行对照原版，并把涉及的每个 1.12 API 反查原版实现。

### 核对结果

| 检查点 | 依据 | 结果 |
|---|---|---|
| `!player.canModifyWorld()` 是否等价于原版 `blockActionRestricted` | `GameMode#setAbilities`：`canModifyWorld = !restrictsWorldModification()`，而 `restrictsWorldModification()` = `ADVENTURE \|\| SPECTATOR` | ✓ 冒险/旁观都会命中，与原版一致 |
| 空手时 `isEffective` 会不会 NPE | `ItemStack#getItem()`：`cachedIsEmpty ? Item.byBlock(Blocks.AIR) : item`，空栈返回 AIR 而非 null | ✓ `getMiningSpeed` 返回 1.0，安全 |
| `✔/✕` 的 `scale(0.75)` / `zOffset(800)` 是否生效 | `TextElement#scale` 委托给 `SpecialTextElement`，后者 `pushMatrix` + `translatef(x, y+scale, zOffset)` + `scalef` + `popMatrix` | ✓ 生效 |
| `✔/✕` 的绿/红颜色 | `IThemeHelper.success/danger` 把颜色写进 `Style`，`DisplayHelper#drawText` 用 `getFormattedString()` 输出 § 颜色码 | ✓ 生效 |
| 镐子的等级判定 | `PickaxeItem#canMineBlock`：黑曜石 tier==3、钻石/绿宝石/金 tier>=2、铁/青金石/红石 tier>=1、其余 STONE/IRON/ANVIL | ✓ 与 `isEffective` 组合后等价于 `isCorrectToolForDrops` |
| 斧/铲的生效方块表 | `AxeItem.EFFECTIVE_BLOCKS`（木板/书架/原木/箱子/南瓜/西瓜/梯子/木按钮/木压力板）、`ShovelItem.EFFECTIVE_BLOCKS`（粘土/泥土/耕地/草/沙砾/菌丝/沙子/雪/雪层/灵魂沙/草径/混凝土粉末） | ✓ 全部由 `getMiningSpeed > 1` 自动覆盖 |
| 剪刀 | `ShearsItem#getMiningSpeed`：WEB/LEAVES = 15、WOOL = 5，其余 1.0 | ✓ 树叶/羊毛靠 `getMiningSpeed`，藤蔓/绊线/枯木/高草/蜘蛛网靠 `isShearable` + 显式表 |
| 剑 | `SwordItem#getMiningSpeed` 只对 WEB 返回 15 | ✓ `.addBlock(Blocks.WEB)` |
| 资源重载清缓存 | 原版 `ResourceManagerReloadListener` → `resultCache.invalidateAll()` | ✓ 移植版挂在 `ClientResourceLoaderEvents.END_RESOURCE_RELOAD` |

### 补上最后一块：大型草/蕨（`DOUBLE_PLANT`）

上一轮把它列为「已知差异」，这轮反查后确认**应该补**：

```java
// DoublePlantBlock#beforeMinedByPlayer
if (world.isClient || item.getItem() != Items.SHEARS || state.get(HALF) != Half.LOWER
        || !this.onMinedByPlayer(world, pos, state, player)) { ... }
```

只有**手持剪刀**时才会走 `onMinedByPlayer` 掉落 2 个高草 —— 也就是说双生草/蕨确实是「可剪切」的，
对应原版剪刀列表里的 `LARGE_FERN`。已加入 `CommonProxy#isShearable`：

```java
if (block == Blocks.DOUBLE_PLANT) {
    //port: only the grass/fern variants can be sheared (DoublePlantBlock#beforeMinedByPlayer)
    DoublePlantBlock.Variant variant = state.get(DoublePlantBlock.VARIANT);
    return variant == DoublePlantBlock.Variant.GRASS || variant == DoublePlantBlock.Variant.FERN;
}
```

按变体判断而不是整块加入，避免向日葵/丁香/玫瑰/牡丹也被当成可剪切方块。
`isShearable` 同时被 `ShearsToolHandler#test` 和 `HarvestToolProvider` 的 ✔ 判定使用，两处一起生效。

### 与原版剩余的无害差异

| 位置 | 原版 | 移植版 |
|---|---|---|
| `CHECK` / `X` | 静态 `Component` 常量，复用时传 Component | 直接传字符串给 `IThemeHelper.success/danger`（结果相同） |
| `appendTooltip` 里的 `//TODO: high priority handlers?` 注释 | 有 | 无（无实际逻辑） |

## 6.16 harvest 四次复查：布局/资源链路逐项核对

这轮换角度，不再看判定逻辑，而是把「配置 → 语言文件 → 优先级排序 → tooltip 行布局 → 元素渲染」整条链路走了一遍。

### 核对结果（全部正常）

| 环节 | 依据 | 结果 |
|---|---|---|
| 配置项语言键 | `MC_SHOW_UNBREAKABLE` = `minecraft:harvest_tool.show_unbreakable` → `config.jade.plugin_minecraft.harvest_tool.show_unbreakable` | ✓ 存在（我一开始按 `plugin_minecraft.show_unbreakable` 查，误报了一次） |
| 其余 harvest 语言键 | `harvest_tool` / `.effective_tool` / `.new_line` / `.creative` / `.creative_desc` / `jade.harvest_tool.unbreakable` | ✓ 全部存在 |
| 优先级排序方向 | `PriorityStore`：`list.sort(Comparator.comparingInt(store::byKey))` 升序；`TooltipPosition.HEAD = -10000` | ✓ 名字 provider(-10000) 先跑，harvest(-8000) 后跑 → `append(0, ...)` 正好把工具挂到名字那一行 |
| `append(0, List)` 语义 | `Tooltip#append`：空 tooltip 时 `add(element)` 建第 0 行，之后都进第 0 行 | ✓ 工具不会跑到第一行、名字跑到第二行 |
| `✔/✕` 元素路径 | `IElementHelper.text(...).scale(0.75F)` → `TextElement#scale` 委托 `SpecialTextElement`（`pushMatrix` + `translatef(x, y+scale, zOffset)` + `scalef` + `popMatrix`） | ✓ scale 与 zOffset 都生效 |
| `✔/✕` 颜色 | `IThemeHelper.success/danger` 写进 `Style`，`drawText` 用 `getFormattedString()` 输出 § 码 | ✓ 绿/红正确 |
| 工具图标元素 | `ItemStackElement#getSize` = `floor(18*scale)`、`render` = `drawItem(x+1, y+1, stack, scale, text)`，`size(10,0)` 覆盖布局尺寸 | ✓ 与原版逐行相同 |
| 资源重载 | `ClientResourceLoaderEvents.END_RESOURCE_RELOAD` → `HarvestToolProvider.clearCache()` | ✓ 等价于原版 `ResourceManagerReloadListener` |
| 配置分类覆盖 | `setConfigCategoryOverride(MC_HARVEST_TOOL, block)` | ✓ 显示在 plugins 界面的 block 分类下 |

### 判定逻辑再确认（无误报/漏报）

`isEffective` 的第二个条件只在**需要工具**的材料上生效，逐个核对 1.12 的 `Material.setRequiresTool()` 集合：

| 材料 | 需要工具 | 对应工具 | `canMineBlock` 是否覆盖 |
|---|---|---|---|
| `STONE` / `IRON` / `ANVIL` | 是 | 镐 | ✓ 覆盖三种材料 + 矿石等级 |
| `SNOW` / `SNOW_LAYER` | 是 | 铲 | ✓ `ShovelItem#canMineBlock` 正好返回 true |
| `COBWEB` | 是 | 剑 / 剪刀 | ✓ 两者都覆盖 |
| `BARRIER` | 是 | — | 硬度 -1，提前返回 |
| 其余（`WOOD`/`DIRT`/`SAND`/`GRASS`/`LEAVES`/`WOOL`…） | 否 | 斧/铲/剪刀 | 短路跳过，不会被 `canMineBlock` 的 false 挡住 |

反向也成立：`getMiningSpeed <= 1` 的工具一律不显示，所以不存在「工具没加成却显示」的误报。

### 结论

本轮**没有发现新 Bug**。harvest 的判定、布局、资源链路现在与原版一致；剩下的只有两处无实际影响的行文差异（`CHECK`/`X` 用字符串而非静态 Component、少一句 `//TODO` 注释）。

## 6.17 全 addon 包复查：又找到 4 个 Bug

把 `snownee.jade.addon` 下全部文件（core 5 + debug 3 + harvest 4 + vanilla 40）逐个与原版对照，
并反查涉及的 1.12 原版实现。

### Bug 1：配置文件里的非法 id 会让启动崩溃（`CorePlugin`）

```java
// 修复前
Class<? extends Entity> entityType = Entities.REGISTRY.get(new Identifier(id));
```

原版用 `ResourceLocation.tryParse(id)` —— 非法字符串返回 `null` 直接跳过。
1.12 的 `new Identifier(String)` **会抛 `InvalidIdentifierException`**，而这段代码在 `registerClient`
（mod 初始化阶段）执行，`hide-entities.json` 里写错一个 id 就是启动崩溃。

已给两个循环加上 try/catch，行为与原版一致。

### Bug 2：音符盒的乐器判断顺序错（`NoteBlockProvider`）

1.12 的 `NoteBlockBlockEntity#playNote` 是**连续 if 覆盖**，方块判定在材料判定**之后**：

```java
if (material == Material.STONE) i = 1;     // 骨块是 Material.STONE
...
if (block == Blocks.BONE_BLOCK) i = 9;     // 再覆盖成木琴
```

移植版用了提前 `return`，于是**骨块 → 1（贝斯鼓）**而不是 9（木琴）✗。
已改成与原版相同的覆盖式写法。

### Bug 3：音符盒的音高永远是旧值（`NoteBlockProvider`）

1.12 的 note 存在**方块实体**里（`NoteBlockBlockEntity.note`），而 `NoteBlockBlockEntity`
**没有 `createUpdatePacket`** —— 玩家右键调音后，服务端只 `markDirty()`，客户端拿不到新值，
Jade 会一直显示旧音高，直到重新加载区块 ✗。

原版 1.20 的 note 是**方块状态属性**，天然同步，所以原版不需要服务端数据。

已让 `NoteBlockProvider` 实现 `IServerDataProvider<BlockAccessor>`，由服务端发送
`Note` + `Instrument`，并在 `VanillaPlugin` 注册
`registerBlockDataProvider(NoteBlockProvider.INSTANCE, NoteBlockBlockEntity.class)`；
客户端优先读服务端数据，读不到再退回方块实体（单机/离线兜底）。

### Bug 4：胡萝卜、马铃薯、可可豆不显示生长进度（`CropProgressProvider`）

原版靠 `block instanceof CropBlock` + `AGE_7`/`AGE_2`/`AGE_3` 属性覆盖所有作物。
1.12 没有 `CropBlock` 基类，移植版只写了小麦、甜菜根、地狱疣三种：

| 1.12 作物 | AGE 范围 | 修复前 |
|---|---|---|
| 小麦 `WheatBlock` | 0-7 | ✓ |
| 胡萝卜 `CarrotsBlock` | 0-7 | ✗ 不显示 |
| 马铃薯 `PotatoesBlock` | 0-7 | ✗ 不显示 |
| 甜菜根 `BeetrootsBlock` | 0-3 | ✓ |
| 地狱疣 `NetherWartBlock` | 0-3 | ✓ |
| 可可豆 `CocoaBlock` | 0-2 | ✗ 不显示 |

已补上胡萝卜、马铃薯、可可豆（对应原版的 `AGE_7` 和 `AGE_2` 分支）。

### Bug 5：马的最大速度常量用错（`HorseStatsProvider`）

```java
// 修复前
private static final double MAX_MOVEMENT_SPEED = 0.225 * 42.16;
```

原版用 `AbstractHorse.MAX_MOVEMENT_SPEED`（= 0.3375）。1.12 的马速度随机范围是
`HorseBaseEntity#getChildMovementSpeedBonus`：`(0.45 + rnd*0.3 + rnd*0.3 + rnd*0.3) * 0.25` → **0.1125~0.3375**，
而 0.225 只是 `HorseBaseEntity` 构造函数里的**默认基础速度**。

结果：按住显示详情时，速度会显示成 `x / 9.49`（0.225×42.16）而不是 `x / 14.23` ✗。已改为 0.3375。

### 其余 provider 核对结果（无问题）

| provider | 核对要点 |
|---|---|
| `RedstoneProvider` | 拉杆/中继器/比较器/红石线 power 属性/漏斗锁定；1.12 的 `POWERED_REPEATER`、`POWERED_COMPARATOR` 分开的方块已覆盖；`ComparatorBlock.Mode` ✓ |
| `StatusEffectsProvider` | 1.12 无 `MobEffectInstance` 序列化组件，改为整包 `toNbt`/`fromNbt` + `isHarmful()`；无限时长用 `duration >= 1000000` 判定 ✓ |
| `FurnaceProvider` | 三格布局、进度条、`total` 防 0 ✓ |
| `MobGrowthProvider` / `MobBreedingProvider` | `-getBreedingAge()` / `getBreedingAge()` ✓ |
| `ChickenEggProvider` | `layEggCooldown` ✓ |
| `JukeboxProvider` | 唱片名 + `stripColor` ✓ |
| `CommandBlockProvider` | `canUseGameMasterBlocks()` → `canUseCommand(2, "")`；`getCommandExecutor()` ✓ |
| `BrewingStandProvider` | `brewTime`/`fuel` → `getData(0)`/`getData(1)` ✓ |
| `TNTStabilityProvider` | `UNSTABLE` → `EXPLODE` ✓ |
| `ItemTooltipProvider` | 跳过首行、过滤模组名、超宽截断（`trim(245)`）✓（仅丢失粗体/斜体，颜色本来就被原版剥掉） |
| `FallingBlockProvider` / `ItemFrameProvider` / `PaintingProvider` / `EntityHealthProvider` / `EntityArmorProvider` / `ArmorStandProvider` | 字段/方法名替换正确 ✓ |
| `MobSpawnerProvider` / `PlayerHeadProvider` / `EnchantmentPowerProvider` / `ItemBERProvider` / `TotalEnchantmentPowerProvider` / `ZombieVillagerProvider` / `VillagerProfessionProvider` / `AnimalOwnerProvider` | 见 6.9 / 6.12 / 6.13 各节 ✓ |
| `core` 其余 + `debug` 全部 | ✓ |

### 语言键检查

addon 包里用到的 38 个键（含 `jade.instrument.*`、`tooltip.jade.mode_*`、`jade.potion`、
`tooltip.jade.crop_*`、`jade.horseStat.*`、`jade.llamaStrength` 等）全部存在于 `en_us.json` ✓。

## 6.18 任意 RGB 文字渲染（替代 16 色近似）

### 之前为什么只能近似

原版 1.20 的 `ThemeHelper#colorStyle(int)` 是 `Style.EMPTY.withColor(int)` —— `TextColor` 能存任意 RGB。
1.12 的 `Style` 只有一个 `Formatting color` 字段，所以移植版只能把主题色（`#55FF55`、`#FFF3CD`、
坐标用的 `0xef9a9a`/`0xa5d6a7`/`0x90caf9` …）就近映射到 16 色之一。

### 1.12 其实能做到

关键点：**1.12 的 `TextRenderer#draw(String, x, y, int color, boolean shadow)` 本来就接受任意 ARGB**
—— 它内部直接 `GlStateManager.color4f(r, g, b, a)`。受限的只是 `Style`，不是渲染器。

所以只要把「精确颜色」带到绘制调用即可：

1. **新增 `snownee.jade.util.RgbText`**（实现 `Text`，不继承 `BaseText`）
   - 持有 `delegate` + 精确 `color`
   - `getString()` / `getContent()` / `getFormattedString()` 全部委托给 delegate（所以复述、JSON、物品名等只认 `Style` 的路径照常工作）
   - `iterator()` 只返回自身，作为渲染树的叶子节点
   - `copy()` 保留颜色（`TranslatableText#iterator` 会 `copy()` 参数，颜色不会丢）

2. **`ThemeHelper#colorStyle(int)` 被替换为 `ThemeHelper#color(Text, int)`**

原版是 `public static Style colorStyle(int color) { return Style.EMPTY.withColor(color); }`。
1.12 的 `Style` 装不下 RGB，所以「返回一个带颜色的 Style」这件事在这个版本**不可能成立** ——
与其保留一个「就近 16 色」的假实现，不如把语义改成它真正能表达的东西：

```java
/**
 * 1.20's {@code Style.EMPTY.withColor(int)} equivalent. 1.12's Style can only hold one
 * of the 16 Formatting colors, so the exact color is carried by RgbText and
 * DisplayHelper#drawText hands it straight to the font renderer.
 */
public static Text color(Text text, int color) {
    RgbText rgbText = new RgbText(text, color);
    Style style = text.getStyle();
    if (!style.isEmpty()) {
        // keep bold/italic/..., drop the old 16 color one, the wrapper overrides it anyway
        rgbText.setStyle(style.copy().setColor((Formatting) null));
    }
    return rgbText;
}
```

`nearestFormatting` / `toHsl` 这两个「拟合 16 色」的辅助方法已**整体删除**，代码里不再有任何颜色近似。
`protected Text color(Object, int)` 简化为转调：

```java
protected Text color(Object componentOrString, int color) {
    Text text = componentOrString instanceof Text
        ? (Text) componentOrString
        : new LiteralText(Objects.toString(componentOrString));
    return color(text, color);
}
```

3. **`DisplayHelper#drawText(Text, ...)`** 改成遍历文本树逐节点绘制：

```java
for (Text node : text) {
    String content = node.getContent();
    if (content.isEmpty()) continue;
    if (node instanceof RgbText) {
        font.draw(node.getStyle().asString() + content + Formatting.RESET, curX, y, ((RgbText) node).getColor(), shadow);
    } else {
        font.draw(node.getStyle().asString() + content + Formatting.RESET, curX, y, color, shadow);
    }
    curX += font.getWidth(content);
}
```

- `RgbText` 节点 → 用精确 ARGB 作为基色
- 其他节点 → 沿用原来的 `§` 格式码 + 基色
- **嵌套也生效**：`TranslatableText("jade.blockpos", RgbText(X), RgbText(Y), RgbText(Z))` 遍历时会
  分别命中三个 `RgbText` 节点，三个坐标各自渲染成自己的颜色（这正是之前「只有 Z 是蓝色」那类问题的根因之一）

### 顺带修正的两处

| 位置 | 原来 | 现在 |
|---|---|---|
| `drawText` 在 `text.shadow == false` 时 | `Formatting.strip(getFormattedString())` —— **把整行格式全剥掉**（颜色/粗体都没了） | 不再剥离，只把 `shadow` 参数设为 false（与原版 `drawString(..., shadow)` 一致） |
| `colorStyle` | 就近 16 色拟合（`nearestFormatting`） | **删除**，改为 `color(Text, int)`，颜色由 `RgbText` 精确携带；`nearestFormatting`/`toHsl` 一并移除 |

### 自审后修正的 5 处

| # | 问题 | 修正 |
|---|---|---|
| 1 | `RgbText` 自己存了一份 `style`，`copy()` 时丢失（`TranslatableText#iterator` 会 `copy()` 参数，粗体等格式就没了） | 去掉独立字段，`setStyle`/`getStyle` 直接转发给 delegate —— `copy()` 自然保留，`getFormattedString()` 与 `getStyle()` 也不会不一致 |
| 2 | 颜色没补 alpha：`DistanceProvider` 的 `0xef9a9a` 是 **0x00RRGGBB**，`applyAlpha` 算出来 alpha=0，随后被 `TextRenderer` 强制补成不透明 | 构造时归一化：`(color & 0xFF000000) == 0 ? color \| 0xFF000000 : color`（和 `TextRenderer#draw` 的处理一致） |
| 3 | 给 RGB 文字乘了 tooltip 的淡入 alpha | 1.20 里带样式文字的颜色来自 `TextColor`（不透明），**不参与淡入**；改成 RGB 段直接用不透明颜色，与 1.20 行为一致 |
| 4 | `ThemeHelper.color` 把去掉颜色的 style 设在**包装器**上，而 `getStyle()`/`getFormattedString()` 读的是 delegate，两者会不一致 | 改为设在 delegate 上（`style.copy().setColor(null)` —— `copy()` 会把父样式里继承来的颜色也实体化，所以能彻底去掉旧颜色） |
| 5 | 每个节点末尾多拼了一个 `§r` | 每次 `font.draw` 都会用传入的颜色重新设置状态，`§r` 是多余的；去掉后每节点少 2 个字符 |

### 关于优化（做了 2 处，其余确认无需改）

| 优化 | 说明 |
|---|---|
| `drawText` 加单节点快路径 | 最常见的场景是「整段文字一个 `RgbText`」（`info`/`success`/`title` 等），直接一次 `font.draw` 就返回，省掉迭代器和循环 |
| `RgbText#iterator` 用 `Iterators.singletonIterator(this)` | 原来 `Collections.singletonList(this).iterator()` 每次要分配 list + iterator 两个对象，现在只分配一个 |

确认**不需要**改的：

- `Style#asString()` 对空样式只是几次判空（返回 `""` 或父样式的字符串），不需要额外加 `isEmpty()` 分支 —— 加了反而会在「样式只有父级有内容」时出错（`isEmpty()` 不检查父级）。
- 遍历 `Text` 的迭代器开销与旧的 `getFormattedString()` 完全一致（两者都会走 `BaseText.iterator`），没有额外性能损失。
- `RgbText` 只在 `ThemeHelper.color(...)` 里创建，每次 tooltip 构建每段文字一个实例，可忽略。

### 调用方改造

`DistanceProvider#display` 之前直接拿 `Style`：

```java
// 修复前：坐标颜色被压成 16 色
return new LiteralText(Integer.toString(i)).setStyle(ThemeHelper.colorStyle(colors[colorIndex]));
// 修复后：交给 RgbText
return ThemeHelper.color(new LiteralText(Integer.toString(i)), colors[colorIndex]);
```

这样 `new TranslatableText("jade.blockpos", display(X), display(Y), display(Z))` 的三个参数都是
`RgbText`，遍历文本树时各自命中，X/Y/Z 分别渲染成 `#ef9a9a` / `#a5d6a7` / `#90caf9`（亮色方案换成
`#b02a37` / `#198754` / `#0a58ca`），不再有「只有 Z 是蓝色」那类问题。

`IThemeHelper` 的 `info` / `success` / `warning` / `danger` / `failure` / `title` / `seconds` 全部经过
`color(...)`，因此以下位置现在都是精确颜色：

- 主题 `text.colors`（dark/create/top/waila 及自定义主题 JSON 里的 `#RRGGBB`）
- `DistanceProvider` 的 X/Y/Z 坐标（`0xef9a9a` / `0xa5d6a7` / `0x90caf9`，亮色方案另三种）
- `HealthElement` / `ArmorElement` 的数值颜色
- `HarvestToolProvider` 的 ✔/✕（`success` / `danger`）
- `RedstoneProvider` 的开关/功率、`CropProgressProvider` 的成熟度、`HorseStatsProvider` 的数值、
  `MobGrowthProvider` 的倒计时等所有用 `IThemeHelper` 上色的文字

### 说明

- 主题 JSON 里的颜色是 `#RRGGBB`，`Color.valueOf` 会补成 `0xFFRRGGBB` ✓ 现在会原样渲染。
- 物品名（`IWailaConfig.IConfigFormatting#getItemModNameStyle`）仍然走 `Style`，因为原版那里也是 `Style`
  （用户可配置粗体/斜体/下划线），不受影响。
- 复述（narration）走 `getString()`，不会带上颜色 ✓。

## 6.19 theme 部分复查：找到 1 个真 Bug + 3 处细节

对照 `api/theme/*`、`impl/theme/*`、`api/ui/BoxStyle`、`api/ui/ColorPalette`、4 个主题 JSON、
`WailaConfig.ConfigOverlay` 的主题字段、`WailaConfigScreen` 的主题选项、`BoxElement#setThemeIcon`。

### Bug：`itemAmountColor` 解析了却从未使用，物品数量根本不渲染

原版 `DisplayHelper` 自己实现了一套 `renderGuiItemDecorations`：

```java
if (stack.getCount() != 1 || text != null) {
    String s = text == null ? INSTANCE.humanReadableNumber(stack.getCount(), "", false, null) : text;
    boolean smaller = s.length() > 3;
    float scale = smaller ? 0.5F : 0.75F;
    int x = smaller ? 32 : 22;
    int y = smaller ? 23 : 13;
    ...
    int color = IThemeHelper.get().theme().text.itemAmountColor();   // ← 主题色
    guiGraphics.drawString(font, s, i + x - font.width(s), j + y, color, true);
}
```

移植版为了不显示原版白色数字，把副本 `setSize(1)` 后直接交给 vanilla 的
`renderGuiItemDecorations` —— 结果 **数量完全不画**，主题里的 `itemAmountColor` 也就成了死配置。

已补上 `DisplayHelper#renderAmount`（与上面逐行对应：`humanReadableNumber`、>3 位缩到 0.5、
`itemAmountColor`、z=200、带阴影），`drawItem` 里先让 vanilla 只画耐久条/冷却，再调 `renderAmount`：

```java
mc.getItemRenderer().renderGuiItemDecorations(mc.textRenderer, overlayRender, 0, 0, null);
renderAmount(mc.textRenderer, stack, text);
```

### 细节 1：`changeOpacity` 没有钳制

1.20 用 `Codec.floatRange(0, 1)` 解析，移植版直接 `getAsFloat()`。虽然 `ConfigOverlay#setAlpha`
内部会 clamp，但为了与原版一致，已在解析时 `MathHelper.clamp(..., 0, 1)`。

### 细节 2：`modName` 少了原版的样式缓存

原版用 `Style[3]` 缓存 `itemStyle / themeStyle / 合并结果`（按引用比较），移植版每次重新
`themeStyle.copy().setParent(itemStyle)` —— 每 tick 每个 tooltip 都分配一个 Style。已补回同款缓存。

### 细节 3：`BoxElement#setThemeIcon` 少了一次尺寸失效

原版最后是 `icon = theme.iconSlotSpriteCache.size(null);` —— 因为图标槽里的内容刚被
`clear()` + `add(icon)` 换掉，必须让缓存的尺寸失效。移植版直接 `icon = theme.iconSlotSpriteCache;`
会让图标槽一直沿用第一次算出的尺寸。

（内置主题都没有 `iconSlotSprite`，所以这个分支平时不触发；但自定义主题会用到。）

### 其余核对结果（无问题）

| 文件 | 结论 |
|---|---|
| `api/theme/Theme.java` | ✓ 仅 `Optional` → `@Nullable` |
| `api/theme/TextSetting.java` | ✓ record → 普通类 + 访问器 |
| `api/theme/IThemeHelper.java` | ✓ 方法集合完全一致（`MutableComponent` → `Text`） |
| `impl/theme/ThemeHelper` 的 `parse` | ✓ 各字段默认值与 codec 一致（`nestedBoxStyle`/`viewGroupStyle` 用 `clone()` 更安全；`tooltipStyle` 缺失时抛异常→主题被跳过，与原版一致） |
| `reload()` | ✓ version 100~199 校验、`themesHash` 重算、`autoEnable`、`changeRoundCorner`/`changeOpacity` 应用 |
| 4 个内置主题 JSON | ✓ 与原版逐字节相同 |
| 主题切换 UI | ✓ `overlay_theme` 选项、切换后回填 square/alpha、`hidden` 过滤、名称 `jade.theme.ns.path` |
| `BoxStyle` / `ColorPalette` | ✓（数组长度校验见 6.6） |
| `Theme.iconSlotInflation` | ✓ 用于图标槽 padding |

## 6.20 移植 universal 包（物品部分）

之前 `addon/universal` 整包没移植（依赖流体/存储 API）。这次把**物品相关**的部分补齐了。

### 新增文件（11 个）

| 文件 | 说明 |
|---|---|
| `api/view/ViewGroup` | 视图组的 NBT 序列化（`NbtList` 不支持 foreach，改成下标循环） |
| `api/view/ItemView` | 物品视图（`Container` → `Inventory`） |
| `api/view/ClientViewGroup` | 客户端视图组（`Component` → `Text`，其余逐行对应） |
| `api/view/IServerExtensionProvider` | 服务端扩展 provider 接口 |
| `api/view/IClientExtensionProvider` | 客户端扩展 provider 接口 |
| `addon/universal/ItemIterator` | 容器迭代器（`Container#getContainerSize/getItem` → `Inventory#getSize/getItem`） |
| `addon/universal/ItemCollector` | 物品汇总/缓存（record `ItemDefinition` → 手写 `equals`/`hashCode`，否则 map 去重失效） |
| `addon/universal/ItemStorageProvider` | 方块容器（箱子/桶/熔炉除外…） |
| `addon/universal/EntityItemStorageProvider` | 实体容器（马） |
| `addon/universal/UniversalPlugin` | 只注册物品部分 |
| `mixin/HorseBaseEntityAccess` | 访问马的 `inventory` 字段（1.12 没有 `AbstractHorseAccess`） |

### 1.12 适配要点

| 原版 1.20 | 1.12 |
|---|---|
| `Container` | `Inventory`（`getSize()`/`getItem(int)`） |
| `stack.getTag()/setTag()` | `getNbt()/setNbt()` |
| `stack.getCount()/setCount()` | `getSize()/setSize()` |
| `getAllKeys()` | `getKeys()` |
| `record ItemDefinition` | 普通类 + 手写 `equals`/`hashCode`（`item` 用 `==`，`tag` 用 `NbtCompound.equals`） |
| `RandomizableContainer` + `ContainerEntity` 的 `getLootTable()` | `LootInventoryBlockEntity#getLootTableId()` |
| `LockCode` / `BaseContainerBlockEntity.lockKey` | 1.12 无锁码机制，跳过（`getCombinedMenuProvider(..., false)` 遇到上锁箱子会返回 null，回退到单箱子） |
| `AbstractFurnaceBlockEntity` | `FurnaceBlockEntity` |
| `PlayerEnderChestContainer` | `player.getEnderChestInventory()` |
| `AbstractHorseAccess#getInventory()` | `HorseBaseEntityAccess#jade$getInventory()`（mixin 访问 `inventory` 字段） |
| `ChestBlock.getContainer(...)` | `ChestBlock#getCombinedMenuProvider(world, pos, false)`（返回 `LockableMenuProvider`，本身就是 `Inventory`，双箱子会合并成 `DoubleInventory`） |
| Fabric `ItemStorage.SIDED` | 1.12 无存储 API；`createItemCollector` 只认 `Inventory` |
| `Component.translatable` | `new LiteralText(I18n.translate(...))` |
| `" %s%%".formatted(x)` | `String.format(" %s%%", x)` |
| `List.of` / `toList()` | `Collections.singletonList` / 循环 |

### 注册链路

- `IWailaCommonRegistration#registerItemStorage` + `WailaCommonRegistration.itemStorageProviders`
  （`HierarchyLookup<>(Object.class, true)`，`loadComplete` 里补了 `itemStorageProviders.loadComplete(priorities)`）
- `IWailaClientRegistration#registerItemStorageClient` + `WailaClientRegistration.itemStorageProviders`
  （`Map<Identifier, IClientExtensionProvider<ItemStack, ItemView>>`）
- `CommonProxy#createItemCollector / containerGroup / storageGroup`
- `Identifiers.UNIVERSAL_ITEM_STORAGE` 从注释里放出来
- `fabric.mod.json` 的 `jade` entrypoint 加上 `UniversalPlugin`

### 功能

- 箱子 / 大箱子 / 木桶 / 漏斗 / 发射器 / 投掷器 / 熔炉（跳过）/ 酿造台 / 唱片机 / 潜影盒（1.12 无）… 只要是 `Inventory`
- 玩家末影箱（显示玩家自己的末影箱内容，与原版一致）
- 马/驴/骡的背包（从索引 2 开始，跳过鞍和 armor）
- 战利品箱未生成 → 显示 `jade.loot_not_generated`
- 超过 54 种物品时分批收集，显示 `jade.collectingItems` 进度
- 4 个配置项：`item_storage.detailed_amount` / `normal_amount` / `show_name_amount` / `items_per_line`
  （lang 键本来就齐全 ✓）

### 未移植（仍属于流体/能量/进度）

`FluidStorageProvider`、`EntityFluidStorageProvider`、`EnergyStorageProvider`、
`EntityEnergyStorageProvider`、`ProgressProvider`、`EntityProgressProvider` —— 1.12 没有流体/能量 API，
进度视图也没有对应场景。

## 6.21 universal 物品部分的复查

逐行对照 `ItemStorageProvider` / `ItemCollector` / `ItemIterator` / `api/view/*` 与原版，
并回查 1.12 源码。

### 找到并修复：`Locked` 没实现（1.12 其实有锁）

我第一版以为 1.12 没有锁码机制，直接把 `Locked` 分支跳过了 —— 但 1.12 **有**：

```java
public interface LockableMenuProvider extends Inventory, MenuProvider {
    boolean isLocked();
    ...
}
public class InventoryBlockEntity extends BlockEntity implements LockableMenuProvider { ... }
public class InventoryLock {
    public static final InventoryLock NONE = new InventoryLock("");
    public boolean isEmpty() { ... }
}
```

箱子可以用 `/setblock ... {Lock:"钥匙"}` 上锁，`ChestBlock.isLocked(world, pos)` 也会因此返回 true。
所以补回了原版的两处：

```java
// getGroups：上锁的容器不显示内容（创造/旁观模式除外）
if (player != null && !player.abilities.creativeMode && !player.isSpectator()
        && target instanceof LockableMenuProvider && ((LockableMenuProvider) target).isLocked()) {
    return Collections.emptyList();
}

// putData：拿不到数据时标记 Locked，客户端显示 "Locked"
} else if (player != null && !player.abilities.creativeMode && !player.isSpectator()
        && target instanceof LockableMenuProvider && ((LockableMenuProvider) target).isLocked()) {
    tag.putBoolean("Locked", true);
}
```

（顺带解决了 `putData` 里 `player` 变量声明了却没用的问题。）

### 其余核对结果（无问题）

| 检查点 | 结论 |
|---|---|
| `ItemCollector` 分批收集（每批 `MAX_SIZE*2 = 108`，最多 10000） | ✓ 与原版一致 |
| `mergedResult` 缓存 + `lastTimeFinished + 5` 节流 | ✓ |
| `ItemIterator.version` 全局自增导致「内容未变」分支永不命中 | ✓ 原版就是这样（照搬） |
| `ItemDefinition` 值相等 | ✓ 手写 `equals`/`hashCode`（`item` 用 `==`，`tag` 用 `NbtCompound.equals`） |
| 大箱子合并 | ✓ `ChestBlock#getCombinedMenuProvider(world, pos, false)` 返回的 `LockableMenuProvider` 本身就是 `Inventory`，双箱子时是 `DoubleInventory` |
| 马背包 `fromIndex = 2` | ✓ 跳过鞍/护甲槽；没箱子的马 `getSize() == 2` → 空流 → 不显示 ✓ |
| 末影箱 | ✓ `player.getEnderChestInventory()` |
| NBT 往返 | ✓ `>64` 时先 `setSize(1)` 再写（1.12 `Count` 是 **byte**，不 clamp 会溢出）→ 写 `NewCount` → 还原 |
| 未知物品 id | ✓ 1.12 `ItemStack(NbtCompound)` 里 `Item.byKey` 返回 null → `isEmpty()` → 客户端跳过 |
| `groups == null` 守卫 | ✓ 原版没有（`readList` 可能返回 null，原版会 NPE），移植版加了 |
| `HierarchyLookup<>(Object.class, true)` | ✓ `getInternal` 对 `Object.class` 会走到 `objects.get(Object)` ✓ |
| 配置项 / lang 键 | ✓ 4 个配置 + `config.jade.plugin_minecraft.item_storage*` 全部存在 |
| `renderAmount`（上一节补的） | ✓ `helper.item(stack)` 现在会画数量，`smallItem` 传 `""` 不画 |

### 与原版仍存的差异（不可消除）

| 原版 1.20 | 移植版 | 影响 |
|---|---|---|
| Fabric `ItemStorage.SIDED`（任意方块实体暴露的物品存储） | 只认 `Inventory` | 1.12 的 mod 容器几乎都实现 `IInventory`，实际影响很小 |
| `ContainerEntity`（带战利品表的运输实体） | 无 | 1.12 运输实体没有战利品表概念 |
| `Container#getContainerSize/getItem` | `Inventory#getSize/getItem` | 纯改名 |
| `record ItemDefinition` | 手写类 | 行为等价 |
| `Component.translatable` | `I18n.translate` + `LiteralText` | 文本相同，无颜色差异 |
| `" %s%%".formatted()` | `String.format` | 等价 |

## 6.22 universal 物品部分二次复查

这次做了三件事：逐方法结构对比、逐个 1.12 API 回查、无用代码清理。

### 方法结构对比（6 个类逐一核对）

用脚本把原版和移植版的**方法签名列表**拉出来对比，确认没有漏方法：

| 类 | 结论 |
|---|---|
| `ItemStorageProvider` | 8 个方法一一对应 ✓ |
| `ItemCollector` | 4 个方法 + `ItemDefinition`（record → 类 + 手写 `equals`/`hashCode`）✓ |
| `ItemIterator` | 4 个抽象/工具方法 + `SlottedItemIterator`(2) + `ContainerItemIterator`(3) + `SlotlessItemIterator`(2) ✓ |
| `ViewGroup` | 7 个方法 ✓ |
| `ItemView` / `ClientViewGroup` | ✓ |

### 本轮没有发现新的功能性 Bug

几个重点回查结果：

| 疑点 | 结论 |
|---|---|
| `ItemIterator#getVersion` 全局自增 → 「内容未变」分支永不命中 | **原版就是这样**，照搬；实际靠 `lastTimeFinished + 5` 节流 ✓ |
| `ItemCollector` 只读 27 格箱子时 `progress = 0` | `Collecting` 只在 `group.views.isEmpty()` 时才显示，正常箱子不显示 ✓ |
| `ItemDefinition` 按引用持有 NBT | 原版同样；收集完立即 `items.clear()`，窗口很小 ✓ |
| `NON_EMPTY` 里的 `CustomModelData` 判断 | 1.12 没有这个 NBT，属死代码但无害，保留以示对齐 ✓ |
| 未知/缺失 mod 物品 id | 1.12 `ItemStack(NbtCompound)` → `Item.byKey` 返回 null → `isEmpty()` → 客户端跳过 ✓ |
| `writeNbt` 对空栈 | 1.12 会写 `id = minecraft:air`，不会 NPE ✓ |
| 陷阱箱 | `Blocks.TRAPPED_CHEST` 也是 `ChestBlock`，双箱合并同样生效 ✓ |
| 创造模式看上锁箱子 | `getGroups` 的上锁判断放行 → `getCombinedMenuProvider(..., false)` 返回 null → 回退到单箱子，内容可见 ✓（与原版一致）|
| 0 格容器 / 空容器 | `IntStream.range(0,0)` → 空流 → `finished` → 空视图 → `saveList` 跳过 → 不显示 ✓ |
| 双箱 54 格 | `DoubleInventory.getSize() = 54` ✓ |

### 清理

- `ItemStorageProvider` 里三个没用到的 import（`Block` / `BlockEntity` / `Entity`）已删除
- `CommonProxy` 新增方法里的全限定名（`com.google.common.cache.Cache`、`snownee.jade.api.Accessor`、
  `java.util.List`、`java.util.concurrent.ExecutionException`）改成正常 import，风格与原版一致

### 与原版仍存的差异（均为 1.12 限制，非 bug）

| 原版 1.20 | 移植版 | 说明 |
|---|---|---|
| Fabric `ItemStorage.SIDED`（任意方块实体暴露的物品存储） | 只认 `Inventory` | 1.12 的 mod 容器基本都实现 `IInventory`，实际影响很小 |
| `ContainerEntity`（带战利品表的运输实体） | 无 | 1.12 运输实体没有战利品表 |
| `Container#getContainerSize/getItem` | `Inventory#getSize/getItem` | 纯改名 |
| `record ItemDefinition` | 手写类 | 行为等价（`item` 用 `==`，`tag` 用 `NbtCompound.equals`） |
| `Component.translatable` | `I18n.translate` + `LiteralText` | 文本相同 |
| `" %s%%".formatted()` | `String.format` | 等价 |

## 6.23 严重 Bug：服务端 accessor 没拿到 serverData，物品存储直接 NPE

### 现象

游戏里看向箱子，物品列表**完全不显示**。

### 根因

原版服务端收数据时，`tag` 是**从 accessor 里取的**：

```java
// 原版 BlockAccessorImpl#handleRequest
BlockAccessor accessor = data.unpack(player);   // unpack 时已经带上了 serverData
...
CompoundTag tag = accessor.getServerData();     // ← 就是 provider 要写的那个 tag
for (provider : providers) provider.appendServerData(tag, accessor);
```

移植版是反过来的 —— 先 new 一个 tag，再 build accessor，**没有把 tag 传进去**：

```java
// 修复前
BlockAccessor accessor = new Builder()
    .level(world).player(player).serverConnected(true)   // ← 没有 .serverData(tag)
    ...build();

NbtCompound tag = new NbtCompound();                     // ← 另一个 tag
provider.appendServerData(tag, accessor);
```

于是 `accessor.getServerData()` 返回 **null**，而 `ItemStorageProvider.putData` 第一行就是：

```java
NbtCompound tag = accessor.getServerData();   // null
...
tag.putString("JadeItemStorageUid", ...);     // NPE
```

NPE 被 `WailaExceptionHandler` 吞掉（只打日志），tag 里什么都没写，客户端自然什么也收不到。

### 修复

照原版改成「先建 tag，再把它交给 accessor」（方块和实体两条路径都要改）：

```java
NbtCompound tag = new NbtCompound();
BlockAccessor accessor = new Builder()
    .level(world)
    .player(player)
    //port: providers read accessor.getServerData() on the server, it must be the same tag
    .serverData(tag)
    .serverConnected(true)
    ...
    .build();
```

`EntityAccessorImpl` 同样处理。

### 影响面

全项目只有 `ItemStorageProvider#putData` 在服务端读 `accessor.getServerData()`（已用脚本把所有
`appendServerData` 实现扫了一遍确认），所以这个 bug 只影响物品存储；其余 provider 都用方法参数 `tag`，
不受影响。

### 教训

前两轮 review 只盯着新移植的 addon 代码，没有回头检查**框架侧**的 accessor 构造 ——
而 `putData` 的写法是照抄原版的，问题出在移植框架时漏了 `.serverData(tag)`。

## 6.24 按原版重写 BlockAccessorImpl / EntityAccessorImpl（含 SyncData#unpack）

### 为什么之前那个「修复」是错的

上一节我在 `handleRequest` 里加了 `.serverData(tag)` 并继续用 `new NbtCompound()` 传参 —— 但
`AccessorImpl` 的构造函数是：

```java
this.serverData = serverData == null ? new NbtCompound() : serverData.copy();
```

它会 **copy** 一份，所以 `accessor.getServerData()` 拿到的是副本，`ItemStorageProvider#putData`
写进副本、然后被丢弃 —— 数据照样传不出去。

### 原版的结构

```java
BlockAccessor accessor = data.unpack(player);     // unpack 不传 serverData
...
CompoundTag tag = accessor.getServerData();       // ← accessor 自己那份（构造时 new 出来的）
provider.appendServerData(tag, accessor);         // 写进同一个对象
responseSender.accept(tag);
```

关键是 `unpack` **不**传 serverData，让 `AccessorImpl` 构造时 new 一个，之后 provider 写的和
`accessor.getServerData()` 读的就是**同一个实例**。

### 重写内容

**`BlockAccessorImpl`**
- 新增 `SyncData#unpack(ServerPlayerEntity)`：按 `blockState.hasBlockEntity()` 决定是否挂
  `Suppliers.memoize(() -> player.world.getBlockEntity(hit.getPos()))`，其余照原版 build
- `handleRequest` 改成 `data.unpack(player)` + `accessor.getServerData()`
- `SyncData` 从「pos + hit」改成原版的「showDetails + hit + blockState + fakeBlock」，
  方块状态走 `Block.serialize` / `Block.byId` / `getStateFromMetadata`（1.12 没有 `Block.stateById`）
- `Builder` 补回 `verify` 字段 + `requireVerification()`，`build()` 里 `if (verify) accessor.requireVerification()`
- 服务端数据补上原版的 `tag.putString("id", ...)`

**`EntityAccessorImpl`**
- 新增 `SyncData#unpack(ServerPlayerEntity)`：`CommonProxy.getPartEntity(player.world.getEntity(id), partIndex)`
  包成 memoize，`hit` 也用 memoize 的 `new HitResult(entity.get(), hitVec)`
- `handleRequest` 改成 `data.unpack(player)` + `accessor.getServerData()`
- 删掉原来的 `unpackEntity(player)` 临时方法

### 1.12 适配对照

| 原版 | 移植版 |
|---|---|
| `ServerLevel` / `player.serverLevel()` | `World` / `player.world` |
| `BlockHitResult` | `HitResult`（`getPos()`/`face`/`facePos`） |
| `buf.writeBlockHitResult/readBlockHitResult` | 手写 `writeBlockPos + face + 3×float` |
| `Block.getId(state)` / `Block.stateById(id)` | `Block.serialize(state)` / `Block.byId(id & 0xFFF)` + `getStateFromMetadata(id >> 12)` |
| `pos.distSqr(player.blockPosition())` | 自写 `distanceSq(pos, player)` |
| `world.isLoaded(pos)` | `world.isChunkLoaded(pos)` |
| `tile.getType()` | `tile.getBlock()` + `CommonProxy.getId(Block)` |
| `entity.getId()` | `entity.getNetworkId()` |

### 结果

`ItemStorageProvider#putData` 现在写的就是最终发出去的那个 tag，箱子物品列表可以正常显示了。

## 6.25 全部 Accessor 相关代码复查

对照范围：`api/Accessor`、`api/AccessorImpl`、`api/BlockAccessor`、`api/EntityAccessor`、
`api/AccessorClientHandler`、`impl/BlockAccessorImpl`、`impl/EntityAccessorImpl`、
`impl/BlockAccessorClientHandler`、`impl/EntityAccessorClientHandler`、`impl/ObjectDataCenter`、
`overlay/WailaTickHandler`、`util/CommonProxy`（part entity 部分）、`overlay/RayTracing`。

### 找到并修复：3 个问题

#### 1. `BlockAccessorClientHandler#getIcon` 的兜底图标判断永远成立

```java
// 原版：方块有对应物品才兜底
if (RayTracing.isEmptyElement(icon) && block.asItem() != Items.AIR) {
    icon = ItemStackElement.of(new ItemStack(block));
}

// 修复前：1.12 的 Item.byBlock 永远不会返回 null，它会退化成空气物品
if (RayTracing.isEmptyElement(icon) && Item.byBlock(block) != null) { ... }
```

1.12 的 `Item.byBlock(Block)` 是：

```java
public static Item byBlock(Block block) {
    Item item = BLOCK_ITEMS.get(block);
    return item == null ? byBlock(Blocks.AIR) : item;   // ← 永不 null
}
```

所以对没有物品的方块（水、火、活塞头…）会生成一个空气 `ItemStack`。已改成与空气物品比较：

```java
//port: 1.12 Item.byBlock never returns null, it falls back to the air item
if (RayTracing.isEmptyElement(icon) && Item.byBlock(block) != Item.byBlock(Blocks.AIR)) {
```

#### 2. 缺了液体图标分支

原版在物品兜底之后还有一段：

```java
if (RayTracing.isEmptyElement(icon) && block instanceof LiquidBlock) {
    icon = ClientProxy.elementFromLiquid((LiquidBlock) block);
}
```

移植版整段缺失 —— 水/岩浆方块没有图标。1.12 没有流体渲染 API，所以补了一个桶图标近似：

```java
// impl/BlockAccessorClientHandler
if (RayTracing.isEmptyElement(icon) && blockState.getMaterial().isLiquid()) {
    icon = ClientProxy.elementFromLiquid(block);
}

// util/ClientProxy
public static IElement elementFromLiquid(Block block) {
    if (block.getMaterial(block.defaultState()) == Material.LAVA) {
        return ItemStackElement.of(new ItemStack(Items.LAVA_BUCKET));
    }
    return ItemStackElement.of(new ItemStack(Items.WATER_BUCKET));
}
```

#### 3. `CommonProxy#isMultipartEntity` 语义与原版相反

```java
// 原版
return target instanceof EnderDragon;
// 修复前
return target instanceof EnderDragonPart;
```

`RayTracing#canBeTarget` 里原版是 `if (isMultipartEntity(target) && !target.isPickable()) return false;`。
1.12 没有 `isPickable()`，且**龙的身体部位是独立实体**，所以拆成两段：

```java
if (CommonProxy.isMultipartEntity(target) && !target.isAlive())
    return false;
//port: 1.12 lists dragon parts as separate entities, never target them directly
if (target instanceof EnderDragonPart)
    return false;
```

`isMultipartEntity` 本身恢复成原版的 `instanceof EnderDragon`。

### 核对无问题的部分

| 文件 | 结论 |
|---|---|
| `api/Accessor` | ✓ 只有类型改名（`Level`→`World`、`Player`→`PlayerEntity`、`CompoundTag`→`NbtCompound`、`FriendlyByteBuf`→`PacketBuffer`） |
| `api/AccessorImpl` | ✓ 一致；`serverData == null ? new NbtCompound() : serverData.copy()` 与原版相同（这是物品存储那个 bug 的根源，见 6.24） |
| `api/BlockAccessor` / `api/EntityAccessor` | ✓ 只有类型改名；本轮把 `interface Builder` 补回 `public interface Builder` |
| `api/AccessorClientHandler` | ✓ 完全一致 |
| `impl/BlockAccessorImpl` | ✓ 已按原版重写（见 6.24） |
| `impl/EntityAccessorImpl` | ✓ 已按原版重写（见 6.24） |
| `impl/EntityAccessorClientHandler` | ✓ 等价（`!displayBosses && isBoss` → `displayBosses || !isBoss`） |
| `impl/ObjectDataCenter` | ✓ `getKeys().clear()` 在 1.12 返回的是 `elements.keySet()` 实时视图，行为与原版 `getAllKeys().clear()` 一致 |
| `overlay/WailaTickHandler` 的 accessor 构造 | ✓ `blockState/blockEntity/hit/requireVerification` 全部对应 |
| `util/CommonProxy` 的 `getPartEntityIndex` / `getPartEntity` | ✓ 与原版一致 |

## 6.26 Accessor 代码二次复查（对齐结构）

这轮把 accessor 相关代码逐文件做**方法级结构对比**，把能对齐的都对齐了。

### 方法结构对比（1:1）

| 文件 | 原版方法数 | 移植版 | 结论 |
|---|---|---|---|
| `api/Accessor` | 11 | 11 | ✓ 只有类型改名 |
| `api/AccessorImpl` | 6 | 6 | ✓ |
| `api/BlockAccessor` | 7 + Builder(12) | 同 | ✓ |
| `api/EntityAccessor` | 3 + Builder(11) | 同 | ✓ |
| `api/AccessorClientHandler` | 5 | 5 | ✓ 完全一致 |
| `impl/BlockAccessorImpl` | 14 + Builder(12) + SyncData(5) | 同 | ✓ |
| `impl/EntityAccessorImpl` | 7 + Builder(10) + SyncData(5) | 同 | ✓ |
| `impl/BlockAccessorClientHandler` | 5 | 5 | ✓ |
| `impl/EntityAccessorClientHandler` | 5 | 5 | ✓ |
| `impl/ObjectDataCenter` | 9 | 9 | ✓ |

### 本轮对齐的 3 处

#### 1. 删掉多余的 `getAccessorType()` 覆写

`BlockAccessor` / `EntityAccessor` 接口里已经有 `default getAccessorType()`，原版的
`BlockAccessorImpl` / `EntityAccessorImpl` **不覆写**它。移植版多写了两个 `@Override`，已删除。

#### 2. 距离判断改用 1.12 自带方法，去掉自定义 helper

```java
// 原版
if (pos.distSqr(player.blockPosition()) > Jade.MAX_DISTANCE_SQR || !world.isLoaded(pos)) return;

// 上一版（自己写的 helper）
if (distanceSq(pos, player) > Jade.MAX_DISTANCE_SQR || !world.isChunkLoaded(pos)) return;
private static double distanceSq(...) { ... }

// 现在
//port: 1.20 uses pos.distSqr(player.blockPosition()), 1.12 only offers this helper
if (player.getSquaredDistanceToCenter(pos) > Jade.MAX_DISTANCE_SQR || !world.isChunkLoaded(pos)) return;
```

`Entity#getSquaredDistanceToCenter(BlockPos)` 是 1.12 自带的，语义与原版的 `distSqr` 基本一致，
自定义的 `distanceSq` 已删除。

#### 3. `EntityAccessorClientHandler#shouldDisplay` 恢复原版的 if 结构

```java
// 原版
if (!general.getDisplayBosses() && CommonProxy.isBoss(accessor.getEntity())) {
    return false;
}
return true;

// 上一版（等价但结构不同）
return general.getDisplayBosses() || !CommonProxy.isBoss(accessor.getEntity());
```

### 逐项 bug 复核（本轮无新 bug）

| 检查点 | 结论 |
|---|---|
| `SyncData` 写入/读取字段顺序 | 方块：`showDetails, pos, face, 3×float, stateId, item` ✓ 对称；实体：`showDetails, id, partIndex, 3×float` ✓ 对称 |
| `Block.serialize` / `Block.byId` / `getStateFromMetadata` 往返 | ✓ `stateId == 0` 视为空气（只有空气的 id 是 0） |
| `unpack` 不传 serverData | ✓ 让 `AccessorImpl` 构造时 new 一个，provider 写的和 `getServerData()` 读的是同一实例（6.24 的修复） |
| `unpack` 里 `hasBlockEntity()` 判断 | ✓ 无 BE 的方块 `blockEntity = null`，`handleRequest` 会提前返回 |
| 实体 `unpack` 的 memoize | ✓ `getPartEntity(parent, partIndex)`，`hit` 也用 memoize |
| `verifyData` | 方块比 x/y/z ✓、实体比 `WailaEntityID` ✓（移植版多加了 null / contains 守卫，行为更稳） |
| `getPickedResult` | ✓ 方块走 `getBlockPickedResult`，实体走 `getEntityPickedResult` |
| `getTarget` | 方块返回 `getBlockEntity()` ✓、实体返回 `getEntity()`（已 wrap 到父实体）✓ |
| `ObjectDataCenter` | ✓ `getKeys().clear()` 在 1.12 返回实时 keySet，等同原版 `getAllKeys().clear()` |
| `WailaTickHandler` 构造 accessor | ✓ `blockState / blockEntity / hit / requireVerification` 一一对应 |

## 6.27 Accessor 相关代码第三次细查

这次逐个方法、逐行对照，并把**周边链路**（请求包、响应包、图标、数据缓存）也一起查了。

### 找到并修复：`getEntityPickedResult` 恒返回空

```java
// 修复前
public static ItemStack getEntityPickedResult(Entity entity, PlayerEntity player, HitResult hitResult) {
    return ItemStack.EMPTY;   // ← 什么都没做
}
```

原版是：

```java
public static ItemStack getEntityPickedResult(Entity entity, Player player, EntityHitResult hitResult) {
    if (entity instanceof EntityPickInteractionAware aware) {
        return aware.getPickedStack(player, hitResult);
    }
    ItemStack stack = entity.getPickResult();
    return stack == null ? ItemStack.EMPTY : stack;
}
```

1.12 没有 `Entity#getPickResult`，但**刷怪蛋是用 NBT 指定实体的**（`SpawnEggItem#readTypeFromNbt`
读 `EntityTag.id`），所以按这个方式补上：

```java
//port: 1.12 has no Entity#getPickResult, spawn eggs carry the entity id in their nbt
if (entity instanceof LivingEntity) {
    Identifier id = Entities.getKey(entity);
    if (id != null) {
        ItemStack stack = new ItemStack(Items.SPAWN_EGG);
        NbtCompound entityTag = new NbtCompound();
        entityTag.putString("id", id.toString());
        NbtCompound tag = new NbtCompound();
        tag.put("EntityTag", entityTag);
        stack.setNbt(tag);
        return stack;
    }
}
return ItemStack.EMPTY;
```

**注意**：工具提示的图标**看起来没变化** —— 因为 `EntityAccessorClientHandler` 本来就会把「刷怪蛋 + 活体实体」
的情况过滤掉（`icon` 保持 null，交给 provider）。改的是 API 行为，让 `accessor.getPickedResult()`
与原版一致。

### 逐文件逐方法核对结果

| 文件 | 核对内容 | 结论 |
|---|---|---|
| `api/Accessor` | 11 个方法签名 | ✓ 完全一致（只有类型名） |
| `api/AccessorImpl` | 构造器 `serverData == null ? new : copy()`、6 个方法 | ✓ 逐行一致 |
| `api/BlockAccessor` | 7 个方法 + Builder 12 个 | ✓ 逐行一致 |
| `api/EntityAccessor` | `getRawEntity()` 默认实现 + Builder 11 个 | ✓ 逐行一致 |
| `api/AccessorClientHandler` | 5 个方法 | ✓ 完全一致 |
| `impl/BlockAccessorImpl` | `handleRequest` / `SyncData` / `Builder` / 14 个方法 | ✓ 1:1 |
| `impl/EntityAccessorImpl` | `handleRequest` / `SyncData` / `Builder` / 7 个方法 | ✓ 1:1 |
| `impl/BlockAccessorClientHandler` | `shouldDisplay` / `shouldRequestData` / `requestData` / `getIcon` / `gatherComponents` | ✓ |
| `impl/EntityAccessorClientHandler` | 同上 5 个 | ✓ 完全一致（含刷怪蛋过滤） |
| `impl/ObjectDataCenter` | `set` / `get` / `getServerData` / `setServerData` / 计时器 / `getIcon` | ✓ 逐行一致 |
| `overlay/WailaTickHandler` | accessor 构造 + `shouldRequestData` + 限流请求 | ✓ 一致 |
| `overlay/RayTracing#getIcon` | `ObjectDataCenter.get()` → `getIcon()` → `isEmptyElement` | ✓ 完全一致 |
| `util/CommonProxy` | `getPartEntityIndex` / `getPartEntity` / `wrapPartEntityParent` / `isMultipartEntity` / `getBlockPickedResult` / `getEntityPickedResult` | ✓（最后一项本轮修好） |
| `util/ClientProxy` | `requestBlockData` / `requestEntityData` | ✓ 一致 |

### 唯一有意保留的差异

`BlockAccessorImpl#verifyData` 比原版多了两个守卫：

```java
if (getBlockEntity() == null || !data.contains("x")) {
    return false;
}
```

原版直接读 `data.getInt("x")`，方块实体在请求与响应之间被移除时会拿到 0 并可能误判通过。
移植版选择返回 false（丢弃过期数据），行为更稳；除此之外与原版逐行相同。

## 6.28 物品存储显示错误（金合欢显示成橡木）：metadata 丢失

### 现象

箱子里放金合欢原木/木板，Jade 显示成橡木。

### 根因：`ItemDefinition` 只存了 item + NBT，丢了 metadata

```java
// 修复前
ItemDefinition(ItemStack stack) {
    this(stack.getItem(), stack.getNbt());   // ← metadata 没存
}
public ItemStack toStack(int count) {
    ItemStack stack = new ItemStack(item);   // ← metadata = 0（橡木）
    ...
}
```

1.20 里每种木头是**独立物品**（`oak_log` / `acacia_log` …），item 本身就区分开了；
**1.12 里是同一个 `minecraft:log`，靠 metadata 区分 0=橡木 1=云杉 2=白桦 3=丛林 4=金合欢 5=深色橡木**。
所以聚合时所有木头都塌缩成 meta 0 的橡木。

### 修复

```java
public static class ItemDefinition {
    public final Item item;
    //port: 1.12 shares one Item between all wood types, the metadata is what tells acacia from oak
    public final int metadata;
    @Nullable
    public final NbtCompound tag;

    ItemDefinition(ItemStack stack) {
        this(stack.getItem(), stack.getMetadata(), stack.getNbt());
    }

    public ItemStack toStack(int count) {
        ItemStack stack = new ItemStack(item, 1, metadata);
        stack.setSize(count);
        stack.setNbt(tag);
        return stack;
    }

    // equals / hashCode 也把 metadata 算进去，否则不同木头会被当成同一条
}
```

### 顺带查出同类的另外 2 处 metadata 丢失

把全项目 15 处 `new ItemStack(...)` 逐个过了一遍，又找到两处：

#### 1. `BlockAccessorClientHandler#getIcon` 的兜底图标

```java
// 修复前：木板 meta 4（金合欢）会画成 meta 0（橡木）
icon = ItemStackElement.of(new ItemStack(block));
// 修复后
icon = ItemStackElement.of(new ItemStack(Item.byBlock(block), 1, block.getMetadataFromState(blockState)));
```

#### 2. `FallingBlockProvider` 的图标

```java
// 修复前：掉落的金合欢原木显示成橡木
ItemStack stack = new ItemStack(entity.getBlock().getBlock());
// 修复后
BlockState state = entity.getBlock();
ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetadataFromState(state));
```

### 其余链路确认无 metadata 丢失

| 位置 | 结论 |
|---|---|
| `ItemStorageProvider#putData` | ✓ `item.writeNbt(itemTag)` 会写 `Damage` |
| `ItemStorageProvider#append` | ✓ `new ItemStack(itemTag)` 从 NBT 还原（含 Damage） |
| `ItemCollector` 聚合/`toStack` | ✓ 本轮修好 |
| `ItemIterator.ContainerItemIterator` | ✓ 直接返回容器里的原始 stack |
| `FurnaceProvider` / `JukeboxProvider` | ✓ 走 NBT |
| `CommonProxy#getEntityPickedResult` | ✓ 刷怪蛋用 NBT 指定实体 |
| `ClientProxy#elementFromLiquid` | ✓ 桶没有 metadata |
| `CropProgressProvider` / `BrewingStandProvider` / harvest 工具 | ✓ 都是无 metadata 的物品 |
| `PlayerHeadProvider` | ✓ 不构造 ItemStack |

## 6.29 addon 全包复查（第四轮）

按 diff 行数排序逐个核对，重点看之前没细看的 `core/ObjectNameProvider`（100 行差异）、`debug/*`、
`harvest/ShearsToolHandler`，以及 `VanillaPlugin` 的注册表完整性。

### 修复 1：`ObjectNameProvider` 缺「大箱子」名字

原版服务端是这样取名字的：

```java
if (blockEntity instanceof ChestBlockEntity && accessor.getBlock() instanceof ChestBlock chestBlock) {
    MenuProvider menuProvider = accessor.getBlockState().getMenuProvider(accessor.getLevel(), accessor.getPosition());
    if (menuProvider != null) {
        name = menuProvider.getDisplayName();
    }
} else if (nameable.hasCustomName()) {
    name = nameable.getDisplayName();
}
```

这段的作用是让**双箱子显示 "Large Chest"**。移植版只留了 `hasCustomName()` 分支，
所以双箱子会退化成方块名 "Chest"。1.12 的等价写法：

```java
//port: 1.12 has no MenuProvider on the block state, the combined chest provider carries "Large Chest"
if (blockEntity instanceof ChestBlockEntity && accessor.getBlock() instanceof ChestBlock) {
    LockableMenuProvider provider = ((ChestBlock) accessor.getBlock()).getCombinedMenuProvider(accessor.getLevel(), accessor.getPosition(), false);
    if (provider != null) {
        name = provider.getDisplayName();
    }
} else if (nameable.hasCustomName()) {
    name = nameable.getDisplayName();
}
```

（`getCombinedMenuProvider` 单箱子返回箱子本身、双箱子返回 `DoubleInventory`，名字分别是
"Chest" / "Large Chest"。）

### 修复 2：`ObjectNameProvider#getEntityName` 缺 `shouldPick` 分支

原版在没有自定义名字时，先看 `WailaClientRegistration#shouldPick(entity)`，命中就用
`entity.getPickResult().getHoverName()`。移植版整段缺失，已补上（1.12 用 `getEntityPickedResult`）：

```java
if (WailaClientRegistration.instance().shouldPick(entity)) {
    ItemStack stack = CommonProxy.getEntityPickedResult(entity, Minecraft.getInstance().player, null);
    if (stack != null && !stack.isEmpty()) {
        return new LiteralText(stack.getHoverName());
    }
}
```

### 修复 3：16 处「把带颜色的文本拍平成字符串」会丢掉 RGB 颜色

这是**上一轮加 RGB 支持后才暴露出来**的问题。这些 provider 原来是：

```java
tooltip.add(new LiteralText(I18n.translate("tooltip.jade.power", t.info(power).getFormattedString())));
```

`getFormattedString()` 会退化成 `§` 颜色码（16 色），内层 `RgbText` 的精确颜色就没了。原版是
`Component.translatable(key, component)`，所以统一改成 `TranslatableText`：

```java
tooltip.add(new TranslatableText("tooltip.jade.power", t.info(power)));
```

`drawText` 现在会遍历 `TranslatableText` 的参数节点，`RgbText` 参数能保留精确颜色。

改到的文件（16 处）：

| 文件 | 处数 |
|---|---|
| `RedstoneProvider` | 5 |
| `CropProgressProvider` | 2 |
| `HorseStatsProvider` | 2 |
| `ChickenEggProvider` / `EnchantmentPowerProvider` / `JukeboxProvider` / `MobBreedingProvider` / `MobGrowthProvider` / `MobSpawnerProvider` / `StatusEffectsProvider` | 各 1 |

### 核对无问题的部分

| 检查 | 结论 |
|---|---|
| `VanillaPlugin` 注册表 | 83 条 vs 原版 107 条，差的正好是 8 个 1.14+ 内容（蜂巢/营火/讲台/雕纹书架/蜡封/展示实体×2/刷怪笼冷却）✓ 无多余注册 |
| `debug/BlockStatesProvider` | ✓ 仅多了一个泛型 `getValue` 辅助（1.12 的 `Property#getValue` 需要 unchecked 转换） |
| `debug/RegistryNameProvider` | ✓ 方块/方块实体 id 对应；流体那行是 1.13+ 才有，已注释说明 |
| `debug/DebugPlugin` | ✓ |
| `harvest/ShearsToolHandler` | ✓ 1.12 没有 block tag，羊毛/树叶/双高草由 `CommonProxy#isShearable` 覆盖 |
| `EnchantmentPowerProvider` | ✓（本轮顺手改成 TranslatableText） |
| 其余 vanilla provider | 见 6.17 / 6.19 / 6.28 各轮 ✓ |

## 6.30 addon 第五轮：`%d` 格式键导致 "Format error"

### 现象

看向红石粉/中继器，提示里出现：

```
Format error: Power: 1
```

### 根因链

**1. Jade 的语言文件里三个键用的是 `%d`**

- `tooltip.jade.delay` = `Delay: %d`
- `tooltip.jade.power` = `Power: %d`
- `jade.fraction` = `%d / %d`

（`de_de`/`it_it`/`ja_jp`/`ko_kr`/`pl_pl`/`pt_br`/`ru_ru`/`uk_ua`/`zh_cn`/`zh_tw` 里也有；
`zh_tw` 的 `jade.nextEgg`、`jade.mobgrowth.time` 同样是 `%d`。）

**2. 1.12 的 `Locale#translate` 即使没有参数也会 `String.format`**

```java
public String translate(String key, Object[] args) {
    String string = this.getTranslationOrKey(key);
    try {
        return String.format(string, args);
    } catch (IllegalFormatException e) {
        return "Format error: " + string;
    }
}
```

所以 `I18n.translate("tooltip.jade.power")` 直接返回 `"Format error: Power: %d"`。
我上一版想用 `I18n.translate(key).replace("%d","%s")` 取原始串，拿到的就是这个错误串，
再格式化就成了 `Format error: Power: 1` —— 方法确实是狗屎。

**3. 1.12 的 `TranslatableText` 只支持 `%s`**

```java
if (!"s".equals(string)) {
    throw new TranslationException(this, "Unsupported format: '" + string2 + "'");
}
```

所以 `new TranslatableText(key, value)` 也会抛异常。

### 正确修复：把语言文件里的 `%d` 统一改成 `%s`

1.12 没有「只取原文不格式化」的 API，`%d` 在这个版本里两条路都走不通；而 Jade 的语言文件是我们
自己的资源，所以直接规范化占位符：

```
11 个语言文件共 57 处 %d → %s
tooltip.jade.delay  = "Delay: %s"
tooltip.jade.power  = "Power: %s"
jade.fraction       = "%s / %s"
jade.input_signal / mymod.fuel = "%s"
zh_tw 的 jade.nextEgg / jade.mobgrowth.time = "%s"
```

代码回到最干净的写法（等同原版的 `Component.translatable(key, value)` 结构）：

```java
// RedstoneProvider
tooltip.add(new TranslatableText("tooltip.jade.delay", t.info(delay)));
tooltip.add(new TranslatableText("tooltip.jade.power", t.info(state.get(power))));

// HorseStatsProvider
return new TranslatableText(key, new TranslatableText("jade.fraction", valueText, DisplayHelper.dfCommas.format(max)));
```

`%s` + `Text` 参数 → `drawText` 会遍历参数节点 → 数值仍然是主题的**精确颜色** ✓。

### 全量审计

- 11 个语言文件重新解析全部通过 ✓
- 全项目「带参数的翻译键」逐个检查，占位符已 100% 为 `%s`/`%%` ✓

## 6.31 addon 第六轮：`jade.error` 也是 Format error

### 用脚本做全量审计

这轮不再靠肉眼，写了三个静态检查：

1. **无参数翻译但键里有占位符** → 会触发 `Format error`（1.12 的 `Locale#translate` 即使没有参数也
   会 `String.format`）
2. **占位符数量 ≠ 实参数量** → `MissingFormatArgumentException` / 参数被吞
3. **非 `%s` 占位符**（`%d`/`%f` 等）→ `TranslatableText` 抛 `TranslationException`

### 查出：`WailaExceptionHandler` 的错误行永远显示 Format error

```java
// 修复前
Text text = new LiteralText(String.format(I18n.translate("jade.error"), ModIdentification.getModName(modid)));
```

`jade.error` = `<Error occurred, plz report to %s>`，而 `I18n.translate("jade.error")` **没有传参**，
`String.format` 直接抛 `MissingFormatArgumentException` 被 `Locale` 吞掉，返回
`"Format error: <Error occurred, plz report to %s>"`，再 `String.format` 一次也没用了。

所以任何 provider 抛异常时，玩家看到的都是 `Format error: ...`，而不是「请联系某某」。

**修复**（和原版一致）：

```java
Text text = new TranslatableText("jade.error", ModIdentification.getModName(modid));
text.setStyle(new Style().setColor(Formatting.DARK_RED));
tooltip.add(text);
```

### 审计结果（修复后）

```
无参数翻译 + 占位符 : 0
占位符/实参数量不匹配 : 0
非 %s 占位符        : 0
```

### 其它核对（无问题）

| 检查 | 结论 |
|---|---|
| 各 provider 的注册类 vs 强转类 | 全部一致（`BrewingStandProvider`→`BrewingStandBlockEntity`、`FurnaceProvider`→`FurnaceBlockEntity`、`HorseStatsProvider`→`HorseBaseEntity`…）|
| `state.get(...)` 守卫 | 红石（`instanceof LeverBlock` / 方块判断）、作物（`instanceof`）、唱片机（注册在 `JukeboxBlock`）、TNT（注册在 `TntBlock`）✓ |
| `new Identifier(...)` | `CorePlugin`、`ItemStorageProvider` 都在 try/catch 里 ✓ |
| NBT 写入/读取键 | 逐文件比对，`BrewingStand`/`Time`/`Fuel`、`JadeItemStorage`/`NewCount` 等全部对得上 ✓ |
| `MobSpawnerProvider` 的展示实体 | 1.12 的 `MobSpawner#getDisplayEntity()` 内部有缓存，不会每帧新建实体 ✓ |
| `AnimalOwnerProvider` | ✓ 单机时跳过同步，与原版一致 |
| `BrewingStandProvider` | ✓ `getData(0)/getData(1)` 对应 `brewTime`/`fuel` |
| `VillagerProfessionProvider` | ✓ 1.12 用 `getProfession()` + 职业名数组 |
| `ZombieVillagerProvider` | ✓ 通过 mixin 读私有 `conversionTicks` |
| `TotalEnchantmentPowerProvider` | ✓ 复刻 1.12 `EnchantingTableMenu` 的书架几何 |

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
