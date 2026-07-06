package com.example.sambookcatalogue;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sambookcatalogue.databinding.ItemBookBinding;
import java.util.List;
import java.util.Locale;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public interface OnBuyClickListener {
        void onBuyClick(Book book);
    }

    public interface OnReserveClickListener {
        void onReserveClick(Book book);
    }

    private final List<Book> books;
    private final OnBookClickListener listener;
    private OnBuyClickListener buyListener;
    private OnReserveClickListener reserveListener;

    public BookAdapter(List<Book> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    public BookAdapter(List<Book> books, OnBookClickListener listener, OnBuyClickListener buyListener, OnReserveClickListener reserveListener) {
        this.books = books;
        this.listener = listener;
        this.buyListener = buyListener;
        this.reserveListener = reserveListener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookBinding binding = ItemBookBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book, listener, buyListener, reserveListener);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookBinding binding;

        public BookViewHolder(ItemBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Book book, OnBookClickListener listener, OnBuyClickListener buyListener, OnReserveClickListener reserveListener) {
            binding.bookCover.setImageResource(book.getCoverResourceId());
            binding.bookTitleLabel.setText(book.getTitle());
            binding.bookAuthorLabel.setText(book.getAuthor());
            binding.bookPriceLabel.setText(String.format(Locale.getDefault(), "$%.2f", book.getPrice()));
            
            binding.getRoot().setOnClickListener(v -> listener.onBookClick(book));
            
            if (buyListener != null) {
                binding.btnBuy.setVisibility(android.view.View.VISIBLE);
                binding.btnBuy.setOnClickListener(v -> buyListener.onBuyClick(book));
            } else {
                binding.btnBuy.setVisibility(android.view.View.GONE);
            }

            if (reserveListener != null) {
                binding.btnReserve.setVisibility(android.view.View.VISIBLE);
                binding.btnReserve.setOnClickListener(v -> reserveListener.onReserveClick(book));
            } else {
                binding.btnReserve.setVisibility(android.view.View.GONE);
            }
        }
    }
}