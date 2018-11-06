package com.ealax.opencvdemo

import android.opengl.GLSurfaceView
import com.ealax.opencvdemo.adapters.CameraProjectionAdapter
import com.ealax.opencvdemo.fliters.ar.ARFilter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11

/**
 * Created by linwuyi on 2018/11/5
 * Explanation:
 */
class ARCubeRenderer : GLSurfaceView.Renderer {

    public var filter: ARFilter? = null
    public var cameraProjectionAdapter: CameraProjectionAdapter? = null
    public var scale = 1f

    private var mSurfaceWidth = 0
    private var mSurfaceHeight = 0


    companion object {
        var VERTICES: ByteBuffer? = null
        var COLORS: ByteBuffer? = null
        var TRIANGLES: ByteBuffer? = null
        var TRIANGLE_FAN_0: ByteBuffer? = null
        var TRIANGLE_FAN_1: ByteBuffer? = null

        init {
            VERTICES = ByteBuffer.allocateDirect(96)
            VERTICES!!.order(ByteOrder.nativeOrder())
            VERTICES!!.asFloatBuffer().put(floatArrayOf(
                    -0.5f, -0.5f, 0.5f,
                    0.5f, -0.5f, 0.5f,
                    0.5f, 0.5f, 0.5f,
                    -0.5f, 0.5f, 0.5f,

                    -0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f,
                    0.5f, 0.5f, -0.5f,
                    -0.5f, 0.5f, -0.5f))
            VERTICES!!.position(0)

            COLORS = ByteBuffer.allocateDirect(32)
            val maxColor = 255.toByte()
            COLORS!!.put(byteArrayOf(
                    //front
                    maxColor, 0, 0, maxColor,             //red
                    maxColor, maxColor, 0, maxColor,      //yellow
                    maxColor, maxColor, 0, maxColor,      //yellow
                    maxColor, 0, 0, maxColor,             //red
                    //back
                    0, maxColor, 0, maxColor,             //green
                    0, 0, maxColor, maxColor,             //blue
                    0, 0, maxColor, maxColor,             //blue
                    0, maxColor, 0, maxColor))            //green
            COLORS!!.position(0)

            TRIANGLES = ByteBuffer.allocateDirect(36)
            TRIANGLES!!.put(byteArrayOf(
                    // Front.
                    0, 1, 2, 2, 3, 0,
                    3, 2, 6, 6, 7, 3,
                    7, 6, 5, 5, 4, 7,
                    // Back.
                    4, 5, 1, 1, 0, 4,
                    4, 0, 3, 3, 7, 4,
                    1, 5, 6, 6, 2, 1
            ))
            TRIANGLES!!.position(0)
//            TRIANGLE_FAN_0 = ByteBuffer.allocateDirect(18)
//            TRIANGLE_FAN_0!!.put(byteArrayOf(
//                    1,0,3,
//                    1,3,2,
//                    1,2,6,
//                    1,6,5,
//                    1,5,4,
//                    1,4,0
//            ))
//            TRIANGLE_FAN_0!!.position(0)
//            TRIANGLE_FAN_1 = ByteBuffer.allocateDirect(18)
//            TRIANGLE_FAN_1!!.put(byteArrayOf(
//                    7,4,5,
//                    7,5,6,
//                    7,6,2,
//                    7,2,3,
//                    7,3,0,
//                    7,0,4
//            ))
//            TRIANGLE_FAN_1!!.position(0)

        }
    }

    override fun onDrawFrame(gl: GL10?) {
        gl!!.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
//        gl.glClearColor(0f,0f,0f,0f)
        if (filter == null) {
            return
        }
        if (cameraProjectionAdapter == null)
            return
        val pose = filter!!.getGLPose() ?: return

        val adjustedWidth = (mSurfaceHeight *
                cameraProjectionAdapter!!.getAspectRatio()).toInt()
        val marginX = (mSurfaceWidth - adjustedWidth) / 2
        gl.glViewport(marginX, 0, adjustedWidth, mSurfaceHeight)

        gl.glMatrixMode(GL10.GL_PROJECTION)
        val projection = cameraProjectionAdapter!!.getProjectionGL()
        gl.glLoadMatrixf(projection, 0)

        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadMatrixf(pose, 0)
        gl.glScalef(scale, scale, scale)

        gl.glTranslatef(0f, 0f, 0.5f)

        gl.glVertexPointer(3, GL11.GL_FLOAT, 0, VERTICES)
        gl.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 0, COLORS)

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY)

//        gl.glDrawElements(GL10.GL_TRIANGLE_FAN, 18, GL10.GL_UNSIGNED_BYTE, TRIANGLE_FAN_0)
//        gl.glDrawElements(GL10.GL_TRIANGLE_FAN, 18, GL10.GL_UNSIGNED_BYTE, TRIANGLE_FAN_1)
        gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, TRIANGLES)

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mSurfaceWidth = width
        mSurfaceHeight = height
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        gl!!.glClearColor(0f, 0f, 0f, 0f) // transparent
        gl.glEnable(GL10.GL_CULL_FACE)
    }

}