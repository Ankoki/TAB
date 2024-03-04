package me.neznamy.tab.platforms.bukkit.fake;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FakeListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private void onJoin(PlayerJoinEvent event) {
        FakePlayer.joinFor(event.getPlayer());
        FakePlayer.join((int) Math.floor(BukkitUtils.getOnlinePlayers().length * FakeConfig.INCREASE));
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        int amount = (int) Math.floor(BukkitUtils.getOnlinePlayers().length * FakeConfig.INCREASE);
        if (amount < FakePlayer.getAmountOnline())
            FakePlayer.quit(FakePlayer.getAmountOnline() - amount);
    }

    @EventHandler
    private void onMessage(AsyncPlayerChatEvent event) {
        for (FakePlayer fakePlayer : FakePlayer.getOnlinePlayers()) {
            if (StringUtils.containsIgnoreCase(event.getMessage(), fakePlayer.getName()))
                fakePlayer.quit();
        }
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent event) {
        for (FakePlayer fakePlayer : FakePlayer.getOnlinePlayers()) {
            if (StringUtils.containsIgnoreCase(event.getMessage(), fakePlayer.getName()))
                fakePlayer.quit();
        }
    }

}
