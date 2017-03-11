package io.lassondehacks.destinatr

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.lassondehacks.destinatr.fragments.ResultListViewFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.activity_main, ResultListViewFragment("Centre de Foire", 10, { selectDest() }))
        ft.commit()
    }

    fun selectDest() {

    }
}
