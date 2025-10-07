package com.example.ovep.fragments;

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
import com.example.ovep.models.Estado;
import com.example.ovep.services.IbgeService;
import com.example.ovep.services.RetrofitClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FiltroEstadoFragment extends Fragment {

    private View loadingLayout, errorLayout;
    private RecyclerView recyclerView;
    private Button btnTentarNovamente, btnVoltar;
    private FiltroAdapter<Estado> adapter;
    private List<Estado> estadosOriginais = new ArrayList<>();
    private String segmentoEscolhido;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_filtro_estado, container, false);
        inicializarViews(rootView);
        configurarListeners();
        recuperarSegmentoDoBundle();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mostrarLoading();
        carregarEstados();
    }

    private void inicializarViews(View rootView) {
        loadingLayout = rootView.findViewById(R.id.loadingLayout);
        recyclerView = rootView.findViewById(R.id.recyclerViewFiltros);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        errorLayout = rootView.findViewById(R.id.layoutErro);
        btnTentarNovamente = rootView.findViewById(R.id.btnTentarNovamente);
        btnVoltar = rootView.findViewById(R.id.btnVoltar);
    }

    private void configurarListeners() {
        btnTentarNovamente.setOnClickListener(v -> {
            mostrarLoading();
            carregarEstados();
        });

        btnVoltar.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void recuperarSegmentoDoBundle() {
        Bundle args = getArguments();
        if (args != null) {
            segmentoEscolhido = args.getString("segmento");
            Log.d("FiltroEstadoFragment", "Segmento recebido do bundle: '" + segmentoEscolhido + "'");

            // Mostrar Toast para teste
            if (getContext() != null && segmentoEscolhido != null) {
                Toast.makeText(getContext(),
                        "Segmento escolhido: " + segmentoEscolhido,
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w("FiltroEstadoFragment", "Nenhum bundle recebido com segmento.");
        }
    }

    private void carregarEstados() {
        IbgeService service = RetrofitClient.getIbgeService();
        service.listarEstados().enqueue(new Callback<List<Estado>>() {
            @Override
            public void onResponse(@NonNull Call<List<Estado>> call,
                                   @NonNull Response<List<Estado>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    estadosOriginais = new ArrayList<>(response.body());
                    Collections.sort(estadosOriginais, (e1, e2) -> e1.getNome().compareToIgnoreCase(e2.getNome()));
                    configurarAdapter(estadosOriginais);
                    mostrarConteudo();
                } else {
                    mostrarErro("Erro ao carregar estados");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Estado>> call, @NonNull Throwable t) {
                Log.e("FiltroEstadoFragment", "Falha na requisição", t);
                mostrarErro("Falha na requisição: " + t.getMessage());
            }
        });
    }

    private void configurarAdapter(List<Estado> estados) {
        adapter = new FiltroAdapter<>(
                estados,
                this::abrirFiltroCidade,
                (e1, e2) -> e1.getNome().compareToIgnoreCase(e2.getNome()),
                Estado::getNome
        );
        recyclerView.setAdapter(adapter);
    }

    private void abrirFiltroCidade(Estado estado) {
        FiltroCidadeFragment cidadeFragment = new FiltroCidadeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("idEstado", estado.getId());
        bundle.putString("nomeEstado", estado.getNome());
        // Passando segmento escolhido adiante
        bundle.putString("segmento", segmentoEscolhido);
        cidadeFragment.setArguments(bundle);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, cidadeFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void mostrarLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        if (errorLayout != null) errorLayout.setVisibility(View.GONE);
    }

    private void mostrarConteudo() {
        loadingLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        if (errorLayout != null) errorLayout.setVisibility(View.GONE);
    }

    private void mostrarErro(String mensagem) {
        loadingLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        if (errorLayout != null) errorLayout.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), mensagem, Toast.LENGTH_LONG).show();
    }

    public void aplicarFiltro(String textoFiltro) {
        if (adapter == null || estadosOriginais.isEmpty()) return;

        String filtro = textoFiltro.toLowerCase().trim();
        if (filtro.isEmpty()) {
            adapter.updateData(estadosOriginais);
            return;
        }

        List<Estado> filtrados = new ArrayList<>();
        for (Estado e : estadosOriginais) {
            if (e.getNome().toLowerCase().contains(filtro)) filtrados.add(e);
        }
        adapter.updateData(filtrados);
    }

    public String getSegmentoEscolhido() {
        return segmentoEscolhido;
    }
}
