package org.prayaga.reader;

import org.prayaga.utils.CompressionUtils;
import org.prayaga.utils.SerializationUtils;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

public class MultiFileItemReader<T> implements ItemReader<T> {

    private final List<FlatFileItemReader<T>> readers = new ArrayList<>();
    private int currentReaderIndex = 0;

    public MultiFileItemReader(List<Resource> resources, Class<T> targetType) {
        for (Resource resource : resources) {
            FlatFileItemReader<T> reader = new FlatFileItemReader<>();
            reader.setResource(resource);

            DefaultLineMapper<T> lineMapper = new DefaultLineMapper<>();
            DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
            tokenizer.setNames("field1", "field2", "field3"); // Adjust based on your file structure

            lineMapper.setLineTokenizer(tokenizer);
            lineMapper.setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(targetType);
            }});

            reader.setLineMapper(lineMapper);
            readers.add(reader);
        }
    }

    @Override
    public T read() throws Exception {
        if (currentReaderIndex >= readers.size()) {
            return null; // All files have been read
        }

        FlatFileItemReader<T> currentReader = readers.get(currentReaderIndex);

        // Open the reader if it hasn't been opened yet
        if (!isReaderOpen(currentReader)) {
            currentReader.open(new ExecutionContext());
        }

        T item = currentReader.read();

        if (item == null) {
            // Close the current reader and move to the next one
            currentReader.close();
            currentReaderIndex++;
            return read(); // Recursively read from the next file
        }

        // Compress the item (if needed)
        byte[] compressedData = CompressionUtils.compress(SerializationUtils.serialize(item));
        // Decompress the item (if needed)
        byte[] decompressedData = CompressionUtils.decompress(compressedData);
        T decompressedItem = SerializationUtils.deserialize(decompressedData, targetType);

        return decompressedItem;
    }

    private boolean isReaderOpen(FlatFileItemReader<T> reader) {
        try {
            // Attempt to read a line to check if the reader is open
            reader.read();
            return true;
        } catch (IllegalStateException e) {
            return false; // Reader is not open
        }
    }
}