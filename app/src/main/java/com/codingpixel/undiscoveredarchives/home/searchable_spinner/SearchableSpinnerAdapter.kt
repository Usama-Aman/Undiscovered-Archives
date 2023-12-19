package com.codingpixel.undiscoveredarchives.home.searchable_spinner

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemSearchableSpinnerBinding
import com.codingpixel.undiscoveredarchives.auth.models.SearchableSpinnerModel
import com.codingpixel.undiscoveredarchives.utils.viewGone

class SearchableSpinnerAdapter(
    var context: Context,
    val searchableList: ArrayList<SearchableSpinnerModel>,
    private val searchableSpinnerInterface: SearchableSpinnerInterface
) :
    RecyclerView.Adapter<SearchableSpinnerAdapter.ViewHolder>(), Filterable {

    private var filteredSearchableList = searchableList
    private lateinit var mContext: Context

    interface SearchableSpinnerInterface {
        fun onItemClicked(selectedId: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            DataBindingUtil.inflate(
                inflater, R.layout.item_searchable_spinner, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int = filteredSearchableList.size

    inner class ViewHolder(var binding: ItemSearchableSpinnerBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {
            binding.ivImage.viewGone()
            binding.tvName.text = filteredSearchableList[position].value

            if (searchableList[position].isSelected)
                binding.itemSpinner.setBackgroundColor(ContextCompat.getColor(mContext, R.color.lighter_grey))
            else
                binding.itemSpinner.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white))

            binding.root.setOnClickListener {
                searchableSpinnerInterface.onItemClicked(filteredSearchableList[position].id)
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults? {
                val charString = charSequence.toString()
                val filteredList: ArrayList<SearchableSpinnerModel?> = ArrayList()
                if (charString.isEmpty()) {
                    filteredList.addAll(searchableList)
                } else {
                    for (row in searchableList) {
                        if (row.value.contains(charString, true)) {
                            filteredList.add(row)
                        }
                    }
                }
                try {
                    val filterResults = FilterResults()
                    filterResults.values = filteredList
                    return filterResults
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                filteredSearchableList = filterResults.values as ArrayList<SearchableSpinnerModel>
                notifyDataSetChanged()
            }
        }
    }
}