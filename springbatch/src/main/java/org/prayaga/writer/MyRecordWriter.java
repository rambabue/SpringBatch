package org.prayaga.writer;

import com.example.model.MyRecord;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class MyRecordWriter implements ItemWriter<MyRecord> {

    @Override
    public void write(List<? extends MyRecord> items) throws Exception {
        for (MyRecord item : items) {
            System.out.println("Writing record: " + item);
        }
    }
}