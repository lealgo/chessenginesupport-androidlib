package com.kalab.chess.leelaengine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.style.StyleSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ForegroundColorSpan;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
}
