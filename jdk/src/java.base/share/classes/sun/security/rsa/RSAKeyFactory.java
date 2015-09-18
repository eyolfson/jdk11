/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.security.rsa;

import java.math.BigInteger;

import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;

import sun.security.action.GetPropertyAction;

/**
 * KeyFactory for RSA keys. Keys must be instances of PublicKey or PrivateKey
 * and getAlgorithm() must return "RSA". For such keys, it supports conversion
 * between the following:
 *
 * For public keys:
 *  . PublicKey with an X.509 encoding
 *  . RSAPublicKey
 *  . RSAPublicKeySpec
 *  . X509EncodedKeySpec
 *
 * For private keys:
 *  . PrivateKey with a PKCS#8 encoding
 *  . RSAPrivateKey
 *  . RSAPrivateCrtKey
 *  . RSAPrivateKeySpec
 *  . RSAPrivateCrtKeySpec
 *  . PKCS8EncodedKeySpec
 * (of course, CRT variants only for CRT keys)
 *
 * Note: as always, RSA keys should be at least 512 bits long
 *
 * @since   1.5
 * @author  Andreas Sterbenz
 */
public final class RSAKeyFactory extends KeyFactorySpi {

    private static final Class<?> rsaPublicKeySpecClass =
                                                RSAPublicKeySpec.class;
    private static final Class<?> rsaPrivateKeySpecClass =
                                                RSAPrivateKeySpec.class;
    private static final Class<?> rsaPrivateCrtKeySpecClass =
                                                RSAPrivateCrtKeySpec.class;

    private static final Class<?> x509KeySpecClass  = X509EncodedKeySpec.class;
    private static final Class<?> pkcs8KeySpecClass = PKCS8EncodedKeySpec.class;

    public static final int MIN_MODLEN = 512;
    public static final int MAX_MODLEN = 16384;

    /*
     * If the modulus length is above this value, restrict the size of
     * the exponent to something that can be reasonably computed.  We
     * could simply hardcode the exp len to something like 64 bits, but
     * this approach allows flexibility in case impls would like to use
     * larger module and exponent values.
     */
    public static final int MAX_MODLEN_RESTRICT_EXP = 3072;
    public static final int MAX_RESTRICTED_EXPLEN = 64;

    private static final boolean restrictExpLen =
        "true".equalsIgnoreCase(AccessController.doPrivileged(
            new GetPropertyAction(
                "sun.security.rsa.restrictRSAExponent", "true")));

    // instance used for static translateKey();
    private static final RSAKeyFactory INSTANCE = new RSAKeyFactory();

    public RSAKeyFactory() {
        // empty
    }

    /**
     * Static method to convert Key into an instance of RSAPublicKeyImpl
     * or RSAPrivate(Crt)KeyImpl. If the key is not an RSA key or cannot be
     * used, throw an InvalidKeyException.
     *
     * Used by RSASignature and RSACipher.
     */
    public static RSAKey toRSAKey(Key key) throws InvalidKeyException {
        if ((key instanceof RSAPrivateKeyImpl) ||
            (key instanceof RSAPrivateCrtKeyImpl) ||
            (key instanceof RSAPublicKeyImpl)) {
            return (RSAKey)key;
        } else {
            return (RSAKey)INSTANCE.engineTranslateKey(key);
        }
    }

    /*
     * Single test entry point for all of the mechanisms in the SunRsaSign
     * provider (RSA*KeyImpls).  All of the tests are the same.
     *
     * For compatibility, we round up to the nearest byte here:
     * some Key impls might pass in a value within a byte of the
     * real value.
     */
    static void checkRSAProviderKeyLengths(int modulusLen, BigInteger exponent)
            throws InvalidKeyException {
        checkKeyLengths(((modulusLen + 7) & ~7), exponent,
            RSAKeyFactory.MIN_MODLEN, Integer.MAX_VALUE);
    }

    /**
     * Check the length of an RSA key modulus/exponent to make sure it
     * is not too short or long.  Some impls have their own min and
     * max key sizes that may or may not match with a system defined value.
     *
     * @param modulusLen the bit length of the RSA modulus.
     * @param exponent the RSA exponent
     * @param minModulusLen if {@literal > 0}, check to see if modulusLen is at
     *        least this long, otherwise unused.
     * @param maxModulusLen caller will allow this max number of bits.
     *        Allow the smaller of the system-defined maximum and this param.
     *
     * @throws InvalidKeyException if any of the values are unacceptable.
     */
     public static void checkKeyLengths(int modulusLen, BigInteger exponent,
            int minModulusLen, int maxModulusLen) throws InvalidKeyException {

        if ((minModulusLen > 0) && (modulusLen < (minModulusLen))) {
            throw new InvalidKeyException( "RSA keys must be at least " +
                minModulusLen + " bits long");
        }

        // Even though our policy file may allow this, we don't want
        // either value (mod/exp) to be too big.

        int maxLen = Math.min(maxModulusLen, MAX_MODLEN);

        // If a RSAPrivateKey/RSAPublicKey, make sure the
        // modulus len isn't too big.
        if (modulusLen > maxLen) {
            throw new InvalidKeyException(
                "RSA keys must be no longer than " + maxLen + " bits");
        }

        // If a RSAPublicKey, make sure the exponent isn't too big.
        if (restrictExpLen && (exponent != null) &&
                (modulusLen > MAX_MODLEN_RESTRICT_EXP) &&
                (exponent.bitLength() > MAX_RESTRICTED_EXPLEN)) {
            throw new InvalidKeyException(
                "RSA exponents can be no longer than " +
                MAX_RESTRICTED_EXPLEN + " bits " +
                " if modulus is greater than " +
                MAX_MODLEN_RESTRICT_EXP + " bits");
        }
    }

    /**
     * Translate an RSA key into a SunRsaSign RSA key. If conversion is
     * not possible, throw an InvalidKeyException.
     * See also JCA doc.
     */
    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key must not be null");
        }
        String keyAlg = key.getAlgorithm();
        if (keyAlg.equals("RSA") == false) {
            throw new InvalidKeyException("Not an RSA key: " + keyAlg);
        }
        if (key instanceof PublicKey) {
            return translatePublicKey((PublicKey)key);
        } else if (key instanceof PrivateKey) {
            return translatePrivateKey((PrivateKey)key);
        } else {
            throw new InvalidKeyException("Neither a public nor a private key");
        }
    }

    // see JCA doc
    protected PublicKey engineGeneratePublic(KeySpec keySpec)
            throws InvalidKeySpecException {
        try {
            return generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw e;
        } catch (GeneralSecurityException e) {
            throw new InvalidKeySpecException(e);
        }
    }

    // see JCA doc
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec)
            throws InvalidKeySpecException {
        try {
            return generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw e;
        } catch (GeneralSecurityException e) {
            throw new InvalidKeySpecException(e);
        }
    }

    // internal implementation of translateKey() for public keys. See JCA doc
    private PublicKey translatePublicKey(PublicKey key)
            throws InvalidKeyException {
        if (key instanceof RSAPublicKey) {
            if (key instanceof RSAPublicKeyImpl) {
                return key;
            }
            RSAPublicKey rsaKey = (RSAPublicKey)key;
            try {
                return new RSAPublicKeyImpl(
                    rsaKey.getModulus(),
                    rsaKey.getPublicExponent()
                );
            } catch (RuntimeException e) {
                // catch providers that incorrectly implement RSAPublicKey
                throw new InvalidKeyException("Invalid key", e);
            }
        } else if ("X.509".equals(key.getFormat())) {
            byte[] encoded = key.getEncoded();
            return new RSAPublicKeyImpl(encoded);
        } else {
            throw new InvalidKeyException("Public keys must be instance "
                + "of RSAPublicKey or have X.509 encoding");
        }
    }

    // internal implementation of translateKey() for private keys. See JCA doc
    private PrivateKey translatePrivateKey(PrivateKey key)
            throws InvalidKeyException {
        if (key instanceof RSAPrivateCrtKey) {
            if (key instanceof RSAPrivateCrtKeyImpl) {
                return key;
            }
            RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey)key;
            try {
                return new RSAPrivateCrtKeyImpl(
                    rsaKey.getModulus(),
                    rsaKey.getPublicExponent(),
                    rsaKey.getPrivateExponent(),
                    rsaKey.getPrimeP(),
                    rsaKey.getPrimeQ(),
                    rsaKey.getPrimeExponentP(),
                    rsaKey.getPrimeExponentQ(),
                    rsaKey.getCrtCoefficient()
                );
            } catch (RuntimeException e) {
                // catch providers that incorrectly implement RSAPrivateCrtKey
                throw new InvalidKeyException("Invalid key", e);
            }
        } else if (key instanceof RSAPrivateKey) {
            if (key instanceof RSAPrivateKeyImpl) {
                return key;
            }
            RSAPrivateKey rsaKey = (RSAPrivateKey)key;
            try {
                return new RSAPrivateKeyImpl(
                    rsaKey.getModulus(),
                    rsaKey.getPrivateExponent()
                );
            } catch (RuntimeException e) {
                // catch providers that incorrectly implement RSAPrivateKey
                throw new InvalidKeyException("Invalid key", e);
            }
        } else if ("PKCS#8".equals(key.getFormat())) {
            byte[] encoded = key.getEncoded();
            return RSAPrivateCrtKeyImpl.newKey(encoded);
        } else {
            throw new InvalidKeyException("Private keys must be instance "
                + "of RSAPrivate(Crt)Key or have PKCS#8 encoding");
        }
    }

    // internal implementation of generatePublic. See JCA doc
    private PublicKey generatePublic(KeySpec keySpec)
            throws GeneralSecurityException {
        if (keySpec instanceof X509EncodedKeySpec) {
            X509EncodedKeySpec x509Spec = (X509EncodedKeySpec)keySpec;
            return new RSAPublicKeyImpl(x509Spec.getEncoded());
        } else if (keySpec instanceof RSAPublicKeySpec) {
            RSAPublicKeySpec rsaSpec = (RSAPublicKeySpec)keySpec;
            return new RSAPublicKeyImpl(
                rsaSpec.getModulus(),
                rsaSpec.getPublicExponent()
            );
        } else {
            throw new InvalidKeySpecException("Only RSAPublicKeySpec "
                + "and X509EncodedKeySpec supported for RSA public keys");
        }
    }

    // internal implementation of generatePrivate. See JCA doc
    private PrivateKey generatePrivate(KeySpec keySpec)
            throws GeneralSecurityException {
        if (keySpec instanceof PKCS8EncodedKeySpec) {
            PKCS8EncodedKeySpec pkcsSpec = (PKCS8EncodedKeySpec)keySpec;
            return RSAPrivateCrtKeyImpl.newKey(pkcsSpec.getEncoded());
        } else if (keySpec instanceof RSAPrivateCrtKeySpec) {
            RSAPrivateCrtKeySpec rsaSpec = (RSAPrivateCrtKeySpec)keySpec;
            return new RSAPrivateCrtKeyImpl(
                rsaSpec.getModulus(),
                rsaSpec.getPublicExponent(),
                rsaSpec.getPrivateExponent(),
                rsaSpec.getPrimeP(),
                rsaSpec.getPrimeQ(),
                rsaSpec.getPrimeExponentP(),
                rsaSpec.getPrimeExponentQ(),
                rsaSpec.getCrtCoefficient()
            );
        } else if (keySpec instanceof RSAPrivateKeySpec) {
            RSAPrivateKeySpec rsaSpec = (RSAPrivateKeySpec)keySpec;
            return new RSAPrivateKeyImpl(
                rsaSpec.getModulus(),
                rsaSpec.getPrivateExponent()
            );
        } else {
            throw new InvalidKeySpecException("Only RSAPrivate(Crt)KeySpec "
                + "and PKCS8EncodedKeySpec supported for RSA private keys");
        }
    }

    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec)
            throws InvalidKeySpecException {
        try {
            // convert key to one of our keys
            // this also verifies that the key is a valid RSA key and ensures
            // that the encoding is X.509/PKCS#8 for public/private keys
            key = engineTranslateKey(key);
        } catch (InvalidKeyException e) {
            throw new InvalidKeySpecException(e);
        }
        if (key instanceof RSAPublicKey) {
            RSAPublicKey rsaKey = (RSAPublicKey)key;
            if (rsaPublicKeySpecClass.isAssignableFrom(keySpec)) {
                return keySpec.cast(new RSAPublicKeySpec(
                    rsaKey.getModulus(),
                    rsaKey.getPublicExponent()
                ));
            } else if (x509KeySpecClass.isAssignableFrom(keySpec)) {
                return keySpec.cast(new X509EncodedKeySpec(key.getEncoded()));
            } else {
                throw new InvalidKeySpecException
                        ("KeySpec must be RSAPublicKeySpec or "
                        + "X509EncodedKeySpec for RSA public keys");
            }
        } else if (key instanceof RSAPrivateKey) {
            if (pkcs8KeySpecClass.isAssignableFrom(keySpec)) {
                return keySpec.cast(new PKCS8EncodedKeySpec(key.getEncoded()));
            } else if (rsaPrivateCrtKeySpecClass.isAssignableFrom(keySpec)) {
                if (key instanceof RSAPrivateCrtKey) {
                    RSAPrivateCrtKey crtKey = (RSAPrivateCrtKey)key;
                    return keySpec.cast(new RSAPrivateCrtKeySpec(
                        crtKey.getModulus(),
                        crtKey.getPublicExponent(),
                        crtKey.getPrivateExponent(),
                        crtKey.getPrimeP(),
                        crtKey.getPrimeQ(),
                        crtKey.getPrimeExponentP(),
                        crtKey.getPrimeExponentQ(),
                        crtKey.getCrtCoefficient()
                    ));
                } else {
                    throw new InvalidKeySpecException
                    ("RSAPrivateCrtKeySpec can only be used with CRT keys");
                }
            } else if (rsaPrivateKeySpecClass.isAssignableFrom(keySpec)) {
                RSAPrivateKey rsaKey = (RSAPrivateKey)key;
                return keySpec.cast(new RSAPrivateKeySpec(
                    rsaKey.getModulus(),
                    rsaKey.getPrivateExponent()
                ));
            } else {
                throw new InvalidKeySpecException
                        ("KeySpec must be RSAPrivate(Crt)KeySpec or "
                        + "PKCS8EncodedKeySpec for RSA private keys");
            }
        } else {
            // should not occur, caught in engineTranslateKey()
            throw new InvalidKeySpecException("Neither public nor private key");
        }
    }
}
