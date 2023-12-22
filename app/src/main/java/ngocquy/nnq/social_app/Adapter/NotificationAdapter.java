package ngocquy.nnq.social_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ngocquy.nnq.social_app.CommentActivity;
import ngocquy.nnq.social_app.Model.Notification;
import ngocquy.nnq.social_app.Model.Post;
import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.R;
import ngocquy.nnq.social_app.databinding.Thongbao2RvSampleBinding;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder>{
    ArrayList<Notification> list;
    Context context;

    public NotificationAdapter(ArrayList<Notification> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.thongbao2_rv_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Notification notification = list.get(position);
        String type = notification.getType();
        holder.binding.time.setText(getRelativeTime(notification.getNotificationAt()));
        FirebaseDatabase.getInstance().getReference().child("User")
                .child(notification.getNotificationBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Picasso.get().load(user.getProfile()).placeholder(R.drawable.placeholder)
                                .into(holder.binding.profileImage);
                        DatabaseReference checkPost = FirebaseDatabase.getInstance().getReference().child("posts")
                                .child(notification.getPostID());
                        if(type.equals("like")){
                            checkPost.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                Post post = snapshot.getValue(Post.class);
                                                holder.binding.notification.setText(Html.fromHtml("<b>"+user.getName()+"</b>"+ " đã like bài viết "+"<b>"+post.getPostDescription()+"</b> của bạn"));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                        }else if (type.equals("Bình luận")) {
                            checkPost.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        Post post = snapshot.getValue(Post.class);
                                        holder.binding.notification.setText(Html.fromHtml("<b>" + user.getName() + "</b>" + " đã bình luận bài viết" + " <b>" + post.getPostDescription() + "</b> của bạn"));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else {
                            holder.binding.notification.setText(Html.fromHtml("<b>"+user.getName()+"</b>"+ " đã theo dõi bạn"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.binding.openNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!type.equals("Theo dõi")){
                    FirebaseDatabase.getInstance().getReference()
                            .child("notification")
                            .child(notification.getPostedBy())
                            .child(notification.getNotificationID())
                                    .child("checkOpen")
                                            .setValue(true);
                    holder.binding.openNotification.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    Intent intent = new Intent(context, CommentActivity.class);
                    intent.putExtra("postID",notification.getPostID());
                    intent.putExtra("postedBy",notification.getPostedBy());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });

        Boolean checkOpen = notification.isCheckOpen();
        if(checkOpen == true){
            holder.binding.openNotification.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }else {
            holder.binding.openNotification.setBackgroundColor(Color.parseColor("#E2ECF3"));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        Thongbao2RvSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = Thongbao2RvSampleBinding.bind(itemView);

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
