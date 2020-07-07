import javafx.util.Pair;

import java.awt.*;
import java.util.ArrayList;

public class Test {
    public static void main (String[] args) {
        MyMath myMath = new MyMath();

        //Test midpoint
        Point midpoint = myMath.getMidpoint(new Point(0, 0), new Point(4, 4));
        System.out.println("midpoint (0,0) & (4,4): (" + Integer.toString(midpoint.x) + "," + Integer.toString(midpoint.y) + ")");
        midpoint = myMath.getMidpoint(new Point(0, 1), new Point(0, 5));
        System.out.println("midpoint (0,1) & (0,5): (" + Integer.toString(midpoint.x) + "," + Integer.toString(midpoint.y) + ")");
        midpoint = myMath.getMidpoint(new Point(3, 0), new Point(3, 4));
        System.out.println("midpoint (3,0) & (3,4): (" + Integer.toString(midpoint.x) + "," + Integer.toString(midpoint.y) + ")");

        //Test best fit line
        ArrayList<Point> points = new ArrayList<>();
        points.add(new Point(0,0));
        points.add(new Point(1,1));
        points.add(new Point(3,3));
        Pair<Integer, Integer> line = myMath.getBestFitLine(points);
        System.out.println("line (0,0) (1,1) (3,3): y = " + Integer.toString(line.getKey()) + "x + " + Integer.toString(line.getValue()));

        ArrayList<Point> points1 = new ArrayList<>();
        points1.add(new Point(3,0));
        points1.add(new Point(2,2));
        points1.add(new Point(1,3));
        Pair<Integer, Integer> line1 = myMath.getBestFitLine(points1);
        System.out.println("line (1,1) (2,1) (3,3): y = " + Integer.toString(line1.getKey()) + "x + " + Integer.toString(line1.getValue()));

        //Test intersection
        Pair<Double, Double> linePositive = new Pair(1.0, 0.0);
        Pair<Double, Double> lineNegative = new Pair(-1.0, 4.0);
        Point intersection = myMath.getIntersection(linePositive, lineNegative);
        System.out.println("intersection of y = x & y = -x + 4: (" + Integer.toString(intersection.x) + "," + Integer.toString(intersection.y) + ")");
    }

}
