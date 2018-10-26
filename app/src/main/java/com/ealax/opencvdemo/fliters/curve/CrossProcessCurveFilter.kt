package com.ealax.opencvdemo.fliters.curve

import com.ealax.opencvdemo.fliters.curve.CurveFilter

/**
 * Created by linwuyi on 2018/10/26
 * Explanation:
 */
class CrossProcessCurveFilter : CurveFilter {
    constructor() :
            super(doubleArrayOf(0.0, 255.0), doubleArrayOf(0.0, 255.0),
                    doubleArrayOf(0.0, 56.0, 211.0, 255.0), doubleArrayOf(0.0, 22.0, 255.0, 255.0),
                    doubleArrayOf(0.0, 56.0, 208.0, 255.0), doubleArrayOf(0.0, 39.0, 226.0, 255.0),
                    doubleArrayOf(0.0, 255.0), doubleArrayOf(20.0, 235.0))
}