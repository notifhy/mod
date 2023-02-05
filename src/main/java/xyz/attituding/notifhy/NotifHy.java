package xyz.attituding.notifhy;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Base64;

@Mod(modid = "notifhy", version = "0.0.1")
public class NotifHy {
    public static final Logger LOGGER = LogManager.getLogger("NotifHy");

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

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

    private static void preconditions(JsonObject json, NetworkManager manager) {
//        NotifHyConfig config = AutoConfig.getConfigHolder(NotifHyConfig.class).getConfig();
//
//        if (config.authentication.length() == 0) {
//            LOGGER.warn("No authentication token set");
//            return;
//        }

        SocketAddress socketAddress = manager.getRemoteAddress();

        // SocketAddress is usually an InetSocketAddress
        if (!(socketAddress instanceof InetSocketAddress)) {
            NotifHy.LOGGER.warn("Socket address is not an internet protocol socket, might be a local world: " + socketAddress.toString());
            return;
        }

        String hostString = ((InetSocketAddress) socketAddress).getHostString();

//        // Ignore all domains that are not in the list (modifiable in config)
//        if (!config.advanced.domains.contains(domain)) {
//            NotifHy.LOGGER.warn("Private domain is not in list: " + domain);
//            return;
//        }

        json.addProperty("domain", hostString);

        ping(json);
    }

    public static void ping(JsonObject json) {
        try {
            // NotifHyConfig config = AutoConfig.getConfigHolder(NotifHyConfig.class).getConfig();

            GameProfile profile = Minecraft.getMinecraft().getSession().getProfile();

            if (profile == null) {
                NotifHy.LOGGER.warn("UUID is null, cannot proceed");
                return;
            }

            String uuid = profile.getId().toString();
            String authorization = "Basic " + Base64.getEncoder().encodeToString((uuid + ":"/* + config.authentication */).getBytes());

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(/*config.advanced.server*/ "https://notifhy-api.attituding.xyz/v1/event");
            request.addHeader("Context-Type", "application/json");
            request.addHeader("Authorization", authorization);
            request.setEntity(new StringEntity(json.toString()));

            HttpResponse response = httpClient.execute(request);

            NotifHy.LOGGER.debug("Sending " + json);

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
