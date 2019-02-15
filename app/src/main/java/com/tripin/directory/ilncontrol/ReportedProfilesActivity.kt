package com.tripin.directory.ilncontrol

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tripin.directory.ilncontrol.models.ReportedCompanyPojo
import com.tripin.directory.ilncontrol.viewholders.ReportedCompViewHolder
import kotlinx.android.synthetic.main.activity_reported_profiles.*
import java.text.SimpleDateFormat

class ReportedProfilesActivity : AppCompatActivity() {

    lateinit var context : Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reported_profiles)
        context = this
        setListners()
        setAdapter()
    }

    private fun setAdapter() {

        val query = FirebaseFirestore.getInstance()
            .collection("adminappdata")
            .document("reports")
            .collection("reportedcomps")
            .whereEqualTo("mReportStatus",0)
            .orderBy("mReportsCount",Query.Direction.DESCENDING)
            .orderBy("mTimeStamp",Query.Direction.DESCENDING)


        val options = FirestoreRecyclerOptions.Builder<ReportedCompanyPojo>()
            .setQuery(query, ReportedCompanyPojo::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = object : FirestoreRecyclerAdapter<ReportedCompanyPojo, ReportedCompViewHolder>(options) {
            public override fun onBindViewHolder(holder: ReportedCompViewHolder, position: Int, model: ReportedCompanyPojo) {



                holder.itemView.setOnClickListener {
                    val i = Intent(context, ReportDetailsActivity::class.java)
                    i.putExtra("uid",model.getmCompanyUid())
                    i.putExtra("rmn",model.getmCompanyRmn())
                    i.putExtra("fuid",model.getmCompanyFuid())
                    startActivity(i)
                }

                holder.name.text = model.getmCompanyName()

                holder.reportcount.text = "${model.getmReportsCount().toInt()} users reported!"

                if(model.getmTimeStamp()!=null)
                holder.time.text = SimpleDateFormat("dd MMM / HH:mm").format(model.getmTimeStamp())

                holder.call.setOnClickListener {
                    callNumber(model.getmCompanyRmn())
                }

            }

            override fun onCreateViewHolder(group: ViewGroup, i: Int): ReportedCompViewHolder {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                val view = LayoutInflater.from(group.context)
                    .inflate(R.layout.item_reported_company, group, false)

                return ReportedCompViewHolder(view)
            }

            override fun onDataChanged() {
                super.onDataChanged()
            }

        }

        reportedcomps_list.layoutManager = LinearLayoutManager(this)
        reportedcomps_list.adapter = adapter
    }

    private fun setListners() {

        seecorrected.setOnClickListener {
            val i = Intent(context, CorrectedProfilesActivity::class.java)
            startActivity(i)
        }

        seeblocked.setOnClickListener {
            val i = Intent(context, BlockedProfilesActivity::class.java)
            startActivity(i)
        }

    }

    private fun callNumber(number: String) {
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:" + Uri.encode(number.trim { it <= ' ' }))
        callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(callIntent)
    }


}
