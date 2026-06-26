package sv.edu.uca.delivery.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(UploadProperties.class)
public class UploadResourceConfig implements WebMvcConfigurer {

    private final UploadProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicPattern = normalizePublicPath(properties.getPublicPath()) + "/**";
        String location = Path.of(properties.getRootPath()).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(publicPattern)
                .addResourceLocations(location);
    }

    private String normalizePublicPath(String publicPath) {
        if (publicPath == null || publicPath.isBlank()) {
            return "/uploads";
        }
        String normalized = publicPath.startsWith("/") ? publicPath : "/" + publicPath;
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
