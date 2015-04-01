package fhooe.ai;

import java.awt.geom.Point2D;

public class GravityPoint {


    private Point2D.Double mPosition;
    private double power;

    public GravityPoint(Point2D.Double _position, double pPower) {
        setPosition(_position);
        setPower(pPower);
    }

    public Point2D.Double getPosition() {
        return mPosition;
    }

    public void setPosition(Point2D.Double _position) {
        mPosition = _position;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double _power) {
        power = _power;
    }
}
