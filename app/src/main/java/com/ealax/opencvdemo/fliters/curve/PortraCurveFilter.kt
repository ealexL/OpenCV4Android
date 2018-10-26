package com.ealax.opencvdemo.fliters.curve

/**
 * Created by linwuyi on 2018/10/26
 * Explanation:
 */
class PortraCurveFilter : CurveFilter {

    constructor() :
            super(doubleArrayOf(0.0, 23.0, 157.0, 255.0), doubleArrayOf(0.0, 20.0, 173.0, 255.0),
                    doubleArrayOf(0.0, 69.0, 213.0, 255.0), doubleArrayOf(0.0, 69.0, 218.0, 255.0),
                    doubleArrayOf(0.0, 52.0, 189.0, 255.0), doubleArrayOf(0.0, 47.0, 196.0, 255.0),
                    doubleArrayOf(0.0, 41.0, 231.0, 255.0), doubleArrayOf(0.0, 46.0, 226.0, 255.0))
}