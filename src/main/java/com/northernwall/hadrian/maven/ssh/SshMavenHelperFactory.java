package com.northernwall.hadrian.maven.ssh;

import com.northernwall.hadrian.maven.MavenHelper;
import com.northernwall.hadrian.maven.MavenHelperFactory;
import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;

public class SshMavenHelperFactory implements MavenHelperFactory {

    @Override
    public MavenHelper create(Parameters parameters, OkHttpClient client) {
        return new SshMavenHelper(parameters);
    }

}
