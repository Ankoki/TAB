package me.neznamy.tab.platforms.bukkit;

import com.comphenix.protocol.ProtocolLibrary;
import me.neznamy.tab.platforms.bukkit.fake.FakeConfig;
import me.neznamy.tab.platforms.bukkit.fake.FakeListeners;
import me.neznamy.tab.platforms.bukkit.fake.FakePlayer;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.platform.FoliaPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Main class for Bukkit.
 */
public class BukkitTAB extends JavaPlugin {

    @NotNull // Can ignore this warning, this class has onEnable called pretty much immediately after being created.
    private static FakeConfig config;

    private BukkitRunnable runnable;

    @Override
    public void onEnable() {
        boolean folia = ReflectionUtils.classExists("io.papermc.paper.threadedregions.RegionizedServer");
        TAB.create(folia ? new FoliaPlatform(this) : new BukkitPlatform(this));
        BukkitTAB.config = new FakeConfig(this);
        this.getServer().getPluginManager().registerEvents(new FakeListeners(), this);
        if (FakeListeners.PROTOCOL_LIB_FOUND) {
            FakePlayer.setProtocolManager(ProtocolLibrary.getProtocolManager());
            this.startRunnable();
        }
    }

    @Override
    public void onDisable() {
        TAB.getInstance().unload();
    }

    public void startRunnable() {
        if (runnable != null) {
            try {
                Bukkit.getScheduler().cancelTask(this.runnable.getTaskId());
            } catch (IllegalStateException ignored) {}
        }
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!FakeConfig.FAKE_PLAYERS_ENABLED)
                    return;
                int fakePlayers = (int) Math.floor(Bukkit.getOnlinePlayers().size() * FakeConfig.INCREASE) - FakePlayer.getOnlinePlayers().size();
                if (fakePlayers < 0) {
                    for (int i = fakePlayers; i < 0; i++)
                        FakePlayer.quit(1);
                } else if (fakePlayers > FakePlayer.getOnlinePlayers().size()) {
                    for (int i = 0; i < fakePlayers - Bukkit.getOnlinePlayers().size(); i++)
                        FakePlayer.join(1);
                }
            }
        };
        this.runnable.runTaskTimer(this, 0L, 15 * 20L);
    }

}