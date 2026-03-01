package me.neznamy.tab.platforms.bukkit.fake;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class FakeListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    private void onJoin(PlayerJoinEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED)
            return;
        Player player = event.getPlayer();
        FakePlayer.joinFor(player);
    }

    @EventHandler
    private void onMessage(AsyncChatEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED)
            return;
        List<FakePlayer> quit = new ArrayList<>();
        for (FakePlayer fakePlayer : FakePlayer.getOnlinePlayers()) {
            if (StringUtils.containsIgnoreCase(((TextComponent) event.message()).content(), fakePlayer.getName())) {
                fakePlayer.quit();
                quit.add(fakePlayer);
            }
        }
        for (FakePlayer fakePlayer : quit) {
            FakePlayer.getOnlinePlayers().remove(fakePlayer);
            FakePlayer.getAvailablePlayers().add(fakePlayer);
        }
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED)
            return;
        List<FakePlayer> quit = new ArrayList<>();
        for (FakePlayer fakePlayer : FakePlayer.getOnlinePlayers()) {
            if (StringUtils.containsIgnoreCase(event.getMessage(), fakePlayer.getName())) {
                fakePlayer.quit();
                quit.add(fakePlayer);
            }
        }
        for (FakePlayer fakePlayer : quit) {
            FakePlayer.getOnlinePlayers().remove(fakePlayer);
            FakePlayer.getAvailablePlayers().add(fakePlayer);
        }
    }

}