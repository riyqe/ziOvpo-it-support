package com.example.ziovpo.signature;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class KeyStoreKeyProvider implements KeyProvider {

    private final SignatureProperties properties;

    private volatile PrivateKey cachedPrivateKey;
    private volatile VerificationInfo cachedVerificationInfo;

    public KeyStoreKeyProvider(SignatureProperties properties) {
        this.properties = properties;
    }

    @Override
    public PrivateKey getSigningKey() {
        PrivateKey local = cachedPrivateKey;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (cachedPrivateKey == null) {
                loadAndCache();
            }
            return cachedPrivateKey;
        }
    }

    @Override
    public VerificationInfo getVerificationInfo() {
        VerificationInfo local = cachedVerificationInfo;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (cachedVerificationInfo == null) {
                loadAndCache();
            }
            return cachedVerificationInfo;
        }
    }

    private void loadAndCache() {
        if (properties.getKeyStorePassword() == null || properties.getKeyStorePassword().isBlank()) {
            throw new SignatureModuleException(SignatureErrorCode.AUTH_FAILED, "keystore password is empty");
        }

        try (InputStream inputStream = openKeyStoreStream(properties.getKeyStorePath())) {
            KeyStore keyStore = KeyStore.getInstance(properties.getKeyStoreType());
            char[] storePassword = properties.getKeyStorePassword().toCharArray();
            keyStore.load(inputStream, storePassword);

            String alias = properties.getKeyAlias();
            if (!keyStore.containsAlias(alias)) {
                throw new SignatureModuleException(SignatureErrorCode.KEY_NOT_FOUND, "key alias not found");
            }

            String keyPassword = properties.getKeyPassword();
            if (keyPassword == null || keyPassword.isBlank()) {
                keyPassword = properties.getKeyStorePassword();
            }

            Key key = keyStore.getKey(alias, keyPassword.toCharArray());
            if (!(key instanceof PrivateKey privateKey)) {
                throw new SignatureModuleException(SignatureErrorCode.KEY_FORMAT_INVALID, "alias is not a private key");
            }

            if (!(keyStore.getCertificate(alias) instanceof X509Certificate cert)) {
                throw new SignatureModuleException(SignatureErrorCode.KEY_FORMAT_INVALID, "certificate is missing or invalid");
            }

            cachedPrivateKey = privateKey;
            cachedVerificationInfo = new VerificationInfo(cert.getPublicKey(), cert);
        } catch (SignatureModuleException e) {
            throw e;
        } catch (java.io.IOException e) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_SOURCE_UNAVAILABLE, "keystore source unavailable", e);
        } catch (java.security.UnrecoverableKeyException e) {
            throw new SignatureModuleException(SignatureErrorCode.AUTH_FAILED, "invalid key password", e);
        } catch (java.security.GeneralSecurityException e) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_PROVIDER_ERROR, "failed to load key material", e);
        } catch (Exception e) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_PROVIDER_ERROR, "unexpected key provider error", e);
        }
    }

    private InputStream openKeyStoreStream(String pathValue) throws Exception {
        if (pathValue == null || pathValue.isBlank()) {
            throw new SignatureModuleException(SignatureErrorCode.KEY_SOURCE_UNAVAILABLE, "keystore path is empty");
        }

        if (pathValue.startsWith("classpath:")) {
            String path = pathValue.substring("classpath:".length());
            return new ClassPathResource(path).getInputStream();
        }

        if (pathValue.startsWith("file:")) {
            Path path = Paths.get(pathValue.substring("file:".length()));
            return Files.newInputStream(path);
        }

        return Files.newInputStream(Paths.get(pathValue));
    }
}
