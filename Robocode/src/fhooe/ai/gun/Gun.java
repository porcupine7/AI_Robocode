package fhooe.ai.gun;

import robocode.ScannedRobotEvent;

/**
 * Created by Christian on 15.04.2015.
 */
public interface Gun {
    final double BULLET_MAX_POWER = 3.0;
    final double BULLET_MIN_POWER = 0.5;
    final double MAXIMAL_ENEMY_DISTANCE = 1000;

    double estimateFirePower(double _enemyDistance);
    void scannedRobot(ScannedRobotEvent e);
}
