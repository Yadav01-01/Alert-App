package com.alert.app.activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.alert.app.R
import com.alert.app.base.BaseApplication
import com.alert.app.base.MyApplication
import com.alert.app.base.SessionManagement
import com.alert.app.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var sessionManagement: SessionManagement

    var alert: MyApplication? =null
    var openScreen:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManagement= SessionManagement(this)
        sessionManagement.logOut()
        openScreen=intent?.getStringExtra("openScreen").toString()
        BaseApplication.alertBox(this)

        // using function for find destination graph
        startDestination()

    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear binding to avoid memory leaks
    }

    private fun startDestination() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.frameLayoutAuth) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.auth_graph)
        navGraph.setStartDestination(
            if (openScreen.equals("Login", ignoreCase = true)) R.id.signInFragment
            else R.id.signUpFragment
        )
        navController.graph = navGraph



    }


}
