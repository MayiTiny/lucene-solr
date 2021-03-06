/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.index.StorableField;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.UnicodeUtil;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.search.QParser;

public class StrField extends PrimitiveFieldType {

  @Override
  protected void init(IndexSchema schema, Map<String,String> args) {
    super.init(schema, args);
  }

  @Override
  public List<StorableField> createFields(SchemaField field, Object value,
      float boost) {
    if (field.hasDocValues()) {
      List<StorableField> fields = new ArrayList<StorableField>();
      fields.add(createField(field, value, boost));
      final BytesRef bytes = new BytesRef(value.toString());
      if (field.multiValued()) {
        fields.add(new SortedSetDocValuesField(field.getName(), bytes));
      } else {
        fields.add(new SortedDocValuesField(field.getName(), bytes));
      }
      return fields;
    } else {
      return Collections.singletonList(createField(field, value, boost));
    }
  }

  @Override
  public SortField getSortField(SchemaField field,boolean reverse) {
    return getStringSort(field,reverse);
  }

  @Override
  public void write(TextResponseWriter writer, String name, StorableField f) throws IOException {
    writer.writeStr(name, f.stringValue(), true);
  }

  @Override
  public ValueSource getValueSource(SchemaField field, QParser parser) {
    field.checkFieldCacheSource(parser);
    return new StrFieldSource(field.getName());
  }

  @Override
  public Object toObject(SchemaField sf, BytesRef term) {
    return term.utf8ToString();
  }

  @Override
  public void checkSchemaField(SchemaField field) {
  }

  @Override
  public Object marshalSortValue(Object value) {
    if (null == value) {
      return null;
    }
    CharsRef spare = new CharsRef();
    UnicodeUtil.UTF8toUTF16((BytesRef)value, spare);
    return spare.toString();
  }

  @Override
  public Object unmarshalSortValue(Object value) {
    if (null == value) {
      return null;
    }
    BytesRef spare = new BytesRef();
    String stringVal = (String)value;
    UnicodeUtil.UTF16toUTF8(stringVal, 0, stringVal.length(), spare);
    return spare;
  }
}


