package com.example.uottawa.cali;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends IOActivity implements NavigationView.OnNavigationItemSelectedListener {

    static final int ASSIGNMENT_REQUEST = 1;
    static final int SETTINGS_REQUEST = 2;
    static final int COURSE_REQUEST = 3;
    private int selectedIndex;
    private ArrayList<Course> blackList;
    private DrawerListAdapter drawerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        blackList = new ArrayList<>();
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Assignment freshAssignment = new Assignment();
                freshAssignment.setName(getString(R.string.fresh_assignment_name));
                assignmentsFile.add(freshAssignment);
                selectedIndex = assignmentsFile.size() - 1;
                Intent intent = new Intent(getBaseContext(), AssignmentActivity.class);
                intent.putExtra(getString(R.string.intent_assignment_data_send), freshAssignment);
                intent.putExtra(getString(R.string.intent_assignment_fresh), true);
                intent.putExtra(getString(R.string.intent_course_list), coursesFile);
                startActivityForResult(intent, ASSIGNMENT_REQUEST);
            }
        });
        //Drawer
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //Maybe something needs to happen here
            }
        };

        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //Add course button
        LinearLayout addCourseBtn = (LinearLayout) findViewById(R.id.addCourseLayout);
        addCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.example.uottawa.cali.CourseActivity");
                intent.putExtra("isNewCourse", true);
                intent.putExtra("courseArrayList", coursesFile);
                startActivityForResult(intent, COURSE_REQUEST);
            }
        });

        ImageButton addCourseBtn2 = (ImageButton) findViewById(R.id.addCourseBtn);
        addCourseBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.example.uottawa.cali.CourseActivity");
                intent.putExtra("isNewCourse", true);
                intent.putExtra("courseArrayList", coursesFile);
                startActivityForResult(intent, COURSE_REQUEST);
            }
        });

        //ListView
        //setSampleData();
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(getString(R.string.first_boot_preferences), true)) {
            setSampleData();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.first_boot_preferences), false);
            editor.commit();
        } else {
            if (!readData()) {
                failedToRead();
            }
        }

        reloadAssignments();
        //writeData();
        ListView summaryListView = (ListView)findViewById(R.id.summaryListViewMain);
        summaryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                selectedIndex = position;
                Assignment item = (Assignment)parent.getItemAtPosition(position);
                Intent intent = new Intent(getBaseContext(), AssignmentActivity.class);
                intent.putExtra(getString(R.string.intent_assignment_data_send), item);
                intent.putExtra(getString(R.string.intent_assignment_fresh), false);
                intent.putExtra(getString(R.string.intent_course_list), coursesFile);
                startActivityForResult(intent, ASSIGNMENT_REQUEST);
            }
        });
        summaryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                deleteAssignment(position);
                return true;
            }
        });
        //Drawer layout ListView
        drawerAdapter = new DrawerListAdapter(this, coursesFile);
        final ListView drawerListView = (ListView)findViewById(R.id.drawerlist);
        drawerListView.setAdapter(drawerAdapter);
        drawerListView.setClickable(true);
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                selectedIndex = position;
                Course item = (Course)parent.getItemAtPosition(position);
                Intent intent = new Intent(getBaseContext(), CourseActivity.class);
                intent.putExtra("course", item);
                intent.putExtra("isNewCourse", false);
                intent.putExtra("oldCourseIndex", position);
                intent.putExtra("courseArrayList", coursesFile);
                startActivityForResult(intent, COURSE_REQUEST);
            }
        });
        setCourseDialog(drawerListView, coursesFile, drawerAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ASSIGNMENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                FileOperations assignmentOperation = (FileOperations)data.getSerializableExtra(getString(R.string.intent_assignment_operation));
                if (FileOperations.MODIFY == assignmentOperation) {
                    Assignment freshAssignment = (Assignment)data.getSerializableExtra(getString(R.string.intent_assignment_data_receive));
                    for (Course course : coursesFile) {
                        if (course.getName().equals(freshAssignment.getCourse().getName())) {
                            freshAssignment.setCourse(course);
                        }
                    }
                    assignmentsFile.set(selectedIndex, freshAssignment);
                    reloadAssignments();
                } else if (FileOperations.DELETE == assignmentOperation) {
                    assignmentsFile.remove(selectedIndex);
                    reloadAssignments();
                }
            }
        } else if (requestCode == SETTINGS_REQUEST) {
            reloadAssignments();
        } else if (requestCode == COURSE_REQUEST) {
            if (resultCode == RESULT_OK) {
                FileOperations courseOperation = (FileOperations)data.getSerializableExtra(getString(R.string.intent_course_operation));
                if(FileOperations.MODIFY == courseOperation) {
                    Course freshCourse = (Course)data.getSerializableExtra(getString(R.string.intent_course_data_receive));
                    if(!data.getBooleanExtra("shouldEdit", false)) {
                        coursesFile.add(freshCourse);
                    } else {
                        int index = data.getIntExtra("oldCourseIndex", -1);
                        coursesFile.get(index).setName(freshCourse.getName());
                        coursesFile.get(index).setColorIndex(freshCourse.getColorIndex());
                        coursesFile.get(index).setColorInverseIndex(freshCourse.getColorInverseIndex());
                        //coursesFile.set(index, freshCourse);
                    }
                    drawerAdapter.notifyDataSetChanged();
                    reloadCourses();
                    reloadAssignments();
                }
            }
        }
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_REQUEST);
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

    public void deleteAssignment(final int position) {
        final Assignment deleteMe = assignmentsFile.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_delete_dialog));
        builder.setMessage(getString(R.string.message_delete_dialog) + " " + deleteMe.getName() + "?");
        builder.setPositiveButton(getString(R.string.positive_delete_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                assignmentsFile.remove(position);
                reloadAssignments();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.negative_delete_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void openFilterDialog(View v) {
        ArrayList<String> courseNames = new ArrayList<>();
        boolean[] checkList = new boolean[coursesFile.size()];
        for (int i = 0; i < coursesFile.size(); i ++) {
            courseNames.add(coursesFile.get(i).getName());
            checkList[i] = !blackList.contains(coursesFile.get(i));
        }

        final ArrayList<Integer> changed = new ArrayList<>();
        final ArrayList<Boolean> state = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.title_filter_dialog)
                .setMultiChoiceItems(courseNames.toArray(new String[courseNames.size()]), checkList,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                changed.add(which);
                                state.add(isChecked);
                            }
                        })
                .setPositiveButton(R.string.ok_filter_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        updateFilter(changed, state);
                    }
                })
                .setNegativeButton(R.string.cancel_filter_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.create().show();
    }

    private void failedToRead() {
        Toast.makeText(getBaseContext(), "Failed to read from disk", Toast.LENGTH_LONG).show();
        Toast.makeText(getBaseContext(), "Loading sample data", Toast.LENGTH_LONG).show();
        setSampleData();
    }

    private void reloadAssignments() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        long maximumDays = 1;
        long today = new Date().getTime();
        for (Assignment assignment : assignmentsFile) {
            if (Math.abs(assignment.getDueDate().getTime() - today) > maximumDays) {
                maximumDays = Math.abs(assignment.getDueDate().getTime() - today);
            }
        }
        boolean due = (sharedPref.getBoolean(getString(R.string.due_sort_preferences), true));
        boolean complete = (sharedPref.getBoolean(getString(R.string.complete_sort_preferences), true));
        boolean priority = (sharedPref.getBoolean(getString(R.string.priority_sort_preferences), true));
        boolean allHidden = true;
        for (Assignment assignment : assignmentsFile) {
            assignment.setVisible(!blackList.contains(assignment.getCourse()));
            if (assignment.getVisible() && ((assignment.getDueDate().getTime() - today) < - 86400 && assignment.getComplete() == 100) && !sharedPref.getBoolean(getString(R.string.completed_assignments_preferences), false)) {
                assignment.setVisible(false);
            }
            if (assignment.getVisible()) {
                allHidden = false;
            }
            double rank = 0;
            if (due) {
                System.out.println("Adding due");
                rank += (assignment.getDueDate().getTime() - today) / (maximumDays * 2.0);
            }
            if (complete) {
                System.out.println("Adding complete");
                rank += assignment.getComplete() / 100.0;
            }
            if (priority) {
                System.out.println("Adding priority");
                rank += (assignment.getPriority() - 1)/ 4.0;
            }
            assignment.setRank(rank);
        }
        Collections.sort(assignmentsFile);
        ArrayAdapter summaryListViewAdapter = new AssignmentListAdapter(this, assignmentsFile.toArray(new Assignment[assignmentsFile.size()]));
        summaryListViewAdapter.notifyDataSetChanged();
        ListView summaryListView = (ListView)findViewById(R.id.summaryListViewMain);
        summaryListView.invalidateViews();
        summaryListView.setAdapter(summaryListViewAdapter);
        TextView errorTextView = (TextView)findViewById(R.id.errorTextViewMain);
        if (allHidden || assignmentsFile.size() == 0) {
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText(getString((assignmentsFile.size() == 0)? R.string.no_assignments_error : R.string.over_filter_error));
        } else {
            errorTextView.setVisibility(View.GONE);
        }
        writeData();
    }

    private void updateFilter(ArrayList<Integer> changed, ArrayList<Boolean> state) {
        for (int i = 0; i < changed.size(); i++) {
            if (state.get(i)) {
                blackList.remove(coursesFile.get(changed.get(i)));
            } else {
                blackList.add(coursesFile.get(changed.get(i)));
            }
        }
        reloadAssignments();
    }
    private void reloadCourses() {
        blackList = new ArrayList<>();
    }

    private void setSampleData() {
        Calendar calendar = Calendar.getInstance();
        coursesFile = new ArrayList<>();
        coursesFile.add(Course.getUnsetCourse());
        assignmentsFile = new ArrayList<>();
        coursesFile.add(new Course("Example Course", R.color.courseColor1, R.color.courseColor1a));
        calendar.add(Calendar.HOUR_OF_DAY, 24*7);
        assignmentsFile.add(new Assignment(coursesFile.get(1), "Example Assignment", 10, calendar.getTime(), 1, AssignmentTypes.UNSET, "This is an example assignment, feel free to delete it and add in your own assignments and courses."));
    }
    private void setTestData() {
        Calendar calendar = Calendar.getInstance();
        coursesFile = new ArrayList<>();
        coursesFile.add(Course.getUnsetCourse());
        assignmentsFile = new ArrayList<>();
        coursesFile.add(new Course("Networking", R.color.courseColor1, R.color.courseColor1a));
        coursesFile.add(new Course("UI Design", R.color.courseColor3, R.color.courseColor3a));
        coursesFile.add(new Course("Quality Assurance", R.color.courseColor5, R.color.courseColor5a));
        coursesFile.add(new Course("Operating Systems", R.color.courseColor6, R.color.courseColor6a));
        calendar.set(2017, 6, 19);
        Assignment tempAssignment = new Assignment(coursesFile.get(2), "App Presentation", 0, new Date(), 1, AssignmentTypes.GROUP_WORK, "A presentation that is happening right now");
        tempAssignment.getNamedLinkList().add(0, new NamedLink("Google","http://www.google.com"));
        assignmentsFile.add(tempAssignment);
        calendar.set(2017, 6, 24);
        tempAssignment = new Assignment(coursesFile.get(1), "Routing Lab #8", 75, calendar.getTime(), 2, AssignmentTypes.LAB_REPORT, "A description");
        assignmentsFile.add(tempAssignment);
        calendar.set(2017, 6, 21);
        tempAssignment = new Assignment(coursesFile.get(2), "Assignment #6", 20, calendar.getTime(), 4, AssignmentTypes.PROBLEM_SET, "Another description");
        assignmentsFile.add(tempAssignment);
        calendar.set(2017, 6, 25);
        tempAssignment = new Assignment(coursesFile.get(3), "Final exam", 20, calendar.getTime(), 1, AssignmentTypes.STUDY, "A third descriptions");
        assignmentsFile.add(tempAssignment);
        calendar.set(2017, 6, 26);
        tempAssignment = new Assignment(coursesFile.get(4), "Paging algorithms", 30, calendar.getTime(), 3, AssignmentTypes.project, "Another another description");
        assignmentsFile.add(tempAssignment);
    }

    //Used to set create a dialog for course listview on long press
    public void setCourseDialog(ListView listView, final ArrayList<Course> coursesFile, final ArrayAdapter drawerAdapter) {
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {

                //Do your tasks here
                final int courseSelected = position;
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Options");
                CharSequence[] items = new CharSequence[] {"Remove course"};
                alert.setItems(items, new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int option) {
                        switch (option) {
                            case 0:
                                AlertDialog.Builder confirmation = new AlertDialog.Builder(MainActivity.this);
                                confirmation.setMessage("Are you sure you want to delete this course?")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                deleteCourse(courseSelected);
                                                drawerAdapter.notifyDataSetChanged();
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                            }
                                        }).show();
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                alert.show();
                return true;
            }
        });
    }
    public void deleteCourse(int position) {
        Course deleteMe = coursesFile.get(position);
        for (Assignment assignment : assignmentsFile) {
            if (assignment.getCourse() == deleteMe || assignment.getCourse().getName().equals(deleteMe.getName())) {
                assignment.setCourse(Course.getUnsetCourse());
            }
        }
        coursesFile.remove(deleteMe);
        reloadCourses();
        reloadAssignments();
    }
}
