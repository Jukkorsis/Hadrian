package com.northernwall.hadrian.maven.http;

import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.maven.MavenHelperFactory;
import com.squareup.okhttp.OkHttpClient;
import java.util.Properties;

public class HttpMavenHelperFactory implements MavenHelperFactory {

    @Override
    public MavenHelper create(Properties properties, OkHttpClient client) {
        return new HttpMavenHelper(properties, client);
    }

}
