package ngocquy.nnq.social_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ngocquy.nnq.social_app.Model.User;

public class LoginActivity extends AppCompatActivity {
    TextView btnnextsignup;
    LinearLayout btnloginGG;
    AppCompatButton btnDangNhap;
    EditText account,pass;
    boolean checkpassVisible;
    FirebaseAuth auth;
    FirebaseDatabase database;
    GoogleSignInClient signInClient;
    FirebaseUser currentUser;
    GoogleSignInOptions gso;
    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        account = findViewById(R.id.editaccount);
        pass = findViewById(R.id.editpass);
        btnDangNhap = findViewById(R.id.btnLogin);
        loadingDialog = new LoadingDialog(LoginActivity.this);

        pass.setOnTouchListener(new View.OnTouchListener() { // Gán lắng nghe sự kiện chạm vào "pass".
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) { // Bắt đầu xử lý sự kiện chạm.
                final int Right = 2; // Khai báo một hằng số "Right" với giá trị 2, để xác định phía bên phải của "pass".

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) { // Kiểm tra nếu sự kiện chạm là "ACTION_UP" (khi người dùng nhấc ngón tay ra khỏi màn hình).
                    if (motionEvent.getRawX() >= pass.getRight() - pass.getCompoundDrawables()[Right].getBounds().width()) {
                        // Kiểm tra xem người dùng đã chạm vào vùng bên phải của "pass" (có biểu tượng) bằng cách so sánh tọa độ X tương đối (getRawX()).

                        int selection = pass.getSelectionEnd(); // Lưu trữ vị trí con trỏ hiện tại trong "pass".

                        if (checkpassVisible) { // Kiểm tra xem mật khẩu đang được hiển thị hay không.
                            pass.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_pass_off, 0);
                            // Đặt hình ảnh biểu tượng để ẩn mật khẩu và chuyển đổi "pass" để ẩn mật khẩu.
                            pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            checkpassVisible = false; // Đánh dấu rằng mật khẩu đang bị ẩn.
                        } else {
                            pass.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_pass_on, 0);
                            // Đặt hình ảnh biểu tượng để hiển thị mật khẩu và chuyển đổi "pass" để hiển thị mật khẩu.
                            pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            checkpassVisible = true; // Đánh dấu rằng mật khẩu đang được hiển thị.
                        }

                        pass.setSelection(selection); // Đặt lại vị trí con trỏ trong "pass" sau khi thay đổi.
                        return true; // Báo hiệu rằng sự kiện chạm đã được xử lý.
                    }
                }
                return false; // Báo hiệu rằng sự kiện chạm không được xử lý.
            }
        });
        btnnextsignup =  findViewById(R.id.btnAcSignup);
        btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = account.getText().toString(),
                        passw = pass.getText().toString();
                auth.signInWithEmailAndPassword(email,passw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Mật khẩu hoặc tài khoản không hợp lệ!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        btnnextsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
        btnloginGG = findViewById(R.id.btnloginGG);
        btnloginGG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        signInClient = GoogleSignIn.getClient(this,gso);
    }
    int RC_SIGN_IN = 40;
    private void  signIn(){
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent,RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            loadingDialog.startLoadingDialog();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account1 = task.getResult(ApiException.class);
                firebaseAuth(account1.getIdToken());
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            final String uid = user.getUid();
                            final DatabaseReference userReference = database.getReference().child("User").child(uid);
                            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()){
                                        User users = new User();
                                        users.setName(user.getDisplayName());
                                        users.setEmail(user.getEmail());
                                        users.setPass("");
                                        users.setStatus(true);
                                        userReference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    loadingDialog.dismissDialog();
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                } else {
                                                    // Handle database update failure
                                                    Toast.makeText(LoginActivity.this, "Lỗi", Toast.LENGTH_SHORT).show();
                                                    loadingDialog.dismissDialog();
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        userReference.child("status").setValue(true);
                                        loadingDialog.dismissDialog();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Xử lý lỗi khi truy cập cơ sở dữ liệu
                                    Toast.makeText(LoginActivity.this, "Lỗi truy cập cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                                    loadingDialog.dismissDialog();
                                }
                            });
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Lỗi", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser != null){
            database.getReference().child("User").child(auth.getCurrentUser().getUid()).child("status").setValue(true);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}