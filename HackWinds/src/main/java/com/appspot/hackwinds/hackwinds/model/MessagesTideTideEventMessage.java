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

package com.appspot.hackwinds.hackwinds.model;

/**
 * Model definition for MessagesTideTideEventMessage.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the hackwinds. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MessagesTideTideEventMessage extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private com.google.api.client.util.DateTime date;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String datum;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("tidal_event")
  private java.lang.String tidalEvent;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private MessagesUnitsUnitLabelMessage unit;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("water_level")
  private java.lang.Double waterLevel;

  /**
   * @return value or {@code null} for none
   */
  public com.google.api.client.util.DateTime getDate() {
    return date;
  }

  /**
   * @param date date or {@code null} for none
   */
  public MessagesTideTideEventMessage setDate(com.google.api.client.util.DateTime date) {
    this.date = date;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getDatum() {
    return datum;
  }

  /**
   * @param datum datum or {@code null} for none
   */
  public MessagesTideTideEventMessage setDatum(java.lang.String datum) {
    this.datum = datum;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getTidalEvent() {
    return tidalEvent;
  }

  /**
   * @param tidalEvent tidalEvent or {@code null} for none
   */
  public MessagesTideTideEventMessage setTidalEvent(java.lang.String tidalEvent) {
    this.tidalEvent = tidalEvent;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public MessagesUnitsUnitLabelMessage getUnit() {
    return unit;
  }

  /**
   * @param unit unit or {@code null} for none
   */
  public MessagesTideTideEventMessage setUnit(MessagesUnitsUnitLabelMessage unit) {
    this.unit = unit;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getWaterLevel() {
    return waterLevel;
  }

  /**
   * @param waterLevel waterLevel or {@code null} for none
   */
  public MessagesTideTideEventMessage setWaterLevel(java.lang.Double waterLevel) {
    this.waterLevel = waterLevel;
    return this;
  }

  @Override
  public MessagesTideTideEventMessage set(String fieldName, Object value) {
    return (MessagesTideTideEventMessage) super.set(fieldName, value);
  }

  @Override
  public MessagesTideTideEventMessage clone() {
    return (MessagesTideTideEventMessage) super.clone();
  }

}
