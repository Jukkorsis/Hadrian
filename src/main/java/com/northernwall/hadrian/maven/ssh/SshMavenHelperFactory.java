package com.northernwall.hadrian.maven.ssh;

import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.maven.MavenHelperFactory;
import com.squareup.okhttp.OkHttpClient;
import java.util.Properties;

public class SshMavenHelperFactory implements MavenHelperFactory {

    @Override
    public MavenHelper create(Properties properties, OkHttpClient client) {
        return new SshMavenHelper(properties);
    }

}
