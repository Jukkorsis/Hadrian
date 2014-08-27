package com.northernwall.hadrian.formData;

import com.northernwall.hadrian.domain.Link;
import java.util.List;

public class VersionFormData {
    public String _id;
    public String api;
    public String impl;
    public String status;
    public List<Link> links;
    public List<UsesFormData> uses1;
    public List<UsesFormData> uses2;
    
}
