package com.example.instagram.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.AddStoryActivity;
import com.example.instagram.Model.Story;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.example.instagram.StoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.Viewholder> {

    private Context mContext;
    private List<Story> mStory;

    public StoryAdapter(Context mContext, List<Story> mStory) {
        this.mContext = mContext;
        this.mStory = mStory;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i ==0){
            View view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item, viewGroup,false);
return new StoryAdapter.Viewholder(view);

        }
        else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.story_item, viewGroup,false);
            return new StoryAdapter.Viewholder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull final Viewholder viewholder, int i) {
        final Story story = mStory.get(i);
        userInfo(viewholder,story.getUserid(),i);
        if (viewholder.getAdapterPosition() != 0){
            seenStory(viewholder,story.getUserid());
        }
        if (viewholder.getAdapterPosition() ==0 ){
            myStory(viewholder.addstory_text,viewholder.story_plus,false);

        }
        viewholder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewholder.getAdapterPosition()==0){
                    myStory(viewholder.addstory_text,viewholder.story_plus,true);
                } else {
                    Intent intent = new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userid",story.getUserid());
                    mContext.startActivity(intent);

                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mStory.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder{

        public ImageView story_photo,story_plus,story_photo_seen;
        public TextView story_username,addstory_text;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            story_photo = itemView.findViewById(R.id.story_photo);
            story_plus = itemView.findViewById(R.id.story_plus);
            story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
            story_username = itemView.findViewById(R.id.story_username);
            addstory_text = itemView.findViewById(R.id.addstory_text);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position ==0){
            return 0;
        }
        return 1;
    }

    private void userInfo(final Viewholder viewholder,final String userid,final int pos){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageurl()).into(viewholder.story_photo);
                if (pos != 0 ){
                    Glide.with(mContext).load(user.getImageurl()).into(viewholder.story_photo_seen);
                    viewholder.story_username.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void myStory(final TextView textView, final ImageView imageView, final boolean click){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0 ;
                long timecurrent = System.currentTimeMillis();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                        count ++;
                    }
                }
                 if (click) {
                     if (count > 0 ){
                         final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                         alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View story",
                                 new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialogInterface, int i) {
                                         Intent intent = new Intent(mContext, StoryActivity.class);
                                         intent.putExtra("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                         mContext.startActivity(intent);
                                         dialogInterface.dismiss();

                                     }
                                 });
                         alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add story",
                                 new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialogInterface, int i){
                                         Intent intent = new Intent(mContext, AddStoryActivity.class);
                                         mContext.startActivity(intent);
                                         dialogInterface.dismiss();
                                     }

                                     });
                         alertDialog.show();
                     } else {
                         Intent intent = new Intent(mContext, AddStoryActivity.class);
                         mContext.startActivity(intent);

                     }

                 } else {
                     if (count > 0){
                         textView.setText("My Story");
                         imageView.setVisibility(View.GONE);
                     } else {
                         textView.setText("Add Story");
                         imageView.setVisibility(View.VISIBLE);
                     }
                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private  void  seenStory(final Viewholder viewholder,String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0 ;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (!snapshot.child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists() &&
                    System.currentTimeMillis() < snapshot.getValue(Story.class).getTimeend()){
                        i++;
                    }
                }
                if (i > 0 ){
                    viewholder.story_photo.setVisibility(View.VISIBLE);
                    viewholder.story_photo_seen.setVisibility(View.GONE);
                } else {
                    viewholder.story_photo.setVisibility(View.GONE);
                    viewholder.story_photo_seen.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
