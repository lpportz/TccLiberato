package com.example.ovep.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ovep.R;
import com.example.ovep.adapters.FiltroAdapter;
import com.example.ovep.telas.MainActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FiltroSegmentoFragment extends Fragment {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_SEGMENTO_ESCOLHIDO = "segmento_escolhido";

    private RecyclerView recyclerView;
    private View loadingLayout;
    private FiltroAdapter<String> adapter;
    private List<String> segmentosOriginais = new ArrayList<>();
    private String segmentoEscolhido;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_filtro_segmento, container, false);

        recyclerView = root.findViewById(R.id.recyclerViewFiltros);
        loadingLayout = root.findViewById(R.id.loadingLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FiltroAdapter<>(
                new ArrayList<>(),
                this::onSegmentoSelecionado,
                String::compareToIgnoreCase,
                s -> s
        );

        recyclerView.setAdapter(adapter);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        carregarSegmentos();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).atualizarVisibilidadeBusca(this);
        }
    }

    private void onSegmentoSelecionado(String nomeSegmento) {
        segmentoEscolhido = nomeSegmento;
        salvarSegmento(segmentoEscolhido);

        FiltroEstadoFragment estadoFragment = new FiltroEstadoFragment();
        Bundle args = new Bundle();
        args.putString("segmento", segmentoEscolhido);
        estadoFragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, estadoFragment)
                .addToBackStack(null)
                .commit();
    }

    private void carregarSegmentos() {
        String[] segmentosArray = getResources().getStringArray(R.array.segmentos);
        segmentosOriginais = new ArrayList<>(Arrays.asList(segmentosArray));

        if (segmentosOriginais.isEmpty()) {
            mostrarLoading(true);
            return;
        }

        Collections.sort(segmentosOriginais);
        mostrarLoading(false);
        adapter.updateData(segmentosOriginais);


        recuperarSegmentoSalvo();
        if (segmentoEscolhido == null || segmentoEscolhido.isEmpty()) {
            segmentoEscolhido = segmentosOriginais.get(0);
            salvarSegmento(segmentoEscolhido);
        }
    }

    public void aplicarFiltro(String texto) {
        if (adapter == null || segmentosOriginais == null) return;

        String filtro = texto.toLowerCase().trim();
        if (filtro.isEmpty()) {
            adapter.updateData(segmentosOriginais);
            return;
        }

        List<String> listaFiltrada = new ArrayList<>();
        for (String segmento : segmentosOriginais) {
            if (segmento.toLowerCase().contains(filtro)) {
                listaFiltrada.add(segmento);
            }
        }
        adapter.updateData(listaFiltrada);
    }

    private void mostrarLoading(boolean mostrar) {
        if (loadingLayout == null || recyclerView == null) return;

        loadingLayout.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(mostrar ? View.GONE : View.VISIBLE);
    }

    private void salvarSegmento(String segmento) {
        if (getContext() != null) {
            getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_SEGMENTO_ESCOLHIDO, segmento)
                    .apply();
        }
    }

    private void recuperarSegmentoSalvo() {
        if (getContext() != null) {
            segmentoEscolhido = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getString(KEY_SEGMENTO_ESCOLHIDO, null);
        }
    }

    public String getSegmentoEscolhido() {
        return segmentoEscolhido;
    }
}
