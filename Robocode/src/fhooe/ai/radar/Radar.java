package fhooe.ai.radar;

import robocode.ScannedRobotEvent;

/**
 * Created by andy on 11.04.15.
 */
public interface Radar {

    void init();

    void doScan();

    void lock(String robotName);

    void unlock();

    boolean isLocked();

    String getLockedEnemy();

    void onRobotDeath(String robotName);

    void scannedRobot(ScannedRobotEvent _event);
}
