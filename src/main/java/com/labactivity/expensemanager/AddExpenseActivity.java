package com.labactivity.expensemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.labactivity.expensemanager.databinding.ActivityAddExpenseBinding;

import java.util.Calendar;
import java.util.UUID;

public class AddExpenseActivity extends AppCompatActivity {
    ActivityAddExpenseBinding binding;
    private String type;
    private ExpenseModel expenseModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        type = getIntent().getStringExtra("type");
        expenseModel = (ExpenseModel) getIntent().getSerializableExtra("model");

        if (expenseModel != null) {
            binding.amount.setText(String.valueOf(expenseModel.getAmount()));
            binding.category.setText(expenseModel.getCategory());
            binding.note.setText(expenseModel.getNote());

            // Set the radio button based on the expense type
            if ("Income".equals(expenseModel.getType())) {
                binding.incomeRadio.setChecked(true);
            } else {
                binding.expenseRadio.setChecked(true);
            }

            // Set up radio button listeners
            binding.incomeRadio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    type = "Income";
                }
            });

            binding.expenseRadio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    type = "Expense";
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        if (expenseModel == null) {
            menuInflater.inflate(R.menu.add_menu, menu);
        } else {
            menuInflater.inflate(R.menu.update_menu, menu);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.saveExpense) {
            if (expenseModel != null) {
                updateExpense();
            } else {
                createExpense();
            }
            return true;
        }
        if (id == R.id.deleteExpense) {
            deleteExpense();
        }
        return false;
    }

    private void deleteExpense() {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title and message
        builder.setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?");

        // Add positive button (Yes action)
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete the expense
                FirebaseDatabase.getInstance().getReference("expenses")
                        .child(expenseModel.getExpenseId()).removeValue();
                finish();
            }
        });

        // Add negative button (No action)
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog (do nothing)
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void createExpense() {
        String expenseId = UUID.randomUUID().toString();
        String amount = binding.amount.getText().toString();
        String note = binding.note.getText().toString();
        String category = binding.category.getText().toString();

        boolean incomeChecked = binding.incomeRadio.isChecked();
        String type = incomeChecked ? "Income" : "Expense";

        if (amount.trim().length() == 0) {
            binding.amount.setError("Empty");
            return;
        }

        ExpenseModel expenseModel = new ExpenseModel(expenseId, note, category, type,
                Long.parseLong(amount), Calendar.getInstance().getTimeInMillis(),
                FirebaseAuth.getInstance().getUid());

        DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference("expenses");
        expensesRef.child(expenseId).setValue(expenseModel);
        finish();
    }

    private void updateExpense() {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title and message
        builder.setTitle("Update Expense")
                .setMessage("Are you sure you want to update this expense?");

        // Add positive button (Yes action)
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Update the expense
                String expenseId = expenseModel.getExpenseId();
                String amount = binding.amount.getText().toString();
                String note = binding.note.getText().toString();
                String category = binding.category.getText().toString();

                boolean incomeChecked = binding.incomeRadio.isChecked();
                String type = incomeChecked ? "Income" : "Expense";

                if (amount.trim().length() == 0) {
                    binding.amount.setError("Empty");
                    return;
                }

                ExpenseModel model = new ExpenseModel(expenseId, note, category, type,
                        Long.parseLong(amount), expenseModel.getTime(),
                        FirebaseAuth.getInstance().getUid());

                DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference("expenses");
                expensesRef.child(expenseId).setValue(model);
                finish();
            }
        });

        // Add negative button (No action)
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog (do nothing)
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}