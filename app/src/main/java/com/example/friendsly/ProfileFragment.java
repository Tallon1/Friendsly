package com.example.friendsly;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //Storage
    StorageReference storageReference;

    //Path where images of user's profile & cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";

    //Views from XML
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton fab;

    //Progress Dialog
    ProgressDialog pd;

    //Permissions Constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //Arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    //URI of Picked Image
    Uri image_uri;

    //For Checking Profile or Cover Photo
    String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //Initialise Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //Firebase Storage Reference

        //Initialise Arrays of Permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //Initialise Views
        avatarIv = view.findViewById(R.id.avatarIv);
        coverIv = view.findViewById(R.id.coverIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        fab = view.findViewById(R.id.fab);

        //Initialise Progress Dialog
        pd = new ProgressDialog(getActivity());

        /*To get info of currently signed-in user we get it using user's email or uid*/
        /*With orderByChild we can search all nodes, where the key matches it will get it's details*/
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Checks until the required data is retrieved
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    //Gets data
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String cover = ""+ ds.child("cover").getValue();

                    //Sets data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        //If image is received then set
                        Picasso.get().load(image).into(avatarIv);
                    } catch (Exception e){
                        //If there is any exception while getting the image then set to default
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                    }

                    try {
                        //If image is received then set
                        Picasso.get().load(cover).into(coverIv);
                    } catch (Exception e){
                        //If there is any exception while getting the image then set to default
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //fab Button Click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        return view;
    }

    private boolean checkStoragePermission(){
        //Checks if storage permission is enabled or not, returns true if enabled, false if not
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        //Requests runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        //Checks if storage permission is enabled or not, returns true if enabled, false if not
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        //Requests runtime storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        //Options to show in dialog
        String[] options = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone"};

        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set Title
        builder.setTitle("Choose Action");

        //Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Handles dialog item clicks
                if (which == 0){
                    //Edit Profile Clicked
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "image"; //For changing profile picture
                    showImagePicDialog();

                } else if (which == 1){
                    //Edit Cover Clicked
                    pd.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto = "cover"; //For changing cover photo
                    showImagePicDialog();

                } else if (which == 2){
                    //Edit Name Clicked
                    pd.setMessage("Updating Name");
                    //Calls method & passes key "name" as a parameter to update it's value in the database
                    showNamePhoneUpdateDialog("name");

                } else if (which == 3){
                    //Edit Phone Clicked
                    pd.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");

                }
            }
        });

        //Create & Show Dialog
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(final String key) {
        //Parameter "key" contains either "name" - (used to update a user's name) or "phone" - (used to update a user's phone)

        //Custom Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+ key); //e.g. Update name OR Update phone

        //Set Layout of Dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        //Add EditText
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+ key); //Hint e.g. Edit name OR Edit phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //Add Button in Dialog to Update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Input text from EditText
                String value = editText.getText().toString().trim();
                //Validate if user has entered something or not
                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Updated, now dismisses progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Failed, dismisses progress, get & show error message
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else {
                    Toast.makeText(getActivity(), "Please enter "+ key, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Add Button in Dialog to Cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //Create & Show Dialog
        builder.create().show();

    }

    private void showImagePicDialog() {
        //Show dialog containing options for Camera & Gallery to pick an image
        String options[] = {"Camera", "Gallery"};

        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set Title
        builder.setTitle("Pick An Image From");

        //Set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Handles dialog item clicks
                if (which == 0){
                    //Camera Clicked
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1){
                    //Gallery Clicked
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });

        //Create & Show Dialog
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*This method is called when the user presses Allow/Deny from permission request dialog*/

        switch (requestCode){

            case CAMERA_REQUEST_CODE: {
                //Picking image from camera, first check if camera & storage permissions are allowed
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        //Permissions Enabled
                        pickFromCamera();
                    } else {
                        //Permissions Denied
                        Toast.makeText(getActivity(), "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE: {
                //Picking image from gallery, first check if storage permissions are allowed
                if (grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        //Permissions Enabled
                        pickFromGallery();
                    } else {
                        //Permissions Denied
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //This method is called after picking an image from Camera or Gallery
        if (resultCode == RESULT_OK){

            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //Image is picked from gallery, get uri of the image
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //Image is picked from camera, get uri of the image

                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        //Show Progress
        pd.show();

        //Path & name of image to be stored in Firebase Storage
        //e.g. Users_Profile_Cover_Imgs/image_e12f3456f789.jpg
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Image is uploaded to storage, now get it's URL & store in user's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //Checks if image is uploaded or not + if URL is received
                        if (uriTask.isSuccessful()){
                            //Image Uploaded
                            //Add/Update URL in user's database
                            HashMap<String, Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //URL in user's database is successfully added
                                            //Dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //Error adding URL to user's database
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error Updating Image...", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            //Error Message
                            pd.dismiss();
                            Toast.makeText(getActivity(), "An Error Has Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //If there's an error(s), get & show error message, dismiss progress dialog
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickFromCamera() {
        //Intent for picking image from device's camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //Puts in image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //Intent for starting camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        //Intent for picking from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }
}
