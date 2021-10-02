package com.example.favdish.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.favdish.databinding.FragmentRandomDishBinding


class RandomDishFragment : Fragment() {

    private var binding: FragmentRandomDishBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRandomDishBinding.inflate(layoutInflater)

        return binding!!.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}