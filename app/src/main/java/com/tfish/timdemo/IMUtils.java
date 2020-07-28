package com.tfish.timdemo;


import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.Deflater;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

//即时通讯工具类
public class IMUtils {
	//腾讯云 SDKAppId 它是腾讯云用于区分客户的唯一标识。
	public static final int SDKAPPID = 1400393010;
	//签名过期时间，建议不要设置的过短
	private static final int EXPIRETIME = 604800;
	//计算签名用的加密密钥
	private static final String SECRETKEY = "7838bcf89dce5a3abb8004e18e6b1d9c02932cdce5f029b098262463e0313132";

	public static String genUserSig(String userId) {
		return GenTLSSignature(SDKAPPID, userId, EXPIRETIME, null, SECRETKEY);
	}

	/**
	 * 生成 tls 票据
	 * @param sdkappid    应用的 appid
	 * @param userId      用户 id
	 * @param expire      有效期，单位是秒
	 * @param userbuf     默认填写null
	 * @param priKeyContent 生成 tls 票据使用的私钥内容
	 * @return 如果出错，会返回为空，或者有异常打印，成功返回有效的票据
	 */
	private static String GenTLSSignature(long sdkappid, String userId, long expire, byte[] userbuf, String priKeyContent) {
		if (TextUtils.isEmpty(priKeyContent)) {
			return "";
		}
		long currTime = System.currentTimeMillis() / 1000;
		JSONObject sigDoc = new JSONObject();
		try {
			sigDoc.put("TLS.ver", "2.0");
			sigDoc.put("TLS.identifier", userId);
			sigDoc.put("TLS.sdkappid", sdkappid);
			sigDoc.put("TLS.expire", expire);
			sigDoc.put("TLS.time", currTime);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String base64UserBuf = null;
		if (null != userbuf) {
			base64UserBuf = Base64.encodeToString(userbuf, Base64.NO_WRAP);
			try {
				sigDoc.put("TLS.userbuf", base64UserBuf);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		String sig = hmacsha256(sdkappid, userId, currTime, expire, priKeyContent, base64UserBuf);
		if (sig.length() == 0) {
			return "";
		}
		try {
			sigDoc.put("TLS.sig", sig);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Deflater compressor = new Deflater();
		compressor.setInput(sigDoc.toString().getBytes(Charset.forName("UTF-8")));
		compressor.finish();
		byte[] compressedBytes = new byte[2048];
		int compressedBytesLength = compressor.deflate(compressedBytes);
		compressor.end();
		return new String(base64EncodeUrl(Arrays.copyOfRange(compressedBytes, 0, compressedBytesLength)));
	}


	private static String hmacsha256(long sdkappid, String userId, long currTime, long expire, String priKeyContent, String base64Userbuf) {
		String contentToBeSigned = "TLS.identifier:" + userId + "\n"
				+ "TLS.sdkappid:" + sdkappid + "\n"
				+ "TLS.time:" + currTime + "\n"
				+ "TLS.expire:" + expire + "\n";
		if (null != base64Userbuf) {
			contentToBeSigned += "TLS.userbuf:" + base64Userbuf + "\n";
		}
		try {
			byte[] byteKey = priKeyContent.getBytes("UTF-8");
			Mac hmac = Mac.getInstance("HmacSHA256");
			SecretKeySpec keySpec = new SecretKeySpec(byteKey, "HmacSHA256");
			hmac.init(keySpec);
			byte[] byteSig = hmac.doFinal(contentToBeSigned.getBytes("UTF-8"));
			return new String(Base64.encode(byteSig, Base64.NO_WRAP));
		} catch (UnsupportedEncodingException e) {
			return "";
		} catch (NoSuchAlgorithmException e) {
			return "";
		} catch (InvalidKeyException e) {
			return "";
		}
	}


	private static byte[] base64EncodeUrl(byte[] input) {
		byte[] base64 = new String(Base64.encode(input, Base64.NO_WRAP)).getBytes();
		for (int i = 0; i < base64.length; ++i)
			switch (base64[i]) {
				case '+':
					base64[i] = '*';
					break;
				case '/':
					base64[i] = '-';
					break;
				case '=':
					base64[i] = '_';
					break;
				default:
					break;
			}
		return base64;
	}
}
