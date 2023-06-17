package xyz.attituding.notifhy.core;

import com.google.gson.Gson;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
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
                NotifHyConfig config = AutoConfig.getConfigHolder(NotifHyConfig.class).getConfig();
                String uuid = MinecraftClient.getInstance().getSession().getUuid();
                normalizeUUID(uuid);

                String authorization = "Basic " + Base64.getEncoder().encodeToString((uuid + ":" + config.authentication).getBytes());
                EventPayloadBuilder payload = new EventPayloadBuilder(event);

                CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();
                httpClient.start();

                HttpPost request = createHttpPost(config.advanced.server, authorization, payload);
                CompletableFuture<HttpResponse> future = new CompletableFuture<>();
                httpClient.execute(request, new ResponseHandler(future));

                HttpResponse response = future.get();
                httpClient.close();

                handleResponse(event, request, response);
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

    private static class ResponseHandler implements org.apache.http.concurrent.FutureCallback<HttpResponse> {
        private final CompletableFuture<HttpResponse> future;

        public ResponseHandler(CompletableFuture<HttpResponse> future) {
            this.future = future;
        }

        @Override
        public void completed(HttpResponse result) {
            future.complete(result);
        }

        @Override
        public void failed(Exception ex) {
            future.completeExceptionally(ex);
        }

        @Override
        public void cancelled() {
            future.cancel(true);
        }
    }
}
