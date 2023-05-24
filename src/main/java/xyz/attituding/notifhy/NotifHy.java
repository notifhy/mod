package xyz.attituding.notifhy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.attituding.notifhy.config.NotifHyConfigManager;
import xyz.attituding.notifhy.core.NotifHyCore;

@Mod(modid = NotifHy.MOD_ID, useMetadata = true, clientSideOnly = true, guiFactory = "xyz.attituding.notifhy.config.NotifHyConfigGuiFactory")
public class NotifHy {
    @Mod.Instance(NotifHy.MOD_ID)
    public static NotifHy instance;

    public static final String MOD_ID = "notifhy";
    public static final String NAME = "NotifHy";

    public static final Logger LOGGER = LogManager.getLogger(NotifHy.NAME);

    public static NotifHyConfigManager configManager;
    public static NotifHyCore core;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configManager = new NotifHyConfigManager(event);
        core = new NotifHyCore();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(configManager);
        MinecraftForge.EVENT_BUS.register(core);
    }
}
