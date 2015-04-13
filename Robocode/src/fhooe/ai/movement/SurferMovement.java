package fhooe.ai.movement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fhooe.ai.EnemyBulletWave;
import fhooe.ai.GravityPoint;
import fhooe.ai.TestRobot;
import fhooe.ai.util.MyUtils;
import robocode.util.Utils;

/**
 * Created by Jakob on 01.04.2015.
 */
public class SurferMovement {

    // This is a rectangle that represents an 800x600 battle field,
    // used for a simple, iterative WallSmoothing method (by Kawigi).
    // If you're not familiar with WallSmoothing, the wall stick indicates
    // the amount of space we try to always have on either end of the tank
    // (extending straight out the front or back) before touching a wall.
    public static Rectangle2D.Double mPlayField;
    public static int WALL_STICK = 100;
    public static int WALL_DEAD_ZONE = 60;
    private TestRobot mRobot;
    private AntiGravityMovement mGravityMovement;
    private Random mRandom = new Random();
    private Direction mDirection = Direction.UNDEFINED;
    //for debugging only
    private double mSurfDirection = 0;
    private double mActualDirection = 0;

    public static final boolean log=false;

    private int mDirectionChange =0;
    private int myTick =0;
    private boolean mProblemMode = false;
    private double mDirectionOffset = 0;


    public SurferMovement(TestRobot _robot, AntiGravityMovement _antiGravityMovement) {
        mRobot = _robot;
        mGravityMovement = _antiGravityMovement;
        mPlayField
                = new Rectangle2D.Double(WALL_DEAD_ZONE, WALL_DEAD_ZONE, mRobot.getBattleFieldWidth() - WALL_DEAD_ZONE, mRobot.getBattleFieldHeight() - WALL_DEAD_ZONE);
    }


    public void doSurfing() {
    myTick++;
        if(myTick > 20 && !mProblemMode){
            myTick =0;
            //reset direction change count every x ticks
            mDirectionChange = 0;
        }

        //gravity movement

        GravityPoint gravityPoint = mGravityMovement.getGravityCenter();
        double gravityAngle = MyUtils.absoluteBearing(mRobot.getPosition(), gravityPoint.getPosition());
        double gravityForce = gravityPoint.getPower() / 50f;



        //region wave movement

        //remove waves that already passed the game field
        List<EnemyBulletWave> removeList = new ArrayList<>();
        for (EnemyBulletWave bulletWave : mRobot.getBulletWaves()) {
            if (bulletWave.getDistanceTraveled(mRobot.getTime()) > mRobot.getBattleFieldWidth()) {
                removeList.add(bulletWave);
            }
        }
        mRobot.getBulletWaves().removeAll(removeList);


        if (mRobot.getBulletWaves().size() > 0) {
            EnemyBulletWave wave = mRobot.getBulletWaves().get(0);
            double angle = MyUtils.absoluteBearing(wave.getFireLocation(), mRobot.getPosition());


            if (wave.getEvadeDirection() == Direction.UNDEFINED) {
                if (mRandom.nextFloat() > 0.5) {
                    wave.setEvadeDirection(Direction.FORWARD);
                    if(log)
                         System.out.println("Evade moving forwards");

                } else {
                    wave.setEvadeDirection(Direction.BACKWARD);
                    if(log)
                        System.out.println("Evade moving backwards");
                }
            }


            if (wave.getEvadeDirection() == Direction.FORWARD) {
                // turn 90� from bullet
                mSurfDirection = angle - (Math.PI / 2);
            } else {
                // turn 90� from bullet
                mSurfDirection = angle + (Math.PI / 2);
            }


            //accumulate movement
            if (gravityForce > 1) {
                //gravity force very big, ignore wave surfing
                mActualDirection = gravityAngle;
            } else {
                mActualDirection = (mSurfDirection * (1 - gravityForce)) + (gravityAngle * gravityForce);
            }

        } else {
//no waves present, use anti gravity movement
            if(mProblemMode){
                if(myTick > 6){
                    mDirectionOffset = 0;
                    mDirectionChange = 0;
                    myTick = 0;
                    mProblemMode = false;
                    System.out.println("Problem mode off");
                }
            }else {
                if(mDirectionChange > 3){
                    //danger ahead, turn 90 degree
                    System.out.println("Problem detected");
                    if (mRandom.nextFloat() > 0.5) {

                        mDirectionOffset =  (Math.PI / 2);
                    } else {
                        mDirectionOffset = -(Math.PI / 2);
                    }
                    myTick = 0;
                    mProblemMode = true;
                }


                if(gravityForce > 5){
                    //don't mess around with direction if gravity force is very big
                    // this should help to escape the wall and corners
                    mDirectionOffset =0;
                }

                mActualDirection = gravityAngle -mDirectionOffset;
            }

        }


        turnAndMove(mActualDirection);


        if(log){
            System.out.println("gravity influence: " + gravityForce);
            System.out.println("angle gravity: " + gravityAngle);
            System.out.println("angle surfer: " + mSurfDirection);
            System.out.println("dir " + mDirection);
            System.out.println("angle accumulated: " + mActualDirection);

        }

    }


    public void turnAndMove(double goAngle) {


        int pointDir;
        mRobot.setAhead(1000 * (pointDir = (Math.abs(goAngle - mRobot.getHeadingRadians()) < Math.PI / 2 ? 1 : -1)));
        mRobot.setTurnRightRadians(Utils.normalRelativeAngle(goAngle + (pointDir == -1 ? Math.PI : 0) - mRobot.getHeadingRadians()));

        Direction newDirection =  Direction.fromInt(pointDir);

        if (newDirection != mDirection) {
            mDirection = newDirection;
            mDirectionChange ++;
        }

//        mLastDirection = mDirection;
//
//        double angle =
//                Utils.normalRelativeAngle(goAngle - mRobot.getHeadingRadians());
//        if (Math.abs(angle) > (Math.PI / 2)) {
//            if (mDirection == Direction.BACKWARD || (mRobot.getTime() - mLastDirectionChange) > 77) { // prevent robot from changing direction all the time
//                moveBackward(angle);
//            } else {
//                moveForWard(angle);
//            }
//        } else {
//
//            if (mDirection == Direction.FORWARD || (mRobot.getTime() - mLastDirectionChange) > 77) {// prevent robot from changing direction all the time
//                moveForWard(angle);
//            } else {
//                moveBackward(angle);
//            }
//        }
//
//        if (mLastDirection != mDirection) {
//            mLastDirectionChange = mRobot.getTime();
//        }
    }

    private void moveBackward(double angle) {
        if (angle < 0) {
            mRobot.setTurnRightRadians(Math.PI + angle);
        } else {
            mRobot.setTurnLeftRadians(Math.PI - angle);
        }
        mRobot.setBack(77);
        mDirection = Direction.BACKWARD;
    }

    private void moveForWard(double angle) {


        if (angle < 0) {
            mRobot.setTurnLeftRadians(-1 * angle);
        } else {
            mRobot.setTurnRightRadians(angle);
        }
        mRobot.setAhead(77);
        mDirection = Direction.FORWARD;
    }


//    /**
//     * Adjusts the heading of the tank so he wont hit a wall.
//     * CREDIT: Iterative WallSmoothing by Kawigi
//     *
//     * @param botLocation the location fo the robot
//     * @param angle       the current heading
//     * @param _direction  forward or backward
//     * @return return absolute angle to move at after account for WallSmoothing
//     */
//    public double wallSmoothing(Point2D.Double botLocation, double angle, Direction _direction) {
//        while (!mPlayField.contains(MyUtils.project(botLocation, angle, WALL_STICK))) {
//            angle += _direction.getValue() * 0.1;
//        }
//        return angle;
//    }


    public void draw(Graphics2D _g) {

        //draw dead zone
//        _g.setColor(new Color(1, 0, 0, 0.5f));
//        _g.fillRect(0, 0, (int) mRobot.getBattleFieldWidth(), WALL_DEAD_ZONE);
//        _g.fillRect(0, (int) (mRobot.getBattleFieldHeight() - WALL_DEAD_ZONE), (int) mRobot.getBattleFieldWidth(), WALL_DEAD_ZONE);
//        _g.fillRect(0, 0, WALL_DEAD_ZONE, (int) mRobot.getBattleFieldHeight());
//        _g.fillRect((int) (mRobot.getBattleFieldWidth() - WALL_DEAD_ZONE), 0, WALL_DEAD_ZONE, (int) mRobot.getBattleFieldHeight());
//        _g.setColor(Color.GREEN);


//        //draw direction stick
        if (mDirection == Direction.FORWARD) {

            Point2D.Double stickEnd = MyUtils.project(mRobot.getPosition(), mRobot.getHeadingRadians(), WALL_STICK);
            Point2D.Double stickStart = mRobot.getPosition();
            _g.drawLine((int) stickStart.getX(), (int) stickStart.getY(), (int) stickEnd.getX(), (int) stickEnd.getY());

        } else if (mDirection == Direction.BACKWARD) {
            Point2D.Double stickEnd = mRobot.getPosition();
            Point2D.Double stickStart = MyUtils.project(mRobot.getPosition(), mRobot.getHeadingRadians(), -WALL_STICK);
            _g.drawLine((int) stickStart.getX(), (int) stickStart.getY(), (int) stickEnd.getX(), (int) stickEnd.getY());
        }


        //draw gravity pull
        _g.setColor(Color.red);

        int d = 10;
        _g.fillOval((int) mGravityMovement.getGravityCenter().getPosition().getX() - (d / 2), (int) mGravityMovement.getGravityCenter().getPosition().getY() - (d / 2), d, d);
        _g.drawLine((int) mRobot.getPosition().getX(), (int) mRobot.getPosition().getY(), (int) mGravityMovement.getGravityCenter().getPosition().getX(), (int) mGravityMovement.getGravityCenter().getPosition().getY());


        // draw surf pull
        _g.setColor(Color.blue);
        Point2D pointSurf = MyUtils.project(mRobot.getPosition(), mSurfDirection, 1 - mGravityMovement.getGravityCenter().getPower());
        _g.drawLine((int) mRobot.getPosition().getX(), (int) mRobot.getPosition().getY(), (int) pointSurf.getX(), (int) pointSurf.getY());
        _g.fillOval((int) pointSurf.getX() - (d / 2), (int) pointSurf.getY() - (d / 2), d, d);


        // draw actual direction
        _g.setColor(Color.yellow);
        Point2D pointAct = MyUtils.project(mRobot.getPosition(), mActualDirection, 1000);
        _g.drawLine((int) mRobot.getPosition().getX(), (int) mRobot.getPosition().getY(), (int) pointAct.getX(), (int) pointAct.getY());
        _g.fillOval((int) pointAct.getX() - (d / 2), (int) pointAct.getY() - (d / 2), d, d);

        //draw problem mode indicator
        if (mProblemMode) {
            _g.setColor(Color.ORANGE);
            _g.fillOval((int) mRobot.getPosition().getX() - (d / 2), (int) mRobot.getPosition().getY() - (d / 2), d, d);
        }


    }
}
