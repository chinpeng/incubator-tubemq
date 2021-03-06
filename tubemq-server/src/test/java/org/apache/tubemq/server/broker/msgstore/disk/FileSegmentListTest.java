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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.tubemq.server.broker.utils.DataStoreUtils;
import org.junit.Assert;
import org.junit.Test;

/***
 * FileSegmentList test
 */
public class FileSegmentListTest {

    FileSegmentList fileSegmentList;

    @Test
    public void getView() {
        File dir = new File("src/test/resource/data");
        dir.mkdir();
        try {
            File file =
                    new File("src/test/resource/data",
                            DataStoreUtils.nameFromOffset(0L, DataStoreUtils.DATA_FILE_SUFFIX));
            file.createNewFile();
            // create FileSegmentList.
            fileSegmentList = new FileSegmentList(dir, SegmentType.DATA,
                    true, 0L, Long.MAX_VALUE, new StringBuilder());
            Segment fileSegment = fileSegmentList.last();
            String data = "abc";
            // append data to last FileSegment.
            fileSegment.append(ByteBuffer.wrap(data.getBytes()));
            fileSegment.flush(true);
            // get view
            List<Segment> segmentList = fileSegmentList.getView();
            Assert.assertTrue(segmentList.size() == 1);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dir.exists()) {
                dir.delete();
            }
        }
    }

    @Test
    public void append() {
        File dir = new File("src/test/resource/data");
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                file.delete();
            }
            dir.delete();
        }
        dir.mkdir();
        try {
            File file =
                    new File("src/test/resource/data",
                            DataStoreUtils.nameFromOffset(0L, DataStoreUtils.DATA_FILE_SUFFIX));
            file.createNewFile();
            // create FileSegmentList.
            fileSegmentList = new FileSegmentList(dir, SegmentType.DATA,
                    true, 0L, Long.MAX_VALUE, new StringBuilder());
            Segment fileSegment = new FileSegment(100L, file, true, SegmentType.DATA);
            String data = "abc";
            // append data to last FileSegment.
            fileSegment.append(ByteBuffer.wrap(data.getBytes()));
            fileSegment.flush(true);
            fileSegmentList.append(fileSegment);
            List<Segment> segmentList = fileSegmentList.getView();
            Assert.assertTrue(segmentList.size() == 2);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
