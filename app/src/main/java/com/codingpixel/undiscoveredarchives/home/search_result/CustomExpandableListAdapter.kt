package com.codingpixel.undiscoveredarchives.home.search_result

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.codingpixel.undiscoveredarchives.R

class CustomExpandableListAdapter(
    private val context: Context,
    private val titleList: MutableList<String>,
    private val dataList: HashMap<String, MutableList<String>>
) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return this.dataList[this.titleList[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(
        listPosition: Int,
        expandedListPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertViews = convertView
        val expandedListText = getChild(listPosition, expandedListPosition) as String
        if (convertViews == null) {
            val layoutInflater =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertViews = layoutInflater.inflate(R.layout.item_expandable_list_item, null)
        }
        val expandedListTextView = convertViews!!.findViewById<CheckBox>(R.id.checkbox)
        expandedListTextView.text = expandedListText
        return convertViews
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.dataList[this.titleList[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleList[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.titleList.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {

        var convertViews = convertView
        val listTitle = getGroup(listPosition) as String
        if (convertViews == null) {
            val layoutInflater =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertViews = layoutInflater.inflate(R.layout.item_expandable_list_header, null)
        }
        val arrow = convertView?.findViewById<ImageView>(R.id.ivArrow)
        if (isExpanded) {
            arrow?.setImageResource(R.drawable.ic_arrow_up_solid);
        } else {
            arrow?.setImageResource(R.drawable.ic_arrow_down);
        }
        val listTitleTextView = convertViews!!.findViewById<TextView>(R.id.listTitle)
        listTitleTextView.text = listTitle

        return convertViews
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}