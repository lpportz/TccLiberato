package com.example.ovep.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ovep.R;
import com.example.ovep.models.Comentario;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {

    public interface ComentarioClickListener {
        void onEditar(Comentario comentario);
        void onExcluir(Comentario comentario);
    }

    private final List<Comentario> listaComentarios;
    private final ComentarioClickListener listener;
    private final SimpleDateFormat sdf;
    private final String currentUserId;

    public ComentarioAdapter(List<Comentario> listaComentarios, String currentUserId, ComentarioClickListener listener) {
        this.listaComentarios = listaComentarios;
        this.currentUserId = currentUserId;
        this.listener = listener;
        this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentario comentario = listaComentarios.get(position);

        holder.textAutor.setText(comentario.getAutor());
        holder.textData.setText(comentario.getData() != null ? sdf.format(comentario.getData()) : "");
        holder.textComentario.setText(comentario.getTexto());

        boolean isCurrentUser = comentario.getAutor().equals(currentUserId);

        holder.btnEditar.setVisibility(isCurrentUser ? View.VISIBLE : View.GONE);
        holder.btnExcluir.setVisibility(isCurrentUser ? View.VISIBLE : View.GONE);

        if (isCurrentUser) {
            holder.btnEditar.setOnClickListener(v -> listener.onEditar(comentario));
            holder.btnExcluir.setOnClickListener(v -> listener.onExcluir(comentario));
        } else {
            holder.btnEditar.setOnClickListener(null);
            holder.btnExcluir.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return listaComentarios.size();
    }

    // Tornar a ViewHolder pública para resolver erro de visibilidade
    public static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        TextView textAutor, textData, textComentario;
        Button btnEditar, btnExcluir;

        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            textAutor = itemView.findViewById(R.id.textAutor);
            textData = itemView.findViewById(R.id.textData);
            textComentario = itemView.findViewById(R.id.textComentario);
            btnEditar = itemView.findViewById(R.id.btnEditarComentario);
            btnExcluir = itemView.findViewById(R.id.btnExcluirComentario);
        }
    }
}
