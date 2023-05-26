package xyz.attituding.notifhy.core.events;

import net.minecraft.network.NetworkManager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.attituding.notifhy.NotifHy;
import xyz.attituding.notifhy.config.NotifHyConfig;
import xyz.attituding.notifhy.core.Event;
import xyz.attituding.notifhy.core.EventSender;
import xyz.attituding.notifhy.util.Chat;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

public class NetworkEventHandler {
    // Used for detecting duplicate events, which can occur when transferring between servers
    private boolean connected = false;

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onPlayerLoggedIn(net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Event json = new Event(Event.Type.SERVER_CONNECTED.getType());
        preconditions(json, event.manager);
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onPlayerLoggedOut(net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Event json = new Event(Event.Type.SERVER_DISCONNECTED.getType());
        preconditions(json, event.manager);
    }

    private void preconditions(Event event, NetworkManager manager) {
        if ((event.type == Event.Type.SERVER_CONNECTED.getType() && connected) || (event.type == Event.Type.SERVER_DISCONNECTED.getType() && !connected)) {
            NotifHy.LOGGER.info("Skipped over duplicate connect event, value is " + connected);
            return;
        }

        connected = !connected;

        SocketAddress socketAddress = manager.getRemoteAddress();

        // Verify SocketAddress is InetSocketAddress
        if (!(socketAddress instanceof InetSocketAddress)) {
            NotifHy.LOGGER.info("Socket address is not an internet protocol socket, might be a local world: " + socketAddress.toString());
            return;
        }

        if (NotifHyConfig.authentication.isEmpty()) {
            NotifHy.LOGGER.info("No authentication token set");
            Chat.send(new ChatComponentTranslation("chat.notifhy.preconditions.authentication").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED).setBold(false)));
            return;
        }

        String hostString = ((InetSocketAddress) socketAddress).getHostString();

        // Ignore all hosts that are not in the list (modifiable in config)
        if (!Arrays.asList(NotifHyConfig.Advanced.hosts).contains(hostString)) {
            NotifHy.LOGGER.info("Host is not in list: " + hostString);
            return;
        }

        event.putData("host", hostString);

        EventSender.send(event);
    }
}
