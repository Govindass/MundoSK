package com.pie.tlatoani.SkinTexture;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.pie.tlatoani.Mundo;
import com.pie.tlatoani.Tablist.TabListManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Tlatoani on 9/18/16.
 */
public class SkinManager {
    private static HashMap<UUID, SkinTexture> actualSkins = new HashMap<>();
    private static HashMap<UUID, SkinTexture> displayedSkins = new HashMap<>();
    private static HashMap<UUID, String> nameTags = new HashMap<>();

    static {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Mundo.instance, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!event.isCancelled()) {
                    if (event.getPacket().getPlayerInfoAction().read(0) == EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
                        List<PlayerInfoData> playerInfoDatas = event.getPacket().getPlayerInfoDataLists().readSafely(0);
                        int i = 0;
                        for (PlayerInfoData playerInfoData : playerInfoDatas) {
                            if (!actualSkins.containsKey(playerInfoData.getProfile().getUUID()) && playerInfoData.getProfile().getUUID().toString().substring(14, 15).equals("4")) {
                                Mundo.debug(SkinManager.class, "NEW PLAYER !");
                                SkinTexture skinTexture = new SkinTexture.Collected(playerInfoData.getProfile().getProperties().get("textures"));
                                actualSkins.put(playerInfoData.getProfile().getUUID(), skinTexture);
                                displayedSkins.put(playerInfoData.getProfile().getUUID(), skinTexture);
                            }

                            PlayerInfoData newPlayerInfoData = new PlayerInfoData(playerInfoData.getProfile().withName(nameTags.get(playerInfoData.getProfile().getUUID())), playerInfoData.getLatency(), playerInfoData.getGameMode(), playerInfoData.getDisplayName());
                            playerInfoDatas.set(i, newPlayerInfoData);

                            SkinTexture skinTexture = displayedSkins.get(newPlayerInfoData.getProfile().getUUID());
                            Mundo.debug(SkinManager.class, "PLAYER DISPLAY NAME: " + newPlayerInfoData.getProfile().getName());
                            Mundo.debug(SkinManager.class, "PLAYER INFO DATA NAME: " + newPlayerInfoData.getDisplayName());
                            Mundo.debug(SkinManager.class, "PLAYER UUID: " + newPlayerInfoData.getProfile().getUUID());
                            Mundo.debug(SkinManager.class, "SKINTEXTURE FOUND IN PACKET: " + newPlayerInfoData.getProfile().getProperties().get("textures"));
                            Mundo.debug(SkinManager.class, "SKINTEXTURE REPLACEMENT (MAY OR MAY NOT EXIST): " + skinTexture);
                            if (skinTexture != null) {
                                skinTexture.retrieveSkinTextures(newPlayerInfoData.getProfile().getProperties());
                            }

                            i++;
                        }
                    }
                }
            }
        });
    }

    private SkinManager() {}

    public static void onJoin(Player player) {
        //displayedSkins.put(player.getUniqueId(), new SkinTexture(player));
    }

    public static void onQuit(Player player) {
        actualSkins.remove(player.getUniqueId());
        displayedSkins.remove(player.getUniqueId());
    }

    public static SkinTexture getActualSkin(Player player) {
        return actualSkins.get(player.getUniqueId());
    }

    public static SkinTexture getDisplayedSkin(Player player) {
        return displayedSkins.get(player.getUniqueId());
    }

    public static void setDisplayedSkin(Player player, SkinTexture skinTexture) {
        Mundo.debug(SkinManager.class, "SKINTEXTURE: " + skinTexture);
        if (skinTexture != null)
            displayedSkins.put(player.getUniqueId(), skinTexture);
        else
            displayedSkins.remove(player.getUniqueId());
        refreshPlayer(player);
    }

    public static String getNameTag(Player player) {
        return nameTags.get(player.getUniqueId());
    }

    public static void setNameTag(Player player, String nameTag) {
        nameTags.put(player.getUniqueId(), nameTag);
    }

    private static void refreshPlayer(Player player) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            target.hidePlayer(player);
            //target.showPlayer(player);
        }
        Mundo.scheduler.scheduleSyncDelayedTask(Mundo.instance, new Runnable() {
            @Override
            public void run() {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    target.showPlayer(player);
                }
            }
        }, 1);
    }
}