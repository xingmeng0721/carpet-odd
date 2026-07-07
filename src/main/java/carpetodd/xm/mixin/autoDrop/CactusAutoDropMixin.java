package carpetodd.xm.mixin.autoDrop;

import carpet.patches.EntityPlayerMPFake;
import carpet.utils.Messenger;
import carpetodd.xm.CarpetOddSettings;
import carpetodd.xm.helper.AutoThrowService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class CactusAutoDropMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void carpetOdd$onAttack(Entity target, CallbackInfo ci) {
        if (!CarpetOddSettings.autoDrop) return;

        Player self = (Player) (Object) this;
        if (self.level().isClientSide()) return;

        if (!(target instanceof ServerPlayer fakePlayer)) return;
        if (!(fakePlayer instanceof EntityPlayerMPFake)) return;

        if (!self.getMainHandItem().is(Items.CACTUS)
                && !self.getOffhandItem().is(Items.CACTUS)) {
            return;
        }

        int count = AutoThrowService.processFakePlayer(fakePlayer);

        if (self instanceof ServerPlayer sp) {
            if (count > 0) {
                Messenger.m(sp, "g Dropped " + count + " shulker box(es)");
            } else {
                Messenger.m(sp, "c No qualifying shulker boxes found");
            }
        }

        ci.cancel();
    }
}
