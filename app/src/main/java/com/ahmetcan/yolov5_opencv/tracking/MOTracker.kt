package com.ahmetcan.yolov5_opencv.tracking

import android.graphics.Bitmap
import android.util.Log
import com.ahmetcan.yolov5_opencv.tflite.Detection

class MOTracker {

    var trackers= mutableListOf<TrackerObject>()
    var id_counter:Int=0

    fun new_detection(image:Bitmap,x1:Float,y1:Float,x2:Float,y2:Float,detection:Detection,trackerType:String="TrackerKCF"): Boolean {
        id_counter++;
        var bbox=BoundingBox(x1,y1,x2,y2)
        var trackersWithCost= calc_iot(bbox,0.1f)


        if(trackersWithCost.size==0){
            Log.w("actrace","iou_match=null:${detection.title} rect:${bbox.left},${bbox.top} ${bbox.right} ${bbox.bottom}")

            var newTracker=TrackerObject(id_counter.toString(),trackerType)
            newTracker.updateLastDetectionTime()
            newTracker.init(image,bbox,detection)
            trackers.add(newTracker)
            return true
        }
        else{
            var obviousTracker=trackersWithCost[0].second
            if(obviousTracker.detection.confidence<detection.confidence){
                //Log.w("actrace","iou_match ----------------------------------------: ${obviousTracker.detection.title} rect:${obviousTracker.detection.getLocation().left},${obviousTracker.detection.getLocation().top} ${obviousTracker.detection.getLocation().right} ${obviousTracker.detection.getLocation().bottom} new detection ${detection.title} rect:${bbox.left},${bbox.top} ${bbox.right} ${bbox.bottom}")
                obviousTracker.updateLastDetectionTime()
                obviousTracker.init(image,bbox,detection)
            }
            else{
                obviousTracker.trackFailCount++;
                if(obviousTracker.trackFailCount>3){
                    obviousTracker.updateLastDetectionTime()
                    obviousTracker.init(image,bbox,detection)
                    obviousTracker.trackFailCount=0
                }
            }
        }
        return false
    }

    fun process(image:Bitmap){
        var remainedTrackers= mutableListOf<TrackerObject>()

        for(tracker in trackers){
            if(tracker.getDetectionElapsedTime()>2000){
                continue
            }
            if(!tracker.Update(image)){
                continue
            }
            remainedTrackers.add(tracker)
        }

        trackers=remainedTrackers;

    }
    private fun calc_iot(bbox:BoundingBox,min_iot:Float=0f):List<Pair<Float,TrackerObject>> {
        var subject:TrackerObject?=null
        val result = mutableListOf<Pair<Float,TrackerObject>>()

        for(tracker in trackers){
            var iou=BoundingBox.box_iou(tracker.getBoundingBox(),bbox)
            if(iou>=min_iot){
                result.add(iou to tracker)
            }
        }
        result.sortBy { it.first }
        return result

    }



}