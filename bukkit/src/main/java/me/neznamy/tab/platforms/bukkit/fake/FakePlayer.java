package me.neznamy.tab.platforms.bukkit.fake;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakePlayer {

    private static final List<FakePlayer> AVAILABLE_PLAYERS = new ArrayList<>();
    private static final List<FakePlayer> LOGGED_IN_PLAYERS = new ArrayList<>();

    /**
     * Updates the players with new names.
     *
     * @param players the players to add.
     */
    public static void reassignPlayers(List<String> players) {
        for (String name : players) {
            boolean logged = false;
            FakePlayer p = new FakePlayer(name);
            for (FakePlayer player : AVAILABLE_PLAYERS) {
                if (name.equalsIgnoreCase(player.getName())) {
                    AVAILABLE_PLAYERS.remove(player);
                    AVAILABLE_PLAYERS.add(p);
                    logged = true;
                }
            }
            for (FakePlayer player : LOGGED_IN_PLAYERS) {
                if (name.equalsIgnoreCase(player.getName())) {
                    LOGGED_IN_PLAYERS.remove(player);
                    LOGGED_IN_PLAYERS.add(p);
                    logged = true;
                }
            }
            if (!logged)
                AVAILABLE_PLAYERS.add(p);
        }
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
            LOGGED_IN_PLAYERS.remove(player);
            AVAILABLE_PLAYERS.add(player);
        }
    }

    public static int getAmountOnline() {
        return LOGGED_IN_PLAYERS.size();
    }

    public static List<FakePlayer> getOnlinePlayers() {
        return LOGGED_IN_PLAYERS;
    }

    @NotNull
    private final String name;
    @NotNull
    private final UUID uuid;

    /**
     * Creates a new fake player with the given name.
     *
     * @param name the name.
     */
    public FakePlayer(@NotNull String name) {
        this.name = name;
        this.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer" + name).getBytes(StandardCharsets.UTF_8));
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

    /**
     * Makes the fake player join the server.
     */
    public void join() {
        for (TabPlayer tabPlayer : TAB.getInstance().getOnlinePlayers())
            this.joinFor(tabPlayer);
    }

    private void joinFor(TabPlayer tabPlayer) {
        TabList.Entry entry = TabList.Entry.displayName(this.uuid, TabComponent.optimized(FakeConfig.DEFAULT_TAG + " " + this.name));
        Map<UUID, TabList.Entry> entries = new ConcurrentHashMap<>();
        BukkitTabPlayer player = (BukkitTabPlayer) tabPlayer;
        TabList.Entry e = new TabList.Entry(player.getUniqueId(),
                player.getName(),
                player.getSkin(),
                player.getPing(),
                player.getGamemode(),
                null);
        entries.put(player.getUniqueId(), e);
        TabList list = player.getTabList();
        list.removeEntries(new ArrayList<>(entries.keySet()));
        list.addEntry(entry);
    }

    /**
     * Makes the fake player leave the server.
     */
    public void quit() {
        for (TabPlayer tabPlayer : TAB.getInstance().getOnlinePlayers()) {
            BukkitTabPlayer player = (BukkitTabPlayer) tabPlayer;
            TabList list = player.getTabList();
            list.removeEntry(this.uuid);
        }
    }

}
