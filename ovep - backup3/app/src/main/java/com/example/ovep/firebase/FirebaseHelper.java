
package com.example.ovep.firebase;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private final FirebaseFirestore db;

    private ListenerRegistration bairrosListener;

    public FirebaseHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface BairroListener {
        void onUpdate(List<String> bairros);
        void onFailure(Exception e);
    }

    public interface EmpresaListener {
        void onUpdate(List<QueryDocumentSnapshot> empresas);
        void onFailure(Exception e);
    }

    // --- Listener de bairros por cidade (sem segmento) ---
    public void listenBairrosPorCidade(String cidade, BairroListener listener) {
        listenBairrosPorCidadeESegmento(cidade, null, listener);
    }

    // --- Listener de bairros por cidade e segmento ---
    public void listenBairrosPorCidadeESegmento(String cidade, String segmento, BairroListener listener) {
        if (cidade == null || cidade.trim().isEmpty()) {
            Log.w(TAG, "Cidade inválida fornecida ao listenBairrosPorCidadeESegmento");
            listener.onUpdate(new ArrayList<>());
            return;
        }

        if (bairrosListener != null) {
            bairrosListener.remove();
            bairrosListener = null;
        }

        Log.d(TAG, "Iniciando listener para bairros da cidade='" + cidade + "' segmento='" + segmento + "'");

        var query = db.collectionGroup("empresas")
                .whereEqualTo("cidade", cidade);

        if (segmento != null && !segmento.trim().isEmpty()) {
            query = query.whereEqualTo("segmento", segmento);
        }

        bairrosListener = query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "Erro ao escutar bairros", e);
                listener.onFailure(e);
                return;
            }

            if (querySnapshot == null || querySnapshot.isEmpty()) {
                Log.d(TAG, "Nenhum bairro encontrado para cidade='" + cidade + "' segmento='" + segmento + "'");
                listener.onUpdate(new ArrayList<>());
                return;
            }

            Set<String> bairrosSet = new HashSet<>();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                String bairro = doc.getString("bairro");
                if (bairro != null) bairrosSet.add(bairro);
            }

            List<String> bairros = new ArrayList<>(bairrosSet);
            Collections.sort(bairros);
            listener.onUpdate(bairros);
        });
    }


    public void buscarEmpresasPorBairro(String cidade, String bairro, String segmento, EmpresaListener listener) {
        if (cidade == null || bairro == null || cidade.trim().isEmpty() || bairro.trim().isEmpty()) {
            Log.w(TAG, "Cidade ou bairro inválido fornecido ao buscarEmpresasPorBairro");
            listener.onUpdate(new ArrayList<>());
            return;
        }

        Log.d(TAG, "Buscando empresas em: cidade='" + cidade + "' bairro='" + bairro + "' segmento='" + segmento + "'");

        var query = db.collectionGroup("empresas")
                .whereEqualTo("cidade", cidade)
                .whereEqualTo("bairro", bairro);

        if (segmento != null && !segmento.trim().isEmpty()) {
            query = query.whereEqualTo("segmento", segmento);
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<QueryDocumentSnapshot> empresas = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            empresas.add(doc);
                        }
                    }
                    listener.onUpdate(empresas);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao buscar empresas", e);
                    listener.onFailure(e);
                });
    }

    public void removerTodosListeners() {
        if (bairrosListener != null) {
            bairrosListener.remove();
            bairrosListener = null;
            Log.d(TAG, "Listener de bairros removido.");
        }
    }
}
