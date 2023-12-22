package ngocquy.nnq.social_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import ngocquy.nnq.social_app.Model.User;
import ngocquy.nnq.social_app.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    FirebaseDatabase database;
    FirebaseAuth auth;
    ActivitySignupBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnAcLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.editaccountsignup.getText().toString(),
                        email = binding.editEmailsignup.getText().toString(),
                        pass = binding.editpasssignup.getText().toString(),
                        repass = binding.editrepass.getText().toString();
                // Kiểm tra xem mật khẩu có đủ mạnh hay không
                if (pass.length()<6) {
                    Toast.makeText(SignupActivity.this, "Mật khẩu cần ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!repass.equals(pass)) {
                    Toast.makeText(SignupActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                } else {
                    // Kiểm tra xem địa chỉ email đã tồn tại trong Firebase Realtime Database hay chưa
                    database.getReference("User")
                            .orderByChild("email")
                            .equalTo(email)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // Địa chỉ email đã tồn tại, xử lý tại đây
                                        Toast.makeText(SignupActivity.this, "Địa chỉ email đã tồn tại", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Địa chỉ email chưa tồn tại, tiến hành đăng ký bằng Firebase Authentication
                                        auth.createUserWithEmailAndPassword(email, pass)
                                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {
                                                            User users = new User();
                                                            users.setName(name);
                                                            users.setEmail(email);
                                                            users.setPass(pass);
                                                            String id = task.getResult().getUser().getUid();
                                                            database.getReference().child("User").child(id).setValue(users);
                                                            Toast.makeText(SignupActivity.this, "Đăng ký tài khoản thành công", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                                        } else {
                                                            // Xử lý lỗi khác
                                                            Toast.makeText(SignupActivity.this, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Xử lý lỗi khi kiểm tra cơ sở dữ liệu
                                    Toast.makeText(SignupActivity.this, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}