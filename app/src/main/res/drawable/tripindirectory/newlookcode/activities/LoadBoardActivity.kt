package directory.tripin.com.tripindirectory.newlookcode.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentPagerAdapter
import com.google.firebase.FirebaseApp
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import directory.tripin.com.tripindirectory.newlookcode.activities.fragments.loadboard.RecentLoadsFragment
import directory.tripin.com.tripindirectory.newlookcode.activities.fragments.loadboard.YourLoadsFragment
import directory.tripin.com.tripindirectory.R
import directory.tripin.com.tripindirectory.helper.Logger
import kotlinx.android.synthetic.main.content_main_loadboard.*
import kotlinx.android.synthetic.main.layout_loadboard_actionbar.*
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.jaredrummler.materialspinner.MaterialSpinner
import com.keiferstone.nonet.NoNet
import directory.tripin.com.tripindirectory.newlookcode.BasicQueryPojo
import directory.tripin.com.tripindirectory.newlookcode.FacebookRequiredActivity
import directory.tripin.com.tripindirectory.newlookcode.activities.fragments.loadboard.models.Post
import directory.tripin.com.tripindirectory.forum.MainActivity
import directory.tripin.com.tripindirectory.manager.PreferenceManager
import kotlinx.android.synthetic.main.activity_load_board2.*
import kotlinx.android.synthetic.main.item_loadpost_input.*
import libs.mjn.prettydialog.PrettyDialog
import libs.mjn.prettydialog.PrettyDialogCallback
import java.text.SimpleDateFormat
import java.util.*


class LoadBoardActivity : AppCompatActivity() {
    private var mPagerAdapter: FragmentPagerAdapter? = null

    private val POST_LOAD = 1
    private val POST_TRUCK = 2
    private var POST_TYPE = POST_LOAD

    internal var PLACE_AUTOCOMPLETE_REQUEST_CODE_SOURCE = 3
    internal var PLACE_AUTOCOMPLETE_REQUEST_CODE_DESTINATION = 4

    lateinit var context: Context
    lateinit var postpojo : Post
    private val TAG = "LoadBoardActivity"
    private var mDatabase: DatabaseReference? = null
    var fabrotation = 0f
    private lateinit var preferenceManager: PreferenceManager
    lateinit var firebaseAnalytics: FirebaseAnalytics




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_board2)
        context = this
        FirebaseApp.initializeApp(applicationContext)
        mDatabase = FirebaseDatabase.getInstance().reference
        preferenceManager = PreferenceManager.getInstance(this)
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)

        if(FirebaseAuth.getInstance().currentUser==null
                || FirebaseAuth.getInstance().currentUser!!.phoneNumber==null
                || !preferenceManager.isFacebooked){
            val i = Intent(this@LoadBoardActivity, FacebookRequiredActivity::class.java)
            i.putExtra("from", "Loadboard")
            startActivityForResult(i, 3)
            Toast.makeText(applicationContext, "Login with Facebook To Use Loadboard", Toast.LENGTH_LONG).show()
        }

        setFragmentsAdapter()
        setSpinners()
        setListners()
        setRoutePickup()
        postpojo = Post()

        if(intent.extras!=null){
            if(intent.extras.getSerializable("query")!=null){

                val basicQueryPojo:BasicQueryPojo =  intent.extras.getSerializable("query") as BasicQueryPojo
                if(!basicQueryPojo.mSourceCity.isEmpty()){
                    select_source.text = basicQueryPojo.mSourceCity
                    select_source.setTextColor(ContextCompat.getColor(context,R.color.blue_grey_800))
                    postpojo.mSource = basicQueryPojo.mSourceCity
                    newload.visibility = View.GONE
                    loadpost_input.visibility = View.VISIBLE
                }
                if(!basicQueryPojo.mDestinationCity.isEmpty()){
                    select_destination.text = basicQueryPojo.mDestinationCity
                    select_destination.setTextColor(ContextCompat.getColor(context,R.color.blue_grey_800))
                    postpojo.mDestination = basicQueryPojo.mDestinationCity
                    newload.visibility = View.GONE
                    loadpost_input.visibility = View.VISIBLE
                }
            }


        }

        internetCheck()

        if(!preferenceManager.isLBScreenGuided){
            showIntro()
        }


    }

    private fun setListners() {

        newload.setOnClickListener {
            newload.visibility = View.GONE
            loadpost_input.visibility = View.VISIBLE
        }

        fab_revert.setOnClickListener {
            flipthefab()
        }

        showall.setOnClickListener {
            startAllLoadsActivity()
        }

        back.setOnClickListener {
            finish()
        }
        post.setOnClickListener {
            if(postpojo.mSource==null||postpojo.mDestination==null){
                Toast.makeText(context,"Please Enter Route",Toast.LENGTH_LONG).show()
                post.text = "POST NOW"

            }else{
                if(weight.text != null){
                    //add unit
                    if(!weight.text.isEmpty()){
                        val list = spinnerweight.getItems<String>()
                        postpojo.mPayload = weight.text.toString()+list.get(spinnerweight.selectedIndex)
                    }else{
                        postpojo.mPayload = ""
                    }

                }
                if(length.text != null){
                    //add unit
                    if(!length.text.isEmpty()){
                        val list = spinnerlength.getItems<String>()
                        postpojo.mTruckLength = length.text.toString()+list.get(spinnerlength.selectedIndex)
                    }else{
                        postpojo.mTruckLength = ""
                    }


                }
                if(otherreq.text != null){
                    postpojo.mRemark = otherreq.text.toString()
                }
                if(material.text != null){
                    postpojo.mMeterial = material.text.toString()
                }

                postpojo.setmFuid(getFuid())
                postpojo.setmUid(getUid())
                postpojo.setmContactNo(getUserPhoneNo())
                postpojo.setmDate(getDate())
                postpojo.setmPhotoUrl(preferenceManager.imageUrl)
                Logger.v("image set: "+postpojo.getmPhotoUrl())
                postpojo.setmAuthor(preferenceManager.displayName)
                postpojo.mFindOrPost = POST_TYPE


                // [START single_value_read]
                val userId = getUid()

                if(FirebaseAuth.getInstance().currentUser!=null){
                    showaggrementdialog()
                }else{
                    Toast.makeText(context,"Error, Not Signed in",Toast.LENGTH_SHORT).show()
                }



            }
        }
    }



    private fun startAllLoadsActivity() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }

    private fun writeNewPost(userId: String, postpojo: Post) {
        post.text = "..."
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show()

        val key = mDatabase!!.child("posts").push().getKey()
        val postValues = postpojo.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates["/posts/" + key!!] = postValues
        childUpdates["/user-posts/$userId/$key"] = postValues

        mDatabase!!.updateChildren(childUpdates).addOnCompleteListener {
            cleanUpPostInput()

            Toast.makeText(context,"Successfully Posted!",Toast.LENGTH_LONG).show()
            post.text = "POST NOW"

            val bundle = Bundle()
            bundle.putString("by_rmn",preferenceManager.rmn)

            if(postpojo.mMeterial!=null){
                if(!postpojo.mMeterial.isEmpty()){
                    bundle.putString("is_material_entered","Yes")
                }else{
                    bundle.putString("is_material_entered","No")
                }
            }else{
                bundle.putString("is_material_entered","No")
            }
            firebaseAnalytics.logEvent("z_loadboard_post", bundle)

        }.addOnCanceledListener {
            Toast.makeText(context,"Try Again",Toast.LENGTH_LONG).show()
        }
    }

    private fun cleanUpPostInput() {
        select_destination.text = "Destination"
        select_destination.setTextColor(ContextCompat.getColor(context,R.color.blue_grey_200))
        select_source.text = "Source"
        select_source.setTextColor(ContextCompat.getColor(context,R.color.blue_grey_200))
        postpojo.mSource = null
        postpojo.mDestination = null
        postpojo.mMeterial = null
        postpojo.mPayload = null
        postpojo.mTruckLength = null
        weight.setText("")
        length.setText("")
        material.setText("")
        otherreq.setText("")
        newload.visibility = View.VISIBLE
        loadpost_input.visibility = View.GONE

    }
    private fun flipthefab() {
        fabrotation = if (fabrotation == 0f) {
            180f
        } else {
            0f
        }
        val interpolator = OvershootInterpolator()
        ViewCompat.animate(fab_revert)
                .rotation(fabrotation)
                .withLayer()
                .setDuration(300)
                .setInterpolator(interpolator)
                .start()

        //swap cities
        if(postpojo.mSource==null||postpojo.mDestination==null){
            Toast.makeText(context,"Please Enter Route",Toast.LENGTH_LONG).show()
        }else{
            val t1 = select_source.text.toString()
            val t2 = select_destination.text.toString()

            select_source.text = t2
            select_destination.text = t1
            postpojo.mDestination = t1.substring(2)
            postpojo.mSource = t2.substring(2)
        }


    }
    // [END write_fan_out]

    private fun setSpinners() {
        val spinnervehicle = findViewById<MaterialSpinner>(R.id.spinnervtype)
        spinnervehicle.setItems("Select",
                "LCV",
                "Truck",
                "Tusker",
                "Taurus",
                "Trailers",
                "Container Body",
                "Refrigerated Van",
                "Tankers",
                "Tippers",
                "Bulkers",
                "Car Carriers",
                "Scooter Body",
                "Hydraulic Axles")
        spinnervehicle.setOnItemSelectedListener { view, position, id, item ->
            postpojo.setmTruckType(item.toString())
        }

        val spinnerbody = findViewById<MaterialSpinner>(R.id.spinnerbtype)
        spinnerbody.setItems("Select",
                "Normal",
                "Full Body",
                "Half Body",
                "Open Body",
                "Platform",
                "Skeleton",
                "Semi-low",
                "Low",
                "Trolla/Body Trailer",
                "Refer",
                "High Cube")
        spinnerbody.setOnItemSelectedListener { view, position, id, item ->
            postpojo.setmTruckBodyType(item.toString())
        }
        val spinnerweight = findViewById<MaterialSpinner>(R.id.spinnerweight)
        spinnerweight.setItems("KG", "Tons", "MT")
        spinnerweight.setOnItemSelectedListener { view, position, id, item ->  }
        val spinnerlength = findViewById<MaterialSpinner>(R.id.spinnerlength)
        spinnerlength.setItems("Meters", "Feets")
        spinnerlength.setOnItemSelectedListener { view, position, id, item ->  }    }

    private fun setFragmentsAdapter() {

        val mPagerAdapter = FragmentPagerItemAdapter(
                supportFragmentManager, FragmentPagerItems.with(this)
                .add("All", RecentLoadsFragment::class.java)
                .add("Yours", YourLoadsFragment::class.java)
                .create())
        mViewPager.adapter = mPagerAdapter
        Logger.v("LoadBoardActivity")
        val viewPagerTab = findViewById<SmartTabLayout>(R.id.tabs)
        viewPagerTab.setViewPager(mViewPager)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                PLACE_AUTOCOMPLETE_REQUEST_CODE_SOURCE -> {
                    if(data!=null){
                        val place = PlaceAutocomplete.getPlace(context, data)
                        select_source.text = ". ${place.name}"
                        select_source.setTextColor(ContextCompat.getColor(context, R.color.blue_grey_900))
                        postpojo.mSource = place.name.toString()
                    }else{
                        Toast.makeText(context,"Try Again!",Toast.LENGTH_LONG).show()
                    }

                }
                PLACE_AUTOCOMPLETE_REQUEST_CODE_DESTINATION -> {
                    if(data!=null){
                        val place = PlaceAutocomplete.getPlace(context, data)
                        select_destination.text = ". ${place.name}"
                        select_destination.setTextColor(ContextCompat.getColor(context, R.color.blue_grey_900))
                        postpojo.mDestination = place.name.toString()
                    }else{
                        Toast.makeText(context,"Try Again!",Toast.LENGTH_LONG).show()
                    }


                }

                3->{
                    Toast.makeText(applicationContext, "Welcome", Toast.LENGTH_LONG).show()
                }

            }
        }else{
            when (requestCode) {
                3->{
                    finish()
                }
            }
        }
    }

    private fun setRoutePickup() {

        select_source.setOnClickListener {
            starttheplacesfragment(PLACE_AUTOCOMPLETE_REQUEST_CODE_SOURCE)
        }

        select_destination.setOnClickListener {
            starttheplacesfragment(PLACE_AUTOCOMPLETE_REQUEST_CODE_DESTINATION)
        }

    }

    private fun starttheplacesfragment(code: Int) = try {
        val typeFilter = AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .setCountry("IN")
                .build()
        val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                .setFilter(typeFilter)
                .build(this)
        startActivityForResult(intent, code)
    } catch (e: GooglePlayServicesRepairableException) {
        // TODO: Handle the error.
    } catch (e: GooglePlayServicesNotAvailableException) {
        // TODO: Handle the error.
    }

    fun getUid(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
    private fun getFuid(): String {
        return preferenceManager.fuid!!
    }

    fun getUserPhoneNo(): String? {
        val user = getCurrentUser()
        return user!!.phoneNumber
    }

    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    private fun getDate(): String {
        val c = Calendar.getInstance()
        println("Current time => " + c.time)

        val df = SimpleDateFormat("MMM dd, HH:mm")
        return df.format(c.time)
    }

    private fun showIntro() {

        val tapTargetSequence : TapTargetSequence = TapTargetSequence(this)
                .targets(
                        TapTarget.forView(tabs, "ILN LoadBoard, Tabs here!","Here you can toggle all loads and your loads list. Tap on the target!")
                                .transparentTarget(true)
                                .targetRadius(100)
                                .drawShadow(true)
                                .cancelable(false).outerCircleColor(R.color.primaryColor),
                        TapTarget.forView(lbform, "The Load-Post form here","Tap on the target!")
                                .transparentTarget(true)
                                .targetRadius(120)
                                .drawShadow(true)
                                .cancelable(true).outerCircleColor(R.color.primaryColor)

                )
                .listener(object: TapTargetSequence.Listener {
                    override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
                    }

                    override fun onSequenceFinish() {
                        Toast.makeText(applicationContext,"Welcome to Loadboard",Toast.LENGTH_SHORT).show()
                        preferenceManager.setisLBScreenGuided(true)
                    }
                    override fun onSequenceCanceled(lastTarget: TapTarget) {
                        // Boo
                        preferenceManager.setisLBScreenGuided(true)

                    }
                })

        tapTargetSequence.start()
    }

    /**
     * This method is use for checking internet connectivity
     * If there is no internet it will show an snackbar to user
     */
    private fun internetCheck() {
        NoNet.monitor(this)
                .poll()
                .snackbar()
    }

    private fun showaggrementdialog() {

        val prettyDialog: PrettyDialog = PrettyDialog(this)

        prettyDialog
                .setTitle("Loadboard Agreement")
                .setMessage("By posting your requirement on Loadboard we cant give you the guarantee of getting any useful responses. It completely depends on how many and who are interested in this deal at this time. We just make sure This post is notified and visible to all transporters.")
                .addButton(
                        "I agree, Post now!",
                        R.color.pdlg_color_white,
                        R.color.green_400
                ) {
                    prettyDialog.dismiss()
                    writeNewPost(getUid(), postpojo)

                }.addButton(
                        "No, Find on directory",
                        R.color.pdlg_color_white,
                        R.color.blue_grey_100
                ) {
                    prettyDialog.dismiss()
                    finish()
                }.addButton(
                        "Cancel",
                        R.color.pdlg_color_white,
                        R.color.blue_grey_100
                ) {
                    prettyDialog.dismiss()

                }
        prettyDialog.show()


    }


}
