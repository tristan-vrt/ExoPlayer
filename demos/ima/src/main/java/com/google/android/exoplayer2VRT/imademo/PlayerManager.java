/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2VRT.imademo;

import android.content.Context;
import android.net.Uri;
import com.google.android.exoplayer2VRT.C;
import com.google.android.exoplayer2VRT.C.ContentType;
import com.google.android.exoplayer2VRT.ExoPlayer;
import com.google.android.exoplayer2VRT.ExoPlayerFactory;
import com.google.android.exoplayer2VRT.SimpleExoPlayer;
import com.google.android.exoplayer2VRT.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2VRT.source.MediaSource;
import com.google.android.exoplayer2VRT.source.ProgressiveMediaSource;
import com.google.android.exoplayer2VRT.source.ads.AdsMediaSource;
import com.google.android.exoplayer2VRT.source.dash.DashMediaSource;
import com.google.android.exoplayer2VRT.source.hls.HlsMediaSource;
import com.google.android.exoplayer2VRT.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2VRT.ui.PlayerView;
import com.google.android.exoplayer2VRT.upstream.DataSource;
import com.google.android.exoplayer2VRT.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2VRT.util.Util;

/** Manages the {@link ExoPlayer}, the IMA plugin and all video playback. */
/* package */ final class PlayerManager implements AdsMediaSource.MediaSourceFactory {

  private final ImaAdsLoader adsLoader;
  private final DataSource.Factory dataSourceFactory;

  private SimpleExoPlayer player;
  private long contentPosition;

  public PlayerManager(Context context) {
    String adTag = context.getString(R.string.ad_tag_url);
    adsLoader = new ImaAdsLoader(context, Uri.parse(adTag));
    dataSourceFactory =
        new DefaultDataSourceFactory(
            context, Util.getUserAgent(context, context.getString(R.string.application_name)));
  }

  public void init(Context context, PlayerView playerView) {
    // Create a player instance.
    player = ExoPlayerFactory.newSimpleInstance(context);
    adsLoader.setPlayer(player);
    playerView.setPlayer(player);

    // This is the MediaSource representing the content media (i.e. not the ad).
    String contentUrl = context.getString(R.string.content_url);
    MediaSource contentMediaSource = buildMediaSource(Uri.parse(contentUrl));

    // Compose the content media source into a new AdsMediaSource with both ads and content.
    MediaSource mediaSourceWithAds =
        new AdsMediaSource(
            contentMediaSource, /* adMediaSourceFactory= */ this, adsLoader, playerView);

    // Prepare the player with the source.
    player.seekTo(contentPosition);
    player.prepare(mediaSourceWithAds);
    player.setPlayWhenReady(true);
  }

  public void reset() {
    if (player != null) {
      contentPosition = player.getContentPosition();
      player.release();
      player = null;
      adsLoader.setPlayer(null);
    }
  }

  public void release() {
    if (player != null) {
      player.release();
      player = null;
    }
    adsLoader.release();
  }

  // AdsMediaSource.MediaSourceFactory implementation.

  @Override
  public MediaSource createMediaSource(Uri uri) {
    return buildMediaSource(uri);
  }

  @Override
  public int[] getSupportedTypes() {
    // IMA does not support Smooth Streaming ads.
    return new int[] {C.TYPE_DASH, C.TYPE_HLS, C.TYPE_OTHER};
  }

  // Internal methods.

  private MediaSource buildMediaSource(Uri uri) {
    @ContentType int type = Util.inferContentType(uri);
    switch (type) {
      case C.TYPE_DASH:
        return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
      case C.TYPE_SS:
        return new SsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
      case C.TYPE_HLS:
        return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
      case C.TYPE_OTHER:
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
      default:
        throw new IllegalStateException("Unsupported type: " + type);
    }
  }

}
