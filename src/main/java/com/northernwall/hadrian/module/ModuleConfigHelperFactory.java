package com.northernwall.hadrian.module;

import com.northernwall.hadrian.parameters.Parameters;
import com.squareup.okhttp.OkHttpClient;

public interface ModuleConfigHelperFactory {

    ModuleConfigHelper create(Parameters parameters, OkHttpClient client);

}
