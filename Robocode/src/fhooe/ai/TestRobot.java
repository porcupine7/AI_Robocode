package fhooe.ai;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import fhooe.ai.data.EnemiesCache;
import fhooe.ai.data.Enemy;
import fhooe.ai.movement.AntiGravityMovement;
import fhooe.ai.movement.CombinedMovement;
import fhooe.ai.movement.SurferMovement;
import fhooe.ai.radar.OldestScannedRadar;
import fhooe.ai.radar.Radar;
import robocode.*;
import robocode.util.Utils;

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
    private Radar mRadar;
    private CombinedMovement mCombinedMovement;

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
        /* Set some crazy colors! */
        setBodyColor(Color.black);
        setGunColor(Color.BLUE);
        setRadarColor(Color.magenta);
        setBulletColor(Color.CYAN);
        setScanColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));

        addCustomEvent(new RadarTurnCompleteCondition(this));
        addCustomEvent(new DetectBulletFiredCondition(this));

        //turn robot radar and gun independently
        setAdjustRadarForGunTurn(true);
//        setAdjustGunForRobotTurn(true);
        setAdjustRadarForRobotTurn(true);

        //initialize movement strategies
        mAntiGravityMovement = new AntiGravityMovement(this);
        mSurferMovement = new SurferMovement(this);
        mCombinedMovement = new CombinedMovement(this, mAntiGravityMovement,mSurferMovement);

        //init radar
        mRadar = new OldestScannedRadar(this);
        mRadar.init();
        System.out.println("Start2");


        // Robot main loop
        while (true) {
            mRadar.doScan();

            mAntiGravityMovement.doAntiGravity();
            mSurferMovement.doSurfing();

            //actual movement
            mCombinedMovement.doMove();
            execute();

        }
    }

    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    public void onHitByBullet(HitByBulletEvent e) {
        // If the mEnemyWaves collection is empty, we must have missed the
        // detection of this wave somehow.
        if (!mBulletWaves.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(
                    e.getBullet().getX(), e.getBullet().getY());
            EnemyBulletWave hitWave = null;

            // look through the EnemyWaves, and find one that could've hit us.
            for (EnemyBulletWave ew : mBulletWaves) {
                if (Math.abs(ew.getDistanceTraveled(getTime()) -
                        getPosition().distance(ew.getFireLocation())) < 50
                        && Math.abs(Rules.getBulletSpeed(e.getBullet().getPower())
                        - ew.getBulletVelocity()) < 0.001) {
                    hitWave = ew;
                    break;
                }
            }

            if (hitWave != null) {
                mSurferMovement.logHit(hitWave, hitBulletLocation);

                // We can remove this wave now, of course.
                mBulletWaves.remove(mBulletWaves.lastIndexOf(hitWave));
            }
        }
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
        mEnemiesCache.addEvent(e);
        mRadar.scannedRobot(e);

        double absBearing=e.getBearingRadians()+getHeadingRadians();
        setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()));
//        setFire(2);
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        super.onRobotDeath(event);
        mEnemiesCache.removeEnemy(event.getName());
        mRadar.onRobotDeath(event.getName());
    }

    @Override
    public void onCustomEvent(CustomEvent event) {
        super.onCustomEvent(event);
        if (event.getCondition() instanceof RadarTurnCompleteCondition) {
//            System.out.println("Completed turning!");
        } else if (event.getCondition() instanceof DetectBulletFiredCondition) {
            DetectBulletFiredCondition firedCondition = (DetectBulletFiredCondition) event.getCondition();
            mBulletWaves.addAll(firedCondition.getDetectedWaves());
//            System.out.println("Added waves " + firedCondition.getDetectedWaves().size());
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
        mCombinedMovement.draw(g);
    }
}

