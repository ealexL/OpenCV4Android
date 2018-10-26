package com.ealax.opencvdemo.fliters.mixer

import com.ealax.opencvdemo.fliters.Filter
import org.opencv.core.Core
import org.opencv.core.Mat

/**
 * Created by linwuyi on 2018/10/26
 * Explanation:
 */

class RecolorCMVFilter : Filter {

    private var mChannels = arrayOfNulls<Mat>(4).toMutableList()

    override fun apply(src: Mat, dst: Mat) {
        Core.split(src, mChannels)
        val r = mChannels[0]
        val g = mChannels[1]
        val b = mChannels[2]
        //dst.b = max(dst.r, dst.g, dst.b)
        Core.max(b, r, b)
        Core.max(b, g, b)
        Core.merge(mChannels, dst)
    }
}