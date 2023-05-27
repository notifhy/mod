package xyz.attituding.notifhy;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.attituding.notifhy.config.NotifHyConfig;
import xyz.attituding.notifhy.core.events.ClientPlayConnectionEventsHandler;

public class NotifHy implements ClientModInitializer {
    public static final String MOD_ID = "notifhy";
    public static final String NAME = "NotifHy";

    public static final Logger LOGGER = LoggerFactory.getLogger(NotifHy.NAME);

    @Override
    public void onInitializeClient() {
        AutoConfig.register(NotifHyConfig.class, GsonConfigSerializer::new);
        new ClientPlayConnectionEventsHandler();
    }
}
