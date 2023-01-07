package xyz.attituding.notifhy;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Base64;

public class EventHandler {
    @SubscribeEvent
    public void onPlayerLoggedIn(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        // event.manager.getRemoteAddress()
        JsonObject json = new JsonObject();
        json.addProperty("joined", true);
        preconditions(json);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        JsonObject json = new JsonObject();
        json.addProperty("joined", false);
        preconditions(json);
    }

    private static void preconditions(JsonObject json) {
//        NotifHyConfig config = AutoConfig.getConfigHolder(NotifHyConfig.class).getConfig();
//
//        if (config.authentication.length() == 0) {
//            LOGGER.warn("No authentication token set");
//            return;
//        }

        if (MinecraftServer.getServer().isSinglePlayer()) {
            NotifHy.LOGGER.info("Server is local world");
            return;
        }

//        // Ignore all domains that are not in the list (modifiable in config)
//        if (!config.advanced.domains.contains(domain)) {
//            LOGGER.warn("Private domain is not in list: " + domain);
//            return;
//        }

        json.addProperty("domain", MinecraftServer.getServer().getServerHostname());

        ping(json);
    }

    public static void ping(JsonObject json) {
        try {
            // NotifHyConfig config = AutoConfig.getConfigHolder(NotifHyConfig.class).getConfig();

            String uuid = Minecraft.getMinecraft().thePlayer.getUniqueID().toString();
            String authorization = "Basic " + Base64.getEncoder().encodeToString((uuid + ":"/* + config.authentication */).getBytes());

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(/*config.advanced.server*/ "https://notifhy-api.attituding.live/v1/event");
            request.addHeader("Context-Type", "application/json");
            request.addHeader("Authorization", authorization);
            request.setEntity(new StringEntity(json.toString()));

            HttpResponse response = httpClient.execute(request);

            NotifHy.LOGGER.info("Sending " + json);

            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == HttpStatus.SC_OK) {
                NotifHy.LOGGER.debug("Successfully pinged");
            } else {
                NotifHy.LOGGER.warn("Failed to ping with response code " + responseCode);
            }
        } catch (Exception e) {
            NotifHy.LOGGER.error("Failed to ping", e);
        }
    }
}
