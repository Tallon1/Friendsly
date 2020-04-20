package com.example.friendsly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    //Firebase Authentication
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Actionbar & it's Title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //Initialise
        firebaseAuth = FirebaseAuth.getInstance();

        //Bottom Navigation
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //Home Fragment Transaction (Default)
        actionBar.setTitle("Home"); //Changes ActionBar Title
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //Handle Item Clicks
                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            //Home Fragment Transaction
                            actionBar.setTitle("Home"); //Changes ActionBar Title
                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1, "");
                            ft1.commit();
                            return true;
                        case R.id.nav_profile:
                            //Profile Fragment Transaction
                            actionBar.setTitle("Profile"); //Changes ActionBar Title
                            ProfileFragment fragment2 = new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2, "");
                            ft2.commit();
                            return true;
                        case R.id.nav_users:
                            //Users Fragment Transaction
                            actionBar.setTitle("Users"); //Changes ActionBar Title
                            UsersFragment fragment3 = new UsersFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, fragment3, "");
                            ft3.commit();
                            return true;
                    }

                    return false;
                }
            };

    public void checkUserStatus(){
        //Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //User is signed-in, stay here
            //Set email of logged-in user
            //mProfileTv.setText(user.getEmail());
        } else {
            //User isn't signed-in, go to main activity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //Checks on start of app
        checkUserStatus();
        super.onStart();
    }

    /*Inflate Options Menu*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflating the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*Handle Menu Item Clicks*/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Get Item ID
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}
