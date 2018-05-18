package east.orientation.caster.cnjy21.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import east.orientation.caster.cnjy21.constant.APIConstant;


/**
 * 签名辅助类
 * 
 * @author zhoushubing
 *
 */
public class SignatureHelper {
	

	/**
	 * 生成签名
	 * 
	 * @param params
	 *            签名参数
	 * @param secret
	 *            签名密钥
	 * @return 签名值
	 */
	public static String generator(Map<String, String> params, String secret) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		StringBuffer strBuffer = new StringBuffer();
		Object value;
		for (String key : keys) {
			value = params.get(key);
			if (!key.equals(APIConstant.PARAM_SIGN) && value != null)
				strBuffer.append(key).append("=").append(value).append("&");
		}
		if (strBuffer.length() > 0) {
			strBuffer.deleteCharAt(strBuffer.length() - 1);
		}
		String md5val = MD5.md5(strBuffer.toString());
		return hashHmac(md5val,secret).toUpperCase();
	}

	public static String hashHmac(String text, String secret) {
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			String hash = Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(text.getBytes()));
			return hash;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
//	public static void main(String[] args) {
//		String s = MD5.md5("access-key=21cnjy1CEWkVCfrpLxQr6N&chapterId=7752&page=1&salt=0.5752473579918589&stage=1&subjectId=2&timestamp=1516350493&title=但是法");
//		System.out.println(hashHmac(s,"02w7g8eem6hpxtiMJnbxj63K1eYLT88z"));
//	}
}
