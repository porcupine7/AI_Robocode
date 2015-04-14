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
public class GFGun {
    private static final double BULLET_POWER = 1.9;

    private static double lateralDirection;
    private static double lastEnemyVelocity;

    private final AdvancedRobot mRobot;
    /**
     * waves for robots gun
     */
    private List<WaveBullet> mWaves = new ArrayList<>();
    //private static int[] stats = new int[31];   // 31 is the number of unique GuessFactors we're using
    // improved
    int[][] stats = new int[13][31]; // onScannedRobot can scan up to 1200px, so there are only 13.
    // Note: this must be odd number so we can get
    // GuessFactor 0 at middle.
    private int direction = 1;

    public GFGun(TestRobot _robot){
        mRobot=_robot;
    }

    /**
     *
     * FROM GFTargetingBot
     * @param e
     */
    private void test1(ScannedRobotEvent e){
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
        //mRobot.movement.onScannedRobot(e);
        mRobot.setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - mRobot.getRadarHeadingRadians()) * 2);

    }
    private void test2(ScannedRobotEvent e){
        // Enemy absolute bearing, you can use your one if you already declare it.
        double absBearing = mRobot.getHeadingRadians() + e.getBearingRadians();

        // find our enemy's location:
        double ex = mRobot.getX() + Math.sin(absBearing) * e.getDistance();
        double ey = mRobot.getY() + Math.cos(absBearing) * e.getDistance();

        // Let's process the waves now:
        for (int i=0; i < mWaves.size(); i++)
        {
            WaveBullet currentWave = (WaveBullet)mWaves.get(i);
            if (currentWave.checkHit(ex, ey, mRobot.getTime()))
            {
                mWaves.remove(currentWave);
                i--;
            }
        }

        double power = Math.min(3, Math.max(.1,Math.random()*1.5));
        // don't try to figure out the direction they're moving
        // they're not moving, just use the direction we had before
        if (e.getVelocity() != 0)
        {
            if (Math.sin(e.getHeadingRadians()-absBearing)*e.getVelocity() < 0)
                direction = -1;
            else
                direction = 1;
        }
        int[] currentStats = stats[(int)(e.getDistance() / 100)];
        // show something else later
        WaveBullet newWave = new WaveBullet(mRobot.getX(), mRobot.getY(), absBearing, power,
                direction, mRobot.getTime(), currentStats);

        int bestindex = 15;	// initialize it to be in the middle, guessfactor 0.
        for (int i=0; i<31; i++)
            if (currentStats[bestindex] < currentStats[i])
                bestindex = i;

        // this should do the opposite of the math in the WaveBullet:
        double guessfactor = (double)(bestindex - (stats.length - 1) / 2)
                / ((stats.length - 1) / 2);
        double angleOffset = direction * guessfactor * newWave.maxEscapeAngle();
        double gunAdjust = Utils.normalRelativeAngle(
                absBearing - mRobot.getGunHeadingRadians() + angleOffset);
        mRobot.setTurnGunRightRadians(gunAdjust);

        if (mRobot.getGunHeat() == 0 && gunAdjust < Math.atan2(9, e.getDistance()) && mRobot.setFireBullet(power) != null) {
        //if(mRobot.setFireBullet(power)!=null){
            mWaves.add(newWave);
        }
    }

    public void scannedRobot (ScannedRobotEvent e){
        test2(e);
    }


    /*
 * This class is the data we will need to use for our targeting waves.
 */
    public class GunWave{
        double speed;
        Point2D.Double origin;
        int velSeg;
        double absBearing;
        double startTime;
    }


}
