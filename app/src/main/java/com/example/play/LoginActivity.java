package com.example.play;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.AuthCredential;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001; // Request code для Google Sign-In

    private FirebaseAuth mAuth;  // Firebase Authentication
    private Button googleSignInButton; // Кнопка для входа через Google
    private Button loginButton; // Кнопка для входа через email/password
    private EditText emailEditText, passwordEditText; // Поля для ввода email и пароля
    private TextView signUpTextView; // Текст для перехода на экран регистрации

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Устанавливаем layout

        // Инициализируем FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Находим кнопки и текстовые поля
        googleSignInButton = findViewById(R.id.googleSignInButton);
        loginButton = findViewById(R.id.loginButton);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpTextView = findViewById(R.id.signupTextView);

        // Устанавливаем обработчик для кнопки входа через Google
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        // Устанавливаем обработчик для кнопки входа через email/password
        loginButton.setOnClickListener(v -> loginWithEmailAndPassword());

        // Устанавливаем обработчик для перехода на экран регистрации
        signUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Проверим, если пользователь уже авторизован в Firebase, то сразу переходим в MainActivity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Если пользователь уже авторизован, сразу переходим в MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // Метод для входа через email и пароль
    private void loginWithEmailAndPassword() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Переход на MainActivity после успешного входа через email/password
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка авторизации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Метод для запуска Google Sign-In
    private void signInWithGoogle() {
        // Настроим GoogleSignInOptions с необходимыми параметрами
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("926841970234-vs5ebuk0b9ggj3c2fj341uo9jlj9r9t9.apps.googleusercontent.com") // ID клиента для Web из Firebase
                .requestEmail()
                .build();

        // Создаем клиент для входа через Google
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Создаем интент для входа через Google
        Intent signInIntent = googleSignInClient.getSignInIntent();

        // Запускаем окно входа через Google
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Получение результата авторизации
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account); // Авторизация с помощью полученного Google аккаунта
            } catch (ApiException e) {
                Toast.makeText(this, "Ошибка входа через Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Авторизация через Firebase с использованием Google аккаунта
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // Получаем учётные данные (ID токен) из Google
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        // Используем Firebase для аутентификации
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser(); // Получаем пользователя
                        if (user != null) {
                            // Получаем UID и информацию о пользователе
                            String userId = user.getUid();
                            String name = user.getDisplayName();
                            String email = user.getEmail();

                            // Сохраняем пользователя в Firebase Realtime Database
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(userId)
                                    .setValue(new User(name, email))
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Авторизация успешна", Toast.LENGTH_SHORT).show();
                                            // После успешного входа переходим в MainActivity
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
