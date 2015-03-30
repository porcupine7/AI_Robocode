package fhooe.ai;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.util.Collection;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * TestRobot - a robot by JJHG
 * A robot for the AI course
 */
public class TestRobot extends AdvancedRobot {
  private final EnemiesCache mEnemiesCache = new EnemiesCache();

  /**
   * run: TestRobot's default behavior
   */
  public void run() {
    // Initialization of the robot
    setColors(Color.blue, Color.white, Color.black); // body,gun,radar
    addCustomEvent(new
        RadarTurnCompleteCondition(this));
    setAdjustRadarForGunTurn(true);
    setTurnRadarRight(360);

    // Robot main loop
    while (true) {
      ahead(100);
      turnGunRight(360);
      back(100);
      turnGunRight(360);
    }
  }

  /**
   * onScannedRobot: What to do when you see another robot
   */
  public void onScannedRobot(ScannedRobotEvent e) {
    fire(1);
    mEnemiesCache.addEvent(e);
  }

  /**
   * onHitByBullet: What to do when you're hit by a bullet
   */
  public void onHitByBullet(HitByBulletEvent e) {
  }

  /**
   * onHitWall: What to do when you hit a wall
   */
  public void onHitWall(HitWallEvent e) {
  }

  @Override
  public void onCustomEvent(CustomEvent event) {
    super.onCustomEvent(event);
    if (event.getCondition() instanceof RadarTurnCompleteCondition) {
      sweep();
    }
  }

  //Either 1 or -1, indicates left or right
  private int mRadarDirection = 1;

  private void sweep() {
    double maxBearing = 0;
    int scannedBots = 0;

    Collection<Enemy> enemies = mEnemiesCache.getEnemyMap().values();
    for (Enemy curEnemy : enemies) {
      if (curEnemy.isUpdated(getTime())) {
        double curBearing = getHeading() + curEnemy.getBearing() - getRadarHeading();
        //Normalize bearing, so that it is between 180 and -180
        double normalizedBearing = Utils.normalRelativeAngle(curBearing);
        if (Math.abs(normalizedBearing) > Math.abs(maxBearing)) {
          maxBearing = normalizedBearing;
        }
        scannedBots++;
      }
    }
    double radarTurn = 180 * mRadarDirection;
    if (scannedBots == getOthers()) {
      double safetyMargin = Math.signum(maxBearing) * 22.5;
      radarTurn = maxBearing + safetyMargin;
    }
    setTurnRadarRight(radarTurn);
    mRadarDirection = (int) Math.signum(radarTurn);

  }
}
