/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://github.com/google/apis-client-generator/
 * (build: 2018-04-06 17:52:19 UTC)
 * on 2018-05-07 at 00:11:22 UTC 
 * Modify at your own risk.
 */

package com.appspot.hackwinds.hackwinds;

/**
 * Service definition for Hackwinds (v1).
 *
 * <p>
 * This is an API
 * </p>
 *
 * <p>
 * For more information about this service, see the
 * <a href="" target="_blank">API Documentation</a>
 * </p>
 *
 * <p>
 * This service uses {@link HackwindsRequestInitializer} to initialize global parameters via its
 * {@link Builder}.
 * </p>
 *
 * @since 1.3
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public class Hackwinds extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient {

  // Note: Leave this static initializer at the top of the file.
  static {
    com.google.api.client.util.Preconditions.checkState(
        com.google.api.client.googleapis.GoogleUtils.MAJOR_VERSION == 1 &&
        com.google.api.client.googleapis.GoogleUtils.MINOR_VERSION >= 15,
        "You are currently running with version %s of google-api-client. " +
        "You need at least version 1.15 of google-api-client to run version " +
        "1.23.0 of the hackwinds library.", com.google.api.client.googleapis.GoogleUtils.VERSION);
  }

  /**
   * The default encoded root URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_ROOT_URL = "https://hackwinds.appspot.com/_ah/api/";

  /**
   * The default encoded service path of the service. This is determined when the library is
   * generated and normally should not be changed.
   *
   * @since 1.7
   */
  public static final String DEFAULT_SERVICE_PATH = "hackwinds/v1/";

  /**
   * The default encoded batch path of the service. This is determined when the library is
   * generated and normally should not be changed.
   *
   * @since 1.23
   */
  public static final String DEFAULT_BATCH_PATH = "batch";

  /**
   * The default encoded base URL of the service. This is determined when the library is generated
   * and normally should not be changed.
   */
  public static final String DEFAULT_BASE_URL = DEFAULT_ROOT_URL + DEFAULT_SERVICE_PATH;

  /**
   * Constructor.
   *
   * <p>
   * Use {@link Builder} if you need to specify any of the optional parameters.
   * </p>
   *
   * @param transport HTTP transport, which should normally be:
   *        <ul>
   *        <li>Google App Engine:
   *        {@code com.google.api.client.extensions.appengine.http.UrlFetchTransport}</li>
   *        <li>Android: {@code newCompatibleTransport} from
   *        {@code com.google.api.client.extensions.android.http.AndroidHttp}</li>
   *        <li>Java: {@link com.google.api.client.googleapis.javanet.GoogleNetHttpTransport#newTrustedTransport()}
   *        </li>
   *        </ul>
   * @param jsonFactory JSON factory, which may be:
   *        <ul>
   *        <li>Jackson: {@code com.google.api.client.json.jackson2.JacksonFactory}</li>
   *        <li>Google GSON: {@code com.google.api.client.json.gson.GsonFactory}</li>
   *        <li>Android Honeycomb or higher:
   *        {@code com.google.api.client.extensions.android.json.AndroidJsonFactory}</li>
   *        </ul>
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   * @since 1.7
   */
  public Hackwinds(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
      com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
    this(new Builder(transport, jsonFactory, httpRequestInitializer));
  }

  /**
   * @param builder builder
   */
  Hackwinds(Builder builder) {
    super(builder);
  }

  @Override
  protected void initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest<?> httpClientRequest) throws java.io.IOException {
    super.initialize(httpClientRequest);
  }

  /**
   * An accessor for creating requests from the Camera collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Hackwinds hackwinds = new Hackwinds(...);}
   *   {@code Hackwinds.Camera.List request = hackwinds.camera().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Camera camera() {
    return new Camera();
  }

  /**
   * The "camera" collection of methods.
   */
  public class Camera {

    /**
     * Create a request for the method "camera.cameras".
     *
     * This request holds the parameters needed by the hackwinds server.  After setting any optional
     * parameters, call the {@link Cameras#execute()} method to invoke the remote operation.
     *
     * @param premium
     * @return the request
     */
    public Cameras cameras(java.lang.Boolean premium) throws java.io.IOException {
      Cameras result = new Cameras(premium);
      initialize(result);
      return result;
    }

    public class Cameras extends HackwindsRequest<com.appspot.hackwinds.hackwinds.model.MessagesCameraCameraLocationsMessage> {

      private static final String REST_PATH = "camera/cameras";

      /**
       * Create a request for the method "camera.cameras".
       *
       * This request holds the parameters needed by the the hackwinds server.  After setting any
       * optional parameters, call the {@link Cameras#execute()} method to invoke the remote operation.
       * <p> {@link
       * Cameras#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)} must
       * be called to initialize this instance immediately after invoking the constructor. </p>
       *
       * @param premium
       * @since 1.13
       */
      protected Cameras(java.lang.Boolean premium) {
        super(Hackwinds.this, "GET", REST_PATH, null, com.appspot.hackwinds.hackwinds.model.MessagesCameraCameraLocationsMessage.class);
        this.premium = com.google.api.client.util.Preconditions.checkNotNull(premium, "Required parameter premium must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public Cameras setAlt(java.lang.String alt) {
        return (Cameras) super.setAlt(alt);
      }

      @Override
      public Cameras setFields(java.lang.String fields) {
        return (Cameras) super.setFields(fields);
      }

      @Override
      public Cameras setKey(java.lang.String key) {
        return (Cameras) super.setKey(key);
      }

      @Override
      public Cameras setOauthToken(java.lang.String oauthToken) {
        return (Cameras) super.setOauthToken(oauthToken);
      }

      @Override
      public Cameras setPrettyPrint(java.lang.Boolean prettyPrint) {
        return (Cameras) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Cameras setQuotaUser(java.lang.String quotaUser) {
        return (Cameras) super.setQuotaUser(quotaUser);
      }

      @Override
      public Cameras setUserIp(java.lang.String userIp) {
        return (Cameras) super.setUserIp(userIp);
      }

      @com.google.api.client.util.Key
      private java.lang.Boolean premium;

      /**

       */
      public java.lang.Boolean getPremium() {
        return premium;
      }

      public Cameras setPremium(java.lang.Boolean premium) {
        this.premium = premium;
        return this;
      }

      @Override
      public Cameras set(String parameterName, Object value) {
        return (Cameras) super.set(parameterName, value);
      }
    }

  }

  /**
   * An accessor for creating requests from the Sun collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Hackwinds hackwinds = new Hackwinds(...);}
   *   {@code Hackwinds.Sun.List request = hackwinds.sun().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Sun sun() {
    return new Sun();
  }

  /**
   * The "sun" collection of methods.
   */
  public class Sun {

    /**
     * Create a request for the method "sun.week_forecast".
     *
     * This request holds the parameters needed by the hackwinds server.  After setting any optional
     * parameters, call the {@link WeekForecast#execute()} method to invoke the remote operation.
     *
     * @return the request
     */
    public WeekForecast weekForecast() throws java.io.IOException {
      WeekForecast result = new WeekForecast();
      initialize(result);
      return result;
    }

    public class WeekForecast extends HackwindsRequest<com.appspot.hackwinds.hackwinds.model.MessagesSunSunForecastMessage> {

      private static final String REST_PATH = "sun/forecast/week";

      /**
       * Create a request for the method "sun.week_forecast".
       *
       * This request holds the parameters needed by the the hackwinds server.  After setting any
       * optional parameters, call the {@link WeekForecast#execute()} method to invoke the remote
       * operation. <p> {@link
       * WeekForecast#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
       * must be called to initialize this instance immediately after invoking the constructor. </p>
       *
       * @since 1.13
       */
      protected WeekForecast() {
        super(Hackwinds.this, "GET", REST_PATH, null, com.appspot.hackwinds.hackwinds.model.MessagesSunSunForecastMessage.class);
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public WeekForecast setAlt(java.lang.String alt) {
        return (WeekForecast) super.setAlt(alt);
      }

      @Override
      public WeekForecast setFields(java.lang.String fields) {
        return (WeekForecast) super.setFields(fields);
      }

      @Override
      public WeekForecast setKey(java.lang.String key) {
        return (WeekForecast) super.setKey(key);
      }

      @Override
      public WeekForecast setOauthToken(java.lang.String oauthToken) {
        return (WeekForecast) super.setOauthToken(oauthToken);
      }

      @Override
      public WeekForecast setPrettyPrint(java.lang.Boolean prettyPrint) {
        return (WeekForecast) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public WeekForecast setQuotaUser(java.lang.String quotaUser) {
        return (WeekForecast) super.setQuotaUser(quotaUser);
      }

      @Override
      public WeekForecast setUserIp(java.lang.String userIp) {
        return (WeekForecast) super.setUserIp(userIp);
      }

      @Override
      public WeekForecast set(String parameterName, Object value) {
        return (WeekForecast) super.set(parameterName, value);
      }
    }

  }

  /**
   * An accessor for creating requests from the Surf collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Hackwinds hackwinds = new Hackwinds(...);}
   *   {@code Hackwinds.Surf.List request = hackwinds.surf().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Surf surf() {
    return new Surf();
  }

  /**
   * The "surf" collection of methods.
   */
  public class Surf {

    /**
     * Create a request for the method "surf.forecast".
     *
     * This request holds the parameters needed by the hackwinds server.  After setting any optional
     * parameters, call the {@link Forecast#execute()} method to invoke the remote operation.
     *
     * @return the request
     */
    public Forecast forecast() throws java.io.IOException {
      Forecast result = new Forecast();
      initialize(result);
      return result;
    }

    public class Forecast extends HackwindsRequest<com.appspot.hackwinds.hackwinds.model.MessagesDataSurfForecastMessage> {

      private static final String REST_PATH = "surf/forecast";

      /**
       * Create a request for the method "surf.forecast".
       *
       * This request holds the parameters needed by the the hackwinds server.  After setting any
       * optional parameters, call the {@link Forecast#execute()} method to invoke the remote operation.
       * <p> {@link
       * Forecast#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
       * must be called to initialize this instance immediately after invoking the constructor. </p>
       *
       * @since 1.13
       */
      protected Forecast() {
        super(Hackwinds.this, "GET", REST_PATH, null, com.appspot.hackwinds.hackwinds.model.MessagesDataSurfForecastMessage.class);
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public Forecast setAlt(java.lang.String alt) {
        return (Forecast) super.setAlt(alt);
      }

      @Override
      public Forecast setFields(java.lang.String fields) {
        return (Forecast) super.setFields(fields);
      }

      @Override
      public Forecast setKey(java.lang.String key) {
        return (Forecast) super.setKey(key);
      }

      @Override
      public Forecast setOauthToken(java.lang.String oauthToken) {
        return (Forecast) super.setOauthToken(oauthToken);
      }

      @Override
      public Forecast setPrettyPrint(java.lang.Boolean prettyPrint) {
        return (Forecast) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public Forecast setQuotaUser(java.lang.String quotaUser) {
        return (Forecast) super.setQuotaUser(quotaUser);
      }

      @Override
      public Forecast setUserIp(java.lang.String userIp) {
        return (Forecast) super.setUserIp(userIp);
      }

      @Override
      public Forecast set(String parameterName, Object value) {
        return (Forecast) super.set(parameterName, value);
      }
    }

  }

  /**
   * An accessor for creating requests from the Tide collection.
   *
   * <p>The typical use is:</p>
   * <pre>
   *   {@code Hackwinds hackwinds = new Hackwinds(...);}
   *   {@code Hackwinds.Tide.List request = hackwinds.tide().list(parameters ...)}
   * </pre>
   *
   * @return the resource collection
   */
  public Tide tide() {
    return new Tide();
  }

  /**
   * The "tide" collection of methods.
   */
  public class Tide {

    /**
     * Create a request for the method "tide.week_forecast".
     *
     * This request holds the parameters needed by the hackwinds server.  After setting any optional
     * parameters, call the {@link WeekForecast#execute()} method to invoke the remote operation.
     *
     * @param waterLevel
     * @return the request
     */
    public WeekForecast weekForecast(java.lang.Boolean waterLevel) throws java.io.IOException {
      WeekForecast result = new WeekForecast(waterLevel);
      initialize(result);
      return result;
    }

    public class WeekForecast extends HackwindsRequest<com.appspot.hackwinds.hackwinds.model.MessagesTideTideForecastMessage> {

      private static final String REST_PATH = "tide/forecast/week";

      /**
       * Create a request for the method "tide.week_forecast".
       *
       * This request holds the parameters needed by the the hackwinds server.  After setting any
       * optional parameters, call the {@link WeekForecast#execute()} method to invoke the remote
       * operation. <p> {@link
       * WeekForecast#initialize(com.google.api.client.googleapis.services.AbstractGoogleClientRequest)}
       * must be called to initialize this instance immediately after invoking the constructor. </p>
       *
       * @param waterLevel
       * @since 1.13
       */
      protected WeekForecast(java.lang.Boolean waterLevel) {
        super(Hackwinds.this, "GET", REST_PATH, null, com.appspot.hackwinds.hackwinds.model.MessagesTideTideForecastMessage.class);
        this.waterLevel = com.google.api.client.util.Preconditions.checkNotNull(waterLevel, "Required parameter waterLevel must be specified.");
      }

      @Override
      public com.google.api.client.http.HttpResponse executeUsingHead() throws java.io.IOException {
        return super.executeUsingHead();
      }

      @Override
      public com.google.api.client.http.HttpRequest buildHttpRequestUsingHead() throws java.io.IOException {
        return super.buildHttpRequestUsingHead();
      }

      @Override
      public WeekForecast setAlt(java.lang.String alt) {
        return (WeekForecast) super.setAlt(alt);
      }

      @Override
      public WeekForecast setFields(java.lang.String fields) {
        return (WeekForecast) super.setFields(fields);
      }

      @Override
      public WeekForecast setKey(java.lang.String key) {
        return (WeekForecast) super.setKey(key);
      }

      @Override
      public WeekForecast setOauthToken(java.lang.String oauthToken) {
        return (WeekForecast) super.setOauthToken(oauthToken);
      }

      @Override
      public WeekForecast setPrettyPrint(java.lang.Boolean prettyPrint) {
        return (WeekForecast) super.setPrettyPrint(prettyPrint);
      }

      @Override
      public WeekForecast setQuotaUser(java.lang.String quotaUser) {
        return (WeekForecast) super.setQuotaUser(quotaUser);
      }

      @Override
      public WeekForecast setUserIp(java.lang.String userIp) {
        return (WeekForecast) super.setUserIp(userIp);
      }

      @com.google.api.client.util.Key("water_level")
      private java.lang.Boolean waterLevel;

      /**

       */
      public java.lang.Boolean getWaterLevel() {
        return waterLevel;
      }

      public WeekForecast setWaterLevel(java.lang.Boolean waterLevel) {
        this.waterLevel = waterLevel;
        return this;
      }

      @Override
      public WeekForecast set(String parameterName, Object value) {
        return (WeekForecast) super.set(parameterName, value);
      }
    }

  }

  /**
   * Builder for {@link Hackwinds}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.3.0
   */
  public static final class Builder extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient.Builder {

    /**
     * Returns an instance of a new builder.
     *
     * @param transport HTTP transport, which should normally be:
     *        <ul>
     *        <li>Google App Engine:
     *        {@code com.google.api.client.extensions.appengine.http.UrlFetchTransport}</li>
     *        <li>Android: {@code newCompatibleTransport} from
     *        {@code com.google.api.client.extensions.android.http.AndroidHttp}</li>
     *        <li>Java: {@link com.google.api.client.googleapis.javanet.GoogleNetHttpTransport#newTrustedTransport()}
     *        </li>
     *        </ul>
     * @param jsonFactory JSON factory, which may be:
     *        <ul>
     *        <li>Jackson: {@code com.google.api.client.json.jackson2.JacksonFactory}</li>
     *        <li>Google GSON: {@code com.google.api.client.json.gson.GsonFactory}</li>
     *        <li>Android Honeycomb or higher:
     *        {@code com.google.api.client.extensions.android.json.AndroidJsonFactory}</li>
     *        </ul>
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     * @since 1.7
     */
    public Builder(com.google.api.client.http.HttpTransport transport, com.google.api.client.json.JsonFactory jsonFactory,
        com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      super(
          transport,
          jsonFactory,
          DEFAULT_ROOT_URL,
          DEFAULT_SERVICE_PATH,
          httpRequestInitializer,
          false);
      setBatchPath(DEFAULT_BATCH_PATH);
    }

    /** Builds a new instance of {@link Hackwinds}. */
    @Override
    public Hackwinds build() {
      return new Hackwinds(this);
    }

    @Override
    public Builder setRootUrl(String rootUrl) {
      return (Builder) super.setRootUrl(rootUrl);
    }

    @Override
    public Builder setServicePath(String servicePath) {
      return (Builder) super.setServicePath(servicePath);
    }

    @Override
    public Builder setBatchPath(String batchPath) {
      return (Builder) super.setBatchPath(batchPath);
    }

    @Override
    public Builder setHttpRequestInitializer(com.google.api.client.http.HttpRequestInitializer httpRequestInitializer) {
      return (Builder) super.setHttpRequestInitializer(httpRequestInitializer);
    }

    @Override
    public Builder setApplicationName(String applicationName) {
      return (Builder) super.setApplicationName(applicationName);
    }

    @Override
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      return (Builder) super.setSuppressPatternChecks(suppressPatternChecks);
    }

    @Override
    public Builder setSuppressRequiredParameterChecks(boolean suppressRequiredParameterChecks) {
      return (Builder) super.setSuppressRequiredParameterChecks(suppressRequiredParameterChecks);
    }

    @Override
    public Builder setSuppressAllChecks(boolean suppressAllChecks) {
      return (Builder) super.setSuppressAllChecks(suppressAllChecks);
    }

    /**
     * Set the {@link HackwindsRequestInitializer}.
     *
     * @since 1.12
     */
    public Builder setHackwindsRequestInitializer(
        HackwindsRequestInitializer hackwindsRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(hackwindsRequestInitializer);
    }

    @Override
    public Builder setGoogleClientRequestInitializer(
        com.google.api.client.googleapis.services.GoogleClientRequestInitializer googleClientRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(googleClientRequestInitializer);
    }
  }
}
