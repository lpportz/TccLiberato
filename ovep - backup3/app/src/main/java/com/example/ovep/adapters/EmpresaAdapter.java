package com.example.ovep.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ovep.R;
import com.example.ovep.models.Empresa;

import java.util.List;

public class EmpresaAdapter extends RecyclerView.Adapter<EmpresaAdapter.EmpresaViewHolder> {

    public interface OnItemClickListener {
        void onEmpresaClick(Empresa empresa);
        void onEditarClick(Empresa empresa);
        void onRemoverClick(Empresa empresa);
    }

    private final List<Empresa> empresas;
    private final OnItemClickListener listener;
    private final boolean isModoVisualizacao;

    public EmpresaAdapter(List<Empresa> empresas, OnItemClickListener listener, boolean isModoVisualizacao) {
        this.empresas = empresas;
        this.listener = listener;
        this.isModoVisualizacao = isModoVisualizacao;
    }

    @NonNull
    @Override
    public EmpresaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_empresa, parent, false);
        return new EmpresaViewHolder(view, isModoVisualizacao);
    }

    @Override
    public void onBindViewHolder(@NonNull EmpresaViewHolder holder, int position) {
        holder.bind(empresas.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return empresas.size();
    }

    public static class EmpresaViewHolder extends RecyclerView.ViewHolder {
        private final Button btnEmpresa;
        private final TextView textSegmento;
        private final Button btnEditar;
        private final Button btnRemover;
        private final boolean isModoVisualizacao;

        public EmpresaViewHolder(@NonNull View itemView, boolean isModoVisualizacao) {
            super(itemView);
            this.isModoVisualizacao = isModoVisualizacao;

            btnEmpresa = itemView.findViewById(R.id.btnEmpresa);
            textSegmento = itemView.findViewById(R.id.textSegmento);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnRemover = itemView.findViewById(R.id.btnRemover);

            if (isModoVisualizacao) {
                btnEditar.setVisibility(View.GONE);
                btnRemover.setVisibility(View.GONE);
            }
        }

        public void bind(@NonNull Empresa empresa, @NonNull OnItemClickListener listener) {
            btnEmpresa.setText(empresa.getNome());
            textSegmento.setText(empresa.getSegmento());

            btnEmpresa.setOnClickListener(v -> listener.onEmpresaClick(empresa));

            if (!isModoVisualizacao) {
                btnEditar.setOnClickListener(v -> listener.onEditarClick(empresa));
                btnRemover.setOnClickListener(v -> listener.onRemoverClick(empresa));
            }
        }
    }
}
