package xyz.attituding.notifhy.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
//import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import xyz.attituding.notifhy.NotifHy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotifHyConfigGuiFactory implements IModGuiFactory {
    public static class NotifHyGuiConfig extends GuiConfig {

        public NotifHyGuiConfig(GuiScreen parentScreen) {
            super(parentScreen, getConfigElements(), NotifHy.MOD_ID, false, false, ""/* I18n.format("config.notifhy.title") */);
        }

        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> elements = new ArrayList<>();

            // Make CATEGORY_NONE consist of top-level elements rather than having a category
            Map<String, Property> generalProperties = NotifHy.configManager.getConfig().getCategory(NotifHyConfig.CATEGORY_NONE).getValues();
            for (Property generalProperty : generalProperties.values()) {
                elements.add(new ConfigElement(generalProperty));
            }

            elements.add(new ConfigElement(NotifHy.configManager.getConfig().getCategory(NotifHyConfig.CATEGORY_ADVANCED)));
            return elements;
        }
    }

    @Override
    public void initialize(Minecraft minecraftInstance) {
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return NotifHyGuiConfig.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}