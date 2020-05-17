package com.kalab.chess.leelaengine;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.style.StyleSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ForegroundColorSpan;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

	private class HeaderView extends LinearLayout {
		public HeaderView(Context context) {
			super(context);
			ImageView icon = new ImageView(context);
			icon.setImageResource(R.drawable.ic_launcher);
			int iconSize = getRawPixels(48f);
			LayoutParams iconParams = LinearLayoutParams();
			iconParams.width = iconSize;
			iconParams.height = iconSize;
			iconParams.gravity = Gravity.CENTER_VERTICAL;
			addView(icon, iconParams);
			TextView text = new TextView(context);
			text.setGravity(Gravity.CENTER);
			text.setText(R.string.app_name);
			LayoutParams textParams = LinearLayoutParams();
			textParams.gravity = Gravity.CENTER_VERTICAL;
			addView(text, textParams);
		}
	}

	private class VerticalSeparator extends View {
		private final int col = 0xff808080;
		public VerticalSeparator(Context context) {
			super(context, null);
			setBackgroundColor(col);
		}
		public VerticalSeparator(Context context, AttributeSet attrs) {
			super(context, attrs);
			setBackgroundColor(col);
		}
		@Override
		public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getRawPixels(1f), MeasureSpec.EXACTLY));
		}
	}

	private static String baseDirPath = "lc0";
	private static String weightsDirPath = "networks";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createDirectories();
		createLayout();
	}

	void createLayout() {
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		int spacing = getRawPixels(12f);
		layout.setPadding(spacing, spacing, spacing, spacing);
		HeaderView header = new HeaderView(this);
		LinearLayout.LayoutParams headerParams = LinearLayoutParams();
		headerParams.gravity = Gravity.CENTER_HORIZONTAL;
		layout.addView(header, headerParams);
		layout.addView(new VerticalSeparator(this));
		TextView label = new TextView(this);
		label.setMovementMethod(LinkMovementMethod.getInstance());
		label.setText(getLabel());
		LinearLayout.LayoutParams labelParams = LinearLayoutParams();
		labelParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
		labelParams.weight = 1.0f;
		labelParams.topMargin = spacing;
		layout.addView(label, labelParams);
		TextView notice = new TextView(this);
		notice.setText(getNotice());
		layout.addView(notice);
		setContentView(layout);
	}

	String getVersionName() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "undefined";
		}
	}

	String getNetworkWeights() {
		return getText(R.string.network_weights).toString();
	}

	private CharSequence getLabel()  {
		SpannableStringBuilder b = new SpannableStringBuilder();
		appendLine(b, getText(R.string.includes_the_following_engines));
		appendDetail(b, "Chess Engine Version", getVersionName());
		appendDetail(b, "Network Weights File", getNetworkWeights());
		appendLine(b, getText(R.string.to_use_them));
		appendLink(b, getText(R.string.released_under), getText(R.string.source_code));
		return b;
	}

	private CharSequence getNotice()  {
		if (!getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
			return getText(R.string.storage_permission);
		else
			return null;
	}

	private static void appendLine(SpannableStringBuilder b, CharSequence line) {
		b.append(line);
		b.append("\n\n");
	}

	private static void appendDetail(SpannableStringBuilder b, String title, String subtitle) {
		b.append("    ");
		int nameStart = b.length();
		b.append(title);
		b.setSpan(new StyleSpan(Typeface.BOLD), nameStart, b.length(), 0);
		b.append("\n    ");
		int subtitleStart = b.length();
		b.append(subtitle);
		b.setSpan(new RelativeSizeSpan(0.8f), subtitleStart, b.length(), 0);
		b.setSpan(new ForegroundColorSpan(0xff808080), subtitleStart, b.length(), 0);
		b.append("\n\n");
	}

	private void appendLink(SpannableStringBuilder b, CharSequence line, final CharSequence link) {
		b.append(line);
		b.append(" (");
		int downloadLinkStart = b.length();
		b.append("source code");
		b.setSpan(new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				launchUri(link);
			}
		}, downloadLinkStart, b.length(), 0);
		b.append(").");
	}

	void launchUri(CharSequence uri) {
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString())));
		} catch (Exception e) {
			//Log.w(LOG_TAG, "cannot display website: $e");
		}
	}

	private int getRawPixels(float dp) {
		return (int)(dp * getResources().getDisplayMetrics().density + 0.5f);
	}

	private LinearLayout.LayoutParams LinearLayoutParams() {
		return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	}

	/** Create directory structure on external storage. */
	private void createDirectories() {
		if (!getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
			return;
		copyAssets(baseDirPath, weightsDirPath);
	}

	boolean getPermission(String permission) {
		if (ContextCompat.checkSelfPermission(this, permission) ==
				PackageManager.PERMISSION_GRANTED) {
			return true;
		} else {
			ActivityCompat.requestPermissions(this, new String[]{permission}, 0);
			return false;
		}
	}

	@Override
	public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
		for (int i = 0; i < permissions.length; ++i) {
			if (results[i] == PackageManager.PERMISSION_GRANTED) {
				if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					createDirectories();
				}
			}
		}
	}

	/** Copy assets to external storage. */
	private void copyAssets(String base, String dir) {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list(dir);
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		if (files != null) {
			for (String filename : files) {
				InputStream in = null;
				OutputStream out = null;
				try {
					File outDir = new File(Environment.getExternalStorageDirectory() + File.separator + base + File.separator + dir);
					outDir.mkdirs();
					File outFile = new File(outDir,  filename);

					AssetFileDescriptor asset = assetManager.openFd(dir + File.separator + filename);
					if (asset.getLength() != outFile.length()) {
						in = asset.createInputStream();
						out = new FileOutputStream(outFile);
						copyFile(in, out);
					}
				} catch (IOException e) {
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// NOOP
						}
					}
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							// NOOP
						}
					}
				}
			}
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}
}
