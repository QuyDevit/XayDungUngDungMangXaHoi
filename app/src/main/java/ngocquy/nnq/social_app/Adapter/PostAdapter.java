package ngocquy.nnq.social_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import ngocquy.nnq.social_app.CommentActivity;
import ngocquy.nnq.social_app.Model.Like;
import ngocquy.nnq.social_app.Model.Notification;
import ngocquy.nnq.social_app.Model.Post;
import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.R;
import ngocquy.nnq.social_app.databinding.DasboardRvSampleBinding;
import ngocquy.nnq.social_app.databinding.FragmentTrangchuBinding;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder> {

    ArrayList<Post> list;
    Context context;

    public PostAdapter(ArrayList<Post> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dasboard_rv_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        Post post = list.get(position);
        Picasso.get().load(post.getPostImage())
                .placeholder(R.drawable.placeholder)
                .into(holder.binding.postImg);
        holder.binding.like.setText(post.getPostLike()+"");
        holder.binding.comment.setText(post.getCommentCount()+"");
        String description = post.getPostDescription();
        if(description.equals("")){
            holder.binding.txtTitle.setVisibility(View.GONE);
        }else {
            holder.binding.txtTitle.setText(post.getPostDescription());
            holder.binding.txtTitle.setVisibility(View.VISIBLE);
        }

        holder.binding.about.setText(getRelativeTime(post.getPostedAt()));
        FirebaseDatabase.getInstance().getReference()
                .child("User").child(post.getPostedBy())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Picasso.get().load(user.getProfile())
                        .placeholder(R.drawable.placeholder)
                        .into(holder.binding.profileImage);
                holder.binding.userName.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference()
                .child("posts")
                .child(post.getPostId())
                .child("likes")
                .child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Drawable drawableLeft = context.getResources().getDrawable(R.drawable.ic_liked);
                            holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
                            holder.binding.like.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Remove the like from the post
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("posts")
                                            .child(post.getPostId())
                                            .child("likes")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    // Update the post's like count
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child("posts")
                                                            .child(post.getPostId())
                                                            .child("postLike")
                                                            .setValue(post.getPostLike() - 1)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Drawable drawableLeft = context.getResources().getDrawable(R.drawable.ic_like);
                                                                    holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                        }
                        else {
                            Like like = new Like();
                            like.setLike(true);
                            like.setLikedby(FirebaseAuth.getInstance().getUid());
                            Drawable drawableLeft = context.getResources().getDrawable(R.drawable.ic_like);
                            holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
                            holder.binding.like.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    FirebaseDatabase.getInstance().getReference().child("posts")
                                            .child(post.getPostId()).child("likes")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .setValue(like).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child("posts")
                                                            .child(post.getPostId())
                                                            .child("postLike")
                                                            .setValue(post.getPostLike() + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Drawable drawableLeft = context.getResources().getDrawable(R.drawable.ic_liked);
                                                                    holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);

                                                                    Notification notification = new Notification();
                                                                    notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                                    notification.setNotificationAt(new Date().getTime());
                                                                    notification.setPostID(post.getPostId());
                                                                    notification.setPostedBy(post.getPostedBy());
                                                                    notification.setType("like");

                                                                    FirebaseDatabase.getInstance().getReference().child("notification")
                                                                            .child(post.getPostedBy())
                                                                            .push()
                                                                            .setValue(notification);
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.binding.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postID",post.getPostId());
                intent.putExtra("postedBy",post.getPostedBy());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        DasboardRvSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DasboardRvSampleBinding.bind(itemView);

        }
    }
    private String getRelativeTime(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - timestamp;

        if (timeDifference < 60000) { // Less than 1 minute
            return "vừa xong";
        } else if (timeDifference < 3600000) { // Less than 1 hour
            long minutesAgo = timeDifference / 60000;
            return minutesAgo + " phút trước";
        } else if (timeDifference < 86400000) { // Less than 1 day
            long hoursAgo = timeDifference / 3600000;
            return hoursAgo + " giờ trước";
        } else { // More than 1 day
            long daysAgo = timeDifference / 86400000;
            return daysAgo + " ngày trước";
        }
    }
}
