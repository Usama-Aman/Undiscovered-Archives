package com.codingpixel.undiscoveredarchives.home.messages

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.codingpixel.undiscoveredarchives.base.BaseActivity
import com.codingpixel.undiscoveredarchives.databinding.ActivityAllMessagesBinding
import com.codingpixel.undiscoveredarchives.home.chat.ChatActivity
import com.codingpixel.undiscoveredarchives.utils.viewVisible

class AllMessagesActivity : BaseActivity() {

    private lateinit var binding: ActivityAllMessagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        blackStatusBarIcons()

        initViews()
        initListeners()
        initAdapter()

    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.topBar.tvScreenTitle.text = "Messages"
        binding.topBar.ivBack.viewVisible()
    }

    private fun initListeners() {
        binding.topBar.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun initAdapter() {
        binding.messagesRecyclerView.adapter = AllMessagesAdapter(object : AllMessagesAdapter.AllMessagesInterface {
            override fun onItemClicked(position: Int) {
                startActivity(Intent(this@AllMessagesActivity, ChatActivity::class.java))
            }

        })
    }

}