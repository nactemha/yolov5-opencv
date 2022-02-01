/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.ahmetcan.yolov5_opencv.tracking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;

import com.ahmetcan.yolov5_opencv.env.BorderedText;
import com.ahmetcan.yolov5_opencv.env.ImageUtils;
import com.ahmetcan.yolov5_opencv.env.Logger;
import com.ahmetcan.yolov5_opencv.tflite.Detection;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/** A tracker that handles non-max suppression and matches existing objects to new detections. */
public class MultiBoxTracker {
  private static final float TEXT_SIZE_DIP = 18;
  private static final float MIN_SIZE = 16.0f;
  private static final int[] COLORS = {
          Color.BLUE,
          Color.RED,
          Color.GREEN,
          Color.YELLOW,
          Color.CYAN,
          Color.MAGENTA,
          Color.WHITE,
          Color.parseColor("#55FF55"),
          Color.parseColor("#FFA500"),
          Color.parseColor("#FF8888"),
          Color.parseColor("#AAAAFF"),
          Color.parseColor("#FFFFAA"),
          Color.parseColor("#55AAAA"),
          Color.parseColor("#AA33AA"),
          Color.parseColor("#0D0068")
  };
  final List<Pair<Float, RectF>> screenRects = new LinkedList<Pair<Float, RectF>>();
  private final Logger logger = new Logger();
  private final Queue<Integer> availableColors = new LinkedList<Integer>();
  private final Paint boxPaint1 = new Paint();
  private final Paint boxPaint2 = new Paint();
  private final float textSizePx;
  private final BorderedText borderedText;
  private Matrix frameToCanvasMatrix;
  private int frameWidth;
  private int frameHeight;
  private int sensorOrientation;

  public MultiBoxTracker(final Context context) {
    for (final int color : COLORS) {
      availableColors.add(color);
    }

    boxPaint1.setColor(Color.RED);
    boxPaint1.setStyle(Style.STROKE);
    boxPaint1.setStrokeWidth(10.0f);
    boxPaint1.setStrokeCap(Cap.ROUND);
    boxPaint1.setStrokeJoin(Join.ROUND);
    boxPaint1.setStrokeMiter(100);

    boxPaint2.setColor(Color.RED);
    boxPaint2.setStyle(Style.STROKE);
    boxPaint2.setStrokeWidth(10.0f);
    boxPaint2.setStrokeCap(Cap.ROUND);
    boxPaint2.setStrokeJoin(Join.ROUND);
    boxPaint2.setStrokeMiter(100);

    textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, context.getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
  }

  public synchronized void setFrameConfiguration(
          final int width, final int height, final int sensorOrientation) {
    frameWidth = width;
    frameHeight = height;
    this.sensorOrientation = sensorOrientation;
  }


  private Matrix getFrameToCanvasMatrix() {
    return frameToCanvasMatrix;
  }

  public synchronized void draw(final Canvas canvas,final List<TrackerObject> trackedObjects,final List<Detection> detections) {
    final boolean rotated = sensorOrientation % 180 == 90;
    final float multiplier =
            Math.min(
                    canvas.getHeight() / (float) (rotated ? frameWidth : frameHeight),
                    canvas.getWidth() / (float) (rotated ? frameHeight : frameWidth));
    frameToCanvasMatrix =
            ImageUtils.getTransformationMatrix(
                    frameWidth,
                    frameHeight,
                    (int) (multiplier * (rotated ? frameHeight : frameWidth)),
                    (int) (multiplier * (rotated ? frameWidth : frameHeight)),
                    sensorOrientation,
                    false);
    for (final Detection detection : detections) {
      final RectF trackedPos1 = new RectF(detection.getDisplayLoc());

      getFrameToCanvasMatrix().mapRect(trackedPos1);
      int color1 = COLORS[detection.getDetectedClass() % COLORS.length ];

      boxPaint1.setColor(color1);

      float cornerSize1 = Math.min(trackedPos1.width(), trackedPos1.height()) / 8.0f;
      //canvas.drawRoundRect(trackedPos1, cornerSize1, cornerSize1, boxPaint1);

      final String labelString1 = String.format("%s: %s %.2f",  detection.getId(),detection.getTitle(), (100 * detection.getConfidence()));

      // labelString);
     // borderedText.drawText(canvas, trackedPos1.left + cornerSize1, trackedPos1.top, labelString1 + "%", boxPaint1);
    }
    for (final TrackerObject trackerObject : trackedObjects) {
      final RectF trackedPos1 = new RectF(trackerObject.getDisplayLoc());
     // final RectF trackedPos2 = new RectF(trackerObject.detection.getDisplayLoc());

      getFrameToCanvasMatrix().mapRect(trackedPos1);
     // getFrameToCanvasMatrix().mapRect(trackedPos2);
      int color1 = COLORS[trackerObject.detection.getDetectedClass() % COLORS.length +1];
      //int color2 = COLORS[trackerObject.detection.getDetectedClass() % COLORS.length +2];

      boxPaint1.setColor(color1);
     // boxPaint2.setColor(color2);

      float cornerSize1 = Math.min(trackedPos1.width(), trackedPos1.height()) / 8.0f;
    //  float cornerSize2 = Math.min(trackedPos2.width(), trackedPos2.height()) / 8.0f;
      canvas.drawRoundRect(trackedPos1, cornerSize1, cornerSize1, boxPaint1);
    //  canvas.drawRoundRect(trackedPos2, cornerSize2, cornerSize2, boxPaint2);

      final String labelString1 = String.format("%s: %s %.2f",  trackerObject.getId(),trackerObject.detection.getTitle(), (100 * trackerObject.detection.getConfidence()));
    //  final String labelString2 =String.format("%s %.2f", trackerObject.detection.getTitle(), (100 * trackerObject.detection.getConfidence()));

      // labelString);
      borderedText.drawText(canvas, trackedPos1.left + cornerSize1, trackedPos1.top, labelString1 + "%", boxPaint1);
      //borderedText.drawText(canvas, trackedPos2.left + cornerSize2, trackedPos2.top, labelString2 + "%", boxPaint2);
    }

  }

}
