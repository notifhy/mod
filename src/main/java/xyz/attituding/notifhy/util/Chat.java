package xyz.attituding.notifhy.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import xyz.attituding.notifhy.NotifHy;

public class Chat {
    public static final IChatComponent PREFIX = new ChatComponentText("NotifHy").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD).setBold(true)).appendSibling(new ChatComponentText(" > ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY).setBold(true)));

    public static void send(IChatComponent text) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        if (player != null) {
            player.addChatMessage(PREFIX.createCopy().appendSibling(text));
        } else {
            NotifHy.LOGGER.warn("Player is null for some reason, this isn't supposed to happen");
        }
    }
}
