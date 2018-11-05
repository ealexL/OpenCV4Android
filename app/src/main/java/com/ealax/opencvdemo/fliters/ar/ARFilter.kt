package com.ealax.opencvdemo.fliters.ar

import com.ealax.opencvdemo.fliters.Filter

/**
 * Created by linwuyi on 2018/11/5
 * Explanation:
 */
interface ARFilter : Filter{
    public fun getGLPose():FloatArray?
}