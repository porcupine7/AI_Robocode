package fhooe.ai.radar;

import fhooe.ai.TestRobot;
import robocode.ScannedRobotEvent;

/**
 * Created by andy on 11.04.15.
 */
public class GunHeatLockRadar implements Radar {

    private final TestRobot mRobot;

    public GunHeatLockRadar(TestRobot _robot) {
        mRobot = _robot;
    }

    @Override
    public void init() {
        mRobot.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
    }

    @Override
    public void doScan() {
        //not needed
    }

    @Override
    public void onRobotDeath(String robotName) {

    }

    @Override
    public void scannedRobot(ScannedRobotEvent _event) {
        double absoluteBearing = mRobot.getHeadingRadians() + _event.getBearingRadians();
        //TODO
    }
}
