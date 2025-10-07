package com.example.ovep.telas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ovep.R;
import com.example.ovep.adapters.EmpresaAdapter;
import com.example.ovep.models.Empresa;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FavoritosActivity extends AppCompatActivity {

    private EmpresaAdapter adapter;
    private final List<Empresa> listaFavoritos = new ArrayList<>();

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout layoutVazio;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_favoritos);

        inicializarComponentes();
        configurarRecyclerView();
        configurarBottomNavigation();
        carregarFavoritos();
    }

    private void inicializarComponentes() {
        recyclerView = findViewById(R.id.recyclerFavoritos);
        progressBar = findViewById(R.id.progressBarFavoritos);
        layoutVazio = findViewById(R.id.layoutVazioFavoritos);
        bottomNavigationView = findViewById(R.id.bottom_navigation); // ID corrigido
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EmpresaAdapter(listaFavoritos, new EmpresaAdapter.OnItemClickListener() {
            @Override
            public void onEmpresaClick(Empresa empresa) {
                Intent intent = new Intent(FavoritosActivity.this, EmpresaApresentacaoActivity.class);
                intent.putExtra("empresa", empresa);
                startActivity(intent);
            }

            @Override
            public void onEditarClick(Empresa empresa) {
                // Desabilitado para favoritos
            }

            @Override
            public void onRemoverClick(Empresa empresa) {
                // Desabilitado para favoritos
            }
        }, true); // true: somente visualização

        recyclerView.setAdapter(adapter);
    }

    private void configurarBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                abrirMainActivityComFragmento("home");
                return true;
            } else if (id == R.id.navigation_user) {
                abrirMainActivityComFragmento("user");
                return true;
            }
            return false;
        });
    }

    private void abrirMainActivityComFragmento(String nomeFragmento) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment", nomeFragmento);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void carregarFavoritos() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .collection("favoritos")
                .get()
                .addOnSuccessListener(snapshot -> {
                    listaFavoritos.clear();

                    for (var doc : snapshot.getDocuments()) {
                        Empresa empresa = doc.toObject(Empresa.class);
                        if (empresa != null) {
                            empresa.setIdDocumento(doc.getId());
                            listaFavoritos.add(empresa);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (listaFavoritos.isEmpty()) {
                        mostrarListaVazia();
                    } else {
                        mostrarListaNormal();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Erro ao carregar favoritos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void mostrarListaVazia() {
        recyclerView.setVisibility(View.GONE);
        layoutVazio.setVisibility(View.VISIBLE);
    }

    private void mostrarListaNormal() {
        recyclerView.setVisibility(View.VISIBLE);
        layoutVazio.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarFavoritos();
    }
}
