import javafx.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MyMath {

    ArrayList<HashMap<String, Pair<Point, Point>>> buckets = new ArrayList();

    public Point getMidpoint(Point point1, Point point2) {
        return new Point((point1.x + point2.x) / 2, (point1.y + point2.y) / 2);
    }

    public boolean isSameLine(double[] points1, double[] points2) {
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(new Point((int) points1[0], (int) points1[1]));
        points.add(new Point((int) points1[2], (int) points1[3]));
        points.add(new Point((int) points2[0], (int) points2[1]));
        points.add(new Point((int) points2[2], (int) points2[3]));

        Pair<Double, Double> vect1 = new Pair(points1[2] - points1[0], points1[3] - points1[1]);
        Pair<Double, Double> vect2 = new Pair(points2[2] - points2[0], points2[3] - points2[1]);

        double dotProduct = vect1.getKey() * vect2.getKey() + vect1.getValue() + vect2.getValue();
        double mag1 = Math.sqrt(vect1.getKey() * vect1.getKey() + vect1.getValue() * vect1.getValue());
        double mag2 = Math.sqrt(vect2.getKey() * vect2.getKey() + vect2.getValue() * vect2.getValue());

        double theta = Math.acos(dotProduct / (mag1 * mag2));
        if(theta > 0.05) {
            return false;
        }
        Pair<Integer, Integer> bestFitLine = getBestFitLine(points);
        Point midpoint = getMidpoint(new Point((int) points2[0], (int) points2[1]), new Point((int) points2[2], (int) points2[3]));
        double mIntersect = -1.0 / bestFitLine.getKey();
        double bIntersect = midpoint.y - mIntersect * midpoint.x;
        Point intersect = getIntersection(bestFitLine, new Pair(mIntersect, bIntersect));
        double distance = Math.sqrt((midpoint.x - intersect.x) * (midpoint.x - intersect.x) + (midpoint.y - intersect.y) * (midpoint.y - intersect.y));
        if(distance > 5) {
            return false;
        }
        return true;
    }

    public Pair<Integer, Integer> getBestFitLine(ArrayList<Point> pointPool) {

        double xsum = 0;
        double ysum = 0;

        for(Point point: pointPool) {
            xsum = xsum + point.x;
            ysum = ysum + point.y;
        }
        System.out.println("xsum = " + Double.toString(xsum));
        System.out.println("ysum = " + Double.toString(ysum));
        double xavg = xsum / pointPool.size();
        double yavg = ysum / pointPool.size();
        System.out.println("xavg = " + Double.toString(xsum));
        System.out.println("yavg = " + Double.toString(ysum));
        double numerator = 0;
        double denominator = 0;
        for(Point point: pointPool) {
            numerator = numerator + (point.x - xavg) * (point.y - yavg);
            denominator = denominator + (point.x - xavg) * (point.x - xavg);
        }
        double m = numerator / denominator;
        double b = yavg - m * xavg;

        return new Pair((int) m, (int) b);
    }

    public Pair<Point, Point> checkBuckets(double[] points, int j) {

        for(HashMap<String, Pair<Point, Point>> bucket2search: buckets) {
            if(bucket2search.containsKey("line" + Integer.toString(j))) {
                return null;
            }
        }

        Point ln2pt1 = new Point((int) points[0], (int) points[1]);
        Point ln2pt2 = new Point((int) points[2], (int) points[3]);
        Pair line2 = new Pair(ln2pt1, ln2pt2);
        return line2;
    }

    public Point getIntersection(Pair<Double, Double> line1, Pair<Double, Double> line2) {
        double x = (line2.getValue() -  line1.getValue()) / (line1.getKey() - line2.getKey());
        double y = line1.getKey() * x + line1.getValue();
        return new Point((int) x, (int) y);
    }
}
