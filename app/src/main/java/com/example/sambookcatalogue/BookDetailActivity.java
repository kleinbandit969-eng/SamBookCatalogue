package com.example.sambookcatalogue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sambookcatalogue.databinding.ActivityBookDetailBinding;
import com.example.sambookcatalogue.databinding.DialogBookingDetailsBinding;
import java.util.Locale;

public class BookDetailActivity extends AppCompatActivity {

    private ActivityBookDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupView();
    }

    private void setupView() {
        String title = getIntent().getStringExtra("book_title");
        String author = getIntent().getStringExtra("book_author");
        int imageRes = getIntent().getIntExtra("book_image", 0);
        String description = getIntent().getStringExtra("book_description");
        double price = getIntent().getDoubleExtra("book_price", 0.0);

        binding.detailBookTitle.setText(title);
        binding.detailBookAuthor.setText(author != null ? "By " + author : "Unknown Author");
        binding.detailBookPrice.setText(String.format(Locale.getDefault(), "$%.2f", price));
        
        if (imageRes != 0) {
            binding.detailBookCover.setImageResource(imageRes);
        }
        if (description != null) {
            binding.detailBookDescription.setText(description);
        }

        binding.detailToolbar.setNavigationOnClickListener(v -> finish());
        
        Book currentBook = new Book(title, author, imageRes, description, price);

        binding.btnBuyDetail.setOnClickListener(v -> showBookingDialog(true, currentBook));
        binding.btnReserveDetail.setOnClickListener(v -> showBookingDialog(false, currentBook));
    }

    private void showBookingDialog(boolean isPurchase, Book book) {
        DialogBookingDetailsBinding dialogBinding = DialogBookingDetailsBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.TransparentDialog)
                .setView(dialogBinding.getRoot())
                .create();

        if (!isPurchase) {
            dialogBinding.tvDialogTitle.setText("Reservation Details");
            dialogBinding.btnConfirmBooking.setText("Reserve Book");
        } else {
            dialogBinding.tvDialogTitle.setText("Buy Now");
            dialogBinding.btnConfirmBooking.setText("Confirm Purchase");
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
}