/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.toro.exoplayer;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.ui.PlayerView;
import im.ene.toro.ToroPlayer;
import im.ene.toro.exoplayer.Playable.EventListener;
import im.ene.toro.helper.ToroPlayerHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;

/**
 * @author eneim (2018/01/24).
 */

public class ExoPlayerViewHelper extends ToroPlayerHelper {

  @SuppressWarnings("unused") static final String TAG = "ToroExo:Helper";
  @NonNull private final Playable playable;
  @NonNull private final MyEventListeners listeners;

  public ExoPlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri uri) {
    this(container, player, uri, ToroExo.with(container.getContext()).getDefaultCreator());
  }

  public ExoPlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri uri, @NonNull ExoCreator creator) {
    this(container, player, uri, creator, null);
  }

  public ExoPlayerViewHelper(@NonNull Container container, @NonNull ToroPlayer player,
      @NonNull Uri uri, @NonNull ExoCreator creator, @Nullable EventListener listener) {
    super(container, player);
    if (!(player.getPlayerView() instanceof PlayerView)) {
      throw new IllegalArgumentException("Require PlayerView");
    }

    listeners = new MyEventListeners();
    if (listener != null) listeners.add(listener);
    playable = creator.createPlayable(uri);
    playable.addEventListener(listeners);
  }

  @Override public void initialize(@Nullable PlaybackInfo playbackInfo) {
    playable.prepare();
    playable.attachView((PlayerView) player.getPlayerView());
    if (playbackInfo != null) playable.setPlaybackInfo(playbackInfo);
  }

  @Override public void release() {
    super.release();
    playable.detachView();
    playable.removeEventListener(listeners);
    playable.release();
  }

  @Override public void play() {
    playable.play();
  }

  @Override public void pause() {
    playable.pause();
  }

  @Override public boolean isPlaying() {
    return playable.isPlaying();
  }

  @Override public void setVolume(float volume) {
    playable.setVolume(volume);
  }

  @Override public float getVolume() {
    return playable.getVolume();
  }

  @NonNull @Override public PlaybackInfo getLatestPlaybackInfo() {
    return playable.getPlaybackInfo();
  }

  @SuppressWarnings("WeakerAccess") //
  public void addEventListener(@NonNull EventListener listener) {
    //noinspection ConstantConditions
    if (listener != null) this.listeners.add(listener);
  }

  @SuppressWarnings("WeakerAccess") //
  public void removeEventListener(EventListener listener) {
    this.listeners.remove(listener);
  }

  // A proxy, to also hook into ToroPlayerHelper's state change event.
  private class MyEventListeners extends Playable.EventListeners {

    MyEventListeners() {
    }

    @Override public void onMetadata(Metadata metadata) {
      super.onMetadata(metadata);
      Log.d(TAG, "onMetadata() called with: metadata = [" + metadata + "]");
    }

    @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      ExoPlayerViewHelper.super.onPlayerStateUpdated(playWhenReady, playbackState); // important
      Log.d(TAG, "onPlayerStateChanged() called with: playWhenReady = ["
          + playWhenReady
          + "], playbackState = ["
          + playbackState
          + "]");
      super.onPlayerStateChanged(playWhenReady, playbackState);
    }
  }
}
