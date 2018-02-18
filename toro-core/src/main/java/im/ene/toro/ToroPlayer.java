/*
 * Copyright (c) 2017 Nam Nguyen, nam@ene.im
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

package im.ene.toro;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.VideoView;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author eneim | 5/31/17.
 */

public interface ToroPlayer {

  /**
   * Called by {@link Container} and other classes to make sure current {@link ToroPlayer} has a
   * valid View used for playback. It can be {@link VideoView} or any non-Surface View, depends on
   * Client's implementation.
   *
   * @return current valid {@link View} for the playback.
   */
  @NonNull View getPlayerView();

  /**
   * Returns current {@link PlaybackInfo} of the playback.
   *
   * @return current {@link PlaybackInfo} of the playback.
   */
  @NonNull PlaybackInfo getCurrentPlaybackInfo();

  /**
   * Initialize resource for the incoming playback. After this point, {@link ToroPlayer} should be
   * able to start the playback at anytime in the future (This doesn't mean that any call to {@link
   * ToroPlayer#play()} will start the playback immediately. It may start buffering enough resource
   * for rendering).
   *
   * @param container the RecyclerView contains this Player.
   * @param playbackInfo initialize info for the preparation.
   */
  void initialize(@NonNull Container container, @Nullable PlaybackInfo playbackInfo);

  /**
   * Start playback or resume from a pausing state.
   */
  void play();

  /**
   * Pause current playback.
   */
  void pause();

  boolean isPlaying();

  /**
   * Called when the {@link RecyclerView.ViewHolder} is detached from the {@link Container}. This
   * means that the {@link RecyclerView.ViewHolder} may still hold the data, but its {@link View}
   * has been detached from the Window, and it is released to the Scrap heap.
   *
   * Client should tear down all the setup and release all playback setup.
   */
  void release();

  boolean wantsToPlay();

  /**
   * @return prefer playback order in list. Can be customized.
   */
  int getPlayerOrder();

  /**
   * Notify a Player about its {@link Container}'s scroll state change.
   *
   * Deprecated, no-longer used.
   */
  @Deprecated void onSettled(Container container);

  /**
   * A convenient callback to help {@link ToroPlayer} to listen to different playback states.
   */
  interface EventListener {

    void onBuffering(); // ExoPlayer state: 2

    void onPlaying(); // ExoPlayer state: 3, play flag: true

    void onPaused();  // ExoPlayer state: 3, play flag: false

    void onCompleted(); // ExoPlayer state: 4
  }

  // Adapt from ExoPlayer.
  @Retention(RetentionPolicy.SOURCE)  //
  @IntDef({ State.STATE_IDLE, State.STATE_BUFFERING, State.STATE_READY, State.STATE_END })  //
  @interface State {
    int STATE_IDLE = 1;
    int STATE_BUFFERING = 2;
    int STATE_READY = 3;
    int STATE_END = 4;
  }
}
