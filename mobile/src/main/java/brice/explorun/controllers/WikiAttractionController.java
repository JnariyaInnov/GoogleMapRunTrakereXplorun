package brice.explorun.controllers;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import brice.explorun.fragments.RouteInfoFragment;
import brice.explorun.models.CustomRequestQueue;
import brice.explorun.models.Place;
import brice.explorun.utilities.Utility;

public class WikiAttractionController
{
	private final String WIKI_API_BASE_URL = "https://" + Locale.getDefault().getLanguage() + ".wikipedia.org/w/api.php?";
	private final String TAG = "WikiController";

	private int countPlace = 0;
	private ArrayList<Place> places;

	private RouteInfoFragment observer;

	public void setPlaces(ArrayList<Place> places)
	{
		this.places = places;
	}

	public WikiAttractionController(RouteInfoFragment observer)
	{
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

	public void getWikiAttractions()
	{
		if (Utility.isOnline(this.observer.getActivity()))
		{
			for (Place place: this.places)
			{
				this.getWikiAttractionByString(place.getName(), place);
			}
		}
	}

	private void getWikiAttractionByString(String attractionName, final Place place)
	{
		if (Utility.isOnline(this.observer.getActivity()))
		{
			String url = getWikiApiUrl(attractionName);
			final WikiAttractionController _this = this;
			JsonObjectRequest request = new JsonObjectRequest(
					Request.Method.GET,
					url,
					null,
					new Response.Listener<JSONObject>()
					{
						@Override
						public void onResponse(JSONObject response)
						{
							_this.onResponse(response, place);
						}
					},
					new Response.ErrorListener()
					{
						@Override
						public void onErrorResponse(VolleyError error)
						{
							_this.onErrorResponse(error);
						}
					}
			);

			CustomRequestQueue.getInstance(this.observer.getActivity()).getRequestQueue().add(request);
		}
	}

	private void onResponse(JSONObject response, Place place)
	{
		try
		{
			String status = response.getString("query");

			Pattern p = Pattern.compile("\"pages\":\\{\"-1\"");
			Matcher m = p.matcher(status);
			if (m.find())
			{
				Log.d(TAG,"Page non trouvée");
				afterResponse();
			}
			else
			{
				p = Pattern.compile("\"\\*\":\"#REDIRECT");
				m = p.matcher(status);
				if (m.find())
				{
					status = status.replaceAll(".*\"\\*\":\"#REDIRECT(.*)","$1");
					status = status.replaceAll("\\[","");
					status = status.replaceAll("\\]","");
					status = status.replaceAll("\\}","");
					status = status.replaceAll("\"","");
					getWikiAttractionByString(status, place);
				}
				else
				{
					p = Pattern.compile("\"\\*\":\"#REDIRECTION");
					m = p.matcher(status);
					if (m.find())
					{
						status = status.replaceAll(".*\"\\*\":\"#REDIRECTION(.*)", "$1");
						status = status.replaceAll("\\[", "");
						status = status.replaceAll("\\]", "");
						status = status.replaceAll("\\}", "");
						status = status.replaceAll("\"", "");
						getWikiAttractionByString(status, place);
					}
					else
					{
						String description = textCleaner(status);
						Log.d(TAG, description);
						place.setDescription(description);
						afterResponse();
					}
				}
			}
		}
		catch (JSONException e)
		{
			afterResponse();
			Log.e(TAG, e.getMessage());
		}
	}

	private void afterResponse()
	{
		this.countPlace++;
		if (this.countPlace == this.places.size() && this.observer != null)
		{
			this.observer.onWikiResponse();
			this.countPlace = 0;
		}
	}

	private void onErrorResponse(VolleyError error)
	{
		Log.e(TAG, error.getMessage());
		afterResponse();
	}

	private String textCleaner(String status)
	{
		String paragraph;

		Pattern p = Pattern.compile("'''");
		Matcher m = p.matcher(status);
		if (m.find())
		{
			paragraph = status.substring(status.indexOf("'''"), status.indexOf("=="));
		}
		else
		{
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
		paragraph = paragraph.replaceAll("&nbsp;", " ");
		paragraph = paragraph.replaceAll("\\\\n","");
		paragraph = paragraph.replaceAll("<ref.*?/>","");
		paragraph = paragraph.replaceAll("<ref.*?ref>","");
		paragraph = paragraph.replaceAll("\\{\\{Prononciation.*?\\}\\}","");
		paragraph = paragraph.replaceAll("\\[\\[.[^,\\]\\]]*?\\|(.*?)\\]\\]", "$1");
		paragraph = paragraph.replaceAll("\\{\\{unité\\|(.[^,\\}\\}]*)\\|(.[^,\\}\\}]*)\\}\\}","$1 $2");
		paragraph = paragraph.replaceAll("\\{\\{note.*note\\}\\}","");
		paragraph = paragraph.replaceAll("formatnum:", "");
		paragraph = paragraph.replaceAll("<!--.*?-->", "");
		paragraph = paragraph.replaceAll("\\{\\{","");
		paragraph = paragraph.replaceAll("\\[\\[","");
		paragraph = paragraph.replaceAll("]]","");
		paragraph = paragraph.replaceAll("\\}\\}","");
		paragraph = paragraph.replaceAll("\\|"," ");
		paragraph = paragraph.replaceAll("'''","");
		paragraph = paragraph.replaceAll("\\(IPA.*?\\) ", "");
		paragraph = paragraph.replaceAll("\\(Lang-.*?\\) ", "");
		paragraph = paragraph.replaceAll("(^.*?\\..*?\\..*?\\.)(.*)", "$1");

		return paragraph;
	}
}
