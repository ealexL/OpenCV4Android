package com.ealax.opencvdemo

import android.content.ContentValues
import android.content.Intent
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import com.ealax.opencvdemo.fliters.*
import com.ealax.opencvdemo.fliters.ar.ImageDetectionFilter
import com.ealax.opencvdemo.fliters.convolution.StrokeEdgesFilter
import com.ealax.opencvdemo.fliters.curve.CrossProcessCurveFilter
import com.ealax.opencvdemo.fliters.curve.PortraCurveFilter
import com.ealax.opencvdemo.fliters.curve.ProviaCurveFilter
import com.ealax.opencvdemo.fliters.mixer.RecolorCMVFilter
import com.ealax.opencvdemo.fliters.curve.VelviaCureFilter
import com.ealax.opencvdemo.fliters.mixer.RecolorRCFilter
import com.ealax.opencvdemo.fliters.mixer.RecolorRGVFilter
import org.opencv.android.*
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File


/**
 * Created by linwuyi on 2018/10/19
 * Explanation:
 */

class CameraActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {


    public val instance by lazy { this } //这里使用了委托，表示只有使用到instance才会执行该段代码

    companion object {
        private val TAG = "CameraActivity"
        private val STATE_CAMEERA_INDEX = "cameraIndex"
        private val STATE_CURVE_FILTER_INDEX = "curveFilterIndex"
        private val STATE_MIXER_FILTER_INDEX = "mixerFilterIndex"
        private val STATE_CONVOLUTION_FILTER_INDEX = "convolutionFilterIndex"
        private val STATE_IMAGE_DETECTION_FILTER_INDEX = "imageDetectionFilterIndex"
    }

    private var mCameraIndex: Int = 0
    private var mIsCameraFrontFacing: Boolean = false
    private var mNumCameras: Int = 0
    private var mCameraView: CameraBridgeViewBase? = null
    private var mIsPhotoPending: Boolean = false
    private var mBgr: Mat? = null
    private var mIsMenuLocked: Boolean = false

    private var mImageDetectionFilters: Array<Filter> = emptyArray()
    private var mCurveFilters: Array<Filter> = emptyArray()
    private var mMixerFilters: Array<Filter> = emptyArray()
    private var mConvolutionFilters: Array<Filter> = emptyArray()

    private var mImageDetectionFilterIndex = 0
    private var mCurveFilterIndex = 0
    private var mMixerFilterIndex = 0
    private var mConvolutionFilterIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (savedInstanceState != null) {
            mCameraIndex = savedInstanceState.getInt(STATE_CAMEERA_INDEX, 0)
            mCurveFilterIndex = savedInstanceState.getInt(STATE_CURVE_FILTER_INDEX, 0)
            mMixerFilterIndex = savedInstanceState.getInt(STATE_MIXER_FILTER_INDEX, 0)
            mConvolutionFilterIndex = savedInstanceState.getInt(STATE_CONVOLUTION_FILTER_INDEX, 0)
            mImageDetectionFilterIndex = savedInstanceState.getInt(STATE_IMAGE_DETECTION_FILTER_INDEX, 0)
        } else {
            mCameraIndex = 0
            mCurveFilterIndex = 0
            mMixerFilterIndex = 0
            mConvolutionFilterIndex = 0
            mImageDetectionFilterIndex = 0
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            var cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(mCameraIndex, cameraInfo)
            mIsCameraFrontFacing = (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            mNumCameras = Camera.getNumberOfCameras()
        } else {
            mIsCameraFrontFacing = false
            mNumCameras = 1
        }
        mCameraView = JavaCameraView(this, mCameraIndex)
        mCameraView!!.setCvCameraViewListener(this)
        setContentView(mCameraView)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putInt(STATE_CAMEERA_INDEX, mCameraIndex)
        outState.putInt(STATE_CURVE_FILTER_INDEX, mCurveFilterIndex)
        outState.putInt(STATE_MIXER_FILTER_INDEX, mMixerFilterIndex)
        outState.putInt(STATE_CONVOLUTION_FILTER_INDEX, mConvolutionFilterIndex)
        outState.putInt(STATE_IMAGE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        mCameraView!!.disableView()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        mIsMenuLocked = false

    }

    override fun onDestroy() {
        super.onDestroy()
        mCameraView!!.disableView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_camera, menu)
        if (mNumCameras < 2) {
            menu.removeItem(R.id.menu_next_camera)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (mIsMenuLocked)
            return true
        when (item!!.itemId) {
            R.id.menu_next_camera -> {
                mIsMenuLocked = true
                mCameraIndex++
                if (mCameraIndex == mNumCameras)
                    mCameraIndex = 0
                recreate()
                return true
            }
            R.id.menu_take_photo -> {
                mIsMenuLocked = true
                mIsPhotoPending = true
                return true
            }
            R.id.menu_next_curve_filter -> {
                mCurveFilterIndex++
                if (mCurveFilterIndex == mCurveFilters.size) {
                    mCurveFilterIndex = 0
                }
                return true
            }
            R.id.menu_next_mixer_filter -> {
                mMixerFilterIndex++
                if (mMixerFilterIndex == mMixerFilters.size) {
                    mMixerFilterIndex = 0
                }
                return true
            }
            R.id.menu_next_convolution_filter -> {
                mConvolutionFilterIndex++
                if (mConvolutionFilterIndex == mConvolutionFilters.size) {
                    mConvolutionFilterIndex = 0
                }
                return true
            }
            R.id.menu_next_image_detection_filter -> {
                mImageDetectionFilterIndex++
                if (mImageDetectionFilterIndex == mImageDetectionFilters.size) {
                    mImageDetectionFilterIndex = 0
                }
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    var mLoaderCallback = object : BaseLoaderCallback(this) {

        override fun onPackageInstall(operation: Int, callback: InstallCallbackInterface?) {
            super.onPackageInstall(operation, callback)
        }

        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    mCameraView!!.enableView()
                    mBgr = Mat()
                    mCurveFilters = arrayOf(
                            NoneFilter(),
                            PortraCurveFilter(),
                            ProviaCurveFilter(),
                            VelviaCureFilter(),
                            CrossProcessCurveFilter())
                    mMixerFilters = arrayOf(
                            NoneFilter(),
                            RecolorRCFilter(),
                            RecolorRGVFilter(),
                            RecolorCMVFilter())
                    mConvolutionFilters = arrayOf(
                            NoneFilter(),
                            StrokeEdgesFilter())
                    try {
                        val starryWight = ImageDetectionFilter(this@CameraActivity, R.drawable.starry_night)
                        val akbarHunting = ImageDetectionFilter(this@CameraActivity, R.drawable.akbar_hunting_with_cheetahs)
                        mImageDetectionFilters = arrayOf(NoneFilter(), starryWight, akbarHunting)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                else -> {
                    super.onManagerConnected(status)
                }

            }
        }
    }


    override fun onCameraViewStarted(p0: Int, p1: Int) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCameraViewStopped() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        //To change body of created functions use File | Settings | File Templates.
        val rgha = inputFrame!!.rgba()
        //Apply the active filters
        mCurveFilters[mCurveFilterIndex].apply(rgha,rgha)
        mMixerFilters[mMixerFilterIndex].apply(rgha,rgha)
        mConvolutionFilters[mConvolutionFilterIndex].apply(rgha,rgha)
        mImageDetectionFilters[mImageDetectionFilterIndex].apply(rgha,rgha)
        if (mIsPhotoPending) {
            mIsPhotoPending = false
            takePhoto(rgha)
        }
        if (mIsCameraFrontFacing) {
            Core.flip(rgha, rgha, 1)
        }
        return rgha
    }

    private fun takePhoto(rgha: Mat) {
        val currentTimeMillis = System.currentTimeMillis()
        val appName = getString(R.string.app_name)
        val galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val albumPath = "$galleryPath/$appName"
        val photoPath = "$albumPath/$currentTimeMillis.png"
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DATA, photoPath)
        values.put(MediaStore.Images.Media.MIME_TYPE, LabActivity.PHOTO_MIME_TYPE)
        values.put(MediaStore.Images.Media.TITLE, appName)
        values.put(MediaStore.Images.Media.DESCRIPTION, appName)
        values.put(MediaStore.Images.Media.DATE_TAKEN, currentTimeMillis)
        var album = File(albumPath)
        if (!album.isDirectory && !album.mkdirs()) {
            onTakePhotoFailed()
            return
        }
        Imgproc.cvtColor(rgha, mBgr, Imgproc.COLOR_RGBA2BGR, 3)
        if (!Imgcodecs.imwrite(photoPath, mBgr)) {
            onTakePhotoFailed()
        }
        var uri: Uri
        try {
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Exception) {
            e.printStackTrace()
            var photo = File(photoPath)
            if (!photo.delete()) {

            }
            onTakePhotoFailed()
            return
        }
        val intent = Intent(this, LabActivity::class.java)
        intent.putExtra(LabActivity.EXTRA_PHOTO_URI, uri)
        intent.putExtra(LabActivity.EXTRA_PHOTO_DATA_PATH, photoPath)
        startActivity(intent)
    }

    private fun onTakePhotoFailed() {
        mIsMenuLocked = false
        val errorMessage = getString(R.string.photo_delete_prompt_message)
        runOnUiThread {
            Toast.makeText(this@CameraActivity, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

}