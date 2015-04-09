package fhooe.ai;

import java.util.Map;
import java.util.TreeMap;

import robocode.ScannedRobotEvent;

/**
 * Created by andy on 30.03.15.
 */
public class EnemiesCache {
  private final TreeMap<String, Enemy> mNameEnemyMap= new TreeMap<String, Enemy>();

  public EnemiesCache(TestRobot _robot) {
    mRobot = _robot;
  }

  private final TestRobot mRobot;


  public Map<String, Enemy> getEnemyMap() {
    return mNameEnemyMap;
  }

  public void addEvent(ScannedRobotEvent _event) {
    Enemy enemy = mNameEnemyMap.get(_event.getName());
    if (enemy == null) {
      enemy = new Enemy(_event.getName());
      mNameEnemyMap.put(_event.getName(), enemy);
    }
    enemy.addScanEvent(_event,mRobot);

    mRobot.setMainEnemy(mNameEnemyMap.get(mNameEnemyMap.firstKey()));
  }

  public int nrOfEnemies() {
    return mNameEnemyMap.keySet().size();
  }

  @Override
  public String toString() {
    return mNameEnemyMap.toString();
  }
}
