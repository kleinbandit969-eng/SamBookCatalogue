package com.example.sambookcatalogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingManager {
    private static BookingManager instance;
    private final Map<Book, Integer> cartItems = new HashMap<>();
    private final List<Book> reservationItems = new ArrayList<>();
    private final List<OnDataChangedListener> listeners = new ArrayList<>();

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    private BookingManager() {}

    public static synchronized BookingManager getInstance() {
        if (instance == null) {
            instance = new BookingManager();
        }
        return instance;
    }

    public void addToCart(Book book) {
        cartItems.put(book, cartItems.getOrDefault(book, 0) + 1);
        notifyListeners();
    }

    public void removeFromCart(Book book) {
        if (cartItems.containsKey(book)) {
            int count = cartItems.get(book);
            if (count > 1) {
                cartItems.put(book, count - 1);
            } else {
                cartItems.remove(book);
            }
            notifyListeners();
        }
    }

    public void deleteFromCart(Book book) {
        cartItems.remove(book);
        notifyListeners();
    }

    public void clearCart() {
        cartItems.clear();
        notifyListeners();
    }

    public void addToReservations(Book book) {
        if (!reservationItems.contains(book)) {
            reservationItems.add(book);
            notifyListeners();
        }
    }

    public void removeReservation(Book book) {
        if (reservationItems.remove(book)) {
            notifyListeners();
        }
    }

    public void clearReservations() {
        reservationItems.clear();
        notifyListeners();
    }

    public Map<Book, Integer> getCartItems() {
        return cartItems;
    }

    public List<Book> getReservationItems() {
        return reservationItems;
    }

    public void addListener(OnDataChangedListener listener) {
        listeners.add(listener);
    }

    public void removeListener(OnDataChangedListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (OnDataChangedListener listener : listeners) {
            listener.onDataChanged();
        }
    }
}