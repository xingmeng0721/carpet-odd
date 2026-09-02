package carpetodd.xm.mixin.carpet;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.SettingsManager;
import carpetodd.xm.network.OddNetwork;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Re-syncs playerInventoryStack rules to every client whenever the rule is toggled via /carpet. */
@Mixin(SettingsManager.class)
public abstract class SettingsManagerMixin {

    @WrapOperation(method = "setRule",
            at = @At(value = "INVOKE",
                    target = "Lcarpet/api/settings/CarpetRule;set(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V"))
    private <T> void carpetOdd$broadcastOnToggle(CarpetRule<T> rule, CommandSourceStack source, String value,
                                                 Operation<Void> original) {
        original.call(rule, source, value);
        if ("playerInventoryStack".equals(rule.name())) {
            OddNetwork.broadcast();
        }
    }
}
