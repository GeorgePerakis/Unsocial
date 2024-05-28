package com.example.unsocial;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Post_Adapter extends RecyclerView.Adapter<Post_Adapter.MyViewHolder> {
    Context context;
    ArrayList<Post> posts;

    public Post_Adapter(Context context,ArrayList<Post> posts)
    {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public Post_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_layout,parent,false);
        return new Post_Adapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Post_Adapter.MyViewHolder holder, int position) {
        holder.name.setText(posts.get(position).getName());
        holder.address.setText(posts.get(position).getAddress());
        holder.title.setText(posts.get(position).getTitle());
        holder.description.setText(posts.get(position).getDescription());
        String username = posts.get(position).getName();
        UserUtil.UsersTable.orderByChild("username").equalTo(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String imageUrl = snapshot.child(username).child("url").getValue(String.class);

                if(imageUrl != null) {
                    Log.e("URL",imageUrl);
                    Picasso.get().load(imageUrl).resize(100,100).into(holder.image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FAILED TO FIND USER","FAILED");
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Other_User_Profile.class);

                intent.putExtra("username", username);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView address,name,title,description;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.Post_Title);
            description = itemView.findViewById(R.id.Post_Description);
            name = itemView.findViewById(R.id.Post_User);
            address = itemView.findViewById(R.id.Post_Address);
            image = itemView.findViewById(R.id.Post_Profile);

        }
    }


}
