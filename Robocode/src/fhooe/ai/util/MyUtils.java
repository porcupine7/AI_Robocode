package fhooe.ai.util;

import java.awt.geom.Point2D;

import static java.lang.Math.PI;
/**
 * Created by Jakob on 31.03.2015.
 */
public class MyUtils {

    /**Returns the distance between two points**/
   public static double getDistance(double x1, double y1, double x2, double y2) {
        double x = x2-x1;
        double y = y2-y1;
        double range = Math.sqrt(x*x + y*y);
        return range;
    }

    //if a bearing is not within the -pi to pi range, alters it to provide the shortest angle
   public static double normaliseBearing(double ang) {
        if (ang > PI)
            ang -= 2*PI;
        if (ang < -PI)
            ang += 2*PI;
        return ang;
    }

    //if a heading is not within the 0 to 2pi range, alters it to provide the shortest angle
    public static  double normaliseHeading(double ang) {
        if (ang > 2*PI)
            ang -= 2*PI;
        if (ang < 0)
            ang += 2*PI;
        return ang;
    }

    // got this from RaikoMicro, by Jamougha, but I think it's used by many authors
    //  - returns the absolute angle (in radians) from source to target points
    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    public static  double absbearing( Point2D.Double _source,  Point2D.Double _target ) {
        return absbearing(_source.x, _source.y, _target.x, _target.y);
    }

    //gets the absolute bearing between to x,y coordinates
    public static  double absbearing( double x1,double y1, double x2,double y2 )
    {
        double xo = x2-x1;
        double yo = y2-y1;
        double h = getDistance(x1, y1, x2, y2);
        if( xo > 0 && yo > 0 )
        {
            return Math.asin( xo / h );
        }
        if( xo > 0 && yo < 0 )
        {
            return Math.PI - Math.asin( xo / h );
        }
        if( xo < 0 && yo < 0 )
        {
            return Math.PI + Math.asin( -xo / h );
        }
        if( xo < 0 && yo > 0 )
        {
            return 2.0*Math.PI - Math.asin( -xo / h );
        }
        return 0;
    }

    // CREDIT: from CassiusClay, by PEZ
    //   - returns point length away from sourceLocation, at angle
    // robowiki.net?CassiusClay
    public static Point2D.Double project(Point2D.Double sourceLocation, double _angle, double _distance) {
        return new Point2D.Double(sourceLocation.x + Math.sin(_angle) * _distance,
                sourceLocation.y + Math.cos(_angle) * _distance);
    }

}
