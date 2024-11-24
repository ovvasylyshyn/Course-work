package org.agency.course_work.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Відповідь з токеном доступу")
public class JwtAuthenticationResponse {
    @Schema(description = "Токен доступу", example = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9VU0VSIiwiaWQiOjYsImVtYWlsIjoidmxhZGlAZ21haWwuY29tIiwic3ViIjoidmxhYWQiLCJpYXQiOjE3MzI0NDk2MTQsImV4cCI6MTczMjU5MzYxNH0.pxsnX5POIf15BJeHB5oCpwlvglocTL-9-GlbHgjYNe4")
    private String token;
}
