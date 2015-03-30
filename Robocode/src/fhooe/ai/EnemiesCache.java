package fhooe.ai;

import robocode.ScannedRobotEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andy on 30.03.15.
 */
public class EnemiesCache {
  private final Map<String, Enemy> mNameEnemyMap;

  public EnemiesCache() {
    mNameEnemyMap = new HashMap<String, Enemy>();
  }

  public Map<String, Enemy> getEnemyMap() {
    return mNameEnemyMap;
  }

  public void addEvent(ScannedRobotEvent _event) {
    Enemy enemy = mNameEnemyMap.get(_event.getName());
    if (enemy == null) {
      enemy = new Enemy(_event.getName());
      mNameEnemyMap.put(_event.getName(), enemy);
    }
    enemy.addScanEvent(_event);
  }

  public int nrOfEnemies() {
    return mNameEnemyMap.keySet().size();
  }

  @Override
  public String toString() {
    return mNameEnemyMap.toString();
  }
}
