package decok.dfcdvadstf.bugfwxer.mixins.mixin.minecraft.entity;

import net.minecraft.client.model.ModelBat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ModelBat.class)
public abstract class MixinEntityBat {
    
    /**
     * <p>
     *     修复蝙蝠翅膀旋转角度数据溢出问题<br>
     *      当 ageInTicks 值变得非常大时，乘以系数后传给 cos 函数会导致浮点数精度丢失。
     *      对 ageInTicks 取模 2*PI，保持动画连续性
     * </p>
     * @author Seniye
     * @see net.minecraft.util.MathHelper#cos(float) MathHelper.cos
     */
    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/MathHelper;cos(F)F",
            ordinal = 0
        )
    )
    private float fixWingRotationOverflow(float ageInTicks) {
        // 对 ageInTicks 取模 2*PI，避免浮点数精度丢失
        // 2*PI ≈ 6.283185307179586
        float twoPi = (float)(Math.PI * 2);
        return ageInTicks % twoPi;
    }
}
