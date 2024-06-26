package com.example.myapplication;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notifications;

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        String notificationText = "새로운 댓글이 있습니다. \"" + notification.getCommentContent() + "\"";
        holder.notificationContent.setText(notificationText);

        // 타임스탬프를 한국 시간으로 변환하여 설정
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String formattedTimestamp = sdf.format(new Date(notification.getTimestamp()));
        holder.notificationTimestamp.setText(formattedTimestamp);

        // 읽음 여부에 따라 텍스트 색상 변경
        if (notification.isRead()) {
            holder.notificationContent.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
        } else {
            holder.notificationContent.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
        }

        holder.itemView.setOnClickListener(v -> {
            // Intent를 생성하여 DetailActivity를 시작하고 postId를 전달합니다.
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("postId", notification.getPostId());

            // 알림을 읽음 상태로 변경
            if (!notification.isRead()) {
                notification.setRead(true);
                notifyItemChanged(position);

                // 데이터베이스에서 isRead 상태를 업데이트
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(notification.getNotificationId());
                databaseReference.child("read").setValue(notification.isRead());
            }

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationContent;
        TextView notificationTimestamp; // 타임스탬프 텍스트뷰

        public NotificationViewHolder(View itemView) {
            super(itemView);
            notificationContent = itemView.findViewById(R.id.notificationContent);
            notificationTimestamp = itemView.findViewById(R.id.notificationTimestamp); // 텍스트뷰 초기화
        }
    }
}
