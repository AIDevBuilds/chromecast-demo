package com.example.chromcastaudiodemo;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity {

	protected static final String TAG = "MainActivity";

	private final double VOLUME_INCREMENT = 0.051;

	public boolean m_WaitingForReconnect, m_ApplicationStarted;
	private String m_SessionId;

	private MediaRouter m_MediaRouter;
	private MediaRouteSelector m_MediaRouteSelector;
	private RemoteMediaPlayer m_RemoteMediaPlayer;

	private GoogleApiClient m_ApiClient;
	private CastDevice m_SelectedDevice;

	private ConnectionCallbacks m_ConnectionCallbacks;
	private ConnectionFailedListener m_ConnectionFailedListener;
	private MyMediaRouterCallback m_MediaRouterCallback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initRouter();

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new ChromecastFragment()).commit();
		}

	}

	private final String getReceiverApplicationId() {

		return CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
	}

	private void initCallbacks() {

		m_MediaRouterCallback = new MyMediaRouterCallback();
		m_ConnectionCallbacks = new ConnectionCallbacks();
		m_ConnectionFailedListener = new ConnectionFailedListener();
	}

	private void initRouter() {

		m_MediaRouter = MediaRouter.getInstance(getApplicationContext());

		m_MediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(
				CastMediaControlIntent.categoryForCast(getReceiverApplicationId())).build();

		initCallbacks();

	}

	private void initGoogleApiClient() {

		@SuppressWarnings("deprecation")
		Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(m_SelectedDevice, mCastClientListener);

		m_ApiClient = new GoogleApiClient.Builder(this).addApi(Cast.API, apiOptionsBuilder.build())
				.addConnectionCallbacks(m_ConnectionCallbacks).addOnConnectionFailedListener(m_ConnectionFailedListener).build();

		m_ApiClient.connect();

	}

	private void initRemoteMediaPlayer() {

		m_RemoteMediaPlayer = new RemoteMediaPlayer();

		System.err.println("Remote Media player : " + m_RemoteMediaPlayer);

		m_RemoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
			@Override
			public void onStatusUpdated() {

				try {
					MediaStatus mediaStatus = m_RemoteMediaPlayer.getMediaStatus();

					boolean isPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;

					// loadMedia("");
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}

			}
		});

		m_RemoteMediaPlayer.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
			@Override
			public void onMetadataUpdated() {

				try {
					MediaInfo mediaInfo = m_RemoteMediaPlayer.getMediaInfo();
					MediaMetadata metadata = mediaInfo.getMetadata();
				} catch (NullPointerException e) {

				}
			}
		});

	}

	public void pausePlayback() {

		if (m_RemoteMediaPlayer != null) {

			m_RemoteMediaPlayer.pause(m_ApiClient).setResultCallback(new ResultCallback<MediaChannelResult>() {

				@Override
				public void onResult(MediaChannelResult result) {

					Status status = result.getStatus();

					if (!status.isSuccess()) {
						Log.w(TAG, "Unable to toggle pause: " + status.getStatusCode());
					}
				}
			});
		}
		else {
			Toast.makeText(getApplicationContext(), "failed to pause", Toast.LENGTH_SHORT).show();
		}

	}

	public void resumePlayback() {

		if (m_RemoteMediaPlayer != null) {

			m_RemoteMediaPlayer.play(m_ApiClient).setResultCallback(new ResultCallback<MediaChannelResult>() {

				@Override
				public void onResult(MediaChannelResult result) {

					Status status = result.getStatus();

					if (!status.isSuccess()) {
						Log.w(TAG, "Unable to toggle pause: " + status.getStatusCode());
					}
				}
			});
		}
		else {
			Toast.makeText(getApplicationContext(), "failed to pause", Toast.LENGTH_SHORT).show();
		}

	}

	public void play(final String i_url) {

		if (m_RemoteMediaPlayer != null) {

			m_RemoteMediaPlayer.requestStatus(m_ApiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
				@Override
				public void onResult(MediaChannelResult result) {
					if (result.getStatus().isSuccess()) {

						System.err.println("url : " + i_url);

						loadMedia(i_url);

					}
					else {
						Log.e(TAG, "Failed to request status.");
					}
				}
			});
		}
		else {
			Toast.makeText(getApplicationContext(), "select the device first", Toast.LENGTH_SHORT).show();
		}
	}

	public void loadMedia(String i_url) {

		if (i_url.trim().equals("")) {

		}
		else {
			Log.e("Song Path", i_url);

			MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

			mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My video");

			MediaInfo mediaInfo = new MediaInfo.Builder(i_url).setContentType("audio/mp3").setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
					.setMetadata(mediaMetadata).build();
			try {
				m_RemoteMediaPlayer.load(m_ApiClient, mediaInfo, true).setResultCallback(
						new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {

							@Override
							public void onResult(MediaChannelResult result) {

								if (result.getStatus().isSuccess()) {
									Log.d(TAG, "Media loaded successfully");

									mHandler.postDelayed(mUpdateTimeTask, 0);
								}
								else {
									Toast.makeText(getApplicationContext(), "Failed to load media", Toast.LENGTH_LONG).show();
								}

							}
						});
			} catch (IllegalStateException e) {
				Log.e(TAG, "Problem occurred with media during loading", e);
			} catch (Exception e) {
				Log.e(TAG, "Problem opening media during loading", e);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
		MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);

		mediaRouteActionProvider.setRouteSelector(m_MediaRouteSelector);

		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		m_MediaRouter.addCallback(m_MediaRouteSelector, m_MediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
		// m_MediaRouter.addCallback(m_MediaRouteSelector, m_MediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
	}

	@Override
	protected void onStop() {
		m_MediaRouter.removeCallback(m_MediaRouterCallback);
		super.onStop();
	}

	private class MyMediaRouterCallback extends MediaRouter.Callback {

		@Override
		public void onRouteSelected(MediaRouter router, RouteInfo info) {
			m_SelectedDevice = CastDevice.getFromBundle(info.getExtras());
			//String routeId = info.getId();

			initGoogleApiClient();

		}

		@Override
		public void onRouteUnselected(MediaRouter router, RouteInfo info) {
			teardown();
			m_SelectedDevice = null;
		}
	}

	private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
		@Override
		public void onConnected(Bundle connectionHint) {

			if (m_WaitingForReconnect) {
				m_WaitingForReconnect = false;
				// reconnectChannels();
			}
			else {
				try {
					Cast.CastApi.launchApplication(m_ApiClient, getReceiverApplicationId(), false).setResultCallback(
							new ResultCallback<Cast.ApplicationConnectionResult>() {
								@Override
								public void onResult(Cast.ApplicationConnectionResult result) {
									Status status = result.getStatus();
									if (status.isSuccess()) {

										//ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
										//String sessionId = result.getSessionId();
										//String applicationStatus = result.getApplicationStatus();
										//boolean wasLaunched = result.getWasLaunched();

										m_ApplicationStarted = true;

										initRemoteMediaPlayer();

										try {
											Cast.CastApi.setMessageReceivedCallbacks(m_ApiClient, m_RemoteMediaPlayer.getNamespace(),
													m_RemoteMediaPlayer);
										} catch (IOException e) {
											Log.e(TAG, "Exception while creating media channel", e);
										}

									}
									else {
										teardown();
									}
								}
							});

				} catch (Exception e) {
					System.err.println("Failed to launch application" + e);

				}
			}
		}

		@Override
		public void onConnectionSuspended(int cause) {
			m_WaitingForReconnect = true;
			// hidePDialog();
		}
	}

	private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
		@Override
		public void onConnectionFailed(ConnectionResult result) {
			teardown();
		}
	}

	private Cast.Listener mCastClientListener = new Cast.Listener() {
		@Override
		public void onApplicationStatusChanged() {
			if (m_ApiClient != null) {
				Log.d(TAG, "onApplicationStatusChanged: " + Cast.CastApi.getApplicationStatus(m_ApiClient));

			}
		}

		@Override
		public void onVolumeChanged() {
			if (m_ApiClient != null) {
				Log.d(TAG, "onVolumeChanged: " + Cast.CastApi.getVolume(m_ApiClient));
			}
		}

		@Override
		public void onApplicationDisconnected(int errorCode) {
			teardown();
		}
	};

	private void teardown() {
		Log.d(TAG, "teardown");
		if (m_ApiClient != null) {
			if (m_ApplicationStarted) {
				if (m_ApiClient.isConnected() || m_ApiClient.isConnecting()) {
					try {
						Cast.CastApi.stopApplication(m_ApiClient, m_SessionId);
						if (m_RemoteMediaPlayer != null) {
							Cast.CastApi.removeMessageReceivedCallbacks(m_ApiClient, m_RemoteMediaPlayer.getNamespace());
							m_RemoteMediaPlayer = null;
						}
					} catch (IOException e) {
						Log.e(TAG, "Exception while removing channel", e);
					}

					m_ApiClient.disconnect();
				}
				m_ApplicationStarted = false;
			}
			m_ApiClient = null;
		}
		m_SelectedDevice = null;
		m_WaitingForReconnect = false;
		m_SessionId = null;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (action == KeyEvent.ACTION_DOWN) {
				if (m_RemoteMediaPlayer != null) {
					double currentVolume = Cast.CastApi.getVolume(m_ApiClient);
					if (currentVolume < 1.0) {
						try {
							Cast.CastApi.setVolume(m_ApiClient, Math.min(currentVolume + VOLUME_INCREMENT, 1.0));
						} catch (Exception e) {
							Log.e(TAG, "unable to set volume", e);
						}
					}
				}
				else {
					Log.e(TAG, "dispatchKeyEvent - volume up");
				}
			}
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (action == KeyEvent.ACTION_DOWN) {
				if (m_RemoteMediaPlayer != null) {
					double currentVolume = Cast.CastApi.getVolume(m_ApiClient);
					if (currentVolume > 0.0) {
						try {
							Cast.CastApi.setVolume(m_ApiClient, Math.max(currentVolume - VOLUME_INCREMENT, 0.0));
						} catch (Exception e) {
							Log.e(TAG, "unable to set volume", e);
						}
					}
				}
				else {
					Log.e(TAG, "dispatchKeyEvent - volume down");
				}
			}
			return true;
		default:
			return super.dispatchKeyEvent(event);
		}
	}

	/**
	 * Temporary code for timer
	 */
	private Handler mHandler = new Handler();
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {

			System.err.println("Stream duration :" + m_RemoteMediaPlayer.getStreamDuration());
			System.err.println("approx. Stream position :" + m_RemoteMediaPlayer.getApproximateStreamPosition());

			// Running this thread after 200 milliseconds
			mHandler.postDelayed(this, 1000);

		}
	};

}
