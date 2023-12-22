package ngocquy.nnq.social_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ngocquy.nnq.social_app.Adapter.MessChatAdapter;
import ngocquy.nnq.social_app.Model.Message;
import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.databinding.ActivityRoomChatBinding;

public class RoomChatActivity extends AppCompatActivity {
    ActivityRoomChatBinding binding;
    FirebaseDatabase database;
    ArrayList<Message> list;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        list = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(RoomChatActivity.this);
        MessChatAdapter adapter = new MessChatAdapter(list,RoomChatActivity.this);
        binding.messRV.setLayoutManager(layoutManager);
        binding.messRV.setAdapter(adapter);



        String roomID = getIntent().getStringExtra("roomID");
        if(roomID != null && !roomID.isEmpty()){
            database.getReference().child("chatRooms").child(roomID).child("messages").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Message message = dataSnapshot.getValue(Message.class);
                        list.add(message);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        String otherID = roomID.replace(auth.getUid(), "");
        database.getReference().child("User").child(otherID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    if(user.isStatus()){
                        binding.time.setText("Đang hoạt động");
                    }else {
                        binding.time.setText("Hoạt động " + getRelativeTime(user.getLasttimeOnline()));
                    }
                    Picasso.get().load(user.getProfile()).placeholder(R.drawable.placeholder).into(binding.profileImage);
                    binding.name.setText(user.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.editTextMess.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length()>0){
                    binding.messPostbtn.setBackgroundDrawable(ContextCompat.getDrawable(RoomChatActivity.this,R.drawable.ic_send));
                    binding.messPostbtn.setEnabled(true);
                }else {
                    binding.messPostbtn.setBackgroundDrawable(ContextCompat.getDrawable(RoomChatActivity.this,R.drawable.ic_checksend));
                    binding.messPostbtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.messPostbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message();
                message.setDescription(binding.editTextMess.getText().toString());
                message.setMessAt(new Date().getTime());
                message.setMessBy(auth.getUid());


                database.getReference().child("chatRooms").child(roomID).child("messages")
                        .push().setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        binding.editTextMess.setText("");
                    }
                });
            }
        });

    }
    private String getRelativeTime(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - timestamp;

       if (timeDifference < 3600000) { // Less than 1 hour
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