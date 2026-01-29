
package io.github.yzjdev.svg2vector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VdTree {
    private static Logger logger = Logger.getLogger(VdTree.class.getSimpleName());

    VdGroup mCurrentGroup = new VdGroup();
    ArrayList<VdElement> mChildren;

    float mBaseWidth = 1;
    float mBaseHeight = 1;
    float mPortWidth = 1;
    float mPortHeight = 1;
    float mRootAlpha = 1;

    // Finish parsing the XML and group all elements
    public void parseFinish() {
        mChildren = mCurrentGroup.getChildren();
    }

    // Add paths or groups to the current group
    public void add(VdElement pathOrGroup) {
        mCurrentGroup.add(pathOrGroup);
    }

    // Get the base width
    public float getBaseWidth() {
        return mBaseWidth;
    }

    // Get the base height
    public float getBaseHeight() {
        return mBaseHeight;
    }

    // Internal drawing method for all elements
    private void drawInternal(Canvas canvas, int w, int h) {
        float scaleX = w / mPortWidth;
        float scaleY = h / mPortHeight;
        float minScale = Math.min(scaleX, scaleY);

        if (mChildren == null) {
            logger.log(Level.FINE, "no paths");
            return;
        }

        canvas.scale(scaleX, scaleY);

        Rect bounds = null;
        for (int i = 0; i < mChildren.size(); i++) {
            VdPath path = (VdPath) mChildren.get(i);
            logger.log(Level.FINE, "mCurrentPaths[" + i + "]=" + path.getName() +
                    Integer.toHexString(path.mFillColor));
            if (mChildren.get(i) != null) {
                Rect r = drawPath(path, canvas, w, h, minScale);
                if (bounds == null) {
                    bounds = r;
                } else {
                    bounds.union(r);
                }
            }
        }

        logger.log(Level.FINE, "Rectangle " + bounds);
        logger.log(Level.FINE, "Port  " + mPortWidth + "," + mPortHeight);
    }

    // Draw a single path into the canvas
    private Rect drawPath(VdPath path, Canvas canvas, int w, int h, float scale) {
        Path path2d = new Path();
        Paint paint = new Paint();
        path.toPath(path2d);

        // Apply rotation transformation
        Matrix matrix = new Matrix();
        matrix.setRotate(path.mRotate, path.mRotateX, path.mRotateY);
        canvas.concat(matrix);

        if (path.mClip) {
            logger.log(Level.FINE, "CLIP");
            paint.setColor(Color.RED);
            canvas.drawPath(path2d, paint);
        }

        if (path.mFillColor != 0) {
            paint.setAntiAlias(true);
            paint.setColor(path.mFillColor);
            canvas.drawPath(path2d, paint);
        }

        if (path.mStrokeColor != 0) {
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(path.mStrokeWidth);
            paint.setColor(path.mStrokeColor);
            canvas.drawPath(path2d, paint);
        }

        // Reverse rotation
        matrix.setRotate(-path.mRotate, path.mRotateX, path.mRotateY);
        canvas.concat(matrix);

        // Return the bounds of the drawn path
        RectF bounds = new RectF();
        path2d.computeBounds(bounds, true);
        return new Rect((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom);
    }

    // Draw the VdTree into an image
    public void drawIntoImage(Bitmap image) {
        Canvas canvas = new Canvas(image);
        int width = image.getWidth();
        int height = image.getHeight();

        Paint paint = new Paint();
        paint.setColor(Color.TRANSPARENT);
        canvas.drawRect(0, 0, width, height, paint);

        float rootAlpha = mRootAlpha;
        if (rootAlpha < 1.0) {
            Bitmap alphaImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(alphaImage);
            drawInternal(tempCanvas, width, height);

            paint.setAlpha((int) (rootAlpha * 255));
            canvas.drawBitmap(alphaImage, 0, 0, paint);
        } else {
            drawInternal(canvas, width, height);
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
//import com.android.ide.common.util.AssetUtil;
//
//import java.awt.*;
//import java.awt.geom.Path2D;
//import java.awt.image.BufferedImage;
//import java.util.ArrayList;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * Used to represent the whole VectorDrawable XML file's tree.
// */
//class VdTree {
//    private static Logger logger = Logger.getLogger(VdTree.class.getSimpleName());
//
//    VdGroup mCurrentGroup = new VdGroup();
//    ArrayList<VdElement> mChildren;
//
//    float mBaseWidth = 1;
//    float mBaseHeight = 1;
//    float mPortWidth = 1;
//    float mPortHeight = 1;
//    float mRootAlpha = 1;
//
//    /**
//     * Ensure there is at least one animation for every path in group (linking
//     * them by names) Build the "current" path based on the first group
//     */
//    void parseFinish() {
//        mChildren = mCurrentGroup.getChildren();
//    }
//
//    void add(VdElement pathOrGroup) {
//        mCurrentGroup.add(pathOrGroup);
//    }
//
//    float getBaseWidth(){
//        return mBaseWidth;
//    }
//
//    float getBaseHeight(){
//        return mBaseHeight;
//    }
//
//    private void drawInternal(Graphics g, int w, int h) {
//        float scaleX = w / mPortWidth;
//        float scaleY = h / mPortHeight;
//        float minScale = Math.min(scaleX, scaleY);
//
//        if (mChildren == null) {
//            logger.log(Level.FINE, "no pathes");
//            return;
//        }
//        ((Graphics2D) g).scale(scaleX, scaleY);
//
//        Rectangle bounds = null;
//        for (int i = 0; i < mChildren.size(); i++) {
//            // TODO: do things differently when it is a path or group!!
//            VdPath path = (VdPath) mChildren.get(i);
//            logger.log(Level.FINE, "mCurrentPaths[" + i + "]=" + path.getName() +
//                                   Integer.toHexString(path.mFillColor));
//            if (mChildren.get(i) != null) {
//                Rectangle r = drawPath(path, g, w, h, minScale);
//                if (bounds == null) {
//                    bounds = r;
//                } else {
//                    bounds.add(r);
//                }
//            }
//        }
//        logger.log(Level.FINE, "Rectangle " + bounds);
//        logger.log(Level.FINE, "Port  " + mPortWidth + "," + mPortHeight);
//        double right = mPortWidth - bounds.getMaxX();
//        double bot = mPortHeight - bounds.getMaxY();
//        logger.log(Level.FINE, "x " + bounds.getMinX() + ", " + right);
//        logger.log(Level.FINE, "y " + bounds.getMinY() + ", " + bot);
//    }
//
//    private Rectangle drawPath(VdPath path, Graphics canvas, int w, int h, float scale) {
//
//        Path2D path2d = new Path2D.Double();
//        Graphics2D g = (Graphics2D) canvas;
//        path.toPath(path2d);
//
//        // TODO: Use AffineTransform to apply group's transformation info.
//        double theta = Math.toRadians(path.mRotate);
//        g.rotate(theta, path.mRotateX, path.mRotateY);
//        if (path.mClip) {
//            logger.log(Level.FINE, "CLIP");
//
//            g.setColor(Color.RED);
//            g.fill(path2d);
//
//        }
//        if (path.mFillColor != 0) {
//            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g.setColor(new Color(path.mFillColor, true));
//            g.fill(path2d);
//        }
//        if (path.mStrokeColor != 0) {
//            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g.setStroke(new BasicStroke(path.mStrokeWidth));
//            g.setColor(new Color(path.mStrokeColor, true));
//            g.draw(path2d);
//        }
//
//        g.rotate(-theta, path.mRotateX, path.mRotateY);
//        return path2d.getBounds();
//    }
//
//    /**
//     * Draw the VdTree into an image.
//     * If the root alpha is less than 1.0, then draw into a temporary image,
//     * then draw into the result image applying alpha blending.
//     */
//    public void drawIntoImage(BufferedImage image) {
//        Graphics2D gFinal = (Graphics2D) image.getGraphics();
//        int width = image.getWidth();
//        int height = image.getHeight();
//        gFinal.setColor(new Color(255, 255, 255, 0));
//        gFinal.fillRect(0, 0, width, height);
//
//        float rootAlpha = mRootAlpha;
//        if (rootAlpha < 1.0) {
//            BufferedImage alphaImage = AssetUtil.newArgbBufferedImage(width, height);
//            Graphics2D gTemp = (Graphics2D)alphaImage.getGraphics();
//            drawInternal(gTemp, width, height);
//            gFinal.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rootAlpha));
//            gFinal.drawImage(alphaImage, 0, 0, null);
//            gTemp.dispose();
//        } else {
//            drawInternal(gFinal, width, height);
//        }
//        gFinal.dispose();
//    }
//}
