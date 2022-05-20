package game.utility.other;

import java.util.List;

/**
 * The {@link EntityType} enumerable describes which category of each entity that encapsulates it.
 */
public enum EntityType {
    /**
     * Defines the type of the entity.
     */
    UNDEFINED, PLAYER, MISSILE, ZAPPER, ZAPPERBASE, ZAPPERRAY, SHIELD, TELEPORT;

    /**
     * A list containing each type of entity that can be implemented.
     */
    public static final List<EntityType> ALL_ENTITY_TYPE = List.of(EntityType.PLAYER, EntityType.ZAPPER, EntityType.MISSILE, EntityType.SHIELD, EntityType.TELEPORT);

    /**
     * @return <code>true</code> if the current type is for a generable entity, <code>false</code> if not
     */
    public boolean isGenerableEntity() {
        switch (this) {
            case MISSILE:
            case ZAPPER:
            case ZAPPERBASE:
            case ZAPPERRAY:
            case SHIELD:
            case TELEPORT:
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * @return <code>true</code> if the current type is for a obstacle, <code>false</code> if not
     */
    public boolean isObstacle() {
        switch (this) {
            case MISSILE:
            case ZAPPER:
            case ZAPPERBASE:
            case ZAPPERRAY:
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * @return <code>true</code> if the current type is for a pickup, <code>false</code> if not
     */
    public boolean isPickUp() {
        switch (this) {
            case SHIELD:
            case TELEPORT:
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
