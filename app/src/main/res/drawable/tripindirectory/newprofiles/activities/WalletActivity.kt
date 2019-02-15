package directory.tripin.com.tripindirectory.newprofiles.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import directory.tripin.com.tripindirectory.R
import kotlinx.android.synthetic.main.activity_recent_calls.*
import kotlinx.android.synthetic.main.activity_wallet.*

class WalletActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
        setListners()
    }

    private fun setListners() {
        back_wallet.setOnClickListener {
            finish()
        }
    }
}
