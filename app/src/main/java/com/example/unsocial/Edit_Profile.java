package com.example.unsocial;

import static com.example.unsocial.UserUtil.storageRef;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresExtension;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Edit_Profile extends AppCompatActivity {
    EditText name;
    EditText password;
    EditText address;
    ImageView profile;
    Button submit;
    String name_input;
    String password_input;
    String address_input;
    public static AppDatabase database;
    User current;
    Uri imageUri;
    ActivityResultLauncher<Intent> resultLauncher;
    String prev;

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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

        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "my-database").addCallback(myCallBack).build();

        getUserinLocal();


        name = findViewById(R.id.user_name);
        password = findViewById(R.id.user_password);
        address = findViewById(R.id.user_address);
        profile = findViewById(R.id.Profile_Pic);
        submit = findViewById(R.id.submitProfileButton);

        registerResult();

        profile.setOnClickListener(view -> pickImage());

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().isEmpty())
                {
                    name_input = current.getUsername();
                } else name_input = name.getText().toString();

                if(password.getText().toString().isEmpty())
                {
                    password_input = current.getPassword();
                }else password_input = password.getText().toString();

                if(address.getText().toString().isEmpty())
                {
                    address_input = current.getAddress();
                }else address_input = address.getText().toString();

                UserUtil.Edit_User(name_input,password_input,address_input,current.getUsername(),imageUri,prev);

                Intent intent = new Intent(Edit_Profile.this, Login.class);
                startActivity(intent);
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
                        getPrevious();
                    }
                });
            }


        });
    }

    public void registerResult(){
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        try {
                            imageUri = o.getData().getData();
                            profile.setImageURI(imageUri);
                            profile.setScaleType(ImageView.ScaleType.FIT_XY);
                        }catch (Exception e)
                        {
                            Toast.makeText(Edit_Profile.this,"Could not upload",Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    public void pickImage()
    {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    public void getPrevious()
    {
        UserUtil.PostsTable.orderByChild("name").equalTo(current.getUsername()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    UserUtil.UsersTable.orderByChild("username").equalTo(current.getUsername()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String url = snapshot.child(current.getUsername()).child("url").getValue(String.class);

                            if(url != null)
                            {
                                Log.e("URL222",url);
                                prev = url;
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