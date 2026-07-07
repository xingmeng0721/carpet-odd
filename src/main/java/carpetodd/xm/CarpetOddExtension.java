package carpetodd.xm;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpetodd.xm.command.AutoDropCommand;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class CarpetOddExtension implements CarpetExtension {

    private static final CarpetOddExtension INSTANCE = new CarpetOddExtension();
    private static final Gson GSON = new Gson();

    private CarpetOddExtension() {}

    public static CarpetOddExtension getInstance() {
        return INSTANCE;
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(CarpetOddSettings.class);
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher,
                                 CommandBuildContext commandBuildContext) {
        dispatcher.register(
                Commands.literal("player")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .then(Commands.literal("autodrop")
                                        .executes(AutoDropCommand::execute))));
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        String path = String.format("assets/%s/lang/%s.json", CarpetOddMod.MOD_ID, lang);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) return Collections.emptyMap();
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return GSON.fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
        } catch (Exception e) {
            CarpetOddMod.LOGGER.warn("Failed to load translations for lang: {}", lang, e);
            return Collections.emptyMap();
        }
    }

    @Override
    public String version() {
        return CarpetOddMod.MOD_ID;
    }
}
