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

public class EventSender {
    private static final Gson gson = new Gson();

    public static void send(Event event) {
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
            EventPayloadBuilder payload = new EventPayloadBuilder(event);

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(NotifHyConfig.Advanced.server);
            request.addHeader("Authorization", authorization);
            request.setEntity(new StringEntity(gson.toJson(payload), ContentType.APPLICATION_JSON));

            HttpResponse response = httpClient.execute(request);

            NotifHy.LOGGER.info("Sent " + EntityUtils.toString(request.getEntity()));

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
