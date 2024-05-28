package com.example.unsocial;


import com.example.unsocial.Callback;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Sign_Up extends AppCompatActivity {
    DatabaseReference UsersTable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView have = findViewById(R.id.AlreadyHaveAccount);
        have.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Sign_Up.this, Login.class);
                startActivity(intent);
            }
        });

        Button Sign_In = findViewById(R.id.button);
        Sign_In.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        TextInputEditText UserName = findViewById(R.id.Username);
        String username_string = UserName.getText().toString().trim();

        TextInputEditText Password = findViewById(R.id.Password);
        String password_string = Password.getText().toString().trim();

        TextInputEditText Address = findViewById(R.id.Address);
        String address_string = Address.getText().toString().trim();

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

        if (TextUtils.isEmpty(address_string)) {
            Address.setError("Please enter address");
            Address.requestFocus();
            return;
        }

        UserUtil.Check_If_User_Exists(username_string, new Callback() {
            @Override
            public void onUserExists(boolean exists,User user) {
                if (exists)
                {
                    UserName.setError("Username Taken");
                    UserName.requestFocus();
                }else
                {
                    UserUtil.Push_User(username_string,password_string,address_string);
                    Toast.makeText(Sign_Up.this,"Account created",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Sign_Up.this, Login.class);
                    startActivity(intent);
                }
            }
        });


    }

}
