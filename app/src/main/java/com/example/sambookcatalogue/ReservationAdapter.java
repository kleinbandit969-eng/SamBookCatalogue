package com.example.sambookcatalogue;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sambookcatalogue.databinding.ItemReservationBookBinding;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    public interface OnReservationCancelListener {
        void onCancel(Book book);
    }

    private final List<Book> reservations;
    private final OnReservationCancelListener listener;

    public ReservationAdapter(List<Book> reservations, OnReservationCancelListener listener) {
        this.reservations = reservations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReservationBookBinding binding = ItemReservationBookBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReservationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        holder.bind(reservations.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        private final ItemReservationBookBinding binding;

        public ReservationViewHolder(ItemReservationBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Book book, OnReservationCancelListener listener) {
            binding.ivResBookCover.setImageResource(book.getCoverResourceId());
            binding.tvResBookTitle.setText(book.getTitle());
            binding.tvResBookAuthor.setText(book.getAuthor());
            binding.btnCancelRes.setOnClickListener(v -> listener.onCancel(book));
        }
    }
}