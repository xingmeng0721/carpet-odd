package carpetodd.xm;

import carpet.CarpetServer;
import carpetodd.xm.network.OddNetwork;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarpetOddMod implements ModInitializer {

    public static final String MOD_ID = "carpet-odd";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        OddNetwork.register();
        OddNetwork.registerServerHooks();
        CarpetOddExtension.init();
        LOGGER.info("Carpet Odd Addition loaded");
    }
}
