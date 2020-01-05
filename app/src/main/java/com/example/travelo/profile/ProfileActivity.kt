package com.example.travelo.profile


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelo.BaseActivity
import com.example.travelo.QApp
import com.example.travelo.R
import com.example.travelo.database.DatabaseUtils
import com.example.travelo.models.Route
import com.example.travelo.models.Tour
import com.example.travelo.models.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jaeger.library.StatusBarUtil
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_profile_toolbar_collapse.*


class ProfileActivity : BaseActivity() {
    private var currentPage = ""
    private lateinit var mRoutesAdapter: RouteRecyclerViewAdapter
    private lateinit var mFriendsAdapter: FriendsRecyclerViewAdapter
    private lateinit var mToursAdapter: TourRecyclerViewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_toolbar_collapse)
        StatusBarUtil.setTransparent(this)
        initToolbar()
        profile_collapsing_routes_fab.setOnClickListener {
            currentPage = "routes"
            initRecyclerView()
        }
        profile_friends_fab.setOnClickListener {
            currentPage = "users"
            initRecyclerView()
        }
        profile_tour_fab.setOnClickListener {
            currentPage = "tours"
            initRecyclerView()
        }
        circle_image_view.setOnLongClickListener { editProfilePicture() }
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        collapsing_toolbar.title = QApp.currentUser?.displayName
        Glide.with(this).load(QApp.currentUser?.image).into(circle_image_view)
    }

    private fun editProfilePicture(): Boolean {
        val popup = PopupMenu(this, circle_image_view)
        popup.menuInflater.inflate(R.menu.popup_menu_edit_profile_picture, popup.menu)
        popup.show()
        popup.setOnMenuItemClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.OFF)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1, 1)
                .start(this)
            true
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                Glide.with(this).load(resultUri).into(circle_image_view)
                DatabaseUtils.addImageToStorage(QApp.fAuth.uid!!, resultUri.toString())
                    .addOnSuccessListener {
                        it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                            QApp.fData.reference.child("users").child(QApp.fAuth.uid.toString()).child("image")
                                .setValue(uri.toString()).addOnSuccessListener {
                                    Toast.makeText(this, "Zmieniłeś zdjęcie profilowe", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_basic, menu)
        return true
    }

    private fun initRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.profile_routes_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        val routesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                when (currentPage) {
                    "routes" -> {
                        val items = arrayListOf<Any>()
                        items.add("Twoje trasy")
                        dataSnapshot.child("users").child(QApp.fAuth.currentUser?.uid!!).child("ownRoutes").children.forEach {
                            val node = dataSnapshot.child("routes").child(it.value.toString())
                            items.add(node.getValue(Route::class.java)!!)
                        }
                        items.add("Ulubione Trasy")
                        dataSnapshot.child("users").child(QApp.fAuth.currentUser?.uid!!).child("likedRoutes").children.forEach {
                            val node = dataSnapshot.child("routes").child(it.value.toString())
                            items.add(node.getValue(Route::class.java)!!)
                        }
                        items.add("Polecane trasy")
                        dataSnapshot.child("routes").children.forEach {
                            items.add(it.getValue(Route::class.java)!!)
                        }
                        mRoutesAdapter = RouteRecyclerViewAdapter(this@ProfileActivity, items)
                        recyclerView.adapter = mRoutesAdapter
                        setUpOnClickListener()
                    }
                    "users" -> {
                        val items = arrayListOf<User>()
                        dataSnapshot.child("users").children.forEach {
                            val item = it.getValue(User::class.java)
                            items.add(item!!)
                            mFriendsAdapter = FriendsRecyclerViewAdapter(this@ProfileActivity, items)
                            recyclerView.adapter = mFriendsAdapter
                        }
                    }
                    "tours" -> {
                        val items: ArrayList<Triple<Tour, Route, ArrayList<User>>> = arrayListOf()
                        dataSnapshot.child("tours").children.forEach {
                            val tour: Tour = it.getValue(Tour::class.java)!!
                            val route: Route = dataSnapshot.child("routes").child(tour.routeId).getValue(Route::class.java)!!
                            val participants = arrayListOf<User>()
                            val usersId = arrayListOf<String>()
                            tour.users.forEach { usersId.add(it.value) }
                            usersId.forEach { participants.add(dataSnapshot.child("users").child(it).getValue(User::class.java)!!) }
                            items.add(Triple(tour, route, participants))
                        }
                        mToursAdapter = TourRecyclerViewAdapter(this@ProfileActivity, items)
                        recyclerView.adapter = mToursAdapter
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(
                    this@ProfileActivity.toString(),
                    "loadPost:onCancelled",
                    databaseError.toException()
                )
            }
        }

        QApp.fData.reference.addListenerForSingleValueEvent(routesListener)
    }

    private fun setUpOnClickListener() {
        mRoutesAdapter.setOnItemClickListener(object : RouteRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, obj: Route?, position: Int) {
                Snackbar.make(parent_view, "Item " + obj!!.name + " clicked", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
