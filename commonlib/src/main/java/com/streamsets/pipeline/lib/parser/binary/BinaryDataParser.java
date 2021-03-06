/**
 * Copyright 2015 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.lib.parser.binary;

import com.google.common.io.ByteStreams;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.Stage;
import com.streamsets.pipeline.lib.parser.AbstractDataParser;
import com.streamsets.pipeline.lib.parser.DataParserException;

import java.io.IOException;
import java.io.InputStream;

public class BinaryDataParser extends AbstractDataParser {

  private final Stage.Context context;
  private final InputStream is;
  private final String id;
  private final int maxDataLength;
  private boolean parsed;
  private boolean closed;
  private long offset;

  public BinaryDataParser(Stage.Context context, InputStream is, String id, int maxDataLength) {
    this.context = context;
    this.is = is;
    this.id = id;
    this.maxDataLength = maxDataLength;
  }

  @Override
  public Record parse() throws IOException, DataParserException {
    if (closed) {
      throw new IOException("The parser is closed");
    }
    Record record = null;
    if (!parsed) {
      record = context.createRecord(id);
      record.set(Field.create(getDataToParse()));
      parsed = true;
    }
    return record;
  }

  @Override
  public String getOffset() {
    return String.valueOf(offset);
  }

  @Override
  public void close() throws IOException {
    is.close();
    closed = true;
  }

  public byte[] getDataToParse() throws IOException, DataParserException {
    byte[] bytes = ByteStreams.toByteArray(ByteStreams.limit(is, maxDataLength));
    if(maxDataLength == bytes.length) {
      //check if there is more data in the stream than 'maxDataLength'.
      //If yes, the record must be sent to error.
      //Does not make sense truncating binary data as we don't know what it is.
      if(is.read() != -1) {
        throw new DataParserException(Errors.BINARY_PARSER_00, id, maxDataLength);
      }
    }
    offset = bytes.length;
    return bytes;
  }

}
