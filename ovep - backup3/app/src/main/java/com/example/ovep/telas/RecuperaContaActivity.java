package com.example.ovep.telas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ovep.databinding.TelaRecuperaContaBinding;
import com.example.ovep.repository.AuthRepository;

public class RecuperaContaActivity extends AppCompatActivity {

    private TelaRecuperaContaBinding binding;
    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TelaRecuperaContaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRecuperarSenha.setOnClickListener(v -> enviarEmailRecuperacao());
        binding.btnVoltarLogin.setOnClickListener(v -> voltarLogin());
    }

    private void enviarEmailRecuperacao() {
        ocultarTeclado();

        String email = binding.editTextEmailRecuperacao.getText().toString().trim().toLowerCase();

        if (email.isEmpty()) {
            mostrarToast("Digite seu e-mail");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarToast("E-mail inválido");
            return;
        }

        bloquearUI(true);

        authRepository.resetPassword(email, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                bloquearUI(false);
                binding.txtMensagemConfirmacao.setVisibility(View.VISIBLE);
                mostrarToast("E-mail de recuperação enviado com sucesso");
            }

            @Override
            public void onError(String mensagem) {
                bloquearUI(false);
                mostrarToast("Erro: " + mensagem);
            }
        });
    }

    private void bloquearUI(boolean bloquear) {
        binding.btnRecuperarSenha.setEnabled(!bloquear);
        binding.btnVoltarLogin.setEnabled(!bloquear);
    }

    private void voltarLogin() {
        startActivity(new Intent(this, LoginActivity.class));
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
