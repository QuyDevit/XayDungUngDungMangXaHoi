package ngocquy.nnq.social_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ngocquy.nnq.social_app.Adapter.ListChatAdapter;
import ngocquy.nnq.social_app.Adapter.ListFriendCanChatApdapter;
import ngocquy.nnq.social_app.Model.ChatRoom;
import ngocquy.nnq.social_app.Model.Friend;
import ngocquy.nnq.social_app.Model.Message;
import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.Model.UserStories;
import ngocquy.nnq.social_app.databinding.ActivityChatMainBinding;

public class ChatMainActivity extends AppCompatActivity {
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Friend> list;
    ArrayList<ChatRoom> listRoom;
    ActivityChatMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database =FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();


        database.getReference().child("User")
                .child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Picasso.get().load(user.getProfile()).placeholder(R.drawable.placeholder).into(binding.profileImage);
                        binding.userName.setText(user.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        list = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(ChatMainActivity.this,LinearLayoutManager.HORIZONTAL,false);
        ListFriendCanChatApdapter adapter = new ListFriendCanChatApdapter(list,ChatMainActivity.this);
        binding.friendRV.setLayoutManager(layoutManager);
        binding.friendRV.setAdapter(adapter);

        database.getReference().child("User")
                .child(auth.getUid())
                .child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot :snapshot.getChildren()){
                    Friend friend = dataSnapshot.getValue(Friend.class);
                    friend.setUserID(dataSnapshot.getKey());

                    list.add(friend);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        listRoom = new ArrayList<>();
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(ChatMainActivity.this);
        ListChatAdapter adapter1 = new ListChatAdapter(listRoom,ChatMainActivity.this);
        binding.chatRV.setLayoutManager(layoutManager1);
        binding.chatRV.setAdapter(adapter1);
        database.getReference().child("chatRooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listRoom.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ChatRoom chatRoom = new ChatRoom();
                    chatRoom.setRoomID(dataSnapshot.getKey());

                    ArrayList<Message> listmess = new ArrayList<>();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.child("messages").getChildren()){
                        Message message = dataSnapshot1.getValue(Message.class);
                        listmess.add(message);
                    }
                    chatRoom.setMessages(listmess);

                    database.getReference().child("chatRooms").child(dataSnapshot.getKey())
                            .child("members").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                listRoom.add(chatRoom);
                                adapter1.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                adapter1.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseDatabase.getInstance().getReference().child("User").child(auth.getCurrentUser().getUid()).child("status").setValue(false);
    }
}