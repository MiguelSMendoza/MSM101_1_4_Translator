package es.netrunners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class TranslatorActivity extends Activity {

	Spinner spinnerFrom;
	Spinner spinnerTo;

	EditText original;
	EditText translation;

	String[] codes;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		codes = getResources().getStringArray(R.array.languagesCodes);

		spinnerFrom = (Spinner) findViewById(R.id.spinnerFrom);
		spinnerTo = (Spinner) findViewById(R.id.spinnerTo);

		original = (EditText) findViewById(R.id.text);
		translation = (EditText) findViewById(R.id.translation);

		ArrayAdapter adapter = ArrayAdapter.createFromResource(
				getApplicationContext(), R.array.languages,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinnerFrom.setAdapter(adapter);
		spinnerTo.setAdapter(adapter);

	}

	public void translate(View v) {
		TranslateTask translate = new TranslateTask(original.getText()
				.toString(), codes[spinnerFrom.getSelectedItemPosition()],
				codes[spinnerTo.getSelectedItemPosition()]);
		translate.execute();
	}

	public class TranslateTask extends AsyncTask<Void, String, String> {

		String from;
		String to;
		String BING_API_URL = "http://api.bing.net/json.aspx?";
		String APP_ID = "EEA58B7682FC551A04ECC84C43BFCA654CCCAFA0";
		String original;
		HttpURLConnection connection;
		URL url;

		public TranslateTask(String or, String f, String t) {
			from = f;
			to = t;
			original = or;
			try {
				url = new URL(BING_API_URL + "AppId=" + APP_ID + "&Query="
						+ original + "&Sources=Translation" + "&Version=2.2"
						+ "&Translation.SourceLanguage=" + from
						+ "&Translation.TargetLanguage=" + to);
			} catch (MalformedURLException e) {
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			String translation = "";
			try {
				connection = (HttpURLConnection) url.openConnection();
				connection.setReadTimeout(10000);
				connection.setConnectTimeout(15000);
				connection.setRequestMethod("GET");
				connection.setDoInput(true);
				connection.connect();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream(),
								"UTF-8"));

				String response = reader.readLine();
				reader.close();

				JSONObject jObject = new JSONObject(response);
				translation = jObject.getJSONObject("SearchResponse")
						.getJSONObject("Translation").getJSONArray("Results")
						.getJSONObject(0).getString("TranslatedTerm");

			} catch (UnsupportedEncodingException e) {
				publishProgress(e.getMessage()+"UnsupportedEncodingException");
			} catch (IOException e) {
				publishProgress(e.getMessage()+"IOException");
			} catch (JSONException e) {
				publishProgress(e.getMessage()+"JSONException");
			}

			return translation;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			Toast.makeText(getApplicationContext(), values[0],
					Toast.LENGTH_LONG).show();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			translation.setText(result);
		}

	}
}