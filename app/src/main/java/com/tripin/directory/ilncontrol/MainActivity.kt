package com.tripin.directory.ilncontrol

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var context :Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        setListners()

    }

    private fun setListners() {
        
        reportedprofiles.setOnClickListener {
            val i = Intent(context, ReportedProfilesActivity::class.java)
            startActivity(i)
        }

    }
}
