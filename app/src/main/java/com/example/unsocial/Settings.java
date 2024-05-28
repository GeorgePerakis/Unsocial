package com.example.unsocial;

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
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Settings extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    AppDatabase database;
    User current;
    List<String> unfollowing_list = new ArrayList<>();
    List<String> unfollowers_list = new ArrayList<>();
    ListView listUnfollowers;
    ListView listUnfollowing;
    Switch togglelists;
    LinearLayout list_container;
    private String mParam1;
    private String mParam2;

    public Settings() {
        // Required empty public constructor
    }

    public static Settings newInstance(String param1, String param2) {
        Settings fragment = new Settings();
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

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        listUnfollowers = view.findViewById(R.id.listView1);
        listUnfollowing = view.findViewById(R.id.listView2);
        togglelists = view.findViewById(R.id.switch2);
        list_container = view.findViewById(R.id.listContainer);

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

        getUserinLocal();


        togglelists.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Switch is ON, make the container visible
                    list_container.setVisibility(View.VISIBLE);
                } else {
                    // Switch is OFF, make the container gone
                    list_container.setVisibility(View.GONE);
                }
            }
        });

        return view;
    }

    public void getNumbers()
    {
        UserUtil.UsersTable.orderByChild("username").equalTo(current.getUsername()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot unfollowingSnapshot = snapshot.child(current.getUsername()).child("unfollowing");

                for (DataSnapshot childSnapshot : unfollowingSnapshot.getChildren()) {
                    String childValue = childSnapshot.getValue(String.class);
                    if(childValue != null) unfollowing_list.add(childValue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FAILED TO FIND USER","FAILED");
            }
        });

        UserUtil.UsersTable.orderByChild("username").equalTo(current.getUsername()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot unfollowersSnapshot = snapshot.child(current.getUsername()).child("unfollowers");

                for (DataSnapshot childSnapshot : unfollowersSnapshot.getChildren()) {
                    String childValue = childSnapshot.getValue(String.class);
                    if(childValue != null) unfollowers_list.add(childValue);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FAILED TO FIND USER","FAILED");
            }
        });

        populateLists();
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
                        getNumbers();
                    }
                });
            }
        });
    }

    public void populateLists()
    {
        if(unfollowers_list != null)
        {
            ArrayAdapter<String> unfollowersAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, unfollowers_list);
            listUnfollowers.setAdapter(unfollowersAdapter);
        }

        if(unfollowing_list != null)
        {
            ArrayAdapter<String> unfollowingAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, unfollowing_list);
            listUnfollowing.setAdapter(unfollowingAdapter);
        }

    }
}
