package com.ealax.opencvdemo.fliters.convolution

import com.ealax.opencvdemo.fliters.Filter
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfInt
import org.opencv.imgproc.Imgproc

/**
 * Created by linwuyi on 2018/10/26
 * Explanation:
 */
class StrokeEdgesFilter : Filter {
    private val mKernel =
            MatOfInt(0, 0, 1, 0, 0,
                    0, 1, 2, 1, 0,
                    1, 2, -16, 2, 1,
                    0, 1, 2, 1, 0,
                    0, 0, 1, 0, 0)
    private val mEdges = Mat()

    override fun apply(src: Mat, dst: Mat) {
        Imgproc.filter2D(src, mEdges, -1, mKernel)
        Core.bitwise_not(mEdges, mEdges)
        Core.multiply(src, mEdges, dst, 1.0 / 255.0)
    }

}