package com.zhang.fan.customwidget

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rippleView.setOnClickListener { rippleView.toggle() }
    }

    override fun onPause() {
        super.onPause()
        rippleView.pause()
    }

    override fun onStop() {
        super.onStop()
        rippleView.stop()
    }
}
