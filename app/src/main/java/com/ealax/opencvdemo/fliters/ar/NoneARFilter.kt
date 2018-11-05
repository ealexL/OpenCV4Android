package com.ealax.opencvdemo.fliters.ar

import com.ealax.opencvdemo.fliters.NoneFilter

/**
 * Created by linwuyi on 2018/11/5
 * Explanation:
 */
class NoneARFilter : NoneFilter(),ARFilter{

    override fun getGLPose(): FloatArray? {
        return null
    }

}