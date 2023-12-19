package com.codingpixel.undiscoveredarchives.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.codingpixel.undiscoveredarchives.R


class CustomDropDownAdapter(context: Context, _dropDownList: MutableList<DropdownModel>) :
    ArrayAdapter<DropdownModel>(context, 0, _dropDownList), SpinnerAdapter {

    private var mContext = context
    private var dropDownList = _dropDownList

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var row = convertView
        row = LayoutInflater.from(mContext).inflate(R.layout.item_spinner_dropdown, parent, false)

        val textView: TextView = row.findViewById(R.id.tvSpinnerTitle)
        val bottomView: View = row.findViewById(R.id.view)

        textView.text = dropDownList[position].value

        if (dropDownList[position].isSelected)
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.black))
        else
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.view_more_grey))

        if (position == dropDownList.size - 1)
            bottomView.viewGone()
        else
            bottomView.viewVisible()
        return row
    }
}

data class DropdownModel(
    var value: String,
    var id: Int,
    var isSelected: Boolean = false,
)