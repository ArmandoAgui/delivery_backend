package sv.edu.uca.delivery.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.uploads")
public class UploadProperties {

    private String rootPath = "uploads";
    private String publicPath = "/uploads";
    private long maxFileSizeBytes = 10 * 1024 * 1024;
    private int restaurantMaxWidth = 1280;
    private int productMaxWidth = 800;
    private float quality = 0.82f;

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getPublicPath() {
        return publicPath;
    }

    public void setPublicPath(String publicPath) {
        this.publicPath = publicPath;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public int getRestaurantMaxWidth() {
        return restaurantMaxWidth;
    }

    public void setRestaurantMaxWidth(int restaurantMaxWidth) {
        this.restaurantMaxWidth = restaurantMaxWidth;
    }

    public int getProductMaxWidth() {
        return productMaxWidth;
    }

    public void setProductMaxWidth(int productMaxWidth) {
        this.productMaxWidth = productMaxWidth;
    }

    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }
}
