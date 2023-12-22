package ngocquy.nnq.social_app.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import ngocquy.nnq.social_app.Adapter.ViewpagerAdapter;
import ngocquy.nnq.social_app.R;


public class ThongbaoFragment extends Fragment {

    ViewPager2 viewPager;
    TabLayout tabLayout;
    ViewpagerAdapter viewpagerAdapter;
    public ThongbaoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_thongbao, container, false);

       viewPager =view.findViewById(R.id.viewpager);
       tabLayout = view.findViewById(R.id.tabLayout);
       viewpagerAdapter = new ViewpagerAdapter(getActivity());
       viewPager.setAdapter(viewpagerAdapter);
       new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
           @Override
           public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
               switch (position){
                   case 0:tab.setText("Thông báo");
                   break;
                   case 1:tab.setText("Yêu cầu");
                   break;
               }
           }
       }).attach();
       return view;
    }
}