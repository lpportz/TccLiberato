package com.example.ovep.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ovep.R;

public class HomeFragment extends Fragment {

    private FiltroSegmentoFragment filtroSegmentoFragment;
    private static final String TAG_FILTRO_SEGMENTO = "filtro_segmento";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recupera o fragmento filho usando tag
        filtroSegmentoFragment = (FiltroSegmentoFragment)
                getChildFragmentManager().findFragmentByTag(TAG_FILTRO_SEGMENTO);

        // Se não existe, cria e adiciona
        if (filtroSegmentoFragment == null) {
            filtroSegmentoFragment = new FiltroSegmentoFragment();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_home, filtroSegmentoFragment, TAG_FILTRO_SEGMENTO)
                    .commitNow(); // commitNow garante que o fragmento exista imediatamente
        }

        // Aplica o segmento salvo automaticamente, se existir
        String segmentoSalvo = filtroSegmentoFragment.getSegmentoEscolhido();
        if (segmentoSalvo != null) {
            filtroSegmentoFragment.aplicarFiltro(segmentoSalvo);
        }
    }

    /**
     * Método para receber o texto do filtro da barra de busca.
     */
    public void aplicarFiltro(String textoFiltro) {
        if (filtroSegmentoFragment != null) {
            filtroSegmentoFragment.aplicarFiltro(textoFiltro);
        }
    }

    /**
     * Retorna o segmento selecionado atualmente (ou null se nenhum).
     */
    public String getSegmentoAtual() {
        return filtroSegmentoFragment != null ? filtroSegmentoFragment.getSegmentoEscolhido() : null;
    }

}
