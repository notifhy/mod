package xyz.attituding.notifhy;

import com.google.common.net.InternetDomainName;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.attituding.notifhy.config.Config;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;

public class NotifHy implements ClientModInitializer {
    public static final String MOD_ID = "notifhy";
    public static final Logger LOGGER = LoggerFactory.getLogger("NotifHy");
    public static final String SERVER_URL = "https://serverless.attituding.workers.dev";

    @Override
    public void onInitializeClient() {
        // Register event listeners
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> preconditions(true, handler));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> preconditions(false, handler));
    }

    private static void preconditions(boolean joined, ClientPlayNetworkHandler handler) {
        SocketAddress socketAddress = handler.getConnection().getAddress();

        // Verify SocketAddress is InetSocketAddress
        // CC BY-SA 3.0 https://stackoverflow.com/a/22691011
        if (!(socketAddress instanceof InetSocketAddress inetSocketAddress)) {
            LOGGER.warn("Socket address not an internet protocol socket: " + socketAddress.toString());
            return;
        }

        String hostString = inetSocketAddress.getHostString();
        String privateDomain = InternetDomainName.from(hostString).topPrivateDomain().toString();

        // Ignore all domains that are not Hypixel (for now, subject to change)
        if (!privateDomain.equals("hypixel.net")) {
            LOGGER.warn("Private domain is not Hypixel: " + privateDomain);
            return;
        }

        ping(joined, privateDomain);
    }

    public static void ping(boolean joined, String domain) {
        try {
            // Create a URL object for the specified server URL
            URIBuilder builder = new URIBuilder(SERVER_URL);
            builder.addParameter("uuid", MinecraftClient.getInstance().getSession().getUuid());
            builder.addParameter("domain", domain);
            builder.addParameter("state", joined ? "1" : "0");
            URL url = builder.build().toURL();

            String authenticationToken = Config.AUTHENTICATION.getValue();

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method and properties
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("AUTHORIZATION", authenticationToken);

            LOGGER.info("Updating joined state of " + domain + " with " + joined + " and authentication token " + authenticationToken);

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
