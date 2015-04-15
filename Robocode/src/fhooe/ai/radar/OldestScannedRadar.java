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
    private String mLockedEnemy;
    private double mScanDir;
    private long mLockTime;

    public OldestScannedRadar(TestRobot _robot) {
        mRobot = _robot;
        mSoughtEnemy = "";
        mLockedEnemy = "";
        mEnemyToBearingMap = new LinkedHashMap<>(5, 2, true);
        mScanDir = 1;
    }

    @Override
    public void init() {
    }

    @Override
    public void doScan() {
        if (!isLocked()) {
            mRobot.setTurnRadarRight(mScanDir * Double.POSITIVE_INFINITY);
            mRobot.scan();
        } else if (mRobot.getRadarTurnRemainingRadians() == 0) {
            // System.out.println("- Locked: RadarTurnRemaingRadians = "+mRobot.getRadarTurnRemainingRadians());
            mRobot.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
    }

    @Override
    public void onRobotDeath(String robotName) {
        mEnemyToBearingMap.remove(robotName);
        mSoughtEnemy = "";
        if (robotName.equals(mLockedEnemy)) {
            unlock();
        }
    }

    @Override
    public void lock(String enemyName) {
        mLockedEnemy = enemyName;
        mLockTime = mRobot.getTime();
    }

    @Override
    public long getLockTime() {
        return mLockTime;
    }


    public void unlock() {
        mLockedEnemy = "";
    }

    @Override
    public void scannedRobot(ScannedRobotEvent _event) {
        if (isLocked()) {
            boolean isLockedEnemy = _event.getName().equals(mLockedEnemy);
            if (isLockedEnemy) {
//                System.out.println("Shootin at " + mLockedEnemy);
                //factor of 2 because Radar arc sweeps through a fixed angle.
                // Exact angle chosen depends on positions of enemy and radar when enemy is first picked up.
                // Angle will be increased if necessary to maintain a lock.
                mScanDir = Utils.normalRelativeAngle(mRobot.getHeadingRadians() + _event.getBearingRadians() - mRobot.getRadarHeadingRadians());
                mRobot.setTurnRadarRightRadians(2.0 * mScanDir);
            }
            return;
        }
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

    public boolean isLocked() {
        return !mLockedEnemy.isEmpty();
    }

    @Override
    public String getLockedEnemy() {
        return mLockedEnemy;
    }

}
