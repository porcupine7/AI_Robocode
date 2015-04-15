package fhooe.ai.gun;

import fhooe.ai.TestRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christian on 15.04.2015.
 * code from tutorial
 */
public class DepriGun implements  Gun {

    private final TestRobot mRobot;
    /**
     * waves for robots gun
     */
    private List<DepriWaveBullet> mWaves = new ArrayList<>();
    //private static int[] stats = new int[31];   // 31 is the number of unique GuessFactors we're using
    // improved
    int[][] stats = new int[13][31]; // onScannedRobot can scan up to 1200px, so there are only 13.
    // Note: this must be odd number so we can get
    // GuessFactor 0 at middle.
    private int direction = 1;
    public DepriGun(TestRobot _robot){
        mRobot=_robot;
    }
    @Override
    public void scannedRobot(ScannedRobotEvent e) {
        // Enemy absolute bearing, you can use your one if you already declare it.
        double absBearing = mRobot.getHeadingRadians() + e.getBearingRadians();

        // find our enemy's location:
        double ex = mRobot.getX() + Math.sin(absBearing) * e.getDistance();
        double ey = mRobot.getY() + Math.cos(absBearing) * e.getDistance();

        // Let's process the waves now:
        for (int i=0; i < mWaves.size(); i++)
        {
            DepriWaveBullet currentWave = (DepriWaveBullet)mWaves.get(i);
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
        DepriWaveBullet newWave = new DepriWaveBullet(mRobot.getX(), mRobot.getY(), absBearing, power,
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
}
