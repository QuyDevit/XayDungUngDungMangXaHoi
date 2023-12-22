package ngocquy.nnq.social_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;


import ngocquy.nnq.social_app.Fragment.ThongtinFragment;
import ngocquy.nnq.social_app.Model.Follow;
import ngocquy.nnq.social_app.Model.Notification;
import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.R;
import ngocquy.nnq.social_app.databinding.UserRvSampleBinding;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder> {

    ArrayList<User> list;
    Context context;

    public UserAdapter(ArrayList<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_rv_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User user = list.get(position);
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.placeholder).into(holder.binding.profileImage);
        holder.binding.name.setText(user.getName());

        holder.binding.itemSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("UserID", user.getUserID());

                ThongtinFragment thongtinFragment = new ThongtinFragment();
                thongtinFragment.setArguments(bundle);

                // Sử dụng FragmentTransaction để thay đổi Fragment trong cùng một Activity
                FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, thongtinFragment);
                transaction.addToBackStack(null); // Để quay lại Fragment trước đó nếu cần
                transaction.commit();
            }
        });

        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference()
                .child("User")
                .child(user.getUserID());
        currentUserRef.child("followers").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.binding.followBtn.setText("Đã theo dõi");
                    holder.binding.followBtn.setTextColor(context.getColor(R.color.silver));
                    holder.binding.followBtn.setBackground(context.getResources().getDrawable(R.drawable.rectangle_unfollow));
                } else {
                    holder.binding.followBtn.setText("Theo dõi");
                    holder.binding.followBtn.setTextColor(context.getColor(R.color.white));
                    holder.binding.followBtn.setBackground(context.getResources().getDrawable(R.drawable.rectangle_follow));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
            }
        });
        holder.binding.followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Follow follow = new Follow();
                follow.setFollowedBy(FirebaseAuth.getInstance().getUid());
                follow.setFollowedAt(new Date().getTime());

                // Kiểm tra xem người dùng đã theo dõi user hiện tại chưa
                currentUserRef.child("followers").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Bỏ theo dõi
                            currentUserRef.child("followers").child(FirebaseAuth.getInstance().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                            // Cập nhật số lượng người theo dõi
                                    currentUserRef.child("followerCount").setValue(user.getFollowerCount() - 1);
                                            holder.binding.followBtn.setText("Theo dõi");
                                            holder.binding.followBtn.setTextColor(context.getColor(R.color.white));
                                            holder.binding.followBtn.setBackground(context.getResources().getDrawable(R.drawable.rectangle_follow));
                                            Toast.makeText(context, "Bạn đã bỏ theo dõi " + user.getName(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Theo dõi
                            currentUserRef.child("followers").child(FirebaseAuth.getInstance().getUid()).setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                            // Cập nhật số lượng người theo dõi
                                    currentUserRef.child("followerCount").setValue(user.getFollowerCount() + 1);
                                            holder.binding.followBtn.setText("Đã theo dõi");
                                            holder.binding.followBtn.setTextColor(context.getColor(R.color.silver));
                                            holder.binding.followBtn.setBackground(context.getResources().getDrawable(R.drawable.rectangle_unfollow));
                                            Toast.makeText(context, "Bạn đã theo dõi " + user.getName(), Toast.LENGTH_SHORT).show();

                                    Notification notification = new Notification();
                                    notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                    notification.setNotificationAt(new Date().getTime());
                                    notification.setType("Theo dõi");
                                    notification.setPostID("");
                                    notification.setPostedBy("");
                                    FirebaseDatabase.getInstance().getReference().child("notification")
                                            .child(user.getUserID())
                                            .push().setValue(notification);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Xử lý lỗi
                    }
                });

            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        UserRvSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = UserRvSampleBinding.bind(itemView);
        }
    }
    public void filterList(ArrayList<User> filteredList) {
        list = filteredList;
        notifyDataSetChanged();
    }
}
