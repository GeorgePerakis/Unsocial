package com.example.unsocial;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Login extends AppCompatActivity {
    long count;
    public static AppDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView donthave = findViewById(R.id.DontHaveAccount);
        donthave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Sign_Up.class);
                startActivity(intent);
            }
        });

        Button Sign_In = findViewById(R.id.button);
        Sign_In.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User_Login();
            }
        });
    }

    private void User_Login() {
        TextInputEditText UserName = findViewById(R.id.Username);
        String username_string = UserName.getText().toString().trim();

        TextInputEditText Password = findViewById(R.id.Password);
        String password_string = Password.getText().toString().trim();

        if (TextUtils.isEmpty(username_string)) {
            UserName.setError("Please enter a username");
            UserName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password_string)) {
            Password.setError("Please enter a password");
            Password.requestFocus();
            return;
        }

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

        UserUtil.Check_If_User_Exists(username_string, new Callback() {
            @Override
            public void onUserExists(boolean exists,User user) {
                if (exists)
                {
                    Login_User(user.getPassword(),user.getUsername(),user.getAddress(),Password);
                }else
                {
                    UserName.setError("User does not exist");
                    UserName.requestFocus();
                }
            }
        });
    }

    private void Login_User(String password_string, String username_string ,String address_string,TextInputEditText Password) {

        DatabaseReference UsersTable = UserUtil.UsersTable;

        UsersTable.orderByChild("username").equalTo(username_string).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String storedPassword = snapshot.child(username_string).child("password").getValue(String.class);
                if(storedPassword.equals(Password.getText().toString()))
                {
                    addUsertoLocalDB(new User(username_string,password_string,address_string));
                }
                else
                {
                    Password.setError("Invalid password");
                    Password.requestFocus();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Login.this,"Server Error",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void pushUserinLocal(User user)
    {
        ExecutorService Service = Executors.newSingleThreadExecutor();

        Handler handler = new Handler(Looper.getMainLooper());

        Service.execute(new Runnable() {
            @Override
            public void run() {
                database.userDao().insert(user);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Login.this, Home_Screen.class);
                        startActivity(intent);
                    }
                });
            }

        });
    }

    public void addUsertoLocalDB(User user)
    {
        ExecutorService Service = Executors.newSingleThreadExecutor();

        Handler handler = new Handler(Looper.getMainLooper());

        Service.execute(new Runnable() {
            @Override
            public void run() {
                database.userDao().deleteAllUsers();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        pushUserinLocal(user);
                    }
                });
            }
        });
    }



}