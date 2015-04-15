package fhooe.ai.data;

import java.util.Map;
import java.util.TreeMap;

import fhooe.ai.Bozilla;
import robocode.ScannedRobotEvent;

/**
 * Created by andy on 30.03.15.
 */
public class EnemiesCache {
    private final TreeMap<String, Enemy> mNameEnemyMap = new TreeMap<String, Enemy>();

    public EnemiesCache(Bozilla _robot) {
        mRobot = _robot;
    }

    private final Bozilla mRobot;


    public Map<String, Enemy> getEnemyMap() {
        return mNameEnemyMap;
    }

    public void addEvent(ScannedRobotEvent _event) {
        Enemy enemy = mNameEnemyMap.get(_event.getName());
        if (enemy == null) {
            enemy = new Enemy(_event.getName());
            mNameEnemyMap.put(_event.getName(), enemy);
        }
        enemy.addScanEvent(_event, mRobot);

        mRobot.setMainEnemy(mNameEnemyMap.get(mNameEnemyMap.firstKey()));
    }

    public boolean oneRemaining() {
        return nrOfEnemies() == 1;
    }

    public String getNearestEnemy() {
        String nearestEnemy = "";
        double nearestDist = Double.MAX_VALUE;
        for (Enemy curEnemy : mNameEnemyMap.values()) {
            if (curEnemy.getDistance() < nearestDist) {
                nearestEnemy = curEnemy.getName();
                nearestDist = curEnemy.getDistance();
            }
        }
        return nearestEnemy;
    }

    public int nrOfEnemies() {
        return mNameEnemyMap.keySet().size();
    }

    public void removeEnemy(String enemyName) {
        mNameEnemyMap.remove(enemyName);
    }

    @Override
    public String toString() {
        return mNameEnemyMap.toString();
    }
}
