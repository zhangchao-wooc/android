package com.cenobots.myapplication.utils;

import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConversionUtil {
    public static JSONObject convertBundleToJson(Bundle bundle) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value instanceof Bundle) {
                // 递归处理嵌套的Bundle
                jsonObject.put(key, convertBundleToJson((Bundle) value));
            } else if (value instanceof String) {
                jsonObject.put(key, (String) value);
            } else if (value instanceof Integer || value instanceof Boolean || value instanceof Long ||
                    value instanceof Double || value instanceof Float || value instanceof Byte) {
                jsonObject.put(key, value);
            } else if (value instanceof String[]) {
                JSONArray array = new JSONArray();
                for (String s : (String[]) value) {
                    array.put(s);
                }
                jsonObject.put(key, array);
            } else if (value instanceof int[]) {
                JSONArray array = new JSONArray();
                for (int s : (int[]) value) {
                    array.put(s);
                }
                jsonObject.put(key, array);
            } else if (value instanceof boolean[]) {
                JSONArray array = new JSONArray();
                for (boolean s : (boolean[]) value) {
                    array.put(s);
                }
                jsonObject.put(key, array);
            } else if (value == null) {
                jsonObject.put(key, JSONObject.NULL);
            } else {
                // 其他类型，可以添加更多类型的处理
                jsonObject.put(key, String.valueOf(value));
            }
        }
        return jsonObject;
    }
}
