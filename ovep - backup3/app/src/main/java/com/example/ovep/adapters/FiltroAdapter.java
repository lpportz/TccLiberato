package com.example.ovep.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ovep.R;
import com.google.android.material.button.MaterialButton;
import com.example.ovep.util.Mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Adapter genérico para exibir uma lista de itens em forma de botões.
 * @param <T> Tipo do dado exibido
 */
public class FiltroAdapter<T> extends RecyclerView.Adapter<FiltroAdapter.FiltroViewHolder<T>> {

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }

    private final List<T> itens;
    private final OnItemClickListener<T> listenerClique;
    private final Mapper<T, String> mapperTexto;
    private final Comparator<T> comparador;

    public FiltroAdapter(List<T> itens,
                         OnItemClickListener<T> listenerClique,
                         Comparator<T> comparador,
                         Mapper<T, String> mapperTexto) {
        this.itens = new ArrayList<>(itens != null ? itens : Collections.emptyList());
        this.listenerClique = listenerClique;
        this.comparador = comparador;
        this.mapperTexto = mapperTexto;
        ordenarItens();
    }

    private void ordenarItens() {
        if (comparador != null) {
            Collections.sort(itens, comparador);
        }
    }

    @NonNull
    @Override
    public FiltroViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filtro, parent, false);
        return new FiltroViewHolder<>(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FiltroViewHolder<T> holder, int position) {
        T item = itens.get(position);
        holder.botao.setText(mapperTexto.map(item));
        holder.botao.setOnClickListener(v -> {
            if (listenerClique != null) {
                listenerClique.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    /**
     * Atualiza a lista de itens exibidos, ordena e notifica o adapter para atualizar a UI.
     * @param novosItens nova lista de itens (pode ser null para limpar a lista)
     */
    public void updateData(List<T> novosItens) {
        itens.clear();
        if (novosItens != null) {
            itens.addAll(novosItens);
        }
        ordenarItens();
        notifyDataSetChanged();
    }

    public static class FiltroViewHolder<T> extends RecyclerView.ViewHolder {
        public final MaterialButton botao;

        public FiltroViewHolder(@NonNull View itemView) {
            super(itemView);
            botao = itemView.findViewById(R.id.btnFiltro);
        }
    }
}
