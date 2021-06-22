package com.example.dictionary.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dictionary.R
import com.example.dictionary.databinding.FragmentSearchBinding
import com.example.dictionary.ui.main.IMainActivity


class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var binding: FragmentSearchBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        // search box gets focus on view create
        binding.etSearch.requestFocus()

        val iMainActivity = activity as IMainActivity
        val toolbar = iMainActivity.getToolbar()
        toolbar.navigationIcon = null
        toolbar.title = "Dictionary"

        openKeyboard()
        setupListeners()
    }

    private fun closeKeyboard() = activity?.currentFocus?.let { view ->
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun openKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setupListeners() = with(binding) {
        etSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val word = binding.etSearch.text.toString()
                    if (word.isNotEmpty()) {
                        val action = SearchFragmentDirections.actionSearchFragmentToDefinitionsFragment(word)

                        closeKeyboard()
                        findNavController().navigate(action)
                    }
                    return true
                }
                return false
            }
        })

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun onTextChanged(s: CharSequence?, after: Int, before: Int, count: Int) {
                s?.let {
                    if (s.isEmpty())
                        etSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24, 0, 0, 0)
                    else
                        etSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24, 0, R.drawable.ic_round_clear_24, 0)
                }
            }
            override fun afterTextChanged(p0: Editable?) = Unit
        })

        vDelete.setOnClickListener {
            etSearch.setText("")
        }
    }
}