package com.ealax.opencvdemo.adapters

import android.hardware.Camera
import android.opengl.Matrix
import org.opencv.core.CvType
import org.opencv.core.MatOfDouble

/**
 * Created by linwuyi on 2018/11/5
 * Explanation:
 */

class CameraProjectionAdapter {
    var mFOVY = 43.6f
    var mFOVX = 65.4f
    var mHeightPx = 640
    var mWidthPx = 480
    var mNear = 1f  //最近裁剪距离
    var mFar = 10000f //最远裁剪距离

    var mProjectionGL = FloatArray(16)
    var mProjectionDirtyGL = true

    var mProjectionCV: MatOfDouble? = null
    var mProjectionDirtyCV = true

    fun setCameraParameters(parameters: Camera.Parameters) {
        mFOVY = parameters.verticalViewAngle
        mFOVX = parameters.horizontalViewAngle

        val pictureSize = parameters.pictureSize
        mHeightPx = pictureSize.height
        mWidthPx = pictureSize.width
    }

    fun setClipDistances(near: Float, far: Float) {
        mNear = near
        mFar = far
        mProjectionDirtyGL = true
    }

    fun getProjectionGL(): FloatArray {
        if (mProjectionDirtyGL) {
            val top = (Math.tan(mFOVY * Math.PI / 360f)).toFloat() * mNear
            val right = (Math.tan(mFOVX * Math.PI / 360f)).toFloat() * mNear
            Matrix.frustumM(mProjectionGL, 0, -right, right
                    , -top, top, mNear, mFar)
            mProjectionDirtyGL = false
        }
        return mProjectionGL
    }

    fun getProjectionCV(): MatOfDouble {
        if (mProjectionDirtyCV) {
            if (mProjectionCV == null) {
                mProjectionCV = MatOfDouble()
                mProjectionCV!!.create(3, 3, CvType.CV_64FC1)
            }
            val diagonalPx = Math.sqrt((Math.pow(mWidthPx.toDouble(), 2.0)
                    + Math.pow(mHeightPx.toDouble(), 2.0)))
            val diagonalFOV = Math.sqrt((Math.pow(mFOVX.toDouble(), 2.0)
                    + Math.pow(mFOVY.toDouble(), 2.0)))
            val focalLengthPx = diagonalPx / (2.0 * Math.tan(0.5 * diagonalFOV))
            mProjectionCV!!.put(0, 0, focalLengthPx)
            mProjectionCV!!.put(0, 1, 0.0)
            mProjectionCV!!.put(0, 2, 0.5 * mWidthPx)
            mProjectionCV!!.put(1, 0, 0.0)
            mProjectionCV!!.put(1, 1, focalLengthPx)
            mProjectionCV!!.put(1, 2, 0.5 * mHeightPx)
            mProjectionCV!!.put(2, 0, 0.0)
            mProjectionCV!!.put(2, 1, 0.0)
            mProjectionCV!!.put(2, 2, 0.0)
        }
        return mProjectionCV!!
    }
}