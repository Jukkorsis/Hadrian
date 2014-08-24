package com.northernwall.hadrian.domain;

import com.northernwall.hadrian.formData.Node;
import com.northernwall.hadrian.formData.Edge;
import java.util.LinkedList;
import java.util.List;

public class Network {
    public List<Node> nodes = new LinkedList<>();
    public List<Edge> edges = new LinkedList<>();
}
