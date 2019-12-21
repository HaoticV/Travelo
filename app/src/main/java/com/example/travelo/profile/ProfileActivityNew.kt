package com.example.travelo.profile


import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
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
import kotlinx.android.synthetic.main.activity_profile_toolbar_collapse.*

class ProfileActivityNew : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.TransparentStatusBar)
        setContentView(R.layout.activity_profile_toolbar_collapse)
        initToolbar()
        profile_collapsing_routes_fab.setOnClickListener { initRoutesRecyclerView() }
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Profil"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_basic, menu)
        return true
    }

    private fun initRoutesRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.profile_routes_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        val items: ArrayList<Route> = arrayListOf()
        val routesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.child("routes").children.forEach {
                    val item = it.getValue(Route::class.java)
                    items.add(item!!)
                }
                recyclerView.adapter = RouteRecyclerViewAdapter(this@ProfileActivityNew, items)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(this@ProfileActivityNew.toString(), "loadPost:onCancelled", databaseError.toException())
            }
        }

        QApp.fData.reference.addValueEventListener(routesListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            finish()
        } else {
            Toast.makeText(applicationContext, item.title, Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }
}