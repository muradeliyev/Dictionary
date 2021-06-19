package com.example.dictionary.ui.definitions

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
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
import retrofit2.HttpException
import java.io.IOException


class DefinitionsFragment : Fragment(R.layout.fragment_definitions) {
    private lateinit var binding: FragmentDefinitionsBinding
    private val args: DefinitionsFragmentArgs by navArgs()
    private lateinit var defAdapter: DefinitionRVAdapter
    private lateinit var audioLink: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDefinitionsBinding.bind(view)

        defAdapter = DefinitionRVAdapter(requireContext())

        val iMainActivity = activity as IMainActivity
        val toolbar = iMainActivity.getToolbar()
        toolbar.setNavigationIcon(R.drawable.ic_round_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        setupRecyclerView()
        initRequestToApi()
    }

    private fun initRequestToApi() {
        lifecycleScope.launchWhenCreated {
            binding.clError.isVisible = false
            binding.tvPhoneticsText.isVisible = false
            binding.searchAnimation.isVisible = true

            val word = args.word
            val response: DictionarySingleResponseModel = try {
                RetrofitService.api.getDefinitions("en_US", word)[0]
            }
            catch (e: IOException) { // when there is no internet access
                binding.clError.isVisible = true
                binding.lotErrorAnimation.setAnimation(R.raw.no_internet_animation)
                binding.tvErrorMessage.text = requireContext().getString(R.string.no_internet_connection)
                binding.searchAnimation.isVisible = false
                return@launchWhenCreated
            }
            catch (e: HttpException) { // when no result is found
                binding.clError.isVisible = true
                binding.lotErrorAnimation.setAnimation(R.raw.no_result_cloud_animation)
                binding.tvErrorMessage.text = requireContext().getString(R.string.not_found, word)
                binding.searchAnimation.isVisible = false
                return@launchWhenCreated
            }
            binding.searchAnimation.isVisible = false

            binding.tvWord.text = response.word
            binding.tvPartOfSpeech.text = requireContext().getString(R.string.part_of_speech, response.meanings[0].partOfSpeech)
            defAdapter.list = response.meanings[0].definitions

            bindClickablePronunciationText(response.phonetics)
        }
    }

    private var isPlaying = false
    private fun bindClickablePronunciationText(phoneticsList: List<PhoneticsModel>) {
        if (phoneticsList.isEmpty())
            return

        val phonetics = phoneticsList[0]
        audioLink = phonetics.audioUrl.toString()

        binding.tvPhoneticsText.apply {
            isVisible = true
            text = phonetics.text

            icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_play_24)
            iconSize = 60
            iconPadding = 0
            setIconTintResource(R.color.light_bluish)

            setOnClickListener {
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
                mediaPlayer.setOnCompletionListener { isPlaying = false }

                if (!isPlaying) {
                    mediaPlayer.setDataSource(audioLink)
                    mediaPlayer.prepareAsync()
                } else {
                    mediaPlayer.release()
                }
                isPlaying = !isPlaying
            }
        }
    }

    private fun setupRecyclerView() = binding.rvMeanings.apply {
            adapter = defAdapter
            layoutManager = LinearLayoutManager(requireContext())
    }
}