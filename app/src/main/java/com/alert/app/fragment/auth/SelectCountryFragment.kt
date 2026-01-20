package com.alert.app.fragment.auth

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.adapter.CountryLanguageAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentSelectCountryBinding
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.OnClickEvent
import com.alert.app.model.CountryLanguageModel

class SelectCountryFragment : Fragment() , OnClickEvent {

    private lateinit var binding: FragmentSelectCountryBinding
    var list:MutableList<CountryLanguageModel> = mutableListOf()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSelectCountryBinding.inflate(layoutInflater, container, false)

        return binding.root
    }


    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.signUpFragment)
            }
        })


        binding.btnNext.setOnClickListener {
            if (binding.tvName.text.toString().equals(getString(R.string.select_country),true)) {
                BaseApplication.alertError(context, MessageClass.selectCountryError,false)
            }else{
                findNavController().navigate(R.id.languageFragment)
            }

        }

        binding.btnSkip.setOnClickListener {
            findNavController().navigate(R.id.languageFragment)
        }


        list.clear()

        list.add(CountryLanguageModel("+1 United States",R.drawable.usa_icon,false))
        list.add(CountryLanguageModel("+34 Spain",R.drawable.spain_icon,false))
        list.add(CountryLanguageModel("+33 France",R.drawable.france_icon,false))


        binding.rcyCountry.adapter= CountryLanguageAdapter(requireContext(),list,this)

        binding.layDropdown.setOnClickListener {
            if (binding.rcyDrop.visibility == View.VISIBLE) {
                binding.rcyDrop.visibility = View.GONE
            } else {
                binding.rcyDrop.visibility = View.VISIBLE
            }
        }




    }

    override fun onClick(data: String) {
        val parts = data.split("@")
        binding.tvName.text=parts[0]
        binding.tvName.setCompoundDrawablesWithIntrinsicBounds(parts[1].toInt(), 0, R.drawable.arrow_bottom, 0)
        binding.rcyDrop.visibility = View.GONE
    }


}