package com.codingpixel.undiscoveredarchives.home.designers

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.databinding.ItemSortingBinding
import com.codingpixel.undiscoveredarchives.home.main.models.DesignersData

class DesignersSortingAdapter(
    val designersList: MutableList<DesignersData>,
    val designerSortingInterface: DesignerSortingInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mContext: Context

    companion object {
        const val ALPHABET_TYPE = 0
        const val DESIGNER_TYPE = 0
    }

    interface DesignerSortingInterface {
        fun onItemClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        mContext = parent.context

        return if (viewType == DESIGNER_TYPE) DesignerViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_sorting,
                parent,
                false
            )
        )
        else
            AlphabetViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_sorting,
                    parent,
                    false
                )
            )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DesignerViewHolder -> holder.onBind(position)
            is AlphabetViewHolder -> holder.onBind(position)

        }
    }

    override fun getItemCount(): Int = designersList.size

    override fun getItemViewType(position: Int): Int = if (designersList[position].isAlphabet) ALPHABET_TYPE else DESIGNER_TYPE

    inner class DesignerViewHolder(var binding: ItemSortingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {

            binding.tvDesignerName.typeface = Typeface.createFromAsset(
                (mContext as AppCompatActivity).assets,
                "roboto_regular.ttf"
            )

            binding.tvDesignerName.text = designersList[position].name

            itemView.setOnClickListener {
                designerSortingInterface.onItemClicked(position)
            }
        }
    }

    inner class AlphabetViewHolder(var binding: ItemSortingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(position: Int) {

            binding.tvDesignerName.typeface = Typeface.createFromAsset(
                (mContext as AppCompatActivity).assets,
                "roboto_bold.ttf"
            )

            binding.tvDesignerName.text = designersList[position].name

        }
    }
}