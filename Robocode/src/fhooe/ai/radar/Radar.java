package fhooe.ai.radar;

import java.util.Collection;

import fhooe.ai.Enemy;
import fhooe.ai.TestRobot;
import robocode.util.Utils;

/**
 * Created by Jakob on 31.03.2015.
 */
public class Radar {

    private TestRobot mRobot;
    //Either 1 or -1, indicates left or right
    private int mRadarDirection = 1;

    public Radar(TestRobot _robot) {
        mRobot = _robot;
    }

    public void sweep() {
        double maxBearing = 0;
        int scannedBots = 0;

        Collection<Enemy> enemies = mRobot.getEnemiesCache().getEnemyMap().values();
        for (Enemy curEnemy : enemies) {
            if (curEnemy.isUpdated(mRobot.getTime())) {
                double curBearing = mRobot.getHeading() + curEnemy.getBearing() - mRobot.getRadarHeading();
                //Normalize bearing, so that it is between 180 and -180
                double normalizedBearing = Utils.normalRelativeAngle(curBearing);
                if (Math.abs(normalizedBearing) > Math.abs(maxBearing)) {
                    maxBearing = normalizedBearing;
                }
                scannedBots++;
            }
        }
        double radarTurn = 180 * mRadarDirection;
        if (scannedBots == mRobot.getOthers()) {
            double safetyMargin = Math.signum(maxBearing) * 22.5;
            radarTurn = maxBearing + safetyMargin;
        }
        mRobot.setTurnRadarRight(radarTurn);
        mRadarDirection = (int) Math.signum(radarTurn);

    }

}
