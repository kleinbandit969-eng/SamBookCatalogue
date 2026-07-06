package com.example.sambookcatalogue;

import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.sambookcatalogue.databinding.ActivityCategoryBinding;
import com.example.sambookcatalogue.databinding.DialogBookingDetailsBinding;
import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private ActivityCategoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String categoryName = getIntent().getStringExtra("category_name");
        binding.categoryToolbar.setTitle(categoryName);
        binding.categoryToolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView(categoryName);
    }

    private void setupRecyclerView(String category) {
        BookingManager manager = BookingManager.getInstance();
        binding.rvCategoryBooks.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvCategoryBooks.setAdapter(new BookAdapter(getMockBooks(category), this::openBookDetails, 
            book -> {
                manager.addToCart(book);
                Toast.makeText(this, book.getTitle() + " added to cart", Toast.LENGTH_SHORT).show();
            },
            book -> showBookingDialog(false, book)
        ));
    }

    private void showBookingDialog(boolean isPurchase, Book book) {
        DialogBookingDetailsBinding dialogBinding = DialogBookingDetailsBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.TransparentDialog)
                .setView(dialogBinding.getRoot())
                .create();

        if (!isPurchase) {
            dialogBinding.tvDialogTitle.setText("Reservation Details");
            dialogBinding.btnConfirmBooking.setText("Reserve Book");
        }

        dialogBinding.btnConfirmBooking.setOnClickListener(v -> {
            String name = dialogBinding.etName.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (isPurchase) {
                Toast.makeText(this, "Purchase complete for: " + book.getTitle() + ". Thank you, " + name, Toast.LENGTH_LONG).show();
            } else {
                BookingManager.getInstance().addToReservations(book);
                Toast.makeText(this, "Book \"" + book.getTitle() + "\" reserved for " + name, Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        });

        dialogBinding.btnCancelBooking.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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

    private List<Book> getMockBooks(String category) {
        List<Book> books = new ArrayList<>();
        int[] drawables;
        String[] titles;
        String[] authors;
        String[] descriptions;
        double[] prices;

        if (category.contains("Sci-Fi")) {
            drawables = new int[]{R.drawable.s1, R.drawable.s2, R.drawable.s3, R.drawable.s4};
            titles = new String[]{"Hyperion", "Dune", "The Martian", "Foundation"};
            authors = new String[]{"Dan Simmons", "Frank Herbert", "Andy Weir", "Isaac Asimov"};
            descriptions = new String[]{
                "A stunning masterpiece of galactic warfare and time-bending mystery.",
                "An epic of politics and survival on the desert planet Arrakis.",
                "Survival story of an astronaut stranded on Mars.",
                "A mathematician predicts the fall of a galactic empire."
            };
            prices = new double[]{12.99, 15.00, 12.50, 11.99};
        } else if (category.contains("Mystery")) {
            drawables = new int[]{R.drawable.m1, R.drawable.m2, R.drawable.m3, R.drawable.m4};
            titles = new String[]{"The Shadow of the Wind", "The Girl on the Train", "Gone Girl", "The Guest List"};
            authors = new String[]{"Carlos Ruiz Zafón", "Paula Hawkins", "Gillian Flynn", "Lucy Foley"};
            descriptions = new String[]{
                "A gothic mystery set in post-war Barcelona about a secret library of forgotten books.",
                "A woman witnesses something suspicious from a train window.",
                "A wife disappears on her anniversary.",
                "A wedding on a remote island turns deadly."
            };
            prices = new double[]{14.99, 9.99, 11.50, 13.00};
        } else if (category.contains("Historical")) {
            drawables = new int[]{R.drawable.h1, R.drawable.h2, R.drawable.h3, R.drawable.h4};
            titles = new String[]{"Circe", "All the Light We Cannot See", "The Nightingale", "Where the Crawdads Sing"};
            authors = new String[]{"Madeline Miller", "Anthony Doerr", "Kristin Hannah", "Delia Owens"};
            descriptions = new String[]{
                "A bold and subversive retelling of the goddess's life.",
                "A blind French girl and a German boy during WWII.",
                "Two sisters in France struggle to survive occupation.",
                "A mystery set in the North Carolina marshes."
            };
            prices = new double[]{15.50, 16.00, 14.00, 14.25};
        } else if (category.contains("Biographies")) {
            drawables = new int[]{R.drawable.b1, R.drawable.b2, R.drawable.b3, R.drawable.b4};
            titles = new String[]{"Leonardo da Vinci", "Becoming", "Educated", "The Diary of Anne Frank"};
            authors = new String[]{"Walter Isaacson", "Michelle Obama", "Tara Westover", "Anne Frank"};
            descriptions = new String[]{
                "A majestic account of the world's most creative genius.",
                "The personal memoir of Michelle Obama.",
                "A woman leaves her survivalist family for education.",
                "The timeless diary of a young girl in hiding."
            };
            prices = new double[]{22.00, 18.00, 13.50, 9.00};
        } else if (category.contains("Business")) {
            drawables = new int[]{R.drawable.f1, R.drawable.f2, R.drawable.f3, R.drawable.f4};
            titles = new String[]{"The Psychology of Money", "The Intelligent Investor", "Thinking, Fast and Slow", "Shoe Dog"};
            authors = new String[]{"Morgan Housel", "Benjamin Graham", "Daniel Kahneman", "Phil Knight"};
            descriptions = new String[]{
                "Timeless lessons on wealth, greed, and happiness.",
                "The classic book on value investing.",
                "Exploration of the systems that drive how we think.",
                "A memoir by the creator of Nike."
            };
            prices = new double[]{16.99, 20.00, 15.00, 16.50};
        } else if (category.contains("Personal")) {
            drawables = new int[]{R.drawable.p1, R.drawable.p2, R.drawable.p3};
            titles = new String[]{"The Mountain Is You", "Man's Search for Meaning", "Deep Work"};
            authors = new String[]{"Brianna Wiest", "Viktor Frankl", "Cal Newport"};
            descriptions = new String[]{
                "Transforming self-sabotage into self-mastery.",
                "A psychiatrist's memoir of his experiences in Nazi concentration camps.",
                "Rules for focused success in a distracted world."
            };
            prices = new double[]{14.99, 10.50, 12.00};
        } else {
            drawables = new int[]{R.drawable.s1, R.drawable.m1, R.drawable.b1, R.drawable.h1};
            titles = new String[]{category + " Best-Seller", category + " Masterclass", category + " Essentials", category + " Volume II"};
            authors = new String[]{"Unknown", "Expert", "Various", "Staff"};
            descriptions = new String[]{"A must-read selection.", "Deep dive into the category.", "Fundamental principles.", "New insights."};
            prices = new double[]{15.00, 15.00, 15.00, 15.00};
        }

        for (int i = 0; i < Math.min(drawables.length, titles.length); i++) {
            books.add(new Book(titles[i], authors[i], drawables[i], descriptions[i], prices[i]));
        }
        return books;
    }
}