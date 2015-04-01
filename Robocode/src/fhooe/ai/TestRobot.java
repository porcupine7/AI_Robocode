package fhooe.ai;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import fhooe.ai.movement.AntiGravityMovement;
import fhooe.ai.movement.SurferMovement;
import fhooe.ai.radar.Radar;
import robocode.AdvancedRobot;
import robocode.CustomEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.RadarTurnCompleteCondition;
import robocode.ScannedRobotEvent;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * TestRobot - a robot by JJHG
 * A robot for the AI course
 */
public class TestRobot extends AdvancedRobot {
    private final EnemiesCache mEnemiesCache = new EnemiesCache(this);


    private AntiGravityMovement mAntiGravityMovement;
    private SurferMovement mSurferMovement;
    private List<EnemyBulletWave> mBulletWaves = new ArrayList<>();
    private Radar mRadar = new Radar(this);

    public Enemy getMainEnemy() {
        return mMainEnemy;
    }

    public void setMainEnemy(Enemy _mainEnemy) {
        mMainEnemy = _mainEnemy;
    }

    private Enemy mMainEnemy;


    public List<EnemyBulletWave> getBulletWaves() {
        return mBulletWaves;
    }

    public EnemiesCache getEnemiesCache() {
        return mEnemiesCache;
    }

    /**
     * run: TestRobot's default behavior
     */
    public void run() {
        // Initialization of the robot
        setColors(Color.blue, Color.white, Color.black); // body,gun,radar
        addCustomEvent(new
                RadarTurnCompleteCondition(this));

        addCustomEvent(new DetectBulletFiredCondition(this));

        //turn robot radar and gun independently
        setAdjustRadarForGunTurn(true);
//        setAdjustGunForRobotTurn(true);
        setAdjustRadarForRobotTurn(true);

        //initialize movement strategies
        mAntiGravityMovement = new AntiGravityMovement(this);
        mSurferMovement = new SurferMovement(this,mAntiGravityMovement);

        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);


        // Robot main loop
        while (true) {
            mAntiGravityMovement.calcGravity();


            if (mBulletWaves.size() > 0) {
                mSurferMovement.doSurfing();
            } else {
                mSurferMovement.doSurfing();

//                mAntiGravityMovement.doGravityMove();
            }
            execute();

        }
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

    /**
     * onScannedRobot: What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {
//        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
        mEnemiesCache.addEvent(e);

    }




    @Override
    public void onCustomEvent(CustomEvent event) {
        super.onCustomEvent(event);
        if (event.getCondition() instanceof RadarTurnCompleteCondition) {
//            mRadar.sweep();
        } else if (event.getCondition() instanceof DetectBulletFiredCondition) {
            DetectBulletFiredCondition firedCondition = (DetectBulletFiredCondition) event.getCondition();
//            mBulletWaves.addAll(firedCondition.getDetectedWaves());
            System.out.println("Added waves "+mBulletWaves.size());
        }
    }

    public Point2D.Double getPosition() {
        return new Point2D.Double(getX(), getY());
    }

    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);
        //draw center
        g.drawOval((int)getBattleFieldWidth()/2-50,(int)getBattleFieldHeight()/2-50,100,100);

        //draw waves
        for (EnemyBulletWave bulletWave : mBulletWaves) {
            int d = (int) bulletWave.getDistanceTraveled(getTime()+1)*2;
            g.drawOval((int) bulletWave.getFireLocation().getX() - (d / 2), (int) bulletWave.getFireLocation().getY() - (d / 2), d, d);

            double rotation = -bulletWave.getDirectAngle();
            g.rotate(rotation, bulletWave.getFireLocation().getX(), bulletWave.getFireLocation().getY());

            g.drawLine((int) bulletWave.getFireLocation().getX(), (int) bulletWave.getFireLocation().getY(), (int) bulletWave.getFireLocation().getX(), (int) bulletWave.getFireLocation().getY()+100);
            g.rotate(-rotation, bulletWave.getFireLocation().getX(), bulletWave.getFireLocation().getY());
        }

        // draw enemy positions
        for (Enemy enemy : mEnemiesCache.getEnemyMap().values()) {
            int d =40;
            g.drawOval((int)enemy.getPosition().getX()-(d/2),(int)enemy.getPosition().getY()-(d/2),d,d);
        }

        mSurferMovement.draw(g);
    }
}

