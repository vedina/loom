package net.idea.loom.pubchem.rest;

import org.codehaus.jackson.JsonNode;

public interface ICallback<OUT> {
    OUT processJSON(JsonNode node);
}
