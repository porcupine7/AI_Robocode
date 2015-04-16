package fhooe.ai.data;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import fhooe.ai.Botzilla;
import fhooe.ai.GravityPoint;
import fhooe.ai.util.MyUtils;
import robocode.ScannedRobotEvent;

/**
 * Created by andy on 30.03.15.
 */
public class Enemy {
  public static final int INVALID = -1;
  public static final int UPDATE_THRESHOLD = 16;
  public static final int MAX_EVENT_COUNT = 10;
  public static final int GRAVITY_FORCE = -400000;
  private final List<ScannedRobotEvent> mScanEvents;
  private final String mName;
  private ScannedRobotEvent mCurEvent;
  private ScannedRobotEvent mLastEvent;

  public double getLastShootingTime() {
    return mLastShootingTime;
  }

  public void setLastShootingTime(double _lastShootingTime) {
    mLastShootingTime = _lastShootingTime;
  }

  private double mLastShootingTime;


//  public double getGunHeat() {
//    return mGunHeat;
//  }
//
//  public void setGunHeat(double _gunHeat) {
//    mGunHeat = _gunHeat;
//  }
//                enemy.setGunHeat(0.2 * (enemy.getLastEvent().getEnergy() - 0.1) + 0.92);

//  private double mGunHeat = 0;

  public Point2D.Double getPosition() {
    return mPosition;
  }

  private Point2D.Double mPosition;

  public Enemy(String _name) {
    mName = _name;
    mScanEvents = new ArrayList<ScannedRobotEvent>();
  }

//  public Point2D.Double guessPosition(long when) {
//    double diff = when - mLastEvent.getTime();
//    double newY = mLastEvent.get + Math.cos(getHeading()) * getVelocity() * diff;
//    double newX = x + Math.sin(getHeading()) * getVelocity() * diff;
//
//    return new Point2D.Double(newX, newY);
//  }

  public String getName() {
    return mName;
  }

  public double getEnergy() {
    return mCurEvent != null ? mCurEvent.getEnergy() : INVALID;
  }

  public double getBearing() {
    return mCurEvent != null ? mCurEvent.getBearing() : INVALID;
  }

  public double getBearingRadians() {
    return mCurEvent != null ? mCurEvent.getBearingRadians() : INVALID;
  }

  public double getVelocity() {
    return mCurEvent != null ? mCurEvent.getVelocity() : INVALID;
  }

  public double getDistance() {
     return mCurEvent != null ? mCurEvent.getDistance() : INVALID;
  }

  public double getHeading() { return mCurEvent != null ? mCurEvent.getHeading() : INVALID; }

  public ScannedRobotEvent getLastEvent() {
    return mLastEvent;
  }

  public void addScanEvent(ScannedRobotEvent _event, Botzilla _robot) {
    mLastEvent = mCurEvent;
    mCurEvent = _event;
    mScanEvents.add(_event);
    if (mScanEvents.size() > MAX_EVENT_COUNT) {
      mScanEvents.remove(0);
    }
    double absBearing = _event.getBearingRadians() + _robot.getHeadingRadians();
    mPosition = MyUtils.project(_robot.getPosition(), absBearing, _event.getDistance());

  }

  public GravityPoint getGravityPoint() {
    return new GravityPoint(mPosition, GRAVITY_FORCE);
  }

//  public void coolDownGun(float _value) {
//    mGunHeat -= _value;
//  }

  public long getLastUpdateTime() {
    return mCurEvent != null ? mCurEvent.getTime() : INVALID;
  }

  public boolean isUpdated(long _robotTime) {
    long lastUpdate = getLastUpdateTime();
    boolean isInvalid = lastUpdate == INVALID;
    if (isInvalid) return false;
    boolean isUpdated = _robotTime - lastUpdate < UPDATE_THRESHOLD;
    return isUpdated;
  }

  @Override
  public String toString() {
    return "nr=" + mScanEvents.size() + "|e=" + getEnergy() + "|v=" + getVelocity();
  }
}
