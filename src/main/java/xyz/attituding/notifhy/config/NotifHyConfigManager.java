package xyz.attituding.notifhy.config;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.attituding.notifhy.NotifHy;

import java.io.File;

public class NotifHyConfigManager {
    private final Configuration config;

    public NotifHyConfigManager(FMLPreInitializationEvent event) {
        File configFile = new File(event.getModConfigurationDirectory(), NotifHy.MOD_ID + ".cfg");
        config = new Configuration(configFile);
        config.load();
        updateConfig();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(NotifHy.MOD_ID)) {
            updateConfig();
        }
    }

    private void updateConfig() {
        Property property;

        property = config.get(NotifHyConfig.CATEGORY_NONE, "authentication", "");
        property.setLanguageKey("config.notifhy.option.authentication");
        property.comment = I18n.format("config.notifhy.option.authentication.tooltip");
        NotifHyConfig.authentication = property.getString();

        property = config.get(NotifHyConfig.CATEGORY_ADVANCED, "advanced.server", "https://notifhy-api.attituding.xyz/v1/event");
        property.setLanguageKey("config.notifhy.option.advanced.server");
        property.comment = I18n.format("config.notifhy.option.advanced.server.tooltip");
        NotifHyConfig.Advanced.server = property.getString();

        property = config.get(NotifHyConfig.CATEGORY_ADVANCED, "advanced.hosts", new String[]{"mc.hypixel.net"});
        property.setLanguageKey("config.notifhy.option.advanced.hosts");
        property.comment = I18n.format("config.notifhy.option.advanced.hosts.tooltip");
        NotifHyConfig.Advanced.hosts = property.getStringList();

        config.setCategoryLanguageKey(NotifHyConfig.CATEGORY_ADVANCED, "config.notifhy.option.advanced");
        config.setCategoryComment(NotifHyConfig.CATEGORY_ADVANCED, I18n.format("config.notifhy.option.advanced.tooltip"));

        if (config.hasChanged()) {
            config.save();
        }
    }

    public Configuration getConfig() {
        return config;
    }
}
