package carpetodd.xm.network;

import carpetodd.xm.CarpetOddMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//#if MC >= 26_01_00
//$$ import net.minecraft.resources.Identifier;
//#else
import net.minecraft.resources.ResourceLocation;
//#endif

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Server-to-client sync of the playerInventoryStack rules (pattern -> custom max size).
 * Clients need the rules so ItemStack.getMaxStackSize() returns the custom limit,
 * which is what client-side sorting mods (e.g. Inventory Profiles Next) read when planning.
 */
public record SyncCustomStackSizePayload(boolean enabled, Map<String, Integer> customStacks)
        implements CustomPacketPayload {

    //#if MC >= 26_01_00
    //$$ public static final Type<SyncCustomStackSizePayload> TYPE =
    //$$         new Type<>(Identifier.fromNamespaceAndPath(CarpetOddMod.MOD_ID, "sync_custom_stack_size"));
    //#else
    public static final Type<SyncCustomStackSizePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CarpetOddMod.MOD_ID, "sync_custom_stack_size"));
    //#endif

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCustomStackSizePayload> CODEC =
            new StreamCodec<>() {
                @Override
                public SyncCustomStackSizePayload decode(RegistryFriendlyByteBuf buf) {
                    boolean enabled = buf.readBoolean();
                    int size = buf.readVarInt();
                    Map<String, Integer> map = new LinkedHashMap<>(size);
                    for (int i = 0; i < size; i++) {
                        map.put(buf.readUtf(), buf.readVarInt());
                    }
                    return new SyncCustomStackSizePayload(enabled, map);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, SyncCustomStackSizePayload value) {
                    buf.writeBoolean(value.enabled);
                    buf.writeVarInt(value.customStacks.size());
                    value.customStacks.forEach((pattern, count) -> {
                        buf.writeUtf(pattern);
                        buf.writeVarInt(count);
                    });
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
