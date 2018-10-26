package com.ealax.opencvdemo.fliters

/**
 * Created by linwuyi on 2018/10/26
 * Explanation:
 */
class ProviaCurveFilter : CurveFilter{
    constructor():
            super(doubleArrayOf(0.0,  255.0), doubleArrayOf(0.0, 255.0),
                    doubleArrayOf(0.0, 59.0, 202.0, 255.0), doubleArrayOf(0.0, 54.0, 210.0, 255.0),
                    doubleArrayOf(0.0, 27.0, 196.0, 255.0), doubleArrayOf(0.0, 21.0, 207.0, 255.0),
                    doubleArrayOf(0.0, 35.0, 205.0, 255.0), doubleArrayOf(0.0, 25.0, 227.0, 255.0))
}