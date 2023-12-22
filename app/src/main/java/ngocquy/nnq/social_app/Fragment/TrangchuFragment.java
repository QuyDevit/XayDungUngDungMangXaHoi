package ngocquy.nnq.social_app.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import ngocquy.nnq.social_app.Adapter.PostAdapter;
import ngocquy.nnq.social_app.Adapter.StoryAdapter;
import ngocquy.nnq.social_app.ChatMainActivity;
import ngocquy.nnq.social_app.Model.Post;
import ngocquy.nnq.social_app.Model.Story;
import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.Model.UserStories;
import ngocquy.nnq.social_app.R;
import ngocquy.nnq.social_app.RoomChatActivity;
import ngocquy.nnq.social_app.databinding.FragmentTrangchuBinding;


public class TrangchuFragment extends Fragment {
    FragmentTrangchuBinding binding;
    ArrayList<Story> list;
    ArrayList<Post> postList;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;
    AlertDialog.Builder builder ;
    AlertDialog progressDialog;
    boolean isImageSelected = false;

    ActivityResultLauncher<String> galleryLauncher;
    public TrangchuFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTrangchuBinding.inflate(inflater, container, false);

        builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Đang tải tin ");
        builder.setMessage("Vui lòng chờ");
        ProgressBar progressBar = new ProgressBar(getContext());
        progressBar.setIndeterminate(true);
        builder.setCancelable(false);
        builder.setView(progressBar);

        progressDialog = builder.create();
        progressDialog.setCancelable(false);

        database.getReference().child("User").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    Picasso.get().load(user.getProfile()).placeholder(R.drawable.placeholder).into(binding.profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // Inflate the layout for this fragment

        list = new ArrayList<>();


        StoryAdapter adapter = new StoryAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        binding.storyRV.setLayoutManager(linearLayoutManager);
        binding.storyRV.setNestedScrollingEnabled(false);
        binding.storyRV.setAdapter(adapter);

        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    list.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Story story = new Story();
                        story.setStoryBy(dataSnapshot.getKey());
                        story.setSotryAt(dataSnapshot.child("postedBy").getValue(Long.class));

                        ArrayList<UserStories> stories = new ArrayList<>();
                        for(DataSnapshot dataSnapshot1 : dataSnapshot.child("userStories").getChildren()){
                            UserStories userStories = dataSnapshot1.getValue(UserStories.class);
                            stories.add(userStories);
                        }
                        story.setStories(stories);
                        list.add(story);
                    }
                    Collections.reverse(list);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postList = new ArrayList<>();

        PostAdapter postAdapter = new PostAdapter(postList,getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.dashboardRV.setLayoutManager(layoutManager);
        binding.dashboardRV.setHasFixedSize(true);
        binding.dashboardRV.setNestedScrollingEnabled(false);
        binding.dashboardRV.setAdapter(postAdapter);

        database.getReference().child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Post post = dataSnapshot.getValue(Post.class);
                    post.setPostId(dataSnapshot.getKey());
                    postList.add(post);
                }
                // Lấy thứ tự ngược lại của danh sách
                Collections.reverse(postList);
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.messchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChatMainActivity.class);
                startActivity(intent);
            }
        });

        binding.story.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryLauncher.launch("image/*");
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if(result!= null){
                    isImageSelected = true;
                    binding.story.setImageURI(result);
                    progressDialog.show();
                    final StorageReference reference = storage.getReference()
                            .child("stories_image")
                            .child(auth.getUid()).child(new Date().getTime()+"");
                    reference.putFile(result).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Story story = new Story();
                                    story.setSotryAt(new Date().getTime());
                                    database.getReference().child("stories")
                                            .child(auth.getUid())
                                            .child("postedBy")
                                            .setValue(story.getSotryAt()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    UserStories userStories = new UserStories(uri.toString(),story.getSotryAt());

                                                    database.getReference().child("stories")
                                                            .child(auth.getUid())
                                                            .child("userStories")
                                                            .push()
                                                            .setValue(userStories);
                                                    progressDialog.dismiss();
                                                }
                                            });
                                }
                            });
                        }
                    });
                }
            }
        });
        // Xử lý sự kiện quay lại (back)
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isImageSelected) {
                    progressDialog.dismiss(); // Nếu ảnh đã được chọn, tắt progressDialog trước khi quay lại
                } else {
                    remove(); // Để quay lại
                }
            }
        });
        return binding.getRoot();
    }

}