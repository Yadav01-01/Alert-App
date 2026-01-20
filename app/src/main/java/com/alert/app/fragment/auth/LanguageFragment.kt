package com.alert.app.fragment.auth
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.adapter.CountryLanguageAdapter
import com.alert.app.base.BaseApplication
import com.alert.app.databinding.FragmentLanguageBinding
import com.alert.app.errormessage.MessageClass
import com.alert.app.listener.OnClickEvent
import com.alert.app.model.CountryLanguageModel

class LanguageFragment : Fragment(), OnClickEvent {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private val languageList = mutableListOf(
        CountryLanguageModel("English", R.drawable.usa_icon, false),
        CountryLanguageModel("Spanish", R.drawable.spain_icon, false),
        CountryLanguageModel("French", R.drawable.france_icon, false)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackPressHandler()
        setupUIListeners()
        setupLanguageDropdown()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateTo(R.id.selectCountryFragment)
            }
        })
    }

    private fun setupUIListeners() {
        binding.apply {
            imgBack.setOnClickListener { navigateTo(R.id.selectCountryFragment) }
            btnSkip.setOnClickListener { navigateTo(R.id.profileFragment) }
            btnNext.setOnClickListener { handleNextButtonClick() }
            layDropdown.setOnClickListener { toggleDropdownVisibility() }
        }
    }

    private fun setupLanguageDropdown() {
        binding.rcyCountry.adapter = CountryLanguageAdapter(requireContext(), languageList, this)
    }

    private fun handleNextButtonClick() {
        if (binding.tvLanguage.text.toString().equals(getString(R.string.select_language), true)) {
            BaseApplication.alertError(context, MessageClass.selectLanguageError, false)
        } else {
            navigateTo(R.id.profileFragment)
        }
    }

    private fun toggleDropdownVisibility() {
        binding.rcyDrop.visibility = if (binding.rcyDrop.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun navigateTo(destinationId: Int) {
        findNavController().navigate(destinationId)
    }

    override fun onClick(data: String) {
        val languageName = data.split("@")[0]
        binding.tvLanguage.text = languageName
        binding.rcyDrop.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

