package com.example.ovep.telas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ovep.R;
import com.example.ovep.models.Empresa;
import com.example.ovep.services.ViaCepResponse;
import com.example.ovep.services.ViaCepService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.*;

import java.util.Objects;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class CadastroDeEmpresaActivity extends AppCompatActivity {

    // Views
    private EditText editTextNomeEmpresa, editTextWhatsApp, editTextRua, editTextBairro,
            editTextCep, editTextCidade, editTextEstado;
    private Spinner spinnerSegmento;
    private TextView textNomeArquivoImagem;
    private Button btnSalvarEmpresa;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;

    // Dados auxiliares
    private Uri imagemSelecionadaUri;
    private Empresa empresaParaEditar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Retrofit ViaCEP
    private ViaCepService viaCepService;

    // Adapter Spinner segmento
    private ArrayAdapter<CharSequence> segmentoAdapter;

    // Activity Result Launcher para seleção de imagem
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_cadastro_de_empresa);

        inicializarFirebase();
        inicializarViews();
        configurarSegmentoSpinner();
        configurarRetrofit();
        configurarActivityResultLauncher();
        configurarBottomNavigation();

        if (getIntent().hasExtra("empresaParaEditar")) {
            empresaParaEditar = (Empresa) getIntent().getSerializableExtra("empresaParaEditar");
            if (empresaParaEditar != null) {
                preencherCamposParaEdicao(empresaParaEditar);
            }
        }
    }

    private void inicializarFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    private void inicializarViews() {
        editTextNomeEmpresa = findViewById(R.id.editTextNomeEmpresa);
        editTextWhatsApp = findViewById(R.id.editTextWhatsApp);
        editTextRua = findViewById(R.id.editTextRua);
        editTextBairro = findViewById(R.id.editTextBairro);
        editTextCep = findViewById(R.id.editTextCep);
        editTextCidade = findViewById(R.id.editTextCidade);
        editTextEstado = findViewById(R.id.editTextEstado);

        editTextCidade.setEnabled(false);
        editTextEstado.setEnabled(false);

        spinnerSegmento = findViewById(R.id.spinnerSegmento);

        Button btnSelecionarImagem = findViewById(R.id.btnSelecionarImagem);
        btnSalvarEmpresa = findViewById(R.id.btnSalvarEmpresa);
        Button btnBuscarCep = findViewById(R.id.btnBuscarCep);

        textNomeArquivoImagem = findViewById(R.id.textNomeArquivoImagem);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        btnSelecionarImagem.setOnClickListener(v -> abrirGaleria());
        btnSalvarEmpresa.setOnClickListener(v -> salvarEmpresa());
        btnBuscarCep.setOnClickListener(v -> buscarEnderecoPorCep());

        bottomNavigation = findViewById(R.id.bottom_navigation); // ID do seu BottomNavigationView
    }

    private void configurarSegmentoSpinner() {
        segmentoAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.segmentos,
                android.R.layout.simple_spinner_item
        );
        segmentoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSegmento.setAdapter(segmentoAdapter);
    }

    private void configurarRetrofit() {
        Retrofit retrofitCep = new Retrofit.Builder()
                .baseUrl("https://viacep.com.br/ws/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        viaCepService = retrofitCep.create(ViaCepService.class);
    }

    private void configurarActivityResultLauncher() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imagemSelecionadaUri = result.getData().getData();
                        textNomeArquivoImagem.setText(obterNomeArquivo(imagemSelecionadaUri));
                    }
                }
        );
    }

    private void configurarBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                Intent intent = new Intent(CadastroDeEmpresaActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }

            if (id == R.id.navigation_user) {
                Intent intent = new Intent(CadastroDeEmpresaActivity.this, MainActivity.class);
                intent.putExtra("abrir_user_fragment", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private String obterNomeArquivo(Uri uri) {
        String nome = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                if (idx >= 0) nome = cursor.getString(idx);
            }
        } catch (Exception e) {
            // Pode logar o erro se quiser
        }
        return nome != null ? nome : "Imagem selecionada";
    }

    private void salvarEmpresa() {
        String nome = editTextNomeEmpresa.getText().toString().trim();
        String whatsapp = editTextWhatsApp.getText().toString().trim();
        String rua = editTextRua.getText().toString().trim();
        String bairro = editTextBairro.getText().toString().trim();
        String cidade = editTextCidade.getText().toString().trim();
        String estado = editTextEstado.getText().toString().trim();
        String segmento = (String) spinnerSegmento.getSelectedItem();
        String cep = editTextCep.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(whatsapp) || TextUtils.isEmpty(rua)
                || TextUtils.isEmpty(bairro) || TextUtils.isEmpty(cidade)
                || TextUtils.isEmpty(estado) || TextUtils.isEmpty(cep)
                || segmento == null || (empresaParaEditar == null && imagemSelecionadaUri == null)) {
            mostrarToast("Preencha todos os campos e selecione uma imagem");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            mostrarToast("Usuário não autenticado");
            return;
        }

        setCarregando(true);
        String userId = user.getUid();

        if (empresaParaEditar != null) {
            boolean mudouSegmento = !segmento.equals(empresaParaEditar.getSegmento());

            if (imagemSelecionadaUri != null) {
                excluirImagemAntigaEEnviarNova(userId, segmento);
            } else if (mudouSegmento) {
                moverImagemParaNovoSegmento(userId, segmento);
            } else {
                salvarNoFirestore(userId, empresaParaEditar.getImagemUrl(), true);
            }
        } else {
            db.collection("usuarios").document(userId).collection("empresas")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.size() >= 3) {
                            mostrarToast("Você já cadastrou 3 segmentos.");
                            setCarregando(false);
                        } else {
                            uploadImagemESalvar(userId, segmento, false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        mostrarToast("Erro ao verificar empresas.");
                        setCarregando(false);
                    });
        }
    }

    private void excluirImagemAntigaEEnviarNova(String userId, String segmento) {
        if (empresaParaEditar.getImagemUrl() != null) {
            try {
                StorageReference refAntigo = storage.getReferenceFromUrl(empresaParaEditar.getImagemUrl());
                refAntigo.delete();
            } catch (Exception ignored) {}
        }
        uploadImagemESalvar(userId, segmento, true);
    }

    private void moverImagemParaNovoSegmento(String userId, String novoSegmento) {
        try {
            StorageReference antigaRef = storage.getReferenceFromUrl(empresaParaEditar.getImagemUrl());
            antigaRef.getBytes(Long.MAX_VALUE)
                    .addOnSuccessListener(bytes -> {
                        String novoNome = System.currentTimeMillis() + ".jpg";
                        StorageReference novaRef = storageRef.child("empresas/" + userId + "/" + novoSegmento + "/" + novoNome);

                        novaRef.putBytes(bytes)
                                .addOnSuccessListener(task -> novaRef.getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            antigaRef.delete();
                                            salvarNoFirestore(userId, uri.toString(), true);
                                        }))
                                .addOnFailureListener(e -> erro("Erro ao mover imagem: " + e.getMessage()));
                    })
                    .addOnFailureListener(e -> erro("Erro ao baixar imagem antiga: " + e.getMessage()));
        } catch (Exception e) {
            erro("Erro ao mover imagem: " + e.getMessage());
        }
    }

    private void uploadImagemESalvar(String userId, String segmento, boolean isEdicao) {
        String nomeArquivo = System.currentTimeMillis() + ".jpg";
        StorageReference imagemRef = storageRef.child("empresas/" + userId + "/" + segmento + "/" + nomeArquivo);

        imagemRef.putFile(imagemSelecionadaUri)
                .addOnSuccessListener(task -> imagemRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> salvarNoFirestore(userId, uri.toString(), isEdicao)))
                .addOnFailureListener(e -> erro("Falha no upload: " + e.getMessage()));
    }

    private void salvarNoFirestore(String userId, String urlImagem, boolean isEdicao) {
        Empresa empresa;

        String nome = editTextNomeEmpresa.getText().toString().trim().toUpperCase();
        String segmento = (String) spinnerSegmento.getSelectedItem();
        String estado = editTextEstado.getText().toString().trim().toUpperCase();
        String cidade = editTextCidade.getText().toString().trim().toUpperCase();
        String rua = editTextRua.getText().toString().trim().toUpperCase();
        String bairro = editTextBairro.getText().toString().trim().toUpperCase();
        String cep = editTextCep.getText().toString().trim();
        String whatsapp = editTextWhatsApp.getText().toString().trim();

        if (isEdicao && empresaParaEditar != null) {
            empresa = new Empresa(
                    empresaParaEditar.getIdDocumento(),
                    nome,
                    segmento,
                    estado,
                    cidade,
                    rua,
                    bairro,
                    cep,
                    whatsapp,
                    urlImagem
            );

            DocumentReference ref = db.collection("usuarios").document(userId)
                    .collection("empresas").document(empresaParaEditar.getIdDocumento());

            ref.set(empresa)
                    .addOnSuccessListener(aVoid -> {
                        mostrarToast("Empresa atualizada com sucesso!");
                        redirecionarParaUserFragment();
                    })
                    .addOnFailureListener(e -> erro("Erro ao salvar: " + e.getMessage()));

        } else {
            DocumentReference ref = db.collection("usuarios").document(userId)
                    .collection("empresas").document();

            String novoIdDocumento = ref.getId();

            empresa = new Empresa(
                    novoIdDocumento,
                    nome,
                    segmento,
                    estado,
                    cidade,
                    rua,
                    bairro,
                    cep,
                    whatsapp,
                    urlImagem
            );

            ref.set(empresa)
                    .addOnSuccessListener(aVoid -> {
                        mostrarToast("Empresa criada com sucesso!");
                        redirecionarParaUserFragment();
                    })
                    .addOnFailureListener(e -> erro("Erro ao salvar: " + e.getMessage()));
        }
    }


    @SuppressLint("SetTextI18n")
    private void preencherCamposParaEdicao(Empresa empresa) {
        editTextNomeEmpresa.setText(empresa.getNome());
        editTextWhatsApp.setText(empresa.getWhatsapp());
        editTextRua.setText(empresa.getRua());
        editTextBairro.setText(empresa.getBairro());
        editTextCep.setText(empresa.getCep());
        editTextCidade.setText(empresa.getCidade());
        editTextEstado.setText(empresa.getEstado());
        spinnerSegmento.post(() -> setSpinnerToValue(spinnerSegmento, empresa.getSegmento()));
        textNomeArquivoImagem.setText("Imagem mantida");
    }

    private void setSpinnerToValue(Spinner spinner, String value) {
        if (segmentoAdapter == null || value == null) return;
        for (int i = 0; i < segmentoAdapter.getCount(); i++) {
            if (value.equals(Objects.requireNonNull(segmentoAdapter.getItem(i)).toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void redirecionarParaUserFragment() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("abrir_user_fragment", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void buscarEnderecoPorCep() {
        String cep = editTextCep.getText().toString().trim().replaceAll("[^0-9]", "");
        if (cep.length() != 8) {
            mostrarToast("CEP inválido");
            return;
        }

        viaCepService.buscarEndereco(cep).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ViaCepResponse> call, @NonNull Response<ViaCepResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ViaCepResponse endereco = response.body();
                    editTextRua.setText(endereco.getLogradouro());
                    editTextBairro.setText(endereco.getBairro());
                    editTextCidade.setText(endereco.getLocalidade());
                    editTextEstado.setText(endereco.getUf());
                } else {
                    mostrarToast("CEP não encontrado");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ViaCepResponse> call, @NonNull Throwable t) {
                mostrarToast("Erro ao buscar CEP: " + t.getMessage());
            }
        });
    }

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnSalvarEmpresa.setEnabled(!carregando);
    }

    private void mostrarToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void erro(String mensagem) {
        setCarregando(false);
        mostrarToast(mensagem);
    }
}
