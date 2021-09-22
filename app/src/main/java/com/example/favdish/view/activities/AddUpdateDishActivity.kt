package com.example.favdish.view.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.favdish.R
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.ActivityAddUpdateDishBinding
import com.example.favdish.databinding.DialogCustomImageSelectionBinding
import com.example.favdish.databinding.DialogCustomListBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.utils.Constants
import com.example.favdish.view.adapters.CustomListItemAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddUpdateDishBinding
    private var mImagePath: String = ""
    private lateinit var mCustomListDialog: Dialog
    private val mFavDishViewModel: FavDishViewModel by viewModels{
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }

    private val takeCameraImage = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val thumbnail: Bitmap = result.data!!.extras!!.get("data") as Bitmap
                Glide.with(this)
                        .load(thumbnail)
                        .centerCrop()
                        .into(binding.ivDishImage)
                mImagePath = saveImageToInternalStorage(thumbnail)
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
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object: RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("ups", "Error loading image")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let{
                            val bitmap: Bitmap = resource.toBitmap()
                            mImagePath = saveImageToInternalStorage(bitmap)
                            Log.e("ups", mImagePath)
                        }
                        return false
                    }
                })
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
        binding.etCategory.setOnClickListener(this)
        binding.etType.setOnClickListener(this)
        binding.etCookingTime.setOnClickListener(this)
        binding.btnAddDish.setOnClickListener(this)
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
                R.id.et_category ->{
                    customItemsListDialog(resources.getString(R.string.title_select_dish_category),
                        Constants.dishCategories(), Constants.DISH_CATEGORY)
                }
                R.id.et_type ->{
                    customItemsListDialog(resources.getString(R.string.title_select_dish_type),
                        Constants.dishTypes(), Constants.DISH_TYPE)
                }
                R.id.et_cooking_time ->{
                    customItemsListDialog(resources.getString(R.string.title_select_dish_cooking_time),
                        Constants.dishCookTime(), Constants.DISH_COOKING_TIME)
                }
                R.id.btn_add_dish ->{
                    val title = binding.etTitle.text.toString().trim{ it <= ' '}
                    val type = binding.etType.text.toString().trim{ it <= ' '}
                    val category = binding.etCategory.text.toString().trim{ it <= ' '}
                    val ingredients = binding.etIngredients.text.toString().trim{ it <= ' '}
                    val cookingTimesInMinutes = binding.etCookingTime.text.toString().trim{ it <= ' '}
                    val cookingDirection = binding.etDirectionToCook.text.toString().trim{ it <= ' '}
                    when {
                        TextUtils.isEmpty(mImagePath) ->{
                            Toast.makeText(this@AddUpdateDishActivity,
                                "Select dish image", Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(title) ->{
                            Toast.makeText(this@AddUpdateDishActivity,
                                "Select dish title", Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(type) ->{
                            Toast.makeText(this@AddUpdateDishActivity,
                                "Select dish type", Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(category) ->{
                            Toast.makeText(this@AddUpdateDishActivity,
                                "Select dish category", Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(ingredients) ->{
                            Toast.makeText(this@AddUpdateDishActivity,
                                "Select dish ingredients", Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(cookingTimesInMinutes) ->{
                            Toast.makeText(this@AddUpdateDishActivity,
                                "Select cooking time", Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(cookingDirection) ->{
                            Toast.makeText(this@AddUpdateDishActivity,
                                "Select cooking directory", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val favDishDetails = FavDish(
                                mImagePath,
                                Constants.DISH_IMAGE_SOURCE_LOCAL,
                                title,
                                type,
                                category,
                                ingredients,
                                cookingTimesInMinutes,
                                cookingDirection,
                                false
                            )
                            mFavDishViewModel.insert(favDishDetails)
                            Toast.makeText(this@AddUpdateDishActivity,
                                "You successfully added your favorite dish details",
                                Toast.LENGTH_SHORT).show()
                            Log.e("Insertion", "Success")
                            finish()
                        }
                    }
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

    fun selectedListItem(item: String, selection: String) {
        when (selection) {
            Constants.DISH_TYPE ->{
                mCustomListDialog.dismiss()
                binding.etType.setText(item)
            }
            Constants.DISH_CATEGORY ->{
                mCustomListDialog.dismiss()
                binding.etCategory.setText(item)
            }
            Constants.DISH_COOKING_TIME ->{
                mCustomListDialog.dismiss()
                binding.etCookingTime.setText(item)
            }
        }
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

    private fun saveImageToInternalStorage(bitmap: Bitmap):String{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    private fun customItemsListDialog(title: String, itemsList: List<String>, selection: String) {
        mCustomListDialog = Dialog(this)
        val binding: DialogCustomListBinding =
            DialogCustomListBinding.inflate(layoutInflater)
        mCustomListDialog.setContentView(binding.root)
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(this)
        val adapter = CustomListItemAdapter(this, itemsList, selection)
        binding.rvList.adapter = adapter
        mCustomListDialog.show()
    }

    companion object{
        private const val IMAGE_DIRECTORY = "FavDishImages"
    }

}