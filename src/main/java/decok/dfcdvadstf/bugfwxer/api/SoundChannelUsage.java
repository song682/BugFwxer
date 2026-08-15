package decok.dfcdvadstf.bugfwxer.api;

/**
 * <p>
 * Immutable snapshot of the paulscode sound channel usage, as consumed by
 * external mods through {@link BugFwxerSoundApi}.<br>
 * paulscode 声道使用情况的不可变快照，外部模组通过
 * {@link BugFwxerSoundApi} 获取。
 * </p>
 * <p>
 * Two independent counters are provided for each channel type:
 * <ul>
 * <li>{@code inUse}: channels that currently have a source attached
 * ({@code Channel.attachedSource != null});</li>
 * <li>{@code playing}: channels that are actively producing audio
 * ({@code Channel.playing()}, i.e. the underlying Clip/SourceDataLine is
 * active).</li>
 * </ul>
 * A channel can be attached but not playing (paused, fading out, or between
 * queued sounds), so both numbers are useful for a usage display.<br>
 * 每种声道提供两个独立计数：{@code inUse}（当前附着有声音源的声道，
 * {@code Channel.attachedSource != null}）与 {@code playing}（正在出声的声道，
 * {@code Channel.playing()}，即底层 Clip/SourceDataLine 处于活跃状态）。
 * 声道可能已附着但未播放（暂停、淡出或切换队列间隙），因此两个数字
 * 对使用情况展示都有意义。
 * </p>
 * <p>
 * If the sound system has not been initialized yet (early in startup, or the
 * API toggle is off), {@link #isAvailable()} returns {@code false} and all
 * counters are zero; callers should always check it first.<br>
 * 若声音系统尚未初始化（启动早期，或 API 开关被关闭），
 * {@link #isAvailable()} 返回 {@code false} 且所有计数为 0；
 * 调用方应总是先检查该标志。
 * </p>
 *
 * @author Seniye
 */
public final class SoundChannelUsage {

    /** Whether the sound system is up and the counters are meaningful. 声音系统是否就绪、计数是否有效。 */
    private final boolean available;

    /** Total number of normal (non-streaming) channels actually created. 实际创建的普通（非流式）声道总数。 */
    private final int normalTotal;

    /** Number of normal channels with a source attached. 附着有声音源的普通声道数。 */
    private final int normalInUse;

    /** Number of normal channels actively playing audio. 正在播放音频的普通声道数。 */
    private final int normalPlaying;

    /** Total number of streaming channels actually created. 实际创建的流式声道总数。 */
    private final int streamingTotal;

    /** Number of streaming channels with a source attached. 附着有声音源的流式声道数。 */
    private final int streamingInUse;

    /** Number of streaming channels actively playing audio. 正在播放音频的流式声道数。 */
    private final int streamingPlaying;

    /**
     * Package-private constructor; only {@link BugFwxerSoundApi} builds
     * snapshots. 包内构造；仅 {@link BugFwxerSoundApi} 创建快照。
     */
    SoundChannelUsage(boolean available, int normalTotal, int normalInUse, int normalPlaying,
            int streamingTotal, int streamingInUse, int streamingPlaying) {
        this.available = available;
        this.normalTotal = normalTotal;
        this.normalInUse = normalInUse;
        this.normalPlaying = normalPlaying;
        this.streamingTotal = streamingTotal;
        this.streamingInUse = streamingInUse;
        this.streamingPlaying = streamingPlaying;
    }

    /**
     * Whether the sound system is up and the counters are meaningful.
     * 声音系统是否就绪、计数是否有效。
     */
    public boolean isAvailable() {
        return available;
    }

    /** Total number of normal (non-streaming) channels actually created. 实际创建的普通（非流式）声道总数。 */
    public int getNormalTotal() {
        return normalTotal;
    }

    /** Number of normal channels with a source attached. 附着有声音源的普通声道数。 */
    public int getNormalInUse() {
        return normalInUse;
    }

    /** Number of normal channels actively playing audio. 正在播放音频的普通声道数。 */
    public int getNormalPlaying() {
        return normalPlaying;
    }

    /** Total number of streaming channels actually created. 实际创建的流式声道总数。 */
    public int getStreamingTotal() {
        return streamingTotal;
    }

    /** Number of streaming channels with a source attached. 附着有声音源的流式声道数。 */
    public int getStreamingInUse() {
        return streamingInUse;
    }

    /** Number of streaming channels actively playing audio. 正在播放音频的流式声道数。 */
    public int getStreamingPlaying() {
        return streamingPlaying;
    }

    @Override
    public String toString() {
        return "SoundChannelUsage[available=" + available
                + ", normal " + normalInUse + "/" + normalTotal + " in use (" + normalPlaying + " playing)"
                + ", streaming " + streamingInUse + "/" + streamingTotal + " in use (" + streamingPlaying + " playing)]";
    }
}
