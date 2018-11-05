package com.ealax.opencvdemo.fliters.ar

import android.content.Context
import com.ealax.opencvdemo.adapters.CameraProjectionAdapter
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
 * Created by linwuyi on 2018/11/5
 * Explanation:
 */

class ImageARDetectionFilter : ARFilter {

    private var mReferenceImage: Mat? = null
    private val mReferenceKeyPoints = MatOfKeyPoint()
    private val mReferenceDescriptors = Mat()
    private val mReferenceCorners = Mat(4, 1, CvType.CV_32FC2)
    private val mSceneKeyPoints = MatOfKeyPoint()
    private val mSceneDescriptors = Mat()
    private val mCandidateSceneCorners = Mat(4, 1, CvType.CV_32FC2)
    private val mSceneCorners = Mat(4, 1, CvType.CV_32FC2)
    private val mIntSceneCorners = MatOfPoint()
    private val mGraySrc = Mat()
    private val mMatches = MatOfDMatch()
    private val mFeatureDetector = FeatureDetector.create(FeatureDetector.STAR)
    private val mDescriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.FREAK)
    private val mDescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING)

    private val mLineColor = Scalar(0.0, 255.0, 0.0)

    private val mDistCoeffs = MatOfDouble(0.0, 0.0, 0.0, 0.0)
    private var mCameraProjectionAdapter: CameraProjectionAdapter? = null
    private val mRVec = MatOfDouble()
    private val mTVec = MatOfDouble()
    private val mRotation = MatOfDouble()
    private val mGLPose = FloatArray(16)
    private var mTargetFound = false


    constructor(context: Context, referenceImageResourceID: Int, cameraProjectionAdapter: CameraProjectionAdapter?) {
        try {
            mReferenceImage = Utils.loadResource(context, referenceImageResourceID, Highgui.CV_LOAD_IMAGE_COLOR)
            val referenceImageGray = Mat()
            Imgproc.cvtColor(mReferenceImage, referenceImageGray, Imgproc.COLOR_BGR2GRAY)
            Imgproc.cvtColor(mReferenceImage, mReferenceImage, Imgproc.COLOR_BGR2RGBA)

            mReferenceCorners.put(0, 0, 0.0, 0.0)
            mReferenceCorners.put(1, 0, referenceImageGray.cols().toDouble(), 0.0)
            mReferenceCorners.put(2, 0, referenceImageGray.cols().toDouble(), referenceImageGray.rows().toDouble())
            mReferenceCorners.put(3, 0, 0.0, referenceImageGray.rows().toDouble())

            mFeatureDetector.detect(referenceImageGray, mReferenceKeyPoints)
            mDescriptorExtractor.compute(referenceImageGray, mReferenceKeyPoints, mReferenceDescriptors)

            mCameraProjectionAdapter = cameraProjectionAdapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getGLPose(): FloatArray? {
        return (if (mTargetFound) mGLPose else null)
    }

    override fun apply(src: Mat, dst: Mat) {
        Imgproc.cvtColor(src, mGraySrc, Imgproc.COLOR_RGBA2GRAY)

        mFeatureDetector.detect(mGraySrc, mSceneKeyPoints)
        mDescriptorExtractor.compute(mGraySrc, mSceneKeyPoints, mSceneDescriptors)
        mDescriptorMatcher.match(mSceneDescriptors, mReferenceDescriptors, mMatches)

        findPose()
        draw(src, dst)
    }

    private fun findPose() {
        val matchesList = mMatches.toList()
        if (matchesList.size < 4) return
        val referenceKeyPointList = mReferenceKeyPoints.toList()
        val sceneKeyPointsList = mSceneKeyPoints.toList()
        var maxDist = 0.0
        var minDist = Double.MAX_VALUE
        for (match in matchesList) {
            val dist = match.distance
            if (dist < minDist)
                minDist = dist.toDouble()
            if (dist > maxDist)
                maxDist = dist.toDouble()
        }
        if (minDist > 50) {
            mSceneCorners.create(0, 0, mSceneCorners.type())
            return
        } else if (minDist > 25) {
            return
        }
        val goodReferencePointsList = arrayListOf<Point3>()
        val goodScenePointsList = arrayListOf<Point>()
        val maxGoodMatchList = 1.75 * minDist
        for (match in matchesList) {
            if (match.distance < maxGoodMatchList) {
                val point = referenceKeyPointList.get(match.trainIdx).pt
                val point3 = Point3(point.x, point.y, 0.0)
                goodReferencePointsList.add(point3)
                goodScenePointsList.add(sceneKeyPointsList.get(match.queryIdx).pt)
            }
        }
        if (goodReferencePointsList.size < 4 || goodScenePointsList.size < 4) {
            return
        }

        val goodReferencePoints = MatOfPoint3f()
        goodReferencePoints.fromList(goodReferencePointsList)
        val goodScenePoints = MatOfPoint2f()
        goodScenePoints.fromList(goodScenePointsList)

//        val homography = Calib3d.findHomography(goodReferencePoints, goodScenePoints)
//        Core.perspectiveTransform(mReferenceCorners, mCandidateSceneCorners, homography)
//        mCandidateSceneCorners.convertTo(mIntSceneCorners, CvType.CV_32S)
//        if (Imgproc.isContourConvex(mIntSceneCorners)) {
//            mCandidateSceneCorners.copyTo(mSceneCorners)
//        }

        val projection = mCameraProjectionAdapter!!.getProjectionCV()
        Calib3d.solvePnP(goodReferencePoints, goodScenePoints, projection, mDistCoeffs, mRVec, mTVec)

        val rVecArray = mRVec.toArray()
        rVecArray[1] *= -1.0
        rVecArray[2] *= -1.0
        mRVec.fromArray(*rVecArray)

        Calib3d.Rodrigues(mRVec, mRotation)

        val tVecArray = mTVec.toArray()
        mGLPose[0] = mRotation.get(0, 0)[0].toFloat()
        mGLPose[1] = mRotation.get(1, 0)[0].toFloat()
        mGLPose[2] = mRotation.get(2, 0)[0].toFloat()
        mGLPose[3] = 0f
        mGLPose[4] = mRotation.get(0, 1)[0].toFloat()
        mGLPose[5] = mRotation.get(1, 1)[0].toFloat()
        mGLPose[6] = mRotation.get(2, 1)[0].toFloat()
        mGLPose[7] = 0f
        mGLPose[8] = mRotation.get(0, 2)[0].toFloat()
        mGLPose[9] = mRotation.get(1, 2)[0].toFloat()
        mGLPose[10] = mRotation.get(2, 2)[0].toFloat()
        mGLPose[11] = 0f
        mGLPose[12] = tVecArray[0].toFloat()
        mGLPose[13] = tVecArray[1].toFloat()
        mGLPose[14] = tVecArray[2].toFloat()
        mGLPose[15] = 1f

        mTargetFound = true
    }

    private fun draw(src: Mat, dst: Mat) {
        if (dst != src) {
            src.copyTo(dst)
        }
//        if (mSceneCorners.height() < 4) {
//            var height = mReferenceImage!!.height()
//            var width = mReferenceImage!!.width()
//            val maxDimension = Math.min(dst.width(), dst.height() / 2)
//            val aspectRatic: Double = width / height.toDouble()
//            if (height > width) {
//                height = maxDimension
//                width = (height * aspectRatic).toInt()
//            } else {
//                width = maxDimension
//                height = (width / aspectRatic).toInt()
//            }
//            val dstROI = dst.submat(0, height, 0, width)
//            Imgproc.resize(mReferenceImage, dstROI, dstROI.size(), 0.0, 0.0, Imgproc.INTER_AREA)
//            return
//        }
//
//        Core.line(dst, Point(mSceneCorners.get(0, 0)), Point(mSceneCorners.get(1, 0)),
//                mLineColor, 4)
//        Core.line(dst, Point(mSceneCorners.get(1, 0)), Point(mSceneCorners.get(2, 0)),
//                mLineColor, 4)
//        Core.line(dst, Point(mSceneCorners.get(2, 0)), Point(mSceneCorners.get(3, 0)),
//                mLineColor, 4)
//        Core.line(dst, Point(mSceneCorners.get(3, 0)), Point(mSceneCorners.get(0, 0)),
//                mLineColor, 4)
        if (!mTargetFound) {
            var height = mReferenceImage!!.height()
            var width = mReferenceImage!!.width()
            val maxDimension = Math.min(dst.width(), dst.height() / 2)
            val aspectRatic: Double = width / height.toDouble()
            if (height > width) {
                height = maxDimension
                width = (height * aspectRatic).toInt()
            } else {
                width = maxDimension
                height = (width / aspectRatic).toInt()
            }
            val dstROI = dst.submat(0, height, 0, width)
            Imgproc.resize(mReferenceImage, dstROI, dstROI.size(),
                    0.0, 0.0, Imgproc.INTER_AREA)
        }
    }
}