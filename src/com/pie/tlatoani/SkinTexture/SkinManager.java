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

                            SkinTexture skinTexture = displayedSkins.get(playerInfoData.getProfile().getUUID());
                            Mundo.debug(SkinManager.class, "PLAYER DISPLAY NAME: " + playerInfoData.getProfile().getName());
                            Mundo.debug(SkinManager.class, "PLAYER INFO DATA NAME: " + playerInfoData.getProfile().getName());
                            Mundo.debug(SkinManager.class, "PLAYER UUID: " + playerInfoData.getProfile().getUUID());
                            Mundo.debug(SkinManager.class, "SKINTEXTURE FOUND IN PACKET: " + playerInfoData.getProfile().getProperties().get("textures"));
                            Mundo.debug(SkinManager.class, "SKINTEXTURE REPLACEMENT (MAY OR MAY NOT EXIST): " + skinTexture);
                            if (skinTexture != null) {
                                skinTexture.retrieveSkinTextures(playerInfoData.getProfile().getProperties());
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
        Mundo.debug(SkinManager.class, "THIS DEBUG MESSAGE SHOULD CONTAIN THE EXACT SAME SKINTEXTURE THAT WAS JUST PRINTED, OTHERWISE THERE IS SOMETHING VERY WRONG WITH HASHMAPS AND UUIDS: " + displayedSkins.get(player.getUniqueId()));
        PlayerInfoData playerInfoData = new PlayerInfoData(WrappedGameProfile.fromPlayer(player), 5, EnumWrappers.NativeGameMode.fromBukkit(player.getGameMode()), WrappedChatComponent.fromJson(TabListManager.colorStringToJson(player.getDisplayName())));
        PacketContainer removePacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        removePacket.getPlayerInfoDataLists().writeSafely(0, Arrays.asList(playerInfoData));
        removePacket.getPlayerInfoAction().writeSafely(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        PacketContainer addPacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        addPacket.getPlayerInfoDataLists().writeSafely(0, Arrays.asList(playerInfoData));
        addPacket.getPlayerInfoAction().writeSafely(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        for (Player target : Bukkit.getOnlinePlayers()) {
            /*if (TabListManager.tablistContainsPlayers(target)) try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(target, removePacket);
                ProtocolLibrary.getProtocolManager().sendServerPacket(target, addPacket);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }*/
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
