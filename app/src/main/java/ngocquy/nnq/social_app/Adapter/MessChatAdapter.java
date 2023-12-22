package ngocquy.nnq.social_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.ktx.Firebase;

import java.util.ArrayList;

import ngocquy.nnq.social_app.Model.Message;
import ngocquy.nnq.social_app.R;
import ngocquy.nnq.social_app.databinding.ItemEmptyViewBinding;
import ngocquy.nnq.social_app.databinding.ItemMessLeftSampleBinding;
import ngocquy.nnq.social_app.databinding.ItemMessRightSampleBinding;

public class MessChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENDER = 1;
    private static final int VIEW_TYPE_RECEIVER = 2;
    private static final int VIEW_TYPE_EMPTY = 3;
    ArrayList<Message> list;
    Context context;

    public MessChatAdapter(ArrayList<Message> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENDER) {
            view = LayoutInflater.from(context).inflate(R.layout.item_mess_right_sample, parent, false);
            return new viewHolerSender(view);
        } else if (viewType == VIEW_TYPE_RECEIVER) {
            view = LayoutInflater.from(context).inflate(R.layout.item_mess_left_sample, parent, false);
            return new viewHolerReceiver(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_empty_view, parent, false);
            return new viewHolerEmpty(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = list.get(position);
        if (holder instanceof viewHolerSender && message.getMessBy().equals(FirebaseAuth.getInstance().getUid())) {
            viewHolerSender senderHolder = (viewHolerSender) holder;
            senderHolder.binding2.messright.setText(message.getDescription());
            senderHolder.binding2.timemess.setText(getRelativeTime(message.getMessAt()));
        } else if (holder instanceof viewHolerReceiver && !message.getMessBy().equals(FirebaseAuth.getInstance().getUid())) {
            viewHolerReceiver receiverHolder = (viewHolerReceiver) holder;
            receiverHolder.binding.messleft.setText(message.getDescription());
            receiverHolder.binding.timemess.setText(getRelativeTime(message.getMessAt()));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (list.isEmpty()) {
            return VIEW_TYPE_EMPTY;
        } else {
            return list.get(position).getMessBy().equals(FirebaseAuth.getInstance().getUid()) ? VIEW_TYPE_SENDER : VIEW_TYPE_RECEIVER;
        }
    }

    public class viewHolerSender extends RecyclerView.ViewHolder {
        ItemMessRightSampleBinding binding2;

        public viewHolerSender(@NonNull View itemView) {
            super(itemView);
            binding2 = ItemMessRightSampleBinding.bind(itemView);
        }
    }

    public class viewHolerReceiver extends RecyclerView.ViewHolder {
        ItemMessLeftSampleBinding binding;

        public viewHolerReceiver(@NonNull View itemView) {
            super(itemView);
            binding = ItemMessLeftSampleBinding.bind(itemView);
        }
    }

    public class viewHolerEmpty extends RecyclerView.ViewHolder {
        ItemEmptyViewBinding binding3;

        public viewHolerEmpty(@NonNull View itemView) {
            super(itemView);
            binding3 = ItemEmptyViewBinding.bind(itemView);
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
