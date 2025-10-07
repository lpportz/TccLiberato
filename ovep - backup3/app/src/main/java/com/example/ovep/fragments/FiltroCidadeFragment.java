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
import com.example.ovep.models.Cidade;
import com.example.ovep.services.IbgeService;
import com.example.ovep.services.RetrofitClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FiltroCidadeFragment extends Fragment {

    private static final String TAG = "FiltroCidadeFragment";

    private View loadingLayout, errorLayout;
    private RecyclerView recyclerView;
    private Button btnTentarNovamente, btnVoltar;
    private FiltroAdapter<Cidade> adapter;
    private List<Cidade> cidadesOriginais = new ArrayList<>();
    private int idEstado = -1;
    private String segmentoEscolhido = ""; // ✅ Recebe segmento

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_filtro_cidade, container, false);
        inicializarViews(rootView);
        configurarListeners();
        recuperarDadosDoBundle(); // ✅ recupera idEstado e segmento
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (idEstado != -1) {
            mostrarLoading();
            carregarCidades(idEstado);
        } else {
            mostrarErro("Estado inválido");
        }
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
            carregarCidades(idEstado);
        });

        btnVoltar.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    // Recupera idEstado e segmento do bundle
    private void recuperarDadosDoBundle() {
        Bundle args = getArguments();
        if (args != null) {
            idEstado = args.getInt("idEstado", -1);
            segmentoEscolhido = args.getString("segmento", "");
            Log.d(TAG, "Segmento recebido do bundle: '" + segmentoEscolhido + "'");

            if (getContext() != null) {
                Toast.makeText(getContext(),
                        "Segmento recebido: " + (segmentoEscolhido.isEmpty() ? "Todos" : segmentoEscolhido),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void carregarCidades(int idEstado) {
        IbgeService service = RetrofitClient.getIbgeService();
        service.listarCidades(idEstado).enqueue(new Callback<List<Cidade>>() {
            @Override
            public void onResponse(@NonNull Call<List<Cidade>> call,
                                   @NonNull Response<List<Cidade>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cidadesOriginais = new ArrayList<>(response.body());
                    Collections.sort(cidadesOriginais, (c1, c2) -> c1.getNome().compareToIgnoreCase(c2.getNome()));
                    configurarAdapter(cidadesOriginais);
                    mostrarConteudo();
                } else {
                    mostrarErro("Erro ao carregar cidades");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Cidade>> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha na requisição", t);
                mostrarErro("Falha na requisição: " + t.getMessage());
            }
        });
    }

    private void configurarAdapter(List<Cidade> cidades) {
        adapter = new FiltroAdapter<>(
                cidades,
                this::abrirFiltroBairro,
                (c1, c2) -> c1.getNome().compareToIgnoreCase(c2.getNome()),
                Cidade::getNome
        );
        recyclerView.setAdapter(adapter);
    }

    private void abrirFiltroBairro(Cidade cidade) {
        FiltroBairroFragment bairroFragment = new FiltroBairroFragment();
        Bundle args = new Bundle();
        args.putString("cidade", cidade.getNome());
        args.putString("segmento", segmentoEscolhido); // ✅ Passa segmento adiante
        bairroFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, bairroFragment)
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
        if (adapter == null || cidadesOriginais.isEmpty()) return;

        String filtro = textoFiltro.toLowerCase().trim();
        if (filtro.isEmpty()) {
            adapter.updateData(cidadesOriginais);
            return;
        }

        List<Cidade> filtradas = new ArrayList<>();
        for (Cidade c : cidadesOriginais) {
            if (c.getNome().toLowerCase().contains(filtro)) filtradas.add(c);
        }
        adapter.updateData(filtradas);
    }
}
