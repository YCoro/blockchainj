/* Copyright (c) 2017 Stash
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.bitcoinj.wallet.bip47;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.ECPrivateKeySpec;
import org.spongycastle.jce.spec.ECPublicKeySpec;
import org.spongycastle.util.encoders.Hex;

public class SecretPoint {
    private static final ECParameterSpec params = ECNamedCurveTable.getParameterSpec("secp256k1");

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private PrivateKey privKey = null;
    private PublicKey pubKey = null;
    private KeyFactory kf = null;

    public SecretPoint() {
    }

    public SecretPoint(byte[] dataPrv, byte[] dataPub) throws InvalidKeySpecException, InvalidKeyException, IllegalStateException, NoSuchAlgorithmException, NoSuchProviderException {
        this.kf = KeyFactory.getInstance("ECDH", "SC");
        this.privKey = this.loadPrivateKey(dataPrv);
        this.pubKey = this.loadPublicKey(dataPub);
    }

    public PrivateKey getPrivKey() {
        return this.privKey;
    }

    public void setPrivKey(PrivateKey privKey) {
        this.privKey = privKey;
    }

    public PublicKey getPubKey() {
        return this.pubKey;
    }

    public void setPubKey(PublicKey pubKey) {
        this.pubKey = pubKey;
    }

    public byte[] ECDHSecretAsBytes() throws InvalidKeyException, IllegalStateException, NoSuchAlgorithmException, NoSuchProviderException {
        return this.ECDHSecret().getEncoded();
    }

    public boolean isShared(SecretPoint secret) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException {
        return this.equals(secret);
    }

    private SecretKey ECDHSecret() throws InvalidKeyException, IllegalStateException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyAgreement ka = KeyAgreement.getInstance("ECDH", "SC");
        ka.init(this.privKey);
        ka.doPhase(this.pubKey, true);
        SecretKey secret = ka.generateSecret("AES");
        return secret;
    }

    private boolean equals(SecretPoint secret) throws InvalidKeyException, IllegalStateException, NoSuchAlgorithmException, NoSuchProviderException {
        return Hex.toHexString(this.ECDHSecretAsBytes()).equals(Hex.toHexString(secret.ECDHSecretAsBytes()));
    }

    private PublicKey loadPublicKey(byte[] data) throws InvalidKeySpecException {
        ECPublicKeySpec pubKey = new ECPublicKeySpec(params.getCurve().decodePoint(data), params);
        return this.kf.generatePublic(pubKey);
    }

    private PrivateKey loadPrivateKey(byte[] data) throws InvalidKeySpecException {
        ECPrivateKeySpec prvkey = new ECPrivateKeySpec(new BigInteger(1, data), params);
        return this.kf.generatePrivate(prvkey);
    }
}
