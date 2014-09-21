package com.northernwall.hadrian.formData;

import com.northernwall.hadrian.domain.Link;
import java.util.LinkedList;
import java.util.List;

public class VersionFormData {
    public String _id;
    public String api;
    public String status;
    public List<Link> links;
    public List<UsesFormData> uses1 = new LinkedList<>();
    public List<UsesFormData> uses2 = new LinkedList<>();
    public List<UsesFormData> uses3 = new LinkedList<>();
    
}
