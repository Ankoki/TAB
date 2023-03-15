package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.features.PlayerList;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerSlot {

    private final Layout layout;
    @Getter private final UUID uniqueId;
    @Getter private TabPlayer player;
    private String text = "";

    public void setPlayer(TabPlayer newPlayer) {
        if (player == newPlayer) return;
        this.player = newPlayer;
        if (player != null) text = "";
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(uniqueId));
        for (TabPlayer viewer : layout.getViewers()) {
            if (viewer.getVersion().getMinorVersion() < 8 || viewer.isBedrockPlayer()) continue;
            viewer.sendCustomPacket(packet);
            viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, getSlot(viewer)));
        }
    }

    public PlayerInfoData getSlot(TabPlayer p) {
        PlayerInfoData data;
        TabPlayer player = this.player; //avoiding NPE from concurrent access
        if (player != null) {
            PlayerList playerList = layout.getManager().getPlayerList();
            data = new PlayerInfoData(
                    layout.getEntryName(p, uniqueId.getLeastSignificantBits()),
                    uniqueId,
                    player.getSkin(),
                    true,
                    player.getPing(),
                    EnumGamemode.SURVIVAL,
                    playerList == null ? new IChatBaseComponent(player.getName()) : playerList.getTabFormat(player, p),
                    null,
                    null);
        } else {
            data = new PlayerInfoData(
                    layout.getEntryName(p, uniqueId.getLeastSignificantBits()),
                    uniqueId,
                    layout.getManager().getSkinManager().getDefaultSkin(),
                    true,
                    layout.getManager().getEmptySlotPing(),
                    EnumGamemode.SURVIVAL,
                    new IChatBaseComponent(text),
                    null,
                    null);
        }
        return data;
    }

    public void setText(String text) {
        if (this.text.equals(text) && player == null) return;
        this.text = text;
        if (player != null) {
            setPlayer(null);
        } else {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(uniqueId, IChatBaseComponent.optimizedComponent(text)));
            for (TabPlayer all : layout.getViewers()) {
                if (all.getVersion().getMinorVersion() < 8 || all.isBedrockPlayer()) continue;
                all.sendCustomPacket(packet);
            }
        }
    }
}
