package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Handler for "/tab reload" subcommand
 */
public class ReloadCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public ReloadCommand() {
        super("reload", TabConstants.Permission.COMMAND_RELOAD);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        TAB.getInstance().unload();
        try {
            Class<?> BukkitTAB = Class.forName("me.neznamy.tab.platforms.bukkit.BukkitTAB");
            Field config = BukkitTAB.getDeclaredField("config");
            config.setAccessible(true);
            Object fakeConfig = config.get(null);
            Class<?> FakeConfig = Class.forName("me.neznamy.tab.platforms.bukkit.fake.FakeConfig");
            Method load = FakeConfig.getMethod("load");
            load.invoke(fakeConfig);
        } catch (ReflectiveOperationException ex) { ex.printStackTrace(); }
        sendMessage(sender, TAB.getInstance().load());
    }
}