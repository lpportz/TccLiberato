package com.example.ovep.repository;

import com.google.firebase.auth.FirebaseAuth;

public class AuthRepository {

    private final FirebaseAuth mAuth;

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
    }

    // Callback para operações de autenticação
    public interface AuthCallback {
        void onSuccess();
        void onError(String mensagem);
    }

    // Login
    public void signIn(String email, String senha, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        String erro = task.getException() != null ? task.getException().getMessage() : "Erro ao logar";
                        callback.onError(erro);
                    }
                });
    }

    // Cadastro
    public void signUp(String email, String senha, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        String erro = task.getException() != null ? task.getException().getMessage() : "Erro ao cadastrar";
                        callback.onError(erro);
                    }
                });
    }

    // Recuperação de senha
    public void resetPassword(String email, AuthCallback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        String erro = task.getException() != null ? task.getException().getMessage() : "Erro ao enviar e-mail de recuperação";
                        callback.onError(erro);
                    }
                });
    }

    // Logout
    public void signOut() {
        mAuth.signOut();
    }
    public String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }
}
