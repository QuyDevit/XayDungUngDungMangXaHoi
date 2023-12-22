package ngocquy.nnq.social_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;



import java.util.ArrayList;
import java.util.Date;

import ngocquy.nnq.social_app.Adapter.CommentAdapter;
import ngocquy.nnq.social_app.Model.Comment;

import ngocquy.nnq.social_app.Model.Notification;
import ngocquy.nnq.social_app.Model.Post;
import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.databinding.ActivityCommentBinding;

public class CommentActivity extends AppCompatActivity {
    Intent intent;
    ActivityCommentBinding binding;
    String postID;
    String postedBy;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<Comment> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarcomment);
        CommentActivity.this.setTitle("Bình Luận");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        intent = getIntent();
        postID = intent.getStringExtra("postID");
        postedBy = intent.getStringExtra("postedBy");

        database.getReference().child("posts")
                .child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                Picasso.get().load(post.getPostImage())
                        .placeholder(R.drawable.placeholder)
                        .into(binding.postImg);
                binding.desciption.setText(post.getPostDescription());
                binding.like.setText(post.getPostLike()+" lượt thích bài viết này, "+post.getCommentCount()+" lượt bình luận");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("User")
                .child(postedBy).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Picasso.get().load(user.getProfile())
                        .placeholder(R.drawable.placeholder)
                        .into(binding.profileImage);
                binding.name.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        database.getReference()
                .child("posts")
                .child(postID)
                .child("likes")
                .child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            setLikeButtonState(true);
                        } else {
                            setLikeButtonState(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle errors if necessary
                    }
                });
        binding.commentPostbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Comment comment = new Comment();
                comment.setCommentedBy(FirebaseAuth.getInstance().getUid());
                comment.setCommentBody(binding.editTextcomment.getText().toString());
                comment.setCommentedAt(new Date().getTime());
                database.getReference().child("posts")
                        .child(postID).child("comments").push()
                        .setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("posts")
                                        .child(postID).child("commentCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                int commentCount = 0;
                                                if(snapshot.exists()){
                                                    commentCount = snapshot.getValue(Integer.class);
                                                }
                                                database.getReference().child("posts")
                                                        .child(postID)
                                                        .child("commentCount")
                                                        .setValue(commentCount + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                binding.editTextcomment.setText("");

                                                                Notification notification = new Notification();
                                                                notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                                notification.setNotificationAt(new Date().getTime());
                                                                notification.setPostID(postID);
                                                                notification.setPostedBy(postedBy);
                                                                notification.setType("Bình luận");

                                                                FirebaseDatabase.getInstance().getReference()
                                                                        .child("notification")
                                                                        .child(postedBy)
                                                                        .push()
                                                                        .setValue(notification);
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        });
            }
        });

        binding.editTextcomment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length()>0){
                    binding.commentPostbtn.setBackgroundDrawable(ContextCompat.getDrawable(CommentActivity.this,R.drawable.ic_send));
                    binding.commentPostbtn.setEnabled(true);
                }else {
                    binding.commentPostbtn.setBackgroundDrawable(ContextCompat.getDrawable(CommentActivity.this,R.drawable.ic_checksend));
                    binding.commentPostbtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        list = new ArrayList<>();
        CommentAdapter adapter = new CommentAdapter(list,this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.commentRV.setLayoutManager(layoutManager);
        binding.commentRV.setAdapter(adapter);

        database.getReference().child("posts")
                .child(postID).child("comments").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Comment comment = dataSnapshot.getValue(Comment.class);
                            comment.setCommentID(dataSnapshot.getKey());
                            list.add(comment);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setLikeButtonState(boolean isLiked) {
        if (isLiked) {
            Drawable drawableLeft = CommentActivity.this.getResources().getDrawable(R.drawable.ic_liked);
            binding.like.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
        } else {
            Drawable drawableLeft = CommentActivity.this.getResources().getDrawable(R.drawable.ic_like);
            binding.like.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseDatabase.getInstance().getReference().child("User").child(auth.getCurrentUser().getUid()).child("status").setValue(false);
    }
}