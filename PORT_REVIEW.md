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
