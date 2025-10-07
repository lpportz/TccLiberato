package com.example.ovep.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ovep.R;
import com.example.ovep.models.Empresa;
import com.example.ovep.telas.CadastroDeEmpresaActivity;
import com.example.ovep.telas.FavoritosActivity;
import com.example.ovep.telas.ListaEmpresasUserActivity;
import com.example.ovep.telas.LoginActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserFragment extends Fragment {

    // Constantes
    private static final String EMPRESAS_PATH = "empresas/";
    private static final String USUARIOS_EMPRESAS_PATH = "usuarios/%s/empresas";

    // Views
    private ProgressBar progressBar;
    private Button btnExcluirConta;
    private Button btnSairConta;
    private Button btnCadastrarEmpresa;
    private Button btnEditarEmpresas;
    private Button btnFavoritos;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFirebase();
        initViews(view);
        setupClickListeners();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    private void initViews(View view) {
        progressBar = view.findViewById(R.id.progressBarUserFragment);
        btnExcluirConta = view.findViewById(R.id.btnExcluirConta);
        btnSairConta = view.findViewById(R.id.btnSairConta);
        btnCadastrarEmpresa = view.findViewById(R.id.btnCadastrarEmpresa);
        btnEditarEmpresas = view.findViewById(R.id.btnEditarEmpresas);
        btnFavoritos = view.findViewById(R.id.btnFavoritos);

        progressBar.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnCadastrarEmpresa.setOnClickListener(v -> navigateTo(CadastroDeEmpresaActivity.class));
        btnEditarEmpresas.setOnClickListener(v -> navigateTo(ListaEmpresasUserActivity.class));
        btnFavoritos.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FavoritosActivity.class);
            startActivity(intent);
        });
        btnExcluirConta.setOnClickListener(v -> showPasswordConfirmationDialog());
        btnSairConta.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void navigateTo(Class<?> destination) {
        startActivity(new Intent(requireActivity(), destination));
    }


    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sair da conta")
                .setMessage("Tem certeza que deseja sair da sua conta?")
                .setPositiveButton("Sair", (dialog, which) -> logoutUser())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void logoutUser() {
        mAuth.signOut();
        redirectToLoginWithFlags();
        Toast.makeText(requireContext(), "Você saiu da conta", Toast.LENGTH_SHORT).show();
    }

    private void redirectToLoginWithFlags() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showPasswordConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Confirmação de senha");

        final EditText inputSenha = new EditText(requireContext());
        inputSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputSenha.setHint("Digite sua senha");
        builder.setView(inputSenha);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String senha = inputSenha.getText().toString().trim();
            if (senha.isEmpty()) {
                showToast("Senha não pode estar vazia");
            } else {
                reauthenticateAndDeleteUser(senha);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void reauthenticateAndDeleteUser(String senha) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            showToast("Usuário não autenticado");
            return;
        }

        showLoading(true);

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), senha);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> deleteUserCompanies(user))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showToast("Falha ao autenticar: " + e.getMessage());
                });
    }

    private void deleteUserCompanies(FirebaseUser user) {
        String userId = user.getUid();
        CollectionReference empresasRef = db.collection(String.format(USUARIOS_EMPRESAS_PATH, userId));

        empresasRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.isEmpty()) {
                deleteStorageFolder(userId);
                deleteUserAccount(user);
                return;
            }

            processCompanyDeletion(snapshot, userId);
        }).addOnFailureListener(e -> {
            showLoading(false);
            showToast("Erro ao buscar empresas: " + e.getMessage());
        });
    }

    private void processCompanyDeletion(QuerySnapshot snapshot, String userId) {
        int total = snapshot.size();
        int[] completed = {0};
        boolean[] errorOccurred = {false};

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Empresa empresa = doc.toObject(Empresa.class);
            String imageUrl = empresa != null ? empresa.getImagemUrl() : null;

            Task<Void> deleteImageTask = imageUrl != null && !imageUrl.isEmpty()
                    ? safeDeleteImage(imageUrl)
                    : Tasks.forResult(null);

            deleteImageTask.continueWithTask(task -> doc.getReference().delete())
                    .addOnSuccessListener(aVoid -> {
                        completed[0]++;
                        if (completed[0] == total && !errorOccurred[0]) {
                            deleteStorageFolder(userId);
                            deleteUserAccount(mAuth.getCurrentUser());
                        }
                    })
                    .addOnFailureListener(e -> {
                        errorOccurred[0] = true;
                        showLoading(false);
                        showToast("Erro ao excluir empresa: " + e.getMessage());
                    });
        }
    }

    private Task<Void> safeDeleteImage(String imageUrl) {
        try {
            return storage.getReferenceFromUrl(imageUrl).delete();
        } catch (Exception e) {
            return Tasks.forException(e);
        }
    }

    private void deleteStorageFolder(String userId) {
        StorageReference folder = storage.getReference().child(EMPRESAS_PATH + userId);

        folder.listAll().addOnSuccessListener(listResult -> {
            deleteStorageItems(listResult.getItems());
            deleteStoragePrefixes(listResult.getPrefixes());
        });
    }

    private void deleteStorageItems(Iterable<StorageReference> items) {
        for (StorageReference item : items) {
            item.delete();
        }
    }

    private void deleteStoragePrefixes(Iterable<StorageReference> prefixes) {
        for (StorageReference prefix : prefixes) {
            prefix.listAll().addOnSuccessListener(inner -> deleteStorageItems(inner.getItems()));
        }
    }

    private void deleteUserAccount(FirebaseUser user) {
        if (user == null) return;

        user.delete()
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    showToast("Conta excluída com sucesso");
                    redirectToLoginWithFlags();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showToast("Erro ao excluir conta: " + e.getMessage());
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        setButtonsEnabled(!show);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnExcluirConta.setEnabled(enabled);
        btnSairConta.setEnabled(enabled);
        btnCadastrarEmpresa.setEnabled(enabled);
        btnEditarEmpresas.setEnabled(enabled);
        btnFavoritos.setEnabled(enabled);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}