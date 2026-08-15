package decok.dfcdvadstf.bugfwxer;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import decok.dfcdvadstf.bugfwxer.audio.AudioDeviceMonitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;

@Mod(modid = Tags.MODID, name = Tags.NAME, version = Tags.VERSION, useMetadata = true)
public class BugFwxer {
    public static Logger logger = LogManager.getLogger(Tags.NAME);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Pre initialization logic
        logger = event.getModLog();
        logger.info("Pre initialization logic complete");
        MixinBootstrap.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // Initialization logic
        logger.info("Initialization logic complete");

        // Start the audio device monitor on the client (gated by the config
        // toggle) so the sound settings GUI can enumerate and switch output
        // devices. 客户端启动音频设备监控器（受配置开关控制），
        // 使声音设置界面能枚举和切换输出设备。
        if (event.getSide().isClient() && BugFwxerConfig.audioOutputDeviceSwitch) {
            AudioDeviceMonitor.INSTANCE.start();
        }
    }
}
