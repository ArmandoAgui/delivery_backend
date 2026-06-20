package sv.edu.uca.delivery.backend.payment.config;

import com.paypal.sdk.Environment;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.authentication.ClientCredentialsAuthModel;
import org.slf4j.event.Level;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import sv.edu.uca.delivery.backend.common.exception.BusinessException;

import org.springframework.http.HttpStatus;

@Configuration
public class PaypalConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.paypal", name = "enabled", havingValue = "true")
    PaypalServerSdkClient paypalClient(PaypalProperties properties) {
        if (!StringUtils.hasText(properties.getClientId()) || !StringUtils.hasText(properties.getClientSecret())) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "PayPal is enabled but credentials are missing");
        }
        Environment environment = "PRODUCTION".equalsIgnoreCase(properties.getEnvironment())
                || "LIVE".equalsIgnoreCase(properties.getEnvironment())
                ? Environment.PRODUCTION
                : Environment.SANDBOX;

        return new PaypalServerSdkClient.Builder()
                .loggingConfig(builder -> builder
                        .level(Level.INFO)
                        .requestConfig(logConfigBuilder -> logConfigBuilder.body(false))
                        .responseConfig(logConfigBuilder -> logConfigBuilder.headers(false)))
                .httpClientConfig(configBuilder -> configBuilder.timeout(30000))
                .environment(environment)
                .clientCredentialsAuth(new ClientCredentialsAuthModel.Builder(
                        properties.getClientId(),
                        properties.getClientSecret()
                ).build())
                .build();
    }
}
