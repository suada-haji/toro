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

package toro.demo.exoplayer.playable

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.exoplayer2.Player
import im.ene.toro.exoplayer.Playable
import im.ene.toro.exoplayer.Playable.DefaultEventListener
import kotlinx.android.synthetic.main.activity_single_player.app_bar
import kotlinx.android.synthetic.main.activity_single_player.toolbar
import kotlinx.android.synthetic.main.content_single_player.container1
import kotlinx.android.synthetic.main.content_single_player.container2
import kotlinx.android.synthetic.main.content_single_player.playerView
import kotlinx.android.synthetic.main.content_single_player.playerView2
import toro.demo.exoplayer.DemoApp
import toro.demo.exoplayer.R

/**
 * Demo for @see [Playable]. Written in Kotlin.
 */
class PlayableDemoActivity : AppCompatActivity() {

    companion object {
        private val TAG = "Toro:Demo"
        private val videoUri = Uri.parse("file:///android_asset/bbb/video.mp4")
    }

    private var playable: Playable? = null
    private val offsetChangeListener: OnOffsetChangedListener by lazy {
        OnOffsetChangedListener { app_bar, offset ->
            // val rate = ((app_bar.height + offset.toFloat()) / app_bar.height.toFloat())
            // if (rate < 0.75) playable!!.pause() else if (!playable!!.isPlaying) playable!!.play()
        }
    }

    private val listener = object : DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.w(TAG, "Playing: $playWhenReady, State: $playbackState")
            val active = playbackState > Player.STATE_IDLE && playbackState < Player.STATE_ENDED
            playerView.keepScreenOn = active
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_player)
        setSupportActionBar(toolbar)

        playable = DemoApp.exoCreator!!.createPlayable(videoUri)
        playable!!.addEventListener(listener)
        playable!!.prepare()

        app_bar.addOnOffsetChangedListener(offsetChangeListener)

        container1.setOnLongClickListener {
            playable?.attachView(playerView)
            true
        }

        container2.setOnLongClickListener {
            playable?.attachView(playerView2)
            true
        }
    }

    override fun onStart() {
        super.onStart()
        playable!!.attachView(playerView)
        playable!!.play()
    }

    override fun onStop() {
        super.onStop()
        playable!!.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        playable!!.removeEventListener(listener)
        playable!!.apply {
            this.detachView()
            this.release()
        }
        app_bar.removeOnOffsetChangedListener(offsetChangeListener)
    }
}
