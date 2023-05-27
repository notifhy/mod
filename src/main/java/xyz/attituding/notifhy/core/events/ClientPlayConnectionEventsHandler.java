package xyz.attituding.notifhy.core.events;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.attituding.notifhy.NotifHy;
import xyz.attituding.notifhy.config.NotifHyConfig;
import xyz.attituding.notifhy.core.Event;
import xyz.attituding.notifhy.core.EventSender;
import xyz.attituding.notifhy.util.Chat;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ClientPlayConnectionEventsHandler {
    // Used for detecting duplicate events, which can occur when transferring between servers
    private boolean connected = false;

    public ClientPlayConnectionEventsHandler() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            preconditions(new Event(Event.Type.SERVER_CONNECTED.getType()), handler);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            preconditions(new Event(Event.Type.SERVER_DISCONNECTED.getType()), handler);
        });
    }

    private void preconditions(Event event, ClientPlayNetworkHandler handler) {
        NotifHyConfig config = AutoConfig.getConfigHolder(NotifHyConfig.class).getConfig();

        if ((event.type == Event.Type.SERVER_CONNECTED.getType() && connected) || (event.type == Event.Type.SERVER_DISCONNECTED.getType() && !connected)) {
            NotifHy.LOGGER.info("Skipped over duplicate connect event, value is " + connected);
            return;
        }

        connected = !connected;

        SocketAddress socketAddress = handler.getConnection().getAddress();

        // Verify SocketAddress is InetSocketAddress
        if (!(socketAddress instanceof InetSocketAddress inetSocketAddress)) {
            NotifHy.LOGGER.info("Socket address is not an internet protocol socket, might be a local world: " + socketAddress.toString());
            return;
        }

        if (config.authentication.isEmpty()) {
            NotifHy.LOGGER.info("No authentication token set");
            Chat.send(Text.translatable("chat.notifhy.preconditions.authentication").setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(false)));
            return;
        }

        String hostString = inetSocketAddress.getHostString();

        // Ignore all hosts that are not in the list (modifiable in config)
        if (!config.advanced.hosts.contains(hostString)) {
            NotifHy.LOGGER.info("Host is not in list: " + hostString);
            return;
        }

        event.putData("host", hostString);

        EventSender.send(event);
    }
}
