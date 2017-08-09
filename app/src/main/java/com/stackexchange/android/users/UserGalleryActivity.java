package com.stackexchange.android.users;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;


public class UserGalleryActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, UserGalleryActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return UserGalleryFragment.newInstance();
    }
}
