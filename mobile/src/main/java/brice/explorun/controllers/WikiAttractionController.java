package brice.explorun.controllers;


import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import brice.explorun.fragments.RouteInfoFragment;
import brice.explorun.models.CustomRequestQueue;
import brice.explorun.models.Place;
import brice.explorun.utilities.Utility;

import static com.android.volley.VolleyLog.TAG;

public class WikiAttractionController {


	private RouteInfoFragment observer;
	private final String WIKI_API_BASE_URL = "https://fr.wikipedia.org/w/api.php?";
	private String attractionText;

	public WikiAttractionController(RouteInfoFragment observer){
		this.observer = observer;
	}


	private String getWikiApiUrl(String attractionName)
	{
		Uri builtUri = Uri.parse(WIKI_API_BASE_URL).buildUpon()
				.appendQueryParameter("action","query")
				.appendQueryParameter("titles", attractionName)
				.appendQueryParameter("prop","revisions")
				.appendQueryParameter("rvprop","content")
				.appendQueryParameter("format","json")
				.build();

		return builtUri.toString();
	}

	public void getWikiAttraction(Place place)
	{
		final Place fplace= place;
		if (Utility.isOnline(this.observer.getActivity())) {
				String url = getWikiApiUrl(place.getName());
				Log.i("url", url);
			final WikiAttractionController _this = this;
			JsonObjectRequest request = new JsonObjectRequest(
					Request.Method.GET,
					url,
					null,
					new Response.Listener<JSONObject>() {

						@Override
						public void onResponse(JSONObject response ) {
							_this.onResponse(response, fplace);
						}
					},
					new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError error) {
							_this.onErrorResponse(error);
						}
					}
			);

			CustomRequestQueue.getInstance(this.observer.getActivity()).getRequestQueue().add(request);

		}
	}

	public void getWikiAttractionByString(String attractionName, Place place)
	{
		final Place fplace = place;
		String url = getWikiApiUrl(attractionName);
		final WikiAttractionController _this = this;
		JsonObjectRequest request = new JsonObjectRequest(
				Request.Method.GET,
				url,
				null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						_this.onResponse(response, fplace);
					}
				},
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						_this.onErrorResponse(error);
					}
				}
		);

		CustomRequestQueue.getInstance(this.observer.getActivity()).getRequestQueue().add(request);
	}


	public void onResponse(JSONObject response, Place place)
	{
		attractionText = " ";
		try
		{
			String status = response.getString("query");

			Pattern p = Pattern.compile("\"pages\":\\{\"-1\"");
			Matcher m = p.matcher(status);
			if (m.find())
			{
				Log.d(TAG,"Page non trouvée");
			}
			else
			{
				p = Pattern.compile("\"\\*\":\"#REDIRECT");
				m = p.matcher(status);
				if (m.find()){
					status = status.replaceAll(".*\"\\*\":\"#REDIRECT(.*)","$1");
					status = status.replaceAll("\\[","");
					status = status.replaceAll("\\]","");
					status = status.replaceAll("\\}","");
					status = status.replaceAll("\"","");
					getWikiAttractionByString(status, place);
				}
				p = Pattern.compile("\"\\*\":\"#REDIRECTION");
				m = p.matcher(status);
				if (m.find()){
					status = status.replaceAll(".*\"\\*\":\"#REDIRECTION(.*)","$1");
					status = status.replaceAll("\\[","");
					status = status.replaceAll("\\]","");
					status = status.replaceAll("\\}","");
					status = status.replaceAll("\"","");
					getWikiAttractionByString(status, place);
				}
				else {
					attractionText  = textCleaner(status);
				}
			}
			place.setDescription(attractionText);
			this.observer.updatePlaceDesc(place);
		}
		catch (JSONException e)
		{
			Log.e(TAG,e.toString());
		}
	}

	public void onErrorResponse(VolleyError error)
	{
		Log.e(TAG,error.toString());
	}

	public String textCleaner(String status){

		String paragraph;

		Pattern p = Pattern.compile("'''");
		Matcher m = p.matcher(status);
		if (m.find()) {
			paragraph = status.substring(status.indexOf("'''"), status.indexOf("=="));
		}
		else {
			paragraph = status;
		}
		paragraph = paragraph.replaceAll("^n","");
		paragraph = paragraph.replaceAll("\u00e0","à");
		paragraph = paragraph.replaceAll("\u00e2","â");
		paragraph = paragraph.replaceAll("\u00e4","ä");
		paragraph = paragraph.replaceAll("\u00e7","ç");
		paragraph = paragraph.replaceAll("\u00e8","è");
		paragraph = paragraph.replaceAll("\u00e9","é");
		paragraph = paragraph.replaceAll("\u00ea","ê");
		paragraph = paragraph.replaceAll("\u00eb","ë");
		paragraph = paragraph.replaceAll("\u00ee","î");
		paragraph = paragraph.replaceAll("\u00ef","ï");
		paragraph = paragraph.replaceAll("\u00f4","ô");
		paragraph = paragraph.replaceAll("\u00f6","ö");
		paragraph = paragraph.replaceAll("\u00f9","ù");
		paragraph = paragraph.replaceAll("\u00fb","û");
		paragraph = paragraph.replaceAll("\u00fc","ü");
		paragraph = paragraph.replaceAll("\u2019","'");
		paragraph = paragraph.replaceAll("\u00ab","");
		paragraph = paragraph.replaceAll("\u00bb","");
		paragraph = paragraph.replaceAll("\u0153","œ");
		paragraph = paragraph.replaceAll("\\\\n","");
		paragraph = paragraph.replaceAll("<ref.*?/>","");
		paragraph = paragraph.replaceAll("<ref.*?ref>","");
		paragraph = paragraph.replaceAll("\\{\\{Prononciation.*?\\}\\}","");
		paragraph = paragraph.replaceAll("\\[\\[.[^,\\]\\]]*?\\|(.*?)\\]\\]", "$1");
		paragraph = paragraph.replaceAll("\\{\\{unité\\|(.[^,\\}\\}]*)\\|(.[^,\\}\\}]*)\\}\\}","$1 $2");
		paragraph = paragraph.replaceAll("\\{\\{note.*note\\}\\}","");
		paragraph = paragraph.replaceAll("\\{\\{","");
		paragraph = paragraph.replaceAll("\\[\\[","");
		paragraph = paragraph.replaceAll("]]","");
		paragraph = paragraph.replaceAll("\\}\\}","");
		paragraph = paragraph.replaceAll("\\|"," ");
		paragraph = paragraph.replaceAll("'''","");
		paragraph = paragraph.replaceAll("(^.*?\\..*?\\..*?\\.)(.*)", "$1");

		return paragraph;
	}

}


