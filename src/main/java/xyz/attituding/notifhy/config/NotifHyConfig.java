package xyz.attituding.notifhy.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import xyz.attituding.notifhy.NotifHy;

import java.util.List;

@Config(name = NotifHy.MOD_ID)
public class NotifHyConfig implements ConfigData {
    @ConfigEntry.Gui.PrefixText
    public String authentication = "";

    @ConfigEntry.Gui.CollapsibleObject
    public Advanced advanced = new Advanced();

    public static class Advanced {
        @ConfigEntry.Gui.PrefixText
        public String server = "https://serverless.attituding.workers.dev";
        // https://tunnels.notifhy.attituding.xyz

        @ConfigEntry.Gui.PrefixText
        public List<String> domains = List.of("hypixel.net");
    }
}