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
    private double mGravityAngle = 0;

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

        }   else
        if (mProblemMode && myTick > 5) {
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
        double gravityForce = gravityPoint.getPower() / 50f;

        //surferMovement
        mSurferAngle = mSurferMovement.getSurfDirection();


        if (gravityForce > 2) {
            //don't mess around with direction if gravity force is very big
            // this should help to escape the wall and corners
            mOffsetAngle = 0;
        }
        mGravityAngle = MyUtils.normaliseHeading(mGravityAngle);
        mSurferAngle = MyUtils.normaliseHeading(mSurferAngle);

        //accumulate movement
        if (gravityForce > 1) {
            //gravity force very big, ignore wave surfing
            mActualAngle = mGravityAngle - mOffsetAngle;
        } else {
            mActualAngle = (mSurferAngle * (1 - gravityForce)) + (mGravityAngle * gravityForce) - mOffsetAngle;
        }


        turnAndMove(mActualAngle);


        if (log) {
            System.out.println("gravity influence: " + gravityForce);
            System.out.println("angle gravity: " + mGravityAngle);
            System.out.println("angle surfer: " + mSurferAngle);
            System.out.println("dir " + mDirection);
            System.out.println("angle accumulated: " + mActualAngle);

        }


    }


    public void turnAndMove(double goAngle) {


        int pointDir;
        mRobot.setAhead(1000 * (pointDir = (Math.abs(goAngle - mRobot.getHeadingRadians()) < Math.PI / 2 ? 1 : -1)));
        mRobot.setTurnRightRadians(Utils.normalRelativeAngle(goAngle + (pointDir == -1 ? Math.PI : 0) - mRobot.getHeadingRadians()));

        Direction newDirection = Direction.fromInt(pointDir);

        if (newDirection != mDirection) {
            mDirection = newDirection;
            mDirectionChange++;
        }

    }


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

            Point2D.Double stickEnd = MyUtils.project(mRobot.getPosition(), mRobot.getHeadingRadians(), 50);
            Point2D.Double stickStart = mRobot.getPosition();
            _g.drawLine((int) stickStart.getX(), (int) stickStart.getY(), (int) stickEnd.getX(), (int) stickEnd.getY());

        } else if (mDirection == Direction.BACKWARD) {
            Point2D.Double stickEnd = mRobot.getPosition();
            Point2D.Double stickStart = MyUtils.project(mRobot.getPosition(), mRobot.getHeadingRadians(), -50);
            _g.drawLine((int) stickStart.getX(), (int) stickStart.getY(), (int) stickEnd.getX(), (int) stickEnd.getY());
        }


        //draw gravity pull
        _g.setColor(Color.red);

        int d = 10;
        _g.fillOval((int) mGravityMovement.getGravityCenter().getPosition().getX() - (d / 2), (int) mGravityMovement.getGravityCenter().getPosition().getY() - (d / 2), d, d);
        _g.drawLine((int) mRobot.getPosition().getX(), (int) mRobot.getPosition().getY(), (int) mGravityMovement.getGravityCenter().getPosition().getX(), (int) mGravityMovement.getGravityCenter().getPosition().getY());


        // draw surf pull
        _g.setColor(Color.blue);
        Point2D pointSurf = MyUtils.project(mRobot.getPosition(), mSurferAngle, 1 - mGravityMovement.getGravityCenter().getPower());
        _g.drawLine((int) mRobot.getPosition().getX(), (int) mRobot.getPosition().getY(), (int) pointSurf.getX(), (int) pointSurf.getY());
        _g.fillOval((int) pointSurf.getX() - (d / 2), (int) pointSurf.getY() - (d / 2), d, d);


        // draw actual direction
        _g.setColor(Color.yellow);
        Point2D pointAct = MyUtils.project(mRobot.getPosition(), mActualAngle, 1000);
        _g.drawLine((int) mRobot.getPosition().getX(), (int) mRobot.getPosition().getY(), (int) pointAct.getX(), (int) pointAct.getY());
        _g.fillOval((int) pointAct.getX() - (d / 2), (int) pointAct.getY() - (d / 2), d, d);

        //draw problem mode indicator
        if (mProblemMode) {
            d=20;
            _g.setColor(Color.ORANGE);
            _g.fillOval((int) mRobot.getPosition().getX() - (d / 2), (int) mRobot.getPosition().getY() - (d / 2), d, d);
        }


    }


}
