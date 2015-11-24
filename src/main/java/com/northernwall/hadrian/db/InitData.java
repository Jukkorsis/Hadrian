package com.northernwall.hadrian.db;

import com.northernwall.hadrian.domain.CustomFunction;
import com.northernwall.hadrian.domain.DataStore;
import com.northernwall.hadrian.domain.Host;
import com.northernwall.hadrian.domain.Service;
import com.northernwall.hadrian.domain.ServiceRef;
import com.northernwall.hadrian.domain.Team;
import com.northernwall.hadrian.domain.Vip;
import com.northernwall.hadrian.domain.VipRef;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitData {
    private final static Logger logger = LoggerFactory.getLogger(InitData.class);

    private InitData() {
    }

    public static void init(DataAccess dataAccess) {
        List<Team> teams = dataAccess.getTeams();
        if (teams == null || teams.isEmpty()) {
            logger.info("Creating the example data");
            
            Team team = new Team("IDS Team");
            team.getUsernames().add("richard");
            team.getUsernames().add("tony");
            dataAccess.saveTeam(team);

            Service ids = new Service("ids", "Identity Service", team.getTeamId(), "Stores user identities", "app-ids", "ids-service", "com.openmarket", "ids", "{host}.openmarket.com:9090/version", "{host}.openmarket.com:9090/availability", "start", "stop");
            dataAccess.saveService(ids);
            dataAccess.saveVip(new Vip("ids-80", ids.getServiceId(), "-", "ids.openmarket.com", false, "prd", "HTTP", 80, 8080));
            dataAccess.saveHost(new Host("wdc-prd-ids-001", ids.getServiceId(), "-", "wdc", "prd", "VM-Java8", "M"));
            dataAccess.saveHost(new Host("wdc-prd-ids-002", ids.getServiceId(), "-", "wdc", "prd", "VM-Java8", "M"));
            dataAccess.saveHost(new Host("vdc-prd-ids-001", ids.getServiceId(), "-", "vdc", "prd", "VM-Java8", "M"));
            dataAccess.saveHost(new Host("vdc-prd-ids-002", ids.getServiceId(), "-", "vdc", "prd", "VM-Java8", "M"));
            dataAccess.saveHost(new Host("ldc-prd-ids-001", ids.getServiceId(), "-", "ldc", "prd", "VM-Java8", "M"));
            dataAccess.saveHost(new Host("ldc-prd-ids-002", ids.getServiceId(), "-", "ldc", "prd", "VM-Java8", "M"));
            dataAccess.saveHost(new Host("adc-prd-ids-001", ids.getServiceId(), "-", "adc", "prd", "VM-Java8", "M"));
            dataAccess.saveHost(new Host("adc-prd-ids-002", ids.getServiceId(), "-", "adc", "prd", "VM-Java8", "M"));
            dataAccess.saveDataStore(new DataStore("scs_db", ids.getServiceId(), "MySQL", "prd"));

            Service cbt = new Service("ssam", "Self Service Account Management", team.getTeamId(), "elf Service Account Management", "app-ids", "cbt", "com.openmarket", "ssam", "{host}.openmarket.com:9090/version", "{host}.openmarket.com:9090/availability", "start", "stop");
            dataAccess.saveService(cbt);
            Vip ep1 = new Vip("ssam-80", cbt.getServiceId(), "-", "ssam.openmarket.com", false, "prd", "HTTP", 80, 8181);
            dataAccess.saveVip(ep1);
            Vip ep2 = new Vip("ssam-443", cbt.getServiceId(), "-", "ssam.openmarket.com", false, "prd", "HTTPS", 443, 81443);
            dataAccess.saveVip(ep2);
            Host si1 = new Host("wdc-prd-ssam-001", cbt.getServiceId(), "-", "wdc", "prd", "VM-Java8", "M");
            dataAccess.saveHost(si1);
            dataAccess.saveVipRef(new VipRef(si1.getHostId(), ep1.getVipId(), "-"));
            dataAccess.saveVipRef(new VipRef(si1.getHostId(), ep2.getVipId(), "-"));

            team = new Team("Platform Service Team");
            team.getUsernames().add("richard");
            team.getUsernames().add("ande");
            team.getUsernames().add("andrew");
            team.getUsernames().add("adam");
            team.getUsernames().add("dan");
            team.getUsernames().add("steven");
            team.getUsernames().add("tom");
            dataAccess.saveTeam(team);

            Service sqs = new Service("sqs", "Simple Queuing Service", team.getTeamId(), "Simple Queuing Service", "app-pst", "sqs", "com.openmarket.sqs", "sqs", "{host}.openmarket.com:7780/sqs/v1/version", "{host}.openmarket.com:7780/sqs/v1/availability", "start", "stop");
            dataAccess.saveService(sqs);
            dataAccess.saveVip(new Vip("sqs-80", sqs.getServiceId(), "-", "sqs.openmarket.com", false, "prd", "HTTP", 80, 7780));
            dataAccess.saveHost(new Host("wdc-prd-sqs-001", sqs.getServiceId(), "-", "wdc", "prd", "D-Java8", "S"));
            dataAccess.saveHost(new Host("wdc-prd-sqs-002", sqs.getServiceId(), "-", "wdc", "prd", "D-Java8", "S"));
            dataAccess.saveHost(new Host("vdc-prd-sqs-001", sqs.getServiceId(), "-", "vdc", "prd", "D-Java8", "S"));
            dataAccess.saveHost(new Host("vdc-prd-sqs-002", sqs.getServiceId(), "-", "vdc", "prd", "D-Java8", "S"));
            dataAccess.saveHost(new Host("ldc-prd-sqs-001", sqs.getServiceId(), "-", "ldc", "prd", "D-Java8", "S"));
            dataAccess.saveHost(new Host("ldc-prd-sqs-002", sqs.getServiceId(), "-", "ldc", "prd", "D-Java8", "S"));
            dataAccess.saveHost(new Host("adc-prd-sqs-001", sqs.getServiceId(), "-", "adc", "prd", "D-Java8", "S"));
            dataAccess.saveHost(new Host("adc-prd-sqs-002", sqs.getServiceId(), "-", "adc", "prd", "D-Java8", "S"));
            dataAccess.saveDataStore(new DataStore("sqs_cass", sqs.getServiceId(), "Cassandra", "prd"));
            dataAccess.saveCustomFunction(new CustomFunction(sqs.getServiceId(), "Version", "GET", "{host}.openmarket.com:7780/sqs/v1/version", "Shows the version of the selected hosts.", false));
            dataAccess.saveCustomFunction(new CustomFunction(sqs.getServiceId(), "Health", "GET", "{host}.openmarket.com:7780/sqs/v1/health", "Shows the health page of the selected hosts.", true));

            Service scs = new Service("scs", "Simple Configuration Service", team.getTeamId(), "Simple Configuration Service", "app-pst", "scs", "com.openmarket.scs", "scs-service", "{host}.openmarket.com:9090/version", "{host}.openmarket.com:9090/availability", "start", "stop");
            dataAccess.saveService(scs);
            Vip ep3 = new Vip("scs-80", scs.getServiceId(), "-", "scs.openmarket.com", false, "prd", "HTTP", 80, 8080);
            dataAccess.saveVip(ep3);
            Host si2 = new Host("wdc-prd-scs-001", scs.getServiceId(), "-", "wdc", "prd", "D-Java8", "S");
            dataAccess.saveHost(si2);
            dataAccess.saveVipRef(new VipRef(si2.getHostId(), ep3.getVipId(), "-"));
            dataAccess.saveDataStore(new DataStore("scs_cass", scs.getServiceId(), "Cassandra", "prd"));
            dataAccess.saveCustomFunction(new CustomFunction(scs.getServiceId(), "Clear Cache", "GET", "{host}.openmarket.com:9090/scs/v1/version", "Clears the cache of each selected hosts.", true));

            team = new Team("Reporting Team");
            team.getUsernames().add("lita");
            team.getUsernames().add("david");
            team.getUsernames().add("nicholas");
            team.getUsernames().add("anton");
            dataAccess.saveTeam(team);

            team = new Team("SMS Team");
            team.getUsernames().add("carl");
            team.getUsernames().add("scot");
            dataAccess.saveTeam(team);

            team = new Team("Messaging Team");
            team.getUsernames().add("rob");
            dataAccess.saveTeam(team);

            team = new Team("CMX2 Team");
            team.getUsernames().add("james");
            dataAccess.saveTeam(team);

            team = new Team("Push Team");
            team.getUsernames().add("lily");
            dataAccess.saveTeam(team);

            Service invoke = new Service("invoke", "Invoke", team.getTeamId(), "Used to invoke other internal services", "app-push", "invoke", "com.openmarket", "invoke", "{host}.openmarket.com:9090/version", "{host}.openmarket.com:9090/availability", "start", "stop");
            dataAccess.saveService(invoke);
            dataAccess.saveVip(new Vip("invoke-80", invoke.getServiceId(), "-", "invoke.openmarket.com", false, "prd", "HTTP", 80, 8080));
            dataAccess.saveHost(new Host("wdc-prd-invoke-001", invoke.getServiceId(), "-", "wdc", "prd", "VM-Java8", "L"));
            dataAccess.saveHost(new Host("vdc-prd-invoke-001", invoke.getServiceId(), "-", "vdc", "prd", "VM-Java8", "L"));

            dataAccess.saveServiceRef(new ServiceRef(cbt.getServiceId(), ids.getServiceId()));
            dataAccess.saveServiceRef(new ServiceRef(ids.getServiceId(), scs.getServiceId()));
            dataAccess.saveServiceRef(new ServiceRef(sqs.getServiceId(), scs.getServiceId()));
            dataAccess.saveServiceRef(new ServiceRef(invoke.getServiceId(), scs.getServiceId()));
            dataAccess.saveServiceRef(new ServiceRef(invoke.getServiceId(), sqs.getServiceId()));
            dataAccess.saveServiceRef(new ServiceRef(invoke.getServiceId(), ids.getServiceId()));
        }
    }

}
