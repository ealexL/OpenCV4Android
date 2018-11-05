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

    var filter: ARFilter? = null
    var cameraProjectionAdapter: CameraProjectionAdapter? = null
    var scale = 100f


    companion object {
        var VERTICES: ByteBuffer? = null
        var COLORS: ByteBuffer? = null
        var TRIANGLE_FAN_0: ByteBuffer? = null
        var TRIANGLE_FAN_1: ByteBuffer? = null
        init {
            VERTICES = ByteBuffer.allocateDirect(96)
            COLORS = ByteBuffer.allocateDirect(32)
            TRIANGLE_FAN_0 = ByteBuffer.allocateDirect(18)
            TRIANGLE_FAN_1 = ByteBuffer.allocateDirect(18)
            VERTICES!!.order(ByteOrder.nativeOrder())
            VERTICES!!.asFloatBuffer().put(floatArrayOf(
                    -1f, 1f, 1f,
                    1f, 1f, 1f,
                    1f, -1f, 1f,
                    -1f, -1f, 1f,

                    -1f, 1f, -1f,
                    1f, 1f, -1f,
                    1f, -1f, -1f,
                    -1f, -1f, -1f))
            VERTICES!!.position(0)
            COLORS!!.put(byteArrayOf(
                    Byte.MAX_VALUE,Byte.MAX_VALUE,0,Byte.MAX_VALUE,
                    0,Byte.MAX_VALUE,Byte.MAX_VALUE,Byte.MAX_VALUE,
                    0,0,0,Byte.MAX_VALUE,
                    Byte.MAX_VALUE,0,Byte.MAX_VALUE,Byte.MAX_VALUE,

                    Byte.MAX_VALUE,0,0,Byte.MAX_VALUE,
                    0,Byte.MAX_VALUE,0,Byte.MAX_VALUE,
                    0,0,Byte.MAX_VALUE,Byte.MAX_VALUE,
                    0,0,0,Byte.MAX_VALUE))
            COLORS!!.position(0)
            TRIANGLE_FAN_0!!.put(byteArrayOf(
                    1,0,3,
                    1,3,2,
                    1,2,6,
                    1,6,5,
                    1,5,4,
                    1,4,0
            ))
            TRIANGLE_FAN_0!!.position(0)
            TRIANGLE_FAN_1!!.put(byteArrayOf(
                    7,4,5,
                    7,5,6,
                    7,6,2,
                    7,2,3,
                    7,3,0,
                    7,0,4
            ))
            TRIANGLE_FAN_1!!.position(0)

        }
    }

    override fun onDrawFrame(gl: GL10?) {
        gl!!.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        gl.glClearColor(0f,0f,0f,0f)
        if (filter == null){
            return
        }
        if (cameraProjectionAdapter == null)
            return
        val pose = filter!!.getGLPose() ?: return
        gl.glMatrixMode(GL10.GL_PROJECTION)
        val projection = cameraProjectionAdapter!!.getProjectionGL()
        gl.glLoadMatrixf(projection,0)

        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadMatrixf(pose,0)
        gl.glTranslatef(0f,0f,1f)
        gl.glScalef(scale,scale,scale)

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY)

        gl.glVertexPointer(3, GL11.GL_FLOAT, 0, VERTICES)
        gl.glColorPointer(4,GL11.GL_UNSIGNED_BYTE,0, COLORS)

        gl.glDrawElements(GL10.GL_TRIANGLE_FAN,18,GL10.GL_UNSIGNED_BYTE, TRIANGLE_FAN_0)
        gl.glDrawElements(GL10.GL_TRIANGLE_FAN,18,GL10.GL_UNSIGNED_BYTE, TRIANGLE_FAN_1)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

    }

}