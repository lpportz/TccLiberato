package com.example.ovep.telas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.ovep.R;
import com.example.ovep.fragments.FiltroBairroFragment;
import com.example.ovep.fragments.FiltroCidadeFragment;
import com.example.ovep.fragments.FiltroEstadoFragment;
import com.example.ovep.fragments.FiltroSegmentoFragment;
import com.example.ovep.fragments.HomeFragment;
import com.example.ovep.fragments.ListaEmpresasFragment;
import com.example.ovep.fragments.NavBarBottonFragment;

public class MainActivity extends AppCompatActivity implements FiltroBairroFragment.OnBairroSelecionadoListener {

    private EditText editSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.tela_main);

        configurarEdgeToEdge();
        inicializarNavBar();
        inicializarBarraBusca();
    }

    private void configurarEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void inicializarNavBar() {
        NavBarBottonFragment.setupNavigation(this);
    }

    private void inicializarBarraBusca() {
        editSearch = findViewById(R.id.edit_search);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltroFragmentAtivo(s.toString());
            }
        });
    }

    private void aplicarFiltroFragmentAtivo(String textoFiltro) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        if (fragment == null) return;

        if (fragment instanceof FiltroSegmentoFragment) {
            ((FiltroSegmentoFragment) fragment).aplicarFiltro(textoFiltro);
        } else if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).aplicarFiltro(textoFiltro);
        } else if (fragment instanceof FiltroEstadoFragment) {
            ((FiltroEstadoFragment) fragment).aplicarFiltro(textoFiltro);
        } else if (fragment instanceof FiltroCidadeFragment) {
            ((FiltroCidadeFragment) fragment).aplicarFiltro(textoFiltro);
        } else if (fragment instanceof ListaEmpresasFragment) {
            ListaEmpresasFragment listaFragment = (ListaEmpresasFragment) fragment;
            listaFragment.aplicarFiltro(
                    listaFragment.cidadeSelecionada,
                    listaFragment.bairroSelecionado,
                    listaFragment.segmentoSelecionado
            );
        }
    }

    public void carregarFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment);

        if (addToBackStack) transaction.addToBackStack(null);

        transaction.commit();

        atualizarVisibilidadeBusca(fragment);
        resetarBarraBusca(fragment);
    }

    public void atualizarVisibilidadeBusca(Fragment fragment) {
        if (editSearch == null) return;

        if (fragment instanceof FiltroSegmentoFragment
                || fragment instanceof FiltroEstadoFragment
                || fragment instanceof FiltroCidadeFragment) {

            editSearch.setVisibility(View.VISIBLE);
            editSearch.setText("");
            aplicarFiltroFragmentAtivo("");
        } else {
            editSearch.setText("");
            editSearch.setVisibility(View.GONE);
        }
    }

    private void resetarBarraBusca(Fragment fragment) {
        if (editSearch == null) return;

        editSearch.setText("");
        editSearch.clearFocus();

        if (fragment instanceof FiltroSegmentoFragment) {
            editSearch.setHint("Buscar segmento...");
        } else if (fragment instanceof FiltroEstadoFragment) {
            editSearch.setHint("Buscar estado...");
        } else if (fragment instanceof FiltroCidadeFragment) {
            editSearch.setHint("Buscar cidade...");
        } else if (fragment instanceof HomeFragment) {
            editSearch.setHint("Buscar segmento...");
        }
    }

    @Override
    public void onBairroSelecionado(String cidade, String bairro, String segmento) {
        ListaEmpresasFragment fragment = new ListaEmpresasFragment();
        Bundle args = new Bundle();
        args.putString("cidade", cidade);
        args.putString("bairro", bairro);
        args.putString("segmento", segmento);
        fragment.setArguments(args);

        carregarFragment(fragment, true);
    }
}
