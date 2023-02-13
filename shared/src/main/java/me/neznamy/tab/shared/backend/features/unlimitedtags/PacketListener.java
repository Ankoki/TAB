package me.neznamy.tab.shared.backend.features.unlimitedtags;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The packet listening part for securing proper functionality of armor stands.
 * Events are too unreliable and delayed/ahead which causes de-sync
 * if trying to listen to move event.
 * For entering/leaving tracking range there are no events and
 * periodic / move-triggered distance checks would cause high CPU usage.
 */
public class PacketListener extends TabFeature {

    /** Reference to the main feature */
    protected final BackendNameTagX nameTagX;

    /** A player map by entity id, used for better performance */
    private final Map<Integer, TabPlayer> entityIdMap = new ConcurrentHashMap<>();

    /**
     * Constructs new instance with given parameter
     *
     * @param   nameTagX
     *          main feature
     */
    public PacketListener(BackendNameTagX nameTagX) {
        super(nameTagX.getFeatureName(), null);
        this.nameTagX = nameTagX;
    }

    @Override
    public void load() {
        for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
            entityIdMap.put(nameTagX.getEntityId(all), all);
        }
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        entityIdMap.put(nameTagX.getEntityId(connectedPlayer), connectedPlayer);
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        entityIdMap.remove(nameTagX.getEntityId(disconnectedPlayer));
    }

    /**
     * Processes named entity spawn packet and spawns armor stands if
     * entity ID belongs to an online player.
     *
     * @param   receiver
     *          packet receiver
     * @param   entityId
     *          spawned entity
     */
    public void onEntitySpawn(TabPlayer receiver, int entityId) {
        TabPlayer spawnedPlayer = entityIdMap.get(entityId);
        if (spawnedPlayer != null && spawnedPlayer.isLoaded() && !nameTagX.isPlayerDisabled(spawnedPlayer)) {
            TabAPI.getInstance().getThreadManager().runMeasuredTask(nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_SPAWN,
                    () -> nameTagX.getArmorStandManager(spawnedPlayer).spawn(receiver));
        }
    }

    /**
     * Processes entity move packet. If entity ID belongs to a player,
     * armor stands of that player are teleported to player who received the packet.
     * If it belongs to a vehicle carrying a player, that player's armor stands are
     * teleported as well.
     *
     * @param   receiver
     *          packet receiver
     * @param   entityId
     *          entity that moved
     */
    public void onEntityMove(TabPlayer receiver, int entityId) {
        TabPlayer pl = entityIdMap.get(entityId);
        if (pl != null) {
            //player moved
            if (nameTagX.isPlayerDisabled(pl) || !pl.isLoaded()) return;
            TabAPI.getInstance().getThreadManager().runMeasuredTask(nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE,
                    () -> nameTagX.getArmorStandManager(pl).teleport(receiver));
        } else {
            //a vehicle carrying something moved
            for (Integer entity : nameTagX.getVehicleManager().getVehicles().getOrDefault(entityId, Collections.emptyList())) {
                TabPlayer passenger = entityIdMap.get(entity);
                if (passenger != null && nameTagX.getArmorStandManager(passenger) != null) {
                    TabAPI.getInstance().getThreadManager().runMeasuredTask(nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE_PASSENGER,
                            () -> nameTagX.getArmorStandManager(passenger).teleport(receiver));
                }
            }
        }
    }

    /**
     * Processes entity destroy packet and destroys armor stands if
     * entity ID belongs to an online player.
     *
     * @param   receiver
     *          packet receiver
     * @param   entities
     *          de-spawned entities
     */
    public void onEntityDestroy(TabPlayer receiver, List<Integer> entities) {
        for (int entity : entities) {
            onEntityDestroy(receiver, entity);
        }
    }

    /**
     * Processes entity destroy packet and destroys armor stands if
     * entity ID belongs to an online player.
     *
     * @param   receiver
     *          packet receiver
     * @param   entities
     *          de-spawned entities
     */
    public void onEntityDestroy(TabPlayer receiver, int... entities) {
        for (int entity : entities) {
            onEntityDestroy(receiver, entity);
        }
    }

    /**
     * Processes entity destroy packet and destroys armor stands if
     * entity ID belongs to an online player.
     *
     * @param   receiver
     *          packet receiver
     * @param   entity
     *          de-spawned entity
     */
    public void onEntityDestroy(TabPlayer receiver, int entity) {
        TabPlayer deSpawnedPlayer = entityIdMap.get(entity);
        if (deSpawnedPlayer != null && deSpawnedPlayer.isLoaded() && !nameTagX.isPlayerDisabled(deSpawnedPlayer)) {
            BackendArmorStandManager asm = nameTagX.getArmorStandManager(deSpawnedPlayer);
            TabAPI.getInstance().getThreadManager().runMeasuredTask(nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_DESTROY,
                    () -> asm.destroy(receiver));
        }
    }
}