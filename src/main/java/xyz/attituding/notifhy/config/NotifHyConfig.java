package xyz.attituding.notifhy.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import xyz.attituding.notifhy.NotifHy;

import java.util.Arrays;
import java.util.List;

@Config(name = NotifHy.MOD_ID)
public class NotifHyConfig implements ConfigData {
    @ConfigEntry.Gui.PrefixText
    public String authentication = "";

    @ConfigEntry.Gui.CollapsibleObject
    public Advanced advanced = new Advanced();

    public static class Advanced {
        @ConfigEntry.Gui.PrefixText
        public String server = "https://notifhy-api.attituding.xyz/v1/event";

        @ConfigEntry.Gui.PrefixText
        public List<String> hosts = Arrays.asList("hypixel.net");
    }
}