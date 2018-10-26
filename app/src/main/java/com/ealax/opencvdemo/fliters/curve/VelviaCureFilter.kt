package com.ealax.opencvdemo.fliters.curve

/**
 * Created by linwuyi on 2018/10/26
 * Explanation:
 */
class VelviaCureFilter : CurveFilter {
    constructor() :
            super(doubleArrayOf(0.0, 128.0, 221.0, 255.0), doubleArrayOf(0.0, 118.0, 215.0, 255.0),
                    doubleArrayOf(0.0, 25.0, 122.0, 163.0, 255.0), doubleArrayOf(0.0, 21.0, 153.0, 206.0, 255.0),
                    doubleArrayOf(0.0, 25.0, 95.0, 181.0, 255.0), doubleArrayOf(0.0, 21.0, 102.0, 208.0, 255.0),
                    doubleArrayOf(0.0, 35.0, 205.0, 255.0), doubleArrayOf(0.0, 25.0, 227.0, 255.0))
}