package com.aliyun.tea;

import com.aliyun.tea.utils.X509TrustManagerImp;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class TeaTest {
    @Test
    public void init() {
        Assert.assertNotNull(new Tea());
    }

    @Test
    public void composeUrlTest() throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        Method composeUrl = Tea.class.getDeclaredMethod("composeUrl", TeaRequest.class);
        composeUrl.setAccessible(true);
        TeaRequest request = new TeaRequest();
        Map<String, String> map = new HashMap<>();
        map.put("test", null);
        map.put("host", "test");
        request.headers = map;
        request.pathname = "/test";
        request.protocol = null;
        request.query = map;
        String str = (String) composeUrl.invoke(Tea.class, request);
        Assert.assertEquals("http://test/test?host=test", str);

        request.query = new HashMap<>();
        request.pathname = null;
        str = (String) composeUrl.invoke(Tea.class, request);
        Assert.assertEquals("http://test", str);

        request.query = new HashMap<>();
        request.query.put("test", "and");
        request.pathname = "?test";
        request.protocol = "HTTP";
        str = (String) composeUrl.invoke(Tea.class, request);
        Assert.assertEquals("HTTP://test?test&test=and", str);
    }

    @Test
    public void doActionTest() throws Exception {
        TeaRequest request = new TeaRequest();
        Map<String, String> map = new HashMap<>();
        map.put("host", "www.baidu.com");
        request.protocol = "http";
        request.headers = map;
        request.method = "post";
        Map<String, Object> runtimeOptions = new HashMap<>();
        TeaResponse response = Tea.doAction(request, runtimeOptions);
        Assert.assertNotNull(response.getResponse());

        request.protocol = "https";
        request.body = new ByteArrayInputStream("{}".getBytes("UTF-8"));
        runtimeOptions.put("readTimeout", "50000");
        runtimeOptions.put("connectTimeout", "50000");
        response = Tea.doAction(request, runtimeOptions);
        Assert.assertNotNull(response.getResponse());

        request.body = new ByteArrayInputStream("{}".getBytes("UTF-8"));
        request.method = "get";
        response = Tea.doAction(request, runtimeOptions);
        Assert.assertNotNull(response.getResponse());

        response = Tea.doAction(request, runtimeOptions, "test");
        Assert.assertEquals(200, response.statusCode);
        response = Tea.doAction(request, runtimeOptions, "test");
        Assert.assertEquals(200, response.statusCode);
    }

    @Test
    public void toUpperFirstCharTest() {
        String name = Tea.toUpperFirstChar("word");
        Assert.assertEquals("Word", name);
    }

    @Test
    public void createSSLSocketFactoryTest() throws Exception {
        X509Certificate x509Certificates = mock(X509Certificate.class);
        Tea tea = new Tea();
        X509TrustManagerImp x509 = new X509TrustManagerImp();
        x509.getAcceptedIssuers();
        x509.checkServerTrusted(new X509Certificate[]{x509Certificates}, "test");
        x509.checkClientTrusted(new X509Certificate[]{x509Certificates}, "test1");
        SSLSocketFactory sslSocketFactory = Whitebox.invokeMethod(tea, "createSSLSocketFactory");
        Assert.assertNotNull(sslSocketFactory);
    }

    @Test
    public void getBackoffTimeTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("policy", "");
        int number = Tea.getBackoffTime(map, 88);
        Assert.assertEquals(0, number);

        map.put("policy", "no");
        number = Tea.getBackoffTime(map, 88);
        Assert.assertEquals(0, number);

        map.put("policy", "one");
        map.put("period", null);
        number = Tea.getBackoffTime(map, 88);
        Assert.assertEquals(0, number);

        map.put("policy", "one");
        map.put("period", 0);
        number = Tea.getBackoffTime(map, 88);
        Assert.assertEquals(88, number);

        map.put("policy", "one");
        map.put("period", 66);
        number = Tea.getBackoffTime(map, 88);
        Assert.assertEquals(66, number);
    }

    @Test
    public void sleepTest() throws InterruptedException {
        long start = System.currentTimeMillis();
        Tea.sleep(100);
        long end = System.currentTimeMillis();
        Assert.assertTrue(end - start >= 100);
    }

    @Test
    public void isRetryableTest() {
        Assert.assertFalse(Tea.isRetryable(new RuntimeException()));
    }

    @Test
    public void allowRetryTest() {
        Map<String, Object> map = null;
        Assert.assertFalse(Tea.allowRetry(map, 6, 6L));

        map = new HashMap<>();
        map.put("maxAttempts", null);
        Assert.assertFalse(Tea.allowRetry(map, 6, 6L));

        map.put("maxAttempts", 8);
        Assert.assertTrue(Tea.allowRetry(map, 6, 6L));
    }

    @Test
    public void toReadableTest() throws IOException {
        String str = "readable test";
        InputStream inputStream = Tea.toReadable(str);
        byte[] bytes = new byte[1024];
        int index = inputStream.read(bytes);
        String result = new String(bytes, 0, index);
        Assert.assertTrue(str.equals(result));
    }
}
