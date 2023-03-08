package me.neznamy.tab.shared;

import com.viaversion.viaversion.api.Via;
import me.neznamy.tab.api.FeatureManager;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.*;
import me.neznamy.tab.shared.features.alignedplayerlist.AlignedPlayerList;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerList;
import me.neznamy.tab.shared.features.layout.LayoutManager;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * An interface with methods that are called in universal code,
 * but require platform-specific API calls
 */
public abstract class Platform {

    public void sendConsoleMessage(String message, boolean translateColors) {
        Object logger = TAB.getInstance().getLogger();
        if (logger instanceof java.util.logging.Logger) {
            ((java.util.logging.Logger) logger).info(translateColors ? EnumChatFormat.color(message) : message);
        } else if (logger instanceof Logger) {
            ((Logger) logger).info(translateColors ? EnumChatFormat.color(message) : message);
        }
    }

    /**
     * Detects permission plugin and returns it's representing object
     *
     * @return  the interface representing the permission hook
     */
    public abstract PermissionPlugin detectPermissionPlugin();

    /**
     * Loads features
     */
    public void loadFeatures() {
        Configs configuration = TAB.getInstance().getConfiguration();
        FeatureManager featureManager = TAB.getInstance().getFeatureManager();

        LayoutManager layout = null;
        NameTag nameTags = null;
        BelowName belowName = null;
        YellowNumber yellowNumber = null;
        RedisSupport redis;
        PlayerList playerList = null;
        Sorting sorting = null;
        PipelineInjector pipelineInjector = null;
        GlobalPlayerList globalPlayerList = null;

        if (configuration.getConfig().getBoolean("bossbar.enabled", false)) {
            if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
                featureManager.registerFeature(TabConstants.Feature.BOSS_BAR, new BossBarManagerImpl());
            } else {
                featureManager.registerFeature(TabConstants.Feature.BOSS_BAR, getLegacyBossBar());
            }
        }

        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9 &&
                configuration.getConfig().getBoolean("fix-pet-names.enabled", false)) {
            featureManager.registerFeature(TabConstants.Feature.PET_FIX, getPetFix());
        }

        if (configuration.getConfig().getBoolean("header-footer.enabled", true))
            featureManager.registerFeature(TabConstants.Feature.HEADER_FOOTER, new HeaderFooter());

        if (configuration.isRemoveGhostPlayers())
            featureManager.registerFeature(TabConstants.Feature.GHOST_PLAYER_FIX, new GhostPlayerFix());

        if (configuration.getConfig().getBoolean("prevent-spectator-effect.enabled", false))
            featureManager.registerFeature(TabConstants.Feature.SPECTATOR_FIX, new SpectatorFix());

        if (configuration.isPipelineInjection()) {
            featureManager.registerFeature(TabConstants.Feature.PIPELINE_INJECTION, pipelineInjector = getPipelineInjector());
        }

        if (configuration.getConfig().getBoolean("scoreboard.enabled", false))
            featureManager.registerFeature(TabConstants.Feature.SCOREBOARD, new ScoreboardManagerImpl(pipelineInjector));

        if (configuration.getConfig().getBoolean("placeholders.register-tab-expansion", false)) {
            TAB.getInstance().getPlaceholderManager().setTabExpansion(getTabExpansion());
        }

        if (configuration.getConfig().getBoolean("per-world-playerlist.enabled", false)) {
            featureManager.registerFeature(TabConstants.Feature.PER_WORLD_PLAYER_LIST, getPerWorldPlayerlist());
        }

        // Must be loaded after: PipelineInjector, Sorting
        if (configuration.getConfig().getBoolean("scoreboard-teams.enabled", true)) {
            featureManager.registerFeature(TabConstants.Feature.SORTING, sorting = new Sorting());
            if (configuration.getConfig().getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)
                    && TAB.getInstance().getServerVersion().getMinorVersion() >= 8) {
                featureManager.registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS, nameTags = getUnlimitedNametags());
            } else {
                featureManager.registerFeature(TabConstants.Feature.NAME_TAGS, nameTags = new NameTag());
            }
        }

        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 8 && configuration.getLayout().getBoolean("enabled", false)) {
            if (sorting == null) {
                //sorting is disabled, but layout needs team names
                featureManager.registerFeature(TabConstants.Feature.SORTING, sorting = new Sorting());
            }
            featureManager.registerFeature(TabConstants.Feature.LAYOUT, layout = new LayoutManager(sorting));
        }

        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 8 && configuration.getConfig().getBoolean("tablist-name-formatting.enabled", true)) {
            if (configuration.getConfig().getBoolean("tablist-name-formatting.align-tabsuffix-on-the-right", false)) {
                featureManager.registerFeature(TabConstants.Feature.PLAYER_LIST, playerList = new AlignedPlayerList(layout));
            } else {
                featureManager.registerFeature(TabConstants.Feature.PLAYER_LIST, playerList = new PlayerList(layout));
            }
        }

        if (configuration.getConfig().getBoolean("global-playerlist.enabled", false) &&
                TAB.getInstance().getServerVersion() == ProtocolVersion.PROXY) {
            featureManager.registerFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST, globalPlayerList = new GlobalPlayerList(playerList));
        }

        if (configuration.getConfig().getBoolean("ping-spoof.enabled", false))
            featureManager.registerFeature(TabConstants.Feature.PING_SPOOF, new PingSpoof(layout));

        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.REDIS_BUNGEE, redis = getRedisSupport(globalPlayerList, playerList, nameTags));

        if (configuration.getConfig().getBoolean("yellow-number-in-tablist.enabled", true))
            featureManager.registerFeature(TabConstants.Feature.YELLOW_NUMBER, yellowNumber = new YellowNumber(redis));

        if (configuration.getConfig().getBoolean("belowname-objective.enabled", true))
            featureManager.registerFeature(TabConstants.Feature.BELOW_NAME, belowName = new BelowName(redis));

        featureManager.registerFeature(TabConstants.Feature.NICK_COMPATIBILITY, new NickCompatibility(nameTags, belowName, yellowNumber, redis));
    }

    public BossBarManagerImpl getLegacyBossBar() {
        return new BossBarManagerImpl();
    }

    /**
     * Returns protocol version of requested player using ViaVersion
     *
     * @param   player
     *          UUID of player to get protocol version of
     * @param   playerName
     *          Name of the player
     * @return  protocol version of the player using ViaVersion
     */
    public int getProtocolVersionVia(UUID player, String playerName, int retryLevel) {
        try {
            if (retryLevel == 10) {
                TAB.getInstance().debug("Failed to get protocol version of " + playerName + " after 10 retries");
                return TAB.getInstance().getServerVersion().getNetworkId();
            }
            int version = Via.getAPI().getPlayerVersion(player);
            if (version == -1) {
                Thread.sleep(5);
                return getProtocolVersionVia(player, playerName, retryLevel + 1);
            }
            TAB.getInstance().debug("ViaVersion returned protocol version " + version + " for " + playerName);
            return version;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } catch (Exception | LinkageError e) {
            TAB.getInstance().getErrorManager().printError(String.format("Failed to get protocol version of %s using ViaVersion v%s",
                    playerName, getPluginVersion(TabConstants.Plugin.VIAVERSION)), e);
            return TAB.getInstance().getServerVersion().getNetworkId();
        }
    }

    /**
     * Creates an instance of {@link me.neznamy.tab.api.placeholder.Placeholder}
     * to handle this unknown placeholder (typically a PAPI placeholder)
     *
     * @param   identifier
     *          placeholder's identifier
     */
    public abstract void registerUnknownPlaceholder(String identifier);

    /**
     * Performs platform-specific plugin manager call and returns the result.
     * If plugin is not installed, returns {@code null}.
     *
     * @param   plugin
     *          Plugin to check version of
     * @return  Version string if plugin is installed, {@code null} if not
     */
    public abstract String getPluginVersion(String plugin);

    /**
     * Creates instance for all online players and adds them to the plugin
     */
    public abstract void loadPlayers();

    /**
     * Registers all placeholders, including universal and platform-specific ones
     */
    public abstract void registerPlaceholders();

    public abstract @Nullable PipelineInjector getPipelineInjector();

    public abstract NameTag getUnlimitedNametags();

    public abstract TabExpansion getTabExpansion();

    public abstract @Nullable TabFeature getPetFix();

    public abstract @Nullable RedisSupport getRedisSupport(GlobalPlayerList global, PlayerList playerList, NameTag nameTags);

    public abstract @Nullable TabFeature getPerWorldPlayerlist();

    public abstract PacketBuilder getPacketBuilder();
}