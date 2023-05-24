package xyz.attituding.notifhy.core;

import com.google.gson.JsonObject;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
import java.util.Base64;

public class NotifHyCore {
    private boolean joinedState = false;

    public NotifHyCore() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            JsonObject json = new JsonObject();
            json.addProperty("joined", true);
            preconditions(json, handler);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            JsonObject json = new JsonObject();
            json.addProperty("joined", false);
            preconditions(json, handler);
        });
    }

    private void preconditions(JsonObject json, ClientPlayNetworkHandler handler) {
        NotifHyConfig config = AutoConfig.getConfigHolder(NotifHyConfig.class).getConfig();

        if (json.has("joined")) {
            boolean newJoinedState = json.get("joined").getAsBoolean();
            if (newJoinedState == joinedState) {
                NotifHy.LOGGER.info("Skipped over duplicate joined event, value is " + joinedState);
                return;
            }

            joinedState = newJoinedState;
        }

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

        json.addProperty("host", hostString);

        ping(json);
    }

    private void ping(JsonObject json) {
        try {
            NotifHyConfig config = AutoConfig.getConfigHolder(NotifHyConfig.class).getConfig();
            String uuid = MinecraftClient.getInstance().getSession().getUuid();

            // Normalize uuid as uuid may not have dashes
            if (uuid.length() == 32) {
                StringBuilder uuidTemp = new StringBuilder(uuid);
                uuidTemp.insert(20, '-');
                uuidTemp.insert(16, '-');
                uuidTemp.insert(12, '-');
                uuidTemp.insert(8, '-');
                uuid = uuidTemp.toString();
            }

            String authorization = "Basic " + Base64.getEncoder().encodeToString((uuid + ":" + config.authentication).getBytes());

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(config.advanced.server);
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
