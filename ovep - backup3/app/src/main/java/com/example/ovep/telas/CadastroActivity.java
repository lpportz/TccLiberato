package com.example.ovep.telas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ovep.databinding.TelaCadastroBinding;
import com.example.ovep.models.Usuario;
import com.example.ovep.repository.AuthRepository;
import com.example.ovep.telas.valitador.Validator;
import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroActivity extends AppCompatActivity {

    private TelaCadastroBinding binding;
    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TelaCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnCreateCount.setOnClickListener(v -> validarDados());
    }

    private void validarDados() {
        ocultarTeclado();

        String email = binding.editTextUsuarioemail.getText().toString().trim().toLowerCase();
        String nome = binding.editTextUsuarioNome.getText().toString().trim();
        String senha = binding.editTextSenha.getText().toString().trim();

        if (!Validator.validarEmailSenha(this, email, senha)) return;
        if (nome.isEmpty()) {
            mostrarToast("Preencha seu nome");
            return;
        }

        bloquearUI(true);
        criarContaFirebase(email, senha, nome);
    }

    private void criarContaFirebase(String email, String senha, String nome) {
        authRepository.signUp(email, senha, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                String uid = authRepository.getCurrentUserId();
                if (uid != null) {
                    Usuario usuario = new Usuario(uid, nome, email);
                    FirebaseFirestore.getInstance()
                            .collection("usuarios")
                            .document(uid)
                            .set(usuario)
                            .addOnSuccessListener(unused -> {
                                mostrarToast("Conta criada com sucesso!");
                                startActivity(new Intent(CadastroActivity.this, CadastroRealizadoComSucesso.class));
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                mostrarToast("Erro ao salvar dados: " + e.getMessage());
                                bloquearUI(false);
                            });
                } else {
                    mostrarToast("Usuário não autenticado");
                    bloquearUI(false);
                }
            }

            @Override
            public void onError(String mensagem) {
                mostrarToast("Falha ao criar conta: " + mensagem);
                bloquearUI(false);
            }
        });
    }

    private void bloquearUI(boolean bloquear) {
        binding.progressBar.setVisibility(bloquear ? View.VISIBLE : View.GONE);
        binding.btnCreateCount.setEnabled(!bloquear);
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
