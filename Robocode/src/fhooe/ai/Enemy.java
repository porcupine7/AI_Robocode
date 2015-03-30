package fhooe.ai;

import robocode.ScannedRobotEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andy on 30.03.15.
 */
public class Enemy {
  public static final int INVALID = -1;
  public static final int UPDATE_THRESHOLD = 16;
  private final List<ScannedRobotEvent> mScanEvents;
  private final String mName;
  private ScannedRobotEvent mCurEvent;

  public Enemy(String _name) {
    mName = _name;
    mScanEvents = new ArrayList<ScannedRobotEvent>();
  }

  public String getName() {
    return mName;
  }

  public double getEnergy() {
    return mCurEvent != null ? mCurEvent.getEnergy() : INVALID;
  }

  public double getBearing() {
    return mCurEvent != null ? mCurEvent.getBearing() : INVALID;
  }

  public double getVelocity() {
    return mCurEvent != null ? mCurEvent.getVelocity() : INVALID;
  }

  public void addScanEvent(ScannedRobotEvent _event) {
    mCurEvent = _event;
    mScanEvents.add(_event);
  }

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
