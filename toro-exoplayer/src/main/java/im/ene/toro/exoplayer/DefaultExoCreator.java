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

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import im.ene.toro.media.PlaybackInfo;
import java.io.IOException;

import static im.ene.toro.ToroUtil.checkNotNull;
import static im.ene.toro.exoplayer.ToroExo.toro;
import static im.ene.toro.exoplayer.ToroExo.with;
import static im.ene.toro.media.PlaybackInfo.INDEX_UNSET;
import static im.ene.toro.media.PlaybackInfo.TIME_UNSET;

/**
 * @author eneim (2018/02/04).
 *
 *         Usage: use this as-it or inheritance.
 */

@SuppressWarnings({ "unused", "WeakerAccess" }) //
public class DefaultExoCreator implements ExoCreator, MediaSourceEventListener {

  private final Context context;  // per application
  private final TrackSelector trackSelector;  // 'maybe' stateless
  private final LoadControl loadControl;  // stateless
  private final MediaSourceBuilder mediaSourceBuilder;  // stateless
  private final RenderersFactory renderersFactory;  // stateless
  private final DataSource.Factory mediaDataSourceFactory;  // stateless
  private final DataSource.Factory manifestDataSourceFactory; // stateless

  @SuppressWarnings("unchecked") DefaultExoCreator(Context context, Config config, String appName) {
    this.context = context.getApplicationContext();
    trackSelector = new DefaultTrackSelector(config.meter);
    loadControl = config.loadControl;
    mediaSourceBuilder = config.mediaSourceBuilder;
    renderersFactory = new DefaultRenderersFactory(this.context,  //
        null /* config.drmSessionManager */, config.extensionMode);
    DataSource.Factory factory = new DefaultDataSourceFactory(this.context, appName, config.meter);
    if (config.cache != null) factory = new CacheDataSourceFactory(config.cache, factory);
    mediaDataSourceFactory = factory;
    manifestDataSourceFactory = new DefaultDataSourceFactory(this.context, appName);
  }

  @SuppressWarnings("unchecked")  //
  public DefaultExoCreator(Context context, Config config) {
    this(context, config, with(context).appName);
  }

  public DefaultExoCreator(Context context) {
    this(context, with(context).defaultConfig);
  }

  @SuppressWarnings("SimplifiableIfStatement") @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DefaultExoCreator that = (DefaultExoCreator) o;

    if (!context.equals(that.context)) return false;
    if (!trackSelector.equals(that.trackSelector)) return false;
    if (!loadControl.equals(that.loadControl)) return false;
    if (!mediaSourceBuilder.equals(that.mediaSourceBuilder)) return false;
    if (!renderersFactory.equals(that.renderersFactory)) return false;
    if (!mediaDataSourceFactory.equals(that.mediaDataSourceFactory)) return false;
    return manifestDataSourceFactory.equals(that.manifestDataSourceFactory);
  }

  @Override public int hashCode() {
    int result = context.hashCode();
    result = 31 * result + trackSelector.hashCode();
    result = 31 * result + loadControl.hashCode();
    result = 31 * result + mediaSourceBuilder.hashCode();
    result = 31 * result + renderersFactory.hashCode();
    result = 31 * result + mediaDataSourceFactory.hashCode();
    result = 31 * result + manifestDataSourceFactory.hashCode();
    return result;
  }

  @Override public SimpleExoPlayer createPlayer() {
    return ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
  }

  @Override public MediaSource createMediaSource(Uri uri) {
    return mediaSourceBuilder.buildMediaSource(this.context, uri, new Handler(),
        manifestDataSourceFactory, mediaDataSourceFactory, this);
  }

  @Override public Playable createPlayable(Uri uri) {
    return new PlayableImpl(this, uri);
  }

  /// MediaSourceEventListener

  @Override
  public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat,
      int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs,
      long mediaEndTimeMs, long elapsedRealtimeMs) {
    // no-ops
  }

  @Override
  public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat,
      int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs,
      long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
    // no-ops
  }

  @Override
  public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat,
      int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs,
      long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
    // no-ops
  }

  @Override
  public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat,
      int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs,
      long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded,
      IOException error, boolean wasCanceled) {
    // no-ops
  }

  @Override
  public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {
    // no-ops
  }

  @Override
  public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason,
      Object trackSelectionData, long mediaTimeMs) {
    // no-ops
  }

  /// Playable implementation

  /**
   * TODO [20180208]
   * I'm trying to reuse this thing. Not only to save resource, improve performance, but also to
   * have a way to keep the playback smooth across config change.
   */
  private static class PlayableImpl implements Playable {

    final PlaybackInfo playbackInfo = new PlaybackInfo(); // never expose to outside.
    final EventListeners listeners = new EventListeners();  // original listener.

    protected final Uri mediaUri; // immutable
    protected final ExoCreator creator; // cached

    protected SimpleExoPlayer player; // on-demand, cached
    protected PlayerView playerView; // on-demand, not always required.
    protected MediaSource mediaSource;  // on-demand
    private boolean listenerApplied = false;

    public PlayableImpl(@NonNull ExoCreator creator, @NonNull Uri uri) {
      this.creator = checkNotNull(creator);
      this.mediaUri = checkNotNull(uri);
    }

    @Override public void prepare() {
      if (player == null) player = requestPlayer(creator);
      if (!listenerApplied) {
        player.addListener(listeners);
        player.addVideoListener(listeners);
        player.addTextOutput(listeners);
        listenerApplied = true;
      }
      if (playerView != null && playerView.getPlayer() != player) playerView.setPlayer(player);
      boolean haveResumePosition = playbackInfo.getResumeWindow() != C.INDEX_UNSET;
      if (haveResumePosition) {
        player.seekTo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
      }
      if (mediaSource == null) {
        mediaSource = creator.createMediaSource(mediaUri);
        player.prepare(mediaSource, !haveResumePosition, false);
      }
    }

    @Override public void attachView(@NonNull PlayerView playerView) {
      //noinspection ConstantConditions
      if (playerView == null) throw new IllegalArgumentException("PlayerView is null.");
      if (this.playerView == playerView) return;
      if (this.player == null) {
        this.playerView = playerView; // before the prepare.
        this.prepare();
      } else {
        PlayerView.switchTargetView(this.player, this.playerView, playerView);
        this.playerView = playerView; // after the switch.
      }
    }

    @Override public void detachView() {
      if (this.playerView != null) {
        this.playerView.setPlayer(null);
        this.playerView = null;
      }
    }

    @Override public PlayerView getPlayerView() {
      return this.playerView;
    }

    @Nullable @Override public SimpleExoPlayer getPlayer() {
      return this.player;
    }

    @Override public void play() {
      checkNotNull(player, "Playable#play(): Player is null!").setPlayWhenReady(true);
    }

    @Override public void pause() {
      checkNotNull(player, "Playable#pause(): Player is null!").setPlayWhenReady(false);
    }

    @Override public void reset() {
      this.playbackInfo.reset();
      if (player != null) {
        player.stop(true);  // back to IDLE first, reset positions and windows.
        // re-prepare using new MediaSource instance.
        // TODO [20180214] Maybe change this when ExoPlayer 2.7.0 is finally released.
        mediaSource = creator.createMediaSource(mediaUri);
        player.prepare(mediaSource);
      }
    }

    @Override public void release() {
      if (this.playerView != null) detachView(); // detach view if need
      if (player != null) {
        player.stop(true);
        if (listenerApplied) {
          player.removeListener(listeners);
          player.removeVideoListener(listeners);
          player.removeTextOutput(listeners);
          listenerApplied = false;
        }
        toro.getPool(creator).release(player);
      }
      player = null;
      mediaSource = null;
    }

    @NonNull @Override public PlaybackInfo getPlaybackInfo() {
      updatePlaybackInfo();
      return new PlaybackInfo(playbackInfo.getResumeWindow(), playbackInfo.getResumePosition());
    }

    @Override public void setPlaybackInfo(@NonNull PlaybackInfo playbackInfo) {
      this.playbackInfo.setResumeWindow(playbackInfo.getResumeWindow());
      this.playbackInfo.setResumePosition(playbackInfo.getResumePosition());

      if (player != null) {
        boolean haveResumePosition = this.playbackInfo.getResumeWindow() != INDEX_UNSET;
        if (haveResumePosition) {
          player.seekTo(this.playbackInfo.getResumeWindow(), this.playbackInfo.getResumePosition());
        }
      }
    }

    @Override public void addEventListener(@NonNull EventListener listener) {
      //noinspection ConstantConditions
      if (listener != null) this.listeners.add(listener);
    }

    @Override public void removeEventListener(EventListener listener) {
      this.listeners.remove(listener);
    }

    @Override public void setVolume(float volume) {
      checkNotNull(player, "Playable#setVolume(): Player is null!").setVolume(volume);
    }

    @Override public float getVolume() {
      return checkNotNull(player, "Playable#getVolume(): Player is null!").getVolume();
    }

    @Override public boolean isPlaying() {
      return player != null && player.getPlayWhenReady();
    }

    private void updatePlaybackInfo() {
      if (player == null || player.getPlaybackState() == Player.STATE_IDLE) return;
      playbackInfo.setResumeWindow(player.getCurrentWindowIndex());
      playbackInfo.setResumePosition(player.isCurrentWindowSeekable() ? //
          Math.max(0, player.getCurrentPosition()) : TIME_UNSET);
    }
  }

  /**
   * A helper method to help clients to request a {@link SimpleExoPlayer} instance.
   */
  @NonNull public static SimpleExoPlayer requestPlayer(@NonNull ExoCreator creator) {
    SimpleExoPlayer player = toro.getPool(checkNotNull(creator)).acquire();
    if (player == null) player = creator.createPlayer();
    return player;
  }
}
