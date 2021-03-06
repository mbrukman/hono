/*******************************************************************************
 * Copyright (c) 2016, 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.hono.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.hono.client.ServiceInvocationException;
import org.eclipse.hono.config.ClientConfigProperties;
import org.eclipse.hono.util.BufferResult;
import org.eclipse.hono.util.Constants;
import org.eclipse.hono.util.TimeUntilDisconnectNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

/**
 * A helper class for integration tests.
 *
 */
public final class IntegrationTestSupport {

    public static final int    DEFAULT_AMQP_PORT = 4040;
    public static final int    DEFAULT_AMQPS_PORT = 4041;
    public static final int    DEFAULT_DEVICEREGISTRY_AMQP_PORT = 25672;
    public static final int    DEFAULT_DEVICEREGISTRY_HTTP_PORT = 28080;
    public static final int    DEFAULT_DOWNSTREAM_PORT = 15672;
    public static final String DEFAULT_HOST = InetAddress.getLoopbackAddress().getHostAddress();
    public static final int    DEFAULT_HTTP_PORT = 8080;
    public static final int    DEFAULT_HTTPS_PORT = 8443;
    public static final int    DEFAULT_MAX_BCRYPT_ITERATIONS = 10;
    public static final int    DEFAULT_MQTT_PORT = 1883;
    public static final int    DEFAULT_MQTTS_PORT = 8883;

    public static final String PROPERTY_AUTH_HOST = "auth.host";
    public static final String PROPERTY_AUTH_PORT = "auth.amqp.port";
    public static final String PROPERTY_HONO_USERNAME = "hono.username";
    public static final String PROPERTY_HONO_PASSWORD = "hono.password";
    public static final String PROPERTY_DEVICEREGISTRY_HOST = "deviceregistry.host";
    public static final String PROPERTY_DEVICEREGISTRY_AMQP_PORT = "deviceregistry.amqp.port";
    public static final String PROPERTY_DEVICEREGISTRY_HTTP_PORT = "deviceregistry.http.port";
    public static final String PROPERTY_DOWNSTREAM_HOST = "downstream.host";
    public static final String PROPERTY_DOWNSTREAM_PORT = "downstream.amqp.port";
    public static final String PROPERTY_DOWNSTREAM_USERNAME = "downstream.username";
    public static final String PROPERTY_DOWNSTREAM_PASSWORD = "downstream.password";
    public static final String PROPERTY_HTTP_HOST = "http.host";
    public static final String PROPERTY_HTTP_PORT = "http.port";
    public static final String PROPERTY_HTTPS_PORT = "https.port";
    public static final String PROPERTY_MQTT_HOST = "mqtt.host";
    public static final String PROPERTY_MQTT_PORT = "mqtt.port";
    public static final String PROPERTY_MQTTS_PORT = "mqtts.port";
    public static final String PROPERTY_AMQP_HOST = "adapter.amqp.host";
    public static final String PROPERTY_AMQP_PORT = "adapter.amqp.port";
    public static final String PROPERTY_AMQPS_PORT = "adapter.amqps.port";
    public static final String PROPERTY_TENANT = "tenant";
    public static final String PROPTERY_MAX_BCRYPT_ITERATIONS = "max.bcrypt.iterations";

    public static final String AUTH_HOST = System.getProperty(PROPERTY_AUTH_HOST, DEFAULT_HOST);
    public static final int    AUTH_PORT = Integer.getInteger(PROPERTY_AUTH_PORT, Constants.PORT_AMQP);

    public static final String HONO_USER = System.getProperty(PROPERTY_HONO_USERNAME);
    public static final String HONO_PWD = System.getProperty(PROPERTY_HONO_PASSWORD);

    public static final String HONO_DEVICEREGISTRY_HOST = System.getProperty(PROPERTY_DEVICEREGISTRY_HOST, DEFAULT_HOST);
    public static final int    HONO_DEVICEREGISTRY_AMQP_PORT = Integer.getInteger(PROPERTY_DEVICEREGISTRY_AMQP_PORT, DEFAULT_DEVICEREGISTRY_AMQP_PORT);
    public static final int    HONO_DEVICEREGISTRY_HTTP_PORT = Integer.getInteger(PROPERTY_DEVICEREGISTRY_HTTP_PORT, DEFAULT_DEVICEREGISTRY_HTTP_PORT);

    public static final String DOWNSTREAM_HOST = System.getProperty(PROPERTY_DOWNSTREAM_HOST, DEFAULT_HOST);
    public static final int    DOWNSTREAM_PORT = Integer.getInteger(PROPERTY_DOWNSTREAM_PORT, DEFAULT_DOWNSTREAM_PORT);
    public static final String DOWNSTREAM_USER = System.getProperty(PROPERTY_DOWNSTREAM_USERNAME);
    public static final String DOWNSTREAM_PWD = System.getProperty(PROPERTY_DOWNSTREAM_PASSWORD);
    public static final String RESTRICTED_CONSUMER_NAME = "user1@HONO";
    public static final String RESTRICTED_CONSUMER_PWD = "pw";

    public static final String HTTP_HOST = System.getProperty(PROPERTY_HTTP_HOST, DEFAULT_HOST);
    public static final int    HTTP_PORT = Integer.getInteger(PROPERTY_HTTP_PORT, DEFAULT_HTTP_PORT);
    public static final int    HTTPS_PORT = Integer.getInteger(PROPERTY_HTTPS_PORT, DEFAULT_HTTPS_PORT);
    public static final String MQTT_HOST = System.getProperty(PROPERTY_MQTT_HOST, DEFAULT_HOST);
    public static final int    MQTT_PORT = Integer.getInteger(PROPERTY_MQTT_PORT, DEFAULT_MQTT_PORT);
    public static final int    MQTTS_PORT = Integer.getInteger(PROPERTY_MQTTS_PORT, DEFAULT_MQTTS_PORT);
    public static final String AMQP_HOST = System.getProperty(PROPERTY_AMQP_HOST, DEFAULT_HOST);
    public static final int    AMQP_PORT = Integer.getInteger(PROPERTY_AMQP_PORT, DEFAULT_AMQP_PORT);
    public static final int    AMQPS_PORT = Integer.getInteger(PROPERTY_AMQPS_PORT, DEFAULT_AMQPS_PORT);

    public static final String PATH_SEPARATOR = System.getProperty("hono.pathSeparator", "/");
    public static final int    MSG_COUNT = Integer.getInteger("msg.count", 400);

    public static final int    MAX_BCRYPT_ITERATIONS = Integer.getInteger(PROPTERY_MAX_BCRYPT_ITERATIONS, DEFAULT_MAX_BCRYPT_ITERATIONS);

    public static final String TRUST_STORE_PATH = System.getProperty("trust-store.path");

    /**
     * The name of the tenant for which only the MQTT adapter is enabled.
     */
    public static final String TENANT_MQTT_ONLY = "MQTT_ONLY";
    /**
     * The name of the tenant for which only the HTTP adapter is enabled.
     */
    public static final String TENANT_HTTP_ONLY = "HTTP_ONLY";

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestSupport.class);
    private static final BCryptPasswordEncoder bcryptPwdEncoder = new BCryptPasswordEncoder(4);

    /**
     * A client for managing tenants/devices/credentials.
     */
    public DeviceRegistryHttpClient registry;
    /**
     * A client for connecting to the AMQP Messaging Network.
     * This can be used for downstream <em>or</em> upstream messages.
     */
    public IntegrationTestHonoClient honoClient;

    private final Set<String> tenantsToDelete = new HashSet<>();
    private final Map<String, Set<String>> devicesToDelete = new HashMap<>();
    private final Vertx vertx;

    /**
     * Creates a new helper instance.
     * 
     * @param vertx The vert.x instance.
     * @throws NullPointerException if vert.x is {@code null}.
     */
    public IntegrationTestSupport(final Vertx vertx) {
        this.vertx = Objects.requireNonNull(vertx);
    }

    /**
     * Connects to the AMQP 1.0 Messaging Network.
     * <p>
     * Also creates an HTTP client for accessing the Device Registry.
     * 
     * @param ctx The vert.x test context.
     */
    public void init(final TestContext ctx) {

        final ClientConfigProperties downstreamProps = new ClientConfigProperties();
        downstreamProps.setHost(IntegrationTestSupport.DOWNSTREAM_HOST);
        downstreamProps.setPort(IntegrationTestSupport.DOWNSTREAM_PORT);
        downstreamProps.setUsername(IntegrationTestSupport.DOWNSTREAM_USER);
        downstreamProps.setPassword(IntegrationTestSupport.DOWNSTREAM_PWD);
        downstreamProps.setFlowLatency(200);
        init(ctx, downstreamProps);
    }

    /**
     * Connects to the AMQP 1.0 Messaging Network.
     * <p>
     * Also creates an HTTP client for accessing the Device Registry.
     * 
     * @param ctx The vert.x test context.
     * @param downstreamProps The properties for connecting to the AMQP Messaging
     *                           Network.
     */
    public void init(final TestContext ctx, final ClientConfigProperties downstreamProps) {

        initRegistryClient(ctx);
        final Async amqpNetworkConnection = ctx.async();
        honoClient = new IntegrationTestHonoClient(vertx, downstreamProps);
        honoClient.connect().setHandler(ctx.asyncAssertSuccess(ok -> {
            LOGGER.info("connected to AMQP Messaging Network [{}:{}]", downstreamProps.getHost(), downstreamProps.getPort());
            amqpNetworkConnection.complete();
        }));
        amqpNetworkConnection.await();
    }

    /**
     * Creates an HTTP client for accessing the Device Registry.
     * 
     * @param ctx The vert.x test context.
     */
    public void initRegistryClient(final TestContext ctx) {

        registry = new DeviceRegistryHttpClient(
                vertx,
                IntegrationTestSupport.HONO_DEVICEREGISTRY_HOST,
                IntegrationTestSupport.HONO_DEVICEREGISTRY_HTTP_PORT);
    }

    /**
     * Deletes all temporary objects from the Device Registry which
     * have been created during the last test execution.
     * 
     * @param ctx The vert.x context.
     */
    public void deleteObjects(final TestContext ctx) {

        devicesToDelete.forEach((tenantId, devices) -> {
            devices.forEach(deviceId -> {
                final Async deletion = ctx.async();
                CompositeFuture.join(
                        registry.deregisterDevice(tenantId, deviceId),
                        registry.removeAllCredentials(tenantId, deviceId))
                    .setHandler(ok -> deletion.complete());
                deletion.await(1000);
            });
        });
        devicesToDelete.clear();

        tenantsToDelete.forEach(tenantId -> {
            final Async deletion = ctx.async();
            registry.removeTenant(tenantId).setHandler(ok -> deletion.complete());
            deletion.await(1000);
        });
        tenantsToDelete.clear();
    }

    /**
     * Closes the connections to the AMQP 1.0 Messaging Network.
     * 
     * @param ctx The vert.x test context.
     */
    public void disconnect(final TestContext ctx) {

        final Async shutdown = ctx.async();
        honoClient.shutdown(ctx.asyncAssertSuccess(ok -> {
            LOGGER.info("connection to AMQP Messaging Network closed");
            shutdown.complete();
        }));
        shutdown.await();
    }

    /**
     * Gets a random tenant identifier and adds it to the list
     * of tenants to be deleted after the current test has finished.
     * 
     * @return The identifier.
     * @see #deleteObjects(TestContext)
     */
    public String getRandomTenantId() {
        final String tenantId = UUID.randomUUID().toString();
        tenantsToDelete.add(tenantId);
        return tenantId;
    }

    /**
     * Gets a random device identifier and adds it to the list
     * of devices to be deleted after the current test has finished.
     * 
     * @param tenantId The tenant that he device belongs to.
     * @return The identifier.
     * @see #deleteObjects(TestContext)
     */
    public String getRandomDeviceId(final String tenantId) {
        final String deviceId = UUID.randomUUID().toString();
        final Set<String> devices = devicesToDelete.computeIfAbsent(tenantId, t -> new HashSet<>());
        devices.add(deviceId);
        return deviceId;
    }

    /**
     * Sends a command to a device.
     * 
     * @param notification The empty notification indicating the device's readiness to receive a command.
     * @param command The name of the command to send.
     * @param contentType The type of the command's input data.
     * @param payload The command's input data to send to the device.
     * @param properties The headers to include in the command message as AMQP application properties.
     * @return A future that is either succeeded with the response payload from the device or
     *         failed with a {@link ServiceInvocationException}.
     */
    public Future<BufferResult> sendCommand(
            final TimeUntilDisconnectNotification notification,
            final String command,
            final String contentType,
            final Buffer payload,
            final Map<String, Object> properties) {

        return sendCommand(
                notification.getTenantId(),
                notification.getDeviceId(),
                command,
                contentType,
                payload,
                properties,
                notification.getMillisecondsUntilExpiry());
    }

    /**
     * Sends a command to a device.
     * 
     * @param tenantId The tenant that the device belongs to.
     * @param deviceId The identifier of the device.
     * @param command The name of the command to send.
     * @param contentType The type of the command's input data.
     * @param payload The command's input data to send to the device.
     * @param properties The headers to include in the command message as AMQP application properties.
     * @param requestTimeout The number of milliseconds to wait for a response from the device.
     * @return A future that is either succeeded with the response payload from the device or
     *         failed with a {@link ServiceInvocationException}.
     */
    public Future<BufferResult> sendCommand(
            final String tenantId,
            final String deviceId,
            final String command,
            final String contentType,
            final Buffer payload,
            final Map<String, Object> properties,
            final long requestTimeout) {

        return honoClient.getOrCreateCommandClient(tenantId, deviceId).compose(commandClient -> {

            commandClient.setRequestTimeout(requestTimeout);
            final Future<BufferResult> result = Future.future();
            final Handler<Void> send = s -> {
                // send the command upstream to the device
                LOGGER.trace("sending command [name: {}, contentType: {}, payload: {}]", command, contentType, payload);
                commandClient.sendCommand(command, contentType, payload, properties).map(responsePayload -> {
                    LOGGER.debug("successfully sent command [name: {}, payload: {}] and received response [payload: {}]",
                            command, payload, responsePayload);
                    commandClient.close(v -> {});
                    return responsePayload;
                }).recover(t -> {
                    LOGGER.debug("could not send command or did not receive a response: {}", t.getMessage());
                    commandClient.close(v -> {});
                    return Future.failedFuture(t);
                }).setHandler(result);
            };
            if (commandClient.getCredit() == 0) {
                commandClient.sendQueueDrainHandler(send);
            } else {
                send.handle(null);
            }
            return result;
        });
    }

    /**
     * Sends a one-way command to a device.
     * 
     * @param notification The empty notification indicating the device's readiness to receive a command.
     * @param command The name of the command to send.
     * @param contentType The type of the command's input data.
     * @param payload The command's input data to send to the device.
     * @param properties The headers to include in the command message as AMQP application properties.
     * @return A future that is either succeeded if the command has been sent to the device or
     *         failed with a {@link ServiceInvocationException}.
     */
    public Future<Void> sendOneWayCommand(
            final TimeUntilDisconnectNotification notification,
            final String command,
            final String contentType,
            final Buffer payload,
            final Map<String, Object> properties) {

        return sendOneWayCommand(
                notification.getTenantId(),
                notification.getDeviceId(),
                command,
                contentType,
                payload,
                properties,
                notification.getMillisecondsUntilExpiry());
    }

    /**
     * Sends a one-way command to a device.
     * 
     * @param tenantId The tenant that the device belongs to.
     * @param deviceId The identifier of the device.
     * @param command The name of the command to send.
     * @param contentType The type of the command's input data.
     * @param payload The command's input data to send to the device.
     * @param properties The headers to include in the command message as AMQP application properties.
     * @param requestTimeout The number of milliseconds to wait for the command being sent to the device.
     * @return A future that is either succeeded if the command has been sent to the device or
     *         failed with a {@link ServiceInvocationException}.
     */
    public Future<Void> sendOneWayCommand(
            final String tenantId,
            final String deviceId,
            final String command,
            final String contentType,
            final Buffer payload,
            final Map<String, Object> properties,
            final long requestTimeout) {

        return honoClient.getOrCreateCommandClient(tenantId, deviceId).compose(commandClient -> {

            commandClient.setRequestTimeout(requestTimeout);
            final Future<Void> result = Future.future();
            final Handler<Void> send = s -> {
                // send the command upstream to the device
                LOGGER.trace("sending one-way command [name: {}, contentType: {}, payload: {}]", command, contentType, payload);
                commandClient.sendOneWayCommand(command, contentType, payload, properties).map(ok -> {
                    LOGGER.debug("successfully sent one-way command [name: {}, payload: {}]", command, payload);
                    commandClient.close(v -> {});
                    return (Void) null;
                }).recover(t -> {
                    LOGGER.debug("could not send one-way command: {}", t.getMessage());
                    commandClient.close(v -> {});
                    return Future.failedFuture(t);
                }).setHandler(result);
            };
            if (commandClient.getCredit() == 0) {
                commandClient.sendQueueDrainHandler(send);
            } else {
                send.handle(null);
            }
            return result;
        });
    }

    /**
     * A simple implementation of subtree containment: all entries of the JsonObject that is tested to be contained
     * must be contained in the other JsonObject as well. Nested JsonObjects are treated the same by recursively calling
     * this method to test the containment.
     * JsonArrays are tested for containment as well: all elements in a JsonArray belonging to the contained JsonObject
     * must be present in the corresponding JsonArray of the other JsonObject as well. The sequence of the array elements
     * is not important (suitable for the current tests).
     * @param jsonObject The JsonObject that must fully contain the other JsonObject (but may contain more entries as well).
     * @param jsonObjectToBeContained The JsonObject that needs to be fully contained inside the other JsonObject.
     * @return The result of the containment test.
     */
    public static boolean testJsonObjectToBeContained(final JsonObject jsonObject, final JsonObject jsonObjectToBeContained) {
        if (jsonObjectToBeContained == null) {
            return true;
        }
        if (jsonObject == null) {
            return false;
        }
        final AtomicBoolean containResult = new AtomicBoolean(true);

        jsonObjectToBeContained.forEach(entry -> {
            if (!jsonObject.containsKey(entry.getKey())) {
                containResult.set(false);
            } else {
                if (entry.getValue() == null) {
                    if (jsonObject.getValue(entry.getKey()) != null) {
                        containResult.set(false);
                    }
                } else if (entry.getValue() instanceof JsonObject) {
                    if (!(jsonObject.getValue(entry.getKey()) instanceof JsonObject)) {
                        containResult.set(false);
                    } else {
                        if (!testJsonObjectToBeContained((JsonObject) entry.getValue(),
                                (JsonObject) jsonObject.getValue(entry.getKey()))) {
                            containResult.set(false);
                        }
                    }
                } else if (entry.getValue() instanceof JsonArray) {
                    if (!(jsonObject.getValue(entry.getKey()) instanceof JsonArray)) {
                        containResult.set(false);
                    } else {
                        // compare two JsonArrays
                        final JsonArray biggerArray = (JsonArray) jsonObject.getValue(entry.getKey());
                        final JsonArray smallerArray = (JsonArray) entry.getValue();

                        if (!testJsonArrayToBeContained(biggerArray, smallerArray)) {
                            containResult.set(false);
                        }
                    }
                } else {
                    if (!entry.getValue().equals(jsonObject.getValue(entry.getKey()))) {
                        containResult.set(false);
                    }
                }
            }
        });
        return containResult.get();
    }

    /**
     * A simple implementation of JsonArray containment: all elements of the JsonArray that is tested to be contained
     * must be contained in the other JsonArray as well. Contained JsonObjects are tested for subtree containment as
     * implemented in {@link #testJsonObjectToBeContained(JsonObject, JsonObject)}.
     * <p>
     * The order sequence of the elements is intentionally not important - the containing array is always iterated from
     * the beginning and the containment of an element is handled as successful if a suitable element in the containing
     * array was found (sufficient for the current tests).
     * <p>
     * For simplicity, the elements of the arrays must be of type JsonObject (sufficient for the current tests).
     * <p>
     * Also note that this implementation is by no means performance optimized - it is for sure not suitable for huge JsonArrays
     * (by using two nested iteration loops inside) and is meant only for quick test results on smaller JsonArrays.
     *
     * @param containingArray The JsonArray that must contain the elements of the other array (the sequence is not important).
     * @param containedArray The JsonArray that must consist only of elements that can be found in the containingArray
     *                       as well (by subtree containment test).
     * @return The result of the containment test.
     */
    public static boolean testJsonArrayToBeContained(final JsonArray containingArray, final JsonArray containedArray) {
        for (final Object containedElem: containedArray) {
            // currently only support contained JsonObjects
            if (!(containedElem instanceof JsonObject)) {
                return false;
            }

            boolean containingElemFound = false;
            for (final Object elemOfBiggerArray: containingArray) {
                if (!(elemOfBiggerArray instanceof JsonObject)) {
                    return false;
                }

                if (testJsonObjectToBeContained((JsonObject) elemOfBiggerArray, (JsonObject) containedElem)) {
                    containingElemFound = true;
                    break;
                }
            }
            if (!containingElemFound) {
                // a full iteration of the containing array did not find a matching element
                return false;
            }
        }
        return true;
    }

    /**
     * Creates an authentication identifier from a device and tenant ID.
     * <p>
     * The returned identifier can be used as the <em>username</em> with
     * Hono's protocol adapters that support username/password authentication.
     * 
     * @param deviceId The device identifier.
     * @param tenant The tenant that the device belongs to.
     * @return The authentication identifier.
     */
    public static String getUsername(final String deviceId, final String tenant) {
        return String.format("%s@%s", deviceId, tenant);
    }

    /**
     * Gets a hash for a password using a given digest based hash function.
     * 
     * @param hashFunction The hash function.
     * @param salt The salt.
     * @param clearTextPassword The password.
     * @return The Base64 encoded password hash.
     */
    public static String getBase64EncodedDigestPasswordHash(final String hashFunction, final byte[] salt, final String clearTextPassword) {
        try {
            final MessageDigest digest = MessageDigest.getInstance(hashFunction);
            if (salt != null) {
                digest.update(salt);
            }
            return Base64.getEncoder().encodeToString(digest.digest(clearTextPassword.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            return "hash function not supported";
        }
    }

    /**
     * Gets a hash for a password using the bcrypt hash function.
     * 
     * @param clearTextPassword The password.
     * @return The hashed password.
     */
    public static String getBcryptHash(final String clearTextPassword) {
        return bcryptPwdEncoder.encode(clearTextPassword);
    }

    /**
     * Generates a certificate object and initializes it with the data read from a file.
     * 
     * @param path The file-system path to load the certificate from.
     * @return A future with the generated certificate on success.
     */
    public Future<X509Certificate> getCertificate(final String path) {

        return loadFile(path).map(buffer -> {
            try (InputStream is = new ByteArrayInputStream(buffer.getBytes())) {
                final CertificateFactory factory = CertificateFactory.getInstance("X.509");
                return (X509Certificate) factory.generateCertificate(is);
            } catch (final Exception e) {
                throw new IllegalArgumentException("file cannot be parsed into X.509 certificate");
            }
        });
    }

    /**
     * Creates a new EC based private/public key pair.
     * 
     * @return The key pair.
     * @throws GeneralSecurityException if the JVM doesn't support ECC.
     */
    public KeyPair newEcKeyPair() throws GeneralSecurityException {

        final KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        return gen.generateKeyPair();
    }

    //----------------------------------< private methods >---
    private Future<Buffer> loadFile(final String path) {

        final Future<Buffer> result = Future.future();
        vertx.fileSystem().readFile(path, result.completer());
        return result;
    }

}
