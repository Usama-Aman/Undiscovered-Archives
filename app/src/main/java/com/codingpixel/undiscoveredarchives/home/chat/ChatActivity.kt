package com.codingpixel.undiscoveredarchives.home.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.codingpixel.undiscoveredarchives.R
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityChatBinding
import com.codingpixel.undiscoveredarchives.utils.viewVisible


class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        blackStatusBarIcons()

        initViews()
        initListeners()
        initAdapter()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.ivBack.viewVisible()
        binding.topBar.tvScreenTitle.text = "Stephen Holmes"
        binding.topBar.ivRightButton.viewVisible()
        binding.topBar.ivRightButton.setImageResource(R.drawable.ic_menu)
    }

    private fun initListeners() {
        binding.topBar.ivBack.setOnClickListener {
            finish()
        }

        binding.topBar.ivRightButton.setOnClickListener {
            val popup = PopupMenu(this, binding.topBar.ivRightButton)
            val inflate: MenuInflater = popup.menuInflater
            inflate.inflate(R.menu.chat_menu, popup.menu)
            popup.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.deleteChat ->                 //action
                true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initAdapter() {
        binding.chatRecyclerView.adapter = ChatAdapter()
    }
}
