package ngocquy.nnq.social_app.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import ngocquy.nnq.social_app.Adapter.NotificationAdapter;
import ngocquy.nnq.social_app.Model.Notification;
import ngocquy.nnq.social_app.R;
import ngocquy.nnq.social_app.databinding.FragmentThongbao2Binding;


public class Thongbao2Fragment extends Fragment {

    FragmentThongbao2Binding binding;
    ArrayList<Notification> list;
    FirebaseDatabase database;
    public Thongbao2Fragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentThongbao2Binding.inflate(inflater, container, false);
        list = new ArrayList<>();
        //list.add(new Notification(R.drawable.user,"<b>Bình Báo</b> vừa mới bình luận : Tuyệt vời vào bài đăng của bạn","2p trước"));


        NotificationAdapter adapter = new NotificationAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.notification2RV.setLayoutManager(linearLayoutManager);
        binding.notification2RV.setAdapter(adapter);

        database.getReference().child("notification")
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Notification notification = dataSnapshot.getValue(Notification.class);
                            notification.setNotificationID(dataSnapshot.getKey());
                            list.add(notification);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        return  binding.getRoot();
    }
}