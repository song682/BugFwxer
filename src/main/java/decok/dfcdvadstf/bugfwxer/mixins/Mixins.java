package decok.dfcdvadstf.bugfwxer.mixins;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.IBaseTransformer.Phase;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;
import decok.dfcdvadstf.bugfwxer.BugFwxerConfig;

/**
 * <p>
 * Declarative registry of every mixin shipped by this mod, in GTNH style.<br>
 * 本模组全部 Mixin 的声明式注册表，采用 GTNH 风格。
 * </p>
 * <p>
 * Each enum constant carries its own {@link MixinBuilder}: the phase
 * (EARLY for vanilla/Forge classes patched before mod loading, LATE for mod
 * classes, {@code null} for the default main config), the target side
 * (COMMON / CLIENT / SERVER) and an {@code applyIf} condition wired to the
 * {@link BugFwxerConfig} toggle of the fix.<br>
 * 每个枚举常量携带自己的 {@link MixinBuilder}：阶段（EARLY 用于在模组加载前
 * 修补的 vanilla/Forge 类，LATE 用于模组类，普通主配置为 {@code null}）、
 * 目标侧（COMMON / CLIENT / SERVER）以及关联 {@link BugFwxerConfig}
 * 修复开关的 {@code applyIf} 条件。
 * </p>
 * <p>
 * The lists are consumed by {@link EarlyMixinPlugin} (EARLY), the main
 * {@link MixinPlugin} (default phase) and {@link LateMixinPlugin} (LATE),
 * which dynamically replace the mixin arrays of their JSON configs.<br>
 * 这些列表由 {@link EarlyMixinPlugin}（EARLY）、主 {@link MixinPlugin}
 * （默认阶段）与 {@link LateMixinPlugin}（LATE）消费，
 * 动态替换各自 JSON 配置中的 mixin 数组。
 * </p>
 *
 * @author Seniye
 */
public enum Mixins implements IMixins {

    // ------------------------------------------------------------------
    // EARLY phase — targets vanilla/Forge classes, registered by the
    // coremod EarlyMixinPlugin before any Minecraft class is loaded.
    // EARLY 阶段——目标为 vanilla/Forge 类，由 coremod EarlyMixinPlugin
    // 在任何 Minecraft 类加载之前注册。
    // ------------------------------------------------------------------

    /**
     * Block only the Alt+F4 window close request (Minecraft#runGameLoop).
     * 仅阻拦 Alt+F4 触发的窗口关闭请求（Minecraft#runGameLoop）。
     */
    BLOCK_ALT_F4_WINDOW_CLOSE(new MixinBuilder("Block Alt+F4 window close request")
            .setPhase(Phase.EARLY)
            .addClientMixins("minecraft.client.MixinMinecraft")
            .setApplyIf(() -> BugFwxerConfig.blockAltF4WindowClose)),

    /**
     * Keep background music playing across GUI switches: the pause menu no
     * longer pauses/resumes the MusicTicker track (SoundManager
     * pauseAllSounds/resumeAllSounds + MusicTicker#update fallback).
     * GUI 切换期间保持背景音乐持续播放：暂停菜单不再暂停/恢复 MusicTicker 曲目
     * （SoundManager pauseAllSounds/resumeAllSounds + MusicTicker#update 兜底）。
     */
    KEEP_MUSIC_DURING_GUI_SWITCH(new MixinBuilder("Keep music playing across GUI switches")
            .setPhase(Phase.EARLY)
            .addClientMixins("minecraft.audio.MixinSoundManager", "minecraft.audio.MixinMusicTicker")
            .setApplyIf(() -> BugFwxerConfig.keepMusicDuringGuiSwitch)),

    /**
     * Fire Forge's village InitMapGenEvent inside ChunkProviderFlat so mods
     * that replace the village generator (e.g. VillageNames) take over in
     * superflat worlds, where the event is otherwise never posted.
     * 在 ChunkProviderFlat 中触发村庄 InitMapGenEvent，使替换村庄生成器的模组
     * （如 VillageNames）能在超平坦世界中接管——否则该事件永远不会被发布。
     */
    FIX_VILLAGENAMES_FLAT_WORLD_VILLAGES(new MixinBuilder("Fire InitMapGenEvent for superflat village generation")
            .setPhase(Phase.EARLY)
            .addCommonMixins("minecraft.world.gen.MixinChunkProviderFlat")
            .setApplyIf(() -> BugFwxerConfig.fixVillageNamesFlatWorldVillages)),

    /**
     * Filter virtual/recording audio devices and fix device-name encoding at
     * the JavaSound API level (AudioSystem#getMixerInfo), so the audio output
     * device switcher sees a clean, readable device list.
     * 在 JavaSound API 层面（AudioSystem#getMixerInfo）过滤虚拟/录音设备并
     * 修复设备名编码，让音频输出设备切换器拿到干净可读的设备列表。
     */
    AUDIO_DEVICE_LIST_CLEANUP(new MixinBuilder("Clean up the JavaSound device list")
            .setPhase(Phase.EARLY)
            .addClientMixins("java.MixinAudioSystem")
            .setApplyIf(() -> BugFwxerConfig.audioOutputDeviceSwitch)),

    // ------------------------------------------------------------------
    // Default phase — registered by the main mixins.bugfwxer.json plugin
    // during the tweak/launch stage; enough for classes loaded later in
    // the game startup. 默认阶段——由主配置 mixins.bugfwxer.json 的插件在
    // tweak/启动阶段注册；对启动后期才加载的类已经足够。
    // ------------------------------------------------------------------

    /**
     * Fix dark oak saplings consuming bonemeal without a valid 2x2 cluster.
     * 修复黑橡木树苗在没有有效 2x2 阵型时白白消耗骨粉的问题。
     */
    FIX_DARK_OAK_SAPLING_BONEMEAL(new MixinBuilder("Fix dark oak sapling bonemeal consumption")
            .addCommonMixins("minecraft.block.MixinBlockSapling")
            .setApplyIf(() -> BugFwxerConfig.fixDarkOakSaplingBonemeal)),

    /**
     * Fix bonemeal being consumed on plants at the world height limit
     * (tall grass IGrowable check + ItemDye bonemeal interception).
     * 修复世界高度上限处植物仍消耗骨粉的问题（草方块 IGrowable 判定 + ItemDye 骨粉拦截）。
     */
    FIX_BONEMEAL_HEIGHT_LIMIT(new MixinBuilder("Fix bonemeal consumption at the world height limit")
            .addCommonMixins("minecraft.block.MixinBlockTallGrass", "minecraft.item.MixinItemDye")
            .setApplyIf(() -> BugFwxerConfig.fixBonemealHeightLimit)),

    /**
     * Fix MC-4: item entity position desync between client and server.
     * 修复 MC-4：物品实体在客户端与服务端之间的位置不同步问题。
     */
    FIX_ITEM_POSITION_DESYNC(new MixinBuilder("Fix MC-4 item entity position desync")
            .addCommonMixins("minecraft.entity.MixinEntityItem")
            .setApplyIf(() -> BugFwxerConfig.fixItemPositionDesync)),

    /**
     * Fix NPE crash when hovering an ItemStack whose item is null
     * (creative inventory fast tab switching race).
     * 修复创造模式物品栏快速切换标签页时 ItemStack.hasEffect 的空指针崩溃。
     */
    FIX_ITEMSTACK_HAS_EFFECT_NPE(new MixinBuilder("Fix ItemStack.hasEffect NPE")
            .addCommonMixins("minecraft.item.MixinItemStack")
            .setApplyIf(() -> BugFwxerConfig.fixItemStackHasEffectNpe)),

    /**
     * Block monster spawn eggs in peaceful mode and show a tooltip warning.
     * 和平模式下阻止怪物蛋使用，并在 Tooltip 中显示警告。
     */
    PEACEFUL_MONSTER_EGG_RESTRICTION(new MixinBuilder("Restrict monster spawn eggs in peaceful mode")
            .addCommonMixins("minecraft.item.MixinItemMonsterPlacer")
            .setApplyIf(() -> BugFwxerConfig.peacefulMonsterEggRestriction)),

    /**
     * Fix bat wing animation float overflow when ageInTicks grows very large.
     * 修复 ageInTicks 过大时蝙蝠翅膀动画的浮点溢出问题。
     */
    FIX_BAT_WING_ANIMATION_OVERFLOW(new MixinBuilder("Fix bat wing animation float overflow")
            .addClientMixins("minecraft.entity.MixinEntityBat")
            .setApplyIf(() -> BugFwxerConfig.fixBatWingAnimationOverflow)),

    /**
     * Clamp GuiSlot scroll amount to non-negative for short lists.
     * 修复短列表时 GuiSlot 滚动量为负数的问题。
     */
    FIX_GUI_SLOT_NEGATIVE_SCROLL(new MixinBuilder("Fix GuiSlot negative scroll amount")
            .addClientMixins("minecraft.client.gui.MixinGuiSlot")
            .setApplyIf(() -> BugFwxerConfig.fixGuiSlotNegativeScroll)),

    /**
     * Fix MC-179165: releasing the mouse over a tab while dragging the
     * creative inventory scrollbar no longer switches to that tab.
     * 修复 MC-179165：拖动创造模式物品栏滚动条时，在标签上松开鼠标
     * 不再误切换到该标签。
     */
    FIX_CREATIVE_SCROLLBAR_TAB_CLICK(new MixinBuilder("Fix creative scrollbar tab click")
            .addClientMixins("minecraft.client.gui.MixinGuiContainerCreative")
            .setApplyIf(() -> BugFwxerConfig.fixCreativeScrollbarTabClick)),

    /**
     * Fix FontRenderer logic errors: unicode width matches the rendered
     * advance, overflowing first characters no longer crash word-wrapping,
     * trailing format codes cost no width, and the obfuscated style can no
     * longer hang the game.
     * 修复 FontRenderer 逻辑错误：unicode 宽度与实际渲染推进一致、超宽首字符
     * 不再导致换行崩溃、末尾格式码不再占宽度、乱码样式不再卡死游戏。
     */
    FIX_FONT_RENDERER_LOGIC(new MixinBuilder("Fix FontRenderer logic")
            .addClientMixins("minecraft.client.gui.MixinFontRenderer")
            .setApplyIf(() -> BugFwxerConfig.fixFontRendererLogic)),

    /**
     * Add an audio output device switcher button to the sound settings GUI:
     * SoundManager prefers paulscode LibraryJavaSound (so switching the mixer
     * actually reroutes every channel), and GuiScreenOptionsSounds gets a
     * button that cycles through the detected output devices.
     * 在声音设置界面添加音频输出设备切换按钮：SoundManager 优先使用 paulscode
     * LibraryJavaSound（这样切换混音器才能真正改道所有声道），
     * GuiScreenOptionsSounds 上新增一个循环切换检测到的输出设备的按钮。
     */
    AUDIO_OUTPUT_DEVICE_SWITCHER(new MixinBuilder("Audio output device switcher")
            .addClientMixins("minecraft.audio.MixinSoundManagerJavaSound",
                    "minecraft.client.gui.GuiScreenOptionsSoundsMixin")
            .setApplyIf(() -> BugFwxerConfig.audioOutputDeviceSwitch)),

    /**
     * Fix paulscode's OGG decoder (CodecJOrbis#readAll) reallocating and
     * copying the whole accumulated buffer for every ~16 KB decoded chunk
     * while holding the global sound lock, which stalled the sound system
     * when a large sound was first played.
     * 修复 paulscode 的 OGG 解码器（CodecJOrbis#readAll）每解码一个约 16KB
     * 块就整体重新分配复制累计缓冲、且全程持有声音全局锁的问题，
     * 该问题会在首次播放大型音效时使整个声音系统卡顿。
     */
    FIX_OGG_DECODE_QUADRATIC_COPY(new MixinBuilder("Fix paulscode OGG decode quadratic copy")
            .addClientMixins("java.MixinCodecJOrbis")
            .setApplyIf(() -> BugFwxerConfig.fixOggDecodeQuadraticCopy));

    /** The builder describing this mixin entry. 描述该 Mixin 条目的构建器。 */
    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Override
    public MixinBuilder getBuilder() {
        return this.builder;
    }
}
