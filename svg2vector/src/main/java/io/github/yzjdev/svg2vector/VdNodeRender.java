

package io.github.yzjdev.svg2vector;

import android.graphics.Path;

class VdNodeRender {

    private static final String TAG = "VdNodeRender";

    public static void createPath(VdPath.Node[] node, Path path) {
        float[] current = new float[6];
        char lastCmd = ' ';
        for (VdPath.Node value : node) {
            addCommand(path, current, value.type, lastCmd, value.params);
            lastCmd = value.type;
        }
    }

    private static void addCommand(Path path, float[] current, char cmd,
                                   char lastCmd, float[] val) {
        int incr = 2;

        float cx = current[0];
        float cy = current[1];
        float cpx = current[2];
        float cpy = current[3];
        float loopX = current[4];
        float loopY = current[5];

        switch (cmd) {
            case 'z':
            case 'Z':
                path.close();
                cx = loopX;
                cy = loopY;
                break;
            case 'm':
            case 'M':
            case 'l':
            case 'L':
            case 't':
            case 'T':
                incr = 2;
                break;
            case 'h':
            case 'H':
            case 'v':
            case 'V':
                incr = 1;
                break;
            case 'c':
            case 'C':
                incr = 6;
                break;
            case 's':
            case 'S':
            case 'q':
            case 'Q':
                incr = 4;
                break;
            case 'a':
            case 'A':
                incr = 7;
        }

        for (int k = 0; k < val.length; k += incr) {
            boolean reflectCtrl;
            float tempReflectedX, tempReflectedY;

            switch (cmd) {
                case 'm':
                    cx += val[k];
                    cy += val[k + 1];
                    path.moveTo(cx, cy);
                    loopX = cx;
                    loopY = cy;
                    break;
                case 'M':
                    cx = val[k];
                    cy = val[k + 1];
                    path.moveTo(cx, cy);
                    loopX = cx;
                    loopY = cy;
                    break;
                case 'l':
                    cx += val[k];
                    cy += val[k + 1];
                    path.lineTo(cx, cy);
                    break;
                case 'L':
                    cx = val[k];
                    cy = val[k + 1];
                    path.lineTo(cx, cy);
                    break;
                case 'z':
                case 'Z':
                    path.close();
                    cx = loopX;
                    cy = loopY;
                    break;
                case 'h':
                    cx += val[k];
                    path.lineTo(cx, cy);
                    break;
                case 'H':
                    cx = val[k];
                    path.lineTo(cx, cy);
                    break;
                case 'v':
                    cy += val[k];
                    path.lineTo(cx, cy);
                    break;
                case 'V':
                    cy = val[k];
                    path.lineTo(cx, cy);
                    break;
                case 'c':
                    path.cubicTo(cx + val[k], cy + val[k + 1],
                            cx + val[k + 2], cy + val[k + 3],
                            cx + val[k + 4], cy + val[k + 5]);
                    cpx = cx + val[k + 2];
                    cpy = cy + val[k + 3];
                    cx += val[k + 4];
                    cy += val[k + 5];
                    break;
                case 'C':
                    path.cubicTo(val[k], val[k + 1],
                            val[k + 2], val[k + 3],
                            val[k + 4], val[k + 5]);
                    cpx = val[k + 2];
                    cpy = val[k + 3];
                    cx = val[k + 4];
                    cy = val[k + 5];
                    break;
                case 's':
                    reflectCtrl = lastCmd == 'c' || lastCmd == 's' || lastCmd == 'C' || lastCmd == 'S';
                    float ctrl1X = reflectCtrl ? 2 * cx - cpx : cx;
                    float ctrl1Y = reflectCtrl ? 2 * cy - cpy : cy;
                    path.cubicTo(ctrl1X, ctrl1Y,
                            cx + val[k], cy + val[k + 1],
                            cx + val[k + 2], cy + val[k + 3]);
                    cpx = cx + val[k];
                    cpy = cy + val[k + 1];
                    cx += val[k + 2];
                    cy += val[k + 3];
                    break;
                case 'S':
                    reflectCtrl = lastCmd == 'c' || lastCmd == 's' || lastCmd == 'C' || lastCmd == 'S';
                    ctrl1X = reflectCtrl ? 2 * cx - cpx : cx;
                    ctrl1Y = reflectCtrl ? 2 * cy - cpy : cy;
                    path.cubicTo(ctrl1X, ctrl1Y,
                            val[k], val[k + 1],
                            val[k + 2], val[k + 3]);
                    cpx = val[k];
                    cpy = val[k + 1];
                    cx = val[k + 2];
                    cy = val[k + 3];
                    break;
                case 'q':
                    path.quadTo(cx + val[k], cy + val[k + 1],
                            cx + val[k + 2], cy + val[k + 3]);
                    cpx = cx + val[k];
                    cpy = cy + val[k + 1];
                    cx += val[k + 2];
                    cy += val[k + 3];
                    break;
                case 'Q':
                    path.quadTo(val[k], val[k + 1], val[k + 2], val[k + 3]);
                    cpx = val[k];
                    cpy = val[k + 1];
                    cx = val[k + 2];
                    cy = val[k + 3];
                    break;
                case 't':
                    reflectCtrl = lastCmd == 'q' || lastCmd == 't' || lastCmd == 'Q' || lastCmd == 'T';
                    tempReflectedX = reflectCtrl ? 2 * cx - cpx : cx;
                    tempReflectedY = reflectCtrl ? 2 * cy - cpy : cy;
                    path.quadTo(tempReflectedX, tempReflectedY, cx + val[k], cy + val[k + 1]);
                    cpx = tempReflectedX;
                    cpy = tempReflectedY;
                    cx += val[k];
                    cy += val[k + 1];
                    break;
                case 'T':
                    reflectCtrl = lastCmd == 'q' || lastCmd == 't' || lastCmd == 'Q' || lastCmd == 'T';
                    tempReflectedX = reflectCtrl ? 2 * cx - cpx : cx;
                    tempReflectedY = reflectCtrl ? 2 * cy - cpy : cy;
                    path.quadTo(tempReflectedX, tempReflectedY, val[k], val[k + 1]);
                    cpx = tempReflectedX;
                    cpy = tempReflectedY;
                    cx = val[k];
                    cy = val[k + 1];
                    break;
                case 'a':
                    drawArc(path, cx, cy,
                            val[k], val[k + 1], val[k + 2],
                            val[k + 3] != 0, val[k + 4] != 0,
                            cx + val[k + 5], cy + val[k + 6]);
                    cx += val[k + 5];
                    cy += val[k + 6];
                    break;
                case 'A':
                    drawArc(path, cx, cy,
                            val[k], val[k + 1], val[k + 2],
                            val[k + 3] != 0, val[k + 4] != 0,
                            val[k + 5], val[k + 6]);
                    cx = val[k + 5];
                    cy = val[k + 6];
                    break;
            }

            lastCmd = cmd;
        }

        current[0] = cx;
        current[1] = cy;
        current[2] = cpx;
        current[3] = cpy;
        current[4] = loopX;
        current[5] = loopY;
    }

    private static void drawArc(Path path,
                                float cx, float cy, float rx, float ry,
                                float angle, boolean largeArc, boolean sweep,
                                float x, float y) {

        if (rx == 0 || ry == 0) {
            path.lineTo(x, y);
            return;
        }

        double radAngle = Math.toRadians(angle % 360.0);
        double cosAngle = Math.cos(radAngle);
        double sinAngle = Math.sin(radAngle);

        double dx2 = (cx - x) / 2.0;
        double dy2 = (cy - y) / 2.0;

        double x1 = cosAngle * dx2 + sinAngle * dy2;
        double y1 = -sinAngle * dx2 + cosAngle * dy2;

        double rxSq = rx * rx;
        double rySq = ry * ry;
        double x1Sq = x1 * x1;
        double y1Sq = y1 * y1;

        double radiiCheck = x1Sq / rxSq + y1Sq / rySq;
        if (radiiCheck > 1) {
            rx *= Math.sqrt(radiiCheck);
            ry *= Math.sqrt(radiiCheck);
            rxSq = rx * rx;
            rySq = ry * ry;
        }

        double sign = (largeArc == sweep) ? -1 : 1;
        double sq = ((rxSq * rySq) - (rxSq * y1Sq) - (rySq * x1Sq)) /
                ((rxSq * y1Sq) + (rySq * x1Sq));
        sq = (sq < 0) ? 0 : sq;
        double coef = sign * Math.sqrt(sq);
        double cx1 = coef * ((rx * y1) / ry);
        double cy1 = coef * -((ry * x1) / rx);

        double centerX = cosAngle * cx1 - sinAngle * cy1 + (cx + x) / 2.0;
        double centerY = sinAngle * cx1 + cosAngle * cy1 + (cy + y) / 2.0;

        double ux = (x1 - cx1) / rx;
        double uy = (y1 - cy1) / ry;
        double vx = (-x1 - cx1) / rx;
        double vy = (-y1 - cy1) / ry;

        double startAngle = Math.toDegrees(Math.atan2(uy, ux));
        double sweepAngle = Math.toDegrees(Math.atan2(vy, vx)) - startAngle;
        if (!sweep && sweepAngle > 0) {
            sweepAngle -= 360.0;
        } else if (sweep && sweepAngle < 0) {
            sweepAngle += 360.0;
        }
        sweepAngle = Math.max(-360.0, Math.min(360.0, sweepAngle));

        arcToBezier(path, centerX, centerY, rx, ry, radAngle, startAngle, sweepAngle);
    }


    private static void arcToBezier(Path path,
                                    double cx, double cy, double rx, double ry,
                                    double theta, double startAngle, double sweepAngle) {

        int numSegments = (int) Math.ceil(Math.abs(sweepAngle) / 90.0);
        double eta = Math.toRadians(sweepAngle / numSegments);
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);
        double angle = Math.toRadians(startAngle);

        double prevX = cx + rx * Math.cos(angle) * cosTheta - ry * Math.sin(angle) * sinTheta;
        double prevY = cy + rx * Math.cos(angle) * sinTheta + ry * Math.sin(angle) * cosTheta;

        for (int i = 0; i < numSegments; i++) {
            double nextAngle = angle + eta;
            double cosAngle = Math.cos(angle);
            double sinAngle = Math.sin(angle);
            double cosNext = Math.cos(nextAngle);
            double sinNext = Math.sin(nextAngle);

            double alpha = Math.tan(eta / 2.0);
            double factor = (4.0 / 3.0) * alpha / (1 + Math.cos(eta / 2.0));

            double x1 = prevX;
            double y1 = prevY;

            double dx1 = rx * (-sinAngle) * factor;
            double dy1 = ry * cosAngle * factor;

            double dx2 = rx * (-sinNext) * factor;
            double dy2 = ry * cosNext * factor;

            double x2 = cx + (rx * cosNext) * cosTheta - (ry * sinNext) * sinTheta;
            double y2 = cy + (rx * cosNext) * sinTheta + (ry * sinNext) * cosTheta;

            float c1x = (float) (x1 + dx1 * cosTheta - dy1 * sinTheta);
            float c1y = (float) (y1 + dx1 * sinTheta + dy1 * cosTheta);
            float c2x = (float) (x2 + dx2 * cosTheta - dy2 * sinTheta);
            float c2y = (float) (y2 + dx2 * sinTheta + dy2 * cosTheta);

            path.cubicTo(c1x, c1y, c2x, c2y, (float) x2, (float) y2);

            angle = nextAngle;
            prevX = x2;
            prevY = y2;
        }
    }


}



///*
// * Copyright (C) 2015 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.android.ide.common.vectordrawable;
//
//import java.awt.geom.Path2D;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * Given an array of VdPath.Node, generate a Path2D object.
// * In another word, this is the engine which converts the pathData into
// * a Path2D object, which is able to draw on Swing components.
// * The logic and math here are the same as PathParser.java in framework.
// */
//class VdNodeRender {
//    private static Logger logger = Logger.getLogger(VdNodeRender.class
//            .getSimpleName());
//
//    public static void creatPath(VdPath.Node[] node, Path2D path) {
//        float[] current = new float[6];
//        char lastCmd = ' ';
//        for (int i = 0; i < node.length; i++) {
//            addCommand(path, current, node[i].type, lastCmd,node[i].params);
//            lastCmd = node[i].type;
//        }
//    }
//
//    private static void addCommand(Path2D path, float[] current, char cmd,
//            char lastCmd, float[] val) {
//
//        int incr = 2;
//
//        float cx = current[0];
//        float cy = current[1];
//        float cpx = current[2];
//        float cpy = current[3];
//        float loopX = current[4];
//        float loopY = current[5];
//
//        switch (cmd) {
//            case 'z':
//            case 'Z':
//                path.closePath();
//                cx = loopX;
//                cy = loopY;
//            case 'm':
//            case 'M':
//            case 'l':
//            case 'L':
//            case 't':
//            case 'T':
//                incr = 2;
//                break;
//            case 'h':
//            case 'H':
//            case 'v':
//            case 'V':
//                incr = 1;
//                break;
//            case 'c':
//            case 'C':
//                incr = 6;
//                break;
//            case 's':
//            case 'S':
//            case 'q':
//            case 'Q':
//                incr = 4;
//                break;
//            case 'a':
//            case 'A':
//                incr = 7;
//        }
//
//        for (int k = 0; k < val.length; k += incr) {
//            boolean reflectCtrl = false;
//            float tempReflectedX, tempReflectedY;
//
//            switch (cmd) {
//                case 'm':
//                    cx += val[k + 0];
//                    cy += val[k + 1];
//                    path.moveTo(cx, cy);
//                    loopX = cx;
//                    loopY = cy;
//                    break;
//                case 'M':
//                    cx = val[k + 0];
//                    cy = val[k + 1];
//                    path.moveTo(cx, cy);
//                    loopX = cx;
//                    loopY = cy;
//                    break;
//                case 'l':
//                    cx += val[k + 0];
//                    cy += val[k + 1];
//                    path.lineTo(cx, cy);
//                    break;
//                case 'L':
//                    cx = val[k + 0];
//                    cy = val[k + 1];
//                    path.lineTo(cx, cy);
//                    break;
//                case 'z':
//                case 'Z':
//                    path.closePath();
//                    cx = loopX;
//                    cy = loopY;
//                    break;
//                case 'h':
//                    cx += val[k + 0];
//                    path.lineTo(cx, cy);
//                    break;
//                case 'H':
//                    path.lineTo(val[k + 0], cy);
//                    cx = val[k + 0];
//                    break;
//                case 'v':
//                    cy += val[k + 0];
//                    path.lineTo(cx, cy);
//                    break;
//                case 'V':
//                    path.lineTo(cx, val[k + 0]);
//                    cy = val[k + 0];
//                    break;
//                case 'c':
//                    path.curveTo(cx + val[k + 0], cy + val[k + 1], cx + val[k + 2],
//                            cy + val[k + 3], cx + val[k + 4], cy + val[k + 5]);
//                    cpx = cx + val[k + 2];
//                    cpy = cy + val[k + 3];
//                    cx += val[k + 4];
//                    cy += val[k + 5];
//                    break;
//                case 'C':
//                    path.curveTo(val[k + 0], val[k + 1], val[k + 2], val[k + 3],
//                            val[k + 4], val[k + 5]);
//                    cx = val[k + 4];
//                    cy = val[k + 5];
//                    cpx = val[k + 2];
//                    cpy = val[k + 3];
//                    break;
//                case 's':
//                    reflectCtrl = (lastCmd == 'c' || lastCmd == 's' || lastCmd == 'C' || lastCmd == 'S');
//                    path.curveTo(reflectCtrl ? 2 * cx - cpx : cx, reflectCtrl ? 2
//                            * cy - cpy : cy, cx + val[k + 0], cy + val[k + 1], cx
//                            + val[k + 2], cy + val[k + 3]);
//
//                    cpx = cx + val[k + 0];
//                    cpy = cy + val[k + 1];
//                    cx += val[k + 2];
//                    cy += val[k + 3];
//                    break;
//                case 'S':
//                    reflectCtrl = (lastCmd == 'c' || lastCmd == 's' || lastCmd == 'C' || lastCmd == 'S');
//                    path.curveTo(reflectCtrl ? 2 * cx - cpx : cx, reflectCtrl ? 2
//                            * cy - cpy : cy, val[k + 0], val[k + 1], val[k + 2],
//                            val[k + 3]);
//                    cpx = (val[k + 0]);
//                    cpy = (val[k + 1]);
//                    cx = val[k + 2];
//                    cy = val[k + 3];
//                    break;
//                case 'q':
//                    path.quadTo(cx + val[k + 0], cy + val[k + 1], cx + val[k + 2],
//                            cy + val[k + 3]);
//                    cpx = cx + val[k + 0];
//                    cpy = cy + val[k + 1];
//                    // Note that we have to update cpx first, since cx will be updated here.
//                    cx += val[k + 2];
//                    cy += val[k + 3];
//                    break;
//                case 'Q':
//                    path.quadTo(val[k + 0], val[k + 1], val[k + 2], val[k + 3]);
//                    cx = val[k + 2];
//                    cy = val[k + 3];
//                    cpx = val[k + 0];
//                    cpy = val[k + 1];
//                    break;
//                case 't':
//                    reflectCtrl = (lastCmd == 'q' || lastCmd == 't' || lastCmd == 'Q' || lastCmd == 'T');
//                    tempReflectedX = reflectCtrl ? 2 * cx - cpx : cx;
//                    tempReflectedY = reflectCtrl ? 2 * cy - cpy : cy;
//                    path.quadTo(tempReflectedX, tempReflectedY, cx + val[k + 0], cy + val[k + 1]);
//                    cpx = tempReflectedX;
//                    cpy = tempReflectedY;
//                    cx += val[k + 0];
//                    cy += val[k + 1];
//                    break;
//                case 'T':
//                    reflectCtrl = (lastCmd == 'q' || lastCmd == 't' || lastCmd == 'Q' || lastCmd == 'T');
//                    tempReflectedX = reflectCtrl ? 2 * cx - cpx : cx;
//                    tempReflectedY = reflectCtrl ? 2 * cy - cpy : cy;
//                    path.quadTo(tempReflectedX, tempReflectedY, val[k + 0], val[k + 1]);
//                    cx = val[k + 0];
//                    cy = val[k + 1];
//                    cpx = tempReflectedX;
//                    cpy = tempReflectedY;
//                    break;
//                case 'a':
//                    // (rx ry x-axis-rotation large-arc-flag sweep-flag x y)
//                    drawArc(path, cx, cy, val[k + 5] + cx, val[k + 6] + cy,
//                            val[k + 0], val[k + 1], val[k + 2], val[k + 3] != 0,
//                                    val[k + 4] != 0);
//                    cx += val[k + 5];
//                    cy += val[k + 6];
//                    cpx = cx;
//                    cpy = cy;
//
//                    break;
//                case 'A':
//                    drawArc(path, cx, cy, val[k + 5], val[k + 6], val[k + 0],
//                            val[k + 1], val[k + 2], val[k + 3] != 0,
//                                    val[k + 4] != 0);
//                    cx = val[k + 5];
//                    cy = val[k + 6];
//                    cpx = cx;
//                    cpy = cy;
//                    break;
//
//            }
//            lastCmd = cmd;
//        }
//        current[0] = cx;
//        current[1] = cy;
//        current[2] = cpx;
//        current[3] = cpy;
//        current[4] = loopX;
//        current[5] = loopY;
//
//    }
//
//    private static void drawArc(Path2D p, float x0, float y0, float x1,
//            float y1, float a, float b, float theta, boolean isMoreThanHalf,
//            boolean isPositiveArc) {
//
//        logger.log(Level.FINE, "(" + x0 + "," + y0 + ")-(" + x1 + "," + y1
//                + ") {" + a + " " + b + "}");
//        /* Convert rotation angle from degrees to radians */
//        double thetaD = theta * Math.PI / 180.0f;
//        /* Pre-compute rotation matrix entries */
//        double cosTheta = Math.cos(thetaD);
//        double sinTheta = Math.sin(thetaD);
//        /* Transform (x0, y0) and (x1, y1) into unit space */
//        /* using (inverse) rotation, followed by (inverse) scale */
//        double x0p = (x0 * cosTheta + y0 * sinTheta) / a;
//        double y0p = (-x0 * sinTheta + y0 * cosTheta) / b;
//        double x1p = (x1 * cosTheta + y1 * sinTheta) / a;
//        double y1p = (-x1 * sinTheta + y1 * cosTheta) / b;
//        logger.log(Level.FINE, "unit space (" + x0p + "," + y0p + ")-(" + x1p
//                + "," + y1p + ")");
//        /* Compute differences and averages */
//        double dx = x0p - x1p;
//        double dy = y0p - y1p;
//        double xm = (x0p + x1p) / 2;
//        double ym = (y0p + y1p) / 2;
//        /* Solve for intersecting unit circles */
//        double dsq = dx * dx + dy * dy;
//        if (dsq == 0.0) {
//            logger.log(Level.FINE, " Points are coincident");
//            return; /* Points are coincident */
//        }
//        double disc = 1.0 / dsq - 1.0 / 4.0;
//        if (disc < 0.0) {
//            logger.log(Level.FINE, "Points are too far apart " + dsq);
//            float adjust = (float) (Math.sqrt(dsq) / 1.99999);
//            drawArc(p, x0, y0, x1, y1, a * adjust, b * adjust, theta,
//                    isMoreThanHalf, isPositiveArc);
//            return; /* Points are too far apart */
//        }
//        double s = Math.sqrt(disc);
//        double sdx = s * dx;
//        double sdy = s * dy;
//        double cx;
//        double cy;
//        if (isMoreThanHalf == isPositiveArc) {
//            cx = xm - sdy;
//            cy = ym + sdx;
//        } else {
//            cx = xm + sdy;
//            cy = ym - sdx;
//        }
//
//        double eta0 = Math.atan2((y0p - cy), (x0p - cx));
//        logger.log(Level.FINE, "eta0 = Math.atan2( " + (y0p - cy) + " , "
//                + (x0p - cx) + ") = " + Math.toDegrees(eta0));
//
//        double eta1 = Math.atan2((y1p - cy), (x1p - cx));
//        logger.log(Level.FINE, "eta1 = Math.atan2( " + (y1p - cy) + " , "
//                + (x1p - cx) + ") = " + Math.toDegrees(eta1));
//        double sweep = (eta1 - eta0);
//        if (isPositiveArc != (sweep >= 0)) {
//            if (sweep > 0) {
//                sweep -= 2 * Math.PI;
//            } else {
//                sweep += 2 * Math.PI;
//            }
//        }
//
//        cx *= a;
//        cy *= b;
//        double tcx = cx;
//        cx = cx * cosTheta - cy * sinTheta;
//        cy = tcx * sinTheta + cy * cosTheta;
//        logger.log(
//                Level.FINE,
//                        "cx, cy, a, b, x0, y0, thetaD, eta0, sweep = " + cx + " , "
//                        + cy + " , " + a + " , " + b + " , " + x0 + " , " + y0
//                        + " , " + Math.toDegrees(thetaD) + " , "
//                        + Math.toDegrees(eta0) + " , " + Math.toDegrees(sweep));
//
//        arcToBezier(p, cx, cy, a, b, x0, y0, thetaD, eta0, sweep);
//    }
//
//    /**
//     * Converts an arc to cubic Bezier segments and records them in p.
//     *
//     * @param p The target for the cubic Bezier segments
//     * @param cx The x coordinate center of the ellipse
//     * @param cy The y coordinate center of the ellipse
//     * @param a The radius of the ellipse in the horizontal direction
//     * @param b The radius of the ellipse in the vertical direction
//     * @param e1x E(eta1) x coordinate of the starting point of the arc
//     * @param e1y E(eta2) y coordinate of the starting point of the arc
//     * @param theta The angle that the ellipse bounding rectangle makes with the horizontal plane
//     * @param start The start angle of the arc on the ellipse
//     * @param sweep The angle (positive or negative) of the sweep of the arc on the ellipse
//     */
//    private static void arcToBezier(Path2D p, double cx, double cy, double a,
//            double b, double e1x, double e1y, double theta, double start,
//            double sweep) {
//        // Taken from equations at:
//        // http://spaceroots.org/documents/ellipse/node8.html
//        // and http://www.spaceroots.org/documents/ellipse/node22.html
//
//        // Maximum of 45 degrees per cubic Bezier segment
//        int numSegments = Math.abs((int) Math.ceil(sweep * 4 / Math.PI));
//
//        double eta1 = start;
//        double cosTheta = Math.cos(theta);
//        double sinTheta = Math.sin(theta);
//        double cosEta1 = Math.cos(eta1);
//        double sinEta1 = Math.sin(eta1);
//        double ep1x = (-a * cosTheta * sinEta1) - (b * sinTheta * cosEta1);
//        double ep1y = (-a * sinTheta * sinEta1) + (b * cosTheta * cosEta1);
//
//        double anglePerSegment = sweep / numSegments;
//        for (int i = 0; i < numSegments; i++) {
//            double eta2 = eta1 + anglePerSegment;
//            double sinEta2 = Math.sin(eta2);
//            double cosEta2 = Math.cos(eta2);
//            double e2x = cx + (a * cosTheta * cosEta2)
//                    - (b * sinTheta * sinEta2);
//            double e2y = cy + (a * sinTheta * cosEta2)
//                    + (b * cosTheta * sinEta2);
//            double ep2x = -a * cosTheta * sinEta2 - b * sinTheta * cosEta2;
//            double ep2y = -a * sinTheta * sinEta2 + b * cosTheta * cosEta2;
//            double tanDiff2 = Math.tan((eta2 - eta1) / 2);
//            double alpha = Math.sin(eta2 - eta1)
//                    * (Math.sqrt(4 + (3 * tanDiff2 * tanDiff2)) - 1) / 3;
//            double q1x = e1x + alpha * ep1x;
//            double q1y = e1y + alpha * ep1y;
//            double q2x = e2x - alpha * ep2x;
//            double q2y = e2y - alpha * ep2y;
//
//            p.curveTo((float) q1x, (float) q1y, (float) q2x, (float) q2y,
//                    (float) e2x, (float) e2y);
//            eta1 = eta2;
//            e1x = e2x;
//            e1y = e2y;
//            ep1x = ep2x;
//            ep1y = ep2y;
//        }
//    }
//
//}
