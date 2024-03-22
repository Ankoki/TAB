package me.neznamy.tab.platforms.bukkit.fake;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.scoreboard.packet.PacketScoreboard;
import me.neznamy.tab.platforms.bukkit.tablist.PacketTabList1193;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class FakePlayer {

    private static final List<Integer> PING_OPTIONS = new ArrayList<>();

    static {
        PING_OPTIONS.add(599);
        PING_OPTIONS.add(299);
        PING_OPTIONS.add(149);
    }

    private static final List<FakePlayer> AVAILABLE_PLAYERS = new ArrayList<>();
    private static final List<FakePlayer> LOGGED_IN_PLAYERS = new ArrayList<>();
    private static ProtocolManager protocolManager;

    public static void setProtocolManager(ProtocolManager protocolManager) {
        FakePlayer.protocolManager = protocolManager;
    }

    /**
     * Updates the players with new names.
     *
     * @param players the players to add.
     */
    public static void reassignPlayers(List<String> players) {
        for (String name : players) {
            boolean logged = false;
            FakePlayer p = new FakePlayer(name);
            List<FakePlayer> change = new ArrayList<>();
            for (FakePlayer player : AVAILABLE_PLAYERS) {
                if (name.equalsIgnoreCase(player.getName())) {
                    change.add(player);
                    logged = true;
                }
            }
            for (FakePlayer player : change) {
                AVAILABLE_PLAYERS.remove(player);
                AVAILABLE_PLAYERS.add(p);
            }
            change.clear();
            for (FakePlayer player : LOGGED_IN_PLAYERS) {
                if (name.equalsIgnoreCase(player.getName())) {
                    change.add(player);
                    logged = true;
                }
            }
            for (FakePlayer player : change) {
                LOGGED_IN_PLAYERS.remove(player);
                LOGGED_IN_PLAYERS.add(p);
            }
            if (!logged)
                AVAILABLE_PLAYERS.add(p);
        }
    }

    /**
     * Constructs the fake player team for the given player.
     *
     * @param player the player to construct it for.
     */
    public static void constructTeam(Player player) {
        PacketContainer teamPacket = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        teamPacket.getStrings().write(0, "zzzfakeplayers");
        teamPacket.getIntegers().write(0, 0);
        Optional<InternalStructure> optional = teamPacket.getOptionalStructures().read(0);
        if (optional.isPresent()) {
            InternalStructure structure = optional.get();
            structure.getChatComponents().write(0, WrappedChatComponent.fromText(""));
            structure.getIntegers().write(0, 1);
            structure.getStrings().write(0, "always");
            structure.getStrings().write(1, "always");
            structure.getChatComponents().write(1, WrappedChatComponent.fromText(""));
            structure.getChatComponents().write(2, WrappedChatComponent.fromText(""));
            structure.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, ChatColor.WHITE);
            teamPacket.getOptionalStructures().write(0, Optional.of(structure));
        }
        teamPacket.getModifier().write(2, Collections.emptyList());
        // protocolManager.sendServerPacket(player, teamPacket); // Servers connected to a bungee already have the scoreboard team created for some godforsaken reason.
    }

    public static void joinFor(Player player) {
        TabPlayer tabPlayer = TAB.getInstance().getPlayer(player.getUniqueId());
        if (tabPlayer == null) {
            TAB.getInstance().addPlayer(new BukkitTabPlayer((BukkitPlatform) TAB.getInstance().getPlatform(), player));
            tabPlayer = TAB.getInstance().getPlayer(player.getUniqueId());
            if (tabPlayer == null) // Well we tried :p
                return;
        }
        for (FakePlayer fakePlayer : LOGGED_IN_PLAYERS)
            fakePlayer.joinFor(tabPlayer);
    }

    /**
     * Makes fake players join.
     *
     * @param amount the amount that should join.
     */
    public static void join(int amount) {
        if (AVAILABLE_PLAYERS.size() < amount)
            return;
        FakePlayer[] players = new FakePlayer[amount];
        Collections.shuffle(AVAILABLE_PLAYERS);
        for (int i = 0; i < amount; i++)
            players[i] = AVAILABLE_PLAYERS.get(i);
        for (FakePlayer player : players) {
            player.join();
            TAB.getInstance().debug(player + " is logging in.");
        }
        for (FakePlayer player : players) {
            AVAILABLE_PLAYERS.remove(player);
            LOGGED_IN_PLAYERS.add(player);
        }
    }

    /**
     * Make fake players quit.
     *
     * @param amount the amount that should quit.
     */
    public static void quit(int amount) {
        if (LOGGED_IN_PLAYERS.size() < amount)
            amount = LOGGED_IN_PLAYERS.size();
        FakePlayer[] players = new FakePlayer[amount];
        Collections.shuffle(LOGGED_IN_PLAYERS);
        for (int i = 0; i < amount; i++)
            players[i] = LOGGED_IN_PLAYERS.get(i);
        for (FakePlayer player : players) {
            player.quit();
            if (FakeConfig.DEBUG)
                System.out.println(player + " is quitting.");
            LOGGED_IN_PLAYERS.remove(player);
            AVAILABLE_PLAYERS.add(player);
        }
    }

    public static List<FakePlayer> getOnlinePlayers() {
        return LOGGED_IN_PLAYERS;
    }

    @NotNull
    private final String name;
    @NotNull
    private final UUID uuid;
    private final int ping;
    private final int gamemode = 0;

    /**
     * Creates a new fake player with the given name.
     *
     * @param name the name.
     */
    public FakePlayer(@NotNull String name) {
        this.name = name;
        this.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer" + name).getBytes(StandardCharsets.UTF_8));
        Collections.shuffle(PING_OPTIONS);
        this.ping = PING_OPTIONS.get(0);
    }

    /**
     * Gets the name of this fake player.
     *
     * @return the player's name.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets the uuid of this fake player.
     *
     * @return the player's uuid.
     */
    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    public int getPing() {
        return ping;
    }

    public int getGamemode() {
        return gamemode;
    }

    /**
     * Makes the fake player join the server.
     */
    public void join() {
        for (TabPlayer tabPlayer : TAB.getInstance().getOnlinePlayers())
            this.joinFor(tabPlayer);
    }

    private void joinFor(TabPlayer tabPlayer) {
        BukkitTabPlayer player = (BukkitTabPlayer) tabPlayer;
        TabList list = player.getTabList();
        Collections.shuffle(PING_OPTIONS);
        if (list instanceof PacketTabList1193) {
            PacketTabList1193 tab = (PacketTabList1193) list;
            TabList.Entry entry = this.createEntry();
            Object playerInfoDataPacket = tab.createPacket(TabList.Action.ADD_PLAYER, entry);
            PacketScoreboard.packetSender.sendPacket(player.getPlayer(), playerInfoDataPacket);
            PacketContainer teamPacket = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
            teamPacket.getStrings().write(0, "zzzfakeplayers");
            teamPacket.getIntegers().write(0, 3);
            Optional<InternalStructure> optional = teamPacket.getOptionalStructures().read(0);
            if (optional.isPresent()) {
                InternalStructure structure = optional.get();
                structure.getIntegers().write(0, 1);
                teamPacket.getOptionalStructures().write(0, Optional.of(structure));
            }
            teamPacket.getModifier().write(2, Collections.singletonList(this.name));
            protocolManager.sendServerPacket(player.getPlayer(), teamPacket);
            if (FakeConfig.DEBUG)
                System.out.println("FakePlayer[" + this + "].joinFor(TabPlayer[" + tabPlayer.getName() + "])");
        }
    }

    /**
     * Makes the fake player leave the server.
     */
    public void quit() {
        TabList.Entry entry = this.createEntry();
        for (TabPlayer tabPlayer : TAB.getInstance().getOnlinePlayers()) {
            BukkitTabPlayer player = (BukkitTabPlayer) tabPlayer;
            TabList list = player.getTabList();
            if (list instanceof PacketTabList1193) {
                PacketTabList1193 tab = (PacketTabList1193) list;
                Object playerInfoDataPacket = tab.createPacket(TabList.Action.REMOVE_PLAYER, entry);
                PacketScoreboard.packetSender.sendPacket(player.getPlayer(), playerInfoDataPacket);
                if (FakeConfig.DEBUG)
                    System.out.println("FakePlayer[" + this + "].quit()[" + tabPlayer.getName() + "]");
            } else if (FakeConfig.DEBUG)
                System.out.println("FakePlayer[" + this + "].quit()[" + tabPlayer.getName() + "] FALLTHROUGH");
        }
    }

    @NotNull
    public TabList.Entry createEntry() {
        return new TabList.Entry(this.uuid, this.name, null, this.ping, this.gamemode, TabComponent.optimized(FakeConfig.DEFAULT_TAG + " " + this.name));
    }

    @Override
    public String toString() {
        return this.name + "[" + this.uuid + "," + this.ping + "," + this.gamemode + "]";
    }

}
