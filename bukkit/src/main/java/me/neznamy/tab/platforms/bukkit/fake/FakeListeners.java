package me.neznamy.tab.platforms.bukkit.fake;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Collections;

public class FakeListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private void onJoin(PlayerJoinEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED)
            return;
        Player bukkitPlayer = event.getPlayer();
        TabPlayer player = TAB.getInstance().getPlayer(bukkitPlayer.getUniqueId());
        if (player == null) {
            TAB.getInstance().addPlayer(new BukkitTabPlayer((BukkitPlatform) TAB.getInstance().getPlatform(), bukkitPlayer));
            player = TAB.getInstance().getPlayer(bukkitPlayer.getUniqueId());
            if (player == null) // We tried :p
                return;
        }
        // bukkitPlayer.getScoreboard().registerNewTeam("zzzfakeplayers");
        FakePlayer.joinFor(event.getPlayer());
        FakePlayer.join((int) Math.floor(BukkitUtils.getOnlinePlayers().length * FakeConfig.INCREASE));
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED)
            return;
        int amount = (int) Math.floor(BukkitUtils.getOnlinePlayers().length * FakeConfig.INCREASE);
        if (amount < FakePlayer.getAmountOnline())
            FakePlayer.quit(FakePlayer.getAmountOnline() - amount);
    }

    @EventHandler
    private void onMessage(AsyncPlayerChatEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED)
            return;
        for (FakePlayer fakePlayer : FakePlayer.getOnlinePlayers()) {
            if (StringUtils.containsIgnoreCase(event.getMessage(), fakePlayer.getName()))
                fakePlayer.quit();
        }
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED)
            return;
        for (FakePlayer fakePlayer : FakePlayer.getOnlinePlayers()) {
            if (StringUtils.containsIgnoreCase(event.getMessage(), fakePlayer.getName()))
                fakePlayer.quit();
        }
    }

}
