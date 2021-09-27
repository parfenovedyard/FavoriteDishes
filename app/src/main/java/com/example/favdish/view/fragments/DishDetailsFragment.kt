package com.example.favdish.view.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.favdish.R
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.FragmentDishDetailsBinding
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import java.io.IOException
import java.util.*

class DishDetailsFragment : Fragment() {

    private var binding: FragmentDishDetailsBinding? = null
    private val mFavDishModelFactory: FavDishViewModel by viewModels {
        FavDishViewModelFactory(((requireActivity().application) as FavDishApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDishDetailsBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: DishDetailsFragmentArgs by navArgs()
        args.let {
            try {
                Glide.with(requireActivity())
                    .load(it.dishDetails.image)
                    .centerCrop()
                    .listener(object: RequestListener<Drawable>{
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("ups", "error loading Image")
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            resource.let {
                            Palette.from(resource!!.toBitmap()).generate() { palette ->
                                val intColor = palette?.vibrantSwatch?.rgb ?: 0
                                binding!!.rlDishDetailMain.setBackgroundColor(intColor)
                                }
                            }
                            return false
                        }
                    })
                    .into(binding!!.ivDishImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            binding!!.tvTitle.text = it.dishDetails.title
            binding!!.tvType.text =
                it.dishDetails.type.capitalize(Locale.ROOT)
            binding!!.tvCategory.text = it.dishDetails.category
            binding!!.tvIngredients.text = it.dishDetails.ingredients
            binding!!.tvCookingDirection.text = it.dishDetails.directionToCook
            binding!!.tvCookingTime.text =
                resources.getString(R.string.lbl_estimate_cooking_time, it.dishDetails.cookingTime)
            if (args.dishDetails.favoriteDish) {
                binding!!.ivFavoriteDish.setImageDrawable(ContextCompat.getDrawable(
                    requireActivity(), R.drawable.ic_favorite_selected))
            }else{
                binding!!.ivFavoriteDish.setImageDrawable(ContextCompat.getDrawable(
                    requireActivity(), R.drawable.ic_favorite_unselected))
            }

        }

        binding!!.ivFavoriteDish.setOnClickListener {
            args.dishDetails.favoriteDish = !args.dishDetails.favoriteDish
            mFavDishModelFactory.update(args.dishDetails)
            if (args.dishDetails.favoriteDish) {
                binding!!.ivFavoriteDish.setImageDrawable(ContextCompat.getDrawable(
                    requireActivity(), R.drawable.ic_favorite_selected))
                Toast.makeText(requireActivity(), "Added to favorite", Toast.LENGTH_SHORT).show()
            }else{
                binding!!.ivFavoriteDish.setImageDrawable(ContextCompat.getDrawable(
                    requireActivity(), R.drawable.ic_favorite_unselected))
                Toast.makeText(requireActivity(), "Removed from favorite", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}