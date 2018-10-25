package com.ealax.opencvdemo.fliters

import org.opencv.core.Mat

/**
 * Created by linwuyi on 2018/10/24
 * Explanation:
 */
interface Filter {
    public fun apply(src: Mat, dst:Mat)
}