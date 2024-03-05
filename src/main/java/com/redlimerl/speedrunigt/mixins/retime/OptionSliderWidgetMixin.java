package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.class_394;
import net.minecraft.client.option.GameOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_394.class)
public class OptionSliderWidgetMixin {


    @Shadow private GameOption field_1283;

    @Inject(method = "mouseReleased", at = @At("RETURN"))
    public void onClickOption(int mouseY, int par2, CallbackInfo ci) {
        InGameTimerUtils.CHANGED_OPTIONS.add(this.field_1283);
    }
}
