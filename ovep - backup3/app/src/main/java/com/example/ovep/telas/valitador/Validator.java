package com.example.ovep.telas.valitador;

import android.content.Context;
import android.util.Patterns;
import android.widget.Toast;

public class Validator {
    public static boolean validarEmailSenha(Context context, String email, String senha) {
        if (email.isEmpty()) {
            Toast.makeText(context, "Digite seu e-mail", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "E-mail inválido", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (senha.isEmpty()) {
            Toast.makeText(context, "Digite sua senha", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
