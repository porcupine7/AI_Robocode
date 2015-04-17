package fhooe.ai.movement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import fhooe.ai.Botzilla;
import fhooe.ai.EnemyBulletWave;
import fhooe.ai.util.MyUtils;
import robocode.util.Utils;

/**
 * Created by Jakob on 01.04.2015.
 */
public class SurferMovement {

    public static final boolean log = false;
    // This is a rectangle that represents an 800x600 battle field,
    // used for a simple, iterative WallSmoothing method (by Kawigi).
    // If you're not familiar with WallSmoothing, the wall stick indicates
    // the amount of space we try to always have on either end of the tank
    // (extending straight out the front or back) before touching a wall.
    public static Rectangle2D.Double mPlayField;
    public static int WALL_STICK = 60;
    public static int WALL_DEAD_ZONE = 60;
    public static int BINS = 47; // SEGMENTS
    public static double mSurfStats[] = new double[BINS];
    private Botzilla mRobot;

    private double mSurfAngle = Double.NaN;
    private Direction mSurfDirection;

    public void setCombinedMovement(CombinedMovement _combinedMovement) {
        mCombinedMovement = _combinedMovement;
    }

    private CombinedMovement mCombinedMovement;

    public SurferMovement(Botzilla _robot) {
        mRobot = _robot;
        mPlayField
                = new Rectangle2D.Double(WALL_DEAD_ZONE, WALL_DEAD_ZONE, mRobot.getBattleFieldWidth() - (WALL_DEAD_ZONE * 2), mRobot.getBattleFieldHeight() - (WALL_DEAD_ZONE * 2));

    }

    public double getSurfAngle() {
        return mSurfAngle;
    }

    public void doSurfing() {


        cleanWaves();
        EnemyBulletWave wave = getClosestSurfableWave();
        if (wave != null) {

            double dangerBackward = checkDanger(wave, Direction.BACKWARD);
            double dangerForward = checkDanger(wave, Direction.FORWARD);

            if (log) {
//                System.out.println("danger backward: " + dangerBackward);
//                System.out.println("danger forward: " + dangerForward);
            }

            double angle = MyUtils.absoluteBearing(wave.getFireLocation(), mRobot.getPosition());

            if (wave.getEvadeDirection() == Direction.UNDEFINED) {
                if (dangerBackward < dangerForward) {
                    wave.setEvadeDirection(Direction.BACKWARD);
                    if (log)
                        System.out.println("Evade moving backwards");
                } else {
                    if (log)
                        System.out.println("Evade moving forwards");
                    wave.setEvadeDirection(Direction.FORWARD);
                    mCombinedMovement.newWave();
                }
            }

                if (wave.getEvadeDirection() == Direction.FORWARD) {
                    // turn 90� from bullet
                    mSurfAngle = wallSmoothing(mRobot.getPosition(), angle + (Math.PI / 2), wave.getEvadeDirection());
                    System.out.println("wave dir:" + wave.getEvadeDirection());
                } else {
                    // turn 90� from bullet
                    mSurfAngle = wallSmoothing(mRobot.getPosition(), angle - (Math.PI / 2), wave.getEvadeDirection());
                    System.out.println("wave dir:" + wave.getEvadeDirection());
                }
                mSurfAngle = MyUtils.normaliseHeading(mSurfAngle);
                mSurfDirection = wave.getEvadeDirection();

//            turnAndMove(mSurfAngle);
            } else {
//no waves present return NaN
                mSurfAngle = Double.NaN;
            }


    }

    public EnemyBulletWave getClosestSurfableWave() {
        double closestDistance = 500000; // I use use some very big number here
        EnemyBulletWave surfWave = null;

        for (EnemyBulletWave ew : mRobot.getBulletWaves()) {
            double distance = mRobot.getPosition().distance(ew.getFireLocation())
                    - ew.getDistanceTraveled(mRobot.getTime());

            if (distance > ew.getBulletVelocity() && distance < closestDistance) {
                surfWave = ew;
                closestDistance = distance;
            }
        }

        return surfWave;
    }

    private void cleanWaves() {

        //remove waves that already passed the robot
        for (int x = 0; x < mRobot.getBulletWaves().size(); x++) {
            EnemyBulletWave ew = mRobot.getBulletWaves().get(x);
            double distanceTraveled = ew.getDistanceTraveled(mRobot.getTime());

            if (distanceTraveled >
                    mRobot.getPosition().distance(ew.getFireLocation()) + 50) {
                mRobot.getBulletWaves().remove(x);
                x--;
            }
        }

    }

    public double checkDanger(EnemyBulletWave surfWave, Direction direction) {
        int index = getFactorIndex(surfWave,
                predictPosition(surfWave, direction));

        return mSurfStats[index];
    }

    public Point2D.Double predictPosition(EnemyBulletWave surfWave, Direction direction) {

        Point2D.Double predictedPosition = (Point2D.Double) (mRobot.getPosition().clone());
        double predictedVelocity = mRobot.getVelocity();
        double predictedHeading = mRobot.getHeadingRadians();
        double maxTurning, moveAngle, moveDir;

        int counter = 0; // number of ticks in the future
        boolean intercepted = false;

        do {    // the rest of these code comments are rozu's
            moveAngle =
                    wallSmoothing(predictedPosition, absoluteBearing(surfWave.getFireLocation(),
                            predictedPosition) + (direction.getValue() * (Math.PI / 2)), direction)
                            - predictedHeading;
            moveDir = 1;

            if (Math.cos(moveAngle) < 0) {
                moveAngle += Math.PI;
                moveDir = -1;
            }

            moveAngle = Utils.normalRelativeAngle(moveAngle);

            // maxTurning is built in like this, you can't turn more then this in one tick
            maxTurning = Math.PI / 720d * (40d - 3d * Math.abs(predictedVelocity));
            predictedHeading = Utils.normalRelativeAngle(predictedHeading
                    + limit(-maxTurning, moveAngle, maxTurning));

            // this one is nice ;). if predictedVelocity and moveDir have
            // different signs you want to breack down
            // otherwise you want to accelerate (look at the factor "2")
            predictedVelocity +=
                    (predictedVelocity * moveDir < 0 ? 2 * moveDir : moveDir);
            predictedVelocity = limit(-8, predictedVelocity, 8);

            // calculate the new predicted position
            predictedPosition = project(predictedPosition, predictedHeading,
                    predictedVelocity);

            counter++;

            if (predictedPosition.distance(surfWave.getFireLocation()) <
                    surfWave.getDistanceTraveled(mRobot.getTime()) + (counter * surfWave.getBulletVelocity())
                            + surfWave.getBulletVelocity()) {
                intercepted = true;
            }
        } while (!intercepted && counter < 500);

        return predictedPosition;
    }

    /**
     * Adjusts the heading of the tank so he wont hit a wall.
     * CREDIT: Iterative WallSmoothing by Kawigi
     *
     * @param botLocation the location fo the robot
     * @param angle       the current heading
     * @param _direction  forward or backward
     * @return return absolute angle to move at after account for WallSmoothing
     */
    public double wallSmoothing(Point2D.Double botLocation, double angle, Direction _direction) {
        while (!mPlayField.contains(MyUtils.project(botLocation, angle, WALL_STICK))) {
            angle += _direction.getValue() * 0.1;
        }
        return angle;
    }

    // CREDIT: from CassiusClay, by PEZ
    //   - returns point length away from sourceLocation, at angle
    // robowiki.net?CassiusClay
    public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
                sourceLocation.y + Math.cos(angle) * length);
    }

    // Given the EnemyWave that the bullet was on, and the point where we
    // were hit, calculate the index into our stat array for that factor.
    public static int getFactorIndex(EnemyBulletWave ew, Point2D.Double targetLocation) {
        double offsetAngle = (absoluteBearing(ew.getFireLocation(), targetLocation)
                - ew.getDirectAngle());
        double factor = Utils.normalRelativeAngle(offsetAngle)
                / maxEscapeAngle(ew.getBulletVelocity()) * ew.getDirectAngle(); // http://old.robowiki.net/robowiki?MaxEscapeAngle

        return (int) limit(0,
                (factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2),
                BINS - 1);
    }

    public static double limit(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }

    // got this from RaikoMicro, by Jamougha, but I think it's used by many authors
    //  - returns the absolute angle (in radians) from source to target points
    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    public static double maxEscapeAngle(double velocity) {
        return Math.asin(8.0 / velocity);
    }

    public void turnAndMove(double absAngle) {

        int pointDir = (Math.abs(absAngle - mRobot.getHeadingRadians()) < Math.PI / 2 ? 1 : -1);
        mRobot.setAhead(1000 * pointDir);
        mRobot.setTurnRightRadians(Utils.normalRelativeAngle(absAngle + (pointDir == -1 ? Math.PI : 0) - mRobot.getHeadingRadians()));
        Direction newDirection = Direction.fromInt(pointDir);
        mRobot.setDirection(newDirection);
        System.out.println(newDirection.toString());

    }

    // Given the EnemyWave that the bullet was on, and the point where we
    // were hit, update our stat array to reflect the danger in that area.
    public void logHit(EnemyBulletWave ew, Point2D.Double targetLocation) {
        int index = getFactorIndex(ew, targetLocation);

        for (int x = 0; x < BINS; x++) {
            // for the spot bin that we were hit on, add 1;
            // for the bins next to it, add 1 / 2;
            // the next one, add 1 / 5; and so on...
            mSurfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
        }
    }


    public void draw(Graphics2D _g) {

        if (log) {
            //draw dead zone
            _g.setColor(new Color(1, 0, 0, 0.5f));
            _g.fillRect(0, 0, (int) mRobot.getBattleFieldWidth(), WALL_DEAD_ZONE);
            _g.fillRect(0, (int) (mRobot.getBattleFieldHeight() - WALL_DEAD_ZONE), (int) mRobot.getBattleFieldWidth(), WALL_DEAD_ZONE);
            _g.fillRect(0, 0, WALL_DEAD_ZONE, (int) mRobot.getBattleFieldHeight());
            _g.fillRect((int) (mRobot.getBattleFieldWidth() - WALL_DEAD_ZONE), 0, WALL_DEAD_ZONE, (int) mRobot.getBattleFieldHeight());
            _g.setColor(Color.GREEN);


//        //draw direction stick

            Point2D.Double stickEnd = MyUtils.project(mRobot.getPosition(), mRobot.getHeadingRadians(), WALL_STICK);
            Point2D.Double stickStart = mRobot.getPosition();
            _g.drawLine((int) stickStart.getX(), (int) stickStart.getY(), (int) stickEnd.getX(), (int) stickEnd.getY());


            Point2D.Double stickEnd1 = mRobot.getPosition();
            Point2D.Double stickStart1 = MyUtils.project(mRobot.getPosition(), mRobot.getHeadingRadians(), -WALL_STICK);
            _g.drawLine((int) stickStart1.getX(), (int) stickStart1.getY(), (int) stickEnd1.getX(), (int) stickEnd1.getY());


        }


    }


    public Direction getSurfDirection() {
        return mSurfDirection;
    }
}
