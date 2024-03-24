package me.neznamy.tab.platforms.bukkit.fake;

import me.neznamy.tab.shared.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class FakeListeners implements Listener {

    public static final boolean PROTOCOL_LIB_FOUND = ReflectionUtils.classExists("com.comphenix.protocol.ProtocolManager");

    @EventHandler(priority = EventPriority.LOWEST)
    private void onJoin(PlayerJoinEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED || !PROTOCOL_LIB_FOUND)
            return;
        Player player = event.getPlayer();
        FakePlayer.joinFor(player);
    }

    @EventHandler
    private void onMessage(AsyncPlayerChatEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED || !PROTOCOL_LIB_FOUND)
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

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent event) {
        if (!FakeConfig.FAKE_PLAYERS_ENABLED || !PROTOCOL_LIB_FOUND)
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
