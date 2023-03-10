package me.neznamy.tab.shared.features;

import java.util.NoSuchElementException;
import java.util.function.Function;

import io.netty.channel.ChannelDuplexHandler;
import lombok.Setter;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * Packet intercepting to secure proper functionality of some features:
 * TabList names - anti-override
 * NameTags - anti-override
 * Scoreboard - disabling tab's scoreboard to prevent conflict
 * SpectatorFix - to change game mode to something else than spectator
 * PetFix - to remove owner field from entity data
 * PingSpoof - full feature functionality
 * Unlimited name tags - replacement for bukkit events with much better accuracy and reliability
 */
public abstract class PipelineInjector extends TabFeature {

    //handler to inject before
    private final String injectPosition;

    //preventing spam when packet is sent to everyone
    private String lastTeamOverrideMessage;

    //anti-override rules
    protected boolean antiOverrideTeams;
    @Setter protected boolean byteBufDeserialization;

    /**
     * Constructs new instance with given parameter
     *
     * @param   injectPosition
     *          position to inject handler before
     */
    protected PipelineInjector(String injectPosition) {
        super("Pipeline injection", null);
        this.injectPosition = injectPosition;
    }

    /**
     * Injects custom channel duplex handler to prevent other plugins from overriding this one
     *
     * @param   player
     *          player to inject
     */
    public void inject(TabPlayer player) {
        if (player.getVersion().getMinorVersion() < 8 || player.getChannel() == null) return; //hello A248
        if (!player.getChannel().pipeline().names().contains(injectPosition)) {
            //fake player or waterfall bug
            return;
        }
        uninject(player);
        try {
            player.getChannel().pipeline().addBefore(injectPosition, TabConstants.PIPELINE_HANDLER_NAME, getChannelFunction().apply(player));
        } catch (NoSuchElementException | IllegalArgumentException e) {
            //I don't really know how does this keep happening but whatever
        }
    }

    public void uninject(TabPlayer player) {
        if (player.getVersion().getMinorVersion() < 8 || player.getChannel() == null) return; //hello A248
        try {
            if (player.getChannel().pipeline().names().contains(TabConstants.PIPELINE_HANDLER_NAME))
                player.getChannel().pipeline().remove(TabConstants.PIPELINE_HANDLER_NAME);
        } catch (NoSuchElementException e) {
            //for whatever reason this rarely throws
            //java.util.NoSuchElementException: TAB
        }
    }

    @Override
    public void load() {
        antiOverrideTeams = TAB.getInstance().getConfig().getBoolean("scoreboard-teams.enabled", true) &&
                TAB.getInstance().getConfig().getBoolean("scoreboard-teams.anti-override", true);
        boolean respectOtherScoreboardPlugins = TAB.getInstance().getConfig().getBoolean("scoreboard.enabled", false) &&
                TAB.getInstance().getConfig().getBoolean("scoreboard.respect-other-plugins", true);
        byteBufDeserialization = antiOverrideTeams || respectOtherScoreboardPlugins;
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            inject(p);
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            uninject(p);
        }
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        inject(connectedPlayer);
    }

    protected void logTeamOverride(String team, String player, String expectedTeam) {
        String message = "Something just tried to add player " + player + " into team " + team + " (expected team: " + expectedTeam + ")";
        //not logging the same message for every online player who received the packet
        if (!message.equals(lastTeamOverrideMessage)) {
            lastTeamOverrideMessage = message;
            TAB.getInstance().getErrorManager().printError(message, null, false, TAB.getInstance().getErrorManager().getAntiOverrideLog());
        }
    }

    public abstract Function<TabPlayer, ChannelDuplexHandler> getChannelFunction();
}