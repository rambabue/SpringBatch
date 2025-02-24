package org.prayaga.config;


import com.example.model.MyRecord;
import com.example.processor.MyRecordProcessor;
import com.example.reader.MultiFileItemReader;
import com.example.writer.MyRecordWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public MultiFileItemReader<MyRecord> multiFileItemReader() {
        List<FileSystemResource> resources = Arrays.asList(
                new FileSystemResource("src/main/resources/file1.csv"),
                new FileSystemResource("src/main/resources/file2.csv")
        );
        return new MultiFileItemReader<>(resources, MyRecord.class);
    }

    @Bean
    public Step step() {
        return stepBuilderFactory.get("step")
                .<MyRecord, MyRecord>chunk(10)
                .reader(multiFileItemReader())
                .processor(new MyRecordProcessor())
                .writer(new MyRecordWriter())
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .start(step())
                .build();
    }
}