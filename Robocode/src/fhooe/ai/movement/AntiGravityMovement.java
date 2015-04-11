package fhooe.ai.movement;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import fhooe.ai.data.Enemy;
import fhooe.ai.GravityPoint;
import fhooe.ai.util.MyUtils;
import fhooe.ai.TestRobot;
import robocode.util.Utils;

/**
 * Created by Jakob on 31.03.2015.
 */
public class AntiGravityMovement {

    private TestRobot mRobot;

    List<GravityPoint> mCorners = new ArrayList<>(4);

    public AntiGravityMovement(TestRobot _robot) {
        mRobot = _robot;
        mGravityCenter = new GravityPoint(mRobot.getPosition(), 0);
        mCorners.add(new GravityPoint(new Point2D.Double(0, 0), -10000));
        mCorners.add(new GravityPoint(new Point2D.Double(0, mRobot.getBattleFieldWidth()), -10000));
        mCorners.add(new GravityPoint(new Point2D.Double(mRobot.getBattleFieldHeight(), 0), -10000));
        mCorners.add(new GravityPoint(new Point2D.Double(mRobot.getBattleFieldHeight(), mRobot.getBattleFieldWidth()), -10000));
    }

    public GravityPoint getGravityCenter() {
        return mGravityCenter;
    }

    private GravityPoint mGravityCenter;

   public void calcGravity() {

        double xforce = 0;
        double yforce = 0;
        double force;
        double ang;

//        for (EnemyBulletWave bulletWave : mRobot.getBulletWaves()) {
//
//            for (GravityPoint gravityPoint : bulletWave.getGravityPoints()) {
//                //Calculate the total force from this point on us
//                force = gravityPoint.power / Math.pow(MyUtils.getDistance(mRobot.getX(), mRobot.getY(), gravityPoint.mPosition.x, gravityPoint.mPosition.y), 2);
//                //Find the bearing from the point to us
//                ang = MyUtils.normaliseBearing(Math.PI / 2 - Math.atan2(mRobot.getY() - gravityPoint.mPosition.y, mRobot.getX() - gravityPoint.mPosition.x));
//                //Add the components of this force to the total force in their
//                //respective directions
//                xforce += Math.sin(ang) * force;
//                yforce += Math.cos(ang) * force;
//            }
//        }

        for (Enemy enemy : mRobot.getEnemiesCache().getEnemyMap().values()) {
            GravityPoint gravityPoint =  enemy.getGravityPoint();
            //Calculate the total force from this point on us
            force = gravityPoint.getPower() / Math.pow(MyUtils.getDistance(mRobot.getX(), mRobot.getY(), gravityPoint.getPosition().x, gravityPoint.getPosition().y), 2);
            //Find the bearing from the point to us
            ang = MyUtils.normaliseBearing(Math.PI / 2 - Math.atan2(mRobot.getY() - gravityPoint.getPosition().y, mRobot.getX() - gravityPoint.getPosition().x));
            //Add the components of this force to the total force in their
            //respective directions
            xforce += Math.sin(ang) * force;
            yforce += Math.cos(ang) * force;
        }

        /**The following four lines add wall avoidance.  They will only
         affect us if the bot is close to the walls due to the
         force from the walls decreasing at a power 3.**/
        xforce += 500000 / Math.pow(MyUtils.getDistance(mRobot.getX(), mRobot.getY(), mRobot.getBattleFieldWidth(), mRobot.getY()),2.1);
        xforce -= 500000/ Math.pow(MyUtils.getDistance(mRobot.getX(), mRobot.getY(), 0, mRobot.getY()), 2.1);
        yforce += 500000 / Math.pow(MyUtils.getDistance(mRobot.getX(), mRobot.getY(), mRobot.getX(), mRobot.getBattleFieldHeight()), 2.1);
        yforce -= 500000 / Math.pow(MyUtils.getDistance(mRobot.getX(),mRobot.getY(), mRobot.getX(), 0), 2.1);


       for (GravityPoint gravityPoint : mCorners) {
           //Calculate the total force from this point on us
           force = gravityPoint.getPower() / Math.pow(MyUtils.getDistance(mRobot.getX(), mRobot.getY(), gravityPoint.getPosition().x, gravityPoint.getPosition().y), 2.1);
           //Find the bearing from the point to us
           ang = MyUtils.normaliseBearing(Math.PI / 2 - Math.atan2(mRobot.getY() - gravityPoint.getPosition().y, mRobot.getX() - gravityPoint.getPosition().x));
           //Add the components of this force to the total force in their
           //respective directions
           xforce += Math.sin(ang) * force;
           yforce -= Math.cos(ang) * force;
       }
// positive x force -> move to the left
//negative x force -> move to the right
       // positive y force -> move to the top
       // negative y force -> move to the bottom

       //Move in the direction of our resolved force.
       mGravityCenter.getPosition().setLocation(mRobot.getX() - xforce, mRobot.getY() - yforce);
       mGravityCenter.setPower(MyUtils.getDistance(0, 0, xforce, yforce));


   }

    /**
     * Executes the gravity move
     * Make sure to call calcGravity before calling this.
     */
    public void doGravityMove(){
        goTo(mGravityCenter.getPosition().getX(), mGravityCenter.getPosition().getY());
    }

    /**Move in the direction of an x and y coordinate**/
    void goTo(double x, double y) {
        double angle = Math.toDegrees(MyUtils.absbearing(mRobot.getX(), mRobot.getY(), x, y));

         angle =
                Utils.normalRelativeAngle(angle - mRobot.getHeadingRadians());

        if (Math.abs(angle) > (Math.PI/2)) {
            if (angle < 0) {
                mRobot.setTurnRightRadians(Math.PI + angle);
            } else {
                mRobot.setTurnLeftRadians(Math.PI - angle);
            }
            mRobot.setBack(100);
        } else {
            if (angle < 0) {
                mRobot.setTurnLeftRadians(-1*angle);
            } else {
                mRobot.setTurnRightRadians(angle);
            }
            mRobot.setAhead(100);
        }

    }


}
