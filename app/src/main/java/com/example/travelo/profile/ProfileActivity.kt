package com.example.travelo.profile

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelo.BaseActivity
import com.example.travelo.QApp
import com.example.travelo.R
import com.example.travelo.models.Route
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_profile_fab_menu.*

class ProfileActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_fab_menu)
        initToolbar()

        profile_routes_fab.setOnClickListener { initRoutesRecyclerView() }
    }

    private fun initRoutesRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.routes_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        val items: ArrayList<Route> = arrayListOf()
        val routesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.child("routes").children.forEach {
                    val item = it.getValue(Route::class.java)
                    items.add(item!!)
                }
                recyclerView.adapter = RouteRecyclerViewAdapter(applicationContext, items)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(this@ProfileActivity.toString(), "loadPost:onCancelled", databaseError.toException())
            }
        }

        QApp.fData.reference.addValueEventListener(routesListener)
    }


    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        setSupportActionBar(toolbar)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.title = "Profil"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }
}
