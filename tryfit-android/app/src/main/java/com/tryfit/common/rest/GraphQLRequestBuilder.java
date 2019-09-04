package com.tryfit.common.rest;

/**
 * Created by alexeyreznik on 28/09/2017.
 */

public class GraphQLRequestBuilder {

    public static GraphQLRequest buildGetClientInfoRequest(String clientId) {
        String query = "{\n" +
                "client(id:\"" + clientId + "\"){\n" +//client
                "id, \n" +
                "name,\n" +
                "surname, \n" +
                "email, \n" +
                "contractorID,\n" +
                "scan {\n" +//scan
                "id,\n" +
                "clientID,\n" +
                "scannerID,\n" +
                "created,\n" +
                "updated,\n" +
                "leftMeasures,\n" +
                "rightMeasures,\n" +
                "weight,\n" +
                "deleted\n" +
                "}\n" +//scan
                "scan2D {\n" +//scan2D
                "id,\n" +
                "clientID,\n" +
                "scannerID,\n" +
                "created,\n" +
                "updated,\n" +
                "leftMeasures,\n" +
                "rightMeasures,\n" +
                "weight,\n" +
                "deleted\n" +
                "}\n" +//scan2D
                "}\n" +//client
                "}\n";
        return new GraphQLRequest(query);
    }

    public static GraphQLRequest buildGetClientFittingsRequest(String clientId, String groupId, int start, int limit) {
        String query = "query($filter: [FilterInput], $sort: [SortInput]) {\n" +
                "  clientFitting(\n" +
                "    filter: $filter,\n" +
                "    sort: $sort,\n" +
                "    start: " + start + ",\n" +
                "    limit: " + limit + ",\n" +
                "    clientID: \"" + clientId + "\"\n" +
                "  ) {\n" +
                "    total,\n" +
                "    items {\n" +
                "    product {\n" +
                "      id, code, name, pictures, price, sex, groupID, sizes {\n" +
                "        value, fitrate, fitrateABS, available }\n" +
                "      },\n" +
                "    size {\n" +
                "      value, fitrate, fitrateABS, available}\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String queryParams = "{\n" +
                "  \"filter\": [{\"field\": \"product.groupID\", \"query\": \"" + groupId + "\"}, {\"field\":  \"size.available\", \"query\": \"true\"}]," +
                "  \"sort\": [{\"field\": \"size.fitrateAbs\", \"dir\": \"desc\"}]\n" +
                "}";
        return new GraphQLRequest(query, queryParams);
    }

    public static GraphQLRequest buildGetGroupsRequest(String contractorId) {
        String query = "query{groups(contractorID:\"" + contractorId + "\") {\n" +
                "  id,\n" +
                "  name\n" +
                "}}";
        return new GraphQLRequest(query);
    }

    public static GraphQLRequest buildLoginInPluginRequest(String clientId, String sid) {
        String query = "query{\n" +
                "  clientLoginInPlugin(\n" +
                "    clientID: \"" + clientId + "\" , \n" +
                "    sessionID: \"" + sid + "\"){\n" +
                "    id\n" +
                "  }\n" +
                "}";
        return new GraphQLRequest(query);
    }
}
