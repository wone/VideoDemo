package com.example.videodemo.proxy;

import android.util.Log;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Http处理类
 *
 */
public class HttpParser {
    final static public String TAG = "VideoDemo.HttpParser";
    final static private String RANGE_PARAMS = "Range: bytes=";
    final static private String RANGE_PARAMS_0 = "Range: bytes=0-";


    private static final int HEADER_BUFFER_LENGTH_MAX = 1024 * 10;
    private byte[] headerBuffer = new byte[HEADER_BUFFER_LENGTH_MAX];
    private int headerBufferLength = 0;

    /**
     * 链接带的端口
     */
    private int remotePort = -1;
    /**
     * 远程服务器地址
     */
    private String remoteHost;
    /**
     * 代理服务器使用的端口
     */
    private int localPort;
    /**
     * 本地服务器地址
     */
    private String localHost;

    public class ProxyRequest {
        /**
         * Http Request 内容
         */
        public String _body;
        /**
         * 对应的预加载文件路径
         */
        public String _prebufferFilePath;
        /**
         * 是否从0开始请求数据
         */
        public boolean _isReqRange0;
    }

    public HttpParser(String rHost, int rPort, String lHost, int lPort) {
        remoteHost = rHost;
        remotePort = rPort;
        localHost = lHost;
        localPort = lPort;
    }

    public void clearHttpBody() {
        headerBuffer = new byte[HEADER_BUFFER_LENGTH_MAX];
        headerBufferLength = 0;

        Log.d(TAG, "clearHttpBody");
    }


    /**
     * Request报文解析转换ProxyRequest
     *
     * @param requestBuffer
     * @return
     */
    public ProxyRequest getProxyRequest(byte[] requestBuffer) {
        ProxyRequest result = new ProxyRequest();
        //获取Body
        result._body = new String(requestBuffer);

        // 把request中的本地ip改为远程ip
        result._body = result._body.replace(localHost, remoteHost);

        // 把代理服务器端口改为原URL端口
        if (remotePort == -1) {
            String replace = result._body.replace(":" + localPort, "");
            result._body = replace;
        }
        else
            result._body = result._body.replace(":" + localPort, ":" + remotePort);

        //不带Ranage则添加上，方便后面处理
        if (result._body.contains(RANGE_PARAMS) == false)
            result._body = result._body.replace(C.HTTP_BODY_END,
                    "\r\n" + RANGE_PARAMS_0 + C.HTTP_BODY_END);

        Log.e(TAG, "getProxyRequest:  _body=" + result._body);

        // 获取文件名
        String fileName = getURIPath(result._body);
        fileName = ProxyUtils.urlToFileName(fileName);
        result._prebufferFilePath = C.getBufferDir() + "/" + fileName;
        Log.e(TAG, "getProxyRequest:  _prebufferFilePath:" + result._prebufferFilePath);

        // 判断是否带有Rage:bytes -0从0开始,或者不带Range
        result._isReqRange0 = result._body.contains(RANGE_PARAMS_0);

        Log.e(TAG, "getProxyRequest:  _isReqRange0:" + result._isReqRange0);
        return result;
    }

    /**
     * 获取Request报文
     *
     * @param source
     * @param length
     * @return
     */
    public byte[] getRequestBody(byte[] source, int length) {
        List<byte[]> httpRequest = getHttpBody(C.HTTP_REQUEST_BEGIN,
                C.HTTP_BODY_END,
                source,
                length);
        if (httpRequest.size() > 0) {
            return httpRequest.get(0);
        }
        return null;
    }

    /**
     * 获取Response报文
     *
     * @param source
     * @param length
     * @return [0]:Response报文；[1]:Response报文后的二进制数据
     */
    public List<byte[]> getResponseBody(byte[] source, int length) {
        List<byte[]> httpResponse = getHttpBody(C.HTTP_RESPONSE_BEGIN,
                C.HTTP_BODY_END,
                source,
                length);

        return httpResponse;
    }

    /**
     * 替换Request报文中的Range位置
     *
     * @param requestStr
     * @param position
     * @return
     */
    public String modifyRequestRange(String requestStr, int position) {
        int startIndex = requestStr.indexOf(RANGE_PARAMS);
        int endIndex = requestStr.indexOf("\r\n", startIndex);
        String str = requestStr.substring(startIndex, endIndex);
        String result = requestStr.replaceAll(str, RANGE_PARAMS + position + "-");
        return result;
    }

    private List<byte[]> getHttpBody(String beginStr, String endStr, byte[] source, int length) {
        if ((headerBufferLength + length) >= headerBuffer.length) {
            clearHttpBody();
        }

        System.arraycopy(source, 0, headerBuffer, headerBufferLength, length);
        headerBufferLength += length;

        List<byte[]> result = new ArrayList<byte[]>();
        String responseStr = new String(headerBuffer);

        Log.d(TAG, "getHttpBody：str = " + responseStr);

        if (responseStr.contains(beginStr)
                && responseStr.contains(endStr)) {
            int startIndex = responseStr.indexOf(beginStr, 0);
            int endIndex = responseStr.indexOf(endStr, startIndex);
            endIndex += endStr.length();

            byte[] header = new byte[endIndex - startIndex];
            System.arraycopy(headerBuffer, startIndex, header, 0, header.length);
            result.add(header);

            if (headerBufferLength > header.length) {//还有数据
                byte[] other = new byte[headerBufferLength - header.length];
                System.arraycopy(headerBuffer, header.length, other, 0, other.length);
                result.add(other);
            }
            Log.e(TAG, "getHttpBody：total:" + headerBufferLength + ",header.length:" + header.length);
            clearHttpBody();
        }

        return result;
    }

    private String getURIPath(String requestStr) {
        try {
            int startIndex = requestStr.indexOf(C.HTTP_REQUEST_BEGIN)
                    + C.HTTP_REQUEST_BEGIN.length();
            int endIndex = requestStr.indexOf(C.HTTP_REQUEST_LINE1_END);
            String result = requestStr.substring(startIndex, endIndex);
            result = "http://127.0.0.1" + result;
            URI uri = new URI(result);
            return uri.getPath();
        } catch (Exception e) {
            return null;
        }
    }
}
