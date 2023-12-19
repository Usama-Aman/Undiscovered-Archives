package com.codingpixel.undiscoveredarchives.home.sales

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.codingpixel.undiscoveredarchives.databinding.TabFavoriteFragmentBinding
import com.codingpixel.undiscoveredarchives.home.order_detail.OrderDetailActivity
import com.codingpixel.undiscoveredarchives.home.purchases.PurchaseAdapter
import com.codingpixel.undiscoveredarchives.utils.OrderDetailEnum
import com.codingpixel.undiscoveredarchives.utils.viewGone

class SalesHistoryFragment : Fragment() {

    private lateinit var binding: TabFavoriteFragmentBinding
    private lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TabFavoriteFragmentBinding.inflate(inflater, container, false)
        mContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initAdapter()
    }

    private fun initViews() {
        binding.filter.viewGone()
    }

    private fun initAdapter() {
//        binding.recyclerFavorites.adapter = PurchaseAdapter(purchaseOrdersList, object : PurchaseAdapter.PurchaseInterface {
//            override fun onItemClicked(position: Int) {
//                val intent = Intent(mContext, OrderDetailActivity::class.java)
//                intent.putExtra("fromWhere", OrderDetailEnum.SALES_HISTORY.type)
//                startActivity(intent)
//            }
//        })
    }

}
