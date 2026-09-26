package com.example.smartpantrymanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRecipes;
    private TextView tvNoRecipesMessage;

    private DatabaseHelper databaseHelper;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> suggestedRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        tvNoRecipesMessage = findViewById(R.id.tvNoRecipesMessage);

        databaseHelper = new DatabaseHelper(this);

        suggestedRecipes = new ArrayList<>();

        recyclerViewRecipes.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recipeAdapter = new RecipeAdapter(suggestedRecipes);
        recyclerViewRecipes.setAdapter(recipeAdapter);

        loadSuggestedRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null) {
            loadSuggestedRecipes();
        }
    }

    private void loadSuggestedRecipes() {

        suggestedRecipes.clear();

        List<Ingredient> pantryIngredients = loadPantryIngredients();
        List<Recipe> allRecipes = loadAllRecipes();

        for (Recipe recipe : allRecipes) {

            if (canMakeRecipe(recipe, pantryIngredients)) {
                suggestedRecipes.add(recipe);
            }
        }

        recipeAdapter.notifyDataSetChanged();

        if (suggestedRecipes.isEmpty()) {
            tvNoRecipesMessage.setVisibility(TextView.VISIBLE);
            recyclerViewRecipes.setVisibility(RecyclerView.GONE);
        } else {
            tvNoRecipesMessage.setVisibility(TextView.GONE);
            recyclerViewRecipes.setVisibility(RecyclerView.VISIBLE);
        }
    }

    private List<Ingredient> loadPantryIngredients() {

        List<Ingredient> ingredients = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "ingredients",
                null,
                null,
                null,
                null,
                null,
                "name ASC"
        );

        while (cursor.moveToNext()) {

            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
            );

            double quantity = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("quantity")
            );

            String unit = cursor.getString(
                    cursor.getColumnIndexOrThrow("unit")
            );

            String expiryDate = cursor.getString(
                    cursor.getColumnIndexOrThrow("expiryDate")
            );

            ingredients.add(
                    new Ingredient(
                            id,
                            name,
                            quantity,
                            unit,
                            expiryDate
                    )
            );
        }

        cursor.close();
        db.close();

        return ingredients;
    }

    private List<Recipe> loadAllRecipes() {

        List<Recipe> recipes = new ArrayList<>();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "recipes",
                null,
                null,
                null,
                null,
                null,
                "name ASC"
        );

        while (cursor.moveToNext()) {

            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
            );

            String description = cursor.getString(
                    cursor.getColumnIndexOrThrow("description")
            );

            String ingredients = cursor.getString(
                    cursor.getColumnIndexOrThrow("ingredients")
            );

            String instructions = cursor.getString(
                    cursor.getColumnIndexOrThrow("instructions")
            );

            recipes.add(
                    new Recipe(
                            id,
                            name,
                            description,
                            ingredients,
                            instructions
                    )
            );
        }

        cursor.close();
        db.close();

        return recipes;
    }

    private boolean canMakeRecipe(
            Recipe recipe,
            List<Ingredient> pantryIngredients) {

        String[] requiredIngredients =
                recipe.getIngredients().split(",");

        for (String required : requiredIngredients) {

            String[] parts = required.split(":", 3);

            if (parts.length != 3) {
                return false;
            }

            String requiredName = normalizeName(parts[0]);

            double requiredQuantity;

            try {
                requiredQuantity = Double.parseDouble(parts[1]);
            } catch (NumberFormatException e) {
                return false;
            }

            String requiredUnit = normalizeUnit(parts[2]);

            double requiredBaseQuantity =
                    convertToBaseQuantity(
                            requiredQuantity,
                            requiredUnit
                    );

            double pantryBaseQuantity = 0;

            for (Ingredient pantryIngredient : pantryIngredients) {

                String pantryName =
                        normalizeName(pantryIngredient.getName());

                String pantryUnit =
                        normalizeUnit(pantryIngredient.getUnit());

                if (pantryName.equals(requiredName)
                        && areCompatibleUnits(
                        pantryUnit,
                        requiredUnit)) {

                    double pantryQuantity =
                            convertToBaseQuantity(
                                    pantryIngredient.getQuantity(),
                                    pantryUnit
                            );

                    pantryBaseQuantity += pantryQuantity;
                }
            }

            if (pantryBaseQuantity < requiredBaseQuantity) {
                return false;
            }
        }

        return true;
    }

    private String normalizeName(String name) {

        String result = name
                .toLowerCase(Locale.ROOT)
                .trim();

        // Handle common plural forms.
        if (result.equals("tomatoes")) {
            return "tomato";
        }

        if (result.endsWith("ies")) {
            return result.substring(
                    0,
                    result.length() - 3
            ) + "y";
        }

        if (result.endsWith("s")
                && !result.endsWith("ss")) {

            result = result.substring(
                    0,
                    result.length() - 1
            );
        }

        return result;
    }

    private String normalizeUnit(String unit) {

        String result = unit
                .toLowerCase(Locale.ROOT)
                .trim();

        if (result.equals("item")
                || result.equals("items")
                || result.equals("piece")
                || result.equals("pieces")) {

            return "items";
        }

        if (result.equals("g")
                || result.equals("gram")
                || result.equals("grams")) {

            return "grams";
        }

        if (result.equals("kg")
                || result.equals("kilogram")
                || result.equals("kilograms")) {

            return "kg";
        }

        if (result.equals("ml")
                || result.equals("millilitre")
                || result.equals("millilitres")) {

            return "ml";
        }

        if (result.equals("l")
                || result.equals("litre")
                || result.equals("litres")) {

            return "l";
        }

        return result;
    }

    private boolean areCompatibleUnits(
            String pantryUnit,
            String requiredUnit) {

        // Both are counting individual items.
        if (pantryUnit.equals("items")
                && requiredUnit.equals("items")) {

            return true;
        }

        // Both are weight measurements.
        if ((pantryUnit.equals("grams")
                || pantryUnit.equals("kg"))
                && (requiredUnit.equals("grams")
                || requiredUnit.equals("kg"))) {

            return true;
        }

        // Both are volume measurements.
        if ((pantryUnit.equals("ml")
                || pantryUnit.equals("l"))
                && (requiredUnit.equals("ml")
                || requiredUnit.equals("l"))) {

            return true;
        }

        return false;
    }

    private double convertToBaseQuantity(
            double quantity,
            String unit) {

        switch (unit) {

            case "kg":
                // Convert kilograms to grams.
                return quantity * 1000;

            case "grams":
                // Grams are our base weight unit.
                return quantity;

            case "l":
                // Convert litres to millilitres.
                return quantity * 1000;

            case "ml":
                // Millilitres are our base volume unit.
                return quantity;

            case "items":
                // Items stay as items.
                return quantity;

            default:
                // Unknown units cannot be safely converted.
                return quantity;
        }
    }
}