package com.example.travelo.profile


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.travelo.BaseActivity
import com.example.travelo.R
import com.google.android.material.tabs.TabLayout
import java.util.*

class ProfileActivityNew : BaseActivity() {
    private var view_pager: ViewPager? = null
    private var viewPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var tab_layout: TabLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_toolbar_collapse)
        initToolbar()
        initComponent()
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_basic, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            finish()
        } else {
            Toast.makeText(getApplicationContext(), item.title, Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initComponent() {
        view_pager = findViewById(R.id.view_pager)
        tab_layout = findViewById(R.id.tab_layout)
        setupViewPager(view_pager)
        tab_layout.setupWithViewPager(view_pager)
        tab_layout.getTabAt(0)?.setIcon(R.drawable.ic_profile_users)
        tab_layout.getTabAt(1)?.setIcon(R.drawable.ic_profile_map)
        tab_layout.getTabAt(2)?.setIcon(R.drawable.ic_profile_stats)
        // set icon color pre-selected
        //tab_layout.getTabAt(0)?.getIcon()?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        //tab_layout.getTabAt(1)?.getIcon()?.setColorFilter(getResources().getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN)
        //tab_layout.getTabAt(2)?.getIcon()?.setColorFilter(getResources().getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN)
        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                //tab.getIcon()?.setColorFilter(getResources().getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        viewPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        viewPagerAdapter!!.addFragment(Fragment(), "Znajomi") // index 0
        viewPagerAdapter!!.addFragment(Fragment(), "Mapy") // index 1
        viewPagerAdapter!!.addFragment(Fragment(), "Statystyki") // index 2
        viewPager?.adapter = viewPagerAdapter
    }

    private inner class SectionsPagerAdapter(manager: FragmentManager?) : FragmentPagerAdapter(manager!!) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }


        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }
    }
}