package com.practice.api;

import com.google.gson.Gson;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;

/**
 * Thin wrapper around Playwright's {@link APIRequestContext}. Concrete
 * clients (one per resource/endpoint group) extend this and expose
 * intention-revealing methods instead of raw HTTP verbs.
 */
public abstract class BaseApiClient {

    protected final APIRequestContext requestContext;
    protected final Gson gson = new Gson();

    protected BaseApiClient(APIRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    protected APIResponse get(String url) {
        return requestContext.get(url);
    }

    protected APIResponse post(String url, Object body) {
        return requestContext.post(url, RequestOptions.create().setData(body));
    }

    protected APIResponse put(String url, Object body) {
        return requestContext.put(url, RequestOptions.create().setData(body));
    }

    protected APIResponse delete(String url) {
        return requestContext.delete(url);
    }

    protected APIResponse options(String url) {
        return requestContext.fetch(url, RequestOptions.create().setMethod("OPTIONS"));
    }

    protected <T> T parse(APIResponse response, Class<T> type) {
        return gson.fromJson(response.text(), type);
    }
}
