package ar.com.develup.tateti.adaptadores;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ar.com.develup.tateti.R;
import ar.com.develup.tateti.modelo.Partida;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by maribelmai on 26/3/17.
 */

public class AdaptadorPartidas extends RecyclerView.Adapter<AdaptadorPartidas.Holder> {

    private List<Partida> partidas = new ArrayList<>();

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_partida, null);
        return new Holder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {

        Partida partida = partidas.get(position);
        holder.idPartida.setText(partida.getId());
        holder.jugadores.setText(partida.getRetador());
    }

    @Override
    public int getItemCount() {
        return partidas.size();
    }

    public void agregarPartida(Partida partida) {

        if (!partidas.contains(partida)) {
            partidas.add(partida);
            notifyItemInserted(partidas.size() - 1);
        }
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.idPartida)
        TextView idPartida;
        @BindView(R.id.jugadores)
        TextView jugadores;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
