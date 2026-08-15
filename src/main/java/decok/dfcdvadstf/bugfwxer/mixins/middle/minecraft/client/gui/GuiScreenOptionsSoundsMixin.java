package decok.dfcdvadstf.bugfwxer.mixins.middle.minecraft.client.gui;

import decok.dfcdvadstf.bugfwxer.audio.AudioDeviceMonitor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <p>
 * GuiScreenOptionsSoundsMixin<br>
 * Adds an audio output device switcher button to the vanilla sound settings
 * screen; implements {@link AudioDeviceMonitor.DeviceChangeListener} so the
 * button text follows device hot-plug events in real time.<br>
 * 在原版声音设置界面添加音频输出设备切换按钮；实现
 * {@link AudioDeviceMonitor.DeviceChangeListener}，
 * 让按钮文字实时跟随设备热插拔事件。
 * </p>
 *
 * @author Seniye
 */
@Mixin(GuiScreenOptionsSounds.class)
public abstract class GuiScreenOptionsSoundsMixin extends GuiScreen
        implements AudioDeviceMonitor.DeviceChangeListener {

    /** Button id of the audio output device switcher. 音频输出设备切换按钮的 id。 */
    private static final int AUDIO_DEVICE_BUTTON_ID = 300;

    /** The switcher button; null until initGui runs. 切换按钮；initGui 前为 null。 */
    private GuiButton audioDeviceButton;

    /** Set by the background monitor thread, consumed on the render thread. 由后台监控线程置位，渲染线程消费。 */
    private volatile boolean deviceListChanged = false;

    /**
     * Registers this screen as a device change listener and adds the switcher
     * button below the vanilla category buttons (above the "Done" button).
     * 注册本界面为设备变化监听器，并在原版类别按钮下方
     * （"完成"按钮上方）添加切换按钮。
     */
    @Inject(method = "initGui", at = @At("TAIL"))
    private void bugfwxer$onInitGui(CallbackInfo ci) {
        // Register as device change listener (remove first to avoid duplicates)
        // 注册为设备变化监听器（先移除旧注册，防止重复）
        AudioDeviceMonitor.INSTANCE.removeDeviceChangeListener(this);
        AudioDeviceMonitor.INSTANCE.addDeviceChangeListener(this);

        // Trigger an immediate scan so the screen always shows fresh devices
        // 立即触发设备扫描，确保打开界面时显示最新设备状态
        AudioDeviceMonitor.INSTANCE.forceCheckImmediately();

        // Place the button in the free space between the category buttons
        // (they end at height / 6 + 84) and the Done button (height / 6 + 168)
        // 把按钮放在类别按钮（结束于 height / 6 + 84）与"完成"按钮
        // （height / 6 + 168）之间的空位
        int buttonY = this.height / 6 + 144;

        audioDeviceButton = new GuiButton(AUDIO_DEVICE_BUTTON_ID,
            this.width / 2 - 100, buttonY, 200, 20, getAudioDeviceButtonText());

        @SuppressWarnings("unchecked")
        java.util.List<GuiButton> buttons = this.buttonList;
        buttons.add(audioDeviceButton);
    }

    /**
     * DeviceChangeListener callback - only sets a flag; the actual UI update
     * happens in drawScreen on the render thread.
     * DeviceChangeListener 回调——只设标志位，实际 UI 更新在 drawScreen
     * 的渲染线程中完成。
     */
    @Override
    public void onDeviceListChanged() {
        deviceListChanged = true;
    }

    /**
     * Cycles to the next audio output device when the switcher button is
     * clicked. 点击切换按钮时循环切换到下一个音频输出设备。
     */
    @Inject(method = "actionPerformed", at = @At("TAIL"))
    private void bugfwxer$onActionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.enabled && button.id == AUDIO_DEVICE_BUTTON_ID) {
            switchToNextAudioDevice();
        }
    }

    /**
     * Asks the monitor to switch to the next playback device and plays the
     * vanilla button-click sound on success.
     * 让监控器切换到下一个播放设备，成功时播放原版按钮点击音效。
     */
    private void switchToNextAudioDevice() {
        String newDevice = AudioDeviceMonitor.INSTANCE.switchToNextPlaybackDevice();
        if (newDevice != null) {
            Minecraft.getMinecraft().getSoundHandler().playSound(
                PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));

            // Refresh the button text 刷新按钮文字
            refreshAudioDeviceButton();
        }
    }

    /**
     * Refreshes the button text when the device list changed on the background
     * thread, and continuously syncs it with the current device name.
     * 设备列表在后台线程变化时刷新按钮文字，并持续与当前设备名保持同步。
     */
    @Inject(method = "drawScreen", at = @At("TAIL"))
    private void bugfwxer$onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        // Device list changed on the background thread - refresh the button text
        // 后台线程检测到设备列表变化——刷新按钮文字
        if (deviceListChanged) {
            deviceListChanged = false;
            refreshAudioDeviceButton();
        }

        // Keep the button text in sync with the current device name
        // 持续让按钮文字与当前设备名保持一致
        if (audioDeviceButton != null) {
            String currentText = getAudioDeviceButtonText();
            if (!audioDeviceButton.displayString.equals(currentText)) {
                audioDeviceButton.displayString = currentText;
            }
        }
    }

    /**
     * Button text: "output device: [device name]".
     * 按钮文字："输出设备: [设备名]"。
     */
    private String getAudioDeviceButtonText() {
        return I18n.format("button.output_device", getCurrentAudioDeviceName());
    }

    /**
     * Name of the currently selected audio output device.
     * 当前选中的音频输出设备名称。
     */
    private String getCurrentAudioDeviceName() {
        try {
            String deviceName = AudioDeviceMonitor.INSTANCE.getCurrentPlaybackDeviceName();
            if (deviceName != null) {
                return deviceName;
            }
        } catch (Exception e) {
            // Fall back to "unknown" 回退为"未知"
        }

        return I18n.format("button.unknown_device");
    }

    /**
     * Refreshes the switcher button text. 刷新切换按钮文字。
     */
    private void refreshAudioDeviceButton() {
        if (audioDeviceButton != null) {
            audioDeviceButton.displayString = getAudioDeviceButtonText();
        }
    }
}
