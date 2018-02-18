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

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools;
import com.google.android.exoplayer2.SimpleExoPlayer;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;

import static com.google.android.exoplayer2.util.Util.getUserAgent;
import static im.ene.toro.exoplayer.BuildConfig.LIB_NAME;
import static java.lang.Runtime.getRuntime;

/**
 * Global helper class to manage {@link ExoCreator} and {@link SimpleExoPlayer} instances. In this
 * setup, {@link ExoCreator} and {@link SimpleExoPlayer} pools are cached. A {@link Config} is a key
 * for each {@link ExoCreator}.
 *
 * ExoCreator creator = ToroExo.with(this).getDefaultCreator();
 * SimpleExoPlayer player = creator.createPlayer();
 * MediaSource source = creator.createMediaSource(videoUri);
 * // TODO for client: do stuff with SimpleExoPlayer instance and MediaSource instance.
 *
 * @author eneim (2018/01/26).
 */

public final class ToroExo {

  @SuppressLint("StaticFieldLeak") static volatile ToroExo toro;
  private static final int MAX_POOL_CAPACITY = Math.max(4, getRuntime().availableProcessors());

  public static ToroExo with(Context context) {
    if (toro == null) {
      synchronized (ToroExo.class) {
        if (toro == null) toro = new ToroExo(context);
      }
    }
    return toro;
  }

  final String appName;
  final Config defaultConfig;
  @SuppressWarnings("WeakerAccess") //
  final ExoCreator defaultCreator;

  @NonNull private final Context context;  // Application context
  @NonNull private final Map<Config, ExoCreator> creators;
  @NonNull private final Map<ExoCreator, Pools.Pool<SimpleExoPlayer>> playerPools;

  private ToroExo(Context context) {
    this.context = context.getApplicationContext();
    this.appName = getUserAgent(context.getApplicationContext(), LIB_NAME);
    this.playerPools = new HashMap<>();
    this.creators = new HashMap<>();
    this.defaultConfig = new Config.Builder().build();
    this.defaultCreator = new DefaultExoCreator(this.context, this.defaultConfig);

    // Adapt from ExoPlayer demo app. Start this on demand.
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    if (CookieHandler.getDefault() != cookieManager) {
      CookieHandler.setDefault(cookieManager);
    }
  }

  public ExoCreator getCreator(Config config) {
    ExoCreator creator = this.creators.get(config);
    if (creator == null) {
      creator = new DefaultExoCreator(context, config);
      this.creators.put(config, creator);
    }

    return creator;
  }

  public ExoCreator getDefaultCreator() {
    return this.defaultCreator;
  }

  // Release and clear all current cached ExoPlayer instances. This should be called when
  // Client Application runs out of memory (Application#onTrimMemory for example).
  public void cleanUp() {
    for (Pools.Pool<SimpleExoPlayer> pool : playerPools.values()) {
      SimpleExoPlayer item;
      while ((item = pool.acquire()) != null) {
        item.release();
      }
    }
  }

  /// internal APIs
  Pools.Pool<SimpleExoPlayer> getPool(@NonNull ExoCreator creator) {
    Pools.Pool<SimpleExoPlayer> pool = playerPools.get(creator);
    if (pool == null) {
      pool = new Pools.SimplePool<>(MAX_POOL_CAPACITY);
      playerPools.put(creator, pool);
    }

    return pool;
  }
}