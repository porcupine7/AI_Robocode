package fhooe.ai.movement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Random;

import fhooe.ai.GravityPoint;
import fhooe.ai.TestRobot;
import fhooe.ai.util.MyUtils;
import robocode.util.Utils;

/**
 * Created by Jakob on 13.04.2015.
 */
public class CombinedMovement {

    public static final boolean log = true;
    private TestRobot mRobot;
    private AntiGravityMovement mGravityMovement;
    private SurferMovement mSurferMovement;
    // current movement direction
    private Direction mDirection = Direction.UNDEFINED;

    private Random mRandom = new Random();

    //Needed for back forth handling
    private int mDirectionChange = 0;
    private int myTick = 0;
    private boolean mProblemMode = false;

    //movement angles
    private double mOffsetAngle = 0;
    private double mActualAngle = 0;
    private double mSurferAngle = 0;
    private double mGravityAngle = Double.NaN;

    public CombinedMovement(TestRobot _robot, AntiGravityMovement _gravityMovement, SurferMovement _surferMovement) {
        mRobot = _robot;
        mGravityMovement = _gravityMovement;
        mSurferMovement = _surferMovement;
    }

    public void doMove() {

        //region problem handling (turning back and forth repeatedly)

        myTick++;

        if (!mProblemMode) {

            if (mDirectionChange > 2) {
                //danger ahead, turn 90 degree
                System.out.println("Problem detected");
                if (mRandom.nextFloat() > 0.5) {
                    mOffsetAngle = (Math.PI / 2);
                } else {
                    mOffsetAngle = -(Math.PI / 2);
                }
                myTick = 0;
                mProblemMode = true;
            }

            if (myTick > 20) {
                //reset direction change count every x ticks
                myTick = 0;
                mDirectionChange = 0;
            }

        } else if (mProblemMode && myTick > 5) {
            mOffsetAngle = 0;
            mDirectionChange = 0;
            myTick = 0;
            mProblemMode = false;
            System.out.println("Problem mode off");
        }


// endregion


        //gravity movement
        GravityPoint gravityPoint = mGravityMovement.getGravityCenter();
        mGravityAngle = MyUtils.absoluteBearing(mRobot.getPosition(), gravityPoint.getPosition());
        double gravityForce = gravityPoint.getPower() / 70f;

        //surferMovement
        mSurferAngle = mSurferMovement.getSurfAngle();


        if (gravityForce > 2) {
            //don't mess around with direction if gravity force is very big
            // this should help to escape the wall and corners
            mOffsetAngle = 0;
        }
        mGravityAngle = MyUtils.normaliseHeading(mGravityAngle);

        //accumulate movement

        if (Double.isNaN(mSurferAngle)) {
            //no waves, ignore wave surfing
            mActualAngle = mGravityAngle - mOffsetAngle;
            if (log)
                System.out.println("gravity only");
        }else
        if (gravityForce < 0.2 ) {
            //gravity very weak, use only wave surfing
            mActualAngle = mSurferAngle;
            if (log)
                System.out.println("surfing only");
        }else
        if (gravityForce > 1) {
            //gravity very strong, ignore wave surfing
            mActualAngle = mGravityAngle - mOffsetAngle;
            if (log)
                System.out.println("gravity only");

        } else {
            mActualAngle = (mSurferAngle * (1 - gravityForce)) + (mGravityAngle * gravityForce) - mOffsetAngle;
            if (log)
                System.out.println("combined");
        }

        turnAndMove(mActualAngle);


        if (log) {
            System.out.println("gravity influence: " + gravityForce);

        }


    }


    public void turnAndMove(double absAngle) {

        int pointDir = (Math.abs(absAngle - mRobot.getHeadingRadians()) < Math.PI / 2 ? 1 : -1);
        mRobot.setAhead(100 * pointDir);
        mRobot.setTurnRightRadians(Utils.normalRelativeAngle(absAngle + (pointDir == -1 ? Math.PI : 0) - mRobot.getHeadingRadians()));
        Direction newDirection = Direction.fromInt(pointDir);

        if (newDirection != mDirection) {
            mDirection = newDirection;
            mDirectionChange++;
        }

    }


    public void draw(Graphics2D _g) {

        if (log) {


//        //draw direction stick
//            if (mDirection == Direction.FORWARD) {
//
//                Point2D.Double stickEnd = MyUtils.project(mRobot.getPosition(), mRobot.getHeadingRadians(), 50);
//                Point2D.Double stickStart = mRobot.getPosition();
//                _g.drawLine((int) stickStart.getX(), (int) stickStart.getY(), (int) stickEnd.getX(), (int) stickEnd.getY());
//
//            } else if (mDirection == Direction.BACKWARD) {
//                Point2D.Double stickEnd = mRobot.getPosition();
//                Point2D.Double stickStart = MyUtils.project(mRobot.getPosition(), mRobot.getHeadingRadians(), -50);
//                _g.drawLine((int) stickStart.getX(), (int) stickStart.getY(), (int) stickEnd.getX(), (int) stickEnd.getY());
//            }
            int d = 10;


            // draw actual direction
            _g.setColor(Color.yellow);
            Point2D pointAct = MyUtils.project(mRobot.getPosition(), mActualAngle, 100);
            _g.drawLine((int) mRobot.getPosition().getX(), (int) mRobot.getPosition().getY(), (int) pointAct.getX(), (int) pointAct.getY());
            _g.fillOval((int) pointAct.getX() - (d / 2), (int) pointAct.getY() - (d / 2), d, d);


            //draw gravity pull
            _g.setColor(Color.red);

            _g.fillOval((int) mGravityMovement.getGravityCenter().getPosition().getX() - (d / 2), (int) mGravityMovement.getGravityCenter().getPosition().getY() - (d / 2), d, d);
            _g.drawLine((int) mRobot.getPosition().getX(), (int) mRobot.getPosition().getY(), (int) mGravityMovement.getGravityCenter().getPosition().getX(), (int) mGravityMovement.getGravityCenter().getPosition().getY());


            if(!Double.isNaN(mSurferAngle)) {
                // draw surf pull

                if (mDirection == Direction.FORWARD) {
                    _g.setColor(Color.blue);
                    Point2D pointSurf = MyUtils.project(mRobot.getPosition(), mSurferAngle, (1 - mGravityMovement.getGravityCenter().getPower()) * 10);
                    _g.drawLine((int) mRobot.getPosition().getX(), (int) mRobot.getPosition().getY(), (int) pointSurf.getX(), (int) pointSurf.getY());
                    _g.fillOval((int) pointSurf.getX() - (d / 2), (int) pointSurf.getY() - (d / 2), d, d);
                } else {
                    _g.setColor(Color.CYAN);
                    Point2D pointSurf = MyUtils.project(mRobot.getPosition(), mSurferAngle + Math.PI, (1 - mGravityMovement.getGravityCenter().getPower()) * 10);
                    _g.drawLine((int) mRobot.getPosition().getX(), (int) mRobot.getPosition().getY(), (int) pointSurf.getX(), (int) pointSurf.getY());
                    _g.fillOval((int) pointSurf.getX() - (d / 2), (int) pointSurf.getY() - (d / 2), d, d);
                }

            }



            //draw problem mode indicator
            if (mProblemMode) {
                d = 20;
                _g.setColor(Color.ORANGE);
                _g.fillOval((int) mRobot.getPosition().getX() - (d / 2), (int) mRobot.getPosition().getY() - (d / 2), d, d);
            }

        }

    }


}
