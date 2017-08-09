package com.stackexchange.android.users;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StackExchangeFetchr {
    private static final String TAG = "StackExchangeFetchr";

    private static final String VAL = "stackoverflow";
    private static final String FETCH_RECENTS_METHOD = "users";
    private static final String SEARCH_METHOD = "stackexchange.photos.search";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.stackexchange.com/2.2/users")
            .buildUpon()
            .appendQueryParameter("site", VAL)
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryUser> fetchRecentUsers() {
        String url = buildUrl(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(url);
    }

    private List<GalleryUser> downloadGalleryItems(String url) {
        List<GalleryUser> items = new ArrayList<>();

        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return items;
    }

    private String buildUrl(String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("", method);

        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }

        return uriBuilder.build().toString();
    }

    private void parseItems(List<GalleryUser> items, JSONObject jsonBody)
            throws IOException, JSONException {

        JSONArray photosJsonObject = jsonBody.getJSONArray("items");

        for (int i = 0; i < photosJsonObject.length(); i++) {
            JSONObject userJsonObject = photosJsonObject.getJSONObject(i);

            GalleryUser item = new GalleryUser();


            if (userJsonObject.has("badge_counts")) {
                JSONObject badgeJsonObject = userJsonObject.getJSONObject("badge_counts");
                for (int j = 0; j < userJsonObject.length(); j++) {

                    item.setBadgeCountBronze(badgeJsonObject.getString("bronze"));
                    item.setBadgeCountSilver(badgeJsonObject.getString("silver"));
                    item.setBadgeCountGold(badgeJsonObject.getString("gold"));
                }
            }

            item.setId(userJsonObject.getString("user_id"));
            item.setDisplayName(userJsonObject.getString("display_name"));

            if (!userJsonObject.has("profile_image")) {
                continue;
            }

            item.setProfileImage(userJsonObject.getString("profile_image"));
            items.add(item);
        }
    }

}
