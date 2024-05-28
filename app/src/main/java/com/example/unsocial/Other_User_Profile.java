package com.example.unsocial;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.unsocial.databinding.ActivityOtherUserProfileBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Other_User_Profile extends AppCompatActivity {
    String username;
    AppDatabase database;
    String address;
    String url;
    Button unfollow;
    User current;
    TextView unfollowsNumber;
    TextView unfollowersNumber;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

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

        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "my-database").addCallback(myCallBack).build();

        getUserinLocal();

    }


    private void getUserData(String username)
    {
        ImageView profile = findViewById(R.id.Profile_Pic);

        TextView postsNumber = findViewById(R.id.postsNumber);

        TextView User_address = findViewById(R.id.user_address);

        TextView User_name = findViewById(R.id.user_name);

        TextView profile_text = findViewById(R.id.titleName);

        UserUtil.PostsTable.orderByChild("name").equalTo(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    long numberOfPosts = dataSnapshot.getChildrenCount();
                    postsNumber.setText(String.valueOf(numberOfPosts));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle cancelled event
                Log.w("Post data", "loadPost:onCancelled", databaseError.toException());
            }
        });

        UserUtil.UsersTable.orderByChild("username").equalTo(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String address = snapshot.child(username).child("address").getValue(String.class);
                String imageUrl = snapshot.child(username).child("url").getValue(String.class);

                long unfollowersCount = snapshot.child(username).child("unfollowers").getChildrenCount();
                long unfollowingCount = snapshot.child(username).child("unfollowing").getChildrenCount();

                unfollowersNumber.setText(String.valueOf(unfollowersCount));
                unfollowsNumber.setText(String.valueOf(unfollowingCount));

                profile_text.setText(username+"'s Profile");

                User_address.setText(address);

                User_name.setText(username);

                if(imageUrl != null) {
                    Log.e("URL",imageUrl);
                    Picasso.get().load(imageUrl).resize(169,175).into(profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FAILED TO FIND USER","FAILED");
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
                        intent = getIntent();
                        if (intent != null) {
                            username = intent.getStringExtra("username");

                            if(!username.equals(current.getUsername()))
                            {
                                getUserData(username);

                                unfollowersNumber = findViewById(R.id.followersNumber);
                                unfollowsNumber = findViewById(R.id.followsNumber);

                                unfollow = findViewById(R.id.Unfollow);

                                unfollow.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        checkifUnfollowing();
                                    }
                                });
                            }else
                            {
                                Toast.makeText(getApplicationContext(),"This is your profile",Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                });
            }
        });
    }

    public void checkifUnfollowing()
    {
        UserUtil.UsersTable.orderByChild("username").equalTo(current.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        DataSnapshot followingSnapshot = userSnapshot.child("unfollowing");
                        for (DataSnapshot followingUserSnapshot : followingSnapshot.getChildren()) {
                            String followingUsername = followingUserSnapshot.getValue(String.class);

                            if (followingUsername != null && followingUsername.equals(username)) {
                                Intent intent = new Intent(getApplicationContext(), Home_Screen.class);
                                startActivity(intent);
                                Toast.makeText(getApplicationContext(),"You are already unfollowing",Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }

                    UserUtil.UsersTable.orderByChild("username").equalTo(current.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserUtil.UsersTable.child(current.getUsername()).child("unfollowing").push().setValue(username);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("FAILED TO FIND USER","FAILED");
                        }
                    });

                    UserUtil.UsersTable.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            UserUtil.UsersTable.child(username).child("unfollowers").push().setValue(current.getUsername());

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("FAILED TO FIND USER","FAILED");
                        }
                    });

                    Intent intent = new Intent(getApplicationContext(), Home_Screen.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(),"Unfollowed succesfully",Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FAILED TO FIND USER","FAILED");
            }
        });
    }
}