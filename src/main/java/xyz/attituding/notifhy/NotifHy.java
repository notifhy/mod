package xyz.attituding.notifhy;

import com.google.common.net.InternetDomainName;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.attituding.notifhy.config.NotifHyConfig;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;

public class NotifHy implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("NotifHy");
    public static final String MOD_ID = "notifhy";

    @Override
    public void onInitializeClient() {
        AutoConfig.register(NotifHyConfig.class, GsonConfigSerializer::new);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> preconditions(true, handler));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> preconditions(false, handler));
    }

    private static void preconditions(boolean joined, ClientPlayNetworkHandler handler) {
        NotifHyConfig config = AutoConfig.getConfigHolder(NotifHyConfig.class).getConfig();

        if (config.authentication.equals("")) {
            LOGGER.warn("No authentication token set");
            return;
        }

        SocketAddress socketAddress = handler.getConnection().getAddress();

        // Verify SocketAddress is InetSocketAddress
        // CC BY-SA 3.0 https://stackoverflow.com/a/22691011
        if (!(socketAddress instanceof InetSocketAddress inetSocketAddress)) {
            LOGGER.warn("Socket address not an internet protocol socket: " + socketAddress.toString());
            return;
        }

        String hostString = inetSocketAddress.getHostString();
        String privateDomain = InternetDomainName.from(hostString).topPrivateDomain().toString();

        // Ignore all domains that are not in the list (modifiable in config)
        if (!config.advanced.domains.contains(privateDomain)) {
            LOGGER.warn("Private domain is not in list: " + privateDomain);
            return;
        }

        ping(joined, privateDomain);
    }

    public static void ping(boolean joined, String domain) {
        try {
            NotifHyConfig config = AutoConfig.getConfigHolder(NotifHyConfig.class).getConfig();

            // Create a URL object for the specified server URL
            URIBuilder builder = new URIBuilder(config.advanced.server);
            builder.addParameter("uuid", MinecraftClient.getInstance().getSession().getUuid());
            builder.addParameter("domain", domain);
            builder.addParameter("state", joined ? "1" : "0");
            URL url = builder.build().toURL();

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method and properties
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("AUTHORIZATION", config.authentication);

            LOGGER.debug("Updating joined state of " + domain + " with " + joined + " and authentication token " + config.authentication);

            // Connect to the URL
            connection.connect();

            // Check the response code and log a message
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                LOGGER.info("Successfully pinged: " + url);
            } else {
                LOGGER.warn("Failed to ping: " + url + " (response code: " + responseCode + ")");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to ping with the player join/leave status", e);
        }
    }
}
