package com.iotasol.spaceattack;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.iotasol.dodge.R;
import com.iotasol.spaceattack.model.Field;
import com.iotasol.spaceattack.model.LevelManager;
import com.startapp.android.publish.StartAppAd;

public class DodgeMain extends Activity implements Field.Delegate {

	Field field;
	LevelManager levelManager;

	FieldView fieldView;
	View menuView;
	TextView levelText;
	TextView livesText;
	TextView statusText;
	TextView bestLevelText;
	TextView bestFreePlayLevelText;
	Button continueFreePlayButton;
	View bestFreePlayLevelView;
	View bestLevelView;
	MenuItem endGameMenuItem;
	MenuItem selectBackgroundImageMenuItem;
	MenuItem preferencesMenuItem;

	private static final int ACTIVITY_PREFERENCES = 1;
	public static final String PLAY_MODE = "PLAY_MODE";
	private boolean LIVE_MODE = true;
	Handler messageHandler,handler;

	private static final int MAX_LIVES = 5;
	int lives = MAX_LIVES;
	
	/** StartAppAd object deceleration */
	private StartAppAd startAppAd = new StartAppAd(this);
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		final Bundle extras = getIntent().getExtras();

		if (extras != null) {
			LIVE_MODE = extras.getBoolean(PLAY_MODE);
		}

		messageHandler = new Handler() {
			public void handleMessage(Message m) {
				processMessage(m);
			}
		};

		levelManager = new LevelManager();
		handler = new Handler();
		field = new Field();
		field.setDelegate(this);
		field.setLevelManager(levelManager);
		field.setMaxBullets(levelManager.numberOfBulletsForCurrentLevel());

		levelText = (TextView) findViewById(R.id.levelText);
		livesText = (TextView) findViewById(R.id.livesText);
		statusText = (TextView) findViewById(R.id.statusText);

		// uncomment to clear high scores
		/*
		 * setBestLevel(true, 0); setBestLevel(false, 0);
		 */

		Button newGameButton = (Button) findViewById(R.id.newGameButton);
		newGameButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doNewGame();
			}
		});

		Button freePlayButton = (Button) findViewById(R.id.freePlayButton);
		freePlayButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doFreePlay(1);
			}
		});
		/*
		 * continueFreePlayButton.setOnClickListener(new View.OnClickListener()
		 * { public void onClick(View v) { doFreePlay(bestLevel(true)); } });
		 * 
		 * Button aboutButton = (Button) findViewById(R.id.aboutButton);
		 * aboutButton.setOnClickListener(new View.OnClickListener() { public
		 * void onClick(View v) { doAbout(); } });
		 */

		menuView = findViewById(R.id.menuView);
		updateBestLevelFields();
		menuView.requestFocus();
		menuView.setVisibility(View.INVISIBLE);
		fieldView = (FieldView) findViewById(R.id.fieldView);
		fieldView.setField(field);
		fieldView.setMessageHandler(messageHandler);

		updateFromPreferences();

	}

	@Override
	public void onPause() {
		super.onPause();
		fieldView.stop();
	}

	@Override
	public void onResume() {
		super.onResume();
		fieldView.start();

		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						if (LIVE_MODE) {
							doNewGame();
						} else {
							doFreePlay(1);
						}

					}
				});

			}
		}, 500);

	}

	/**
	 * Called when preferences activity completes, updates background image and
	 * bullet flash settings.
	 */
	void updateFromPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		boolean flash = prefs.getBoolean(DodgePreferences.FLASHING_COLORS_KEY,
				false);
		fieldView.setFlashingBullets(flash);

		boolean tilt = prefs
				.getBoolean(DodgePreferences.TILT_CONTROL_KEY, true);
		fieldView.setTiltControlEnabled(tilt);

		boolean showFPS = prefs
				.getBoolean(DodgePreferences.SHOW_FPS_KEY, false);
		fieldView.setShowFPS(showFPS);
		try {
			fieldView.setBackgroundBitmap(BitmapFactory.decodeResource(
					this.getResources(), R.drawable.bg));
		} catch (Throwable ex) {

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		endGameMenuItem = menu.add(R.string.end_game);
		preferencesMenuItem = menu.add(R.string.preferences);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == endGameMenuItem) {
			doGameOver();
		} else if (item == preferencesMenuItem) {
			Intent settingsActivity = new Intent(getBaseContext(),
					DodgePreferences.class);
			startActivityForResult(settingsActivity, ACTIVITY_PREFERENCES);
		}
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
		case ACTIVITY_PREFERENCES:
			updateFromPreferences();
			break;
		}
	}

	boolean inFreePlay() {
		return (lives < 0);
	}

	// methods to store and retrieve the highest normal and free play levels
	// reached, using SharedPreferences
	int bestLevel(boolean freePlay) {
		String key = (freePlay) ? "BestFreePlayLevel" : "BestLevel";
		return getPreferences(MODE_PRIVATE).getInt(key, 0);
	}

	void setBestLevel(boolean freePlay, int val) {
		String key = (freePlay) ? "BestFreePlayLevel" : "BestLevel";
		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putInt(key, val);
		editor.commit();
	}

	void recordCurrentLevel() {
		if (levelManager.getCurrentLevel() > bestLevel(inFreePlay())) {
			setBestLevel(inFreePlay(), levelManager.getCurrentLevel());
		}
	}

	void updateBestLevelFields() {
		// int bestNormal = bestLevel(false);
		/*
		 * bestLevelText.setText((bestNormal > 1) ? String.valueOf(bestNormal) :
		 * getString(R.string.score_none));
		 */

		// int bestFree = bestLevel(true);
		/*
		 * bestFreePlayLevelText.setText((bestFree > 1) ?
		 * String.valueOf(bestFree) : getString(R.string.score_none));
		 * continueFreePlayButton.setEnabled(bestFree > 1);
		 */
		// menuView.forceLayout();
	}

	void processMessage(Message m) {
		String action = m.getData().getString("event");
		if ("goal".equals(action)) {
			levelManager.setCurrentLevel(1 + levelManager.getCurrentLevel());
			recordCurrentLevel();
			synchronized (field) {
				field.setMaxBullets(levelManager
						.numberOfBulletsForCurrentLevel());
			}
			updateScore();
		} else if ("death".equals(action)) {
			if (lives > 0)
				lives--;
			synchronized (field) {
				if (lives == 0) {
					field.removeDodger();
					doGameOver();
				} else {
					fieldView.startDeathAnimation(field.getDodger()
							.getPosition());
					field.createDodger();
				}
			}
			updateScore();
		} else if ("start".equals(action)) {

		}
	}

	void updateScore() {
		levelText.setText(getString(R.string.level_prefix)
				+ levelManager.getCurrentLevel());
		livesText.setText(getString(R.string.lives_prefix)
				+ ((lives >= 0) ? "" + lives
						: getString(R.string.free_play_lives)));
	}

	void doGameOver() {
		field.removeDodger();
		// statusText.setText(getString(R.string.game_over_message));
		if (levelManager.getCurrentLevel() >= bestLevel(inFreePlay())) {
			statusText.setText("Congratulations!!! You Reached New Level : "
					+ levelManager.getCurrentLevel());
		} else {
			statusText.setText("You Reached Level : "
					+ levelManager.getCurrentLevel());
		}
		updateBestLevelFields();
		menuView.setVisibility(View.VISIBLE);
	}

	void startGameAtLevelWithLives(int startLevel, int numLives) {
		levelManager.setCurrentLevel(startLevel);
		field.setMaxBullets(levelManager.numberOfBulletsForCurrentLevel());
		field.createDodger();
		menuView.setVisibility(View.INVISIBLE);
		lives = numLives;
		updateScore();
	}

	void doNewGame() {
		startGameAtLevelWithLives(1, MAX_LIVES);
	}

	void doFreePlay(int startLevel) {
		startGameAtLevelWithLives(startLevel, -1);
	}

	void doAbout() {
		Intent aboutIntent = new Intent(this, DodgeAbout.class);
		this.startActivity(aboutIntent);
	}

	void sendMessage(Map params) {
		Bundle b = new Bundle();
		for (Object key : params.keySet()) {
			b.putString((String) key, (String) params.get(key));
		}
		Message m = messageHandler.obtainMessage();
		m.setData(b);
		messageHandler.sendMessage(m);
	}

	// Field.Delegate methods
	// these occur in a separate thread, so use Handler
	public void dodgerHitByBullet(Field theField) {
		Map params = new HashMap();
		params.put("event", "death");
		sendMessage(params);
	}

	public void dodgerReachedGoal(Field theField) {
		Map params = new HashMap();
		params.put("event", "goal");
		sendMessage(params);
	}
}