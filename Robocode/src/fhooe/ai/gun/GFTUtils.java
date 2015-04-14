package fhooe.ai.gun;

import java.awt.geom.Point2D;

/**
 * Created by Christian on 14.04.2015.
 */
public class GFTUtils {
    public static double bulletVelocity(double power) {
        return 20 - 3 * power;
    }

    public static Point2D project(Point2D sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.getX() + Math.sin(angle) * length,
                sourceLocation.getY() + Math.cos(angle) * length);
    }

    public static double absoluteBearing(Point2D source, Point2D target) {
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }

    /*
 * This extremely useful method lets us project one point from another given a specific angle and distance.
 */
    public static Point2D.Double project(Point2D.Double origin,double dist,double angle){
        return new Point2D.Double(origin.x+dist*Math.sin(angle),origin.y+dist*Math.cos(angle));
    }

    public static int sign(double v) {
        return v < 0 ? -1 : 1;
    }

    public static int minMax(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
