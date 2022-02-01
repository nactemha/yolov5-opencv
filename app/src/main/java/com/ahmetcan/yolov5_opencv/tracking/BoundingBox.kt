package com.ahmetcan.yolov5_opencv.tracking

import android.graphics.RectF

class BoundingBox:RectF {

    constructor(x1:Float,y1:Float,x2:Float,y2:Float): super(x1,y1,x2,y2){


    }

    companion object{

        fun box_iou(a: RectF, b: RectF): Float {
            return box_intersection(a, b) / box_union(a, b)
        }
        protected fun box_intersection(
            a: RectF,
            b: RectF
        ): Float {
            val w = overlap(
                (a.left + a.right) / 2, a.right - a.left,
                (b.left + b.right) / 2, b.right - b.left
            )
            val h = overlap(
                (a.top + a.bottom) / 2, a.bottom - a.top,
                (b.top + b.bottom) / 2, b.bottom - b.top
            )
            return if (w < 0 || h < 0) 0f else w * h
        }

        protected fun box_union(a: RectF, b: RectF): Float {
            val i = box_intersection(a, b)
            return (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i
        }

        protected fun overlap(x1: Float, w1: Float, x2: Float, w2: Float): Float {
            val l1 = x1 - w1 / 2
            val l2 = x2 - w2 / 2
            val left = if (l1 > l2) l1 else l2
            val r1 = x1 + w1 / 2
            val r2 = x2 + w2 / 2
            val right = if (r1 < r2) r1 else r2
            return right - left
        }
    }

}