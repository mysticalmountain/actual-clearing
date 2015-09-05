package com.cnepay.pos.clearing.conf

import com.cnepay.common.security.CnepayJCEHandler
import org.apache.commons.codec.binary.Base64
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 加载远程配置
 * User: andongxu
 * Time: 15-8-25 下午5:39 
 */
class RemoteConf {

    static Logger log = LoggerFactory.getLogger(RemoteConf)

    static initRomoteConfig() {
        log.info("读取远程配置开始.....")
        DefaultHttpClient httpClient = null
        try {
            httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("http://192.168.1.50/cfg/Product_POS/dev/actual-clearing")
            HttpResponse response = httpClient.execute(httpGet)
            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
            String output;
            String result = "";
            def map = [:]
            while ((output = br.readLine()) != null) {
                result += output;
                if (output.trim().length() > 0 && !output.startsWith("#")) {
                    int num = output.indexOf("=")
                    def key = output.substring(0, num).trim()
                    def value = output.substring(num + 1, output.length()).trim()
                    map.put(key, value)
                }
            }
            def provider = map.get("security.provider")
            map.each { entry ->
                if ("datasource.password".equals(entry.key)) {
                    def value = decript(entry.value, provider).trim()
                    println value
                    System.setProperty(entry.key, value)
                } else {
                    System.setProperty(entry.key, entry.value)
                }
            }
            log.info("读取远程配置完成.....")
        } catch (Exception e) {
            log.error("读取远程配置异常:${e.getMessage()}")
        } finally {
            httpClient.getConnectionManager().shutdown()
        }
    }

    static def decript(def passowrd, def provider) {
        def jce = new CnepayJCEHandler('com.sun.crypto.provider.SunJCE')
        def key = jce.toDESKey(128 as short, Base64.decodeBase64(provider.getBytes('utf8')))
        passowrd = jce.decryptString(passowrd, key)
        return passowrd
    }
}
