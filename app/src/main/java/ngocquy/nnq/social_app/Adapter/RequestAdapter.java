package ngocquy.nnq.social_app.Adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ngocquy.nnq.social_app.Model.Friend;
import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.R;
import ngocquy.nnq.social_app.databinding.YeucauRvSampleBinding;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.viewHolder>{
    ArrayList<Friend> list;
    Context context;

    public RequestAdapter(ArrayList<Friend> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.yeucau_rv_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Friend friend = list.get(position);
        fetchUserName(friend.getUserID(), holder.binding.requestName,holder.binding.profileImage);
        holder.binding.time.setText(getRelativeTime(friend.getFriendAt()));
        holder.binding.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference friendRef = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("User")
                        .child(FirebaseAuth.getInstance().getUid())
                        .child("friends")
                        .child(friend.getUserID());
                friendRef.child("accept").setValue(true, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    User user = snapshot.getValue(User.class);
                                    Friend fr = new Friend();
                                    fr.setFriendBy(FirebaseAuth.getInstance().getUid());
                                    fr.setAccept(true);
                                    fr.setFriendAt(new Date().getTime());
                                    FirebaseDatabase.getInstance().getReference().child("User")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .child("friendCount").setValue(user.getFriendCount() + 1);

                                    FirebaseDatabase.getInstance().getReference().child("User")
                                            .child(friend.getUserID())
                                            .child("friends")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .setValue(fr).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    FirebaseDatabase.getInstance().getReference().child("User").child(friend.getUserID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.exists()){
                                                                User user1 = snapshot.getValue(User.class);
                                                                FirebaseDatabase.getInstance().getReference().child("User").child(friend.getUserID()).child("friendCount").setValue(user1.getFriendCount() + 1);
                                                                Toast.makeText(context, "kết bạn thành công!", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

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
                    }
                });

            }
        });
        holder.binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference friendRef = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("User")
                        .child(FirebaseAuth.getInstance().getUid())
                        .child("friends")
                        .child(friend.getUserID());

                friendRef.removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if(error == null){
                            Toast.makeText(context, "Bạn đã hủy kết bạn với "+holder.binding.requestName.getText(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    private void fetchUserName(String userID, TextView textView, ImageView imageView) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("User")
                .child(userID);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        Picasso.get().load(user.getProfile()).placeholder(R.drawable.placeholder).into(imageView);
                        textView.setText(Html.fromHtml("<b>"+ user.getName()+"</b>"+" đã gửi lời mời kết bạn với bạn") );
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors here
            }
        });
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


    public class viewHolder extends RecyclerView.ViewHolder{
        YeucauRvSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = YeucauRvSampleBinding.bind(itemView);
        }
    }
}
