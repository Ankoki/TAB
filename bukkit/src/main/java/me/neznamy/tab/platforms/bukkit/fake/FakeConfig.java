package me.neznamy.tab.platforms.bukkit.fake;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.neznamy.tab.platforms.bukkit.BukkitTAB;
import org.bukkit.configuration.file.YamlConfiguration;

public class FakeConfig {

    private BukkitTAB tab;
    private File configFile;

    public static boolean FAKE_PLAYERS_ENABLED;
    public static String DEFAULT_TAG;
    public static double INCREASE;

    public FakeConfig(BukkitTAB tab) {
        this.tab = tab;
        load();
    }

    public void load() {
        if (this.configFile == null)
            this.configFile = new File(tab.getDataFolder() + File.separator + "fake-config.yml");
        if (!this.configFile.exists())
            tab.saveResource("fake-config.yml", true);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(this.configFile);
        FAKE_PLAYERS_ENABLED = config.getBoolean("enabled");
        DEFAULT_TAG = config.getString("default-tag");
        INCREASE = config.getDouble("increase");
        FakePlayer.reassignPlayers(validate(config.getStringList("fake-player-names")));
    }

    public List<String> validate(List<String> original) {
        List<String> valid = new ArrayList<>();
        for (String name : original) {
            if (name.matches("^[a-zA-Z0-9_]{3,16}$"))
                valid.add(name);
        }
        return valid;
    }

}