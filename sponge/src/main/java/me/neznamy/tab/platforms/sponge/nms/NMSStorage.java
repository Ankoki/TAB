package me.neznamy.tab.platforms.sponge.nms;

import lombok.Getter;
import net.minecraft.network.protocol.game.*;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class NMSStorage {

    @Getter private static final NMSStorage instance = new NMSStorage();

    public final Field ClientboundPlayerInfoPacket_action = getFields(ClientboundPlayerInfoPacket.class, ClientboundPlayerInfoPacket.Action.class).get(0);
    public final Field ClientboundPlayerInfoPacket_entries = getFields(ClientboundPlayerInfoPacket.class, List.class).get(0);

    public final Field ClientboundSetObjectivePacket_action = getFields(ClientboundSetObjectivePacket.class, int.class).get(0);
    public final Field ClientboundSetObjectivePacket_objectivename = getFields(ClientboundSetObjectivePacket.class, String.class).get(0);

    public final Field ClientboundSetDisplayObjectivePacket_position = getFields(ClientboundSetDisplayObjectivePacket.class, int.class).get(0);
    public final Field ClientboundSetDisplayObjectivePacket_objectivename = getFields(ClientboundSetDisplayObjectivePacket.class, String.class).get(0);

    public final Field ClientboundMoveEntityPacket_ENTITYID = getFields(ClientboundMoveEntityPacket.class, int.class).get(0);

    public final Field ClientboundAddPlayerPacket_ENTITYID = getFields(ClientboundAddPlayerPacket.class, int.class).get(0);

    public final Field ClientboundRemoveEntitiesPacket_ENTITIES = setAccessible(ClientboundRemoveEntitiesPacket.class.getDeclaredFields()[0]);

    public final Field ServerboundInteractPacket_ENTITYID = getFields(ServerboundInteractPacket.class, int.class).get(0);

    public final Field ClientboundTeleportEntityPacket_ENTITYID = getFields(ClientboundTeleportEntityPacket.class, int.class).get(0);
    public final Field ClientboundTeleportEntityPacket_X = getFields(ClientboundTeleportEntityPacket.class, double.class).get(0);
    public final Field ClientboundTeleportEntityPacket_Y = getFields(ClientboundTeleportEntityPacket.class, double.class).get(1);
    public final Field ClientboundTeleportEntityPacket_Z = getFields(ClientboundTeleportEntityPacket.class, double.class).get(2);
    public final Field ClientboundTeleportEntityPacket_YAW = getFields(ClientboundTeleportEntityPacket.class, byte.class).get(0);
    public final Field ClientboundTeleportEntityPacket_PITCH = getFields(ClientboundTeleportEntityPacket.class, byte.class).get(1);

    public final Field ClientboundAddEntityPacket_ENTITYID = getFields(ClientboundAddEntityPacket.class, int.class).get(0);
    public final Field ClientboundAddEntityPacket_ENTITYTYPE = getFields(ClientboundAddEntityPacket.class, int.class).get(1);
    public final Field ClientboundAddEntityPacket_YAW = getFields(ClientboundAddEntityPacket.class, byte.class).get(0);
    public final Field ClientboundAddEntityPacket_PITCH = getFields(ClientboundAddEntityPacket.class, byte.class).get(0);
    public final Field ClientboundAddEntityPacket_UUID = getFields(ClientboundAddEntityPacket.class, UUID.class).get(0);
    public final Field ClientboundAddEntityPacket_X = getFields(ClientboundAddEntityPacket.class, double.class).get(0);
    public final Field ClientboundAddEntityPacket_Y = getFields(ClientboundAddEntityPacket.class, double.class).get(1);
    public final Field ClientboundAddEntityPacket_Z = getFields(ClientboundAddEntityPacket.class, double.class).get(2);

    public final Field ClientboundSetPlayerTeamPacket_NAME = getFields(ClientboundSetPlayerTeamPacket.class, String.class).get(0);
    public final Field ClientboundSetPlayerTeamPacket_ACTION = getInstanceFields(ClientboundSetPlayerTeamPacket.class, int.class).get(0);
    public final Field ClientboundSetPlayerTeamPacket_PLAYERS = getFields(ClientboundSetPlayerTeamPacket.class, Collection.class).get(0);

    public final Field ClientboundSetEntityDataPacket_data = getFields(ClientboundSetEntityDataPacket.class, List.class).get(0);

    /**
     * Returns all fields of class with defined class type
     *
     * @param   clazz
     *          class to check fields of
     * @param   type
     *          field type to check for
     * @return  list of all fields with specified class type
     */
    private List<Field> getFields(Class<?> clazz, Class<?> type) {
        List<Field> list = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == type) {
                list.add(setAccessible(field));
            }
        }
        return list;
    }

    /**
     * Makes object accessible and returns it for chaining.
     *
     * @param   o
     *          Object to make accessible
     * @return  Entered object, for chaining
     */
    public <T extends AccessibleObject> T setAccessible(T o) {
        o.setAccessible(true);
        return o;
    }

    /**
     * Returns all instance fields of class with defined class type
     *
     * @param   clazz
     *          class to check fields of
     * @param   type
     *          field type to check for
     * @return  list of all fields with specified class type
     */
    public List<Field> getInstanceFields(Class<?> clazz, Class<?> type) {
        List<Field> list = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == type && !Modifier.isStatic(field.getModifiers())) {
                list.add(setAccessible(field));
            }
        }
        return list;
    }
}