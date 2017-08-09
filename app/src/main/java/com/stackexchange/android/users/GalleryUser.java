package com.stackexchange.android.users;

public class GalleryUser {
    private String mCaption;
    private String mId;
    private String mProfileImage;
    private String mDisplayName;
    private String mBadgeCountBronze;
    private String mBadgeCountSilver;
    private String mBadgeCountGold;

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getProfileImage() {
        return mProfileImage;
    }

    public void setProfileImage(String profileImage) {
        mProfileImage = profileImage;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public String getBadgeCountBronze() {
        return mBadgeCountBronze;
    }

    public void setBadgeCountBronze(String badgeCountBronze) {
        mBadgeCountBronze = badgeCountBronze;
    }

    public String getBadgeCountSilver() {
        return mBadgeCountSilver;
    }

    public void setBadgeCountSilver(String badgeCountSilver) {
        mBadgeCountSilver = badgeCountSilver;
    }

    public String getBadgeCountGold() {
        return mBadgeCountGold;
    }

    public void setBadgeCountGold(String badgeCountGold) {
        mBadgeCountGold = badgeCountGold;
    }

    @Override
    public String toString() {
        return mCaption;
    }
}
