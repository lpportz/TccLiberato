package com.example.ovep.telas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ovep.databinding.TelaLoginBinding;
import com.example.ovep.repository.AuthRepository;
import com.example.ovep.telas.valitador.Validator;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TelaLoginBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TelaLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        configurarListeners();
    }

    private void configurarListeners() {
        binding.btnLogin.setOnClickListener(v -> login());
        binding.btnCreateCount.setOnClickListener(v -> abrirCadastro());
        binding.txtEsqueceuSenha.setOnClickListener(v -> abrirRecuperaConta());
    }

    private void login() {
        ocultarTeclado();

        String email = binding.editTextUsuarioemail.getText().toString().trim().toLowerCase();
        String senha = binding.editTextSenha.getText().toString().trim();

        if (!Validator.validarEmailSenha(this, email, senha)) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        authRepository.signIn(email, senha, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String mensagem) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Erro: " + mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bloquearUI(boolean bloquear) {
        binding.progressBar.setVisibility(bloquear ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!bloquear);
        binding.btnCreateCount.setEnabled(!bloquear);
        binding.txtEsqueceuSenha.setEnabled(!bloquear);
    }

    private void abrirCadastro() {
        startActivity(new Intent(this, CadastroActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void abrirRecuperaConta() {
        startActivity(new Intent(this, RecuperaContaActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void abrirMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void mostrarToast(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }
}
