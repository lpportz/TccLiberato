package com.example.ovep.telas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ovep.R;
import com.example.ovep.adapters.EmpresaAdapter;
import com.example.ovep.models.Empresa;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ListaEmpresasUserActivity extends AppCompatActivity implements EmpresaAdapter.OnItemClickListener {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LinearLayout layoutVazio;
    private Button btnVoltarUser;
    private BottomNavigationView bottomNavigationView;

    private EmpresaAdapter adapter;
    private final List<Empresa> listaEmpresas = new ArrayList<>();

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_lista_empresas_user);

        inicializarComponentes();
        configurarRecyclerView();
        configurarBotoes();
        configurarBottomNavigation();
        autenticarUsuarioECarregarEmpresas();
    }

    private void inicializarComponentes() {
        recyclerView = findViewById(R.id.recyclerViewEmpresa);
        progressBar = findViewById(R.id.progressBar);
        layoutVazio = findViewById(R.id.layoutVazio);
        btnVoltarUser = findViewById(R.id.btnVoltarUser);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmpresaAdapter(listaEmpresas, this, false);
        recyclerView.setAdapter(adapter);
    }

    private void configurarBotoes() {
        btnVoltarUser.setOnClickListener(v -> abrirMainActivityComFragmento("user"));
    }

    private void configurarBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                abrirMainActivityComFragmento("home");
                return true;
            } else if (item.getItemId() == R.id.navigation_user) {
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

    private void autenticarUsuarioECarregarEmpresas() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = usuario.getUid();
        db = FirebaseFirestore.getInstance();
        carregarEmpresas();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void carregarEmpresas() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("usuarios")
                .document(userId)
                .collection("empresas")
                .get()
                .addOnSuccessListener(snapshot -> {
                    listaEmpresas.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Empresa empresa = doc.toObject(Empresa.class);
                        if (empresa != null) {
                            empresa.setIdDocumento(doc.getId());
                            listaEmpresas.add(empresa);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (listaEmpresas.isEmpty()) {
                        mostrarListaVazia();
                    } else {
                        mostrarListaNormal();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Erro ao carregar empresas: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
    public void onEmpresaClick(Empresa empresa) {
        Toast.makeText(this, "Empresa selecionada: " + empresa.getNome(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditarClick(Empresa empresa) {
        Intent intent = new Intent(this, CadastroDeEmpresaActivity.class);
        intent.putExtra("empresaParaEditar", empresa);
        startActivity(intent);
    }

    @Override
    public void onRemoverClick(Empresa empresa) {
        new AlertDialog.Builder(this)
                .setTitle("Remover empresa")
                .setMessage("Deseja realmente remover a empresa " + empresa.getNome() + "?")
                .setPositiveButton("Sim", (dialog, which) -> excluirEmpresa(empresa))
                .setNegativeButton("Não", null)
                .show();
    }

    private void excluirEmpresa(Empresa empresa) {
        progressBar.setVisibility(View.VISIBLE);
        String imagemUrl = empresa.getImagemUrl();

        if (imagemUrl != null && !imagemUrl.isEmpty()
                && (imagemUrl.startsWith("gs://") || imagemUrl.startsWith("https://"))) {
            try {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imagemUrl);
                storageReference.delete()
                        .addOnSuccessListener(aVoid -> excluirDocumentoFirestore(empresa))
                        .addOnFailureListener(e -> excluirDocumentoFirestore(empresa));
            } catch (IllegalArgumentException e) {
                excluirDocumentoFirestore(empresa);
            }
        } else {
            excluirDocumentoFirestore(empresa);
        }
    }

    private void excluirDocumentoFirestore(Empresa empresa) {
        db.collection("usuarios")
                .document(userId)
                .collection("empresas")
                .document(empresa.getIdDocumento())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Empresa removida com sucesso", Toast.LENGTH_SHORT).show();
                    carregarEmpresas();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao remover empresa: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarEmpresas();
    }
}
