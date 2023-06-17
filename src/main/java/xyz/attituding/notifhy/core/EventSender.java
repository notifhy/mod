package xyz.attituding.notifhy.core;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
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

import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class EventSender {
    private static final Gson gson = new Gson();

    public static void send(Event event) {
        CompletableFuture.supplyAsync(() -> {
            try {
                GameProfile profile = Minecraft.getMinecraft().getSession().getProfile();

                if (profile == null) {
                    NotifHy.LOGGER.warn("Profile is null, cannot proceed");
                    return null;
                }

                String uuid = profile.getId().toString();
                normalizeUUID(uuid);

                String authorization = "Basic " + Base64.getEncoder().encodeToString((uuid + ":" + NotifHyConfig.authentication).getBytes());
                EventPayloadBuilder payload = new EventPayloadBuilder(event);

                HttpPost request = createHttpPost(NotifHyConfig.Advanced.server, authorization, payload);

                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpResponse response = httpClient.execute(request);

                handleResponse(event, createHttpPost(NotifHyConfig.Advanced.server, authorization, payload), response);
            } catch (Exception e) {
                NotifHy.LOGGER.error("Failed to ping", e);
            }

            return null;
        });
    }

    private static void normalizeUUID(String uuid) {
        if (uuid.length() == 32) {
            StringBuilder uuidTemp = new StringBuilder(uuid);
            uuidTemp.insert(20, '-');
            uuidTemp.insert(16, '-');
            uuidTemp.insert(12, '-');
            uuidTemp.insert(8, '-');
            uuid = uuidTemp.toString();
        }
    }

    private static HttpPost createHttpPost(String serverUrl, String authorization, EventPayloadBuilder payload) {
        HttpPost request = new HttpPost(serverUrl);
        request.addHeader("Authorization", authorization);
        request.setEntity(new StringEntity(gson.toJson(payload), ContentType.APPLICATION_JSON));
        return request;
    }

    private static void handleResponse(Event event, HttpPost request, HttpResponse response) throws Exception {
        NotifHy.LOGGER.info("Sent payload type " + event.type + ": " + EntityUtils.toString(request.getEntity()));

        int responseCode = response.getStatusLine().getStatusCode();

        if (responseCode >= HttpStatus.SC_OK && responseCode < HttpStatus.SC_MULTIPLE_CHOICES) {
            NotifHy.LOGGER.info("Successfully pinged");
        } else if (response.getEntity() != null) {
            NotifHy.LOGGER.warn("Failed to ping with response code " + responseCode + " and body " + EntityUtils.toString(response.getEntity()));
        } else {
            NotifHy.LOGGER.warn("Failed to ping with response code " + responseCode);
        }
    }
}
