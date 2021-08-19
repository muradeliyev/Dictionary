package com.example.dictionary.ui.definitions

import android.content.Context
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dictionary.R
import com.example.dictionary.databinding.FragmentDefinitionsBinding
import com.example.dictionary.network.model.DictionarySingleResponseModel
import com.example.dictionary.network.model.PhoneticsModel
import com.example.dictionary.network.util.RetrofitService
import com.example.dictionary.ui.main.IMainActivity
import com.google.android.material.snackbar.Snackbar
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception

class DefinitionsFragment : Fragment(R.layout.fragment_definitions) {
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
        initRequestToApi()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun initRequestToApi() {
        if (!isInternetAvailable())
            return

        lifecycleScope.launchWhenCreated {
            binding.clError.visibility = View.GONE
            binding.tvPhoneticsText.visibility = View.GONE
            binding.searchAnimation.visibility = View.VISIBLE

            val word = args.word
            val response: DictionarySingleResponseModel = try {
                RetrofitService.api.getDefinitions("en_US", word)[0]
            } catch (e: IOException) { // when there is no internet access
                setNoInternetState()
                return@launchWhenCreated
            } catch (e: HttpException) { // when no result is found
                binding.clError.visibility = View.VISIBLE
                binding.lotErrorAnimation.setAnimation(R.raw.no_result_cloud_animation)
                binding.tvErrorMessage.text = requireContext().getString(R.string.not_found, word)
                binding.searchAnimation.visibility = View.GONE
                return@launchWhenCreated
            }
            binding.searchAnimation.visibility = View.GONE

            binding.tvWord.text = response.word
            binding.tvPartOfSpeech.text = requireContext().getString(
                R.string.part_of_speech,
                response.meanings[0].partOfSpeech
            )
            defAdapter.list = response.meanings[0].definitions
            audioLink = response.phonetics[0].audioUrl.toString()

            if (!audioLink.startsWith("https://"))
                audioLink = "https://$audioLink"

            bindClickablePronunciationText(response.phonetics)
        }
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
            }
            catch (e: IOException) {
                Snackbar.make(this, "Some error", Snackbar.LENGTH_SHORT).show()
            }
            catch (e: Exception) {
                e.printStackTrace()
            }

            setOnClickListener {
                Log.d(TAG, "bindClickablePronunciationText: $audioLink")
                try {
                    mediaPlayer!!.start()
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupRecyclerView() = binding.rvMeanings.apply {
        adapter = defAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setNoInternetState() {
        binding.clError.visibility = View.GONE
        binding.rvMeanings.visibility = View.GONE
        binding.lotErrorAnimation.setAnimation(R.raw.no_internet_animation)
        binding.tvErrorMessage.text = requireContext().getString(R.string.no_internet_connection)
        binding.searchAnimation.visibility = View.GONE
    }

    private fun isInternetAvailable() : Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

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