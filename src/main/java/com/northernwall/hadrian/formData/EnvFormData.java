package com.northernwall.hadrian.formData;

import com.northernwall.hadrian.domain.Host;
import java.util.LinkedList;
import java.util.List;

public class EnvFormData {
    public String _id;
    public String name;
    public String vip;
    public List<Host> hosts = new LinkedList<>();
    
}
