package ngocquy.nnq.social_app.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import ngocquy.nnq.social_app.Adapter.RequestAdapter;
import ngocquy.nnq.social_app.Model.Follow;
import ngocquy.nnq.social_app.Model.Friend;
import ngocquy.nnq.social_app.R;
import ngocquy.nnq.social_app.databinding.FragmentYeucauBinding;

public class YeucauFragment extends Fragment {

    FragmentYeucauBinding binding;
    ArrayList<Friend> list;
    FirebaseAuth auth;
    FirebaseDatabase database;
    public YeucauFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        list = new ArrayList<>();
        binding = FragmentYeucauBinding.inflate(inflater,container,false);

        RequestAdapter adapter = new RequestAdapter(list,getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.requestRV.setLayoutManager(layoutManager);
        binding.requestRV.setAdapter(adapter);

        database.getReference()
                .child("User")
                .child(auth.getUid())
                .child("friends").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Friend friend = dataSnapshot.getValue(Friend.class);
                            friend.setUserID(dataSnapshot.getKey());
                            if(!friend.isAccept())
                            {
                                list.add(friend);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Inflate the layout for this fragment
        return binding.getRoot();
    }
}