package carpetodd.xm;

import carpet.api.settings.Rule;

public class CarpetOddSettings {

    public static final String ODD = "odd";

    private CarpetOddSettings() {}

    @Rule(categories = {ODD})
    public static boolean autoDrop = false;

    @Rule(categories = {ODD})
    public static boolean bonemealSporeBlossom = false;

    @Rule(categories = {ODD})
    public static boolean torchflowerDropSeeds = false;

    @Rule(categories = {ODD})
    public static boolean cactusOxidizeCopper = false;

    @Rule(categories = {ODD})
    public static boolean batchPlayerCommand = false;

    @Rule(categories = {ODD})
    public static boolean playerInventoryStack = false;
}
