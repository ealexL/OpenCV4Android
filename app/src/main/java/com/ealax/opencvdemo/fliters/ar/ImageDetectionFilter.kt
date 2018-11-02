package com.ealax.opencvdemo.fliters.ar

import android.content.Context
import com.ealax.opencvdemo.fliters.Filter
import org.opencv.android.Utils
import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.features2d.DescriptorExtractor
import org.opencv.features2d.DescriptorMatcher
import org.opencv.features2d.FeatureDetector
import org.opencv.highgui.Highgui
import org.opencv.imgproc.Imgproc

/**
 * Created by linwuyi on 2018/10/26
 * Explanation:
 */
class ImageDetectionFilter : Filter {
    private var mReferenceImage: Mat? = null
    private val mReferenceKeyPoints = MatOfKeyPoint()
    private val mReferenceDescriptors = Mat()
    private val mReferenceCorners = Mat(4, 1, CvType.CV_32FC2)
    private val mSceneKeyPoints = MatOfKeyPoint()
    private val mSceneDescriptors = Mat()
    private val mCandidateSceneCorners = Mat(4, 1, CvType.CV_32FC2)
    private val mSceneCorners = Mat(4,1,CvType.CV_32FC2)
    private val mIntSceneCorners = MatOfPoint()
    private val mGraySrc = Mat()
    private val mMatches = MatOfDMatch()
    private val mFeatureDetector = FeatureDetector.create(FeatureDetector.STAR)
    private val mDescriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.FREAK)
    private val mDescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING)

    private val mLineColor = Scalar(0.0, 255.0, 0.0)

    constructor(context: Context, referenceImageResourceID: Int) {
        try {
            mReferenceImage = Utils.loadResource(context, referenceImageResourceID, Highgui.CV_LOAD_IMAGE_COLOR)
            val referenceImageGray = Mat()
            Imgproc.cvtColor(mReferenceImage, referenceImageGray, Imgproc.COLOR_BGR2GRAY)

            mReferenceCorners.put(0, 0, 0.0, 0.0)
            mReferenceCorners.put(1,0, referenceImageGray.cols().toDouble(),0.0)
            mReferenceCorners.put(2,0, referenceImageGray.cols().toDouble(),referenceImageGray.rows().toDouble())
            mReferenceCorners.put(3,0, 0.0,referenceImageGray.rows().toDouble())

            mFeatureDetector.detect(referenceImageGray,mReferenceKeyPoints)
            mDescriptorExtractor.compute(referenceImageGray,mReferenceKeyPoints,mReferenceDescriptors)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun apply(src: Mat, dst: Mat) {
        Imgproc.cvtColor(src,mGraySrc,Imgproc.COLOR_RGBA2GRAY)

        mFeatureDetector.detect(mGraySrc,mSceneKeyPoints)
        mDescriptorExtractor.compute(mGraySrc,mSceneKeyPoints,mSceneDescriptors)
        mDescriptorMatcher.match(mSceneDescriptors,mReferenceDescriptors,mMatches)

        findSceneCorners()
        draw(src,dst)
    }

    private fun findSceneCorners() {
        val matchesList = mMatches.toList()
        if (matchesList.size<4) return
        val referenceKeyPointList = mReferenceKeyPoints.toList()
        val sceneKeyPointsList  = mSceneKeyPoints.toList()
        var maxDist = 0.0
        var minDist = Double.MAX_VALUE
        for(match in matchesList){
            val dist = match.distance
            if (dist<minDist)
                minDist = dist.toDouble()
            if (dist>maxDist)
                maxDist = dist.toDouble()
        }
        if (minDist>50){
            mSceneCorners.create(0,0,mSceneCorners.type())
            return
        }else if (minDist>25){
            return
        }
        val goodReferencePointsList = arrayListOf<Point>()
        val goodScenePointsList = arrayListOf<Point>()
        val maxGoodMatchList = 1.75 * minDist
        for (match in matchesList){
            if (match.distance<maxGoodMatchList){
                goodReferencePointsList.add(referenceKeyPointList.get(match.trainIdx).pt)
                goodScenePointsList.add(sceneKeyPointsList.get(match.queryIdx).pt)
            }
        }
        if (goodReferencePointsList.size < 4 || goodScenePointsList.size < 4){
            return
        }

        val goodReferencePoints = MatOfPoint2f()
        goodReferencePoints.fromList(goodReferencePointsList)
        val goodScenePoints = MatOfPoint2f()
        goodScenePoints.fromList(goodScenePointsList)

        val homography = Calib3d.findHomography(goodReferencePoints,goodScenePoints)
        Core.perspectiveTransform(mReferenceCorners,mCandidateSceneCorners,homography)
        mCandidateSceneCorners.convertTo(mIntSceneCorners,CvType.CV_32S)
        if (Imgproc.isContourConvex(mIntSceneCorners)){
            mCandidateSceneCorners.copyTo(mSceneCorners)
        }
    }

    private fun draw(src: Mat, dst: Mat) {
        if (dst != src){
            src.copyTo(dst)
        }
        if (mSceneCorners.height() < 4){
            var height = mReferenceImage!!.height()
            var width = mReferenceImage!!.width()
            val maxDimension = Math.min(dst.width(),dst.height()/2)
            val aspectRatic: Double = width / height.toDouble()
            if (height > width){
                height = maxDimension
                width = (height * aspectRatic).toInt()
            }else{
                width = maxDimension
                height = (width / aspectRatic).toInt()
            }
            val dstROI = dst.submat(0, height, 0, width)
            Imgproc.resize(mReferenceImage, dstROI, dstROI.size(), 0.0, 0.0, Imgproc.INTER_AREA)
            return
        }

        Core.line(dst, Point(mSceneCorners.get(0,0)), Point(mSceneCorners.get(1,0)),
                mLineColor, 4)
        Core.line(dst, Point(mSceneCorners.get(1,0)), Point(mSceneCorners.get(2,0)),
                mLineColor, 4)
        Core.line(dst, Point(mSceneCorners.get(2,0)), Point(mSceneCorners.get(3,0)),
                mLineColor, 4)
        Core.line(dst, Point(mSceneCorners.get(3,0)), Point(mSceneCorners.get(0,0)),
                mLineColor, 4)
    }

}