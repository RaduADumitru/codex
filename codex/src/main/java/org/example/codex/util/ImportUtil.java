package org.example.codex.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;
import java.util.*;

public class ImportUtil {
    private static ImportUtil instance = null;
    private ObjectMapper objectMapper;
    private OkHttpClient okHttpClient;
    private String credentials;
    private String baseRequestUrl;
    private ImportUtil() {
        objectMapper = new ObjectMapper();
        okHttpClient = new OkHttpClient();
        credentials = Credentials.basic("root", "openSesame");
        baseRequestUrl = "http://localhost:8529/_db/dex/_api/";
    }
    public static ImportUtil getInstance() {
        if(instance == null) {
            instance = new ImportUtil();
        }
        return instance;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        instance.objectMapper = objectMapper;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getBaseRequestUrl() {
        return baseRequestUrl;
    }

    public void setBaseRequestUrl(String baseRequestUrl) {
        this.baseRequestUrl = baseRequestUrl;
    }

    public static void sendCreateRequest(String statementText) {

    }
    public static void sendInsertRequest(String statementText) {

    }

    public static void createCollection(String collectionName, boolean isEdgeCollection, String schema) throws IOException {
        String requestURL = ImportUtil.getInstance().getBaseRequestUrl() + "collection";
        ObjectNode createRequestJsonObject = ImportUtil.getInstance().getObjectMapper().createObjectNode();
        createRequestJsonObject.put("name", collectionName);
        if(isEdgeCollection) {
            //2 - document collection, 3 - edge collection
            createRequestJsonObject.put("type", 3);
        }
        if(schema != null) {
            JsonNode schemaTree = ImportUtil.getInstance().getObjectMapper().readTree(schema);
            System.out.println("SCHEMA TREE: " + schemaTree.toString());
            createRequestJsonObject.set("schema", schemaTree);
            }
        String jsonString = ImportUtil.getInstance().getObjectMapper().writeValueAsString(createRequestJsonObject);
//        System.out.println("Create request string: " + jsonString);
        RequestBody createFormBody = FormBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
        Request createRequest = new Request.Builder()
                .url(requestURL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .header("Authorization", ImportUtil.getInstance().getCredentials())
                .post(createFormBody)
                .build();
        Call call = ImportUtil.getInstance().getOkHttpClient().newCall(createRequest);
        Response createResponse = call.execute();
        System.out.println("Created collection " + collectionName + ": " + Objects.requireNonNull(createResponse.body()).string());
        createResponse.close();
    }
    public static void deleteCollections(List<String> collections) throws IOException {
        for(String col : collections) {
            String deleteURL = ImportUtil.getInstance().getBaseRequestUrl() + "collection/" + col;
            Request deleteRequest = new Request.Builder()
                    .url(deleteURL)
                    .addHeader("Accept", "application/json")
                    .header("Authorization", ImportUtil.getInstance().getCredentials())
                    .delete()
                    .build();
            Call call = ImportUtil.getInstance().getOkHttpClient().newCall(deleteRequest);
            Response response = call.execute();
            response.close();
        }
    }
}
