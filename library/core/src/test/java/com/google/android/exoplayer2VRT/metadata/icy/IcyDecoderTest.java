/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2VRT.metadata.icy;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.exoplayer2VRT.metadata.Metadata;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link IcyDecoder}. */
@RunWith(AndroidJUnit4.class)
public final class IcyDecoderTest {

  @Test
  public void decode() {
    IcyDecoder decoder = new IcyDecoder();
    Metadata metadata = decoder.decode("StreamTitle='test title';StreamURL='test_url';");

    assertThat(metadata.length()).isEqualTo(1);
    IcyInfo streamInfo = (IcyInfo) metadata.get(0);
    assertThat(streamInfo.title).isEqualTo("test title");
    assertThat(streamInfo.url).isEqualTo("test_url");
  }

  @Test
  public void decode_titleOnly() {
    IcyDecoder decoder = new IcyDecoder();
    Metadata metadata = decoder.decode("StreamTitle='test title';");

    assertThat(metadata.length()).isEqualTo(1);
    IcyInfo streamInfo = (IcyInfo) metadata.get(0);
    assertThat(streamInfo.title).isEqualTo("test title");
    assertThat(streamInfo.url).isNull();
  }

  @Test
  public void decode_emptyTitle() {
    IcyDecoder decoder = new IcyDecoder();
    Metadata metadata = decoder.decode("StreamTitle='';StreamURL='test_url';");

    assertThat(metadata.length()).isEqualTo(1);
    IcyInfo streamInfo = (IcyInfo) metadata.get(0);
    assertThat(streamInfo.title).isEmpty();
    assertThat(streamInfo.url).isEqualTo("test_url");
  }

  @Test
  public void decode_semiColonInTitle() {
    IcyDecoder decoder = new IcyDecoder();
    Metadata metadata = decoder.decode("StreamTitle='test; title';StreamURL='test_url';");

    assertThat(metadata.length()).isEqualTo(1);
    IcyInfo streamInfo = (IcyInfo) metadata.get(0);
    assertThat(streamInfo.title).isEqualTo("test; title");
    assertThat(streamInfo.url).isEqualTo("test_url");
  }

  @Test
  public void decode_quoteInTitle() {
    IcyDecoder decoder = new IcyDecoder();
    Metadata metadata = decoder.decode("StreamTitle='test' title';StreamURL='test_url';");

    assertThat(metadata.length()).isEqualTo(1);
    IcyInfo streamInfo = (IcyInfo) metadata.get(0);
    assertThat(streamInfo.title).isEqualTo("test' title");
    assertThat(streamInfo.url).isEqualTo("test_url");
  }

  @Test
  public void decode_lineTerminatorInTitle() {
    IcyDecoder decoder = new IcyDecoder();
    Metadata metadata = decoder.decode("StreamTitle='test\r\ntitle';StreamURL='test_url';");

    assertThat(metadata.length()).isEqualTo(1);
    IcyInfo streamInfo = (IcyInfo) metadata.get(0);
    assertThat(streamInfo.title).isEqualTo("test\r\ntitle");
    assertThat(streamInfo.url).isEqualTo("test_url");
  }

  @Test
  public void decode_notIcy() {
    IcyDecoder decoder = new IcyDecoder();
    Metadata metadata = decoder.decode("NotIcyData");

    assertThat(metadata).isNull();
  }
}
