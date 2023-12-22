package ngocquy.nnq.social_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ngocquy.nnq.social_app.Model.Comment;
import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.R;
import ngocquy.nnq.social_app.databinding.CommentRvSampleBinding;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.viewHolder> {
    ArrayList<Comment> list;
    Context context;

    public CommentAdapter(ArrayList<Comment> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_rv_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Comment comment = list.get(position);
        holder.binding.comment.setText(comment.getCommentBody());
        holder.binding.time.setText(getRelativeTime(comment.getCommentedAt()));

        FirebaseDatabase.getInstance().getReference().child("User")
                .child(comment.getCommentedBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Picasso.get().load(user.getProfile())
                                .placeholder(R.drawable.placeholder)
                                .into(holder.binding.profileImage);
                        holder.binding.name.setText(user.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        CommentRvSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CommentRvSampleBinding.bind(itemView);
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
