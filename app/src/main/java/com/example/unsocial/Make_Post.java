package com.example.unsocial;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Make_Post extends AppCompatActivity {
    User current;
    long count;
    String current_name;
    String current_address;

    public static AppDatabase database;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_make_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.post_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RoomDatabase.Callback myCallBack = new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }

            @Override
            public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
                super.onDestructiveMigration(db);
            }
        };

        database = Room.databaseBuilder(context,
                AppDatabase.class, "my-database").addCallback(myCallBack).build();

        getUserinLocal();

        getPostsNumber();

        Button post_button = findViewById(R.id.Publish_Post);

        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_post();
            }
        });

    }

    void add_post(){
        TextInputEditText Title = findViewById(R.id.User_Post_Title);
        String title_string = Title.getText().toString().trim();

        TextInputEditText Description = findViewById(R.id.User_Post_Description);
        String description_string = Description.getText().toString().trim();


        if (TextUtils.isEmpty(title_string)) {
            Title.setError("Please enter a post title");
            Title.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description_string)) {
            Description.setError("Please enter a post description");
            Description.requestFocus();
            return;
        }

        Post created_post = new Post(description_string,current_address,current_name,title_string);

//      String postId = generatePostId(String.valueOf(System.currentTimeMillis())+"_"+current_name);

        UserUtil.PostsTable.child(String.valueOf(count+1)).setValue(created_post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Make_Post.this,"Post published",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Make_Post.this, Home_Screen.class);
                    startActivity(intent);
                } else {
                    // Registration failed, display error message
                    Exception e = task.getException();

                    if (e != null) {
                        String errorMessage = e.getMessage(); // Get the error message
                        Log.e("PostError", "Publish failed: " + errorMessage);
                    } else {
                        Log.e("PostError", "Publish failed: Unknown error occurred");
                    }
                }
            }
        });
    }

    public void getUserinLocal()
    {
        ExecutorService Service = Executors.newSingleThreadExecutor();

        Handler handler = new Handler(Looper.getMainLooper());

        Service.execute(new Runnable() {
            @Override
            public void run() {
                current = database.userDao().getLastUser();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        current_name = current.getUsername();
                        current_address = current.getAddress();
                    }
                });
            }


        });
    }

//    private String generatePostId(String data) {
//        try {
//            // Create MessageDigest instance for MD5
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            // Add data to MessageDigest
//            md.update(data.getBytes());
//            // Get the hash's bytes
//            byte[] bytes = md.digest();
//            // Convert bytes to hexadecimal format
//            StringBuilder sb = new StringBuilder();
//            for (byte aByte : bytes) {
//                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
//            }
//            // Return the hexadecimal string
//            return sb.toString();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            return null; // Return null in case of an error
//        }
//    }

    public void getPostsNumber()
    {
        UserUtil.PostsTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                count = dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseCount", "Error: " + databaseError.getMessage());
            }
        });
    }
}