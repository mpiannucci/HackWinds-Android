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
 * Model definition for MessagesSwellSwellMessage.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the hackwinds. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MessagesSwellSwellMessage extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("compass_direction")
  private java.lang.String compassDirection;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Double direction;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Double period;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private MessagesUnitsUnitLabelMessage unit;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("wave_height")
  private java.lang.Double waveHeight;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getCompassDirection() {
    return compassDirection;
  }

  /**
   * @param compassDirection compassDirection or {@code null} for none
   */
  public MessagesSwellSwellMessage setCompassDirection(java.lang.String compassDirection) {
    this.compassDirection = compassDirection;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getDirection() {
    return direction;
  }

  /**
   * @param direction direction or {@code null} for none
   */
  public MessagesSwellSwellMessage setDirection(java.lang.Double direction) {
    this.direction = direction;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getPeriod() {
    return period;
  }

  /**
   * @param period period or {@code null} for none
   */
  public MessagesSwellSwellMessage setPeriod(java.lang.Double period) {
    this.period = period;
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
  public MessagesSwellSwellMessage setUnit(MessagesUnitsUnitLabelMessage unit) {
    this.unit = unit;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getWaveHeight() {
    return waveHeight;
  }

  /**
   * @param waveHeight waveHeight or {@code null} for none
   */
  public MessagesSwellSwellMessage setWaveHeight(java.lang.Double waveHeight) {
    this.waveHeight = waveHeight;
    return this;
  }

  @Override
  public MessagesSwellSwellMessage set(String fieldName, Object value) {
    return (MessagesSwellSwellMessage) super.set(fieldName, value);
  }

  @Override
  public MessagesSwellSwellMessage clone() {
    return (MessagesSwellSwellMessage) super.clone();
  }

}
