package com.pennywise.ui;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pennywise.R;

public final class CardArt {

    private CardArt() {}

    // The names saved in the database's cardImage column
    public static final String[] NAMES = {
            "card_art_sage", "card_art_blush", "card_art_crimson",
            "card_art_cream", "card_art_night"
    };

    public static int drawableFor(String name) {
        if (name == null) return R.drawable.card_art_sage;
        switch (name) {
            case "card_art_blush":   return R.drawable.card_art_blush;
            case "card_art_crimson": return R.drawable.card_art_crimson;
            case "card_art_cream":   return R.drawable.card_art_cream;
            case "card_art_night":   return R.drawable.card_art_night;
            default:                 return R.drawable.card_art_sage;
        }
    }

    // Dark backgrounds need white text
    public static boolean isDark(String name) {
        return "card_art_crimson".equals(name) || "card_art_night".equals(name);
    }

    // Fills one view_card with data
    public static void bind(View card, String art, String bank, String nickname,
                            String lastFour, String type, String balance, String extra) {

        card.setBackgroundResource(drawableFor(art));
        card.setClipToOutline(true); // keeps the glow inside the rounded corners

        int main = isDark(art) ? Color.WHITE : Color.parseColor("#1F2A1E");
        int soft = isDark(art) ? Color.parseColor("#E6FFFFFF") : Color.parseColor("#33452F");

        TextView bankText = card.findViewById(R.id.cardBank);
        TextView typeText = card.findViewById(R.id.cardType);
        TextView nickText = card.findViewById(R.id.cardNickname);
        TextView balanceText = card.findViewById(R.id.cardBalance);
        TextView extraText = card.findViewById(R.id.cardExtra);
        ImageView wave = card.findViewById(R.id.cardWave);

        bankText.setText(bank);
        typeText.setText(type == null ? "" : type.toUpperCase());

        String digits = (lastFour == null || lastFour.isEmpty()) ? "••••" : lastFour;
        nickText.setText(nickname + " · •••• " + digits);

        balanceText.setText(balance);
        extraText.setText(extra == null ? "" : extra);

        bankText.setTextColor(main);
        typeText.setTextColor(main);
        balanceText.setTextColor(main);
        nickText.setTextColor(soft);
        extraText.setTextColor(soft);
        wave.setColorFilter(main);
    }
}