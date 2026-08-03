package decok.dfcdvadstf.bugfwxer.mixins;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import decok.dfcdvadstf.bugfwxer.Tags;

import java.util.*;

@MCVersion("1.7.10")
@Name("BugFwxer")
public class EarlyMixinPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    @Override
    public String getMixinConfig() {
        return "mixins." + Tags.MODID + ".early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return Collections.singletonList(
                "early.minecraft.MixinRenderBlocks"
        );
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
