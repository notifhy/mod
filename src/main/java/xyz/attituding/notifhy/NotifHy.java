package xyz.attituding.notifhy;

import com.google.common.net.InternetDomainName;
import com.google.gson.JsonObject;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.attituding.notifhy.config.NotifHyConfig;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Base64;

public class NotifHy implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("NotifHy");
    public static final String MOD_ID = "notifhy";

    @Override
    public void onInitializeClient() {
        AutoConfig.register(NotifHyConfig.class, GsonConfigSerializer::new);

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

    private static void preconditions(JsonObject json, ClientPlayNetworkHandler handler) {
        NotifHyConfig config = AutoConfig.getConfigHolder(NotifHyConfig.class).getConfig();

        if (config.authentication.length() == 0) {
            LOGGER.warn("No authentication token set");
            return;
        }

        SocketAddress socketAddress = handler.getConnection().getAddress();

        // Verify SocketAddress is InetSocketAddress
        // CC BY-SA 3.0 https://stackoverflow.com/a/22691011
        if (!(socketAddress instanceof InetSocketAddress inetSocketAddress)) {
            LOGGER.warn("Socket address is not an internet protocol socket, might be a local world: " + socketAddress.toString());
            return;
        }

        String hostString = inetSocketAddress.getHostString();
        String domain = InternetDomainName.from(hostString).topPrivateDomain().toString();

        // Ignore all domains that are not in the list (modifiable in config)
        if (!config.advanced.domains.contains(domain)) {
            LOGGER.warn("Private domain is not in list: " + domain);
            return;
        }

        json.addProperty("domain", domain);

        ping(json);
    }

    public static void ping(JsonObject json) {
        try {
            NotifHyConfig config = AutoConfig.getConfigHolder(NotifHyConfig.class).getConfig();

            String uuid = MinecraftClient.getInstance().getSession().getUuid();
            String authorization = "Basic " + Base64.getEncoder().encodeToString((uuid + ":" + config.authentication).getBytes());

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(config.advanced.server);
            request.addHeader("Context-Type", "application/json");
            request.addHeader("Authorization", authorization);
            request.setEntity(new StringEntity(json.toString()));

            HttpResponse response = httpClient.execute(request);

            LOGGER.debug("Sending " + json);

            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == HttpStatus.SC_OK) {
                LOGGER.debug("Successfully pinged");
            } else {
                LOGGER.warn("Failed to ping with response code " + responseCode);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to ping", e);
        }
    }
}
