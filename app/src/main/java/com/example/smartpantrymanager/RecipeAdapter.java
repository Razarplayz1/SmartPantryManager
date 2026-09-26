package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecipeAdapter
        extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;

    public RecipeAdapter(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);

        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecipeViewHolder holder,
            int position) {

        Recipe recipe = recipeList.get(position);

        holder.tvRecipeName.setText(recipe.getName());

        holder.tvRecipeDescription.setText(
                recipe.getDescription()
        );

        holder.btnViewRecipe.setOnClickListener(v -> {

            android.content.Intent intent =
                    new android.content.Intent(
                            v.getContext(),
                            RecipeDetailActivity.class
                    );

            intent.putExtra(
                    "recipe_id",
                    recipe.getId()
            );

            intent.putExtra(
                    "recipe_name",
                    recipe.getName()
            );

            intent.putExtra(
                    "recipe_description",
                    recipe.getDescription()
            );

            intent.putExtra(
                    "recipe_ingredients",
                    recipe.getIngredients()
            );

            intent.putExtra(
                    "recipe_instructions",
                    recipe.getInstructions()
            );

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvRecipeName;
        TextView tvRecipeDescription;
        Button btnViewRecipe;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRecipeName =
                    itemView.findViewById(
                            R.id.tvRecipeName
                    );

            tvRecipeDescription =
                    itemView.findViewById(
                            R.id.tvRecipeDescription
                    );

            btnViewRecipe =
                    itemView.findViewById(
                            R.id.btnViewRecipe
                    );
        }
    }
}