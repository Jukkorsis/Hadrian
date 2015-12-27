package com.northernwall.hadrian.maven;

import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;

/**
 *
 * @author rthursto
 */
public interface MavenHelperFactory {
    public MavenHelper create(Parameters parameters, OkHttpClient client);
    
}
