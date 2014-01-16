package com.flightcomputer.utilities;

import android.graphics.PointF;

public class Point2d {
    public double x;
    public double y;

    public Point2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point2d(PointF p) {
        this.x = p.x;
        this.y = p.y;
    }

    public Point2d() {

    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
