package com.example.favdish.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.favdish.databinding.FragmentRandomDishBinding
import com.example.favdish.viewmodel.RandomDishViewModel


class RandomDishFragment : Fragment() {

    private var binding: FragmentRandomDishBinding? = null
    private lateinit var mRandomDishViewModel: RandomDishViewModel
    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRandomDishBinding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRandomDishViewModel = ViewModelProvider(this).get(RandomDishViewModel::class.java)
        mRandomDishViewModel.getRandomRecipeFromApi()
        randomDishViewModelObserver()
    }

    private fun randomDishViewModelObserver() {
        mRandomDishViewModel.randomDishResponse.observe(viewLifecycleOwner,{ randomDishResponse ->
            randomDishResponse?.let {
                Log.i("ups", "${randomDishResponse.recipes[0]}")
            }
        })
        mRandomDishViewModel.randomDishLoadingError.observe(viewLifecycleOwner,{ dataError ->
            dataError?.let {
                Log.e("ups", "$dataError")
            }
        })
        mRandomDishViewModel.loadRandomDish.observe(viewLifecycleOwner,{ loadRandomDish ->
            loadRandomDish?.let {
                Log.e("ups", "$loadRandomDish")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}