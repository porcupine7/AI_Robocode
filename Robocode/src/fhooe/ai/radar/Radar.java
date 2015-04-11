package fhooe.ai.radar;

import robocode.ScannedRobotEvent;

/**
 * Created by andy on 11.04.15.
 */
public interface Radar {

    void init();

    void doScan();

    void onRobotDeath(String robotName);

    void scannedRobot(ScannedRobotEvent _event);
}
