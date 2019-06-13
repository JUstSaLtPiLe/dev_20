package com.example.dev20_2.activity;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.dev20_2.MainActivity;
import com.example.dev20_2.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


// khi mở app sẽ vào đây đầu tiên, như đã khai báo trong AndroidManifest
public class LoginActivity extends AppCompatActivity {
    // initialize const & var
    private static final String TAG = "LoginActivity";
    private static final int LOGIN_PERMISSION = 1000;
    private DatabaseReference mDatabase;
    Button btnSignIn;

    // khi chạy ứng dụng thì bắt đầu ở đây
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // dùng view tên là activity_main
        setContentView(R.layout.activity_login);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // tìm nút btnSignIn ở trong view
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        // khi bấm nút btnSignIn thì làm gì
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // khởi tạo activity đăng nhập được tài trợ bởi Firebase authentication
                // activity khởi tạo sẽ cho result, xem ở func onActivityResult dưới
                startActivityForResult(
                        AuthUI.getInstance().createSignInIntentBuilder()
                        .enableAnonymousUsersAutoUpgrade().build(), LOGIN_PERMISSION
                );
            }
        });

        // login thành công thì làm gì
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( LoginActivity.this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                // tạo token cho người dùng
                // token này chưa dùng để làm gì cả
                String newToken = instanceIdResult.getToken();
                Log.e("newToken",newToken);

            }
        });



    }

    // activity login đưa ra result xong sẽ vào đây
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == LOGIN_PERMISSION){ //nếu được phép login
            startNewActivity(resultCode, data); // xuống hàm startNewActivity dưới
        }
    }

    private void startNewActivity(int resultCode, Intent data) {
        if (resultCode == RESULT_OK){ // nếu start activity login ko có lỗi gì
            // chuyển sang activity có tên MarkerActivity
            // vào trang chủ có hiện bản đồ, vị trí hiện tại và có thể gửi thông tin vị trí lên server
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else { // khi login thất bại
            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
        }
    }

}
