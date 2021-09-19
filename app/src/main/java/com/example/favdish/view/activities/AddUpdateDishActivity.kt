package com.example.favdish.view.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.favdish.R
import com.example.favdish.databinding.ActivityAddUpdateDishBinding
import com.example.favdish.databinding.DialogCustomImageSelectionBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddUpdateDishBinding

    private val takeCameraImage = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val thumbnail: Bitmap = result.data!!.extras!!.get("data") as Bitmap
                Glide.with(this)
                        .load(thumbnail)
                        .centerCrop()
                        .into(binding.ivDishImage)
                binding.ivAddDishImage.setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.ic_edit_24))
            }
        }
    private val takeGalleryImage = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Glide.with(this)
                .load(result.data!!.data)
                .centerCrop()
                .into(binding.ivDishImage)
            binding.ivAddDishImage.setImageDrawable(ContextCompat.getDrawable(this,
                R.drawable.ic_edit_24))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        binding.ivAddDishImage.setOnClickListener(this)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarAddDishActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddDishActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.iv_add_dish_image ->{
                   customImageSelectedDialog()
                }
            }
        }
    }

    private fun customImageSelectedDialog() {
        val dialog = Dialog(this)
        val binding: DialogCustomImageSelectionBinding =
            DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.tvCamera.setOnClickListener {
            Dexter.withContext(this@AddUpdateDishActivity).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener( object: MultiplePermissionsListener{
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        p0?.let {
                            if (p0.areAllPermissionsGranted()) {
                                takeCameraImage.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                     p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread().check()
            dialog.dismiss()
        }

        binding.tvGallery.setOnClickListener {
            Dexter.withContext(this@AddUpdateDishActivity).withPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener( object: PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    takeGalleryImage.launch(Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(this@AddUpdateDishActivity,
                        "you don't have gallery permission", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }).onSameThread().check()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRationalDialogForPermissions() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage("You probably turned off permissions, Turn on all in app settings")
            .setPositiveButton(
                "Go to settings"
            ) { _,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }.show()
    }

}