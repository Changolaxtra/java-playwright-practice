package com.practice.api.clients;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.practice.api.BaseApiClient;

public class ProductsApiClient extends BaseApiClient {

    private static final String PRODUCTS_LIST_PATH = "productsList";

    public ProductsApiClient(APIRequestContext requestContext) {
        super(requestContext);
    }

    public APIResponse optionsProductsList() {
        return options(PRODUCTS_LIST_PATH);
    }
}
