package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MypageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        // Firebase 인증 및 데이터베이스 초기화
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 현재 로그인한 사용자의 정보를 가져옵니다.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // 현재 로그인한 사용자의 이메일을 사용하여 데이터베이스에서 사용자 정보 조회
            String userEmail = currentUser.getEmail();
            mDatabase.child("Users").orderByChild("mail").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                            // 이메일 주소가 일치하는 사용자의 이름을 가져옵니다.
                            String name = userSnapshot.child("name").getValue(String.class);
                            TextView nameTextView = findViewById(R.id.name);
                            nameTextView.setText(name);
                            break; // 첫 번째 일치하는 사용자의 이름을 찾았으므로 루프를 종료합니다.
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 데이터베이스 오류 처리
                }
            });
        }

        // 학교 홈페이지로 이동하는 버튼
        Button btnSchoolPage = findViewById(R.id.btnSchoolPage);
        btnSchoolPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.inu.ac.kr/inu/index.do?epTicket=LOG"));
                startActivity(browserIntent);
            }
        });

        // 로그아웃 버튼
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그아웃 로직 (Firebase Authentication 로그아웃)
                mAuth.signOut();
                Intent intent = new Intent(MypageActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 비밀번호 재설정 페이지로 이동하는 버튼
        Button btnResetPassword = findViewById(R.id.btnResetPassword);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MypageActivity.this, PwResetActivity.class);
                startActivity(intent);
            }
        });
    }
}