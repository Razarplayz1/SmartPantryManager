package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddIngredientActivity extends AppCompatActivity {

    private EditText etIngredientName;
    private EditText etQuantity;
    private EditText etUnit;
    private EditText etExpiryDate;
    private Button btnSaveIngredient;
    private Button btnBackFromAddIngredient;

    private DatabaseHelper databaseHelper;

    // This will contain the ID of the ingredient we are editing.
    // If it is -1, we are adding a new ingredient.
    private int ingredientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);

        btnBackFromAddIngredient = findViewById(R.id.btnBackFromAddIngredient);

        btnBackFromAddIngredient.setOnClickListener(v -> finish());

        etIngredientName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etQuantity);
        etUnit = findViewById(R.id.etUnit);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        btnSaveIngredient = findViewById(R.id.btnSaveIngredient);

        databaseHelper = new DatabaseHelper(this);

        // Check if we were sent an ingredient to edit.
        ingredientId = getIntent().getIntExtra("ingredient_id", -1);

        if (ingredientId != -1) {

            // We are editing an existing ingredient.
            etIngredientName.setText(
                    getIntent().getStringExtra("ingredient_name")
            );

            double quantity = getIntent().getDoubleExtra(
                    "ingredient_quantity",
                    0
            );

            etQuantity.setText(String.valueOf(quantity));

            etUnit.setText(
                    getIntent().getStringExtra("ingredient_unit")
            );

            etExpiryDate.setText(
                    getIntent().getStringExtra("ingredient_expiry")
            );

            btnSaveIngredient.setText("Update Ingredient");
        }

        btnSaveIngredient.setOnClickListener(v -> saveIngredient());
    }

    private void saveIngredient() {

        String name = etIngredientName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();

        // Validate ingredient name
        if (name.isEmpty()) {
            etIngredientName.setError("Enter an ingredient name");
            etIngredientName.requestFocus();
            return;
        }

        // Validate quantity
        if (quantityText.isEmpty()) {
            etQuantity.setError("Enter a quantity");
            etQuantity.requestFocus();
            return;
        }

        // Validate unit
        if (unit.isEmpty()) {
            etUnit.setError("Enter a unit");
            etUnit.requestFocus();
            return;
        }

        double quantity;

        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid number");
            etQuantity.requestFocus();
            return;
        }

        // Quantity must be greater than zero
        if (quantity <= 0) {
            etQuantity.setError("Quantity must be greater than 0");
            etQuantity.requestFocus();
            return;
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("quantity", quantity);
        values.put("unit", unit);
        values.put("expiryDate", expiryDate);

        long result;

        if (ingredientId == -1) {

            // No ID means we are adding a NEW ingredient.
            result = db.insert(
                    "ingredients",
                    null,
                    values
            );

        } else {

            // An ID exists, so we are UPDATING an existing ingredient.
            result = db.update(
                    "ingredients",
                    values,
                    "id = ?",
                    new String[]{String.valueOf(ingredientId)}
            );
        }

        db.close();

        if (result != -1) {

            if (ingredientId == -1) {

                Toast.makeText(
                        this,
                        "Ingredient added successfully!",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        this,
                        "Ingredient updated successfully!",
                        Toast.LENGTH_SHORT
                ).show();
            }

            finish();

        } else {

            Toast.makeText(
                    this,
                    "Failed to save ingredient.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}