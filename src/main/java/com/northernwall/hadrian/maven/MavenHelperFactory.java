package com.northernwall.hadrian.maven;

import com.squareup.okhttp.OkHttpClient;
import java.util.Properties;

/**
 *
 * @author rthursto
 */
public interface MavenHelperFactory {
    public MavenHelper create(Properties properties, OkHttpClient client);
    
}
