package com.demo.bleandwebsockethandson.home

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.demo.bleandwebsockethandson.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initObserver()
    }

    private fun initObserver() {
        mainViewModel.showToolbarBack.observe(this, ::handleToolbarBack)
    }

    private fun handleToolbarBack(isShow: Boolean) =
        supportActionBar?.setDisplayHomeAsUpEnabled(isShow)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!findNavController(R.id.fragment_container).navigateUp()) {
            super.onBackPressed()
        }
    }
}