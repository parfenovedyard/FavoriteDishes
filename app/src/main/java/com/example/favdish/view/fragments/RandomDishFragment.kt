package com.example.favdish.view.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.favdish.R
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.FragmentRandomDishBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.model.entities.RandomDish
import com.example.favdish.utils.Constants
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory
import com.example.favdish.viewmodel.RandomDishViewModel


class RandomDishFragment : Fragment() {

    private var binding: FragmentRandomDishBinding? = null
    private lateinit var mRandomDishViewModel: RandomDishViewModel
    private var mProgressDialog: Dialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRandomDishBinding.inflate(layoutInflater)
        return binding!!.root
    }

    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(requireActivity())
        mProgressDialog?.let {
            it.setContentView(R.layout.dialog_custom_progress)
            it.show()
        }
    }
    private fun hideCustomProgressDialog() {
        mProgressDialog?.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRandomDishViewModel = ViewModelProvider(this).get(RandomDishViewModel::class.java)
        mRandomDishViewModel.getRandomRecipeFromApi()
        randomDishViewModelObserver()
        binding!!.srlRandomDish.setOnRefreshListener {
            mRandomDishViewModel.getRandomRecipeFromApi()
        }
    }

    private fun randomDishViewModelObserver() {
        mRandomDishViewModel.randomDishResponse.observe(viewLifecycleOwner,{ randomDishResponse ->
            randomDishResponse?.let {
                Log.i("ups", "${randomDishResponse.recipes[0]}")
                Log.i("ups", "${randomDishResponse.recipes[0].extendedIngredients}")
                if (binding!!.srlRandomDish.isRefreshing) {
                    binding!!.srlRandomDish.isRefreshing = false
                }
                setRandomDishResponseInUI(randomDishResponse.recipes[0])
            }
        })
        mRandomDishViewModel.randomDishLoadingError.observe(viewLifecycleOwner,{ dataError ->
            dataError?.let {
                Log.e("ups", "$dataError")
                if (binding!!.srlRandomDish.isRefreshing) {
                    binding!!.srlRandomDish.isRefreshing = false
                }
            }
        })
        mRandomDishViewModel.loadRandomDish.observe(viewLifecycleOwner,{ loadRandomDish ->
            loadRandomDish?.let {
                Log.e("ups", "$loadRandomDish")
                if (loadRandomDish && !binding!!.srlRandomDish.isRefreshing) {
                    showCustomProgressDialog()
                }else{
                    hideCustomProgressDialog()
                }
            }
        })
    }

    private fun setRandomDishResponseInUI(recipe: RandomDish.Recipe) {
        Glide.with(requireContext())
            .load(recipe.image)
            .centerCrop()
            .into(binding!!.ivDishImageRandom)
        var dishType = "other"
        if (recipe.dishTypes.isNotEmpty()) {
            dishType = recipe.dishTypes[0]
            binding!!.tvTypeRandom.text = dishType
        }
        binding!!.tvCategoryRandom.text = "other"

        var ingredients = ""
        for (value in recipe.extendedIngredients) {
            ingredients = if (ingredients.isNotEmpty()) {
                value.original
            }else{
                ingredients + ", \n" + value.original
            }
        }
        binding!!.tvIngredientsRandom.text = ingredients

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding!!.tvCookingDirectionRandom.text = Html.fromHtml(
                recipe.instructions,
                Html.FROM_HTML_MODE_COMPACT
            )
        }else{
            @Suppress("DEPRECATION")
            binding!!.tvCookingDirectionRandom.text = Html.fromHtml(recipe.instructions)
        }

        binding!!.ivFavoriteDishRandom.setImageDrawable(
            ContextCompat.getDrawable(requireActivity(),R.drawable.ic_favorite_unselected)
        )
        var addedToFavorite = false

        binding!!.tvCookingTimeRandom.text = resources.getString(R.string.lbl_estimate_cooking_time,
        recipe.readyInMinutes.toString())

        binding!!.ivFavoriteDishRandom.setOnClickListener {
            if (addedToFavorite) {
                Toast.makeText(requireActivity(), resources.getString(R.string.msg_added_to_favorite)
                    , Toast.LENGTH_SHORT).show()
            }else{
                val randomDishDetails = FavDish(
                    recipe.image,
                    Constants.DISH_IMAGE_SOURCE_ONLINE,
                    recipe.title,
                    dishType,
                    "Other",
                    ingredients,
                    recipe.readyInMinutes.toString(),
                    recipe.instructions,
                    true
                )
                val mFavDishViewModel: FavDishViewModel by viewModels {
                    FavDishViewModelFactory((requireActivity()
                        .application as FavDishApplication).repository)
                }
                mFavDishViewModel.insert(randomDishDetails)
                addedToFavorite = true
                binding!!.ivFavoriteDishRandom.setImageDrawable(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.ic_favorite_selected)
                )
                Toast.makeText(requireActivity(), "Add to favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}