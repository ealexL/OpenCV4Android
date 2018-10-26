package com.ealax.opencvdemo.fliters.ar

import android.content.Context
import com.ealax.opencvdemo.fliters.Filter
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.DescriptorExtractor
import org.opencv.features2d.DescriptorMatcher
import org.opencv.features2d.FeatureDetector
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

/**
 * Created by linwuyi on 2018/10/26
 * Explanation:
 */
class ImageDeetectionFilter : Filter {
    private var mReferenceImage: Mat? = null
    private val mReferenceKeyPoints = MatOfKeyPoint()
    private val mReferenceDescriptors = Mat()
    private val mReferenceCorners = Mat(4, 1, CvType.CV_32FC2)
    private val mSceneKeyPoints = MatOfKeyPoint()
    private val mSceneDescriptors = Mat()
    private val mCandidateSceneCorners = Mat(4, 1, CvType.CV_32FC2)
    private val mIntSceneCorners = MatOfPoint()
    private val mGraySrc = Mat()
    private val mMatches = MatOfDMatch()
    private val mFeatureDetector = FeatureDetector.create(FeatureDetector.STAR)
    private val mDescriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.FREAK)
    private val mDescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING)

    private val mLineColor = Scalar(0.0, 255.0, 0.0)

    constructor(context: Context, referenceImageResourceID: Int) {
        try {
            mReferenceImage = Utils.loadResource(context, referenceImageResourceID, Imgcodecs.CV_LOAD_IMAGE_COLOR)
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
        var maxDist = Double.MAX_VALUE
        var mixDist = 0.0
//        for(match in matchesList){
//        }
    }

    private fun draw(src: Mat, dst: Mat) {

    }

}