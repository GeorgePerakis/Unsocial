package com.example.unsocial;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Profile extends Fragment {
    public static AppDatabase database;
    TextView name;
    TextView password;
    TextView address;
    TextView postsNumber;
    TextView unfollowsNumber;
    TextView unfollowersNumber;
    Button editProfileButton;
    ImageView profile;
    User current;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Profile() {
        // Required empty public constructor
    }
    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        editProfileButton = view.findViewById(R.id.Unfollow);

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Edit_Profile.class);
                startActivity(intent);
            }
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

        database = Room.databaseBuilder(getActivity().getApplicationContext(),
                AppDatabase.class, "my-database").addCallback(myCallBack).build();


        name = view.findViewById(R.id.user_name);
        password = view.findViewById(R.id.user_password);
        address = view.findViewById(R.id.user_address);
        postsNumber = view.findViewById(R.id.postsNumber);
        profile = view.findViewById(R.id.Profile_Pic);
        unfollowersNumber = view.findViewById(R.id.unfollowersNumber);
        unfollowsNumber = view.findViewById(R.id.unfollowsNumber);

        getUserinLocal();

        return view;
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
                        name.setText(current.getUsername());
                        password.setText(current.getPassword());
                        address.setText(current.getAddress());
                        getUserData();
                    }
                });
            }
        });
    }

    private void getUserData() {
        UserUtil.PostsTable.orderByChild("name").equalTo(current.getUsername()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    long numberOfPosts = dataSnapshot.getChildrenCount();
                    postsNumber.setText(String.valueOf(numberOfPosts));
                    UserUtil.UsersTable.orderByChild("username").equalTo(current.getUsername()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long unfollowersCount = snapshot.child(current.getUsername()).child("unfollowers").getChildrenCount();
                            long unfollowingCount = snapshot.child(current.getUsername()).child("unfollowing").getChildrenCount();

                            unfollowersNumber.setText(String.valueOf(unfollowersCount));
                            unfollowsNumber.setText(String.valueOf(unfollowingCount));

                            String imageUrl = snapshot.child(current.getUsername()).child("url").getValue(String.class);

                            if(imageUrl != null) {
                                if(!imageUrl.isEmpty())
                                {
                                    Picasso.get().load(imageUrl).resize(169,175).into(profile);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("FAILED TO FIND USER","FAILED");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle cancelled event
                Log.w("Post data", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }
}