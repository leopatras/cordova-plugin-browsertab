/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cordova.plugin.browsertab;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import android.util.Log;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

/**
 * Cordova plugin which provides the ability to launch a URL in an
 * in-app browser tab. On Android, this means using the custom tabs support
 * library, if a supporting browser (e.g. Chrome) is available on the device.
 */
public class BrowserTab extends CordovaPlugin {

  public static final int RC_OPEN_URL = 101;

  private static final String LOG_TAG = "BrowserTab";

  private Color colorParser = new Color();

  /**
   * The service we expect to find on a web browser that indicates it supports custom tabs.
   */
  private static final String ACTION_CUSTOM_TABS_CONNECTION =
          "android.support.customtabs.action.CustomTabsService";

  private boolean mFindCalled = false;
  private String mCustomTabsBrowser;
  private String mLog = "";

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    final Context context = this.cordova.getContext();
    Log.d(LOG_TAG, "executing " + action);
    if ("isAvailable".equals(action)) {
      Log.d(LOG_TAG, "caught isAvailable " + action);
      isAvailable(callbackContext);
    } else if ("openUrl".equals(action)) {
      openUrl(args, callbackContext);
    } else if ("close".equals(action)) {
      Intent newIntent = new Intent(cordova.getActivity(), cordova.getActivity().getClass());
      newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      cordova.getActivity().startActivity(newIntent);
      //cordova.getActivity().startActivity(new Intent(cordova.getActivity(), cordova.getActivity().getClass()));
      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
    } else if ("customTabBrowsers".equals(action)) {
      JSONObject result = new JSONObject();
      result.put("packages", getPackagesSupportingCustomTabs(context));
      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
      callbackContext.sendPluginResult(pluginResult);
    } else {
      Log.d(LOG_TAG, "no handler for action:" + action);
      return false;
    }

    return true;
  }

  private void appendLog(String msg) {
    Log.d(LOG_TAG,msg);
    mLog = mLog.concat(msg);
    mLog = mLog.concat("\n");
  }

  private void isAvailable(CallbackContext callbackContext) throws JSONException {
    Log.d(LOG_TAG, "before findCustomTabBrowser");
    String browserPackage = findCustomTabBrowser();
    Log.d(LOG_TAG, "browser package: " + browserPackage);
    PluginResult pluginResult;
    if (browserPackage==null) {
      String s=String.format("no browser,log:%s",mLog);
      pluginResult = new PluginResult( PluginResult.Status.ERROR, s);
    } else {
      pluginResult = new PluginResult( PluginResult.Status.OK, browserPackage);
    }
    callbackContext.sendPluginResult(pluginResult);
    mLog="";
  }

  private void openUrl(JSONArray args, CallbackContext callbackContext) {
    if (args.length() < 1) {
      Log.d(LOG_TAG, "openUrl: no url argument received");
      callbackContext.error("URL argument missing");
      return;
    }

    String urlStr;
    try {
      urlStr = args.getString(0);
    } catch (JSONException e) {
      Log.d(LOG_TAG, "openUrl: failed to parse url argument");
      callbackContext.error("URL argument is not a string");
      return;
    }
    /* leo: doesn't work in the Android emulator even if Chrome and FF are installed
    String customTabsBrowser = findCustomTabBrowser();
    if (customTabsBrowser == null) {
      Log.d(LOG_TAG, "openUrl: no in app browser tab available");
      callbackContext.error("no in app browser tab implementation available");
    }*/

    // Initialize Builder
    CustomTabsIntent.Builder customTabsIntentBuilder = new CustomTabsIntent.Builder();

    // Set tab color
    /* TODO: have an options argument to pass colors etc, Genero Cordova doesn't build the original resource
    String tabColor = cordova.getActivity().getString(cordova.getActivity().getResources().getIdentifier("CUSTOM_TAB_COLOR_RGB", "string", cordova.getActivity().getPackageName()));
    customTabsIntentBuilder.setToolbarColor(colorParser.parseColor(tabColor));
    */

    // Create Intent
    CustomTabsIntent customTabsIntent = customTabsIntentBuilder.build();

    // Load URL
    customTabsIntent.launchUrl(cordova.getActivity(), Uri.parse(urlStr));

    Log.d(LOG_TAG, "in app browser call dispatched");
    callbackContext.success();
  }

  private String findCustomTabBrowser() {
    appendLog(String.format("findCustomTabBrowser,mFindCalled:%b",mFindCalled));
    if (mFindCalled) {
      return mCustomTabsBrowser;
    }

    PackageManager pm = cordova.getActivity().getPackageManager();
    Intent webIntent = new Intent(
        Intent.ACTION_VIEW,
        Uri.parse("http://www.example.com"));
    List<ResolveInfo> resolvedActivityList =
        pm.queryIntentActivities(webIntent, PackageManager.GET_RESOLVED_FILTER);
    appendLog(String.format("resolvedActivityList size:%d",resolvedActivityList.size()));

    for (ResolveInfo info : resolvedActivityList) {
      String name=String.format("%s,%s",info.activityInfo.packageName,info.resolvePackageName);
        mCustomTabsBrowser = info.activityInfo.packageName;
      appendLog(String.format("info %s", name));
      if (!isFullBrowser(info)) {
        appendLog(String.format("no full browser: %s", name));
        continue;
      }

      if (hasCustomTabWarmupService(pm, info.activityInfo.packageName)) {
        mCustomTabsBrowser = info.activityInfo.packageName;
        appendLog(String.format("mCustomTabsBrowser=%s,name:%s",mCustomTabsBrowser,name));
        break;
      } else {
        appendLog(String.format("no custom warmup for browser: %s", name));
      }
    }

    mFindCalled = true;
    return mCustomTabsBrowser;
  }

  private List<String> getPackagesSupportingCustomTabs(Context context) {
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL);
        List<String> packagesSupportingCustomTabs = new ArrayList<>();
        appendLog(String.format("getPackagesSupportingCustomTabs  resolvedActivityList size:%d",resolvedActivityList.size()));
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }
        return packagesSupportingCustomTabs;
    }

  private boolean isFullBrowser(ResolveInfo resolveInfo) {
    // The filter must match ACTION_VIEW, CATEGORY_BROWSEABLE, and at least one scheme,
    if (!resolveInfo.filter.hasAction(Intent.ACTION_VIEW)
            || !resolveInfo.filter.hasCategory(Intent.CATEGORY_BROWSABLE)
            || resolveInfo.filter.schemesIterator() == null) {
        return false;
    }

    // The filter must not be restricted to any particular set of authorities
    if (resolveInfo.filter.authoritiesIterator() != null) {
        return false;
    }

    // The filter must support both HTTP and HTTPS.
    boolean supportsHttp = false;
    boolean supportsHttps = false;
    Iterator<String> schemeIter = resolveInfo.filter.schemesIterator();
    while (schemeIter.hasNext()) {
        String scheme = schemeIter.next();
        supportsHttp |= "http".equals(scheme);
        supportsHttps |= "https".equals(scheme);

        if (supportsHttp && supportsHttps) {
            return true;
        }
    }

    // at least one of HTTP or HTTPS is not supported
    return false;
  }

  private boolean hasCustomTabWarmupService(PackageManager pm, String packageName) {
    Intent serviceIntent = new Intent();
    serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
    serviceIntent.setPackage(packageName);
    return (pm.resolveService(serviceIntent, 0) != null);
  }
}
