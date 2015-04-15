package fhooe.ai.gun;

import fhooe.ai.TestRobot;
import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christian on 14.04.2015.
 * GuessFactor Targeting gun with basic segmentation
 */
public class GFTGun implements Gun {
    private static final double BULLET_POWER = 1.5;

    private static double lateralDirection;
    private static double lastEnemyVelocity;

    private final TestRobot mRobot;
    GFTMovement movement;



    public GFTGun(TestRobot _robot){
        mRobot=_robot;
        movement= new GFTMovement(mRobot);
    }

    /**
     *
     * FROM GFTargetingBot
     * @param e
     */
    private void guessFactorTargeting(ScannedRobotEvent e){
        double enemyAbsoluteBearing = mRobot.getHeadingRadians() + e.getBearingRadians();
        double enemyDistance = e.getDistance();
        double enemyVelocity = e.getVelocity();
        if (enemyVelocity != 0) {
            lateralDirection = GFTUtils.sign(enemyVelocity * Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing));
        }
        GFTWave wave = new GFTWave(mRobot);
        wave.gunLocation = new Point2D.Double(mRobot.getX(), mRobot.getY());
        GFTWave.targetLocation = GFTUtils.project(wave.gunLocation, enemyAbsoluteBearing, enemyDistance);
        wave.lateralDirection = lateralDirection;
        wave.bulletPower = BULLET_POWER;
        wave.setSegmentations(enemyDistance, enemyVelocity, lastEnemyVelocity);
        lastEnemyVelocity = enemyVelocity;
        wave.bearing = enemyAbsoluteBearing;
        mRobot.setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing -mRobot.getGunHeadingRadians() + wave.mostVisitedBearingOffset()));
        mRobot.setFire(wave.bulletPower);
        if (mRobot.getEnergy() >= BULLET_POWER) {
            mRobot.addCustomEvent(wave);
        }
        movement.onScannedRobot(e);
        mRobot.setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - mRobot.getRadarHeadingRadians()) * 2);

    }


    @Override
    public void scannedRobot (ScannedRobotEvent e){
        if(e.getName().equals(mRobot.getRadar().getLockedEnemy())) {
            System.out.println("\n-- Gun scanned "+e.getName());
            guessFactorTargeting(e);
        }
    }



}
