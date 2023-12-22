package ngocquy.nnq.social_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ngocquy.nnq.social_app.Model.ChatRoom;
import ngocquy.nnq.social_app.Model.Message;
import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.Model.UserStories;
import ngocquy.nnq.social_app.R;
import ngocquy.nnq.social_app.RoomChatActivity;
import ngocquy.nnq.social_app.databinding.ItemListchatSampleBinding;

public class ListChatAdapter extends RecyclerView.Adapter<ListChatAdapter.viewHolder>{
    ArrayList<ChatRoom> list;
    Context context;

    public ListChatAdapter(ArrayList<ChatRoom> list, Context context) {
        this.list = list;
        this.context = context;
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_listchat_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        ChatRoom chatRoom = list.get(position);
        if(chatRoom.getMessages().size() > 0){
            Message message = chatRoom.getMessages().get(chatRoom.getMessages().size()-1);
            String otherID = chatRoom.getRoomID().replace(FirebaseAuth.getInstance().getUid(), "");
            FirebaseDatabase.getInstance().getReference().child("User").child(otherID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        User user = snapshot.getValue(User.class);
                        holder.binding.name.setText(user.getName());

                        Picasso.get().load(user.getProfile()).placeholder(R.drawable.placeholder).into(holder.binding.profileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            holder.binding.time.setText(getRelativeTime(message.getMessAt()));
            if(message.getMessBy().equals(FirebaseAuth.getInstance().getUid())){
                holder.binding.chat.setText("Bạn: "+message.getDescription());
            }else {
                holder.binding.chat.setText(message.getDescription());
            }

        }

        holder.binding.itemRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RoomChatActivity.class);
                intent.putExtra("roomID",chatRoom.getRoomID());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        ItemListchatSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemListchatSampleBinding.bind(itemView);
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
