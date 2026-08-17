package pl.m2manager.security.auth.dto;

import java.util.List;

public record PermissionsResponse(List<String> permissions) {
}
