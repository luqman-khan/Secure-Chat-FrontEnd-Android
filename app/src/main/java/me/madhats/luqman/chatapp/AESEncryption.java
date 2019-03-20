package me.madhats.luqman.chatapp;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;

public class AESEncryption {
    private static final String ALGO = "AES/CBC/PKCS5Padding";
    private static final int RANDOM_KEY_SIZE = 16;
    private static final int HASH_MAC_SIZE = 20;
    private byte[] random_key = new byte[RANDOM_KEY_SIZE];
    private byte[] key_value;

    public AESEncryption(String key){
        key_value = key.getBytes();
    }
    public String encrypt (String data) throws Exception{
        new SecureRandom().nextBytes(random_key);
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        IvParameterSpec ivSpec = new IvParameterSpec(random_key);
        c.init(Cipher.ENCRYPT_MODE, key, ivSpec);


        // encrypt the message with iv
        System.out.println("payload length at encryption is :"+ data.getBytes().length + " AND "+ RANDOM_KEY_SIZE );
        byte []encVal = c.doFinal(data.getBytes());
        System.out.println("The encoded message at encryption side is "+ Base64.encodeToString(encVal,Base64.DEFAULT)+" its size is "+ encVal.length);
        System.out.println("The IV at encryption side is "+ Base64.encodeToString(random_key,Base64.DEFAULT)+" its size is "+ RANDOM_KEY_SIZE);

        int payload_length = encVal.length+RANDOM_KEY_SIZE;
        //make byte array for iv and message
        byte[] iv_and_message= new byte[payload_length];
        System.arraycopy(random_key, 0, iv_and_message, 0, RANDOM_KEY_SIZE);
        System.arraycopy(encVal, 0, iv_and_message, random_key.length, encVal.length);
        System.out.println("The iv and message at encryption side is "+Base64.encodeToString(iv_and_message,Base64.DEFAULT)+" its size is "+ iv_and_message.length);

        String ss = Base64.encodeToString(iv_and_message, 0 , payload_length, Base64.DEFAULT);
        byte[] bb = ss.getBytes();
        byte[] encm = new byte[bb.length-RANDOM_KEY_SIZE];
        System.arraycopy(bb , RANDOM_KEY_SIZE, encm, 0, bb.length-RANDOM_KEY_SIZE);
        System.out.println("The encoded message at encryption side ******** is "+ Base64.encodeToString(encm, 0 , (bb.length-RANDOM_KEY_SIZE),Base64.DEFAULT)+" its size is "+ encm.length);

        //calculate mac for the payload
        SecretKeySpec keySpec = new SecretKeySpec(key_value, "HmacSHA1");
        Mac hmac = Mac.getInstance("HmacSHA1");
        hmac.init(keySpec);
        byte [] payload_mac = hmac.doFinal(iv_and_message);

        //create full packet
        byte[] fullPacket= new byte[payload_length+HASH_MAC_SIZE];
        System.arraycopy(iv_and_message , 0, fullPacket, 0, iv_and_message.length);
        System.arraycopy(payload_mac , 0, fullPacket, iv_and_message.length, HASH_MAC_SIZE);
        System.out.println("MAC at encryption is: "+ Base64.encodeToString(payload_mac, Base64.DEFAULT));
        System.out.println("payload mac length at encryption is :"+ payload_mac.length+" and fullpacket size is "+fullPacket.length);

        //return encrypted string
        return Base64.encodeToString(fullPacket, Base64.DEFAULT);
    }

    public String decrypt (String encData) throws Exception{
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        byte[] decodedValue = Base64.decode(encData, Base64.DEFAULT);
        System.out.println("Full packet length at decryption is :"+ decodedValue.length);
        byte[] mac = new byte[HASH_MAC_SIZE];

        //get the mac from decoded value
        System.arraycopy(decodedValue, (decodedValue.length-HASH_MAC_SIZE), mac, 0, HASH_MAC_SIZE);

        //get the iv from decoded value
        System.arraycopy(decodedValue, 0, random_key, 0, RANDOM_KEY_SIZE);
        IvParameterSpec ivSpec = new IvParameterSpec(random_key);
        c.init(Cipher.DECRYPT_MODE, key, ivSpec);
        System.out.println("The IV at decryption side is "+ Base64.encodeToString(ivSpec.getIV(),Base64.DEFAULT)+" its size is "+ ivSpec.getIV().length);

        // get iv and message cipher
        byte[] iv_and_message = new byte[decodedValue.length-HASH_MAC_SIZE];
        System.arraycopy(decodedValue, 0, iv_and_message, 0, decodedValue.length-HASH_MAC_SIZE);
        System.out.println("The iv and message at decryption side is "+Base64.encodeToString(iv_and_message,Base64.DEFAULT)+" its size is "+ iv_and_message.length);

        // get encoded message which is after removing hmac
        byte[] encodedMessage = new byte[decodedValue.length-RANDOM_KEY_SIZE-HASH_MAC_SIZE];
        System.arraycopy(decodedValue, RANDOM_KEY_SIZE, encodedMessage, 0, (decodedValue.length-RANDOM_KEY_SIZE-HASH_MAC_SIZE));
        System.out.println("The encoded message at decryption side is "+ Base64.encodeToString(encodedMessage,Base64.DEFAULT)+" its size is "+ encodedMessage.length);

        // verify iv and message by generating hash mac on iv and message
        SecretKeySpec keySpec = new SecretKeySpec(key_value, "HmacSHA1");
        Mac hmac = Mac.getInstance("HmacSHA1");
        hmac.init(keySpec);
        byte [] verify_mac = hmac.doFinal(iv_and_message);
        System.out.println("MAC key length at decryption is :"+ verify_mac.length );
        System.out.println("MAC at decryption is: "+ Base64.encodeToString(verify_mac, Base64.DEFAULT));

        //verify mac
        if(Arrays.equals(mac,verify_mac)){
            System.out.println("The encoded message at decryption side is "+encodedMessage+" its size is "+ encodedMessage.length);
            return  new String(c.doFinal(encodedMessage));
        } else {
            return "INVALID MESSAGE";
        }
    }

    private Key generateKey() throws Exception {
        Key key = new SecretKeySpec(key_value, ALGO);
        return key;
    }


}
