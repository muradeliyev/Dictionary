package com.example.dictionary.ui.definitions

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dictionary.R
import com.example.dictionary.databinding.ItemLayoutDefinitionBinding
import com.example.dictionary.network.model.DefinitionModel
import org.greenrobot.eventbus.EventBus

class DefinitionRVAdapter(
    private val context: Context
) : RecyclerView.Adapter<DefinitionRVAdapter.DefinitionViewHolder>() {
    inner class DefinitionViewHolder(val binding: ItemLayoutDefinitionBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<DefinitionModel>() {
        override fun areItemsTheSame(oldItem: DefinitionModel, newItem: DefinitionModel): Boolean {
            return oldItem.definition == newItem.definition
        }

        override fun areContentsTheSame(
            oldItem: DefinitionModel,
            newItem: DefinitionModel
        ): Boolean {
            return oldItem == newItem
        }
    }

    private var differ = AsyncListDiffer(this, diffCallback)
    var list: List<DefinitionModel>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefinitionViewHolder {
        return DefinitionViewHolder(
            ItemLayoutDefinitionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DefinitionViewHolder, position: Int) {
        val definitionModel = differ.currentList[position]
        with(holder.binding) {
            tvDefinition.text = definitionModel.definition

            definitionModel.synonyms?.let {
                it.forEach { synonym ->
                    val synonymView = LayoutInflater.from(context)
                        .inflate(R.layout.item_synonym, null) as TextView

                    if (!synonym.contains(" ")) {
                        synonymView.setOnClickListener {
                            EventBus.getDefault().post(synonym)
                        }
                        synonymView.text = context.getString(R.string.underlined_text, synonym)
                    } else synonymView.text = synonym

                    llSynonyms.addView(synonymView)
                }
            }

            // setting Examples for the definition
            if (definitionModel.example != null) tvExample.text =
                context.getString(R.string.example, definitionModel.example)
            else tvExample.isVisible = false
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}