package com.ealax.opencvdemo

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView

/**
 * Created by linwuyi on 2018/10/19
 * Explanation:
 */

class LabActivity : AppCompatActivity() {

    companion object {
        val PHOTO_MIME_TYPE = "image/png"
        val EXTRA_PHOTO_URI = "com.nummist.secondsight.LabActivity.extra.PHOTO_URI"
        val EXTRA_PHOTO_DATA_PATH = "com.nummist.secondsight.LabActivity.extra.PHOTO_DATA_PATH"
    }

    private var mUri: Uri? = null
    private var mDataPath: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_lab)
        var intent = intent
        mUri = intent.getParcelableExtra(EXTRA_PHOTO_URI)
        mDataPath = intent.getStringExtra(EXTRA_PHOTO_DATA_PATH)
        val imageView = ImageView(this)
        imageView.setImageURI(mUri)
        setContentView(imageView)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_lab, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_delete -> {
                deletePhoto()
                return true
            }
            R.id.menu_edit -> {
                editPhoto()
                return true
            }
            R.id.menu_share -> {
                sharePhoto()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun deletePhoto() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle(R.string.photo_delete_prompt_title)
        alert.setMessage(R.string.photo_delete_prompt_message)
        alert.setCancelable(false)
        alert.setPositiveButton(R.string.delete) { _, _ ->
            contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.MediaColumns.DATA + "=?", arrayOf(mDataPath))
            finish()
        }

    }

    private fun editPhoto() {
        val intent = Intent(Intent.ACTION_EDIT)
        intent.setDataAndType(mUri, PHOTO_MIME_TYPE)
        startActivity(Intent.createChooser(intent, getString(R.string.photo_edit_chooser_title)))

    }

    private fun sharePhoto() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = PHOTO_MIME_TYPE
        intent.putExtra(Intent.EXTRA_STREAM, mUri)
        intent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.photo_send_extra_subject))
        intent.putExtra(Intent.EXTRA_TEXT,getString(R.string.photo_send_extra_text))
        startActivity(Intent.createChooser(intent,getString(R.string.photo_send_chooser_title)))
    }

}