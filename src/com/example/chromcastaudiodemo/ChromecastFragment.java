package com.example.chromcastaudiodemo;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ChromecastFragment extends Fragment {

	private View m_rootView;
	private MyServer m_server;
	private String m_file = "";
	private TextView m_textView;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			m_server = new MyServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (m_server != null) {
			m_server.stop();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (m_rootView != null) {
			ViewGroup parent = (ViewGroup) m_rootView.getParent();
			try {
				parent.removeView(m_rootView);
			} catch (NullPointerException e) {

			}
		}
		else {
			m_rootView = inflater.inflate(R.layout.fragment_main, container, false);
		}

		return m_rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		fill();
	}

	private void fill() {

		try {

			Button button = (Button) m_rootView.findViewById(R.id.button1);
			Button button2 = (Button) m_rootView.findViewById(R.id.button2);
			Button button3 = (Button) m_rootView.findViewById(R.id.button3);
			Button button4 = (Button) m_rootView.findViewById(R.id.button4);

			m_textView = (TextView) m_rootView.findViewById(R.id.textView1);

			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (m_file.equals("")) {
						Toast.makeText(getContext(), "Please select the file first", Toast.LENGTH_SHORT).show();
					}
					else {
						String ip = Utils.getIPAddress(true);

						String url = "http://" + ip + ":8080" + m_file;

						((MainActivity) getActivity()).play(url);
					}
				}
			});

			button2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					((MainActivity) getActivity()).pausePlayback();
				}
			});

			button3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_GET_CONTENT);
					intent.setType("audio/mpeg");
					startActivityForResult(Intent.createChooser(intent, "Select file"), 101);
				}
			});

			button4.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					((MainActivity) getActivity()).pausePlayback();
				}
			});

		} catch (Exception e) {
			Log.e("Main", "Exception in fill: " + e.getMessage());
		}
	}

	public String getRealPathFromURI(Uri uri) {
		Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
		cursor.moveToFirst();
		int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
		return cursor.getString(idx);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
			if ((data != null) && (data.getData() != null)) {
				Uri audioFileUri = data.getData();

				m_file = getRealPathFromURI(audioFileUri);

				m_textView.setText("File : " + m_file);

			}
		}
	}
}