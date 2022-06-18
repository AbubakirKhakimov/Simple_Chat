package com.x.a_technologies.simple_chat.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.x.a_technologies.simple_chat.R
import com.x.a_technologies.simple_chat.adapters.ChooseCountryAdapter
import com.x.a_technologies.simple_chat.adapters.ChooseCountryAdapterCallBack
import com.x.a_technologies.simple_chat.databinding.FragmentSelectCountryCodeBinding
import com.x.a_technologies.simple_chat.models.PhoneMask
import com.x.a_technologies.simple_chat.utils.PhoneMaskManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectCountryCodeFragment : Fragment(), ChooseCountryAdapterCallBack {

    private lateinit var binding: FragmentSelectCountryCodeBinding
    private lateinit var chooseCountryAdapter: ChooseCountryAdapter
    private val phoneMasksList = ArrayList<PhoneMask>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSelectCountryCodeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chooseCountryAdapter = ChooseCountryAdapter(phoneMasksList, this)
        binding.countriesRv.adapter = chooseCountryAdapter

        getPhoneMasks()

        binding.backStack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.search.addTextChangedListener {
            chooseCountryAdapter.filter.filter(it.toString())
        }

    }

    private fun getPhoneMasks(){
        lifecycleScope.launch{
            withContext(Dispatchers.Default){
                phoneMasksList.apply {
                    clear()
                    addAll(PhoneMaskManager().loadPhoneMusk())
                }
            }

            chooseCountryAdapter.notifyDataSetChanged()
        }
    }

    override fun itemSelected(position: Int) {
        setFragmentResult("getPhoneMaskPosition", bundleOf("position" to position))
        findNavController().popBackStack()
    }

}