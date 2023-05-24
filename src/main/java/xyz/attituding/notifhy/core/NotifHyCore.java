package xyz.attituding.notifhy.core;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import xyz.attituding.notifhy.NotifHy;
import xyz.attituding.notifhy.config.NotifHyConfig;
import xyz.attituding.notifhy.util.Chat;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Base64;

public class NotifHyCore {
    private boolean joinedState = false;

    @SubscribeEvent
    public void onPlayerLoggedIn(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        JsonObject json = new JsonObject();
        json.addProperty("joined", true);
        preconditions(json, event.manager);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        JsonObject json = new JsonObject();
        json.addProperty("joined", false);
        preconditions(json, event.manager);
    }

    private void preconditions(JsonObject json, NetworkManager manager) {
        if (json.has("joined")) {
            boolean newJoinedState = json.get("joined").getAsBoolean();
            if (newJoinedState == joinedState) {
                NotifHy.LOGGER.info("Skipped over duplicate joined event, value is " + joinedState);
                return;
            }

            joinedState = newJoinedState;
        }

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

        json.addProperty("host", hostString);

        ping(json);
    }

    private void ping(JsonObject json) {
        try {
            GameProfile profile = Minecraft.getMinecraft().getSession().getProfile();

            if (profile == null) {
                NotifHy.LOGGER.warn("Profile is null, cannot proceed");
                return;
            }

            String uuid = profile.getId().toString();

            // Normalize uuid as uuid may not have dashes
            if (uuid.length() == 32) {
                StringBuilder uuidTemp = new StringBuilder(uuid);
                uuidTemp.insert(20, '-');
                uuidTemp.insert(16, '-');
                uuidTemp.insert(12, '-');
                uuidTemp.insert(8, '-');
                uuid = uuidTemp.toString();
            }

            String authorization = "Basic " + Base64.getEncoder().encodeToString((uuid + ":" + NotifHyConfig.authentication).getBytes());

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(NotifHyConfig.Advanced.server);
            request.addHeader("Authorization", authorization);
            request.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

            HttpResponse response = httpClient.execute(request);

            NotifHy.LOGGER.debug("Sent " + EntityUtils.toString(request.getEntity()));

            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode >= HttpStatus.SC_OK && responseCode < HttpStatus.SC_MULTIPLE_CHOICES) {
                NotifHy.LOGGER.info("Successfully pinged");
            } else if (response.getEntity() != null) {
                NotifHy.LOGGER.warn("Failed to ping with response code " + responseCode + " and body " + EntityUtils.toString(response.getEntity()));
            } else {
                NotifHy.LOGGER.warn("Failed to ping with response code " + responseCode);
            }
        } catch (Exception e) {
            NotifHy.LOGGER.error("Failed to ping", e);
        }
    }
}
