package com.labactivity.expensemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labactivity.expensemanager.databinding.ActivityMain2Binding;
import com.labactivity.expensemanager.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity implements OnItemsClick {
    @NonNull
    ActivityMain2Binding binding;
    ActivityMainBinding binding2;
    private FirebaseAuth mAuth;
    private ExpensesAdapter expensesAdapter;
    Intent intent;
    private long income = 0, expense = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        expensesAdapter = new ExpensesAdapter(this, this);
        binding.recycler.setAdapter(expensesAdapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        intent = new Intent(MainActivity2.this, AddExpenseActivity.class);

        binding.addIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("type", "Income");
                startActivity(intent);

            }
        });
        binding.addExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.putExtra("type", "Expense");
                startActivity(intent);

            }
        });
    }


    @Override
    public void onStart() {

        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        income = 0;
        expense = 0;
        getData();
    }

    private void getData() {
        DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference("expenses");
        expensesRef.orderByChild("uid").equalTo(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        expensesAdapter.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ExpenseModel expenseModel = ds.getValue(ExpenseModel.class);
                            if (expenseModel.getType().equals("Income")) {
                                income += expenseModel.getAmount();
                            } else {
                                expense += expenseModel.getAmount();
                            }
                            expensesAdapter.add(expenseModel);
                        }
                        setUpGraph();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle read error
                    }
                });
    }

    private void setUpGraph() {
        List<PieEntry> pieEntryList = new ArrayList<>();
        List<Integer> colorList = new ArrayList<>();
        if (income != 0) {
            pieEntryList.add(new PieEntry(income, "Income"));
            colorList.add(getResources().getColor(R.color.teal_700));
        }
        if (expense != 0) {
            pieEntryList.add(new PieEntry(expense, "Expense"));
            colorList.add(getResources().getColor(R.color.red));
        }
        if (income - expense > 0) {
            // Add the remaining balance entry with white color
            pieEntryList.add(new PieEntry(income - expense, "Remaining Balance"));
            colorList.add(getResources().getColor(android.R.color.holo_blue_light));
        }
        PieDataSet pieDataSet = new PieDataSet(pieEntryList, String.valueOf(income - expense));
        pieDataSet.setColors(colorList);
        pieDataSet.setValueTextSize(12f);
        PieData pieDat = new PieData(pieDataSet);

        binding.pieChart.setData(pieDat);
        binding.pieChart.invalidate();
    }

    @Override
    public void onClick(ExpenseModel expenseModel) {
        intent.putExtra("model", expenseModel);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            // Create an AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);

            // Set the dialog title and message
            builder.setTitle("Logout")
                    .setMessage("Are you sure you want to logout?");

            // Add positive button (Yes action)
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Handle logout
                    FirebaseAuth.getInstance().signOut();
                    // Redirect to login or another activity
                    Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close the current activity
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

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}