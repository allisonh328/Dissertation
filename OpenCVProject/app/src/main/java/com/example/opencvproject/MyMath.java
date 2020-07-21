package com.example.opencvproject;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.util.Pair;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MyMath {

    private ArrayList<HashMap<String, Pair<Point, Point>>> buckets = new ArrayList<>();
    private static final String TAG = "ALLISON_COMMENT";

    public Mat combineLines(Mat lines, Mat size) {

        Mat newLines = new Mat();
        newLines.create(size.rows(), size.cols(), CvType.CV_8UC1);
        Log.i(TAG, "MyMath.combineLines: # of lines = " + Integer.toString(lines.rows()));

        for (int i = 0; i < lines.rows(); i++) {
            double[] points1 = lines.get(i, 0);

            HashMap<String, Pair<Point, Point>> bucket = new HashMap<>();
            buckets.add(bucket);

            for(int j = i; j < lines.rows(); j++) {
                Pair<Point, Point> line2 = checkBuckets(lines.get(j, 0), j);
                if(isSameLine(lines.get(i, 0), lines.get(j, 0))) {
                    Point ln1pt1 = new Point(points1[0], points1[1]);
                    Point ln1pt2 = new Point(points1[2], points1[3]);
                    Pair line1 = new Pair(ln1pt1, ln1pt2);
                    bucket.put("line" + Integer.toString(i), line1);
                    if(line2 != null) {
                        bucket.put("line" + Integer.toString(j), line2);
                    }
                }
            }
            Log.i(TAG, "MyMath.combineLines: # of lines in bucket = " + Integer.toString(bucket.size()));
        }
        Log.i(TAG, "MyMath.combineLines: # of total buckets = " + Integer.toString(buckets.size()));
        ArrayList<HashMap<String, Pair<Point, Point>>> tempBuckets = new ArrayList<>();
        for(HashMap<String, Pair<Point, Point>> bucket2search: buckets) {
            if(bucket2search.size() > 3) {
                tempBuckets.add(bucket2search);
            }
        }
        buckets = tempBuckets;
        Log.i(TAG, "MyMath.combineLines: # of final buckets = " + Integer.toString(buckets.size()));

        for(HashMap<String, Pair<Point, Point>> bucket: buckets) {
            List<Point> pointPool = new ArrayList<>();
            Set<String> keys = bucket.keySet();
            for(String key: keys) {
                pointPool.add(bucket.get(key).first);
                pointPool.add(bucket.get(key).second);
            }
            Pair<Double, Double> newLine = getBestFitLine(pointPool);
            Point pt1 = new Point(0, newLine.second);
            Point pt2 = new Point(-newLine.second / newLine.first, 0);
            //Imgproc.clipLine(Imgproc.boundingRect(lines), pt1, pt2);
            Imgproc.line(newLines, pt1, pt2, new Scalar(255, 0, 0), 2);
        }

        return newLines;
    }

    private Point getMidpoint(Point point1, Point point2) {
        return new Point((point1.x + point2.x) / 2, (point1.y + point2.y) / 2);
    }

    private boolean isSameLine(double[] points1, double[] points2) {
        List<Point> points = new ArrayList<>();
        points.add(new Point(points1[0], points1[1]));
        points.add(new Point(points1[2], points1[3]));
        points.add(new Point(points2[0], points2[1]));
        points.add(new Point(points2[2], points2[3]));

        Pair<Double, Double> vect1 = new Pair(points1[2] - points1[0], points1[3] - points1[1]);
        Pair<Double, Double> vect2 = new Pair(points2[2] - points2[0], points2[3] - points2[1]);

        double dotProduct = vect1.first * vect2.first + vect1.second + vect2.second;
        double mag1 = Math.sqrt(vect1.first * vect1.first + vect1.second * vect1.second);
        double mag2 = Math.sqrt(vect2.first * vect2.first + vect2.second * vect2.second);

        double theta = Math.acos(dotProduct / (mag1 * mag2));
        if(theta > 0.05) {
            return false;
        }
        Pair<Double, Double> bestFitLine = getBestFitLine(points);
        Point midpoint = getMidpoint(new Point(points2[0], points2[1]), new Point(points2[2], points2[3]));
        double mIntersect = -1.0 / bestFitLine.first;
        double bIntersect = midpoint.y - mIntersect * midpoint.x;
        Point intersect = getIntersection(bestFitLine, new Pair(mIntersect, bIntersect));
        double distance = Math.sqrt((midpoint.x - intersect.x) * (midpoint.x - intersect.x) + (midpoint.y - intersect.y) * (midpoint.y - intersect.y));
        if(distance > 5) {
            return false;
        }
        return true;
    }

    private Pair<Double, Double> getBestFitLine(List<Point> pointPool) {

        double xsum = 0;
        double ysum = 0;

        for(Point point: pointPool) {
            xsum = xsum + point.x;
            ysum = ysum + point.y;
        }
        double xavg = xsum / pointPool.size();
        double yavg = ysum / pointPool.size();
        double numerator = 0;
        double denominator = 0;
        for(Point point: pointPool) {
            numerator = numerator + (point.x - xavg) * (point.y - yavg);
            denominator = denominator + (point.x - xavg) * (point.x - xavg);
        }
        double m = numerator / denominator;
        double b = yavg - m * xavg;

        return new Pair(m, b);
    }

    private Pair<Point, Point> checkBuckets(double[] points, int j) {

        for(HashMap<String, Pair<Point, Point>> bucket2search: buckets) {
            if(bucket2search.containsKey("line" + Integer.toString(j))) {
                return null;
            }
        }

        Point ln2pt1 = new Point(points[0], points[1]);
        Point ln2pt2 = new Point(points[2], points[3]);
        Pair line2 = new Pair(ln2pt1, ln2pt2);
        return line2;
    }

    private Point getIntersection(Pair<Double, Double> line1, Pair<Double, Double> line2) {
        double x = (line2.second -  line1.second) / (line1.first - line2.first);
        double y = line1.first * x + line1.second;
        return new Point(x, y);
    }

    public ArrayList<Double[]> drawLines(ArrayList<Point> circles)
    {
        Log.i(TAG, "MyMath.drawLines: # circles = " + Long.toString(circles.size()));


        ArrayList<Double[]> lines = new ArrayList<>();

        int k = 0;
        for(int i = 0; i < circles.size(); i++) {
            Log.i(TAG, "i = " + Integer.toString(i));
            Log.i(TAG, "MyMath.drawLines: point 1 = (" + Double.toString(circles.get(i).x) + "," + Double.toString(circles.get(i).y) + ")");
            for(int j = i + 1; j < circles.size(); j++) {
                Log.i(TAG, "j = " + Integer.toString(j));
                Double[] points = {circles.get(i).x, circles.get(i).y, circles.get(j).x, circles.get(j).y};
                lines.add(points);
                Log.i(TAG, "MyMath.drawLines: point 2 = (" + Double.toString(circles.get(j).x) + "," + Double.toString(circles.get(j).y) + ")");
                k++;
            }
        }
        Log.i(TAG, "MyMath.drawLines: # lines = " + Long.toString(lines.size()));
        return lines;
    }

    private int getSize(int n) {
        if(n == 2) {
            return 1;
        } else {
            return (n - 1) + getSize(n - 1);
        }
    }


}
