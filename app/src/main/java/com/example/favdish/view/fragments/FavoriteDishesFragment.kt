package com.example.favdish.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.FragmentFavoriteDishesBinding
import com.example.favdish.viewmodel.DashboardViewModel
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory

class FavoriteDishesFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var binding: FragmentFavoriteDishesBinding? = null
    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        binding = FragmentFavoriteDishesBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        val textView: TextView = binding!!.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFavDishViewModel.favoriteDishes.observe(viewLifecycleOwner){
            dishes ->
            dishes.let {
                if (it.isNotEmpty()) {
                    for (dish in it) {
                        Log.e("ups", "${dish.id} :: ${dish.title}")
                    }
                }else{
                    Log.e("ups", "is empty")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}