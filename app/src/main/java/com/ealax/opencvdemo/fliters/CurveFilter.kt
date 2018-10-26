package com.ealax.opencvdemo.fliters

import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfInt

/**
 * Created by linwuyi on 2018/10/26
 * Explanation:
 */
 open class CurveFilter : Filter {

    private val mLUT = MatOfInt()

     constructor(vValIn: DoubleArray, vValOut: DoubleArray,
                rValIn: DoubleArray, rValOut: DoubleArray,
                gValIn: DoubleArray, gValOut: DoubleArray,
                bValIn: DoubleArray, bValOut: DoubleArray) {
        //Create the interpolation functions
        var vFunc = newFunc(vValIn, vValOut)
        var rFunc = newFunc(rValIn, rValOut)
        var gFunc = newFunc(gValIn, gValOut)
        var bFunc = newFunc(bValIn, bValOut)
        mLUT.create(256, 1, CvType.CV_8UC4)
        for (i in 0 until 256) {
            var v = vFunc.value(i.toDouble())
            var r = rFunc.value(v)
            var g = gFunc.value(v)
            var b = bFunc.value(v)
            mLUT.put(i, 0, r, g, b, i.toDouble())//alpha is unchanged
        }
    }


    override fun apply(src: Mat, dst: Mat) {
        Core.LUT(src, mLUT, dst)
    }

    private fun newFunc(valIn: DoubleArray, valOut: DoubleArray): UnivariateFunction {
        var interpolator: UnivariateInterpolator
        if (valIn.size > 2) {
            interpolator = SplineInterpolator()
        } else {
            interpolator = LinearInterpolator()
        }
        return interpolator.interpolate(valIn, valOut)
    }
}