package com.example.ovep.telas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ovep.R;

public class TelaErroConexaoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_erro_conexao);

        Button btnTentarNovamente = findViewById(R.id.btnTentarNovamente);
        Button btnVoltar = findViewById(R.id.btnVoltar);

        btnTentarNovamente.setOnClickListener(v -> {
            // Relaunch main or current activity or faça o retry da operação
            finish();  // fecha essa tela e volta para a anterior
        });

        btnVoltar.setOnClickListener(v -> {
            // Voltar para tela inicial, por exemplo
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}

