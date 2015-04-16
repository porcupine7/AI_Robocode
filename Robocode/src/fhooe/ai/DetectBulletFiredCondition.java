package fhooe.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fhooe.ai.data.Enemy;
import fhooe.ai.util.MyUtils;
import robocode.Condition;
import robocode.Rules;

/**
 * Created by Jakob on 31.03.2015.
 */
public class DetectBulletFiredCondition extends Condition {
    private Botzilla mRobot;
    private List<EnemyBulletWave> mDetectedWaves = new ArrayList<>();

    public DetectBulletFiredCondition(Botzilla robot) {
        this.mRobot = robot;
    }

    public DetectBulletFiredCondition(Botzilla robot, int priority) {
        this.mRobot = robot;
        this.priority = priority;
    }

    public List<EnemyBulletWave> getDetectedWaves() {
        return mDetectedWaves;
    }

    public boolean test() {

        Collection<Enemy> enemies = mRobot.getEnemiesCache().getEnemyMap().values();
        mDetectedWaves.clear();

        for (Enemy enemy : enemies) {
            if (enemy.getLastEvent() == null)
                continue;

            double energyChange = enemy.getLastEvent().getEnergy() - enemy.getEnergy();
            double absBearing = enemy.getBearingRadians() + mRobot.getHeadingRadians();

            if (energyChange < 3 && energyChange >= 0.1 && enemy.getLastEvent().getEnergy() > 0 && (mRobot.getTime() - enemy.getLastShootingTime() > 3)) {
                //enemy fired bullet!!
                //System.out.println("enemy fired bullet!!");
                enemy.setLastShootingTime(mRobot.getTime());
                EnemyBulletWave ew = new EnemyBulletWave();
                ew.setFireTime(mRobot.getTime() - 2);
                ew.setBulletVelocity(Rules.getBulletSpeed(energyChange));

                // todo get values from one tick before
                ew.setDirectAngle(absBearing + Math.PI);

                ew.setFireLocation(MyUtils.project(mRobot.getPosition(), absBearing, enemy.getDistance()));
                ew.getGravityPoints().clear();
                ew.getGravityPoints().add(new GravityPoint(ew.getFireLocation(), -1000));

                mDetectedWaves.add(ew);
            }

        }


        return mDetectedWaves.size() > 0;
    }

    public void cleanup() {
        this.mRobot = null;
    }
}



