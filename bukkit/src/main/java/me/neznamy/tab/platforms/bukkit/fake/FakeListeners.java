package me.neznamy.tab.platforms.bukkit.fake;

import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FakeListeners implements Listener {

    public static final boolean PROTOCOL_LIB_FOUND = ReflectionUtils.classExists("com.comphenix.protocol.ProtocolManager");

    @EventHandler(priority = EventPriority.LOWEST)
    private void onJoin(PlayerJoinEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED || !PROTOCOL_LIB_FOUND)
            return;
        Player player = event.getPlayer();
        FakePlayer.constructTeam(player);
        FakePlayer.joinFor(player);
        FakePlayer.join((int) Math.floor(BukkitUtils.getOnlinePlayers().length * FakeConfig.INCREASE));
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED || !PROTOCOL_LIB_FOUND)
            return;
        int amount = (int) Math.floor(BukkitUtils.getOnlinePlayers().length * FakeConfig.INCREASE);
        if (amount < FakePlayer.getAmountOnline())
            FakePlayer.quit(FakePlayer.getAmountOnline() - amount);
    }

    @EventHandler
    private void onMessage(AsyncPlayerChatEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED || !PROTOCOL_LIB_FOUND)
            return;
        for (FakePlayer fakePlayer : FakePlayer.getOnlinePlayers()) {
            if (StringUtils.containsIgnoreCase(event.getMessage(), fakePlayer.getName()))
                fakePlayer.quit();
        }
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED || !PROTOCOL_LIB_FOUND)
            return;
        for (FakePlayer fakePlayer : FakePlayer.getOnlinePlayers()) {
            if (StringUtils.containsIgnoreCase(event.getMessage(), fakePlayer.getName()))
                fakePlayer.quit();
        }
    }

}
