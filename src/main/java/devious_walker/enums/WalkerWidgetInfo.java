package devious_walker.enums;

import lombok.Getter;

@Getter
public enum WalkerWidgetInfo {
    FOSSIL_MUSHROOM_TELEPORT(608, 2),
    FOSSIL_MUSHROOM_HOUSE(608, 4),
    FOSSIL_MUSHROOM_VALLEY(608, 8),
    FOSSIL_MUSHROOM_SWAMP(608, 12),
    FOSSIL_MUSHROOM_MEADOW(608, 16);

    private final int groupId;
    private final int childId;

    WalkerWidgetInfo(int groupId, int childId)
    {
        this.groupId = groupId;
        this.childId = childId;
    }
}
