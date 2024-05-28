package com.example.unsocial;

import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import com.example.unsocial.Callback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

public class UserUtil {
    static StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    static DatabaseReference PostsTable = FirebaseDatabase.getInstance().getReference("posts");
    static DatabaseReference UsersTable = FirebaseDatabase.getInstance().getReference("users");

    static Sign_Up signUp = new Sign_Up();

    static Context mContext;

    public UserUtil(Context context){
        mContext = context.getApplicationContext();
    }


    public static void Push_User(String UserName,String Password, String Address) {

        User Created_User = new User(UserName,Password,Address);

        UsersTable.child(UserName).setValue(Created_User).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    return;
                } else {
                    Exception e = task.getException();

                    if (e != null) {
                        String errorMessage = e.getMessage();
                        Log.e("RegistrationError", "Registration failed: " + errorMessage);
                        Toast.makeText(signUp, "Registration failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("RegistrationError", "Registration failed: Unknown error occurred");
                        Toast.makeText(signUp, "Registration failed: Unknown error occurred", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public static void Check_If_User_Exists(String UserName, Callback callback){

        UsersTable.orderByChild("username").equalTo(UserName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userExists = snapshot.exists();
                if (userExists && snapshot.getChildrenCount() > 0) {
                    User user = snapshot.getChildren().iterator().next().getValue(User.class);
                    callback.onUserExists(userExists, user);
                } else {
                    callback.onUserExists(userExists, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onUserExists(false,new User());
            }
        });
    }

    public static void Edit_User(String UserName,String Password, String Address,String userid, Uri uri, String prev) {

        UsersTable.child(userid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("RemoveUser", "User removed successfully");
                } else {
                    Exception e = task.getException();
                    String errorMessage = e != null ? e.getMessage() : "Unknown error occurred";
                    Log.e("RemoveUser", "Failed to remove user: " + errorMessage);
                    Toast.makeText(signUp, "Failed to remove user: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Push_User(UserName,Password,Address);

        if(uri != null)
        {
            StorageReference imageRef = storageRef.child("images/" + UserName + "/profile.jpg");

            UploadTask uploadTask = imageRef.putFile(uri);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    StorageReference imageRef = storageRef.child("images/" + UserName + "/profile.jpg");
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUrl) {
                            String imageUrl = downloadUrl.toString();
                            UserUtil.UsersTable.child(UserName).child("url").setValue(imageUrl);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("IMAGE", "onFailure: Failed to upload image");
                }
            });
        }
        else if(prev != null)
        {
            UserUtil.UsersTable.orderByChild("username").equalTo(UserName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserUtil.UsersTable.child(UserName).child("url").setValue(prev);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FAILED TO FIND USER","FAILED");
                }
            });
        }
    }

}
