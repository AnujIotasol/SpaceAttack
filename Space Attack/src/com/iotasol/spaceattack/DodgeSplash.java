package com.iotasol.spaceattack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.iotasol.dodge.R;
import com.startapp.android.publish.StartAppAd;

public class DodgeSplash extends Activity {

	private final int DELAY_TIME = 3000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		StartAppAd.init(DodgeSplash.this, "102536442", "202730676");
		
		setContentView(R.layout.splash);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				/*
				 * mainIntent = new Intent(getApplicationContext(),
				 * AdActivity.class);
				 */

				startActivity(new Intent(getApplicationContext(),
						DodgeMenu.class));
				DodgeSplash.this.finish();

			}
		}, DELAY_TIME);
	}

}