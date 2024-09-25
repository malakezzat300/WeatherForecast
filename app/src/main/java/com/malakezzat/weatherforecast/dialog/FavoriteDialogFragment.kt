package com.malakezzat.weatherforecast.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.databinding.DialogFavoriteBinding
import com.malakezzat.weatherforecast.location.OsmMapFragment

class FavoriteDialogFragment : DialogFragment() {

    private var _binding: DialogFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : Editor

    companion object {
        fun newInstance(listener: FavoriteDialogListener): FavoriteDialogFragment {
            val fragment = FavoriteDialogFragment()
            fragment.listener = listener
            return fragment
        }
    }

    interface FavoriteDialogListener {
        fun onDialogPositiveClick()
    }

    private lateinit var listener: FavoriteDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(requireContext())

            sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.my_preference), Context.MODE_PRIVATE)
            editor = sharedPreferences.edit()

            _binding = DialogFavoriteBinding.inflate(layoutInflater)

            binding.addButton.setOnClickListener {

                listener.onDialogPositiveClick()
                dismiss()
            }

            binding.mapFavoriteButton.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction()
                    .add(android.R.id.content, OsmMapFragment(false),"OsmMapFragment").commit()
                listener.onDialogPositiveClick()
                dismiss()
            }

            builder.setView(binding.root)
            builder.create()


        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}