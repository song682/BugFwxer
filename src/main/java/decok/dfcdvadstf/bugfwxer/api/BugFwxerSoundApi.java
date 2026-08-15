package decok.dfcdvadstf.bugfwxer.api;

import paulscode.sound.Channel;
import paulscode.sound.SoundSystemConfig;

import java.util.List;

/**
 * <p>
 * Public API entry point for external mods to query the current sound channel
 * usage (normal and streaming: total, in-use and playing counts).<br>
 * 对外公开的 API 入口：外部模组通过它查询当前声道使用情况
 * （普通与流式：总数、占用数、播放中数）。
 * </p>
 * <p>
 * Usage: {@code SoundChannelUsage usage = BugFwxerSoundApi.getChannelUsage();}
 * then check {@link SoundChannelUsage#isAvailable()} before reading the
 * counters. The returned snapshot is immutable and thread-safe, so it may be
 * polled freely (e.g. once per second for a HUD); each call holds the
 * paulscode {@link SoundSystemConfig#THREAD_SYNC} lock only while scanning
 * the channel lists, so polling here does not disturb sound playback.<br>
 * 用法：{@code SoundChannelUsage usage = BugFwxerSoundApi.getChannelUsage();}
 * 然后先检查 {@link SoundChannelUsage#isAvailable()} 再读取计数。
 * 返回的快照不可变且线程安全，可随意轮询（例如 HUD 每秒一次）；
 * 每次调用仅在扫描声道列表期间持有 paulscode 的
 * {@link SoundSystemConfig#THREAD_SYNC} 锁，因此轮询不会干扰声音播放。
 * </p>
 * <p>
 * This API is always on and requires no configuration: it is a pure read-only
 * "port" for third-party mods. If the sound system has not been initialized
 * yet, {@link SoundChannelUsage#isAvailable()} is {@code false} and all
 * counters are zero.<br>
 * 本 API 始终启用、无需配置：它是面向第三方模组的纯只读"传输口"。
 * 若声音系统尚未初始化，{@link SoundChannelUsage#isAvailable()} 为
 * {@code false} 且所有计数为 0。
 * </p>
 *
 * @author Seniye
 */
public final class BugFwxerSoundApi {

    /** Normal (non-streaming) channel list of the active paulscode Library. 当前 paulscode Library 的普通声道列表。 */
    private static volatile List<Channel> normalChannels;

    /** Streaming channel list of the active paulscode Library. 当前 paulscode Library 的流式声道列表。 */
    private static volatile List<Channel> streamingChannels;

    /** Private constructor; static API only. 私有构造；纯静态 API。 */
    private BugFwxerSoundApi() {}

    /**
     * Returns an immutable snapshot of the current sound channel usage.
     * {@link SoundChannelUsage#isAvailable()} is {@code false} until the
     * paulscode Library has finished {@code init()}.<br>
     * 返回当前声道使用情况的不可变快照。在 paulscode Library 完成
     * {@code init()} 之前 {@link SoundChannelUsage#isAvailable()} 为
     * {@code false}。
     *
     * @return the channel usage snapshot, never null 声道使用快照，永不为 null
     */
    public static SoundChannelUsage getChannelUsage() {
        final List<Channel> normals = normalChannels;
        final List<Channel> streamings = streamingChannels;
        if (normals == null || streamings == null) {
            return new SoundChannelUsage(false, 0, 0, 0, 0, 0, 0);
        }
        // The channel lists are mutated by paulscode's CommandThread/StreamThread
        // under this lock, so scan them under the same lock.
        // 声道列表由 paulscode 的 CommandThread/StreamThread 在该锁下修改，因此须在同一锁下扫描。
        synchronized (SoundSystemConfig.THREAD_SYNC) {
            int normalInUse = 0;
            int normalPlaying = 0;
            for (Channel channel : normals) {
                if (channel.attachedSource != null) {
                    normalInUse++;
                }
                if (channel.playing()) {
                    normalPlaying++;
                }
            }
            int streamingInUse = 0;
            int streamingPlaying = 0;
            for (Channel channel : streamings) {
                if (channel.attachedSource != null) {
                    streamingInUse++;
                }
                if (channel.playing()) {
                    streamingPlaying++;
                }
            }
            return new SoundChannelUsage(true, normals.size(), normalInUse, normalPlaying,
                    streamings.size(), streamingInUse, streamingPlaying);
        }
    }

    /**
     * <p>
     * Internal registration hook: called by MixinLibraryChannelUsage after
     * {@code paulscode.sound.Library#init()} has created the channel lists.
     * <b>External mods must never call this.</b><br>
     * 内部注册钩子：由 MixinLibraryChannelUsage 在
     * {@code paulscode.sound.Library#init()} 创建完声道列表后调用。
     * <b>外部模组切勿调用。</b>
     * </p>
     *
     * @param normal    the normal (non-streaming) channel list 普通声道列表
     * @param streaming the streaming channel list 流式声道列表
     */
    public static void registerChannels(List<Channel> normal, List<Channel> streaming) {
        normalChannels = normal;
        streamingChannels = streaming;
    }
}
