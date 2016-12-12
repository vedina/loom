package net.idea.loom.pubchem.rest;

import com.fasterxml.jackson.databind.JsonNode;

public interface ICallback<OUT> {
    OUT processJSON(JsonNode node);
}
