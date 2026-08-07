package decok.dfcdvadstf.bugfwxer.mixins;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import decok.dfcdvadstf.bugfwxer.BugFwxerConfig;
import decok.dfcdvadstf.bugfwxer.Tags;

import java.util.List;
import java.util.Map;
import java.util.Set;

@MCVersion("1.7.10")
@Name("BugFwxer")
public class EarlyMixinPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    @Override
    public String getMixinConfig() {
        return "mixins." + Tags.MODID + ".early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        // Load config/bugfwxer.cfg before the EARLY applyIf conditions are evaluated,
        // so toggles still gate the early mixins (the main config plugin runs later).
        // 在 EARLY 阶段 applyIf 条件求值之前加载 config/bugfwxer.cfg，
        // 使配置开关对 early mixin 同样生效（主配置插件运行得更晚）。
        BugFwxerConfig.load();
        return IMixins.getEarlyMixins(Mixins.class, loadedCoreMods);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return "";
    }

    @Override
    public String getSetupClass() {
        return "";
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return "";
    }
}
