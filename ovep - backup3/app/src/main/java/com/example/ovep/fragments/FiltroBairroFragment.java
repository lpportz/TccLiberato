package com.example.ovep.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ovep.R;
import com.example.ovep.adapters.FiltroAdapter;
import com.example.ovep.firebase.FirebaseHelper;
import com.example.ovep.telas.TelaSemConteudoActivity;

import java.util.ArrayList;
import java.util.List;

public class FiltroBairroFragment extends Fragment {

    private static final String TAG = "FiltroBairroFragment";

    private FiltroAdapter<String> adapter;
    private String cidadeSelecionada = "";
    private String segmentoEscolhido = "";
    private FirebaseHelper firebaseHelper;
    private OnBairroSelecionadoListener listener;

    private View loadingLayout;
    private View contentLayout;
    private View errorLayout;
    private RecyclerView recyclerView;
    private Button btnTentarNovamente;
    private Button btnVoltar;

    public interface OnBairroSelecionadoListener {
        void onBairroSelecionado(String cidade, String bairro, String segmento);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnBairroSelecionadoListener) {
            listener = (OnBairroSelecionadoListener) context;
        } else {
            throw new RuntimeException(context + " deve implementar OnBairroSelecionadoListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_filtro_bairro, container, false);

        loadingLayout = root.findViewById(R.id.loadingLayout);
        contentLayout = root.findViewById(R.id.contentLayout);
        errorLayout = root.findViewById(R.id.errorLayout);
        recyclerView = root.findViewById(R.id.recyclerViewFiltros);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnTentarNovamente = root.findViewById(R.id.btnTentarNovamente);
        btnVoltar = root.findViewById(R.id.btnVoltar);

        btnTentarNovamente.setOnClickListener(v -> recarregarBairros());
        btnVoltar.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseHelper = new FirebaseHelper();
        recuperarDadosDoBundle();

        adapter = new FiltroAdapter<>(
                new ArrayList<>(),
                bairro -> {
                    Log.d(TAG, "Bairro selecionado: " + bairro);
                    if (listener != null && !cidadeSelecionada.isEmpty()) {
                        listener.onBairroSelecionado(cidadeSelecionada, bairro, segmentoEscolhido);
                    }
                },
                String::compareToIgnoreCase,
                b -> b
        );
        recyclerView.setAdapter(adapter);

        if (!cidadeSelecionada.isEmpty()) {
            mostrarLoading();
            carregarBairrosRealtime();
        } else {
            Toast.makeText(getContext(), "Cidade inválida", Toast.LENGTH_SHORT).show();
        }
    }

    // Recupera dados do bundle
    private void recuperarDadosDoBundle() {
        Bundle args = getArguments();
        if (args != null) {
            cidadeSelecionada = args.getString("cidade", "").toUpperCase();
            segmentoEscolhido = args.getString("segmento", "");

            Log.d(TAG, "Cidade recebida do bundle: " + cidadeSelecionada);
            Log.d(TAG, "Segmento recebido do bundle: '" + segmentoEscolhido + "'");

            if (getContext() != null) {
                Toast.makeText(getContext(),
                        "Segmento escolhido: " + (segmentoEscolhido.isEmpty() ? "Todos" : segmentoEscolhido),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void recarregarBairros() {
        mostrarLoading();
        carregarBairrosRealtime();
    }

    private void carregarBairrosRealtime() {
        Toast.makeText(getContext(),
                "Carregando bairros para cidade: " + cidadeSelecionada +
                        " | segmento: " + (segmentoEscolhido.isEmpty() ? "Todos" : segmentoEscolhido),
                Toast.LENGTH_SHORT).show();

        firebaseHelper.listenBairrosPorCidadeESegmento(cidadeSelecionada, segmentoEscolhido, new FirebaseHelper.BairroListener() {
            @Override
            public void onUpdate(List<String> bairros) {
                if (!isAdded()) return;

                if (bairros == null || bairros.isEmpty()) {
                    abrirTelaSemConteudo();
                } else {
                    requireActivity().runOnUiThread(() -> {
                        adapter.updateData(bairros);
                        mostrarConteudo();
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Erro ao carregar bairros", e);
                if (isAdded()) requireActivity().runOnUiThread(FiltroBairroFragment.this::mostrarErro);
            }

            private void abrirTelaSemConteudo() {
                if (!isAdded()) return;
                Intent intent = new Intent(getActivity(), TelaSemConteudoActivity.class);
                intent.putExtra("mensagem",
                        "Nenhum serviço encontrado para o segmento '" + (segmentoEscolhido.isEmpty() ? "Todos" : segmentoEscolhido) +
                                "' na cidade " + cidadeSelecionada + ".");
                startActivity(intent);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void mostrarLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

    private void mostrarConteudo() {
        loadingLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
    }

    private void mostrarErro() {
        loadingLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firebaseHelper != null) firebaseHelper.removerTodosListeners();
    }
}
