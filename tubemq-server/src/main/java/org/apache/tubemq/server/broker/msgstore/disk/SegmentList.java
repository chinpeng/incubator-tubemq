/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tubemq.server.broker.msgstore.disk;

import java.io.IOException;
import java.util.List;

/***
 * Segment list.
 */
public interface SegmentList {

    void close();

    Segment last();

    Segment findSegment(final long offset);

    List<Segment> getView();

    long getSizeInBytes();

    long getMaxOffset();

    boolean checkExpiredSegments(final long checkTimestamp, final long fileValidTimeMs);

    void delExpiredSegments(final StringBuilder sb);

    void flushLast(boolean force) throws IOException;

    long getCommitMaxOffset();

    long getMinOffset();

    void append(final Segment segment);

    void delete(final Segment segment);

    RecordView getRecordView(final long offset, final int maxSize) throws IOException;

    void relRecordView(RecordView recordView);


}
