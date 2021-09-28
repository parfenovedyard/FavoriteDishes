package com.example.favdish.view.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.favdish.R
import com.example.favdish.application.FavDishApplication
import com.example.favdish.databinding.DialogCustomListBinding
import com.example.favdish.databinding.FragmentAllDishesBinding
import com.example.favdish.model.entities.FavDish
import com.example.favdish.utils.Constants
import com.example.favdish.view.activities.AddUpdateDishActivity
import com.example.favdish.view.activities.MainActivity
import com.example.favdish.view.adapters.CustomListItemAdapter
import com.example.favdish.view.adapters.FavDishAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory

class AllDishesFragment : Fragment() {

    private lateinit var binding: FragmentAllDishesBinding
    private lateinit var mFavDishAdapter: FavDishAdapter
    private lateinit var mCustomListDialog: Dialog
    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllDishesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDishesList.layoutManager = GridLayoutManager(requireActivity(), 2)
        mFavDishAdapter = FavDishAdapter(this@AllDishesFragment)
        binding.rvDishesList.adapter = mFavDishAdapter

        mFavDishViewModel.allDishesList.observe(viewLifecycleOwner) {
            dishes ->
                dishes.let {
                    if (it.isNotEmpty()) {
                        binding.rvDishesList.visibility = View.VISIBLE
                        binding.tvNoDishesAddedYet.visibility = View.GONE
                        mFavDishAdapter.dishesList(it)
                    }else{
                        binding.rvDishesList.visibility = View.GONE
                        binding.tvNoDishesAddedYet.visibility = View.VISIBLE
                    }
                }
        }
    }

    fun dishDetails(favDish: FavDish) {
        findNavController().navigate(AllDishesFragmentDirections.actionAllDishesToDishDetails(favDish))
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)?.hideBottomNavigationView()
        }
    }

    fun deleteDish(dish: FavDish) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(resources.getString(R.string.title_delete_dish))
        builder.setMessage(resources.getString(R.string.msg_delete_dish_dialog, dish.title))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(resources.getString(R.string.lbl_yes)){ dialogInterface, _ ->
            mFavDishViewModel.delete(dish)
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(resources.getString(R.string.lbl_no)){dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun filterDishesListDialog() {
        mCustomListDialog = Dialog(requireActivity())
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        mCustomListDialog.setContentView(binding.root)
        binding.tvTitle.text = resources.getString(R.string.select_item_to_filter)
        val dishTypes = Constants.dishTypes()
        dishTypes.add(0,  Constants.ALL_ITEMS)
        binding.rvList.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = CustomListItemAdapter(requireActivity(), this@AllDishesFragment,
            dishTypes, Constants.FILTER_SELECTION)
        binding.rvList.adapter = adapter
        mCustomListDialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)?.showBottomNavigationView()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_all_dishes, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_dish -> {
                startActivity(Intent(requireActivity(), AddUpdateDishActivity::class.java))
                return true
            }
            R.id.action_filter_dish ->{
                filterDishesListDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun filterItemSelection(filterItemSelection: String) {
        mCustomListDialog.dismiss()
        Log.e("ups", filterItemSelection)
        if (filterItemSelection == Constants.ALL_ITEMS) {
            mFavDishViewModel.allDishesList.observe(viewLifecycleOwner) {
                    dishes ->
                dishes.let {
                    if (it.isNotEmpty()) {
                        binding.rvDishesList.visibility = View.VISIBLE
                        binding.tvNoDishesAddedYet.visibility = View.GONE
                        mFavDishAdapter.dishesList(it)
                    }else{
                        binding.rvDishesList.visibility = View.GONE
                        binding.tvNoDishesAddedYet.visibility = View.VISIBLE
                    }
                }
            }
        }else{
            Log.e("ups", "Get filter list")
        }
    }
}