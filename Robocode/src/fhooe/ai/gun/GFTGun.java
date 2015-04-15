package fhooe.ai.gun;

import fhooe.ai.Bozilla;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.geom.Point2D;

/**
 * Created by Christian on 14.04.2015.
 * GuessFactor Targeting gun with basic segmentation
 */
public class GFTGun implements Gun {
    public static final boolean DEBUG = true;
    private static double lateralDirection;
    private static double lastEnemyVelocity;

    private final Bozilla mRobot;



    public GFTGun(Bozilla _robot){
        mRobot=_robot;
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
        wave.bulletPower = estimateFirePower(enemyDistance);
        wave.setSegmentations(enemyDistance, enemyVelocity, lastEnemyVelocity);
        lastEnemyVelocity = enemyVelocity;
        wave.bearing = enemyAbsoluteBearing;
        mRobot.setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing -mRobot.getGunHeadingRadians() + wave.mostVisitedBearingOffset()));
        mRobot.setFire(wave.bulletPower);
        if (mRobot.getEnergy() >= wave.bulletPower) {
            mRobot.addCustomEvent(wave);
        }

        if(DEBUG){
            System.out.println("---- Shot with power: " + wave.bulletPower);

        }

        // radar controlling is handled in RADAR :)
        //mRobot.setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - mRobot.getRadarHeadingRadians()) * 2);

    }


    @Override
    public double estimateFirePower(double _enemyDistance) {
        double firePower = (BULLET_MAX_POWER - (_enemyDistance/MAXIMAL_ENEMY_DISTANCE)*BULLET_MAX_POWER);
        return firePower < BULLET_MIN_POWER ? BULLET_MIN_POWER : firePower;
    }

    @Override
    public void scannedRobot (ScannedRobotEvent e){
        if(e.getName().equals(mRobot.getRadar().getLockedEnemy())) {
            if(DEBUG) {
                System.out.println("-- Gun scanned enemy: " + e.getName());
                System.out.println("---- Distance: "+e.getDistance()+" | Velocity: "+e.getVelocity());
            }
            guessFactorTargeting(e);
        }
    }



}
