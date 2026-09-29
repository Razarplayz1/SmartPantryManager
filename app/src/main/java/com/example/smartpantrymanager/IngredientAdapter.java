package com.example.smartpantrymanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private List<Ingredient> ingredientList;

    public IngredientAdapter(List<Ingredient> ingredientList) {
        this.ingredientList = ingredientList;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);

        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {

        Ingredient ingredient = ingredientList.get(position);

        holder.tvIngredientName.setText(ingredient.getName());

        String details = ingredient.getQuantity() + " " + ingredient.getUnit();
        holder.tvIngredientDetails.setText(details);

        String expiryDate = ingredient.getExpiryDate();

        if (expiryDate != null && !expiryDate.trim().isEmpty()) {

            holder.tvIngredientExpiry.setText("Expires: " + expiryDate);
            holder.tvIngredientExpiry.setVisibility(View.VISIBLE);

            SharedPreferences preferences = holder.itemView.getContext()
                    .getSharedPreferences(
                            "SmartPantrySettings",
                            Context.MODE_PRIVATE
                    );

            boolean expiryRemindersEnabled = preferences.getBoolean(
                    "expiry_reminders",
                    true
            );

            if (expiryRemindersEnabled) {

                String warning = getExpiryWarning(expiryDate);

                if (warning != null) {

                    holder.tvExpiryWarning.setText(warning);
                    holder.tvExpiryWarning.setVisibility(View.VISIBLE);

                } else {

                    holder.tvExpiryWarning.setText("");
                    holder.tvExpiryWarning.setVisibility(View.GONE);
                }

            } else {

                holder.tvExpiryWarning.setText("");
                holder.tvExpiryWarning.setVisibility(View.GONE);
            }

        } else {

            holder.tvIngredientExpiry.setText("");
            holder.tvIngredientExpiry.setVisibility(View.GONE);

            holder.tvExpiryWarning.setText("");
            holder.tvExpiryWarning.setVisibility(View.GONE);
        }

        // EDIT BUTTON
        holder.btnEditIngredient.setOnClickListener(v -> {

            Intent intent = new Intent(
                    v.getContext(),
                    AddIngredientActivity.class
            );

            intent.putExtra("ingredient_id", ingredient.getId());
            intent.putExtra("ingredient_name", ingredient.getName());
            intent.putExtra("ingredient_quantity", ingredient.getQuantity());
            intent.putExtra("ingredient_unit", ingredient.getUnit());
            intent.putExtra("ingredient_expiry", ingredient.getExpiryDate());

            v.getContext().startActivity(intent);
        });

        // DELETE BUTTON
        holder.btnDeleteIngredient.setOnClickListener(v -> {

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Ingredient")
                    .setMessage(
                            "Are you sure you want to delete "
                                    + ingredient.getName()
                                    + "?"
                    )
                    .setPositiveButton("Delete", (dialog, which) -> {

                        Context context = v.getContext();

                        DatabaseHelper databaseHelper =
                                new DatabaseHelper(context);

                        SQLiteDatabase db =
                                databaseHelper.getWritableDatabase();

                        db.delete(
                                "ingredients",
                                "id = ?",
                                new String[]{
                                        String.valueOf(ingredient.getId())
                                }
                        );

                        db.close();

                        // Remove the ingredient from the list
                        // so it disappears from the screen.
                        int currentPosition = holder.getAdapterPosition();

                        if (currentPosition != RecyclerView.NO_POSITION) {

                            ingredientList.remove(currentPosition);

                            notifyItemRemoved(currentPosition);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private String getExpiryWarning(String expiryDate) {

        String[] dateFormats = {
                "dd/MM/yyyy",
                "yyyy-MM-dd",
                "MM/dd/yyyy"
        };

        Date expiry = null;

        for (String format : dateFormats) {

            try {

                SimpleDateFormat dateFormat =
                        new SimpleDateFormat(
                                format,
                                Locale.getDefault()
                        );

                dateFormat.setLenient(false);

                expiry = dateFormat.parse(expiryDate);

                if (expiry != null) {
                    break;
                }

            } catch (ParseException ignored) {
                // Try the next date format.
            }
        }

        if (expiry == null) {
            return null;
        }

        long currentTime = System.currentTimeMillis();
        long difference = expiry.getTime() - currentTime;

        long daysRemaining =
                difference / (1000L * 60L * 60L * 24L);

        if (daysRemaining < 0) {
            return "Expired!";
        }

        if (daysRemaining <= 3) {
            return "Expires soon!";
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {

        TextView tvIngredientName;
        TextView tvIngredientDetails;
        TextView tvIngredientExpiry;
        TextView tvExpiryWarning;

        Button btnEditIngredient;
        Button btnDeleteIngredient;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);

            tvIngredientName =
                    itemView.findViewById(R.id.tvIngredientName);

            tvIngredientDetails =
                    itemView.findViewById(R.id.tvIngredientDetails);

            tvIngredientExpiry =
                    itemView.findViewById(R.id.tvIngredientExpiry);

            tvExpiryWarning =
                    itemView.findViewById(R.id.tvExpiryWarning);

            btnEditIngredient =
                    itemView.findViewById(R.id.btnEditIngredient);

            btnDeleteIngredient =
                    itemView.findViewById(R.id.btnDeleteIngredient);
        }
    }
}