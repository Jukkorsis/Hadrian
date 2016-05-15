package com.northernwall.hadrian.db.inMemory;

import com.northernwall.hadrian.domain.ModuleRef;
import java.util.function.Predicate;

public class ModuleRefPredicate implements Predicate<ModuleRef> {

    private final String clientServiceId;
    private final String clientModuleId;
    private final String serverServiceId;
    private final String serverModuleId;

    public ModuleRefPredicate(String clientServiceId, String clientModuleId, String serverServiceId, String serverModuleId) {
        this.clientServiceId = clientServiceId;
        this.clientModuleId = clientModuleId;
        this.serverServiceId = serverServiceId;
        this.serverModuleId = serverModuleId;
    }

    @Override
    public boolean test(ModuleRef t) {
        return t.getClientServiceId().equals(clientServiceId)
                && t.getClientModuleId().equals(clientModuleId)
                && t.getServerServiceId().equals(serverServiceId)
                && t.getServerModuleId().equals(serverModuleId);
    }

}
