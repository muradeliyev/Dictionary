package com.example.dictionary.ui.definitions

import android.content.Context
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dictionary.R
import com.example.dictionary.databinding.FragmentDefinitionsBinding
import com.example.dictionary.network.model.DictionarySingleResponseModel
import com.example.dictionary.network.model.PhoneticsModel
import com.example.dictionary.ui.main.IMainActivity
import com.example.dictionary.utils.State
import com.example.dictionary.viewmodels.DictionaryViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class DefinitionsFragment : Fragment(R.layout.fragment_definitions) {

    private val viewModel: DictionaryViewModel by activityViewModels()

    private lateinit var binding: FragmentDefinitionsBinding
    private lateinit var defAdapter: DefinitionRVAdapter
    private lateinit var audioLink: String

    private val args: DefinitionsFragmentArgs by navArgs()

    private var mediaPlayer: MediaPlayer? = null

    private val TAG = "DEFINITIONSFRAGMENT"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDefinitionsBinding.bind(view)

        defAdapter = DefinitionRVAdapter(requireContext())

        val iMainActivity = activity as IMainActivity
        val toolbar = iMainActivity.getToolbar()
        toolbar.setNavigationIcon(R.drawable.ic_round_arrow_back_ios_24)
        toolbar.title = "Search"
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        setupRecyclerView()
        viewModel.getDefinition(args.word)
        setupObservers()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun setupObservers() {
        viewModel.definition.observe(viewLifecycleOwner) { state ->
            when (state) {
                is State.Loading -> setLoadingState()
                is State.NoInternet -> setNoInternetState()
                is State.Error -> setNoResultState(state.message)
                is State.Success<*> -> {
                    binding.searchAnimation.visibility = View.GONE

                    val data = state.data as DictionarySingleResponseModel

                    binding.tvWord.text = data.word
                    binding.tvPartOfSpeech.text = requireContext().getString(
                        R.string.part_of_speech,
                        data.meanings[0].partOfSpeech
                    )
                    defAdapter.list = data.meanings[0].definitions
                    audioLink = data.phonetics[0].audioUrl.toString()

                    if (!audioLink.startsWith("https://"))
                        audioLink = "https://$audioLink"

                    bindClickablePronunciationText(data.phonetics)
                }
            }
        }
    }

    private fun setLoadingState() {
        binding.clError.visibility = View.GONE
        binding.tvPhoneticsText.visibility = View.GONE
        binding.searchAnimation.visibility = View.VISIBLE
    }

    private fun setNoResultState(word: String) = with(binding) {
        clError.visibility = View.VISIBLE
        tvPhoneticsText.visibility = View.GONE
        clError.visibility = View.VISIBLE
        lotErrorAnimation.setAnimation(R.raw.no_result_cloud_animation)
        tvErrorMessage.text = requireContext().getString(R.string.not_found, word)
        searchAnimation.visibility = View.GONE
    }

    private fun bindClickablePronunciationText(phoneticsList: List<PhoneticsModel>) {
        if (phoneticsList.isEmpty())
            return

        val phonetics = phoneticsList[0]

        binding.tvPhoneticsText.apply {
            visibility = View.VISIBLE
            text = phonetics.text

            icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_pronounce)
            iconSize = 60
            iconPadding = 0
            setIconTintResource(R.color.light_bluish)

            mediaPlayer = MediaPlayer()
            try {
                mediaPlayer!!.setDataSource(audioLink)
                mediaPlayer!!.prepareAsync()
            } catch (e: IOException) {
                Snackbar.make(this, "Some error", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            setOnClickListener {
                Log.d(TAG, "bindClickablePronunciationText: $audioLink")
                try {
                    mediaPlayer!!.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupRecyclerView() = binding.rvMeanings.apply {
        adapter = defAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setNoInternetState() = with(binding) {
        clError.visibility = View.GONE
        rvMeanings.visibility = View.GONE
        searchAnimation.visibility = View.GONE
        clError.visibility = View.VISIBLE
        lotErrorAnimation.setAnimation(R.raw.no_internet_animation)
        tvErrorMessage.text = requireContext().getString(R.string.no_internet_connection)
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

}