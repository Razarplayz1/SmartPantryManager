package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {

    private Button btnBackFromRecipeDetail;
    private TextView tvRecipeName;
    private TextView tvRecipeDescription;
    private TextView tvRecipeIngredients;
    private TextView tvRecipeInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        btnBackFromRecipeDetail = findViewById(R.id.btnBackFromRecipeDetail);

        btnBackFromRecipeDetail.setOnClickListener(v -> finish());

        tvRecipeName = findViewById(R.id.tvRecipeName);
        tvRecipeDescription = findViewById(R.id.tvRecipeDescription);
        tvRecipeIngredients = findViewById(R.id.tvRecipeIngredients);
        tvRecipeInstructions = findViewById(R.id.tvRecipeInstructions);

        String recipeName =
                getIntent().getStringExtra("recipe_name");

        String recipeDescription =
                getIntent().getStringExtra("recipe_description");

        String recipeIngredients =
                getIntent().getStringExtra("recipe_ingredients");

        String recipeInstructions =
                getIntent().getStringExtra("recipe_instructions");

        tvRecipeName.setText(recipeName);
        tvRecipeDescription.setText(recipeDescription);
        tvRecipeIngredients.setText(recipeIngredients);
        tvRecipeInstructions.setText(recipeInstructions);
    }
}