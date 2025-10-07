package com.example.ovep.telas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.ovep.R;
import com.example.ovep.adapters.ComentarioAdapter;
import com.example.ovep.databinding.TelaEmpresaApresentacaoBinding;
import com.example.ovep.models.Comentario;
import com.example.ovep.models.Empresa;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class EmpresaApresentacaoActivity extends AppCompatActivity {

    private TelaEmpresaApresentacaoBinding binding;
    private Empresa empresa;
    private boolean isFavorito = false;

    private FirebaseFirestore db;
    private String userId;
    private String userNome;

    private final List<Comentario> listaComentarios = new ArrayList<>();
    private ComentarioAdapter comentarioAdapter;
    private CollectionReference comentariosRef;

    private Comentario comentarioParaEditar = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TelaEmpresaApresentacaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        empresa = (Empresa) getIntent().getSerializableExtra("empresa");
        if (empresa == null) {
            Toast.makeText(this, "Erro ao carregar dados da empresa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        carregarNomeUsuario();
        configurarListeners();
        configurarLayout();
        configurarBottomNavigation();
    }

    private void carregarNomeUsuario() {
        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        userNome = doc.getString("nome");
                        inicializarAdapterEComentarios();
                    } else {
                        Toast.makeText(this, "Usuário não encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao carregar usuário", Toast.LENGTH_SHORT).show());
    }

    private void inicializarAdapterEComentarios() {
        comentarioAdapter = new ComentarioAdapter(listaComentarios, userNome, new ComentarioAdapter.ComentarioClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEditar(Comentario comentario) {
                comentarioParaEditar = comentario;
                binding.editComentario.setText(comentario.getTexto());
            }

            @Override
            public void onExcluir(Comentario comentario) {
                excluirComentario(comentario);
            }
        });

        binding.recyclerComentarios.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerComentarios.setAdapter(comentarioAdapter);

        comentariosRef = db.collection("empresas")
                .document(empresa.getIdDocumento())
                .collection("comentarios");

        carregarComentarios();

        binding.btnEnviarComentario.setOnClickListener(v -> {
            if (comentarioParaEditar != null) {
                editarComentario();
            } else {
                enviarComentario();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void carregarComentarios() {
        comentariosRef.orderBy("data", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Erro ao carregar comentários", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaComentarios.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Comentario comentario = doc.toObject(Comentario.class);
                            comentario.setIdDocumento(doc.getId());
                            listaComentarios.add(comentario);
                        }
                    }
                    comentarioAdapter.notifyDataSetChanged();
                });
    }

    private void enviarComentario() {
        String texto = binding.editComentario.getText().toString().trim();
        if (texto.isEmpty()) {
            Toast.makeText(this, "Digite um comentário antes de enviar", Toast.LENGTH_SHORT).show();
            return;
        }

        Comentario novoComentario = new Comentario(texto, userNome, new Date());
        binding.btnEnviarComentario.setEnabled(false);

        comentariosRef.add(novoComentario)
                .addOnSuccessListener(docRef -> {
                    binding.editComentario.setText("");
                    Toast.makeText(this, "Comentário enviado", Toast.LENGTH_SHORT).show();
                    binding.btnEnviarComentario.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao enviar comentário: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.btnEnviarComentario.setEnabled(true);
                });
    }

    private void editarComentario() {
        String novoTexto = binding.editComentario.getText().toString().trim();
        if (novoTexto.isEmpty()) {
            Toast.makeText(this, "Comentário vazio!", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference ref = comentariosRef.document(comentarioParaEditar.getIdDocumento());
        ref.update("texto", novoTexto)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Comentário atualizado", Toast.LENGTH_SHORT).show();
                    binding.editComentario.setText("");
                    comentarioParaEditar = null;
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao editar comentário", Toast.LENGTH_SHORT).show());
    }

    private void excluirComentario(Comentario comentario) {
        comentariosRef.document(comentario.getIdDocumento())
                .delete()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Comentário excluído", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao excluir comentário", Toast.LENGTH_SHORT).show());
    }

    private void configurarLayout() {
        binding.textNomeEmpresa.setText(empresa.getNome());
        binding.textEndereco.setText(empresa.getRua() + " - " + empresa.getBairro());

        Glide.with(this)
                .load(empresa.getImagemUrl())
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(binding.imageBanner);

        verificarSeFavoritada();
        atualizarIconeFavorito();
    }

    private void configurarListeners() {
        binding.btnWhatsApp.setOnClickListener(v -> abrirWhatsApp());

        binding.btnFavorito.setOnClickListener(v -> {
            isFavorito = !isFavorito;
            atualizarIconeFavorito();
            if (isFavorito) {
                salvarFavoritoNoFirestore();
            } else {
                removerFavoritoDoFirestore();
            }
        });
    }

    private void configurarBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                Intent intent = new Intent(EmpresaApresentacaoActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }

            if (id == R.id.navigation_user) {
                Intent intent = new Intent(EmpresaApresentacaoActivity.this, MainActivity.class);
                intent.putExtra("abrir_user_fragment", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });
    }

    private void atualizarIconeFavorito() {
        binding.btnFavorito.setImageResource(isFavorito ? R.drawable.ic_favorite_ativo : R.drawable.ic_favorite_inativo);
    }

    private void verificarSeFavoritada() {
        db.collection("usuarios").document(userId).collection("favoritos")
                .document(empresa.getIdDocumento())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        isFavorito = true;
                        atualizarIconeFavorito();
                    }
                });
    }

    private void salvarFavoritoNoFirestore() {
        db.collection("usuarios").document(userId).collection("favoritos")
                .document(empresa.getIdDocumento()).set(empresa)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Adicionado aos favoritos", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao favoritar", Toast.LENGTH_SHORT).show());
    }

    private void removerFavoritoDoFirestore() {
        db.collection("usuarios").document(userId).collection("favoritos")
                .document(empresa.getIdDocumento()).delete()
                .addOnSuccessListener(unused -> Toast.makeText(this, "Removido dos favoritos", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao remover favorito", Toast.LENGTH_SHORT).show());
    }

    private void abrirWhatsApp() {
        String telefone = empresa.getWhatsapp();
        if (telefone == null || telefone.isEmpty()) {
            Toast.makeText(this, "Número de WhatsApp não disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://wa.me/55" + telefone.replaceAll("[^0-9]", "");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "WhatsApp não instalado", Toast.LENGTH_SHORT).show();
        }
    }
}
