package com.example.amit.haushaltsbuchapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private TabPagerAdapter mTabPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private DatabaseHelper db;

    public static final String MY_PREFERENCE = "MyPref" ;
    public static final String FIRST_TIME = "first_time";
    public SharedPreferences sharedPref;
    public SharedPreferences.Editor editor;
    private static final int REQUEST_PERMISSION_WRITE = 101;
    private boolean permissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mTabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mTabPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tabActivityIntent = new Intent(getApplicationContext(), TabActivity.class);
                startActivity(tabActivityIntent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_export) {
            Intent exportDataIntent = new Intent(MainActivity.this, ExportDataActivity.class);
            startActivity(exportDataIntent);
            return true;
        }
        else if(id == R.id.action_import)
        {
            Intent fileExplorerIntent = new Intent(this, FileExplorerActivity.class);
            startActivity(fileExplorerIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onStart() {
        super.onStart();
        db = new DatabaseHelper(getApplicationContext());
    }
    @Override
    public void onResume() {
        super.onResume();

        sharedPref=getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        // When the app runs for the first time, loads the default category
        if(sharedPref.contains(FIRST_TIME))
        {
            if(sharedPref.getBoolean(FIRST_TIME, false)){
                loadDefaultCategory();
                editor.putBoolean(FIRST_TIME, false);
                editor.commit();
            }
        }
        else
        {
            loadDefaultCategory();
            editor.putBoolean(FIRST_TIME, false);
            editor.commit();
        }
    }

    /**
     * This method helps to load default categories when the App runs
     */
    private void loadDefaultCategory() {
        CategoryTable category = new CategoryTable();
        String categoryId = category.generateCategoryID(db.getLastCategoryID());
        String categoryName = "Food";
        String type = "Expense";
        db.insertCategory(new CategoryTable(categoryId,categoryName,type));
        categoryId = category.generateCategoryID(db.getLastCategoryID());
        categoryName = "Study";
        type = "Expense";
        db.insertCategory(new CategoryTable(categoryId,categoryName,type));
        db.close();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exit the App")
                .setMessage("Do you like to exit this App?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    // Initiate request for permissions.
    private boolean checkPermissions() {

        if (!isExternalStorageReadable() || !isExternalStorageReadable()) {
            Toast.makeText(this, "This app only works on devices with usable external storage.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE);
            return false;
        } else {
            return true;
        }
    }

    // Handle permissions result to write the file
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    Toast.makeText(this, "External storage permission granted.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You must grant permission!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
