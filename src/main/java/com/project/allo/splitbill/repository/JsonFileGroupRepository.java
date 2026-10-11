package com.project.allo.splitbill.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.allo.splitbill.model.Group;

@Repository 
public class JsonFileGroupRepository implements SplitBillRepository {
    
    private final ObjectMapper mapper;
    private final Path file;
    private final Map<String, Group> groups = new LinkedHashMap<>();

    @Override
    public synchronized Group save(Group group) {
        groups.put(group.getId(), group);
        flush();
        return group;
    }

    @Override
    public synchronized Optional<Group> findById(String id) {
        return Optional.ofNullable(groups.get(id));
    }
    
    public JsonFileGroupRepository(ObjectMapper mapper, @Value("${app.data-file}") String dataFile) {
        this.mapper = mapper;
        this.file = Path.of(dataFile).toAbsolutePath();
        load();
    }
    
    /** Notebook -> whiteboard. Runs once when the app starts. */
    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            List<Group> stored = mapper.readValue(file.toFile(), new TypeReference<List<Group>>() {
            });
            for (Group group : stored) {
                groups.put(group.getId(), group);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read data file " + file, e);
        }
    }

    /** Whiteboard -> notebook. Runs after every save. */
    private void flush() {
        try {
            Files.createDirectories(file.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), new ArrayList<>(groups.values()));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write data file " + file, e);
        }
    }


}
