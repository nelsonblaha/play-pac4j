/*
  Copyright 2012 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.play;

import java.util.*;

import org.pac4j.core.context.BaseResponseContext;

import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.play.store.PlayCacheStore;
import play.api.mvc.RequestHeader;
import play.core.j.JavaHelpers$;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Http.Session;
import play.mvc.Http.Context;

/**
 * <p>This class is the web context for Play (used both for Java and Scala).</p>
 * <p>"Session objects" are managed by the defined {@link SessionStore}.</p>
 * <p>"Request attributes" are saved/restored to/from the context.</p>
 * 
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class PlayWebContext extends BaseResponseContext {

    protected final Context context;

    protected final Request request;

    protected final Response response;

    protected final Session session;

    protected final SessionStore sessionStore;

    public PlayWebContext(final Context context, final SessionStore sessionStore) {
        this.context = context;
        this.request = context.request();
        this.response = context.response();
        this.session = context.session();
        if (sessionStore == null) {
            this.sessionStore = new PlayCacheStore();
        } else {
            this.sessionStore = sessionStore;
        }
    }

    public PlayWebContext(final RequestHeader requestHeader, final SessionStore sessionStore) {
        this(JavaHelpers$.MODULE$.createJavaContext(requestHeader), sessionStore);
    }

    /**
     * Get the Java session.
     *
     * @return the Java session
     */
    public Session getJavaSession() {
        return session;
    }

    /**
     * Get the Java request.
     *
     * @return the Java request
     */
    public Request getJavaRequest() {
        return request;
    }

    /**
     * Get the Java context.
     *
     * @return the Java context.
     */
    public Context getJavaContext() {
        return this.context;
    }

    /**
     * Return the session store.
     *
     * @return the session store
     */
    public SessionStore getSessionStore() { return this.sessionStore; }

    @Override
    public String getRequestHeader(final String name) {
        return request.getHeader(name);
    }

    @Override
    public String getRequestMethod() {
        return request.method();
    }

    @Override
    public String getRequestParameter(final String name) {
        final Map<String, String[]> parameters = getRequestParameters();
        final String[] values = parameters.get(name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        final Http.RequestBody body = request.body();
        final Map<String, String[]> formParameters;
        if (body != null) {
            formParameters = body.asFormUrlEncoded();
        } else {
            formParameters = new HashMap<String, String[]>();
        }
        final Map<String, String[]> urlParameters = request.queryString();
        final Map<String, String[]> parameters = new HashMap<String, String[]>();
        if (formParameters != null) {
            parameters.putAll(formParameters);
        }
        if (urlParameters != null) {
            parameters.putAll(urlParameters);
        }
        return parameters;
    }

    @Override
    public Object getSessionIdentifier() {
        return sessionStore.getOrCreateSessionId(this);
    }

    @Override
    public Object getSessionAttribute(final String key) {
        return sessionStore.get(this, key);
    }

    @Override
    public void setSessionAttribute(final String key, final Object value) {
        sessionStore.set(this, key, value);
    }

    @Override
    public void setResponseHeader(final String name, final String value) {
        response.setHeader(name, value);
    }

    @Override
    public String getServerName() {
        String[] split = request.host().split(":");
        return split[0];
    }

    @Override
    public int getServerPort() {
        String[] split = request.host().split(":");
        String portStr = (split.length > 1) ? split[1] : "80";
        return Integer.valueOf(portStr);
    }

    @Override
    public String getScheme() {
        if (request.secure()) {
            return "https";
        } else {
            return "http";
        }
    }

    @Override
    public boolean isSecure() { return request.secure(); }

    @Override
    public String getFullRequestURL() {
        return getScheme() + "://" + request.host() + request.uri();
    }

    @Override
    public String getRemoteAddr() {
        return request.remoteAddress();
    }

    @Override
    public Object getRequestAttribute(String name) {
        return context.args.get(name);
    }

    @Override
    public void setRequestAttribute(String name, Object value) {
        context.args.put(name, value);
    }

    @Override
    public Collection<Cookie> getRequestCookies() {
        final List<Cookie> cookies = new ArrayList<>();
        final Http.Cookies httpCookies = request.cookies();
        httpCookies.forEach(httpCookie -> {
            final Cookie cookie = new Cookie(httpCookie.name(), httpCookie.value());
            cookie.setDomain(httpCookie.domain());
            cookie.setHttpOnly(httpCookie.httpOnly());
            cookie.setMaxAge(httpCookie.maxAge());
            cookie.setPath(httpCookie.path());
            cookie.setSecure(httpCookie.secure());
            cookies.add(cookie);
        });
        return cookies;
    }

    @Override
    public String getPath() {
        return request.path();
    }
}
