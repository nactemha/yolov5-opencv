package com.ahmetcan.yolov5_opencv.tracking

import android.graphics.Bitmap
import android.graphics.RectF
import com.ahmetcan.yolov5_opencv.tflite.Detection
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Rect2d
import org.opencv.imgproc.Imgproc
import org.opencv.tracking.*
import org.opencv.video.Tracker


class TrackerObject {
    private var ocv_legacy_tracker:legacy_Tracker?=null
    private var ocv_tracker:Tracker?=null
    private var current_legacy:Rect2d= Rect2d()
    private var current:Rect=Rect()
    var id:String=""
    var trackFailCount:Int=0
    private var lastDetectionTime:Long=0

    lateinit var detection:Detection
    var displayLoc=RectF(0f,0f,0f,0f);

    //image:Bitmap format RGBA_8888 olarak kabul edilecek
    constructor(id:String,trackerType:String="TrackerKCF"){
        this.id=id;

        ocv_tracker = when(trackerType){
            "TrackerKCF"-> TrackerKCF.create()
            "TrackerCSRT"-> TrackerCSRT.create()
            else -> null
        }
        if(ocv_tracker==null){
            ocv_legacy_tracker = when(trackerType){
                "legacy_TrackerKCF"-> legacy_TrackerKCF.create()
                "legacy_TrackerCSRT"-> legacy_TrackerCSRT.create()
                "legacy_TrackerBoosting"-> legacy_TrackerBoosting.create()
                "legacy_TrackerMedianFlow"-> legacy_TrackerMedianFlow.create()
                "legacy_TrackerMIL"-> legacy_TrackerMIL.create()
                "legacy_TrackerMOSSE"-> legacy_TrackerMOSSE.create()
                "legacy_TrackerTLD"-> legacy_TrackerTLD.create()
                else -> null
            }
        }



    }
    fun updateLastDetectionTime(){
        lastDetectionTime=System.currentTimeMillis()
    }
    fun getDetectionElapsedTime(): Long {
        return System.currentTimeMillis()-lastDetectionTime
    }
    fun init(image:Bitmap,bbox:BoundingBox,detection: Detection){
        var argb=convertBitmapToMap(image)
        this.detection=detection

        ocv_tracker?.apply {

            init(argb,Rect(bbox.left.toInt(),bbox.top.toInt(),bbox.width().toInt(),bbox.height().toInt()))
        }
        ocv_legacy_tracker?.apply {
            init(argb, Rect2d(bbox.left.toDouble(),bbox.top.toDouble(),bbox.width().toDouble(),bbox.height().toDouble()))
        }
    }

    private fun convertBitmapToMap(image:Bitmap):Mat{
        if(!(image.config==Bitmap.Config.ARGB_8888 || image.config==Bitmap.Config.RGB_565)){
            throw  IllegalArgumentException("bitmap type must be ARGB_8888 or RGB_565");
        }
        val mat = Mat()
        //val bmp32: Bitmap = image.copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(image, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);


        return mat
    }


    fun Update(image: Bitmap):Boolean{
        var argb=convertBitmapToMap(image);
        ocv_tracker?.apply {
            return update(argb,current)
        }
        ocv_legacy_tracker?.apply {
            return update(argb,current_legacy)
        }
        return  false
    }
    fun getBoundingBox():BoundingBox{
        if(ocv_tracker!=null){
            return BoundingBox(current.x.toFloat(),current.y.toFloat(),(current.x.toFloat()+current.width.toFloat()),(current.y.toFloat()+current.height.toFloat()))
        }
        if(ocv_legacy_tracker!=null){
            return BoundingBox(current_legacy.x.toFloat(),current_legacy.y.toFloat(),(current_legacy.x.toFloat()+current_legacy.width.toFloat()),(current_legacy.y.toFloat()+current_legacy.height.toFloat()))
        }
        return BoundingBox(-1f,-1f,-1f,-1f)
    }
}