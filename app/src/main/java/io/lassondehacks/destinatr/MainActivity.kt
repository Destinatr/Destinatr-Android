package io.lassondehacks.destinatr

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.lassondehacks.destinatr.fragments.ResultListViewFragment
import kotlinx.android.synthetic.main.activity_main.*

import io.lassondehacks.destinatr.domain.Result

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var res1 = Result("Premier resultat", "une adresse", 10)
        var res2 = Result("Deuxieme resultat", "une adresse", 101)
        var res3 = Result("troisieme resultat", "une adresse", 1001)

        var results = arrayOf(res1, res2, res3)

        var ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.activity_main, ResultListViewFragment(results, { selectDest() }))
        ft.commit()
    }

    fun selectDest() {

    }
}
