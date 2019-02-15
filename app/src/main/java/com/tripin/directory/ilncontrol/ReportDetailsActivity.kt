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
import com.tripin.directory.ilncontrol.models.ReportedCompanyPojo
import com.tripin.directory.ilncontrol.models.SubmittedReportPojo
import com.tripin.directory.ilncontrol.viewholders.ReportedCompViewHolder
import kotlinx.android.synthetic.main.activity_report_details.*
import kotlinx.android.synthetic.main.activity_reported_profiles.*
import java.text.SimpleDateFormat
import android.widget.Toast
import libs.mjn.prettydialog.PrettyDialog
import libs.mjn.prettydialog.PrettyDialogCallback
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import android.provider.SyncStateContract.Helpers.update
import android.util.Log
import com.google.firebase.firestore.*
import java.lang.reflect.Array.getDouble


class ReportDetailsActivity : AppCompatActivity() {

    lateinit var context: Context
    lateinit var mCompUid: String
    lateinit var mCompFuid: String
    var mCompName: String = "Name"
    var mCompUserName: String = "Name"
    var mCompRMN: String = "***"
    lateinit var mCompPhotourl: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_details)
        context = this
        setListners()
        getIntentData()
        setAdapter()
    }

    private fun getIntentData() {
        if (intent.extras != null) {
            if (intent.extras.getString("uid") != null) {
                mCompUid = intent.extras.getString("uid")
            }
            if (intent.extras.getString("name") != null) {
                mCompName = intent.extras.getString("name")
                title = mCompName
            }
            if (intent.extras.getString("displayname") != null) {
                mCompUserName = intent.extras.getString("display")
            }
            if (intent.extras.getString("rmn") != null) {
                mCompRMN = intent.extras.getString("rmn")
            }
            if (intent.extras.getString("photourl") != null) {
                mCompPhotourl = intent.extras.getString("photourl")
            }
            if (intent.extras.getString("fuid") != null) {
                mCompFuid = intent.extras.getString("fuid")
            }
        }
        if (mCompUid == null) {
            finish()
        }
    }

    private fun setAdapter() {

        val query = FirebaseFirestore.getInstance()
            .collection("adminappdata")
            .document("reports")
            .collection("reportedcomps").document(mCompUid).collection("reportslist")
            .orderBy("mNumberOfTags", Query.Direction.DESCENDING)
            .orderBy("mTimeStamp", Query.Direction.DESCENDING)


        val options = FirestoreRecyclerOptions.Builder<SubmittedReportPojo>()
            .setQuery(query, SubmittedReportPojo::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = object : FirestoreRecyclerAdapter<SubmittedReportPojo, ReportedCompViewHolder>(options) {
            public override fun onBindViewHolder(
                holder: ReportedCompViewHolder,
                position: Int,
                model: SubmittedReportPojo
            ) {


                holder.name.text = model.getmReporterDisplayName()

                holder.reportcount.text = " ${model.getmComment()} "

                if (model.getmTimeStamp() != null) {
                    holder.time.text = SimpleDateFormat("dd MMM / HH:mm").format(model.getmTimeStamp())
                }

                var tags = ""
                model.getmReportTags().forEach {
                    tags = "$tags$it.\n"
                }

                holder.status.text = tags

                holder.call.setOnClickListener {
                    callNumber(model.getmReporterRmn())
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

        reportes_list.layoutManager = LinearLayoutManager(this)
        reportes_list.adapter = adapter
    }

    private fun setListners() {

        takeaction.setOnClickListener {
            shownewlookfeedbackdialog()
        }

        seeprofile.setOnClickListener {

            val launchIntent =
                Intent("directory.tripin.com.tripindirectory.newprofiles.activities.CompanyProfileDisplayActivity")

            if (launchIntent != null) {
                launchIntent.putExtra("uid", mCompUid)
                launchIntent.putExtra("fuid", mCompFuid)
                launchIntent.putExtra("rmn", mCompRMN)
                startActivity(launchIntent)
            } else {
                Toast.makeText(applicationContext, " Install ILN App!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun shownewlookfeedbackdialog() {

        val prettyDialog: PrettyDialog = PrettyDialog(this)

        prettyDialog
            .setTitle("Take Action")
            .setMessage("What should be done with this reported profile?")
            .addButton(
                "Mark it as Corrected.",
                R.color.pdlg_color_white,
                R.color.green_200
            ) {

                markascorrected()

                prettyDialog.dismiss()

            }.addButton(
                "Block On Directory!",
                R.color.pdlg_color_white,
                R.color.red_200
            ) {
                markasBlocked()
                prettyDialog.dismiss()

            }.addButton(
                "Cancel",
                R.color.pdlg_color_white,
                R.color.blue_grey_100,
                PrettyDialogCallback {
                    prettyDialog.dismiss()
                }
            )
        prettyDialog.show()

    }

    private fun markascorrected() {
        FirebaseFirestore.getInstance()
            .collection("adminappdata")
            .document("reports")
            .collection("reportedcomps").document(mCompUid).update("mReportStatus", 1).addOnCompleteListener {
                FirebaseFirestore.getInstance()
                    .collection("partners")
                    .document(mCompUid).update("isSpammed",false).addOnCompleteListener {
                        FirebaseFirestore.getInstance()
                            .collection("denormalizers")
                            .document(mCompUid).update("isSpammed",false).addOnCompleteListener {
                                Toast.makeText(context, "Corrected!", Toast.LENGTH_LONG).show()
                            }
                    }
            }

    }

    private fun markasBlocked() {
        FirebaseFirestore.getInstance()
            .collection("adminappdata")
            .document("reports")
            .collection("reportedcomps").document(mCompUid).update("mReportStatus", 2).addOnCompleteListener {
                FirebaseFirestore.getInstance()
                    .collection("partners")
                    .document(mCompUid).update("isSpammed",true).addOnCompleteListener {
                        FirebaseFirestore.getInstance()
                            .collection("denormalizers")
                            .document(mCompUid).update("isSpammed",true).addOnCompleteListener {
                                Toast.makeText(context, "Blocked", Toast.LENGTH_LONG).show()
                            }
                    }
            }

//        FirebaseFirestore.getInstance().runTransaction { transaction ->
//            val snapshot = transaction.get(sfDocRef)
//            transaction.update(sfDocRef, "mReportStatus", 2)
//
//            // Success
//            null
//        }.addOnSuccessListener {
//            Log.d("RedAdmin", "Transaction success!")
//            Toast.makeText(context, "Blocking!", Toast.LENGTH_LONG).show()
//
//        }.addOnFailureListener { e ->
//            Log.d("RedAdmin", "Transaction failure!")
//
//        }

    }

    private fun callNumber(number: String) {
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:" + Uri.encode(number.trim { it <= ' ' }))
        callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(callIntent)
    }

}
