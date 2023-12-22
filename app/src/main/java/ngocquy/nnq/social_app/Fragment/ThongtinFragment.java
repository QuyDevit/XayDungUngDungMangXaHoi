package ngocquy.nnq.social_app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ngocquy.nnq.social_app.Adapter.FollowersAdapter;
import ngocquy.nnq.social_app.Model.Follow;
import ngocquy.nnq.social_app.Model.Friend;
import ngocquy.nnq.social_app.Model.Notification;
import ngocquy.nnq.social_app.Model.Post;
import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.R;
import ngocquy.nnq.social_app.RoomChatActivity;
import ngocquy.nnq.social_app.databinding.FragmentThongtinBinding;

public class ThongtinFragment extends Fragment {
    FragmentThongtinBinding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    ArrayList<Follow> list;
    String UserID;
    public ThongtinFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        list = new ArrayList<>();
        binding = FragmentThongtinBinding.inflate(inflater, container, false);


        FollowersAdapter adapter = new FollowersAdapter(list,getContext());
        LinearLayoutManager manager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        binding.friendRV.setLayoutManager(manager);
        binding.friendRV.setAdapter(adapter);

        Bundle args = getArguments();
        if (args != null) {
            binding.follow.setEnabled(true);
            binding.addfriend.setEnabled(true);
            binding.mess.setEnabled(true);
            binding.verifiledAccount.setEnabled(false);
            binding.btnEditName.setVisibility(View.GONE);
            binding.changeCoverPhoto.setVisibility(View.GONE);
            UserID = args.getString("UserID");

            binding.mess.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String checkroom;
                    int result = auth.getUid().compareTo(UserID);
                    if(result < 0){
                        checkroom = auth.getUid()+UserID;
                    }else {
                        checkroom = UserID + auth.getUid();
                    }
                    database.getReference().child("chatRooms").child(checkroom).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Intent intent = new Intent(getActivity(),RoomChatActivity.class);
                                intent.putExtra("roomID",checkroom);
                                startActivity(intent);
                            }
                            else {
                                Map<String,Boolean> member = new HashMap<>();
                                member.put(auth.getUid(), true);
                                member.put(UserID, true);
                                database.getReference().child("chatRooms").child(checkroom)
                                        .child("members").setValue(member).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Intent intent = new Intent(getActivity(),RoomChatActivity.class);
                                        intent.putExtra("roomID",checkroom);
                                        startActivity(intent);
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

            database.getReference().child("posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int countpost = 0;
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Post post = dataSnapshot.getValue(Post.class);
                        if(post.getPostedBy().equals(UserID)){
                            countpost += 1;
                        }
                    }
                    binding.txtCountpost.setText(String.valueOf(countpost));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            database.getReference().child("User")
                    .child(UserID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                User user = snapshot.getValue(User.class);
                                Picasso.get().load(user.getCoverPhoto()).placeholder(R.drawable.placeholder).into(binding.coverPhoto);

                                Picasso.get().load(user.getProfile()).placeholder(R.drawable.placeholder).into(binding.profileImage);
                                binding.userName.setText(user.getName());
                                //set lượt theo dõi
                                int followerCount = user.getFollowerCount();
                                binding.txtCountFollow.setText(String.valueOf(followerCount));
                                //set số lượng bạn bè
                                int friendCount = user.getFriendCount();
                                binding.txtCountFriend.setText(String.valueOf(friendCount));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            //cập nhập số lượng follow khi có sự thay đổi
            database.getReference().child("User").child(UserID).child("followerCount").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int followerCount = snapshot.getValue(Integer.class);
                        binding.txtCountFollow.setText(String.valueOf(followerCount));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors here
                }
            });
            //cập nhập số lượng friend khi có sự thay đổi
            database.getReference().child("User").child(UserID).child("friendCount").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int followerCount = snapshot.getValue(Integer.class);
                        binding.txtCountFriend.setText(String.valueOf(followerCount));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors here
                }
            });

            database.getReference()
                    .child("User")
                    .child(UserID)
                    .child("followers").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            list.clear();
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                Follow follow = dataSnapshot.getValue(Follow.class);
                                list.add(follow);
                            }
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference()
                    .child("User")
                    .child(UserID);
            //set nut theo doi
            currentUserRef.child("followers").child(FirebaseAuth.getInstance().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                    binding.txtFollow.setText("Đã Theo dõi");
                                    Drawable drawableLeft = getContext().getResources().getDrawable(R.drawable.ic_unfollow);
                                    binding.txtFollow.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
                                    binding.txtFollow.setTextColor(getContext().getColor(R.color.silver));
                                    binding.follow.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_unfollow));

                            } else {
                                binding.txtFollow.setText("Theo dõi");
                                binding.txtFollow.setTextColor(getContext().getColor(R.color.white));
                                binding.follow.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_follow));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Xử lý lỗi
                        }
                    });
            //set nut ban be
            currentUserRef.child("friends").child(FirebaseAuth.getInstance().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Friend friend = snapshot.getValue(Friend.class);
                            if (snapshot.exists()) {
                                if(friend.isAccept()){
                                    binding.txtAddfriend.setText("Hủy kết bạn");
                                    Drawable drawableLeft = getContext().getResources().getDrawable(R.drawable.ic_addfriend);
                                    binding.txtAddfriend.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
                                    binding.txtAddfriend.setTextColor(getContext().getColor(R.color.white));
                                    binding.addfriend.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_unfriend));
                                }
                                else {
                                    binding.txtAddfriend.setText("Đang chờ");
                                    Drawable drawableLeft = getContext().getResources().getDrawable(R.drawable.ic_addfriend);
                                    binding.txtAddfriend.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
                                    binding.txtAddfriend.setTextColor(getContext().getColor(R.color.white));
                                    binding.addfriend.setEnabled(false);
                                    binding.addfriend.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_confirmation));
                                }
                            } else {
                                binding.txtAddfriend.setText("Kết bạn");
                                binding.txtAddfriend.setTextColor(getContext().getColor(R.color.white));
                                binding.addfriend.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_addfriend));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Xử lý lỗi
                        }
                    });
            //Check follow click
            binding.follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Kiểm tra xem người dùng đã theo dõi user hiện tại chưa
                    Follow follow = new Follow();
                    follow.setFollowedBy(FirebaseAuth.getInstance().getUid());
                    follow.setFollowedAt(new Date().getTime());
                    database.getReference().child("User")
                            .child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        User user = snapshot.getValue(User.class);
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
                                                            binding.txtFollow.setText("Theo dõi");
                                                            binding.txtFollow.setTextColor(getContext().getColor(R.color.white));
                                                            binding.follow.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_follow));
                                                            Drawable drawableLeft = getContext().getResources().getDrawable(R.drawable.ic_follow);
                                                            binding.txtFollow.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
                                                            Toast.makeText(getContext(), "Bạn đã bỏ theo dõi " + user.getName(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else {
                                                    // Theo dõi
                                                    currentUserRef.child("followers").child(FirebaseAuth.getInstance().getUid()).setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            // Cập nhật số lượng người theo dõi
                                                            currentUserRef.child("followerCount").setValue(user.getFollowerCount() + 1);
                                                            binding.txtFollow.setText("Đã theo dõi");
                                                            binding.txtFollow.setTextColor(getContext().getColor(R.color.silver));
                                                            Drawable drawableLeft = getContext().getResources().getDrawable(R.drawable.ic_unfollow);
                                                            binding.txtFollow.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
                                                            binding.follow.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_unfollow));

                                                            Notification notification = new Notification();
                                                            notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                            notification.setNotificationAt(new Date().getTime());
                                                            notification.setType("Theo dõi");
                                                            notification.setPostID("");
                                                            notification.setPostedBy("");
                                                            FirebaseDatabase.getInstance().getReference().child("notification")
                                                                    .child(user.getUserID())
                                                                    .push().setValue(notification);
                                                            Toast.makeText(getContext(), "Bạn đã theo dõi " + user.getName(), Toast.LENGTH_SHORT).show();
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
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            });
            //check addfriend click
            binding.addfriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Friend friend = new Friend();
                    friend.setFriendBy(FirebaseAuth.getInstance().getUid());
                    friend.setAccept(false);
                    friend.setFriendAt(new Date().getTime());
                    database.getReference().child("User")
                            .child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        User user = snapshot.getValue(User.class);
                                        currentUserRef.child("friends").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Friend friend1 = snapshot.getValue(Friend.class);
                                                if (snapshot.exists()) {
                                                    if(friend1.isAccept()){
                                                        // Hủy kết bạn
                                                        currentUserRef.child("friends").child(FirebaseAuth.getInstance()
                                                                .getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                // Cập nhật số lượng người theo dõi
                                                                currentUserRef.child("friendCount").setValue(user.getFriendCount() - 1);

                                                                DatabaseReference userNow = FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getUid());
                                                                userNow.child("friendCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        int friendCount = 0;
                                                                        if(snapshot.exists()){
                                                                            friendCount = snapshot.getValue(Integer.class);
                                                                        }
                                                                        userNow.child("friendCount").setValue(friendCount - 1);

                                                                        userNow.child("friends").child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                Friend friend2 = snapshot.getValue(Friend.class);
                                                                                if(friend2.isAccept()){
                                                                                    userNow.child("friends").child(UserID).removeValue();
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                                            }
                                                                        });
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                                binding.txtAddfriend.setText("Kết bạn");
                                                                binding.txtAddfriend.setTextColor(getContext().getColor(R.color.white));
                                                                binding.addfriend.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_addfriend));
                                                                Toast.makeText(getContext(), "Bạn đã hủy kết bạn với " + user.getName(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    // Thêm bạn
                                                    currentUserRef.child("friends").child(FirebaseAuth.getInstance()
                                                            .getUid()).setValue(friend).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            binding.txtAddfriend.setText("Đang chờ");
                                                            binding.txtAddfriend.setTextColor(getContext().getColor(R.color.white));
                                                            Drawable drawableLeft = getContext().getResources().getDrawable(R.drawable.ic_addfriend);
                                                            binding.txtAddfriend.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
                                                            binding.addfriend.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_confirmation));
                                                            Toast.makeText(getContext(), "Bạn đã gửi yêu cầu kết bạn với " + user.getName(), Toast.LENGTH_SHORT).show();
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
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            });
        }
        else {
            binding.follow.setVisibility(View.GONE);
            binding.addfriend.setVisibility(View.GONE);
            binding.mess.setVisibility(View.GONE);
            binding.txtFollow.setVisibility(View.GONE);
            binding.txtAddfriend.setVisibility(View.GONE);
            binding.txtMess.setVisibility(View.GONE);
            binding.verifiledAccount.setEnabled(true);
            binding.btnEditName.setVisibility(View.VISIBLE);
            binding.changeCoverPhoto.setVisibility(View.VISIBLE);

            database.getReference().child("posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int countpost = 0;
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Post post = dataSnapshot.getValue(Post.class);
                        if(post.getPostedBy().equals(FirebaseAuth.getInstance().getUid())){
                            countpost += 1;
                        }
                    }
                    binding.txtCountpost.setText(String.valueOf(countpost));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            binding.btnEditName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_edit_name, null);
                    dialogBuilder.setView(dialogView);

                    EditText editTextNewName = dialogView.findViewById(R.id.editTextNewName);
                    Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
                    Button buttonSave = dialogView.findViewById(R.id.buttonSave);

                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();

                    buttonCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    buttonSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String newName = editTextNewName.getText().toString();
                            FirebaseDatabase.getInstance().getReference().child("User")
                                    .child(FirebaseAuth.getInstance().getUid()).child("name")
                                    .setValue(newName).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            dialog.dismiss();
                                            binding.userName.setText(newName);
                                            Toast.makeText(getContext(), "Đổi tên thành công", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    });
                }
            });
            //cập nhập số lượng follow khi có sự thay đổi
            database.getReference().child("User").child(auth.getUid()).child("followerCount").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int followerCount = snapshot.getValue(Integer.class);
                        binding.txtCountFollow.setText(String.valueOf(followerCount));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors here
                }
            });
            //cập nhập số lượng friend khi có sự thay đổi
            database.getReference().child("User").child(auth.getUid()).child("friendCount").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int followerCount = snapshot.getValue(Integer.class);
                        binding.txtCountFriend.setText(String.valueOf(followerCount));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors here
                }
            });
            database.getReference().child("User")
                    .child(auth.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                User user = snapshot.getValue(User.class);
                                Picasso.get().load(user.getCoverPhoto()).placeholder(R.drawable.placeholder).into(binding.coverPhoto);

                                Picasso.get().load(user.getProfile()).placeholder(R.drawable.placeholder).into(binding.profileImage);
                                //set lượt theo dõi
                                binding.userName.setText(user.getName());
                                int followerCount = user.getFollowerCount();
                                binding.txtCountFollow.setText(String.valueOf(followerCount));
                                //set số lượng bạn bè
                                int friendCount = user.getFriendCount();
                                binding.txtCountFriend.setText(String.valueOf(friendCount));

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            database.getReference()
                    .child("User")
                    .child(auth.getUid())
                    .child("followers").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            list.clear();
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                Follow follow = dataSnapshot.getValue(Follow.class);
                                list.add(follow);
                            }
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }




        binding.changeCoverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,11);
            }
        });
        binding.verifiledAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,22);
            }
        });


        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == 11){
                if(data.getData()!=null){
                    Uri uri = data.getData();
                    binding.coverPhoto.setImageURI(uri);

                    final StorageReference reference = storage.getReference().child("cover_photo").child(FirebaseAuth.getInstance().getUid());
                    reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getContext(), "Đổi ảnh bìa thành công", Toast.LENGTH_SHORT).show();
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    database.getReference().child("User").child(auth.getUid()).child("coverPhoto").setValue(uri.toString());
                                }
                            });
                        }
                    });
                }
            }else {
                if(data.getData()!=null){
                    Uri uri = data.getData();
                    binding.profileImage.setImageURI(uri);

                    final StorageReference reference = storage.getReference().child("profile_image").child(FirebaseAuth.getInstance().getUid());
                    reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getContext(), "Đổi ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    database.getReference().child("User").child(auth.getUid()).child("profile").setValue(uri.toString());
                                }
                            });
                        }
                    });
                }
            }
        }
    }
}