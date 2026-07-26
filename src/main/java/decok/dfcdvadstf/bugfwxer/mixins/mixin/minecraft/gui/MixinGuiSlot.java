package decok.dfcdvadstf.bugfwxer.mixins.mixin.minecraft.gui;

import net.minecraft.client.gui.GuiSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 *  MixinGuiSlot
 *  Clamps the amount scrolled to be non-negative.
 *  This is for the case when the list is short and the void is return -80
 *  @see GuiSlot#func_148135_f() func_148135_f()
 *  @author Seniye
 */
@Mixin(GuiSlot.class)
public abstract class MixinGuiSlot {
    @Accessor
    abstract float getAmountScrolled();

    @Accessor
    abstract void setAmountScrolled(float var1);

    @Inject(
        method = {"bindAmountScrolled"},
        at = {@At("TAIL")}
    )
    private void modernStatistic$clampScrollNonNegative(CallbackInfo ci) {
        if (this.getAmountScrolled() < 0.0F) {
            this.setAmountScrolled(0.0F);
        }

    }
}
