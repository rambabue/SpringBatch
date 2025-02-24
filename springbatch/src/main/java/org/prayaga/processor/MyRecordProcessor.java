package org.prayaga.processor;


import com.example.model.MyRecord;
import org.springframework.batch.item.ItemProcessor;

public class MyRecordProcessor implements ItemProcessor<MyRecord, MyRecord> {

    @Override
    public MyRecord process(MyRecord item) throws Exception {
        // Perform processing logic here
        item.setField1(item.getField1().toUpperCase()); // Example: Convert field1 to uppercase
        return item;
    }
}