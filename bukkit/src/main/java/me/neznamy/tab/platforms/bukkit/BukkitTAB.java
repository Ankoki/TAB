package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.platforms.bukkit.fake.FakeConfig;
import me.neznamy.tab.platforms.bukkit.fake.FakeListeners;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.platform.FoliaPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main class for Bukkit.
 */
public class BukkitTAB extends JavaPlugin {

    @NotNull // Can ignore this warning, this class has onEnable called pretty much immediately after being created.
    private FakeConfig config;

    @Override
    public void onEnable() {
        boolean folia = ReflectionUtils.classExists("io.papermc.paper.threadedregions.RegionizedServer");
        TAB.create(folia ? new FoliaPlatform(this) : new BukkitPlatform(this));
        this.config = new FakeConfig(this);
        this.getServer().getPluginManager().registerEvents(new FakeListeners(), this);
    }

    @Override
    public void onDisable() {
        TAB.getInstance().unload();
    }

}