package com.example.ovep.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ovep.R;
import com.example.ovep.adapters.EmpresaAdapter;
import com.example.ovep.firebase.FirebaseHelper;
import com.example.ovep.models.Empresa;
import com.example.ovep.telas.EmpresaApresentacaoActivity;
import com.example.ovep.telas.TelaSemConteudoActivity;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListaEmpresasFragment extends Fragment {

    private static final String TAG = "ListaEmpresasFragment";

    private EmpresaAdapter adapter;
    private final List<Empresa> listaEmpresas = new ArrayList<>();
    private FirebaseHelper firebaseHelper;

    private View loadingLayout;
    private RecyclerView recyclerView;
    private View errorLayout;

    public String cidadeSelecionada = "";
    public String bairroSelecionado = "";
    public String segmentoSelecionado = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_lista_empresas, container, false);

        loadingLayout = root.findViewById(R.id.loadingLayout);
        recyclerView = root.findViewById(R.id.recyclerViewEmpresas);
        errorLayout = root.findViewById(R.id.errorLayout);

        root.findViewById(R.id.btnTentarNovamente).setOnClickListener(v -> recarregarEmpresas());
        root.findViewById(R.id.btnVoltar).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        firebaseHelper = new FirebaseHelper();

        adapter = new EmpresaAdapter(listaEmpresas, new EmpresaAdapter.OnItemClickListener() {
            @Override
            public void onEmpresaClick(Empresa empresa) {
                Intent intent = new Intent(getActivity(), EmpresaApresentacaoActivity.class);
                intent.putExtra("empresa", empresa);
                startActivity(intent);
            }

            @Override
            public void onEditarClick(Empresa empresa) {
                Toast.makeText(getContext(), "Editar: " + empresa.getNome(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRemoverClick(Empresa empresa) {
                Toast.makeText(getContext(), "Remover: " + empresa.getNome(), Toast.LENGTH_SHORT).show();
            }
        }, isModoVisualizacao());

        recyclerView.setAdapter(adapter);

        carregarArgumentos();
        recarregarEmpresas();
    }

    private boolean isModoVisualizacao() {
        return getArguments() != null && getArguments().getString("cidade") != null;
    }

    /** Recupera os argumentos do bundle e mantém em variáveis de instância */
    private void carregarArgumentos() {
        Bundle args = getArguments();
        if (args != null) {
            cidadeSelecionada = args.getString("cidade", "").toUpperCase();
            bairroSelecionado = args.getString("bairro", "");
            segmentoSelecionado = args.getString("segmento", "");

            Log.d(TAG, "Cidade: " + cidadeSelecionada + ", Bairro: " + bairroSelecionado +
                    ", Segmento: " + segmentoSelecionado);

            Toast.makeText(getContext(),
                    "Segmento recebido: " + (segmentoSelecionado.isEmpty() ? "Todos" : segmentoSelecionado),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Recarrega empresas usando os filtros atuais */
    private void recarregarEmpresas() {
        if (cidadeSelecionada.isEmpty() || bairroSelecionado.isEmpty()) {
            Toast.makeText(getContext(), "Cidade ou bairro inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        mostrarLoading();
        carregarEmpresas();
    }

    /** Carrega empresas filtrando pelo segmento armazenado */
    private void carregarEmpresas() {
        Log.d(TAG, "Buscando empresas para cidade='" + cidadeSelecionada + "', bairro='" + bairroSelecionado +
                "', segmento='" + segmentoSelecionado + "'");

        Toast.makeText(getContext(),
                "Carregando empresas para bairro: " + bairroSelecionado +
                        " | segmento: " + (segmentoSelecionado.isEmpty() ? "Todos" : segmentoSelecionado),
                Toast.LENGTH_SHORT).show();

        firebaseHelper.buscarEmpresasPorBairro(cidadeSelecionada, bairroSelecionado, segmentoSelecionado,
                new FirebaseHelper.EmpresaListener() {
                    @Override
                    public void onUpdate(List<QueryDocumentSnapshot> documentos) {
                        if (!isAdded()) return;

                        listaEmpresas.clear();
                        for (QueryDocumentSnapshot doc : documentos) {
                            Empresa empresa = doc.toObject(Empresa.class);
                            if (segmentoSelecionado.isEmpty() || segmentoSelecionado.equalsIgnoreCase(empresa.getSegmento())) {
                                listaEmpresas.add(empresa);
                            }
                        }

                        requireActivity().runOnUiThread(() -> {
                            if (listaEmpresas.isEmpty()) {
                                abrirTelaSemConteudo("Nenhuma empresa encontrada para o bairro '" +
                                        bairroSelecionado + "' com segmento '" +
                                        (segmentoSelecionado.isEmpty() ? "Todos" : segmentoSelecionado) +
                                        "' em " + cidadeSelecionada);
                            } else {
                                adapter.notifyDataSetChanged();
                                mostrarConteudo();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Erro ao buscar empresas", e);
                        if (isAdded()) requireActivity().runOnUiThread(ListaEmpresasFragment.this::mostrarErroConexao);
                    }
                });
    }

    private void abrirTelaSemConteudo(String mensagem) {
        if (!isAdded()) return;
        Intent intent = new Intent(getActivity(), TelaSemConteudoActivity.class);
        intent.putExtra("mensagem", mensagem);
        startActivity(intent);

        if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
    }

    private void mostrarLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

    private void mostrarConteudo() {
        loadingLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
    }

    private void mostrarErroConexao() {
        loadingLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firebaseHelper != null) firebaseHelper.removerTodosListeners();
    }

    public void aplicarFiltro(String cidade, String bairro, String segmento) {
        cidadeSelecionada = cidade != null ? cidade.toUpperCase() : "";
        bairroSelecionado = bairro != null ? bairro : "";
        segmentoSelecionado = segmento != null ? segmento : "";

        Toast.makeText(getContext(),
                "Aplicando filtro: " + cidadeSelecionada + " | " +
                        bairroSelecionado + " | " +
                        (segmentoSelecionado.isEmpty() ? "Todos" : segmentoSelecionado),
                Toast.LENGTH_SHORT).show();

        recarregarEmpresas();
    }
}
