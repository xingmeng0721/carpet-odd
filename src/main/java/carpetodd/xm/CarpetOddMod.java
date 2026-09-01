package carpetodd.xm;

import carpet.CarpetServer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarpetOddMod implements ModInitializer {

    public static final String MOD_ID = "carpet-odd";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CarpetOddExtension.init();
        LOGGER.info("Carpet Odd Addition loaded");
    }
}
