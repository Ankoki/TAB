package me.neznamy.tab.platforms.bukkit.fake;

import lombok.Getter;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
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
     * Joins all the current online fake players for the given player.
     *
     * @param player the player.
     */
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
            LOGGED_IN_PLAYERS.remove(player);
            AVAILABLE_PLAYERS.add(player);
            if (FakeConfig.DEBUG)
                System.out.println(player + " is quitting.");
        }
    }

    /**
     * Gets all the online fake players.
     *
     * @return the online fake players.
     */
    public static List<FakePlayer> getOnlinePlayers() {
        return LOGGED_IN_PLAYERS;
    }

    /**
     * Gets all the available fake players.
     *
     * @return the available fake players.
     */
    public static List<FakePlayer> getAvailablePlayers() {
        return AVAILABLE_PLAYERS;
    }

    @NotNull
    private final String name;
    @NotNull
    private final UUID uuid;
    /**
     * -- GETTER --
     *  Gets the ping of this fake player.
     *
     * @return the ping.
     */
    @Getter
    private final int ping;
    /**
     * -- GETTER --
     *  Gets the gamemode of this fake player.
     *
     * @return the gamemode.
     */
    @Getter
    private final int gamemode = 0;
    private final TabList.Entry entry;

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
        this.entry = new TabList.Entry(this.uuid, this.name, null, true, this.ping, this.gamemode, TabComponent.fromColoredText(FakeConfig.DEFAULT_TAG + " " + this.name), FakeConfig.LIST_WEIGHT, true);
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

    /**
     * Makes the fake player join for the given player.
     *
     * @param tabPlayer the player to join for.
     */
    private void joinFor(TabPlayer tabPlayer) {
        BukkitTabPlayer player = (BukkitTabPlayer) tabPlayer;
        TabList list = player.getTabList();
        Collections.shuffle(PING_OPTIONS);
        if (list instanceof TrackedTabList<?>) {
            TrackedTabList<?> tabList = (TrackedTabList<?>) list;
            tabList.addEntry(this.entry);
            if (FakeConfig.DEBUG)
                System.out.println("FakePlayer[" + this + "].joinFor(TabPlayer[" + tabPlayer.getName() + "])");
        }
    }

    /**
     * Makes the fake player leave the server.
     */
    public void quit() {
        for (TabPlayer tabPlayer : TAB.getInstance().getOnlinePlayers()) {
            BukkitTabPlayer player = (BukkitTabPlayer) tabPlayer;
            TabList list = player.getTabList();
            if (list instanceof TrackedTabList<?>) {
                TrackedTabList<?> tabList = (TrackedTabList<?>) list;
                tabList.removeEntry(this.uuid);
            }
        }
    }

    @Override
    public String toString() {
        return this.name + "[" + this.uuid + "," + this.ping + "," + this.gamemode + "]";
    }

}