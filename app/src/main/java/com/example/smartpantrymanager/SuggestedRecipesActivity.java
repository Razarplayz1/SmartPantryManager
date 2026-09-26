package com.example.smartpantrymanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecyclerView recipesListView;
    private TextView noRecipesNotice;
    private Button btnBackToPantry;

    private DatabaseHelper dbHelper;
    private RecipeAdapter adapter;
    private final List<Recipe> availableRecipes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        recipesListView = findViewById(R.id.recyclerViewRecipes);
        noRecipesNotice = findViewById(R.id.tvNoRecipesMessage);

        btnBackToPantry = findViewById(R.id.btnBackToPantry);

        btnBackToPantry.setOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);

        setupRecyclerView();

        updateSuggestedRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null) {
            updateSuggestedRecipes();
        }
    }

    private void setupRecyclerView() {
        recipesListView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(availableRecipes);
        recipesListView.setAdapter(adapter);
    }

    private void updateSuggestedRecipes() {
        availableRecipes.clear();

        List<Ingredient> pantryContents = getPantryIngredients();
        List<Recipe> allRecipes = getAllRecipesFromDatabase();

        for (Recipe r : allRecipes) {
            if (isRecipeFeasible(r, pantryContents)) {
                availableRecipes.add(r);
            }
        }

        adapter.notifyDataSetChanged();
        toggleRecipeVisibility(availableRecipes.isEmpty());
    }

    private void toggleRecipeVisibility(boolean noRecipesAvailable) {
        if (noRecipesAvailable) {
            noRecipesNotice.setVisibility(View.VISIBLE);
            recipesListView.setVisibility(View.GONE);
        } else {
            noRecipesNotice.setVisibility(View.GONE);
            recipesListView.setVisibility(View.VISIBLE);
        }
    }

    private List<Ingredient> getPantryIngredients() {
        List<Ingredient> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "ingredients", null,null,null,null,null,
                "name ASC")) {

            while (cursor.moveToNext()) {
                list.add(new Ingredient(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("quantity")),
                        cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                        cursor.getString(cursor.getColumnIndexOrThrow("expiryDate"))
                ));
            }
        }

        db.close();
        return list;
    }

    private List<Recipe> getAllRecipesFromDatabase() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "recipes", null,null,null,null,null,
                "name ASC")) {

            while (cursor.moveToNext()) {
                list.add(new Recipe(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("ingredients")),
                        cursor.getString(cursor.getColumnIndexOrThrow("instructions"))
                ));
            }
        }

        db.close();
        return list;
    }

    private boolean isRecipeFeasible(Recipe recipe, List<Ingredient> pantry) {
        String[] neededIngredients = recipe.getIngredients().split(",");

        for (String ingredient : neededIngredients) {
            String[] details = ingredient.split(":", 3);
            if (details.length != 3) return false;

            String ingredientName = standardizeName(details[0]);
            double requiredQty;
            try {
                requiredQty = Double.parseDouble(details[1]);
            } catch (NumberFormatException e) {
                return false;
            }
            String ingredientUnit = standardizeUnit(details[2]);
            double requiredBaseQty = toBaseUnitQuantity(requiredQty, ingredientUnit);

            double pantrySum = 0;
            for (Ingredient available : pantry) {
                if (standardizeName(available.getName()).equals(ingredientName) &&
                        areUnitsCompatible(standardizeUnit(available.getUnit()), ingredientUnit)) {

                    pantrySum += toBaseUnitQuantity(available.getQuantity(), standardizeUnit(available.getUnit()));
                }
            }

            if (pantrySum < requiredBaseQty) {
                return false;
            }
        }

        return true;
    }

    private String standardizeName(String name) {
        String low = name.toLowerCase(Locale.ROOT).trim();

        if (low.equals("tomatoes")) return "tomato";
        if (low.endsWith("ies")) return low.substring(0, low.length() - 3) + "y";
        if (low.endsWith("s") && !low.endsWith("ss")) return low.substring(0, low.length() - 1);

        return low;
    }

    private String standardizeUnit(String unit) {
        String u = unit.toLowerCase(Locale.ROOT).trim();
        switch (u) {
            case "item":
            case "items":
            case "piece":
            case "pieces":
                return "items";
            case "g":
            case "gram":
            case "grams":
                return "grams";
            case "kg":
            case "kilogram":
            case "kilograms":
                return "kg";
            case "ml":
            case "millilitre":
            case "millilitres":
                return "ml";
            case "l":
            case "litre":
            case "litres":
                return "l";
            default:
                return u;
        }
    }

    private boolean areUnitsCompatible(String unitA, String unitB) {
        if (unitA.equals("items") && unitB.equals("items")) return true;

        boolean isWeightA = unitA.equals("grams") || unitA.equals("kg");
        boolean isWeightB = unitB.equals("grams") || unitB.equals("kg");
        if (isWeightA && isWeightB) return true;

        boolean isVolumeA = unitA.equals("ml") || unitA.equals("l");
        boolean isVolumeB = unitB.equals("ml") || unitB.equals("l");
        return isVolumeA && isVolumeB;
    }

    private double toBaseUnitQuantity(double quantity, String unit) {
        switch (unit) {
            case "kg": return quantity * 1000;
            case "grams": return quantity;
            case "l": return quantity * 1000;
            case "ml": return quantity;
            case "items": return quantity;
            default: return quantity;
        }
    }
}
