package xyz.attituding.notifhy.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.*;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class Chat {
    public static final LiteralText PREFIX = (LiteralText) new LiteralText("NotifHy").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true)).append(new LiteralText(" > ").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(false)));

    public static void send(MutableText text, Formatting color) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player != null) {
            mc.inGameHud.addChatMessage(MessageType.SYSTEM, PREFIX.shallowCopy().append(text.setStyle(Style.EMPTY.withColor(color).withBold(false))), mc.player.getUuid());
        }
    }
}
