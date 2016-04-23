package supermicro.chu.redfish;

/**
 * Created by Chu on 3/23/2016.
 */
import android.util.Log;

import javax.net.ssl.HostnameVerifier ;
import javax.net.ssl.SSLSession;

public class NullHostNameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        Log.i("RestUtilImpl", "Approving certificate for " + hostname);
        return true;
    }

}
