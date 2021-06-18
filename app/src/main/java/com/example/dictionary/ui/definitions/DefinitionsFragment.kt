package com.example.dictionary.ui.definitions

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dictionary.R
import com.example.dictionary.databinding.FragmentDefinitionsBinding
import com.example.dictionary.network.model.DictionarySingleResponseModel
import com.example.dictionary.network.model.PhoneticsModel
import com.example.dictionary.network.util.RetrofitService
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

        setupRecyclerView()
        initRequestToApi()
    }

    private fun initRequestToApi() {
        lifecycleScope.launchWhenCreated {
            binding.clError.isVisible = false
            binding.tvPhoneticsText.isVisible = false
            binding.progressBar.isVisible = true

            val word = args.word
            val response: DictionarySingleResponseModel = try {
                RetrofitService.api.getDefinitions("en_US", word)[0]
            }
            catch (e: IOException) { // when there is no internet access
                binding.clError.isVisible = true
                binding.ivErrorIcon.setImageResource(R.drawable.ic_no_internet)
                binding.tvErrorMessage.text = requireContext().getString(R.string.no_internet_connection)
                binding.progressBar.isVisible = false
                return@launchWhenCreated
            }
            catch (e: HttpException) { // when no result is found
                binding.clError.isVisible = true
                binding.ivErrorIcon.setImageResource(R.drawable.ic_not_found)
                binding.tvErrorMessage.text = requireContext().getString(R.string.not_found, word)
                binding.progressBar.isVisible = false
                return@launchWhenCreated
            }
            binding.progressBar.isVisible = false

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
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_24, 0, 0, 0)
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