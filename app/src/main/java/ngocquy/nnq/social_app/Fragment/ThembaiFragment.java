package ngocquy.nnq.social_app.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import java.util.Date;

import ngocquy.nnq.social_app.Model.Post;
import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.R;
import ngocquy.nnq.social_app.databinding.FragmentThemBinding;


public class ThembaiFragment extends Fragment {

    FragmentThemBinding binding;
    Uri uri;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    AlertDialog.Builder builder ;
    public ThembaiFragment() {
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
        database.getReference().child("User").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    Picasso.get().load(user.getProfile()).placeholder(R.drawable.placeholder).into(binding.profileImage);
                    binding.name.setText(user.getName());
                    binding.rule.setText("Công khai");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Đang tải bài viết ");
        builder.setMessage("Vui lòng chờ");
        ProgressBar progressBar = new ProgressBar(getContext());
        progressBar.setIndeterminate(true);
        builder.setCancelable(false);
        builder.setView(progressBar);

        // Inflate the layout for this fragment
        binding =  FragmentThemBinding.inflate(inflater, container, false);
        binding.postDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length()>0){
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.rectangle_follow));
                    binding.postBtn.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                    binding.postBtn.setEnabled(true);
                }else {
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.rectangle_unfollow));
                    binding.postBtn.setTextColor(ContextCompat.getColor(getContext(),R.color.silver));
                    binding.postBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,10);
            }
        });

        binding.postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = builder.create();
                dialog.show();
                final StorageReference reference = storage.getReference()
                        .child("post_image")
                        .child(FirebaseAuth.getInstance().getUid())
                        .child(new Date().getTime()+"");
                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Post post = new Post();
                                post.setPostImage(uri.toString());
                                post.setPostedBy(auth.getUid());
                                post.setPostDescription(binding.postDescription.getText().toString());
                                post.setPostedAt(new Date().getTime());
                                database.getReference()
                                        .child("posts").push().setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                dialog.dismiss();
                                                Toast.makeText(getContext(), "Đăng bài thành công", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == 10){
                if(data.getData()!=null){
                    uri = data.getData();
                    binding.postImage.setImageURI(uri);
                    binding.postImage.setVisibility(View.VISIBLE);

                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.rectangle_follow));
                    binding.postBtn.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                    binding.postBtn.setEnabled(true);
                }
            }
        }
    }
}