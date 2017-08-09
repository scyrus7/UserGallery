package com.stackexchange.android.users;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class UserGalleryFragment extends Fragment {
    private static final String TAG = "UserGalleryFragment";

    private RecyclerView mUserRecyclerView;
    private List<GalleryUser> mItems = new ArrayList<>();
    private ThumbnailDownloader<UserHolder> mThumbnailDownloader;

    public static UserGalleryFragment newInstance() {
        return new UserGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        updateItems();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<UserHolder>() {
                    @Override
                    public void onThumbnailDownloaded(UserHolder userHolder, Bitmap bitmap) {
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        userHolder.bindDrawable(drawable);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        //   mTextViewUserName.
        Log.i(TAG, "Background thread started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_gallery, container, false);
        mUserRecyclerView = (RecyclerView) v
                .findViewById(R.id.fragment_user_gallery_recycler_view);
        mUserRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        setupAdapter();

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_user_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "QueryTextSubmit: " + s);
                QueryPreferences.setStoredQuery(getActivity(), s);
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }

    private void setupAdapter() {
        if (isAdded()) {
            mUserRecyclerView.setAdapter(new UserAdapter(mItems));
        }
    }

    private class UserHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;
        private TextView mTextViewUserName;
        private TextView mTextViewBadges;

        public UserHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView
                    .findViewById(R.id.fragment_user_gallery_image_view);
            mTextViewUserName = (TextView) itemView
                    .findViewById(R.id.txtview_usrname);
            mTextViewBadges = (TextView) itemView
                    .findViewById(R.id.txtview_badges);

        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class UserAdapter extends RecyclerView.Adapter<UserHolder> {

        private List<GalleryUser> mGalleryUsers;

        public UserAdapter(List<GalleryUser> galleryUsers) {
            mGalleryUsers = galleryUsers;
        }

        @Override
        public UserHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_gallery, viewGroup, false);
            return new UserHolder(view);
        }

        @Override
        public void onBindViewHolder(UserHolder userHolder, int position) {
            GalleryUser galleryUser = mGalleryUsers.get(position);
            Drawable placeholder = getResources().getDrawable(R.drawable.loading);
            userHolder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(userHolder, galleryUser.getProfileImage());
            userHolder.mTextViewUserName.setText(galleryUser.getDisplayName());
            userHolder.mTextViewBadges.setText("Gold: " + galleryUser.getBadgeCountGold() +
                    "\nBronze: " + galleryUser.getBadgeCountBronze() +
                    "\nSilver: " + galleryUser.getBadgeCountSilver());
        }

        @Override
        public int getItemCount() {
            return mGalleryUsers.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryUser>> {
        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected List<GalleryUser> doInBackground(Void... params) {

            if (mQuery == null) {
                return new StackExchangeFetchr().fetchRecentUsers();
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<GalleryUser> items) {
            mItems = items;
            setupAdapter();
        }

    }

}
