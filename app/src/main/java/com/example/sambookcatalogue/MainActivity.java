package com.example.sambookcatalogue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sambookcatalogue.databinding.ActivityMainBinding;
import com.example.sambookcatalogue.databinding.DialogBookingDetailsBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BookingManager.OnDataChangedListener {

    private ActivityMainBinding binding;
    private final Handler slideshowHandler = new Handler(Looper.getMainLooper());
    private Runnable slideshowRunnable;
    private int currentPopularPosition = 0;
    private static final long SLIDESHOW_DELAY = 4000;

    private CartAdapter cartAdapter;
    private ReservationAdapter reservationAdapter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("SamBookPrefs", MODE_PRIVATE);
        applySavedTheme();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupWindowInsets();
        setupRecyclerViews();
        setupListeners();
        setupSlideshow();
        setupBookingAdapters();
        setupSettingsSection();
        
        BookingManager.getInstance().addListener(this);
    }

    private void applySavedTheme() {
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BookingManager.getInstance().removeListener(this);
    }

    @Override
    public void onDataChanged() {
        if (cartAdapter != null) cartAdapter.updateData();
        if (reservationAdapter != null) {
            reservationAdapter.notifyDataSetChanged();
        }
        updateEmptyStates();
        calculateTotal();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    private void setupRecyclerViews() {
        BookingManager manager = BookingManager.getInstance();
        
        // --- Explore Section ---
        binding.rvSection2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvSection2.setAdapter(new BookAdapter(getMockBooks("Popular"), this::openBookDetails, manager::addToCart, this::initiateReservation));
        new PagerSnapHelper().attachToRecyclerView(binding.rvSection2);

        binding.rvSection1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvSection1.setAdapter(new BookAdapter(getOutstandingCategories(), category -> openCategory(category.getTitle())));

        binding.rvSection3.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvSection3.setAdapter(new BookAdapter(getMockBooks("New Arrival"), this::openBookDetails, manager::addToCart, this::initiateReservation));

        // --- My Library Section ---
        setupCategoryRecyclerView(binding.rvLibrarySciFi, "Sci-Fi & Fantasy");
        setupCategoryRecyclerView(binding.rvLibraryMystery, "Mystery & Thriller");
        setupCategoryRecyclerView(binding.rvLibraryBiographies, "Biographies");
        setupCategoryRecyclerView(binding.rvLibraryHistorical, "Historical Fiction");
        setupCategoryRecyclerView(binding.rvLibraryBusiness, "Business & Finance");
        setupCategoryRecyclerView(binding.rvLibraryPersonal, "Personal Growth");
    }

    private void setupCategoryRecyclerView(RecyclerView recyclerView, String category) {
        if (recyclerView == null) return;
        BookingManager manager = BookingManager.getInstance();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new BookAdapter(getMockBooks(category), this::openBookDetails, manager::addToCart, this::initiateReservation));
    }

    private void initiateReservation(Book book) {
        showBookingDialog(false, book);
    }

    private void setupBookingAdapters() {
        BookingManager manager = BookingManager.getInstance();
        
        // Setup modern Cart Adapter with quantity logic
        cartAdapter = new CartAdapter(manager.getCartItems(), new CartAdapter.OnCartQuantityChangeListener() {
            @Override
            public void onIncrease(Book book) {
                manager.addToCart(book);
            }

            @Override
            public void onDecrease(Book book) {
                manager.removeFromCart(book);
            }

            @Override
            public void onDelete(Book book) {
                manager.deleteFromCart(book);
            }
        });
        binding.rvCart.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCart.setAdapter(cartAdapter);

        // Setup modern Reservation Adapter
        reservationAdapter = new ReservationAdapter(manager.getReservationItems(), manager::removeReservation);
        binding.rvReservations.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReservations.setAdapter(reservationAdapter);
        
        updateEmptyStates();
        calculateTotal();
    }

    private void updateEmptyStates() {
        boolean cartEmpty = BookingManager.getInstance().getCartItems().isEmpty();
        binding.tvEmptyCart.setVisibility(cartEmpty ? View.VISIBLE : View.GONE);
        // Using checkoutContainer as it is defined in activity_main.xml
        binding.checkoutContainer.setVisibility(cartEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyReservations.setVisibility(BookingManager.getInstance().getReservationItems().isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void calculateTotal() {
        double total = 0;
        for (Map.Entry<Book, Integer> entry : BookingManager.getInstance().getCartItems().entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        binding.tvTotalPrice.setText(String.format(Locale.getDefault(), "$%.2f", total));
    }

    private void openCategory(String categoryName) {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra("category_name", categoryName);
        startActivity(intent);
    }

    private void setupSlideshow() {
        slideshowRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding.sectionExplore.getVisibility() == View.VISIBLE) {
                    RecyclerView.Adapter<?> adapter = binding.rvSection2.getAdapter();
                    if (adapter != null && adapter.getItemCount() > 0) {
                        currentPopularPosition = (currentPopularPosition + 1) % adapter.getItemCount();
                        binding.rvSection2.smoothScrollToPosition(currentPopularPosition);
                    }
                }
                slideshowHandler.postDelayed(this, SLIDESHOW_DELAY);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        slideshowHandler.postDelayed(slideshowRunnable, SLIDESHOW_DELAY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        slideshowHandler.removeCallbacks(slideshowRunnable);
    }

    private void setupListeners() {
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_search) {
                SearchView searchView = (SearchView) item.getActionView();
                if (searchView != null) {
                    searchView.setQueryHint("Search library...");
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            Toast.makeText(MainActivity.this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
                            item.collapseActionView();
                            return true;
                        }
                        @Override
                        public boolean onQueryTextChange(String newText) { return false; }
                    });
                }
                return true;
            }
            return false;
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_catalog) showSection(binding.sectionExplore, "Discover");
            else if (itemId == R.id.nav_my_books) showSection(binding.sectionLibrary, "My Library");
            else if (itemId == R.id.nav_reservations) showSection(binding.sectionBookings, "My Bookings");
            else if (itemId == R.id.nav_settings) showSection(binding.sectionOptions, "Settings");
            return true;
        });

        binding.btnCheckout.setOnClickListener(v -> showBookingDialog(true, null));
    }

    private void setupSettingsSection() {
        // Load initial state from Preferences
        binding.switchDarkMode.setChecked(prefs.getBoolean("dark_mode", false));
        binding.switchNotifications.setChecked(prefs.getBoolean("notifications", true));
        binding.tvUserName.setText(prefs.getString("user_name", "Samuel Njuguna"));
        binding.tvUserEmail.setText(prefs.getString("user_email", "samuel@example.com"));
        binding.tvCurrentLanguage.setText(prefs.getString("language", "English"));

        // Dark Mode Toggle logic with persistence
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Notifications Toggle with persistence
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications", isChecked).apply();
            String status = isChecked ? "enabled" : "disabled";
            Toast.makeText(this, "Notifications " + status, Toast.LENGTH_SHORT).show();
        });

        // Edit Profile logic
        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Language selection dialog
        binding.itemLanguage.setOnClickListener(v -> showLanguageDialog());

        // Help Center dialog
        binding.itemHelp.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Help Center")
                    .setMessage("For support, please contact us at support@sambook.com or visit our website.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        // About dialog
        binding.itemAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("About SamBook")
                    .setMessage("SamBook Catalogue v1.0\nModern book management for readers.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        // Logout logic with confirmation
        binding.btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etEmail = dialogView.findViewById(R.id.etEditEmail);

        etName.setText(binding.tvUserName.getText());
        etEmail.setText(binding.tvUserEmail.getText());

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newEmail = etEmail.getText().toString().trim();
                    if (!newName.isEmpty() && !newEmail.isEmpty()) {
                        prefs.edit().putString("user_name", newName).putString("user_email", newEmail).apply();
                        binding.tvUserName.setText(newName);
                        binding.tvUserEmail.setText(newEmail);
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Swahili", "French", "German", "Spanish"};
        new AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setItems(languages, (dialog, which) -> {
                    String selected = languages[which];
                    prefs.edit().putString("language", selected).apply();
                    binding.tvCurrentLanguage.setText(selected);
                    Toast.makeText(this, "Language changed to " + selected, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showBookingDialog(boolean isPurchase, Book book) {
        DialogBookingDetailsBinding dialogBinding = DialogBookingDetailsBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.TransparentDialog)
                .setView(dialogBinding.getRoot())
                .create();

        if (!isPurchase) {
            dialogBinding.tvDialogTitle.setText(R.string.title_reservations);
            dialogBinding.btnConfirmBooking.setText(R.string.btn_reserve_book);
            dialogBinding.tvPaymentLabel.setVisibility(View.GONE);
            dialogBinding.togglePaymentMethod.setVisibility(View.GONE);
            dialogBinding.layoutMpesa.setVisibility(View.GONE);
            dialogBinding.layoutPaypal.setVisibility(View.GONE);
        } else {
            dialogBinding.tvDialogTitle.setText(R.string.title_checkout_details);
            // Initial state: Shipping Info
            dialogBinding.tvDialogSubtitle.setText(R.string.step_shipping_details);
            dialogBinding.tvPaymentLabel.setVisibility(View.GONE);
            dialogBinding.togglePaymentMethod.setVisibility(View.GONE);
            dialogBinding.layoutMpesa.setVisibility(View.GONE);
            dialogBinding.layoutPaypal.setVisibility(View.GONE);
            dialogBinding.btnConfirmBooking.setText(R.string.btn_continue_to_payment);

            double total;
            if (book != null) total = book.getPrice();
            else {
                total = 0;
                for (Map.Entry<Book, Integer> entry : BookingManager.getInstance().getCartItems().entrySet()) {
                    total += entry.getKey().getPrice() * entry.getValue();
                }
            }
            dialogBinding.tvDialogTotal.setVisibility(View.VISIBLE);
            dialogBinding.tvDialogTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));

            // Payment method toggle logic
            dialogBinding.togglePaymentMethod.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.btnMpesa) {
                        dialogBinding.layoutMpesa.setVisibility(View.VISIBLE);
                        dialogBinding.layoutPaypal.setVisibility(View.GONE);
                    } else if (checkedId == R.id.btnPaypal) {
                        dialogBinding.layoutMpesa.setVisibility(View.GONE);
                        dialogBinding.layoutPaypal.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        dialogBinding.btnConfirmBooking.setOnClickListener(v -> {
            Editable nameEditable = dialogBinding.etName.getText();
            String name = (nameEditable != null) ? nameEditable.toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isPurchase) {
                if (dialogBinding.btnConfirmBooking.getText() != null && 
                    dialogBinding.btnConfirmBooking.getText().toString().equals(getString(R.string.btn_continue_to_payment))) {
                    // Switch to Payment Step
                    dialogBinding.tvDialogTitle.setText(R.string.title_payment_credentials);
                    dialogBinding.tvDialogSubtitle.setText(R.string.step_payment_details);
                    dialogBinding.tvPaymentLabel.setVisibility(View.VISIBLE);
                    dialogBinding.togglePaymentMethod.setVisibility(View.VISIBLE);
                    dialogBinding.layoutMpesa.setVisibility(View.VISIBLE);
                    dialogBinding.btnConfirmBooking.setText(R.string.btn_pay_and_complete);
                    return;
                }

                int checkedId = dialogBinding.togglePaymentMethod.getCheckedButtonId();
                if (checkedId == R.id.btnMpesa) {
                    Editable phoneEditable = dialogBinding.etMpesaPhone.getText();
                    String phone = (phoneEditable != null) ? phoneEditable.toString().trim() : "";
                    if (phone.isEmpty()) {
                        Toast.makeText(this, "Please enter M-Pesa phone number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, "STK Push sent to " + phone, Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.btnPaypal) {
                    Editable emailEditable = dialogBinding.etPaypalEmail.getText();
                    String email = (emailEditable != null) ? emailEditable.toString().trim() : "";
                    if (email.isEmpty()) {
                        Toast.makeText(this, "Please enter PayPal email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, "Redirecting to PayPal for " + email, Toast.LENGTH_SHORT).show();
                }
                
                BookingManager.getInstance().clearCart();
                Toast.makeText(this, "Order placed successfully! Thank you, " + name, Toast.LENGTH_LONG).show();
            } else {
                if (book != null) {
                    BookingManager.getInstance().addToReservations(book);
                    Toast.makeText(this, "Book \"" + book.getTitle() + "\" reserved for " + name, Toast.LENGTH_LONG).show();
                }
            }
            dialog.dismiss();
        });

        dialogBinding.btnCancelBooking.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showSection(View sectionToShow, String title) {
        if (sectionToShow.getVisibility() == View.VISIBLE) return;
        binding.toolbar.setTitle(title);
        View[] sections = {binding.sectionExplore, binding.sectionLibrary, binding.sectionBookings, binding.sectionOptions};
        for (View section : sections) {
            if (section.getVisibility() == View.VISIBLE) {
                section.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                    section.setVisibility(View.GONE);
                    sectionToShow.setVisibility(View.VISIBLE);
                    sectionToShow.setAlpha(0f);
                    sectionToShow.animate().alpha(1f).setDuration(200).start();
                }).start();
                return;
            }
        }
        sectionToShow.setVisibility(View.VISIBLE);
        sectionToShow.setAlpha(1f);
    }

    private void openBookDetails(Book book) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("book_title", book.getTitle());
        intent.putExtra("book_author", book.getAuthor());
        intent.putExtra("book_image", book.getCoverResourceId());
        intent.putExtra("book_description", book.getDescription());
        intent.putExtra("book_price", book.getPrice());
        startActivity(intent);
    }

    private List<Book> getOutstandingCategories() {
        List<Book> categories = new ArrayList<>();
        categories.add(new Book("Sci-Fi & Fantasy", "Genre", R.drawable.s1, "Futuristic technology and alien worlds.", 0.0));
        categories.add(new Book("Mystery & Thriller", "Genre", R.drawable.m1, "Suspense and hidden secrets.", 0.0));
        categories.add(new Book("Biographies", "Genre", R.drawable.b1, "Remarkable life stories.", 0.0));
        categories.add(new Book("Historical Fiction", "Genre", R.drawable.h1, "Rich, immersive history.", 0.0));
        categories.add(new Book("Business & Finance", "Genre", R.drawable.f1, "Success strategies and wealth.", 0.0));
        categories.add(new Book("Personal Growth", "Genre", R.drawable.p1, "Wisdom for a better life.", 0.0));
        return categories;
    }

    private List<Book> getMockBooks(String category) {
        List<Book> books = new ArrayList<>();

        if (category.equals("Popular")) {
            books.add(new Book("The Silent Patient", "Alex Michaelides", R.drawable.m1, "A shocking psychological thriller.", 15.99));
            books.add(new Book("The Martian", "Andy Weir", R.drawable.s3, "Survival story on Mars.", 12.50));
            books.add(new Book("Becoming", "Michelle Obama", R.drawable.b2, "Deeply personal memoir.", 18.00));
            books.add(new Book("Where the Crawdads Sing", "Delia Owens", R.drawable.h4, "A haunting mystery.", 14.25));
            books.add(new Book("Shoe Dog", "Phil Knight", R.drawable.f4, "Creator of Nike candid memoir.", 16.50));
            books.add(new Book("The Alchemist", "Paulo Coelho", R.drawable.p1, "Fable about following your heart.", 10.99));
            return books;
        }

        int[] drawables;
        String[] titles;
        String[] authors;
        String[] descriptions;
        double[] prices;

        if (category.equals("New Arrival")) {
            drawables = new int[]{R.drawable.s4, R.drawable.m3, R.drawable.h2, R.drawable.p1, R.drawable.s1, R.drawable.f2, R.drawable.h3, R.drawable.s2, R.drawable.h4, R.drawable.m2};
            titles = new String[]{"The Starless Sea", "The Maid", "Cloud Cuckoo Land", "Four Thousand Weeks", "Sea of Tranquility", "Trust", "Lessons in Chemistry", "Tomorrow, and Tomorrow, and Tomorrow", "Demon Copperhead", "The Night Circus"};
            authors = new String[]{"Erin Morgenstern", "Nita Prose", "Anthony Doerr", "Oliver Burkeman", "Emily St. John Mandel", "Hernan Diaz", "Bonnie Garmus", "Gabrielle Zevin", "Barbara Kingsolver", "Erin Morgenstern"};
            descriptions = new String[]{"Timeless love story.", "Eccentric maid mystery.", "Interlocking stories.", "Time management guide.", "Novel of art and time travel.", "Fortune and greed.", "Chemistry and cooking.", "Game designers story.", "Modern David Copperfield.", "Magic circus competition."};
            prices = new double[]{19.99, 14.50, 17.00, 13.99, 16.75, 15.00, 14.00, 16.00, 17.50, 13.25};
        } else if (category.contains("Sci-Fi")) {
            drawables = new int[]{R.drawable.s1, R.drawable.s2, R.drawable.s3, R.drawable.s4};
            titles = new String[]{"Hyperion", "Dune", "The Martian", "Foundation"};
            authors = new String[]{"Dan Simmons", "Frank Herbert", "Andy Weir", "Isaac Asimov"};
            descriptions = new String[]{"Galactic warfare masterpiece.", "Epic desert planet survival.", "Astronaut stranded on Mars.", "Fall of a galactic empire."};
            prices = new double[]{12.99, 15.00, 12.50, 11.99};
        } else if (category.contains("Mystery")) {
            drawables = new int[]{R.drawable.m1, R.drawable.m2, R.drawable.m3, R.drawable.m4};
            titles = new String[]{"The Shadow of the Wind", "The Girl on the Train", "Gone Girl", "The Guest List"};
            authors = new String[]{"Carlos Ruiz Zafón", "Paula Hawkins", "Gillian Flynn", "Lucy Foley"};
            descriptions = new String[]{"Secret library mystery.", "Suspicious witness on a train.", "Wife disappears on anniversary.", "Remote island wedding mystery."};
            prices = new double[]{14.99, 9.99, 11.50, 13.00};
        } else if (category.contains("Historical")) {
            drawables = new int[]{R.drawable.h1, R.drawable.h2, R.drawable.h3, R.drawable.h4};
            titles = new String[]{"Circe", "All the Light We Cannot See", "The Nightingale", "Where the Crawdads Sing"};
            authors = new String[]{"Madeline Miller", "Anthony Doerr", "Kristin Hannah", "Delia Owens"};
            descriptions = new String[]{"Retelling of the goddess's life.", "Blind French girl in WWII.", "Two sisters in occupied France.", "Marsh mystery."};
            prices = new double[]{15.50, 16.00, 14.00, 14.25};
        } else if (category.contains("Biographies")) {
            drawables = new int[]{R.drawable.b1, R.drawable.b2, R.drawable.b3, R.drawable.b4};
            titles = new String[]{"Leonardo da Vinci", "Becoming", "Educated", "The Diary of Anne Frank"};
            authors = new String[]{"Walter Isaacson", "Michelle Obama", "Tara Westover", "Anne Frank"};
            descriptions = new String[]{"World's most creative genius.", "Michelle Obama's memoir.", "Leaving survivalist family.", "Timeless diary in hiding."};
            prices = new double[]{22.00, 18.00, 13.50, 9.00};
        } else if (category.contains("Business")) {
            drawables = new int[]{R.drawable.f1, R.drawable.f2, R.drawable.f3, R.drawable.f4};
            titles = new String[]{"The Psychology of Money", "The Intelligent Investor", "Thinking, Fast and Slow", "Shoe Dog"};
            authors = new String[]{"Morgan Housel", "Benjamin Graham", "Daniel Kahneman", "Phil Knight"};
            descriptions = new String[]{"Wealth lessons.", "Classic value investing.", "How we think.", "Nike memoir."};
            prices = new double[]{16.99, 20.00, 15.00, 16.50};
        } else if (category.contains("Personal")) {
            drawables = new int[]{R.drawable.p1, R.drawable.p2, R.drawable.p3};
            titles = new String[]{"The Mountain Is You", "Man's Search for Meaning", "Deep Work"};
            authors = new String[]{"Brianna Wiest", "Viktor Frankl", "Cal Newport"};
            descriptions = new String[]{"Self-mastery.", "Meaning in concentration camps.", "Focused success."};
            prices = new double[]{14.99, 10.50, 12.00};
        } else {
            drawables = new int[]{R.drawable.s1, R.drawable.m1, R.drawable.b1, R.drawable.h1};
            titles = new String[]{category + " Best-Seller", category + " Masterclass", category + " Essentials", category + " Volume II"};
            authors = new String[]{"Unknown", "Expert", "Various", "Staff"};
            descriptions = new String[]{"A must-read selection.", "Deep dive into the category.", "Fundamental principles.", "New insights."};
            prices = new double[]{15.00, 15.00, 15.00, 15.00};
        }

        for (int i = 0; i < drawables.length; i++) {
            books.add(new Book(titles[i], authors[i], drawables[i], descriptions[i], prices[i]));
        }
        return books;
    }
}