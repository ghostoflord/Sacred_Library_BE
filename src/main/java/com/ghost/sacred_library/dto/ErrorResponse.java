package com.ghost.sacred_library.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(Instant timestamp, int status, String error, String message, Map<String, String> details) { }
