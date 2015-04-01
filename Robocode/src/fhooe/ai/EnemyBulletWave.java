package fhooe.ai;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import fhooe.ai.movement.Direction;

/**
 * Created by Jakob on 31.03.2015.
 */
// This can be defined as an inner class if you want.
public class EnemyBulletWave {

    private Point2D.Double mFireLocation;
    private long mFireTime;
    private double mBulletVelocity, mDirectAngle;

    public Direction getEvadeDirection() {
        return mEvadeDirection;
    }

    public void setEvadeDirection(Direction _evadeDirection) {
        mEvadeDirection = _evadeDirection;
    }

    private Direction mEvadeDirection = Direction.UNDEFINED;
    private double[] mDangerIndicator = new double[36];
    private List<GravityPoint> mGravityPoints = new ArrayList<>();

    public EnemyBulletWave(Point2D.Double _fireLocation, long _fireTime, double _bulletVelocity, double _directAngle) {

        mFireLocation = _fireLocation;
        mFireTime = _fireTime;
        mBulletVelocity = _bulletVelocity;
        mDirectAngle = _directAngle;
    }

    public EnemyBulletWave() {

    }



    public List<GravityPoint> getGravityPoints() {
        return mGravityPoints;
    }

    public void setGravityPoints(List<GravityPoint> _gravityPoints) {
        mGravityPoints = _gravityPoints;
    }

    public Point2D.Double getFireLocation() {
        return mFireLocation;
    }

    public void setFireLocation(Point2D.Double _fireLocation) {
        mFireLocation = _fireLocation;
    }

    public long getFireTime() {
        return mFireTime;
    }

    public void setFireTime(long _fireTime) {
        mFireTime = _fireTime;
    }

    public double getBulletVelocity() {
        return mBulletVelocity;
    }

    public void setBulletVelocity(double _bulletVelocity) {
        mBulletVelocity = _bulletVelocity;
    }

    public double getDirectAngle() {
        return mDirectAngle;
    }

    public void setDirectAngle(double _directAngle) {
        mDirectAngle = _directAngle;
    }

    public double getDistanceTraveled(long _time) {
        return mBulletVelocity * (_time - mFireTime);
    }






}