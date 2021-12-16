package com.senomas.common.logback.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogDumpRepository extends MongoRepository<LogDump, String> {

}
