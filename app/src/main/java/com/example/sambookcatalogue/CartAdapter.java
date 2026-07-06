package com.example.sambookcatalogue;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sambookcatalogue.databinding.ItemCartBookBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartQuantityChangeListener {
        void onIncrease(Book book);
        void onDecrease(Book book);
        void onDelete(Book book);
    }

    private final List<Book> bookList = new ArrayList<>();
    private final Map<Book, Integer> cartMap;
    private final OnCartQuantityChangeListener listener;

    public CartAdapter(Map<Book, Integer> cartMap, OnCartQuantityChangeListener listener) {
        this.cartMap = cartMap;
        this.listener = listener;
        updateData();
    }

    public void updateData() {
        bookList.clear();
        bookList.addAll(cartMap.keySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBookBinding binding = ItemCartBookBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Book book = bookList.get(position);
        int quantity = cartMap.getOrDefault(book, 0);
        holder.bind(book, quantity, listener);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBookBinding binding;

        public CartViewHolder(ItemCartBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Book book, int quantity, OnCartQuantityChangeListener listener) {
            binding.ivCartBookCover.setImageResource(book.getCoverResourceId());
            binding.tvCartBookTitle.setText(book.getTitle());
            binding.tvCartBookAuthor.setText(book.getAuthor());
            binding.tvCartBookPrice.setText(String.format(Locale.getDefault(), "$%.2f", book.getPrice()));
            binding.tvQuantity.setText(String.valueOf(quantity));

            binding.btnIncreaseQuantity.setOnClickListener(v -> listener.onIncrease(book));
            binding.btnDecreaseQuantity.setOnClickListener(v -> listener.onDecrease(book));
            binding.btnDeleteCartItem.setOnClickListener(v -> listener.onDelete(book));
        }
    }
}