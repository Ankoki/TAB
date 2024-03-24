package me.neznamy.tab.platforms.bukkit.fake;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.neznamy.tab.platforms.bukkit.BukkitTAB;
import org.bukkit.configuration.file.YamlConfiguration;

public class FakeConfig {

    private final BukkitTAB tab;
    private File configFile;
    private YamlConfiguration config;

    public static boolean FAKE_PLAYERS_ENABLED;
    public static boolean DEBUG;
    public static String DEFAULT_TAG;
    public static double INCREASE;

    public FakeConfig(BukkitTAB tab) {
        this.tab = tab;
        this.load();
        // We assign players when the config is loaded due to the amount of fake player names.
        FakePlayer.reassignPlayers(validate(this.config.getStringList("fake-player-names")));
    }

    /**
     * Loads the configuration file. Does not handle fake player names.
     */
    public void load() {
        if (this.configFile == null)
            this.configFile = new File(tab.getDataFolder() + File.separator + "fake-config.yml");
        if (!this.configFile.exists())
            tab.saveResource("fake-config.yml", true);
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        DEBUG = this.config.getBoolean("debug", false);
        FAKE_PLAYERS_ENABLED = this.config.getBoolean("enabled");
        DEFAULT_TAG = this.config.getString("default-tag");
        INCREASE = this.config.getDouble("increase");
    }

    private List<String> validate(List<String> original) {
        List<String> valid = new ArrayList<>();
        for (String name : original) {
            if (name.matches("^[a-zA-Z0-9_]{3,16}$"))
                valid.add(name);
        }
        return valid;
    }

}