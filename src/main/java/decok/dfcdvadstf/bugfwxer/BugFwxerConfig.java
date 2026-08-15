package decok.dfcdvadstf.bugfwxer;

import decok.dfcdvadstf.bugfwxer.mixins.MixinPlugin;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * <p>
 * Central switchboard for every bugfix shipped by this mod.<br>
 * 本模组所有修复项的总开关配置。
 * </p>
 * <p>
 * Loaded by {@link MixinPlugin} before any
 * mixin is applied, so each flag decides whether the corresponding mixin is
 * injected at all — changes therefore require a game restart.<br>
 * 由 {@link MixinPlugin} 在任何 Mixin
 * 应用之前加载，每个开关决定对应 Mixin 是否被注入，因此修改后需要重启游戏生效。
 * </p>
 *
 * @author Seniye
 */
public class BugFwxerConfig {

        /** Config category holding all fix toggles. 存放所有修复开关的配置分类。 */
        public static final String CATEGORY_FIXES = "fixes";

        /**
         * Fix dark oak sapling consuming bonemeal without a valid 2x2 cluster.
         * 修复黑橡木树苗在没有 2x2 阵型时仍消耗骨粉的问题。
         */
        public static boolean fixDarkOakSaplingBonemeal = true;

        /**
         * Fix bonemeal being consumed on plants at the world height limit
         * (tall grass IGrowable check + ItemDye bonemeal interception).
         * 修复世界高度上限处植物仍消耗骨粉的问题（草方块 IGrowable 判定 + ItemDye 骨粉拦截）。
         */
        public static boolean fixBonemealHeightLimit = true;

        /**
         * Fix MC-4: item entity position desync between client and server.
         * 修复 MC-4：物品实体在客户端与服务端之间的位置不同步问题。
         */
        public static boolean fixItemPositionDesync = true;

        /**
         * Fix NPE crash when hovering an ItemStack whose item is null
         * (creative inventory fast tab switching race).
         * 修复创造模式物品栏快速切换标签页时 ItemStack.hasEffect 的空指针崩溃。
         */
        public static boolean fixItemStackHasEffectNpe = true;

        /**
         * Block monster spawn eggs in peaceful mode and show a tooltip warning.
         * 和平模式下阻止怪物蛋使用，并在 Tooltip 中显示警告。
         */
        public static boolean peacefulMonsterEggRestriction = true;

        /**
         * Fix bat wing animation float overflow when ageInTicks grows very large.
         * 修复 ageInTicks 过大时蝙蝠翅膀动画的浮点溢出问题。
         */
        public static boolean fixBatWingAnimationOverflow = true;

        /**
         * Clamp GuiSlot scroll amount to non-negative for short lists.
         * 修复短列表时 GuiSlot 滚动量为负数的问题。
         */
        public static boolean fixGuiSlotNegativeScroll = true;

        /**
         * Block only the Alt+F4 window close request; the title-bar close button
         * keeps working normally.
         * 仅阻拦 Alt+F4 触发的窗口关闭请求；标题栏关闭按钮仍可正常退出。
         */
        public static boolean blockAltF4WindowClose = true;

        /**
         * Keep background music playing across GUI switches: the pause menu no
         * longer pauses/resumes the MusicTicker track, so opening or closing any
         * GUI never interrupts the music.
         * GUI 切换期间保持背景音乐持续播放：暂停菜单不再暂停/恢复 MusicTicker 曲目，
         * 打开或关闭任何 GUI 都不会中断音乐。
         */
        public static boolean keepMusicDuringGuiSwitch = true;

        /**
         * Fix MC-179165: releasing the mouse over a tab while dragging the
         * creative inventory scrollbar no longer switches to that tab.
         * 修复 MC-179165：拖动创造模式物品栏滚动条时，在标签上松开鼠标
         * 不再误切换到该标签。
         */
        public static boolean fixCreativeScrollbarTabClick = true;

        /**
         * Fix FontRenderer logic errors: unicode width now matches the actual
         * rendered advance, overflowing first characters no longer crash
         * word-wrapping with a StackOverflowError, a trailing formatting code
         * costs no width, and the obfuscated style can no longer hang the game.
         * 修复 FontRenderer 逻辑错误：unicode 宽度与实际渲染推进一致、超宽首字符
         * 不再导致换行 StackOverflowError 崩溃、末尾格式码不再占负宽度、
         * 乱码样式不再可能卡死游戏。
         */
        public static boolean fixFontRendererLogic = true;

        /**
         * Fix villages not spawning in superflat worlds when VillageNames is
         * installed: ChunkProviderFlat never fires InitMapGenEvent, so the mod's
         * {@code MapGenVillageVN} is swapped in directly after construction.
         * 修复安装 VillageNames 后超平坦世界不生成村庄的问题：ChunkProviderFlat
         * 从不触发 InitMapGenEvent，改为在构造完成后直接替换为该模组的生成器。
         */
        public static boolean fixVillageNamesFlatWorldVillages = true;

        /**
         * Filter virtual/recording audio devices and fix device-name encoding
         * at the JavaSound API level, plus add an audio output device switcher
         * button to the sound settings GUI (switches via paulscode
         * LibraryJavaSound#setMixer). Client only.
         * 在 JavaSound API 层面过滤虚拟/录音设备并修复设备名编码，
         * 同时在声音设置界面添加音频输出设备切换按钮
         * （通过 paulscode LibraryJavaSound#setMixer 切换）。仅客户端。
         */
        public static boolean audioOutputDeviceSwitch = true;

        /**
         * Fix paulscode's OGG decoder (CodecJOrbis#readAll) reallocating and
         * copying the whole accumulated buffer for every ~16 KB chunk while
         * holding the global sound lock, which stalled the sound system when a
         * large sound was first played. Client only.
         * 修复 paulscode 的 OGG 解码器（CodecJOrbis#readAll）每解码一个约
         * 16KB 块就整体重新分配复制累计缓冲、且全程持有声音全局锁的问题，
         * 该问题会在首次播放大型音效时使整个声音系统卡顿。仅客户端。
         */
        public static boolean fixOggDecodeQuadraticCopy = true;

        /**
         * Raise paulscode's channel counts from the 1.7.10 defaults (28 normal
         * + 4 streaming) to the modern Minecraft standard (247 normal + 8
         * streaming, the vanilla 1.8+ SoundManager values), reducing sounds
         * being cut off or dropped when many effects play at once. Client only.
         * 将 paulscode 声道数从 1.7.10 默认值（28 普通 + 4 流式）提升到现代
         * Minecraft 标准（247 普通 + 8 流式，即原版 1.8+ SoundManager 的数值），
         * 减少多个音效同时播放时声音被截断或丢失的问题。仅客户端。
         */
        public static boolean modernSoundChannelCounts = true;

        /**
         * <p>
         * Load (and create on first run) {@code config/bugfwxer.cfg}.<br>
         * 加载（首次运行时创建）{@code config/bugfwxer.cfg}。
         * </p>
         * <p>
         * Called from the mixin plugin during the coremod/launch phase, so only
         * launch-safe classes (Forge Configuration, LaunchWrapper) may be used
         * here.<br>
         * 在 coremod/启动阶段由 Mixin 插件调用，因此这里只能使用启动期安全的类
         * （Forge Configuration、LaunchWrapper）。
         * </p>
         */
        public static void load() {
                // Resolve the config dir from LaunchWrapper; fall back to the working directory
                // in odd launch setups
                // 通过 LaunchWrapper 定位配置目录；异常启动环境下回退到工作目录
                File gameDir = Launch.minecraftHome != null ? Launch.minecraftHome : new File(".");
                Configuration config = new Configuration(new File(gameDir, "config/bugfwxer.cfg"));

                try {
                        config.load();

                        config.setCategoryComment(CATEGORY_FIXES,
                                        "Toggle individual bugfixes. All fixes are applied as mixins at class-load time,\n"
                                                        + "so changes only take effect after a game restart.\n"
                                                        + "逐项开关各个修复。所有修复均以 Mixin 形式在类加载时注入，修改后需重启游戏生效。");

                        fixDarkOakSaplingBonemeal = config.getBoolean("fixDarkOakSaplingBonemeal", CATEGORY_FIXES, true,
                                        "Prevent dark oak saplings from consuming bonemeal without a valid 2x2 cluster.\n"
                                                        + "防止黑橡木树苗在没有有效 2x2 阵型时白白消耗骨粉。");

                        fixBonemealHeightLimit = config.getBoolean("fixBonemealHeightLimit", CATEGORY_FIXES, true,
                                        "Prevent bonemeal from being consumed on plants at the world height limit.\n"
                                                        + "防止在世界高度上限处对植物使用骨粉时白白消耗骨粉。");

                        fixItemPositionDesync = config.getBoolean("fixItemPositionDesync", CATEGORY_FIXES, true,
                                        "Fix MC-4: item entities visually desyncing between client and server near block edges.\n"
                                                        + "修复 MC-4：物品实体在方块边缘处客户端与服务端位置不同步的问题。");

                        fixItemStackHasEffectNpe = config.getBoolean("fixItemStackHasEffectNpe", CATEGORY_FIXES, true,
                                        "Fix NPE crash from ItemStack.hasEffect when quickly switching creative inventory tabs.\n"
                                                        + "修复创造模式物品栏快速切换标签页时 ItemStack.hasEffect 的空指针崩溃。");

                        peacefulMonsterEggRestriction = config.getBoolean("peacefulMonsterEggRestriction",
                                        CATEGORY_FIXES, true,
                                        "Block monster spawn eggs in peaceful mode and show a tooltip warning.\n"
                                                        + "和平模式下阻止使用怪物刷怪蛋，并在物品提示中显示警告。");

                        fixBatWingAnimationOverflow = config.getBoolean("fixBatWingAnimationOverflow", CATEGORY_FIXES,
                                        true,
                                        "Fix bat wing animation freezing/jittering after very long world uptime (float overflow). Client only.\n"
                                                        + "修复世界运行时间过长后蝙蝠翅膀动画卡住/抖动的浮点溢出问题。仅客户端。");

                        fixGuiSlotNegativeScroll = config.getBoolean("fixGuiSlotNegativeScroll", CATEGORY_FIXES, true,
                                        "Clamp GUI list scroll amount to non-negative when the list is shorter than the view. Client only.\n"
                                                        + "当列表内容不足一屏时，将 GUI 列表滚动量钳制为非负值。仅客户端。");

                        blockAltF4WindowClose = config.getBoolean("blockAltF4WindowClose", CATEGORY_FIXES, true,
                                        "Block only the Alt+F4 window close request; the title-bar close button still works. Client only.\n"
                                                        + "仅阻拦 Alt+F4 触发的窗口关闭请求；标题栏关闭按钮仍可正常退出。仅客户端。");

                        keepMusicDuringGuiSwitch = config.getBoolean("keepMusicDuringGuiSwitch", CATEGORY_FIXES, true,
                                        "Keep background music playing across GUI switches; the pause menu no longer interrupts the track. Client only.\n"
                                                        + "GUI 切换期间保持背景音乐持续播放，暂停菜单不再中断音乐。仅客户端。");

                        fixCreativeScrollbarTabClick = config.getBoolean("fixCreativeScrollbarTabClick", CATEGORY_FIXES,
                                        true,
                                        "Fix MC-179165: releasing the mouse over a creative inventory tab while dragging the scrollbar no longer switches to that tab. Client only.\n"
                                                        + "修复 MC-179165：拖动创造模式物品栏滚动条时，在标签上松开鼠标不再误切换到该标签。仅客户端。");

                        fixFontRendererLogic = config.getBoolean("fixFontRendererLogic", CATEGORY_FIXES, true,
                                        "Fix FontRenderer logic: unicode width matches the rendered advance, overflowing first chars no longer crash word wrap (StackOverflowError), trailing format codes cost no width, and the obfuscated style can no longer hang the game. Client only.\n"
                                                        + "修复 FontRenderer 逻辑：unicode 宽度与实际渲染推进一致、超宽首字符不再导致换行崩溃（StackOverflowError）、末尾格式码不占宽度、乱码样式不再卡死游戏。仅客户端。");

                        fixVillageNamesFlatWorldVillages = config.getBoolean("fixVillageNamesFlatWorldVillages",
                                        CATEGORY_FIXES, true,
                                        "Fix villages not spawning in superflat worlds when VillageNames is installed: ChunkProviderFlat never fires InitMapGenEvent, so the mod's village generator is swapped in directly. Requires VillageNames.\n"
                                                        + "修复安装 VillageNames 后超平坦世界不生成村庄的问题：ChunkProviderFlat 从不触发 InitMapGenEvent，改为直接替换为该模组的村庄生成器。需要安装 VillageNames。");

                        audioOutputDeviceSwitch = config.getBoolean("audioOutputDeviceSwitch", CATEGORY_FIXES, true,
                                        "Filter virtual/recording audio devices, fix device-name encoding at the JavaSound API level, and add an audio output device switcher button to the sound settings GUI. Client only.\n"
                                                        + "过滤虚拟/录音音频设备、修复 JavaSound API 层面的设备名编码，并在声音设置界面添加音频输出设备切换按钮。仅客户端。");

                        fixOggDecodeQuadraticCopy = config.getBoolean("fixOggDecodeQuadraticCopy", CATEGORY_FIXES,
                                        true,
                                        "Fix paulscode's OGG decoder reallocating and copying the whole accumulated buffer for every ~16 KB chunk while holding the global sound lock, which stalled the sound system when a large sound was first played. Client only.\n"
                                                        + "修复 paulscode 的 OGG 解码器每解码一个约 16KB 块就整体重新分配复制累计缓冲、且全程持有声音全局锁的问题，该问题会在首次播放大型音效时使整个声音系统卡顿。仅客户端。");

                        modernSoundChannelCounts = config.getBoolean("modernSoundChannelCounts", CATEGORY_FIXES,
                                        true,
                                        "Raise paulscode's channel counts from the 1.7.10 defaults (28 normal + 4 streaming) to the modern Minecraft standard (247 normal + 8 streaming, the vanilla 1.8+ SoundManager values), reducing sounds being cut off or dropped when many effects play at once. Client only.\n"
                                                        + "将 paulscode 声道数从 1.7.10 默认值（28 普通 + 4 流式）提升到现代 Minecraft 标准（247 普通 + 8 流式，即原版 1.8+ SoundManager 的数值），减少多个音效同时播放时声音被截断或丢失的问题。仅客户端。");
                } finally {
                        // Persist defaults / newly added keys back to disk
                        // 将默认值或新增键写回磁盘
                        if (config.hasChanged()) {
                                config.save();
                        }
                }
        }
}
