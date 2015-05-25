package com.bionym.kbcpulse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class WelcomeActivity extends Activity {
//	static final String LOG_TAG = "KBCPulseLog";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);

		ImageView imagewelcome = (ImageView) findViewById(R.id.imagewelcome);

		imagewelcome.setOnClickListener(new View.OnClickListener() {
			long lastClickTime; // for double click protection

			@Override
			public void onClick(View v) {
				if (System.currentTimeMillis() - lastClickTime >= 1000) { // double
																			// click
																			// protection
					lastClickTime = System.currentTimeMillis();

					Intent intent = new Intent(WelcomeActivity.this,
							NymiConnectionActivity.class);
					startActivity(intent);

				}
			}
		});
	}
}
