package org.example.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.Map;

public class Utils {
    private static final String protocol = "http";

    public static String getServiceBaseUrl(String serviceMarathonPath) {

        Response response = RestAssured.given()
                .auth().basic(getPropertyOfName("mLogin"), getPropertyOfName("mPassword"))
                .baseUri(protocol + getPropertyOfName("mUri"))
                .basePath(serviceMarathonPath)
                .get("?embed=app.taskStats&embed=app.readiness")
                .then()
                .statusCode(200)
                .extract().response();
        //Возвращает первый найденный объект с информацией о работающем инстансе.
        Map<String, ?> state = response.getBody().path("app.tasks.find { it.state == 'TASK_RUNNING' }");

        if (state != null) {
            String host = state.get("host").toString();
            @SuppressWarnings("unchecked")
            ArrayList<String> ports = (ArrayList<String>) state.get("ports");
            String uri = response.getBody().path("app.env.SERVICE_NAME").toString();
            return String.format("%s://%s:%s/%s", protocol, host, ports.get(0), uri);
        }

        throw new NullPointerException("Отсутствуют работающие инстансы для " + serviceMarathonPath);
    }

    private static String getPropertyOfName(String mLogin) {
        return "";
    }


}
