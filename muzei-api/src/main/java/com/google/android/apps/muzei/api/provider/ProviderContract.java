/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.apps.muzei.api.provider;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Contract between Muzei and Muzei Art Providers, containing the definitions for all supported
 * URIs and columns as well as helper methods to make it easier to work with the provided data.
 */
public class ProviderContract {
    /**
     * Constants and helper methods for working with the
     * {@link com.google.android.apps.muzei.api.provider.Artwork} associated
     * with a {@link MuzeiArtProvider}.
     */
    public static final class Artwork implements BaseColumns {
        /**
         * The token that uniquely defines the artwork. Any inserts using the same non-null token
         * will be considered updates to the existing artwork. Therefore there will always be at
         * most one artwork with the same non-null token.
         * <p>
         * This field <strong>cannot</strong> be changed after the artwork is inserted.
         * </p>
         * <P>Type: TEXT</P>
         */
        public static final String TOKEN = "token";
        /**
         * The user-visible title of the artwork
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";
        /**
         * The artwork's byline (such as author and date). This is generally used as a secondary
         * source of information after the {@link #TITLE}.
         * <P>Type: TEXT</P>
         */
        public static final String BYLINE = "byline";
        /**
         * The attribution info for the artwork
         * <P>Type: TEXT</P>
         */
        public static final String ATTRIBUTION = "attribution";
        /**
         * The persistent URI of the artwork
         * <P>Type: TEXT (Uri)</P>
         */
        public static final String PERSISTENT_URI = "persistent_uri";
        /**
         * The web URI of the artwork
         * <P>Type: TEXT (Uri)</P>
         */
        public static final String WEB_URI = "web_uri";
        /**
         * The provider specific metadata about the artwork
         * <P>Type: TEXT</P>
         */
        public static final String METADATA = "metadata";
        /**
         * Path to the file on disk.
         * <p>
         * Note that apps may not have filesystem permissions to directly access
         * this path. Instead of trying to open this path directly, apps should
         * use {@link android.content.ContentResolver#openFileDescriptor(android.net.Uri, String)}
         * to gain access.
         * <p>
         * Type: TEXT
         */
        public static final String DATA = "_data";
        /**
         * The time the file was added to the provider
         * Units are seconds since 1970.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String DATE_ADDED = "date_added";
        /**
         * The time the file was last modified
         * Units are seconds since 1970.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String DATE_MODIFIED = "date_modified";

        /**
         * Retrieve the content URI for the given {@link MuzeiArtProvider}, allowing you to build
         * custom queries, inserts, updates, and deletes using a {@link ContentResolver}.
         * <p>
         * This will throw an {@link IllegalArgumentException} if the provider is not valid or the
         * provider is disabled.
         *
         * @param context  Context used to retrieve the content URI.
         * @param provider The {@link MuzeiArtProvider} you need a content URI for
         * @return The content URI for the {@link MuzeiArtProvider}
         * @see MuzeiArtProvider#getContentUri()
         */
        @NonNull
        public static Uri getContentUri(
                @NonNull Context context,
                @NonNull Class<? extends MuzeiArtProvider> provider
        ) {
            ComponentName componentName = new ComponentName(context, provider);
            PackageManager pm = context.getPackageManager();
            String authority;
            try {
                @SuppressLint("InlinedApi")
                ProviderInfo info = pm.getProviderInfo(componentName, 0);
                authority = info.authority;
            } catch (PackageManager.NameNotFoundException e) {
                throw new IllegalArgumentException("Invalid MuzeiArtProvider: " + componentName
                        + ", is your provider disabled?", e);
            }
            return getContentUri(authority);
        }

        /**
         * Retrieve the content URI for the given {@link MuzeiArtProvider}, allowing you to build
         * custom queries, inserts, updates, and deletes using a {@link ContentResolver}.
         * <p>
         * This <strong>does not</strong> check for the validity of the MuzeiArtProvider. You can
         * use {@link PackageManager#resolveContentProvider(String, int)} passing in the
         * authority if you need to confirm the authority is valid.
         *
         * @param authority The {@link MuzeiArtProvider} you need a content URI for
         * @return The content URI for the {@link MuzeiArtProvider}
         * @see MuzeiArtProvider#getContentUri()
         */
        @NonNull
        public static Uri getContentUri(@NonNull String authority) {
            return new Uri.Builder()
                    .scheme(ContentResolver.SCHEME_CONTENT)
                    .authority(authority)
                    .build();
        }

        /**
         * Retrieve the last added artwork for the given {@link MuzeiArtProvider}.
         *
         * @param context  Context used to retrieve the artwork
         * @param provider The {@link MuzeiArtProvider} to query
         * @return The last added Artwork, or null if no artwork has been added
         * @see MuzeiArtProvider#getLastAddedArtwork()
         */
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        @Nullable
        public static com.google.android.apps.muzei.api.provider.Artwork getLastAddedArtwork(
                @NonNull Context context,
                @NonNull Class<? extends MuzeiArtProvider> provider
        ) {
            return getLastAddedArtwork(context, getContentUri(context, provider));
        }

        /**
         * Retrieve the last added artwork for the given {@link MuzeiArtProvider}.
         *
         * @param context  Context used to retrieve the artwork
         * @param authority The {@link MuzeiArtProvider} to query
         * @return The last added Artwork, or null if no artwork has been added
         * @see MuzeiArtProvider#getLastAddedArtwork()
         */
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        @Nullable
        public static com.google.android.apps.muzei.api.provider.Artwork getLastAddedArtwork(
                @NonNull Context context,
                @NonNull String authority
        ) {
            return getLastAddedArtwork(context, getContentUri(authority));
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        @Nullable
        private static com.google.android.apps.muzei.api.provider.Artwork getLastAddedArtwork(
                @NonNull Context context,
                @NonNull Uri contentUri
        ) {
            try (Cursor data = context.getContentResolver().query(
                    contentUri,
                    null, null, null,
                    BaseColumns._ID + " DESC")) {
                return data != null && data.moveToFirst()
                        ? com.google.android.apps.muzei.api.provider.Artwork.fromCursor(data)
                        : null;
            }
        }

        /**
         * Add a new piece of artwork to the given {@link MuzeiArtProvider}.
         *
         * @param context  Context used to add the artwork
         * @param provider The {@link MuzeiArtProvider} to update
         * @param artwork  The artwork to add
         * @return The URI of the newly added artwork or null if the insert failed
         * @see MuzeiArtProvider#addArtwork(com.google.android.apps.muzei.api.provider.Artwork)
         */
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        @Nullable
        public static Uri addArtwork(
                @NonNull Context context,
                @NonNull Class<? extends MuzeiArtProvider> provider,
                @NonNull com.google.android.apps.muzei.api.provider.Artwork artwork
        ) {
            return addArtwork(context, getContentUri(context, provider), artwork);
        }

        /**
         * Add a new piece of artwork to the given {@link MuzeiArtProvider}.
         *
         * @param context  Context used to add the artwork
         * @param authority The {@link MuzeiArtProvider} to update
         * @param artwork  The artwork to add
         * @return The URI of the newly added artwork or null if the insert failed
         * @see MuzeiArtProvider#addArtwork(com.google.android.apps.muzei.api.provider.Artwork)
         */
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        @Nullable
        public static Uri addArtwork(
                @NonNull Context context,
                @NonNull String authority,
                @NonNull com.google.android.apps.muzei.api.provider.Artwork artwork
        ) {
            return addArtwork(context, getContentUri(authority), artwork);
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        @Nullable
        private static Uri addArtwork(
                @NonNull Context context,
                @NonNull Uri contentUri,
                @NonNull com.google.android.apps.muzei.api.provider.Artwork artwork
        ) {
            ContentResolver contentResolver = context.getContentResolver();
            return contentResolver.insert(contentUri, artwork.toContentValues());
        }

        /**
         * Set the given {@link MuzeiArtProvider} to only show the given artwork, deleting any
         * other artwork previously added.
         * <p>
         * Only in the cases where the artwork is successfully inserted will the other artwork be
         * removed.
         *
         * @param context  Context used to set the artwork
         * @param provider The {@link MuzeiArtProvider} to update
         * @param artwork  The artwork to set
         * @return The URI of the newly set artwork or null if the insert failed
         * @see MuzeiArtProvider#setArtwork(com.google.android.apps.muzei.api.provider.Artwork)
         */
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        @Nullable
        public static Uri setArtwork(
                @NonNull Context context,
                @NonNull Class<? extends MuzeiArtProvider> provider,
                @NonNull com.google.android.apps.muzei.api.provider.Artwork artwork
        ) {
            return setArtwork(context, getContentUri(context, provider), artwork);
        }

        /**
         * Set the given {@link MuzeiArtProvider} to only show the given artwork, deleting any
         * other artwork previously added.
         * <p>
         * Only in the cases where the artwork is successfully inserted will the other artwork be
         * removed.
         *
         * @param context  Context used to set the artwork
         * @param authority The {@link MuzeiArtProvider} to update
         * @param artwork  The artwork to set
         * @return The URI of the newly set artwork or null if the insert failed
         * @see MuzeiArtProvider#setArtwork(com.google.android.apps.muzei.api.provider.Artwork)
         */
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        @Nullable
        public static Uri setArtwork(
                @NonNull Context context,
                @NonNull String authority,
                @NonNull com.google.android.apps.muzei.api.provider.Artwork artwork
        ) {
            return setArtwork(context, getContentUri(authority), artwork);
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        @Nullable
        private static Uri setArtwork(
                @NonNull Context context,
                @NonNull Uri contentUri,
                @NonNull com.google.android.apps.muzei.api.provider.Artwork artwork
        ) {
            ContentResolver contentResolver = context.getContentResolver();
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            operations.add(ContentProviderOperation.newInsert(contentUri)
                    .withValues(artwork.toContentValues())
                    .build());
            operations.add(ContentProviderOperation.newDelete(contentUri)
                    .withSelection(BaseColumns._ID + " != ?", new String[1])
                    .withSelectionBackReference(0, 0)
                    .build());
            try {
                ContentProviderResult[] results = contentResolver.applyBatch(
                        Objects.requireNonNull(contentUri.getAuthority()),
                        operations);
                return results[0].uri;
            } catch (OperationApplicationException|RemoteException e) {
                return null;
            }
        }
    }
}
