package fhooe.ai.movement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jakob on 01.04.2015.
 */
public enum Direction {
    UNDEFINED(0),
    FORWARD(1),
    BACKWARD (-1);

    private int value;

    Direction(int numVal) {
        this.value = numVal;
    }

    public int getValue() {
        return value;
    }

// region int to Direction
    private static final Map<Integer, Direction> intToTypeMap = new HashMap<Integer, Direction>();
    static {
        for (Direction type : Direction.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    public static Direction fromInt(int i) {
        Direction type = intToTypeMap.get(i);
        if (type == null)
            return Direction.UNDEFINED;
        return type;
    }
    // endregion
}
