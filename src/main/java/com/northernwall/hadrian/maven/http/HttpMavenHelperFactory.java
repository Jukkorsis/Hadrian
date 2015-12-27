package com.northernwall.hadrian.maven.http;

import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.maven.MavenHelperFactory;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;

public class HttpMavenHelperFactory implements MavenHelperFactory {

    @Override
    public MavenHelper create(Parameters parameters, OkHttpClient client) {
        return new HttpMavenHelper(parameters, client);
    }

}
