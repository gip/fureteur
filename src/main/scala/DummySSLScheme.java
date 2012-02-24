// /////////////////////////////////////////// //
// Fureteur - https://github.com/gip/fureteur  //
// /////////////////////////////////////////// //

package fureteur.dummyssl;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.conn.scheme.Scheme;

// Dummy Socket factory for SSL
public class DummySSLScheme {
 
    public static org.apache.http.conn.ssl.SSLSocketFactory getDummySSLScheme() {
       try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
  
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }
 
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }
 
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            X509HostnameVerifier verifier = new X509HostnameVerifier() {
 
                @Override
                public void verify(String string, SSLSocket ssls) throws IOException {
                }
 
                @Override
                public void verify(String string, X509Certificate xc) throws SSLException {
                }
 
                @Override
                public void verify(String string, String[] strings, String[] strings1) throws SSLException {
                }
 
                @Override
                public boolean verify(String string, SSLSession ssls) {
                    return true;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            org.apache.http.conn.ssl.SSLSocketFactory ssf = new org.apache.http.conn.ssl.SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(verifier);
            return ssf;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}