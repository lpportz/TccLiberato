package com.example.ovep.telas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ovep.R;

public class TelaSemConteudoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_sem_conteudo);

        // Referências aos elementos de UI
        TextView textMensagem = findViewById(R.id.textMensagemVazio);
        Button btnAcao = findViewById(R.id.btnAcaoVazio);

        // Recupera a mensagem e o destino (caso venha)
        String mensagem = getIntent().getStringExtra("mensagem");
        String destino = getIntent().getStringExtra("destino");

        // Define a mensagem padrão ou personalizada
        if (mensagem != null && !mensagem.isEmpty()) {
            textMensagem.setText(mensagem);
        } else {
            textMensagem.setText("Nenhum item encontrado.");
        }

        // Define ação do botão
        btnAcao.setOnClickListener(v -> {
            if (destino != null && !destino.isEmpty()) {
                try {
                    // Navega para a classe de destino se enviada via intent
                    Class<?> destinoClass = Class.forName(destino);
                    startActivity(new Intent(this, destinoClass));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    finish(); // Volta se o destino não for válido
                }
            } else {
                // Se não tiver destino, abre a MainActivity explicitamente para evitar voltar para a anterior
                Intent intent = new Intent(this, MainActivity.class);
                // Remove outras activities da pilha para evitar voltar a tela anterior
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

    }
}
