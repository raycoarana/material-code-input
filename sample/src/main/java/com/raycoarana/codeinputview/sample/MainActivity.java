package com.raycoarana.codeinputview.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.raycoarana.codeinputview.CodeInputView;

public class MainActivity extends AppCompatActivity {

	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final CodeInputView otherCodeInput = findViewById(R.id.pairing);
		otherCodeInput.addOnCompleteListener(code -> mHandler.postDelayed(() -> {
			//Make the input enable again so the user can change it
			otherCodeInput.setEditable(true);

			//Show error
			otherCodeInput.setError("Your code is incorrect");
		}, 1000));

		final CodeInputView codeInputView = findViewById(R.id.with_complete_callback);

		//Default value
		codeInputView.setCode("23");
		codeInputView.setPasswordMode(true);

		//Action to do when completed
		codeInputView.addOnCompleteListener(code -> {
			Toast.makeText(MainActivity.this, "Your code: " + code, Toast.LENGTH_SHORT).show();

			mHandler.postDelayed(() -> {
				//Make the input enable again so the user can change it
				codeInputView.setEditable(true);

				//Show error
				codeInputView.setError("Your code is incorrect");
			}, 1000);
		});
	}
}
