package com.example.dictionary.ui.definitions

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dictionary.R
import com.example.dictionary.databinding.ItemLayoutDefinitionBinding
import com.example.dictionary.network.model.DefinitionModel

class DefinitionRVAdapter(
    private val context: Context
) : RecyclerView.Adapter<DefinitionRVAdapter.DefinitionViewHolder>() {
    inner class DefinitionViewHolder(val binding: ItemLayoutDefinitionBinding) : RecyclerView.ViewHolder(binding.root)

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
        with (holder.binding) {
            tvDefinition.text = definitionModel.definition

            with (definitionModel.synonyms) {
                if (this == null) {
                    tvSynonyms.isVisible = false
                } else {
                    for (i in indices) {
                        tvSynonyms.append("${this[i]}\n")
                    }
                }
            }
            if (definitionModel.example != null)
                tvExample.text = context.getString(R.string.example, definitionModel.example)
            else
                tvExample.isVisible = false
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}