package com.tryfit.common.rest;

/**
 * Created by alexeyreznik on 27/09/2017.
 */

public class GraphQLRequest {
    private String query;
    private String variables;

    public GraphQLRequest(String query) {
        this.query = query;
    }

    public GraphQLRequest(String query, String variables) {
        this.query = query;
        this.variables = variables;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }
}
