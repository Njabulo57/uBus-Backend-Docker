package org.tracker.ubus.ubus.Components.Encryption.Abstract;

import javax.crypto.SecretKey;

/**
 * Provides an interface for generating secret keys.
 * Implementations of this interface are responsible for generating
 * cryptographic keys based on the provided input.
 */
public interface ISecretKeyGenerator {

    SecretKey generateSecretKey(String key);
}
