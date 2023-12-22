package ngocquy.nnq.social_app.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;


import ngocquy.nnq.social_app.Fragment.Thongbao2Fragment;
import ngocquy.nnq.social_app.Fragment.YeucauFragment;

public class ViewpagerAdapter extends FragmentStateAdapter {


    public ViewpagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:return new Thongbao2Fragment();
            case 1:return new YeucauFragment();
            default:return new Thongbao2Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
