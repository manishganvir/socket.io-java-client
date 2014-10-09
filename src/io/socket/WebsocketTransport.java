package io.socket;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

// Manish - Adding support for cookie
import org.java_websocket.drafts.Draft_17;

class WebsocketTransport extends WebSocketClient implements IOTransport {
    private final static Pattern PATTERN_HTTP = Pattern.compile("^http");
    public static final String TRANSPORT_NAME = "websocket";
    private IOConnection connection;

	// Manish - Adding support for cookie
    // public static IOTransport create(URL url, IOConnection connection) {
    public static IOTransport create(URL url, IOConnection connection, java.util.Map<java.lang.String,java.lang.String> headers) {
        URI uri = URI.create(
                PATTERN_HTTP.matcher(url.toString()).replaceFirst("ws")
                + IOConnection.SOCKET_IO_1 + TRANSPORT_NAME
                + "/" + connection.getSessionId());

        // Manish - Adding support for cookie
    	// return new WebsocketTransport(uri, connection);
    	return new WebsocketTransport(uri, connection, headers);
    }

	public WebsocketTransport(URI uri, IOConnection connection, java.util.Map<java.lang.String,java.lang.String> headers) {
//        super(uri);
//        this.connection = connection;
//        try {
//			X509TrustManager tm = new X509TrustManager() {
//	            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
//	            }
//
//	            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
//	            }
//
//	            public X509Certificate[] getAcceptedIssuers() {
//	                return null;
//	            }
//	        };
//	        SSLContext ctx = SSLContext.getInstance("TLS");
//            ctx.init(null, null, null);
//
//	        if(("wss".equals(uri.getScheme()) || ("https".equals(uri.getScheme()))) && ctx != null) {
//                this.setSocket(ctx.getSocketFactory().createSocket());
//	        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // Manish - Adding support for cookie
    	// super(uri, new Draft());
    	super(uri, new Draft_17(), headers, 60);
        this.connection = connection;
        SSLContext context = IOConnection.getSslContext();
        if(context == null) {
            try {
                context = SSLContext.getInstance("TLS");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            try {
                context.init(null, null, null);
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        }
        if("wss".equals(uri.getScheme()) && context != null) {
            try {
                this.setSocket(context.getSocketFactory().createSocket());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //connection.
        
    }

    /* (non-Javadoc)
     * @see io.socket.IOTransport#disconnect()
     */
    @Override
    public void disconnect() {
        try {
            this.close();
        } catch (Exception e) {
            connection.transportError(e);
        }
    }

    /* (non-Javadoc)
     * @see io.socket.IOTransport#canSendBulk()
     */
    @Override
    public boolean canSendBulk() {
        return false;
    }

    /* (non-Javadoc)
     * @see io.socket.IOTransport#sendBulk(java.lang.String[])
     */
    @Override
    public void sendBulk(String[] texts) throws IOException {
        throw new RuntimeException("Cannot send Bulk!");
    }

    /* (non-Javadoc)
     * @see io.socket.IOTransport#invalidate()
     */
    @Override
    public void invalidate() {
        connection = null;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if(connection != null)
            connection.transportDisconnected();
    }

    @Override
    public void onMessage(String text) {
        if(connection != null)
            connection.transportMessage(text);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        if(connection != null)
            connection.transportConnected();
    }

    @Override
    public String getName() {
        return TRANSPORT_NAME;
    }

    @Override
    public void onError(Exception ex) {
        // TODO Auto-generated method stub

    }
}