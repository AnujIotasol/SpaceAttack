package com.iotasol.spaceattack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.iotasol.dodge.R;
import com.searchboxsdk.android.StartAppSearch;
import com.startapp.android.publish.Ad;
import com.startapp.android.publish.AdDisplayListener;
import com.startapp.android.publish.StartAppAd;

public class DodgeMenu extends Activity implements OnClickListener {

	private ImageView iv_freemode, iv_livemode, iv_copyrights, iv_highscores;

	/** Called when the activity is first created. */
	
	/** StartAppAd object deceleration */
	private StartAppAd startAppAd = new StartAppAd(this);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.menu);
		
		StartAppSearch.showSearchBox(this);

		iv_freemode = (ImageView) this.findViewById(R.id.iv_freemode);
		iv_livemode = (ImageView) this.findViewById(R.id.iv_lifemode);
		iv_copyrights = (ImageView) this.findViewById(R.id.iv_copyrights);
		iv_highscores = (ImageView) this.findViewById(R.id.iv_highscores);

		iv_freemode.setOnClickListener(this);
		iv_livemode.setOnClickListener(this);
		iv_copyrights.setOnClickListener(this);
		iv_highscores.setOnClickListener(this);
		((Button) findViewById(R.id.btnFreeApp)).setOnClickListener(this);

	}

	

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.iv_copyrights:
			goToCopyrights();
			break;
		case R.id.iv_freemode:
			goToFreeMode();
			break;
		case R.id.iv_lifemode:
			goToLifeMode();
			break;
		case R.id.iv_highscores:
			goToHighScores();
			break;
		case R.id.btnFreeApp:
			startAppAd.showAd(new AdDisplayListener() {
				@Override
				public void adHidden(Ad ad) {
					
				}
				@Override
				public void adDisplayed(Ad ad) {

				}
			});
			break;
		}
	}

	private void goToFreeMode() {
		
		startAppAd.showAd(new AdDisplayListener() {
			
			@Override
			public void adHidden(Ad arg0) {
				Intent intent = new Intent(DodgeMenu.this, DodgeMain.class);
				intent.putExtra(DodgeMain.PLAY_MODE, false);
				startActivity(intent);		
			}
			
			@Override
			public void adDisplayed(Ad arg0) {
				
			}
		});
		
		
	}

	private void goToLifeMode() {
		Intent intent = new Intent(DodgeMenu.this, DodgeMain.class);
		intent.putExtra(DodgeMain.PLAY_MODE, true);
		startActivity(intent);
		
		
	}

	private void goToCopyrights() {
		
	}

	private void goToHighScores() {

	}
	
	@Override
	public void onResume() {
		super.onResume();
		startAppAd.onResume();
	}

	/**
	 * part of Activity life cycle Need as part of the StartAppAd
	 * object life cycle
	 */
	@Override
	public void onPause() {
		super.onPause();
		startAppAd.onPause();
	}

}