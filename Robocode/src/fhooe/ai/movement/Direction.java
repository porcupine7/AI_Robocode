package fhooe.ai.movement;

/**
 * Created by Jakob on 01.04.2015.
 */
public enum Direction {
    UNDEFINED(0),
    FORWARD(1),
    BACKWARD (-1);

    private int numVal;

    Direction(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
