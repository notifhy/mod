package xyz.attituding.notifhy.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.TranslatableText;
import xyz.attituding.notifhy.NotifHy;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class NotifHyModMenuApiImpl implements ModMenuApi {
    private static File file = null;

    public NotifHyModMenuApiImpl() {
        load();
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> {
            ConfigBuilder builder = ConfigBuilder.create().setTitle(new TranslatableText("title.notifhy.config"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory testing = builder.getOrCreateCategory(new TranslatableText(""));

            testing.addEntry(entryBuilder.startTextDescription(new TranslatableText("title.notifhy.config.authentication.description")).build());
            testing.addEntry(entryBuilder.startStrField(new TranslatableText("title.notifhy.config.authentication"), Config.AUTHENTICATION.getValue()).setDefaultValue("").setSaveConsumer((newValue -> Config.AUTHENTICATION.setValue(newValue))).build());
            builder.setSavingRunnable(this::save);

            return builder.setParentScreen(screen).build();
        };
    }

    private void load() {
        prepareConfig();

        try {
            if (!file.exists()) {
                save();
            }

            FileReader fw = new FileReader(file);
            JsonObject config = JsonParser.parseReader(fw).getAsJsonObject();
            fw.close();

            // The most temporary solutions are actually the most permanent
            Config.AUTHENTICATION.setValue(config.getAsJsonPrimitive(Config.AUTHENTICATION.getKey()).getAsString());
        } catch (Exception e) {
            NotifHy.LOGGER.error("Failed to load config file", e);
        }
    }

    private void save() {
        prepareConfig();

        JsonObject config = new JsonObject();
        config.addProperty(Config.AUTHENTICATION.getKey(), Config.AUTHENTICATION.getValue());
        String configString = config.toString();

        try {
            FileWriter fw = new FileWriter(file);
            PrintWriter pw = new PrintWriter(fw);

            pw.print(configString);
            fw.close();
        } catch (Exception e) {
            NotifHy.LOGGER.error("Failed to save config file", e);
        }
    }

    private static void prepareConfig() {
        if (file == null) {
            file = new File(FabricLoader.getInstance().getConfigDir().toFile(), NotifHy.MOD_ID + ".json");
        }
    }
}