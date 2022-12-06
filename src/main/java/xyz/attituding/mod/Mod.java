package xyz.attituding.mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

public class Mod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("mod");

	public static final String SERVER_URL = "https://attituding.live";

	@Override
	public void onInitialize()
	{
		LOGGER.info("Hello Fabric world!");

		// Register event listeners
		ServerPlayConnectionEvents.JOIN.register((handler, packet, server) -> preconditions(false, server));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> preconditions(true, server));
	}

	private static void preconditions(boolean joined, MinecraftServer server) {
		if (!server.isSingleplayer()) {
			ping(joined, server.getServerIp());
		}
	}

	public static void ping(boolean joined, String ip) {
		try {
			// Create a URL object for the specified server URL
			URIBuilder builder = new URIBuilder(SERVER_URL);
			builder.addParameter("uuid", MinecraftClient.getInstance().getSession().getUuid());
			builder.addParameter("ip", ip);
			builder.addParameter("state", joined ? "1" : "0");
			URL url = builder.build().toURL();

			// Open a connection to the URL
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// Set the request method and properties
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(3000);
			connection.setReadTimeout(3000);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("AUTHORIZATION", "my-secret-auth-token");

			LOGGER.info(ip, ip.equals(""), ip.equals(" "), joined);

			// Connect to the URL
			connection.connect();

			// Check the response code and log a message
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				LOGGER.info("Successfully pinged server URL with player join/leave status: " + url);
			} else {
				LOGGER.warn("Failed to ping server URL with player join/leave status: " + url + " (response code: " + responseCode + ")");
			}
		} catch (Exception e) {
			LOGGER.error("An error occurred while pinging server URL with player join/leave status", e);
		}
	}
}
