package com.example.friendsly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //View
    EditText mEmailEt,mPasswordEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;

    //ProgressBar to display while registering user
    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Actionbar & it's Title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        //Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //Initialise Views
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mHaveAccountTv = findViewById(R.id.have_accountTv);

        //Initialise the FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        //Handle Register Button Click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Input email, password
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                //Validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //Set error & focus to email edittext
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else if (password.length() < 6){
                    //Set error & focus to email edittext
                    mPasswordEt.setError("Password must be at least 6 characters");
                    mPasswordEt.setFocusable(true);
                }
                else {
                    registerUser(email, password); //Registers the user
                }
            }
        });

        //Handle Login TextView Click Listener
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
            private void registerUser(String email, String password) {
                //Email & password pattern is valid, show progress dialog & start registering user
                progressDialog.show();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Sign-in successful, dismiss dialog & start register activity
                                    progressDialog.dismiss();

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    //Get User Email & User ID From Auth
                                    String email = user.getEmail();
                                    String uid = user.getUid();
                                    //When a user is registered, Store the user info in
                                    //Firebase Realtime Database too using HashMap
                                    HashMap<Object, String> hashMap = new HashMap<>();
                                    //Putting info in the HashMap
                                    hashMap.put("email", email);
                                    hashMap.put("uid", uid);
                                    hashMap.put("name", "");
                                    hashMap.put("phone", "");
                                    hashMap.put("image", "");
                                    hashMap.put("cover", "");

                                    //Firebase Database Instance
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    //Path for storing user data named "Users"
                                    DatabaseReference reference = database.getReference("Users");
                                    //Putting data within the HashMap into the Database
                                    reference.child(uid).setValue(hashMap);

                                    Toast.makeText(RegisterActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                                    finish();
                                } else {
                                    //If sign-in fails, display a message to the user
                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Error, dismiss progress dialog & show error message
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //Go back to previous activity
        return super.onSupportNavigateUp();
    }
}
