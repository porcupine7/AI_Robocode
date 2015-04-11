package fhooe.ai.radar;

import java.util.LinkedHashMap;
import java.util.Map;

import fhooe.ai.TestRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * Created by andi on 31.03.2015.
 */
public class OldestScannedRadar implements Radar {
    private final TestRobot mRobot;
    private final Map<String, Double> mEnemyToBearingMap;

    private String mSoughtEnemy;
    private double mScanDir;

    public OldestScannedRadar(TestRobot _robot) {
        mRobot = _robot;
        mSoughtEnemy = "";
        mEnemyToBearingMap = new LinkedHashMap<>(5, 2, true);
        mScanDir = 1;
    }

    @Override
    public void init() {
    }

    @Override
    public void doScan() {
        mRobot.setTurnRadarRight(mScanDir * Double.POSITIVE_INFINITY);
        mRobot.scan();
    }

    @Override
    public void onRobotDeath(String robotName) {
        mEnemyToBearingMap.remove(robotName);
        mSoughtEnemy = "";
    }

    @Override
    public void scannedRobot(ScannedRobotEvent _event) {
        String scannedName = _event.getName();
        mEnemyToBearingMap.put(scannedName, mRobot.getHeadingRadians() + _event.getBearingRadians());
        boolean allEnemiesScanned = mEnemyToBearingMap.size() == mRobot.getOthers();
        boolean isAnotherEnemy = mSoughtEnemy.isEmpty() || scannedName.equals(mSoughtEnemy);
        if (allEnemiesScanned && isAnotherEnemy) {
            Double nextEnemyBearing = mEnemyToBearingMap.values().iterator().next();
            mScanDir = Utils.normalRelativeAngle(nextEnemyBearing - mRobot.getRadarHeadingRadians());
            mSoughtEnemy = mEnemyToBearingMap.keySet().iterator().next();
        }
    }
}
