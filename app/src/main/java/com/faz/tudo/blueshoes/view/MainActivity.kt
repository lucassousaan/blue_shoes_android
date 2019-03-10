package com.faz.tudo.blueshoes.view

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.faz.tudo.blueshoes.R
import com.faz.tudo.blueshoes.data.NavMenuItemsDataBase
import com.faz.tudo.blueshoes.domain.NavMenuItem
import com.faz.tudo.blueshoes.domain.User
import com.faz.tudo.blueshoes.util.NavMenuItemDetailsLookup
import com.faz.tudo.blueshoes.util.NavMenuItemKeyProvider
import com.faz.tudo.blueshoes.util.NavMenuItemPredicate
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_user_logged.*
import kotlinx.android.synthetic.main.nav_header_user_not_logged.*
import kotlinx.android.synthetic.main.nav_menu.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        const val FRAGMENT_TAG = "frag-tag"
    }

    private fun initFragment(){
        val supFrag = supportFragmentManager
        var fragment = supFrag.findFragmentByTag( FRAGMENT_TAG )

        /*
         * Se não for uma reconstrução de atividade, então não
         * haverá um fragmento em memória, então busca-se o
         * inicial.
         * */
        if( fragment == null ){
            fragment = getFragment( R.id.item_about.toLong() )
        }

        replaceFragment( fragment )
    }

    private fun getFragment( fragId: Long ): Fragment{
        return when( fragId ){
            R.id.item_about.toLong() -> AboutFragment()
            R.id.item_contact.toLong() -> ContactFragment()
            else -> AboutFragment()
        }
    }

    private fun replaceFragment( fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fl_fragment_container,
                fragment,
                FRAGMENT_TAG
            )
            .commit()
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val user = User(
        "Lucas Sousa",
        R.drawable.user,
        true
    )
    lateinit var navMenuItems : List<NavMenuItem>
    lateinit var selectNavMenuItems: SelectionTracker<Long>
    lateinit var navMenuItemsLogged : List<NavMenuItem>
    lateinit var selectNavMenuItemsLogged: SelectionTracker<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initNavMenu( savedInstanceState )

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        initFragment()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun initNavMenuItems(){
        rv_menu_items.setHasFixedSize( false )
        rv_menu_items.layoutManager = LinearLayoutManager( this )
//        rv_menu_items.adapter = NavMenuItemsAdapter( NavMenuItemsDataBase( this ).items )
        rv_menu_items.adapter = NavMenuItemsAdapter(navMenuItems)
        initNavMenuItemsSelection()
    }

    private fun initNavMenuItemsSelection(){

        selectNavMenuItems = SelectionTracker.Builder<Long>(
            "id-selected-item",
            rv_menu_items,
            NavMenuItemKeyProvider( navMenuItems ),
            NavMenuItemDetailsLookup( rv_menu_items ),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate( NavMenuItemPredicate( this ) )
            .build()

        selectNavMenuItems.addObserver(
            SelectObserverNavMenuItems {
                selectNavMenuItemsLogged.selection.filter {
                    selectNavMenuItemsLogged.deselect( it )
                }
            }
        )

        (rv_menu_items.adapter as NavMenuItemsAdapter).selectionTracker = selectNavMenuItems
    }

    private fun initNavMenuItemsLogged(){
        rv_menu_items_logged.setHasFixedSize( true )
        rv_menu_items_logged.layoutManager = LinearLayoutManager( this )
//        rv_menu_items_logged.adapter = NavMenuItemsAdapter( NavMenuItemsDataBase( this ).itemsLogged )
        rv_menu_items_logged.adapter = NavMenuItemsAdapter(navMenuItemsLogged)
        initNavMenuItemsLoggedSelection()

    }

    private fun initNavMenuItemsLoggedSelection(){

        selectNavMenuItemsLogged = SelectionTracker.Builder<Long>(
            "id-selected-item-logged",
            rv_menu_items_logged,
            NavMenuItemKeyProvider( navMenuItemsLogged ),
            NavMenuItemDetailsLookup( rv_menu_items_logged ),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate( NavMenuItemPredicate( this ) )
            .build()

        selectNavMenuItemsLogged.addObserver(
            SelectObserverNavMenuItems {
                selectNavMenuItems.selection.filter {
                    selectNavMenuItems.deselect( it )
                }
            }
        )

        (rv_menu_items_logged.adapter as NavMenuItemsAdapter).selectionTracker = selectNavMenuItemsLogged
    }

    private fun fillUserHeaderNavMenu(){
        if( user.status ) { /* Conectado */
            iv_user.setImageResource(user.image)
            tv_user.text = user.name
        }
    }

    private fun initNavMenu( savedInstanceState: Bundle? ){

        val navMenu = NavMenuItemsDataBase( this )
        navMenuItems = navMenu.items
        navMenuItemsLogged = navMenu.itemsLogged

        showHideNavMenuViews()

        initNavMenuItems()
        initNavMenuItemsLogged()

        if( savedInstanceState != null ){
            selectNavMenuItems.onRestoreInstanceState( savedInstanceState )
            selectNavMenuItemsLogged.onRestoreInstanceState( savedInstanceState )
        }
        else{
            selectNavMenuItems.select( R.id.item_all_shoes.toLong() )
        }
    }

    private fun showHideNavMenuViews(){
        if( user.status ){ /* Conectado */
            rl_header_user_not_logged.visibility = View.GONE
            fillUserHeaderNavMenu()
        }
        else{  /* Não conectado */
            rl_header_user_logged.visibility = View.GONE
            v_nav_vertical_line.visibility = View.GONE
            rv_menu_items_logged.visibility = View.GONE
        }
    }

    override fun onSaveInstanceState( outState: Bundle? ) {
        super.onSaveInstanceState( outState )
        selectNavMenuItems.onSaveInstanceState( outState!! )
        selectNavMenuItemsLogged.onSaveInstanceState( outState )
    }

    inner class SelectObserverNavMenuItems(
        val callbackRemoveSelection: ()->Unit
    ) : SelectionTracker.SelectionObserver<Long>(){

        override fun onItemStateChanged(
            key: Long,
            selected: Boolean ) {

            super.onItemStateChanged( key, selected )

            if( !selected ){
                return
            }

            callbackRemoveSelection()


            val fragment = getFragment( key )
            replaceFragment( fragment )

            drawer_layout.closeDrawer( GravityCompat.START )
        }
    }

    fun updateToolbarTitleInFragment( titleStringId: Int ){
        toolbar.title = getString( titleStringId )
    }
}
