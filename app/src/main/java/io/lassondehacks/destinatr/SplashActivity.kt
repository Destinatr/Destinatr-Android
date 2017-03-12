package io.lassondehacks.destinatr

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.util.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Timer().schedule(object: TimerTask(){
            override fun run() {

                startActivity(Intent(this@SplashActivity, MapsActivity::class.java))
            }
        }, 1500)
    }
}
