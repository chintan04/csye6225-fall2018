package com.csye6225;


import io.restassured.RestAssured;
import org.junit.Test;
import java.net.URI;
import java.net.URISyntaxException;

public class SampleTest {

    @Test
    public void simpleTest() throws Exception {
        System.out.println("Test Successful");
    }

    @Test
    public void testTimeUrl() throws URISyntaxException {
        RestAssured.when().get(new URI("/user/time")).then().statusCode(200);
    }

    @Test
    public void testTimeUrl2() throws URISyntaxException {
        RestAssured.when().get(new URI("/user/")).then().statusCode(200);
    }
    @Test
    public void wrongGetTimeurl() throws URISyntaxException {
        RestAssured.when().get(new URI("/time")).then().statusCode(404);
    }
}

