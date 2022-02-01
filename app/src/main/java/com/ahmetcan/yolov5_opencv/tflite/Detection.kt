package com.ahmetcan.yolov5_opencv.tflite

import android.graphics.RectF

class Detection {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    var id: String

    /**
     * Display name for the recognition.
     */
    var title: String

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    var confidence: Float=0f

    /**
     * Optional location within the source image for the location of the recognized object.
     */
    lateinit var location: RectF
    var displayLoc: RectF=RectF()
    var detectedClass = 0

    constructor(
        id: String, title: String, confidence: Float, location: RectF
    ) {
        this.id = id
        this.title = title
        this.confidence = confidence
        this.location = location
    }

    constructor(
        id: String,
        title: String,
        confidence: Float,
        location: RectF,
        detectedClass: Int
    ) {
        this.id = id
        this.title = title
        this.confidence = confidence
        this.location = location
        this.detectedClass = detectedClass
    }

    override fun toString(): String {
        var resultString = ""
        if (id != null) {
            resultString += "[$id] "
        }
        if (title != null) {
            resultString += "$title "
        }
        if (confidence != null) {
            resultString += String.format("(%.1f%%) ", confidence * 100.0f)
        }
        if (location != null) {
            resultString += location.toString() + " "
        }
        return resultString.trim { it <= ' ' }
    }
}