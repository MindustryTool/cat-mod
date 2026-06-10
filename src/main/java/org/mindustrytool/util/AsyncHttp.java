package org.mindustrytool.util;

import arc.Core;
import arc.func.Cons;
import arc.util.Http;
import arc.util.Http.HttpMethod;
import arc.util.serialization.Jval;
import java.util.HashMap;
import java.util.Map;

/**
 * A clean, fluent utility for making asynchronous HTTP requests that guarantees
 * all callback executions are dispatched back to the main application thread.
 * <p>
 * This ensures full thread-safety when updating UI components and reactive signals
 * without mixing thread-dispatching code in the application services.
 */
public final class AsyncHttp {

    private AsyncHttp() {
        // Prevent instantiation
    }

    /**
     * Creates a new fluent builder for a GET request.
     *
     * @param url the destination URL
     * @return a RequestBuilder instance
     */
    public static RequestBuilder get(String url) {
        return new RequestBuilder(HttpMethod.GET, url);
    }

    /**
     * Creates a new fluent builder for a POST request.
     *
     * @param url     the destination URL
     * @param content the body content of the POST request
     * @return a RequestBuilder instance
     */
    public static RequestBuilder post(String url, String content) {
        return new RequestBuilder(HttpMethod.POST, url).body(content);
    }

    /**
     * Creates a new fluent builder for a POST request with a JSON body.
     *
     * @param url  the destination URL
     * @param json the Jval JSON object payload
     * @return a RequestBuilder instance
     */
    public static RequestBuilder postJson(String url, Jval json) {
        return new RequestBuilder(HttpMethod.POST, url)
                .header("Content-Type", "application/json")
                .body(json.toString());
    }

    /**
     * Fluent request builder that manages headers, timeout, and safely pipes callbacks to the main thread.
     */
    public static class RequestBuilder {
        private final HttpMethod method;
        private final String url;
        private final Map<String, String> headers = new HashMap<>();
        private String body;
        private int timeout = 10000; // default 10 seconds

        RequestBuilder(HttpMethod method, String url) {
            this.method = method;
            this.url = url;
        }

        /**
         * Sets a custom header key-value pair.
         *
         * @param name  the header key name
         * @param value the header value
         * @return this RequestBuilder for chaining
         */
        public RequestBuilder header(String name, String value) {
            if (value != null) {
                headers.put(name, value);
            }
            return this;
        }

        /**
         * Sets the Authorization Bearer token header if the token is valid.
         *
         * @param token the bearer token
         * @return this RequestBuilder for chaining
         */
        public RequestBuilder bearerAuth(String token) {
            if (token != null && !token.isEmpty()) {
                headers.put("Authorization", "Bearer " + token);
            }
            return this;
        }

        /**
         * Sets a custom request body (primarily for POST requests).
         *
         * @param body the raw payload body
         * @return this RequestBuilder for chaining
         */
        public RequestBuilder body(String body) {
            this.body = body;
            return this;
        }

        /**
         * Configures the request timeout duration in milliseconds.
         *
         * @param timeoutMs timeout in milliseconds
         * @return this RequestBuilder for chaining
         */
        public RequestBuilder timeout(int timeoutMs) {
            this.timeout = timeoutMs;
            return this;
        }

        /**
         * Submits the request and calls onSuccess with the raw string response on the main thread.
         *
         * @param onSuccess Callback executed on success on the main thread
         * @param onError   Callback executed on error on the main thread
         */
        public void submit(Cons<String> onSuccess, Cons<Throwable> onError) {
            buildRequest()
                .error(err -> Core.app.post(() -> onError.get(err)))
                .submit(res -> {
                    String result = res.getResultAsString();
                    Core.app.post(() -> onSuccess.get(result));
                });
        }

        /**
         * Submits the request and calls onSuccess with the raw response bytes on the main thread.
         * Useful for downloading files, textures, etc.
         *
         * @param onSuccess Callback executed on success on the main thread
         * @param onError   Callback executed on error on the main thread
         */
        public void submitBytes(Cons<byte[]> onSuccess, Cons<Throwable> onError) {
            buildRequest()
                .error(err -> Core.app.post(() -> onError.get(err)))
                .submit(res -> {
                    byte[] bytes = res.getResult();
                    Core.app.post(() -> onSuccess.get(bytes));
                });
        }

        private Http.HttpRequest buildRequest() {
            Http.HttpRequest req = Http.request(method, url);
            req.timeout(timeout);
            if (body != null) {
                req.content(body);
            }
            headers.forEach(req::header);
            return req;
        }
    }
}
