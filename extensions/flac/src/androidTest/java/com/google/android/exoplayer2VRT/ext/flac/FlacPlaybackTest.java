/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.google.android.exoplayer2VRT.ext.flac;

import static org.junit.Assert.fail;

import android.content.Context;
import android.net.Uri;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.exoplayer2VRT.ExoPlaybackException;
import com.google.android.exoplayer2VRT.ExoPlayer;
import com.google.android.exoplayer2VRT.ExoPlayerFactory;
import com.google.android.exoplayer2VRT.Player;
import com.google.android.exoplayer2VRT.Renderer;
import com.google.android.exoplayer2VRT.extractor.mkv.MatroskaExtractor;
import com.google.android.exoplayer2VRT.source.MediaSource;
import com.google.android.exoplayer2VRT.source.ProgressiveMediaSource;
import com.google.android.exoplayer2VRT.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2VRT.upstream.DefaultDataSourceFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Playback tests using {@link LibflacAudioRenderer}. */
@RunWith(AndroidJUnit4.class)
public class FlacPlaybackTest {

  private static final String BEAR_FLAC_URI = "asset:///bear-flac.mka";

  @Before
  public void setUp() {
    if (!FlacLibrary.isAvailable()) {
      fail("Flac library not available.");
    }
  }

  @Test
  public void testBasicPlayback() throws Exception {
    playUri(BEAR_FLAC_URI);
  }

  private void playUri(String uri) throws Exception {
    TestPlaybackRunnable testPlaybackRunnable =
        new TestPlaybackRunnable(Uri.parse(uri), ApplicationProvider.getApplicationContext());
    Thread thread = new Thread(testPlaybackRunnable);
    thread.start();
    thread.join();
    if (testPlaybackRunnable.playbackException != null) {
      throw testPlaybackRunnable.playbackException;
    }
  }

  private static class TestPlaybackRunnable implements Player.EventListener, Runnable {

    private final Context context;
    private final Uri uri;

    private ExoPlayer player;
    private ExoPlaybackException playbackException;

    public TestPlaybackRunnable(Uri uri, Context context) {
      this.uri = uri;
      this.context = context;
    }

    @Override
    public void run() {
      Looper.prepare();
      LibflacAudioRenderer audioRenderer = new LibflacAudioRenderer();
      DefaultTrackSelector trackSelector = new DefaultTrackSelector();
      player = ExoPlayerFactory.newInstance(context, new Renderer[] {audioRenderer}, trackSelector);
      player.addListener(this);
      MediaSource mediaSource =
          new ProgressiveMediaSource.Factory(
                  new DefaultDataSourceFactory(context, "ExoPlayerExtFlacTest"),
                  MatroskaExtractor.FACTORY)
              .createMediaSource(uri);
      player.prepare(mediaSource);
      player.setPlayWhenReady(true);
      Looper.loop();
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
      playbackException = error;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      if (playbackState == Player.STATE_ENDED
          || (playbackState == Player.STATE_IDLE && playbackException != null)) {
        player.release();
        Looper.myLooper().quit();
      }
    }
  }

}
