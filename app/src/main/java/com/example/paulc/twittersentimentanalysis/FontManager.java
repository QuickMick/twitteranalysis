package com.example.paulc.twittersentimentanalysis;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;


/**
 * Created by paulc on 09.04.2017.
 */

public class FontManager {

    Context c;

    public FontManager(Context context) {
        this.c = context;
    }

    public void setAppMedium (TextView tv) {
        Typeface font = Typeface.createFromAsset(c.getAssets(),"AppSans_medium.otf");
        tv.setTypeface(font);
    }

    public void setAppRegular (TextView tv) {
        Typeface font = Typeface.createFromAsset(c.getAssets(),"AppSans_regular.otf");
        tv.setTypeface(font);
    }

    public void setBackIcon (TextView tv) {
        Typeface font = Typeface.createFromAsset(c.getAssets(),"FontAwesome.otf");
        tv.setTypeface(font);
//        tv.setText(c.getString(R.string.fa_angel_left));
    }
}
