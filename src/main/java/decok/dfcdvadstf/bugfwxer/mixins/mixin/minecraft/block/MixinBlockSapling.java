package decok.dfcdvadstf.bugfwxer.mixins.mixin.minecraft.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockSapling;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockSapling.class)
public abstract class MixinBlockSapling {

    /**
     * Vanilla helper: checks whether the block at (x, y, z) is this sapling with the given type (meta &amp; 7).
     * 原版辅助方法：检查 (x, y, z) 处方块是否为指定类型（meta &amp; 7）的本树苗。
     */
    @Shadow
    public abstract boolean func_149880_a(World world, int x, int y, int z, int meta);

    /**
     * <p>
     *     Fix dark oak sapling consuming bonemeal without a valid 2x2 cluster.<br>
     *     修复黑橡木树苗在没有有效 2x2 阵型时仍消耗骨粉的问题。<br>
     *     Vanilla {@code func_149851_a} unconditionally returns true, but dark oak
     *     (roofed_oak, meta 5) only grows via {@code func_149878_d} case 5 when four
     *     saplings form a 2x2 square — otherwise it silently returns while the
     *     bonemeal is already consumed.<br>
     *     原版 {@code func_149851_a} 无条件返回 true，但黑橡木（roofed_oak，meta 5）
     *     只有在 {@code func_149878_d} 的 case 5 中找到 2x2 四棵一组的阵型才会生成树，
     *     否则静默返回，而骨粉已经被消耗掉了。<br>
     *     Uses MixinExtras {@code @ModifyReturnValue}: the original body still runs,
     *     only the return value is suppressed, so it chains with other mods' handlers.<br>
     *     使用 MixinExtras 的 {@code @ModifyReturnValue}：原方法体照常执行，
     *     仅压制返回值，可与其他模组的处理器链式共存。
     * </p>
     * @author Seniye
     */
    @ModifyReturnValue(
        method = "func_149851_a",
        at = @At("RETURN")
    )
    private boolean requireDarkOakCluster(boolean original, World world, int x, int y, int z, boolean isClient) {
        // 仅处理黑橡木树苗（meta & 7 == 5），其余树苗保持原判定
        // Only handle dark oak saplings (meta & 7 == 5); other sapling types keep the original result
        if ((world.getBlockMetadata(x, y, z) & 7) == 5) {
            // 复刻原版 func_149878_d case 5 的 2x2 扫描：以当前树苗为角，向四个方向各尝试一次
            // Replicate the vanilla 2x2 scan from func_149878_d case 5: try each corner offset around this sapling
            for (int dx = 0; dx >= -1; --dx) {
                for (int dz = 0; dz >= -1; --dz) {
                    if (this.func_149880_a(world, x + dx, y, z + dz, 5)
                            && this.func_149880_a(world, x + dx + 1, y, z + dz, 5)
                            && this.func_149880_a(world, x + dx, y, z + dz + 1, 5)
                            && this.func_149880_a(world, x + dx + 1, y, z + dz + 1, 5)) {
                        // 找到有效的 2x2 阵型，允许使用骨粉
                        // Found a valid 2x2 cluster, bonemeal is allowed
                        return original;
                    }
                }
            }

            // 没有四棵一组的阵型，判定为不可生长，避免白白消耗骨粉
            // No 2x2 cluster present: not growable, so bonemeal is not wasted
            return false;
        }

        // 否则保留原判定结果
        // Otherwise keep the original result
        return original;
    }
}
