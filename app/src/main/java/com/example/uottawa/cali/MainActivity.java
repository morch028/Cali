package com.example.uottawa.cali;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final int ASSIGNMENT_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Toast.makeText(getBaseContext(), "Opened me", Toast.LENGTH_SHORT).show();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        /*NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu courseMenu = navigationView.getMenu();
        courseMenu.add("Algebra");
        courseMenu.add("Notifications");*/


        //ListView
        Course ui = new Course("UI", R.color.courseColor3, R.color.courseColor3a);
        Course networking = new Course("Networking", R.color.courseColor1, R.color.courseColor1a);
        Calendar calendar = Calendar.getInstance();
        calendar.set(117, 6, 15);
        Assignment[] assignments = new Assignment[]{
                new Assignment(ui, "Lab", 75, new Date(1500566400)),
                new Assignment(networking, "Assignment", 30, new Date(1500998400)),
                new Assignment(ui, "Random #3", 10 , calendar.getTime())
        };
        //ListView
        ArrayAdapter adapter = new AssignmentListAdapter(this, assignments);
        ListView summaryListView = (ListView)findViewById(R.id.summaryListViewMain);
        summaryListView.setAdapter(adapter);
        summaryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                Assignment item = (Assignment)parent.getItemAtPosition(position);
                Intent intent = new Intent(getBaseContext(), AssignmentActivity.class);
                startActivityForResult(intent, ASSIGNMENT_REQUEST);
                Toast.makeText(getBaseContext(), item.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        //Drawer layout ListView
        Course[] courses = new Course[] {
                ui,
                new Course("CEG", R.color.colorPrimary, R.color.courseColor8a),
                new Course("OS", R.color.courseColor7a, R.color.courseColor3)
        };
        ArrayAdapter drawerAdapter = new DrawerListAdapter(this, courses);
        ListView drawerListView = (ListView)findViewById(R.id.drawerlist);
        drawerListView.setAdapter(drawerAdapter);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String title = item.getTitle().toString();
        //Toast.makeText(this, item.getTitle(), Toast.LENGTH_LONG).show();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (title == "Notifications"){
            Intent intent = new Intent("com.example.uottawa.cali.Test");
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
