package xyz.attituding.notifhy.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.attituding.notifhy.NotifHy;

public class Chat {
    public static final MutableText PREFIX = Text.literal("NotifHy").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true)).append(Text.literal(" > ").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(true)));

    public static void send(MutableText text) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player != null) {
            mc.inGameHud.getChatHud().addMessage(PREFIX.copy().append(text));
        } else {
            NotifHy.LOGGER.warn("Player is null for some reason, this isn't supposed to happen");
        }
    }
}
